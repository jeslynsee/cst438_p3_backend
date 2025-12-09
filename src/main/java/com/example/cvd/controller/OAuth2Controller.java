package com.example.cvd.controller;

import com.example.cvd.entity.User;
import com.example.cvd.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
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
    @Value("${github.client.id}")
    private String GITHUB_CLIENT_ID;

    @Value("${github.client.secret}")
    private String GITHUB_CLIENT_SECRET;

    @Value("${google.client.id}")
    private String GOOGLE_CLIENT_ID;

    @Value("${google.client.secret}")
    private String GOOGLE_CLIENT_SECRET;



    

    public OAuth2Controller(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/github/exchange")
    public ResponseEntity<?> exchangeGithubCode(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        
        try {
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
            String githubEmail = (String) githubUser.get("email"); 
            String username = (String) githubUser.get("login");
            Long githubId = ((Number) githubUser.get("id")).longValue();
            
            if (githubEmail == null) {
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
                            githubEmail = (String) emailData.get("email");
                            break;
                        }
                    }
                }
                
                // If still no email, use github username
                if (githubEmail == null) {
                    githubEmail = username + "@github.oauth";
                }
            }
            
            // Now use final variable
            final String finalEmail = githubEmail;
            
            // Find or create user
            User user = userRepository.findByEmail(finalEmail)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(finalEmail);
                    newUser.setUsername(username);
                    newUser.setPassword("oauth_github_" + githubId);
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

    @PostMapping("/google/exchange")
    public ResponseEntity<?> exchangeGoogleCode(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        String redirectUri = request.get("redirectUri"); // Need this from frontend
        
        try {
            // Exchange code for access token
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.set("Content-Type", "application/x-www-form-urlencoded");
            
            String tokenRequestBody = String.format(
                "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
                code, GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, redirectUri
            );
            
            HttpEntity<String> tokenEntity = new HttpEntity<>(tokenRequestBody, tokenHeaders);
            
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token",
                tokenEntity,
                Map.class
            );
            
            String accessToken = (String) tokenResponse.getBody().get("access_token");
            
            if (accessToken == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to get access token"));
            }
            
            // Get user info from Google
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            HttpEntity<String> userEntity = new HttpEntity<>(userHeaders);
            
            ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo",
                HttpMethod.GET,
                userEntity,
                Map.class
            );
            
            Map<String, Object> googleUser = userResponse.getBody();
            String googleEmail = (String) googleUser.get("email");
            String name = (String) googleUser.get("name");
            String googleId = (String) googleUser.get("id");
            
            // Use email prefix as username if name not available
            String username = name != null ? name : googleEmail.split("@")[0];
            
            // Make variables final for lambda
            final String finalEmail = googleEmail;
            final String finalUsername = username;
            final String finalGoogleId = googleId;
            
            // Find or create user
            User user = userRepository.findByEmail(finalEmail)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(finalEmail);
                    newUser.setUsername(finalUsername);
                    newUser.setPassword("oauth_google_" + finalGoogleId);
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