package com.start.demo.Services.Users;

import com.start.demo.Entities.Users.User;
import com.start.demo.Entities.Users.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImplimintation implements UserServices {
    UserRepository userrepo;
    EntityManager entity;
    @Autowired
    public UserServiceImplimintation(UserRepository userrepo,EntityManager entity){
        this.userrepo=userrepo;
        this.entity=entity;
    }

    @Override
    public List<User> findAll() {
        return userrepo.findAll();
    }

    @Override
    public User findById(int id) {
        Optional<User> user=userrepo.findById(id);
        User theUser=null;
        if(user.isPresent())
            theUser=user.get();
        else
            throw new RuntimeException("The user can not be found with the id -" + id);
        return theUser;
    }

    @Override
    public User findByEmail(String email) {
        TypedQuery<User> query=entity.createQuery("FROM User WHERE email=:email",User.class);
        query.setParameter("email",email);
        return query.getSingleResult();
    }

    @Override
    public boolean existsByEmail(String email) {
         TypedQuery<User> query=entity.createQuery("FROM User WHERE email=:email",User.class);
         query.setParameter("email",email);
         User theUser=query.getSingleResult();
         Boolean isExist=true;
         if (theUser==null)
             isExist=false;
         return isExist;

    }

    @Override
    public User saveUser(User user) {
        return userrepo.save(user);
    }
}
