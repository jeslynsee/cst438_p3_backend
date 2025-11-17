package com.example.cvd.controller;

import com.example.cvd.entity.Posts;
import com.example.cvd.entity.User;
import com.example.cvd.repository.PostsRepository;
import com.example.cvd.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

record PostCreateDto(String imageUrl, String description, Long userId){}
record PostUpdateDto(String imageUrl, String description, LocalDateTime postedTime){}

@RestController
@RequestMapping("/posts")
public class PostsController {

    private final PostsRepository postsRepo;
    private final UserRepository userRepo;

    public PostsController(PostsRepository postsRepo, UserRepository userRepo) {
        this.postsRepo = postsRepo;
        this.userRepo = userRepo;
    }

    
    @PostMapping
    public ResponseEntity<Posts> createPost(@RequestBody PostCreateDto dto) {
        if(dto.userId() == null){
            return ResponseEntity.badRequest().build();
        }
        User user = userRepo.findById(dto.userId())
            .orElseThrow(() -> new IllegalArgumentException("User not found: "+dto.userId()));
        Posts post = new Posts();
        post.setImageUrl(dto.imageUrl());
        post.setDescription(dto.description());
        post.setLikes(0);
        post.setPostedTime(LocalDateTime.now());
        post.setUser(user);

        Posts saved = postsRepo.save(post);
        return ResponseEntity.created(URI.create("/posts/"+saved.getId())).body(saved);

    }

    
    @GetMapping
    public List<Posts> getAllPosts() {
        return postsRepo.findAll();
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<Posts> getPostById(@PathVariable Long id) {
        return postsRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    
    @PostMapping("/{id}/like")
    public ResponseEntity<Posts> likePost(@PathVariable Long id) {
        int updated = postsRepo.incrementLikes(id); //refer to PostsRepository
        if (updated == 0){
            return ResponseEntity.notFound().build();
        }
        return postsRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());

                
    }

    @PatchMapping("/{id}") //updates only the time and description at this moment. 
    public ResponseEntity<Posts> updatePost(@PathVariable Long id, @RequestBody PostUpdateDto dto) {
        return postsRepo.findById(id).map(existing ->{
            if(dto.description()!=null){
                existing.setDescription(dto.description());
            }
            if(dto.postedTime()!=null){
                existing.setPostedTime(dto.postedTime());
            }
            return ResponseEntity.ok(postsRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
        
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if(!postsRepo.existsById(id)){ 
            return ResponseEntity.notFound().build();
        }
        postsRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
