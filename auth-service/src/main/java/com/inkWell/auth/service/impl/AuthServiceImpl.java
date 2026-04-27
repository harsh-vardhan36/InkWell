package com.inkWell.auth.service.impl;

import com.inkWell.auth.config.RabbitConfig;
import com.inkWell.auth.domain.entity.User;
import com.inkWell.auth.domain.enums.Provider;
import com.inkWell.auth.domain.enums.Role;
import com.inkWell.auth.dto.*;
import com.inkWell.auth.exception.UserAlreadyExistsException;
import com.inkWell.auth.repository.UserRepository;
import com.inkWell.auth.security.JwtUtil;
import com.inkWell.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class AuthServiceImpl implements AuthService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RabbitTemplate rabbitTemplate;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    @Override
    public AuthResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
            if (existingUser.isActive()) {
                throw new UserAlreadyExistsException("Email already registered");
            } else {
                // If the user exists but is not active, we delete the pending account and start over,
                // or just remove it from DB to allow re-registration.
                userRepository.delete(existingUser);
            }
        });

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken");
        }

        User user = User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.READER)
                .provider(Provider.LOCAL)
                .isActive(false) // Account starts as INACTIVE
                .build();

        userRepository.save(user);

        // Generate and store OTP in Redis (5 minute expiry)
        String otp = generateOTP();
        log.info("Generated OTP for {}: {}", user.getEmail(), otp);
        redisTemplate.opsForValue().set("otp:" + user.getEmail(), otp, java.time.Duration.ofMinutes(5));

        // Push notification task to RabbitMQ
        NotificationRequest notification = NotificationRequest.builder()
                .to(user.getEmail())
                .subject("✍️ Verify your InkWell Account")
                .type("VERIFICATION")
                .userId(user.getUserId())
                .data(Map.of(
                        "fullName", user.getFullName(),
                        "otp", otp))
                .build();

        rabbitTemplate.convertAndSend(RabbitConfig.NOTIFICATION_EXCHANGE, RabbitConfig.NOTIFICATION_ROUTING_KEY,
                notification);

        return AuthResponse.builder()
                .message("Registration successful. Please verify your email with the OTP sent.")
                .build();
    }

    @Override
    public void verifyOTP(String email, String otp) {
        String cachedOtp = (String) redisTemplate.opsForValue().get("otp:" + email);
        if (cachedOtp == null || !cachedOtp.equals(otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(true);
        userRepository.save(user);
        redisTemplate.delete("otp:" + email);
    }

    @Override
    public void resendOTP(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isActive()) {
            throw new RuntimeException("Account is already verified");
        }

        String otp = generateOTP();
        log.info("Resending OTP for {}: {}", email, otp);
        redisTemplate.opsForValue().set("otp:" + email, otp, java.time.Duration.ofMinutes(5));

        NotificationRequest notification = NotificationRequest.builder()
                .to(email)
                .subject("✍️ Resend: Verify your InkWell Account")
                .type("VERIFICATION")
                .userId(user.getUserId())
                .data(Map.of(
                        "fullName", user.getFullName(),
                        "otp", otp))
                .build();

        rabbitTemplate.convertAndSend(RabbitConfig.NOTIFICATION_EXCHANGE, RabbitConfig.NOTIFICATION_ROUTING_KEY,
                notification);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is not verified. Please verify your email first.");
        }

        NotificationRequest loginNotification = NotificationRequest.builder()
                .to(user.getEmail())
                .subject("New Login Detected")
                .type("LOGIN_ALERT")
                .userId(user.getUserId())
                .data(Map.of("fullName", user.getFullName()))
                .build();

        rabbitTemplate.convertAndSend(RabbitConfig.NOTIFICATION_EXCHANGE, RabbitConfig.NOTIFICATION_ROUTING_KEY,
                loginNotification);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .build();
    }

    @Override
    public void logout(String token) {
        long expiration = jwtUtil.getRemainingExpiration(token);
        if (expiration > 0) {
            redisTemplate.opsForValue().set("blacklist:" + token, "true", java.time.Duration.ofMillis(expiration));
        }
    }

    @Override
    public void validateToken(String token) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token))) {
            throw new RuntimeException("Token has been revoked (logged out)");
        }
        if (!jwtUtil.isTokenValid(token)) {
            throw new RuntimeException("Invalid or expired token");
        }
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        String email = jwtUtil.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        String newToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return AuthResponse.builder().token(newToken).build();
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = getUserById(userId);
        if (request.getFullName() != null)
            user.setFullName(request.getFullName());
        if (request.getBio() != null)
            user.setBio(request.getBio());
        if (request.getAvatarUrl() != null)
            user.setAvatarUrl(request.getAvatarUrl());
        userRepository.save(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getUserById(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Incorrect current password");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public List<User> searchUsers(String username) {
        return userRepository.searchUsers(username, null, null);
    }

    @Override
    public void requestDeactivation(Long userId) {
        User user = getUserById(userId);
        String otp = generate4DigitOTP();
        log.info("Generated Deactivation OTP for {}: {}", user.getEmail(), otp);
        redisTemplate.opsForValue().set("deactivate_otp:" + user.getUserId(), otp, java.time.Duration.ofMinutes(10));

        NotificationRequest notification = NotificationRequest.builder()
                .to(user.getEmail())
                .subject("⚠️ Confirm Account Deactivation")
                .type("ACCOUNT_DEACTIVATION")
                .userId(user.getUserId())
                .data(Map.of(
                        "fullName", user.getFullName(),
                        "otp", otp))
                .build();

        rabbitTemplate.convertAndSend(RabbitConfig.NOTIFICATION_EXCHANGE, RabbitConfig.NOTIFICATION_ROUTING_KEY,
                notification);
    }

    @Override
    public void verifyDeactivation(Long userId, String otp) {
        String cachedOtp = (String) redisTemplate.opsForValue().get("deactivate_otp:" + userId);
        if (cachedOtp == null || !cachedOtp.equals(otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = getUserById(userId);
        userRepository.delete(user);
        redisTemplate.delete("deactivate_otp:" + userId);
        log.info("User {} (ID: {}) has been deleted from the database", user.getEmail(), userId);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = generateOTP();
        redisTemplate.opsForValue().set("reset_otp:" + user.getEmail(), otp, java.time.Duration.ofMinutes(10));

        NotificationRequest resetNotification = NotificationRequest.builder()
                .to(user.getEmail())
                .subject("✍️ InkWell Password Reset")
                .type("PASSWORD_RESET")
                .userId(user.getUserId())
                .data(Map.of(
                        "fullName", user.getFullName(),
                        "otp", otp))
                .build();

        rabbitTemplate.convertAndSend(RabbitConfig.NOTIFICATION_EXCHANGE, RabbitConfig.NOTIFICATION_ROUTING_KEY,
                resetNotification);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail();
        String cachedOtp = (String) redisTemplate.opsForValue().get("reset_otp:" + email);

        if (cachedOtp == null || !cachedOtp.equals(request.getToken())) {
            throw new RuntimeException("Invalid or expired reset OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        redisTemplate.delete("reset_otp:" + email);
    }

    @Override
    public void requestBecomeAuthor(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.AUTHOR || user.getRole() == Role.ADMIN) {
            throw new RuntimeException("User is already an author or admin");
        }

        String otp = generate4DigitOTP();
        log.info("Generated Become-Author OTP for {}: {}", email, otp);
        redisTemplate.opsForValue().set("become_author_otp:" + email, otp, java.time.Duration.ofMinutes(10));

        NotificationRequest notification = NotificationRequest.builder()
                .to(email)
                .subject("✍️ Become an Author at InkWell")
                .type("AUTHOR_VERIFICATION")
                .userId(user.getUserId())
                .data(Map.of(
                        "fullName", user.getFullName(),
                        "otp", otp))
                .build();

        rabbitTemplate.convertAndSend(RabbitConfig.NOTIFICATION_EXCHANGE, RabbitConfig.NOTIFICATION_ROUTING_KEY,
                notification);
    }

    @Override
    public void verifyBecomeAuthorOtp(String email, String otp) {
        String cachedOtp = (String) redisTemplate.opsForValue().get("become_author_otp:" + email);
        if (cachedOtp == null || !cachedOtp.equals(otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(Role.AUTHOR);
        userRepository.save(user);
        redisTemplate.delete("become_author_otp:" + email);
        log.info("User {} is now an AUTHOR", email);
    }

    private String generateOTP() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    private String generate4DigitOTP() {
        return String.valueOf(new Random().nextInt(9000) + 1000);
    }
}
