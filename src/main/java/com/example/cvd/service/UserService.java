package com.example.cvd.service;

import com.example.cvd.entity.User;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    private final UserRepository repo;
    public UserService(UserRepository repo){
        this.repo = repo;
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
    public User save(User user) { return repo.save(user);}
    public void deleteById(Long id) { repo.deleteById(id);}
}