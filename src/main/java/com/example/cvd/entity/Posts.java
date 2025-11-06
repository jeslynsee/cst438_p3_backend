package com.example.cvd.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
public class Posts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // URL or file path of the image
    @Column(name = "img", nullable = false)
    private String imageUrl;

    @Column(name = "likes", nullable = false)
    private Long likes = 0L;

    // Many posts can belong to one user
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // Timestamp of when it was posted
    @Column(name = "posted_time", nullable = false)
    private LocalDateTime postedTime;

    protected Posts() {
    
    }

    public Posts(String imageUrl, User user) {
        this.imageUrl = imageUrl;
        this.user = user;
        this.likes = 0L;
        this.postedTime = LocalDateTime.now();
    }

    public void setId(Long id){
        this.id = id;
    }
    public Long getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getPostedTime() {
        return postedTime;
    }

    public void setPostedTime(LocalDateTime postedTime) {
        this.postedTime = postedTime;
    }
}
