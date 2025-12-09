package com.example.cvd.controller;

import com.example.cvd.entity.User;
import com.example.cvd.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;


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
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id){
        User found = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return mask(found);
    }

    @GetMapping("/user/email/{email}")
    public User findByEmail(@PathVariable String email) {
        User found = repo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return mask(found);
    }

    @GetMapping("/user/username/{username}")
    public User findByUsername(@PathVariable String username) {
        User found = repo.findByUsername(username)
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
    public ResponseEntity<?> login(@RequestBody User userToLogin) { 
        // find user by email first (matching)
        User found = repo.findByEmail(userToLogin.getEmail())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        
        // compare passwords if matching emails found
        if (found.getPassword().equals(userToLogin.getPassword())) {
            // if passwords match up, then return OK 200 Response and user info, so it can be stored in front end
            return ResponseEntity.ok(
                Map.of(
                    "message", "Login successful",
                    "user", mask(found) 
                )
            );
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

    }

    // check existing users function for when handling sign up
    @PostMapping("/existing")
    public boolean checkExistingUser(@RequestBody User userToSignUp) {
        // using .isPresent to check if the User object we are trying to match is not null, instead of 
        // using ".orElseThrow..." because throwing the error would stop the rest of the code from running
        // and we wouldn't be able to check email AND username to see if user exists by either
        
        // capturing truth value of existing user by matching email
        boolean existingUserByEmail = repo.findByEmail(userToSignUp.getEmail()).isPresent();

        // capturing truth value of existing user by matching username
        boolean existingUserByUsername = repo.findByUsername(userToSignUp.getUsername()).isPresent();
    
        // need to check if email or username already taken, return true if so
        if (existingUserByEmail || existingUserByUsername) {
            return true;
        } 

        // else just return false
        return false;
        
    }

    @GetMapping("/non-admins")
    public List<User> getNonAdminUsers() {
        return repo.findByAdminFalse()
            .stream()
            .map(this::mask)
            .toList();
    }

    // masking function, so passwords don't leak
    private User mask(User u) {
        User copy = new User();
        copy.setId(u.getId());
        copy.setUsername(u.getUsername());
        copy.setEmail(u.getEmail());
        copy.setPassword("********");
        copy.setTeam(u.getTeam());
        copy.setAdmin(u.getAdmin());
        return copy;
    }


}