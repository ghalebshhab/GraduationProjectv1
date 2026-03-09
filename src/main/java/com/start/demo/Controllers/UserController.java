package com.start.demo.Controllers;

import com.start.demo.Entities.Users.User;
import com.start.demo.Services.Users.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class UserController {
    UserServices userService;
    @Autowired
    public UserController(UserServices userService){
        this.userService=userService;
    }
    @GetMapping("/users")
    public List<User> findAll(){
        return userService.findAll();
    }
    @GetMapping("/users/{userId}")
    public User findById(@PathVariable Long userId){
        if(userId<0 || userId>findAll().size())
            throw new RuntimeException("The user id is not found");
        User theUser=userService.findById(userId);
        return theUser;

    }
    @PostMapping("/users")
    public User createUser(@RequestBody User requestBody){
    requestBody.setId(null);
    User user=userService.saveUser(requestBody);
    return user;
    }

}
