package com.example.cvd.posts;

import com.example.cvd.controller.PostsController;
import com.example.cvd.entity.Posts;
import com.example.cvd.entity.User;
import com.example.cvd.repository.PostsRepository;
import com.example.cvd.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostsController.class)
class PostsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostsRepository postsRepo;

    @MockBean
    private UserRepository userRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Posts testPost;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("splurge");

        testPost = new Posts();
        testPost.setId(10L);
        testPost.setImageUrl("http://img/funnylittleimg.png");
        testPost.setDescription("Hello There");
        testPost.setLikes(0);
        testPost.setPostedTime(LocalDateTime.now());
        testPost.setUser(testUser);
    }

    @Test
    void shouldCreatePost() throws Exception {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(postsRepo.save(any(Posts.class))).thenReturn(testPost);

        String body = """
            {"imageUrl":"http://img/funnylittleimg.png","description":"Hello There","userId":1}
        """;

        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value(10))
               .andExpect(jsonPath("$.description").value("Hello There"));
    }

    @Test
    void shouldReturnAllPosts() throws Exception {
        when(postsRepo.findAll()).thenReturn(List.of(testPost));

        mockMvc.perform(get("/posts"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$[0].id").value(10))
               .andExpect(jsonPath("$[0].description").value("Hello There"));
    }

    @Test
    void shouldDeletePost() throws Exception {
        when(postsRepo.existsById(10L)).thenReturn(true);

        mockMvc.perform(delete("/posts/10"))
               .andExpect(status().isNoContent());

        verify(postsRepo).deleteById(10L);
    }
}

