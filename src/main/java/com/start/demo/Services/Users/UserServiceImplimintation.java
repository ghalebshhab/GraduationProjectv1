package com.start.demo.Services.Users;

import com.start.demo.Entities.Users.User;
import com.start.demo.Entities.Users.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImplimintation implements UserServices {

    private final UserRepository userrepo;
    private final EntityManager entity;


    @Override
    public List<User> findAll() {
        return userrepo.findAll();
    }

    @Override
    public ResponseEntity<?> findById(Long id) {
        Optional<User> user = userrepo.findById(id);

        if (user.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "The user cannot be found with the id - " + id));
        }

        return ResponseEntity.ok(user.get());
    }

    @Override
    public ResponseEntity<?> findByEmail(String email) {
        TypedQuery<User> query = entity.createQuery(
                "FROM User WHERE email = :email",
                User.class
        );
        query.setParameter("email", email);

        List<User> users = query.getResultList();

        if (users.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "User not found with email: " + email));
        }

        return ResponseEntity.ok(users.get(0));
    }

    @Override
    public boolean existsByEmail(String email) {
        TypedQuery<User> query = entity.createQuery(
                "FROM User WHERE email = :email",
                User.class
        );
        query.setParameter("email", email);

        List<User> users = query.getResultList();

        return !users.isEmpty();
    }

    @Override
    public User saveUser(User user) {
        return userrepo.save(user);
    }
}
