package com.example.cvd.repository;

import com.example.cvd.entity.Posts;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class PostsRepository {

    private final Map<Long, Posts> posts = new HashMap<>();
    private long nextId = 1L;

    public List<Posts> findAll() {
        return new ArrayList<>(posts.values());
    }

    public Optional<Posts> findById(Long id) {
        return Optional.ofNullable(posts.get(id));
    }

    public Posts save(Posts post) {
        if (post.getId() == null) {
            post.setId(nextId++);
        }
        posts.put(post.getId(), post);
        return post;
    }

    public void deleteById(Long id) {
        posts.remove(id);
    }

    public void incrementLikes(Long id) {
        Posts post = posts.get(id);
        if (post != null) {
            post.setLikes(post.getLikes() + 1);
        }
    }
}
