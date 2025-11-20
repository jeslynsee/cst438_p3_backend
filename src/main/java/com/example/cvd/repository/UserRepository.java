package com.example.cvd.repository;

import java.util.List;
import java.util.Optional;
import com.example.cvd.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository <User, Long>{
    // custom function to find users by filtered team
    List<User> findByTeam(String team);

    // custom function to find non-admin users
    List<User> findByAdminFalse();

    

    // custom function to find a user by their email (should be unique)
    // had to define as Optional, or else would get error in Controller file for trying to use orElseThrow
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
}