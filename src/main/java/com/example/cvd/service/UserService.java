package com.example.cvd.service;

import com.example.cvd.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder pwEncoder;
    public UserService(UserRepository repo, PasswordEncoder pwEncoder){
        this.repo = repo;
        this.pwEncoder = pwEncoder;
    }

    public List<User> findAll() { 
        return repo.findAll();
    }
    public List<User> findByTeam(String team){
        return repo.findByTeam(team);
    }
    public Optional<User> findById(Long id) { 
        return repo.findById(id);
    }
    public User save(User user) { 
        //if (user.getPassword()!= null){
            //user.setPassword()
        //}
        return repo.save(user);
    }
    public void deleteById(Long id) { 
        repo.deleteById(id);
    }
}