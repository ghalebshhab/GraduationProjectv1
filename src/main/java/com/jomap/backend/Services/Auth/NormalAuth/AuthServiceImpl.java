package com.jomap.backend.Services.Auth.NormalAuth;

//import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
//import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.gson.GsonFactory;
import com.jomap.backend.Services.Auth.JwtService;
import lombok.RequiredArgsConstructor;
import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Auth.Login.LoginRequest;
import com.jomap.backend.DTOs.Auth.Login.LoginResponse;
import com.jomap.backend.DTOs.Auth.Register.RegisterRequest;
import com.jomap.backend.DTOs.Auth.Register.RegisterResponse;
import com.jomap.backend.Entities.Users.Role;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.Profile.UserProfile;
import com.jomap.backend.Entities.Users.Profile.UserProfileRepository;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Services.Notefications.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final UserProfileRepository userProfileRepository;
    // @Value("${app.google.client-id}")
    // private String googleClientId;
    private static final List<String> ALLOWED_EMAIL_DOMAINS = List.of(
            "gmail.com",
            "hotmail.com",
            "outlook.com",
            "yahoo.com",
            "icloud.com",
            "jomap.com"
    );

    @Override
    @Transactional
    public ApiResponse<RegisterResponse> register(RegisterRequest request) {

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ApiResponse.error("Email is required");
        }

        if (request.getUsername() == null || !request.getUsername().matches("^[a-z](?!(?:.*_){2})(?!(?:.*\\.){2})[a-z0-9_.]*$")) {
            return ApiResponse.error("Username must start with a letter, contain only lowercase letters/numbers, and use at most one dot and one underscore");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (!isAllowedEmailDomain(normalizedEmail)) {
            return ApiResponse.error("Email domain is not allowed");
        }

        if (request.getPhoneNumber() == null ||
                (!request.getPhoneNumber().matches("^\\+9627\\d{8}$")
                        && !request.getPhoneNumber().matches("^07\\d{8}$"))) {
            return ApiResponse.error("Phone number must be like +9627XXXXXXXX or 07XXXXXXXX");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            return ApiResponse.error("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            return ApiResponse.error("UserName is already used");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            return ApiResponse.error("Phone number is already used");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setUsername(request.getUsername().trim());
        user.setPhoneNumber(request.getPhoneNumber().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        profile.setFirstName(request.getFirstName() != null ? request.getFirstName().trim() : null);
        profile.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
        profile.setGender(request.getGender() != null ? request.getGender().trim() : null);
        profile.setBirthDate(request.getDateOfBirth());
        userProfileRepository.save(profile);

        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }

        RegisterResponse response = new RegisterResponse(
                savedUser.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber(),
                profile.getGender(),
                profile.getBirthDate(),
                savedUser.getRole().name());

        return ApiResponse.success("Registered successfully", response);
    }
    @Override
    @Transactional
    public ApiResponse<LoginResponse> login(LoginRequest request) {

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ApiResponse.error("Email or username is required");
        }

        String loginInput = request.getEmail().trim();

        Optional<User> optionalUser;

        if (loginInput.contains("@")) {
            String normalizedEmail = loginInput.toLowerCase(Locale.ROOT);
            optionalUser = userRepository.findByEmail(normalizedEmail);
        } else {
            optionalUser = userRepository.findByUsername(loginInput);
        }

        if (optionalUser.isEmpty()) {
            return ApiResponse.error("User with this email or username does not exist");
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
    private boolean isAllowedEmailDomain(String email) {

        int atIndex = email.lastIndexOf("@");

        if (atIndex == -1 || atIndex == email.length() - 1) {
            return false;
        }

        String domain = email.substring(atIndex + 1).toLowerCase(Locale.ROOT);

        return ALLOWED_EMAIL_DOMAINS.contains(domain);
    }





    // @Override
    // public ApiResponse<LoginResponse> loginWithGoogle(String idTokenString) {
    // try {
    // if (idTokenString == null || idTokenString.isBlank()) {
    // return new ApiResponse<>(false, "Google token is required", null);
    // }
    //
    // GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
    // new NetHttpTransport(),
    // GsonFactory.getDefaultInstance()
    // )
    // .setAudience(Collections.singletonList(googleClientId))
    // .build();
    //
    // GoogleIdToken idToken = verifier.verify(idTokenString);
    //
    // if (idToken == null) {
    // return new ApiResponse<>(false, "Invalid Google token", null);
    // }
    //
    // GoogleIdToken.Payload payload = idToken.getPayload();
    //
    // String email = payload.getEmail();
    // String username = (String) payload.get("name");
    // String picture = (String) payload.get("picture");
    //
    // if (email == null || email.isBlank()) {
    // return new ApiResponse<>(false, "Google account email not found", null);
    // }
    //
    // User user = userRepository.findByEmail(email)
    // .orElseGet(() -> createSocialUser(email, username, picture));
    //
    // ensureUserProfileExists(user);
    //
    // String token = jwtService.generateToken(user.getEmail());
    //
    // LoginResponse response = new LoginResponse(
    // token,
    // "Bearer",
    // user.getId(),
    // user.getEmail(),
    // user.getUsername(),
    // user.getPhoneNumber()
    // );
    //
    // return new ApiResponse<>(true, "Google login successful", response);
    //
    // } catch (Exception e) {
    // log.error("Google login failed: {}", e.getMessage());
    // return new ApiResponse<>(false, "Google login failed", null);
    // }
    // }
    //
    // @Override
    // public ApiResponse<LoginResponse> loginWithFacebook(String accessToken) {
    // try {
    // if (accessToken == null || accessToken.isBlank()) {
    // return new ApiResponse<>(false, "Facebook token is required", null);
    // }
    //
    // String url = "https://graph.facebook.com/me"
    // + "?fields=id,name,email,picture"
    // + "&access_token=" + accessToken;
    //
    // RestTemplate restTemplate = new RestTemplate();
    // FacebookUserResponse fbUser = restTemplate.getForObject(url,
    // FacebookUserResponse.class);
    //
    // if (fbUser == null || fbUser.email() == null || fbUser.email().isBlank()) {
    // return new ApiResponse<>(false, "Facebook account email not found", null);
    // }
    //
    // String pictureUrl;
    // if (fbUser.picture() != null && fbUser.picture().data() != null) {
    // pictureUrl = fbUser.picture().data().url();
    // } else {
    // pictureUrl = null;
    // }
    //
    // User user = userRepository.findByEmail(fbUser.email())
    // .orElseGet(() -> createSocialUser(fbUser.email(), fbUser.name(),
    // pictureUrl));
    //
    // ensureUserProfileExists(user);
    //
    // String token = jwtService.generateToken(user.getEmail());
    //
    // LoginResponse response = new LoginResponse(
    // token,
    // "Bearer",
    // user.getId(),
    // user.getEmail(),
    // user.getUsername(),
    // user.getPhoneNumber()
    // );
    //
    // return new ApiResponse<>(true, "Facebook login successful", response);
    //
    // } catch (Exception e) {
    // log.error("Facebook login failed: {}", e.getMessage());
    // return new ApiResponse<>(false, "Facebook login failed", null);
    // }
    // }
    // private User createSocialUser(String email, String username, String
    // profileImageUrl) {
    // User user = new User();
    //
    // user.setEmail(email);
    // user.setUsername(generateUniqueUsername(username, email));
    // user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
    // user.setRole(Role.USER);
    // user.setProfileImageUrl(profileImageUrl);
    //
    // return userRepository.save(user);
    // }
    //
    // private void ensureUserProfileExists(User user) {
    // if (!userProfileRepository.existsByUserId(user.getId())) {
    // UserProfile profile = new UserProfile();
    // profile.setUser(user);
    // userProfileRepository.save(profile);
    // }
    // }
    //
    // private String generateUniqueUsername(String name, String email) {
    // String baseUsername;
    //
    // if (name != null && !name.isBlank()) {
    // baseUsername = name.trim()
    // .replaceAll("\\s+", "_")
    // .replaceAll("[^a-zA-Z0-9_]", "");
    // } else {
    // baseUsername = email.substring(0, email.indexOf("@"));
    // }
    //
    // if (baseUsername.isBlank()) {
    // baseUsername = "user";
    // }
    //
    // String username = baseUsername;
    // int counter = 1;
    //
    // while (userRepository.existsByUsername(username)) {
    // username = baseUsername + "_" + counter;
    // counter++;
    // }
    //
    // return username;
    // }
}