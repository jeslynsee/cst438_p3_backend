package com.example.cvd.controller;

import com.example.cvd.entity.User;
import com.example.cvd.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository repo;
    
    public UserController(UserRepository repo){
        this.repo = repo;
    }

    // retrieve all users
    @GetMapping
    public List<User> getAllUsers(){
        return repo.findAll();
    }

    // retrieves users by team
    @GetMapping("/team/{team}")
    public List<User> findByTeam(@PathVariable String team){
        return repo.findByTeam(team);
    }

    // get one particular user
    @GetMapping("/user/{id}")
    public User getUser(@PathVariable Long id){
        User found = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return mask(found);
    }

    @GetMapping("/user/email/{email}")
    public User findUserByEmail(@PathVariable String email) {
        User found = repo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return mask(found);
    }

    // create a user
    @PostMapping
    public User createUser(@RequestBody User user){
        return repo.save(user);
    }

    // reused code from P2 exercises
    // this code allows us to grab user info provided by user in req body JSON form and update user if we find it
    // else just save as new user
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User newUser) {
        return repo.findById(id).map(user -> {
            user.setUsername(newUser.getUsername());
            user.setPassword(newUser.getPassword()); 
            user.setEmail(newUser.getEmail());
            user.setTeam(newUser.getTeam()); 
            return repo.save(user);
          })
          .orElseGet(() -> {
            return repo.save(newUser);
          });
    }

    // delete a user
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        repo.deleteById(id);
    }

    // TODO: Auth routes below 
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void login(@PathVariable User userToLogin) { //UNFINISHED
        User found = repo.findByEmail(userToLogin.getEmail())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

    }
    

    

    // masking function, so passwords don't leak
    private User mask(User u) {
        User copy = new User();
        copy.setId(u.getId());
        copy.setUsername(u.getUsername());
        copy.setEmail(u.getEmail());
        copy.setPassword("********");
        copy.setTeam(u.getTeam());
        return copy;
    }


}