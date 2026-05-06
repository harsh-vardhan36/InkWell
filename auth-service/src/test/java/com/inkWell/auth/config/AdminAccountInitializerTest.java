package com.inkWell.auth.config;

import com.inkWell.auth.domain.entity.User;
import com.inkWell.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAccountInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminAccountInitializer adminAccountInitializer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminAccountInitializer, "adminEmail", "admin@test.com");
        ReflectionTestUtils.setField(adminAccountInitializer, "adminPassword", "password");
    }

    @Test
    void run_NewAdmin_CreatesAccount() {
        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashed");

        adminAccountInitializer.run();

        verify(userRepository).save(any(User.class));
    }

    @Test
    void run_ExistingAdmin_DoesNotCreateAccount() {
        User existingAdmin = new User();
        existingAdmin.setRole(com.inkWell.auth.domain.enums.Role.ADMIN);
        when(userRepository.existsByEmail("admin@test.com")).thenReturn(true);
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(existingAdmin));

        adminAccountInitializer.run();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_ExistingUserNotAdmin_UpdatesRole() {
        User user = new User();
        user.setRole(com.inkWell.auth.domain.enums.Role.READER);
        when(userRepository.existsByEmail("admin@test.com")).thenReturn(true);
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

        adminAccountInitializer.run();

        verify(userRepository).save(user);
        assertEquals(com.inkWell.auth.domain.enums.Role.ADMIN, user.getRole());
    }
}
