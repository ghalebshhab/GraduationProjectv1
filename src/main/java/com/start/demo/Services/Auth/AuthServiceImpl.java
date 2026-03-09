package com.start.demo.Services.Auth;

import com.start.demo.DTOs.Auth.Login.LoginRequest;
import com.start.demo.DTOs.Auth.Login.LoginResponse;
import com.start.demo.DTOs.Auth.Register.RegisterRequest;
import com.start.demo.DTOs.Auth.Register.RegisterResponse;
import com.start.demo.Entities.Users.Role;
import com.start.demo.Entities.Users.User;
import com.start.demo.Entities.Users.UserRepository;
import com.start.demo.Exciptions.BadRequestException;
import com.start.demo.Exciptions.ResourceNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUsername(),
                savedUser.getRole().name()
        );
    }
    @Override
    public LoginResponse login(LoginRequest request) {

        // 1️⃣ Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User with this email does not exist")
                );

        // 2️⃣ Check password
        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new BadRequestException("Incorrect password");
        }

        // 3️⃣ Check if account is active
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BadRequestException("User account is inactive");
        }

        // 4️⃣ Generate JWT token
        String token = jwtService.generateToken(user.getEmail());

        // 5️⃣ Return login response
        return new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );
    }
}