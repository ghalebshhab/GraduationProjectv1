package com.start.demo.Controllers;

import com.start.demo.DTOs.Common.ApiResponse;
import com.start.demo.Entities.Users.User;
import com.start.demo.Services.Users.UserServices;
import com.start.demo.Utils.ResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserServices userService;

    public UserController(UserServices userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> findAll() {
        return ResponseFactory.ok(userService.findAll());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<User>> findById(@PathVariable Long userId) {
        if (userId == null || userId <= 0) {
            return ResponseFactory.badRequest("Invalid user id");
        }

        User user = userService.findById(userId);
        if (user == null) {
            return ResponseFactory.notFound("User not found");
        }

        return ResponseFactory.ok(user);
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User requestBody) {
        if (requestBody == null) {
            return ResponseFactory.badRequest("Request body is required");
        }

        requestBody.setId(null);
        User user = userService.saveUser(requestBody);

        if (user == null) {
            return ResponseFactory.badRequest("Unable to create user");
        }

        return ResponseFactory.created(user, "User created successfully");
    }
}
