package com.example.cvd.controller;

import com.example.cvd.entity.User;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    
    public UserController(UserService service){
        this.service = service;
    }
    @GetMapping
    public List<User> getAllUsers(){
        return service.findAll();
    }
    @GetMapping("/{team}")
    public List<User> cdTeam(){
        return service.findByTeam(team);
    }
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id){
        return service.findById(id);
    }
    @PostMapping
    public User createUser(@RequestBody User user){
        return service.save(user);
    }
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id){
        service.deleteById(id);
    }
}