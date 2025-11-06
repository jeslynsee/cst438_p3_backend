package com.example.cvd.controller;

import com.example.cvd.entity.Posts;
import com.example.cvd.entity.User;
import com.example.cvd.repository.PostsRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostsController {

    private final PostsRepository postsRepository;

    public PostsController(PostsRepository postsRepository) {
        this.postsRepository = postsRepository;
    }

    
    @PostMapping
    public Posts createPost(@RequestBody Posts post) {
        return postsRepository.save(post);
    }

    
    @GetMapping
    public List<Posts> getAllPosts() {
        return postsRepository.findAll();
    }

    
    @GetMapping("/{id}")
    public Posts getPostById(@PathVariable Long id) {
        return postsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    
    @PostMapping("/{id}/like")
    public Posts likePost(@PathVariable Long id) {
        postsRepository.incrementLikes(id);
        return postsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        postsRepository.deleteById(id);
    }
}
