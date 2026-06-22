package com.jomap.backend.Controllers;

import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Services.Users.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "https://jomab-712232187160.europe-west1.run.app", "https://jomap-admin.web.app", "https://jomap-admin.firebaseapp.com"})
//@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "https://jomab-712232187160.europe-west1.run.app", "https://jomap-admin.web.app", "https://jomap-admin.firebaseapp.com"})
@RestController
@RequestMapping("/api")
public class UserController {
    UserServices userService;
    @Autowired
    public UserController(UserServices userService){
        this.userService=userService;
    }
    @GetMapping("/users")
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> findById(@PathVariable Long userId) {
        return userService.findById(userId);
    }
    @PostMapping("/users")
    public User createUser(@RequestBody User requestBody){
    requestBody.setId(null);
    User user=userService.saveUser(requestBody);
    return user;
    }

}
// Graduated Officially

