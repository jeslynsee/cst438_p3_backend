package com.example.cvd.controller;

import com.example.cvd.entity.Posts;
import com.example.cvd.entity.User;
import com.example.cvd.repository.PostsRepository;
import com.example.cvd.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

record PostCreateDto(String imageUrl, String description, Long userId) {}
record PostUpdateDto(String imageUrl, String description, LocalDateTime postedTime) {}
record PostResponseDto(Long id, String imageUrl, String description,
                       Integer likes, LocalDateTime postedTime, Long userId) {}

@RestController
@RequestMapping("/posts")
public class PostsController {

    private final PostsRepository postsRepo;
    private final UserRepository userRepo;

    public PostsController(PostsRepository postsRepo, UserRepository userRepo) {
        this.postsRepo = postsRepo;
        this.userRepo = userRepo;
    }

    private PostResponseDto toResponse(Posts p) {
        return new PostResponseDto(
                p.getId(),
                p.getImageUrl(),
                p.getDescription(),
                p.getLikes(),
                p.getPostedTime(),
                p.getUser().getId()
        );
    }

    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(@RequestBody PostCreateDto dto) {
        if (dto.userId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }

        User user = userRepo.findById(dto.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + dto.userId()));

        Posts post = new Posts();
        post.setImageUrl(dto.imageUrl());
        post.setDescription(dto.description());
        post.setLikes(0);
        post.setPostedTime(LocalDateTime.now());
        post.setUser(user);

        Posts saved = postsRepo.save(post);
        return ResponseEntity
                .created(URI.create("/posts/" + saved.getId()))
                .body(toResponse(saved));
    }

    @GetMapping
    public List<PostResponseDto> getAllPosts() {
        return postsRepo.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/team/{team}")
    public List<PostResponseDto> getPostsByTeam(@PathVariable String team) {
        return postsRepo.getPostsByUser_Team(team)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPostById(@PathVariable Long id) {
        return postsRepo.findById(id)
                .map(p -> ResponseEntity.ok(toResponse(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<PostResponseDto> likePost(@PathVariable Long id) {
        int updated = postsRepo.incrementLikes(id);
        if (updated == 0) {
            return ResponseEntity.notFound().build();
        }
        return postsRepo.findById(id)
                .map(p -> ResponseEntity.ok(toResponse(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PostResponseDto> updatePost(@PathVariable Long id, @RequestBody PostUpdateDto dto) {
        return postsRepo.findById(id).map(existing -> {
            if (dto.description() != null) {
                existing.setDescription(dto.description());
            }
            if (dto.imageUrl() != null) {
                existing.setImageUrl(dto.imageUrl());
            }
            if (dto.postedTime() != null) {
                existing.setPostedTime(dto.postedTime());
            }
            Posts saved = postsRepo.save(existing);
            return ResponseEntity.ok(toResponse(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if (!postsRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        postsRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
