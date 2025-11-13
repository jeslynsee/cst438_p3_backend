package com.example.cvd.entity;

import jakarta.persistence.*;

@Entity

@Table(name = "users")

public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "team", nullable = false)
    private String team;

    @Column(name = "admin", nullable = false)
    private Boolean admin;

    public User(){}

    public User(String username, String password, String email, String team, boolean admin){
        this.username = username;
        this.password = password;
        this.email = email;
        this.team = team;
        this.admin = admin;
    }

    @Override
    public String toString(){
        return String.format(
"User[username = '%s', team = '%s']",
        username, team
        );
    }
    
    public Long getId(){
        return id;
    }

    public void setId(Long id) { 
        this.id = id; 
    }
    
    public String getUsername(){
        return username;
    }

    public void setUsername(String username) { 
        this.username = username; 
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getTeam(){
        return team;
    }

    public void setTeam(String team){
        this.team = team;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public Boolean getAdmin() { 
        return admin; 
    }
    public void setAdmin(Boolean admin) { 
        this.admin = admin; 
    }
}