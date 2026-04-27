package com.inkWell.auth.security;

import com.inkWell.auth.domain.entity.User;
import com.inkWell.auth.domain.enums.Provider;
import com.inkWell.auth.domain.enums.Role;
import com.inkWell.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        return processOAuth2User(registrationId, oAuth2User);
    }

    private OAuth2User processOAuth2User(String registrationId, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        if (email == null && "github".equals(registrationId)) {
            // GitHub might not provide email in the main attributes if it's private
            // For simplicity, we assume it's there or use login as fallback email-like id
            email = (String) attributes.get("login") + "@github.com";
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setFullName(name != null ? name : user.getFullName());
        } else {
            user = User.builder()
                    .email(email)
                    .username(email.split("@")[0] + "_" + System.currentTimeMillis())
                    .fullName(name != null ? name : "User")
                    .passwordHash("") // No password for OAuth users
                    .provider(Provider.valueOf(registrationId.toUpperCase()))
                    .role(Role.READER)
                    .isActive(true)
                    .build();
        }
        userRepository.save(user);
        
        return oAuth2User;
    }
}
