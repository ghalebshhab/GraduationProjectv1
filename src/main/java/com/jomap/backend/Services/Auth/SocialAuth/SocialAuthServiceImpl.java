package com.jomap.backend.Services.Auth.SocialAuth;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Auth.Login.LoginResponse;
import com.jomap.backend.DTOs.Auth.social.SocialUserInfo;
import com.jomap.backend.Entities.Auth.AuthProvider;
import com.jomap.backend.Entities.Users.Role;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Services.Auth.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialAuthServiceImpl implements SocialAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleAuthService googleAuthService;
    private final FacebookAuthService facebookAuthService;

    @Override
    public ApiResponse<LoginResponse> loginWithGoogle(String token) {

        ApiResponse<SocialUserInfo> googleResponse = googleAuthService.verifyGoogleToken(token);

        if (!googleResponse.isSuccess()) {
            return new ApiResponse<>(false, googleResponse.getMessage(), null);
        }

        ApiResponse<User> userResponse = findOrCreateSocialUser(
                googleResponse.getData(),
                AuthProvider.GOOGLE
        );

        if (!userResponse.isSuccess()) {
            return new ApiResponse<>(false, userResponse.getMessage(), null);
        }

        User user = userResponse.getData();

        String jwt = jwtService.generateToken(user.getEmail());

        LoginResponse loginResponse = buildLoginResponse(
                user,
                jwt,
                "Google login successful"
        );

        return new ApiResponse<>(true, "Google login successful", loginResponse);
    }

    @Override
    public ApiResponse<LoginResponse> loginWithFacebook(String token) {

        ApiResponse<SocialUserInfo> facebookResponse = facebookAuthService.verifyFacebookToken(token);

        if (!facebookResponse.isSuccess()) {
            return new ApiResponse<>(false, facebookResponse.getMessage(), null);
        }

        ApiResponse<User> userResponse = findOrCreateSocialUser(
                facebookResponse.getData(),
                AuthProvider.FACEBOOK
        );

        if (!userResponse.isSuccess()) {
            return new ApiResponse<>(false, userResponse.getMessage(), null);
        }

        User user = userResponse.getData();

        String jwt = jwtService.generateToken(user.getEmail());

        LoginResponse loginResponse = buildLoginResponse(
                user,
                jwt,
                "Facebook login successful"
        );

        return new ApiResponse<>(true, "Facebook login successful", loginResponse);
    }

    private ApiResponse<User> findOrCreateSocialUser(SocialUserInfo info, AuthProvider provider) {

        if (info == null) {
            return new ApiResponse<>(false, "Social user info is missing", null);
        }

        if (info.getProviderId() == null || info.getProviderId().isBlank()) {
            return new ApiResponse<>(false, "Provider id is missing", null);
        }

        if (info.getEmail() == null || info.getEmail().isBlank()) {
            return new ApiResponse<>(false, "Email is missing", null);
        }

        Optional<User> existingByProvider =
                userRepository.findByProviderAndProviderId(provider, info.getProviderId());

        if (existingByProvider.isPresent()) {
            return new ApiResponse<>(true, "User found by provider", existingByProvider.get());
        }

        Optional<User> existingByEmail = userRepository.findByEmail(info.getEmail());

        if (existingByEmail.isPresent()) {
            User user = existingByEmail.get();

            /*
             If user registered normally before, we link his account with Google/Facebook.
            */
            user.setProvider(provider);
            user.setProviderId(info.getProviderId());

            User savedUser = userRepository.save(user);

            return new ApiResponse<>(true, "Existing user linked with social account", savedUser);
        }

        User newUser = new User();

        newUser.setEmail(info.getEmail());
        newUser.setUsername(generateUniqueUsername(info.getName(), info.getEmail()));
        newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setRole(Role.USER);
        newUser.setProvider(provider);
        newUser.setProviderId(info.getProviderId());

        User savedUser = userRepository.save(newUser);

        return new ApiResponse<>(true, "New social user created", savedUser);
    }

    private LoginResponse buildLoginResponse(User user, String jwt, String message) {

        return new LoginResponse(
                jwt,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );
    }

    private String generateUniqueUsername(String name, String email) {

        String baseUsername;

        if (name != null && !name.isBlank()) {
            baseUsername = name
                    .trim()
                    .replaceAll("\\s+", "")
                    .replaceAll("[^a-zA-Z0-9]", "")
                    .toLowerCase();
        } else {
            baseUsername = email.substring(0, email.indexOf("@"))
                    .replaceAll("[^a-zA-Z0-9]", "")
                    .toLowerCase();
        }

        if (baseUsername.isBlank()) {
            baseUsername = "user";
        }

        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}