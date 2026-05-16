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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.security.SecureRandom;

@Service
@org.springframework.transaction.annotation.Transactional
public class AuthServiceImpl implements AuthService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RabbitTemplate rabbitTemplate;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;
    private final com.inkWell.auth.repository.FollowRepository followRepository;
    private final org.springframework.web.client.RestTemplate restTemplate;

    public AuthServiceImpl(UserRepository userRepository, 
                           PasswordEncoder passwordEncoder, 
                           JwtUtil jwtUtil, 
                           RabbitTemplate rabbitTemplate, 
                           org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate, 
                           com.inkWell.auth.repository.FollowRepository followRepository, 
                           org.springframework.web.client.RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
        this.followRepository = followRepository;
        this.restTemplate = restTemplate;
    }

    @Value("${internal.secret}")
    private String internalSecret;

    @Value("${newsletter.service.url:http://newsletter-service:8087}")
    private String newsletterServiceUrl;

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
        return String.valueOf(SECURE_RANDOM.nextInt(900000) + 100000);
    }

    private String generate4DigitOTP() {
        return String.valueOf(SECURE_RANDOM.nextInt(9000) + 1000);
    }

    @Override
    public void followUser(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            throw new RuntimeException("You cannot follow yourself");
        }
        if (!followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
            com.inkWell.auth.domain.entity.Follow follow = com.inkWell.auth.domain.entity.Follow.builder()
                    .followerId(followerId)
                    .followedId(followedId)
                    .status("PENDING")
                    .build();
            followRepository.save(follow);
        }
    }

    @Override
    public void unfollowUser(Long followerId, Long followedId) {
        followRepository.deleteByFollowerIdAndFollowedId(followerId, followedId);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followedId) {
        if (followerId == null || followedId == null) return false;
        return followRepository.existsByFollowerIdAndFollowedId(followerId, followedId);
    }

    @Override
    public long getFollowerCount(Long userId) {
        return followRepository.countByFollowedId(userId);
    }

    @Override
    public long getFollowingCount(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    @Override
    public List<User> getFollowers(Long userId) {
        List<com.inkWell.auth.domain.entity.Follow> follows = followRepository.findAllByFollowedId(userId);
        List<Long> followerIds = follows.stream()
                .map(com.inkWell.auth.domain.entity.Follow::getFollowerId)
                .toList();
        return userRepository.findAllById(followerIds);
    }

    @Override
    public void approveFollow(Long followerId, Long followedId) {
        com.inkWell.auth.domain.entity.Follow follow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId)
                .orElseThrow(() -> new RuntimeException("Follow request not found"));
        
        follow.setStatus("APPROVED");
        followRepository.save(follow);

        // Sync with Newsletter service
        try {
            User follower = userRepository.findById(followerId).orElse(null);
            if (follower != null) {
                String baseUrl = newsletterServiceUrl;
                if ("http://newsletter-service:8087".equals(baseUrl)) {
                    try {
                        java.net.InetAddress.getByName("newsletter-service");
                    } catch (java.net.UnknownHostException e) {
                        baseUrl = "http://localhost:8087";
                    }
                }
                String newsletterUrl = baseUrl + "/newsletter/subscribe?email=" + follower.getEmail();
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.set("X-Internal-Secret", internalSecret);
                org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
                restTemplate.postForEntity(newsletterUrl, entity, String.class);
                log.info("Synced follower {} to newsletter service", follower.getEmail());
            }
        } catch (Exception e) {
            log.error("Failed to sync with newsletter service: {}", e.getMessage());
        }
    }

    @Override
    public void rejectFollow(Long followerId, Long followedId) {
        followRepository.deleteByFollowerIdAndFollowedId(followerId, followedId);
    }

    @Override
    public String getFollowStatus(Long followerId, Long followedId) {
        return followRepository.findByFollowerIdAndFollowedId(followerId, followedId)
                .map(com.inkWell.auth.domain.entity.Follow::getStatus)
                .orElse("NONE");
    }
}
