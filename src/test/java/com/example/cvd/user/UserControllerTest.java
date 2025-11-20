package com.example.cvd.user;

import com.example.cvd.controller.UserController;
import com.example.cvd.entity.User;
import com.example.cvd.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    // Autowired annotation means these variables are being automatically injected into this test suite 
    // with what Spring already has access to
    // so no need to make new instance of them
    @Autowired
    private MockMvc mockMvc;

    // MockBean is used here so we can mock database operations with fake data,
    // allowing us to test the routes only and not mess with our actual table
    @MockBean
    private UserRepository userRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setup() {
        // setting up test user below
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("usercontrollertest");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setTeam("cat");
        testUser.setAdmin(false);
    }

    // test for GET all users
    @Test
    void shouldReturnAllUsers() throws Exception {
        when(userRepo.findAll()).thenReturn(List.of(testUser));

        mockMvc.perform(get("/api/users"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$[0].username").value("usercontrollertest"));
    }

    // test for GET users by team
    @Test
    void shouldReturnUsersByTeam() throws Exception {
        when(userRepo.findByTeam("cat")).thenReturn(List.of(testUser));

        mockMvc.perform(get("/api/users/team/cat"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].team").value("cat"));
    }

    // test for GET user by ID
    @Test
    void shouldReturnUserById() throws Exception {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.username").value("usercontrollertest"))
               .andExpect(jsonPath("$.password").value("********")); // masked password
    }

    // test for GET user by email
    @Test
    void shouldReturnUserByEmail() throws Exception {
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/user/email/test@example.com"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    // test for GET user by username
    @Test
    void shouldReturnUserByUsername() throws Exception {
        when(userRepo.findByUsername("usercontrollertest")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/user/username/usercontrollertest"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.username").value("usercontrollertest"));
    }

    // test for POST route to create user
    @Test
    void shouldCreateUser() throws Exception {
        when(userRepo.save(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/users")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(testUser)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.username").value("usercontrollertest"));
    }

    // test PUT route to update existing user
    @Test
    void shouldUpdateExistingUser() throws Exception {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepo.save(any(User.class))).thenReturn(testUser);

        testUser.setUsername("updateduser");

        mockMvc.perform(put("/api/users/1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(testUser)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.username").value("updateduser"));
    }

    // test to DELETE user
    @Test
    void shouldDeleteExistingUser() throws Exception {
        when(userRepo.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/users/1"))
               .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundForDeleteInvalidUser() throws Exception {
        when(userRepo.existsById(9999L)).thenReturn(false);

        mockMvc.perform(delete("/api/users/9999"))
               .andExpect(status().isNotFound());
    }

    // test for POST route for user auth/login
    @Test
    void shouldLoginSuccessfully() throws Exception {
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User loginUser = new User();
        loginUser.setEmail("test@example.com");
        loginUser.setPassword("password123");

        mockMvc.perform(post("/api/users/login")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(loginUser)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Login successful"))
               .andExpect(jsonPath("$.user.username").value("usercontrollertest"));
    }

    // testing for user's inability to log in with wrong password
    @Test
    void shouldFailLoginWithWrongPassword() throws Exception {
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User loginUser = new User();
        loginUser.setEmail("test@example.com");
        loginUser.setPassword("wrong");

        mockMvc.perform(post("/api/users/login")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(loginUser)))
               .andExpect(status().isUnauthorized());
    }

    // testing POST route for existing user check
    @Test
    void shouldReturnTrueForExistingUser() throws Exception {
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepo.findByUsername("usercontrollertest")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/existing")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(testUser)))
               .andExpect(status().isOk())
               .andExpect(content().string("true"));
    }

    @Test
    void shouldReturnFalseForNewUser() throws Exception {
        when(userRepo.findByEmail("unique@example.com")).thenReturn(Optional.empty());
        when(userRepo.findByUsername("unique")).thenReturn(Optional.empty());

        User newUser = new User();
        newUser.setUsername("unique");
        newUser.setEmail("unique@example.com");
        newUser.setPassword("1234");
        newUser.setTeam("cat");
        newUser.setAdmin(false);

        mockMvc.perform(post("/api/users/existing")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(newUser)))
               .andExpect(status().isOk())
               .andExpect(content().string("false"));
    }
}
