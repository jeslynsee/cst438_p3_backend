package com.example.cvd.controller;

import com.example.cvd.entity.User;
import com.example.cvd.repository.UserRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"https://catsvsdogs-web-42f3d8a67c13.herokuapp.com", "http://localhost:8081", "exp://localhost:8081"})
public class OAuth2Controller {
    
    private final UserRepository userRepository;
    private final String GITHUB_CLIENT_ID = "Ov23liUbTaB4he1bfxAS";
    private final String GITHUB_CLIENT_SECRET = "YOUR_GITHUB_CLIENT_SECRET"; // Add your secret here

    public OAuth2Controller(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/github/exchange")
    public ResponseEntity<?> exchangeGithubCode(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        
        try {
            // Exchange code for access token
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.set("Accept", "application/json");
            
            Map<String, String> tokenRequest = Map.of(
                "client_id", GITHUB_CLIENT_ID,
                "client_secret", GITHUB_CLIENT_SECRET,
                "code", code
            );
            
            HttpEntity<Map<String, String>> tokenEntity = new HttpEntity<>(tokenRequest, tokenHeaders);
            
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                "https://github.com/login/oauth/access_token",
                tokenEntity,
                Map.class
            );
            
            String accessToken = (String) tokenResponse.getBody().get("access_token");
            
            if (accessToken == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to get access token"));
            }
            
            // Get user info from GitHub
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            HttpEntity<String> userEntity = new HttpEntity<>(userHeaders);
            
            ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                userEntity,
                Map.class
            );
            
            Map<String, Object> githubUser = userResponse.getBody();
            String email = (String) githubUser.get("email");
            String username = (String) githubUser.get("login");
            Long githubId = ((Number) githubUser.get("id")).longValue();
            
            // If email is private, fetch from emails endpoint
            if (email == null) {
                ResponseEntity<Map[]> emailResponse = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    userEntity,
                    Map[].class
                );
                
                Map[] emails = emailResponse.getBody();
                if (emails != null && emails.length > 0) {
                    for (Map emailData : emails) {
                        if ((Boolean) emailData.get("primary")) {
                            email = (String) emailData.get("email");
                            break;
                        }
                    }
                }
                
                // If still no email, use github username
                if (email == null) {
                    email = username + "@github.oauth";
                }
            }
            
            // Find or create user
            User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(username);
                    newUser.setPassword("oauth_github_" + githubId); // Mark as OAuth user
                    newUser.setTeam("undecided");
                    newUser.setAdmin(false);
                    return userRepository.save(newUser);
                });

            return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "user", maskUser(user)
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "OAuth authentication failed: " + e.getMessage()));
        }
    }

    private User maskUser(User u) {
        User copy = new User();
        copy.setId(u.getId());
        copy.setUsername(u.getUsername());
        copy.setEmail(u.getEmail());
        copy.setPassword("********");
        copy.setTeam(u.getTeam());
        copy.setAdmin(u.getAdmin());
        return copy;
    }
}