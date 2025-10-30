package com.example.cvd.repository;

import java.util.List;
import com.example.cvd.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository <User, Long>{
    List<User> findByTeam(String team);
}