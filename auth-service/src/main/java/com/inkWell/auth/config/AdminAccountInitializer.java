package com.inkWell.auth.config;

import com.inkWell.auth.domain.entity.User;
import com.inkWell.auth.domain.enums.Plan;
import com.inkWell.auth.domain.enums.Provider;
import com.inkWell.auth.domain.enums.Role;
import com.inkWell.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Value("${initial.admin.email}")
    private String adminEmail;

    @org.springframework.beans.factory.annotation.Value("${initial.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .username("inkwell_admin")
                    .fullName("InkWell Admin")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .plan(Plan.PRO)
                    .provider(Provider.LOCAL)
                    .isActive(true)
                    .build();

            userRepository.save(admin);
            System.out.println("Default admin account created: " + adminEmail);
        } else {
            // Ensure existing admin account has ADMIN role
            userRepository.findByEmail(adminEmail).ifPresent(user -> {
                if (user.getRole() != Role.ADMIN) {
                    user.setRole(Role.ADMIN);
                    userRepository.save(user);
                    System.out.println("Updated existing user to ADMIN role: " + adminEmail);
                }
            });
        }
    }
}
