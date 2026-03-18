package com.start.demo.Services.Auth;

import com.start.demo.Entities.Users.User;
import com.start.demo.Entities.Users.UserRepository;
import com.start.demo.Exciptions.BadRequestException;
import com.start.demo.Exciptions.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new BadRequestException("Authenticated user not found");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + authentication.getName()
                ));
    }

    public String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
