package com.example.cvd.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.example.cvd.entity.User;

@Entity
@Table(name = "posts")
public class Posts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // URL or file path of the image
    @Column(name = "img", nullable = false, updatable = false)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer likes = 0;

    // Timestamp of when it was posted
    @Column(name = "postedTime", nullable = false)
    private LocalDateTime postedTime = LocalDateTime.now();

    // Many posts can belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "id", nullable = false)
    private User userId;


    public Posts() {
    
    }

    public Posts(String imageUrl, String description) {
        this.imageUrl = imageUrl;
        this.description = description;
        this.likes = 0;
        this.postedTime = LocalDateTime.now();
    }

    public Long getId() {return id;}

    public void setId(Long id){
        this.id = id;
    }

    public String getImageUrl() {return imageUrl;}

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getLikes() {return likes;}

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public String getDescription() {return description;}

    public void setDescription(String description){
        this.description = description;
    }

    public LocalDateTime getPostedTime() {return postedTime;}

    public void setPostedTime(LocalDateTime postedTime) {
        this.postedTime = postedTime;
    }

    public User getUser() { return userId; }

    public void setUser(User userId) { 
        this.userId = userId; 
    }
}
