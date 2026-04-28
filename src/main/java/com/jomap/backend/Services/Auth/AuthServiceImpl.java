package com.jomap.backend.Services.Auth;

//import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
//import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Auth.Login.LoginRequest;
import com.jomap.backend.DTOs.Auth.Login.LoginResponse;
import com.jomap.backend.DTOs.Auth.Register.RegisterRequest;
import com.jomap.backend.DTOs.Auth.Register.RegisterResponse;
import com.jomap.backend.DTOs.Auth.social.FacebookUserResponse;
import com.jomap.backend.Entities.Users.Role;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.Profile.UserProfile;
import com.jomap.backend.Entities.Users.Profile.UserProfileRepository;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Services.Notefications.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final UserProfileRepository userProfileRepository;
//    @Value("${app.google.client-id}")
//    private String googleClientId;

    @Override
    @Transactional
    public ApiResponse<RegisterResponse> register(RegisterRequest request) {

        if (request.getPhoneNumber() == null ||
                (!request.getPhoneNumber().matches("^\\+9627\\d{8}$")
                        && !request.getPhoneNumber().matches("^07\\d{8}$"))) {
            return ApiResponse.error("Phone number must be like +9627XXXXXXXX or 07XXXXXXXX");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            return ApiResponse.error("UserName is already used");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            return ApiResponse.error("Phone number is already used");
        }

        User user = new User();
        user.setEmail(request.getEmail().trim());
        user.setUsername(request.getUsername().trim());
        user.setPhoneNumber(request.getPhoneNumber().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        userProfileRepository.save(profile);

        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }

        RegisterResponse response = new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUsername(),
                savedUser.getRole().name()
        );

        return ApiResponse.success("Registered successfully", response);
    }
    //new edits

    @Override
    @Transactional
    public ApiResponse<LoginResponse> login(LoginRequest request) {

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return ApiResponse.error("User with this email does not exist");
        }

        User user = optionalUser.get();

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            return ApiResponse.error("Incorrect password");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            return ApiResponse.error("This user account is not active");
        }

        if (!userProfileRepository.existsByUserId(user.getId())) {
            UserProfile profile = new UserProfile();
            profile.setUser(user);
            userProfileRepository.save(profile);
        }
        try {
            emailService.sendLoginSuccessEmail(user.getEmail(), user.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String token = jwtService.generateToken(user.getEmail());

        LoginResponse response = new LoginResponse(
                token,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );

        return ApiResponse.success("Logged in successfully", response);
    }
//    @Override
//    public ApiResponse<LoginResponse> loginWithGoogle(String idTokenString) {
//        try {
//            if (idTokenString == null || idTokenString.isBlank()) {
//                return new ApiResponse<>(false, "Google token is required", null);
//            }
//
//            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
//                    new NetHttpTransport(),
//                    GsonFactory.getDefaultInstance()
//            )
//                    .setAudience(Collections.singletonList(googleClientId))
//                    .build();
//
//            GoogleIdToken idToken = verifier.verify(idTokenString);
//
//            if (idToken == null) {
//                return new ApiResponse<>(false, "Invalid Google token", null);
//            }
//
//            GoogleIdToken.Payload payload = idToken.getPayload();
//
//            String email = payload.getEmail();
//            String username = (String) payload.get("name");
//            String picture = (String) payload.get("picture");
//
//            if (email == null || email.isBlank()) {
//                return new ApiResponse<>(false, "Google account email not found", null);
//            }
//
//            User user = userRepository.findByEmail(email)
//                    .orElseGet(() -> createSocialUser(email, username, picture));
//
//            ensureUserProfileExists(user);
//
//            String token = jwtService.generateToken(user.getEmail());
//
//            LoginResponse response = new LoginResponse(
//                    token,
//                    "Bearer",
//                    user.getId(),
//                    user.getEmail(),
//                    user.getUsername(),
//                    user.getPhoneNumber()
//            );
//
//            return new ApiResponse<>(true, "Google login successful", response);
//
//        } catch (Exception e) {
//            log.error("Google login failed: {}", e.getMessage());
//            return new ApiResponse<>(false, "Google login failed", null);
//        }
//    }
//
//    @Override
//    public ApiResponse<LoginResponse> loginWithFacebook(String accessToken) {
//        try {
//            if (accessToken == null || accessToken.isBlank()) {
//                return new ApiResponse<>(false, "Facebook token is required", null);
//            }
//
//            String url = "https://graph.facebook.com/me"
//                    + "?fields=id,name,email,picture"
//                    + "&access_token=" + accessToken;
//
//            RestTemplate restTemplate = new RestTemplate();
//            FacebookUserResponse fbUser = restTemplate.getForObject(url, FacebookUserResponse.class);
//
//            if (fbUser == null || fbUser.email() == null || fbUser.email().isBlank()) {
//                return new ApiResponse<>(false, "Facebook account email not found", null);
//            }
//
//            String pictureUrl;
//            if (fbUser.picture() != null && fbUser.picture().data() != null) {
//                pictureUrl = fbUser.picture().data().url();
//            } else {
//                pictureUrl = null;
//            }
//
//            User user = userRepository.findByEmail(fbUser.email())
//                    .orElseGet(() -> createSocialUser(fbUser.email(), fbUser.name(), pictureUrl));
//
//            ensureUserProfileExists(user);
//
//            String token = jwtService.generateToken(user.getEmail());
//
//            LoginResponse response = new LoginResponse(
//                    token,
//                    "Bearer",
//                    user.getId(),
//                    user.getEmail(),
//                    user.getUsername(),
//                    user.getPhoneNumber()
//            );
//
//            return new ApiResponse<>(true, "Facebook login successful", response);
//
//        } catch (Exception e) {
//            log.error("Facebook login failed: {}", e.getMessage());
//            return new ApiResponse<>(false, "Facebook login failed", null);
//        }
//    }
//    private User createSocialUser(String email, String username, String profileImageUrl) {
//        User user = new User();
//
//        user.setEmail(email);
//        user.setUsername(generateUniqueUsername(username, email));
//        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
//        user.setRole(Role.USER);
//        user.setProfileImageUrl(profileImageUrl);
//
//        return userRepository.save(user);
//    }
//
//    private void ensureUserProfileExists(User user) {
//        if (!userProfileRepository.existsByUserId(user.getId())) {
//            UserProfile profile = new UserProfile();
//            profile.setUser(user);
//            userProfileRepository.save(profile);
//        }
//    }
//
//    private String generateUniqueUsername(String name, String email) {
//        String baseUsername;
//
//        if (name != null && !name.isBlank()) {
//            baseUsername = name.trim()
//                    .replaceAll("\\s+", "_")
//                    .replaceAll("[^a-zA-Z0-9_]", "");
//        } else {
//            baseUsername = email.substring(0, email.indexOf("@"));
//        }
//
//        if (baseUsername.isBlank()) {
//            baseUsername = "user";
//        }
//
//        String username = baseUsername;
//        int counter = 1;
//
//        while (userRepository.existsByUsername(username)) {
//            username = baseUsername + "_" + counter;
//            counter++;
//        }
//
//        return username;
//    }
}