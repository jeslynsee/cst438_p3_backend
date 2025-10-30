package com.example.cvd.entity;

import jakarta.persistence.*;

@Entity

@Table(name = "users")

public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firstName", length = 50)
    private String firstName;

    @Column(name = "lastName", length = 50)
    private String lastName;

    @Column(name = "userName", length = 12)
    @Size(min = 2, max = 12)
    private String userName;

    @Column(name = "password")
    @Size(min = 8)
    private String password;

    @Column(name = "team")
    private String team;

    protected User(){}

    public User(String firstName,String lastName,String userName,String password,String team){
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.password = password;
        this.team = team;
    }
    @Override
    public String toString(){
        return String.format(
            "User[firstName = '%s', lastName = '%s', userName = '%s', team = '%s']",
            firstName, lastName, userName, team
        );
    }
    
    public Long getId(){
        return id;
    }
    
    public String getFirstName(){
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }

    public String getUserName(){
        return userName;
    }

    public String getTeam(){
        return team;
    }
}