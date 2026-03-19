package com.start.demo.Services.Users;

import com.start.demo.Entities.Users.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserServices {
    List<User> findAll();
    ResponseEntity<?> findById(Long id);
    ResponseEntity<?> findByEmail(String email);
    boolean existsByEmail(String email);
    User saveUser(User user);

}
