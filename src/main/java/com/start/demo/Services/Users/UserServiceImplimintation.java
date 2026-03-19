package com.start.demo.Services.Users;

import com.start.demo.Entities.Users.User;
import com.start.demo.Entities.Users.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImplimintation implements UserServices {

    private final UserRepository userrepo;
    private final EntityManager entity;

    @Autowired
    public UserServiceImplimintation(UserRepository userrepo, EntityManager entity) {
        this.userrepo = userrepo;
        this.entity = entity;
    }

    @Override
    public List<User> findAll() {
        return userrepo.findAll();
    }

    @Override
    public User findById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return userrepo.findById(id).orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return userrepo.findByEmail(email).orElse(null);
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return userrepo.existsByEmail(email);
    }

    @Override
    public User saveUser(User user) {
        if (user == null) {
            return null;
        }
        return userrepo.save(user);
    }
}
