package com.start.demo.Services.Users;

import com.start.demo.Entities.Users.User;

import java.util.List;

public interface UserServices {
    List<User> findAll();
    User findById(Long id);
    User findByEmail(String email);
    boolean existsByEmail(String email);
    User saveUser(User user);

}
