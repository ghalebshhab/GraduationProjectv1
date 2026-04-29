package com.jomap.backend.Config;

import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Entities.Users.Role;
import com.jomap.backend.Services.Auth.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public OAuth2LoginSuccessHandler(
            UserRepository userRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String registrationId = extractRegistrationId(request);
        OAuthUserInfo userInfo = extractUserInfo(registrationId, oauthUser.getAttributes());

        String email = userInfo.email();

        if (email == null || email.isBlank() || "null".equalsIgnoreCase(email)) {
            email = userInfo.providerId() + "@facebook.local";
        }

        OAuthUserInfo fixedUserInfo = new OAuthUserInfo(
                userInfo.providerId(),
                email,
                userInfo.name(),
                userInfo.picture()
        );

        User user = findOrCreateUser(fixedUserInfo, registrationId);

        String token = jwtService.generateToken(user.getEmail());

        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        response.sendRedirect(frontendUrl + "/oauth-success?token=" + encodedToken);
    }

    private String extractRegistrationId(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri.contains("google")) {
            return "GOOGLE";
        }

        if (uri.contains("facebook")) {
            return "FACEBOOK";
        }

        return "UNKNOWN";
    }

    private OAuthUserInfo extractUserInfo(String provider, Map<String, Object> attributes) {

        if ("GOOGLE".equals(provider)) {
            String providerId = String.valueOf(attributes.get("sub"));
            String email = String.valueOf(attributes.get("email"));
            String name = String.valueOf(attributes.get("name"));
            String picture = attributes.get("picture") == null ? null : String.valueOf(attributes.get("picture"));

            return new OAuthUserInfo(providerId, email, name, picture);
        }

        if ("FACEBOOK".equals(provider)) {
            String providerId = String.valueOf(attributes.get("id"));
            String email = attributes.get("email") == null ? null : String.valueOf(attributes.get("email"));
            String name = attributes.get("name") == null ? "Facebook User" : String.valueOf(attributes.get("name"));

            return new OAuthUserInfo(providerId, email, name, null);
        }

        return new OAuthUserInfo(null, null, null, null);
    }

    private User findOrCreateUser(OAuthUserInfo userInfo, String provider) {

        Optional<User> byProvider =
                userRepository.findByProviderAndProviderId(provider, userInfo.providerId());

        if (byProvider.isPresent()) {
            return byProvider.get();
        }

        Optional<User> byEmail = userRepository.findByEmail(userInfo.email());

        if (byEmail.isPresent()) {
            User existingUser = byEmail.get();

            existingUser.setProvider(provider);
            existingUser.setProviderId(userInfo.providerId());

            return userRepository.save(existingUser);
        }

        User newUser = new User();

        newUser.setEmail(userInfo.email());
        newUser.setUsername(generateUsername(userInfo.name(), userInfo.email()));
        newUser.setRole(Role.USER);
        newUser.setIsActive(true);
        newUser.setProfileImageUrl(userInfo.picture());

        newUser.setProvider(provider);
        newUser.setProviderId(userInfo.providerId());

        /*
         If your password column is nullable=false,
         set a random encoded password.
        */
        newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));

        /*
         Optional:
         If your User has profileImage field:
         newUser.setProfileImage(userInfo.picture());
        */

        return userRepository.save(newUser);
    }

    private String generateUsername(String name, String email) {

        String base;

        if (name != null && !name.isBlank() && !"null".equals(name)) {
            base = name.toLowerCase().replaceAll("[^a-z0-9]", "");
        } else {
            base = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        }

        if (base.isBlank()) {
            base = "user";
        }

        String username = base;
        int counter = 1;

        while (userRepository.findByUsername(username).isPresent()) {
            username = base + counter;
            counter++;
        }

        return username;
    }

    private record OAuthUserInfo(
            String providerId,
            String email,
            String name,
            String picture
    ) {
    }
}