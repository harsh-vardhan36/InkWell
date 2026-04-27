package com.inkWell.auth.resource;

import com.inkWell.auth.domain.entity.User;
import com.inkWell.auth.domain.enums.Provider;
import com.inkWell.auth.domain.enums.Role;
import com.inkWell.auth.dto.AuthResponse;
import com.inkWell.auth.dto.LoginRequest;
import com.inkWell.auth.repository.UserRepository;
import com.inkWell.auth.security.JwtUtil;
import com.inkWell.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthResource.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password");
        AuthResponse authResponse = AuthResponse.builder()
                .token("access-token")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"));
    }

    @Test
    void shouldReturnUserProfile() throws Exception {
        String token = "valid.jwt.token";
        String email = "test@example.com";
        User user = User.builder()
                .email(email)
                .fullName("Test User")
                .role(Role.READER)
                .provider(Provider.LOCAL)
                .isActive(true)
                .build();

        when(jwtUtil.extractEmail(anyString())).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/auth/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.role").value("READER"));
    }

    @Test
    void shouldReturnErrorWhenUserNotFound() throws Exception {
        String token = "valid.jwt.token";
        String email = "notfound@example.com";

        when(jwtUtil.extractEmail(anyString())).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        mockMvc.perform(get("/auth/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
