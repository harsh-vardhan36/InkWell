package com.inkWell.auth.resource;

import com.inkWell.auth.domain.entity.User;
import com.inkWell.auth.domain.enums.Plan;
import com.inkWell.auth.dto.CreateOrderRequest;
import com.inkWell.auth.dto.VerifyPaymentRequest;
import com.inkWell.auth.repository.UserRepository;
import com.inkWell.auth.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

/**
 * PaymentResource — Razorpay integration endpoints
 *
 * Flow:
 *  1.  POST /auth/payment/create-order  → frontend sends plan type
 *                                         backend returns { orderId, amount, currency, key }
 *  2.  Razorpay SDK pops up in the browser
 *  3.  POST /auth/payment/verify        → frontend sends { orderId, paymentId, signature, plan }
 *                                         backend HMAC-verifies the signature and upgrades plan
 */
@RestController
@RequestMapping("/auth/payment")
@RequiredArgsConstructor
public class PaymentResource {

    private static final Logger log = LoggerFactory.getLogger(PaymentResource.class);

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    // ── Plan amounts in paise (1 INR = 100 paise) ─────────────────────────────
    private static final Map<String, Long> PLAN_AMOUNTS = Map.of(
        "PRO_TRIAL",   200L,        // ₹2
        "PRO_MONTHLY", 59900L,      // ₹599
        "PRO_YEARLY",  49900L * 12  // ₹498/mo × 12 = ₹5,988
    );

    // ── Plan durations in days ─────────────────────────────────────────────────
    private static final Map<String, Integer> PLAN_DAYS = Map.of(
        "PRO_TRIAL",   15,
        "PRO_MONTHLY", 30,
        "PRO_YEARLY",  365
    );

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Create Razorpay order
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
            HttpServletRequest httpRequest,
            @RequestBody CreateOrderRequest body) {

        User user = getAuthenticatedUser(httpRequest);

        String planKey = body.getPlan();
        if (!PLAN_AMOUNTS.containsKey(planKey)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid plan: " + planKey));
        }

        long amountPaise = PLAN_AMOUNTS.get(planKey);
        String currency  = body.getCurrency() != null ? body.getCurrency() : "INR";

        // Generate a stable receipt id so idempotency is easy to reason about
        String receiptId = "ink_" + user.getUserId() + "_" + UUID.randomUUID().toString().substring(0, 8);

        // Build Razorpay order via REST API (we avoid the Java SDK to keep the
        // pom.xml lean — Razorpay REST API requires no extra library).
        try {
            org.json.JSONObject orderPayload = new org.json.JSONObject();
            orderPayload.put("amount",   amountPaise);
            orderPayload.put("currency", currency);
            orderPayload.put("receipt",  receiptId);
            orderPayload.put("notes",    new org.json.JSONObject()
                    .put("userId", user.getUserId().toString())
                    .put("email",  user.getEmail())
                    .put("plan",   planKey));

            // Call Razorpay Orders API
            String endpoint = "https://api.razorpay.com/v1/orders";
            java.net.URL url = java.net.URI.create(endpoint).toURL();
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            // Basic Auth: key_id:key_secret
            String credentials = razorpayKeyId + ":" + razorpayKeySecret;
            String encoded = java.util.Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + encoded);

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(orderPayload.toString().getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            String responseBody;
            try (java.io.InputStream is = (status < 400) ? conn.getInputStream() : conn.getErrorStream();
                 java.io.BufferedReader reader = new java.io.BufferedReader(
                         new java.io.InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                responseBody = sb.toString();
            }

            if (status != 200) {
                log.error("Razorpay order creation failed: {}", responseBody);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("message", "Payment gateway error. Please try again."));
            }

            org.json.JSONObject order = new org.json.JSONObject(responseBody);

            return ResponseEntity.ok(Map.of(
                    "orderId",  order.getString("id"),
                    "amount",   order.getLong("amount"),
                    "currency", order.getString("currency"),
                    "keyId",    razorpayKeyId,
                    "name",     user.getFullName(),
                    "email",    user.getEmail(),
                    "plan",     planKey
            ));

        } catch (Exception e) {
            log.error("Error creating Razorpay order", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Failed to create payment order: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Verify payment & upgrade plan
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            HttpServletRequest httpRequest,
            @RequestBody VerifyPaymentRequest body) {

        User user = getAuthenticatedUser(httpRequest);

        try {
            // Razorpay signature verification:
            // HMAC-SHA256( orderId + "|" + paymentId , keySecret )
            String message = body.getRazorpayOrderId() + "|" + body.getRazorpayPaymentId();
            String expectedSignature = hmacSha256(message, razorpayKeySecret);

            if (!expectedSignature.equalsIgnoreCase(body.getRazorpaySignature())) {
                log.warn("Signature mismatch for user {}. Expected: {} Got: {}",
                        user.getEmail(), expectedSignature, body.getRazorpaySignature());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Payment verification failed: invalid signature."));
            }

            // Signature OK — upgrade the plan
            String planKey = body.getPlan();
            int days = PLAN_DAYS.getOrDefault(planKey, 30);

            user.setPlan(Plan.PRO);
            user.setPlanExpiry(LocalDateTime.now().plusDays(days));
            userRepository.save(user);

            log.info("✅ User {} upgraded to PRO (plan={}, expiry={})",
                    user.getEmail(), planKey, user.getPlanExpiry());

            return ResponseEntity.ok(Map.of(
                    "message",    "Payment verified! Your Pro plan is now active.",
                    "plan",       "PRO",
                    "planExpiry", user.getPlanExpiry().toString()
            ));

        } catch (Exception e) {
            log.error("Error verifying payment for user {}", user.getEmail(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Verification error: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Get current plan status
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/status")
    public ResponseEntity<?> planStatus(HttpServletRequest httpRequest) {
        User user = getAuthenticatedUser(httpRequest);

        // Auto-downgrade if plan expired
        if (user.getPlan() == Plan.PRO
                && user.getPlanExpiry() != null
                && user.getPlanExpiry().isBefore(LocalDateTime.now())) {
            user.setPlan(Plan.FREE);
            user.setPlanExpiry(null);
            userRepository.save(user);
            log.info("Plan expired for {} — downgraded to FREE", user.getEmail());
        }

        return ResponseEntity.ok(Map.of(
                "plan",       user.getPlan().name(),
                "planExpiry", user.getPlanExpiry() != null ? user.getPlanExpiry().toString() : ""
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────
    private User getAuthenticatedUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String hmacSha256(String data, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
