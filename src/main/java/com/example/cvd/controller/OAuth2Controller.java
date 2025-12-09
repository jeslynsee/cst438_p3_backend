package com.example.cvd.controller;


import com.example.cvd.entity.User;
import com.example.cvd.repository.UserRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"https://catsvsdogs-web-42f3d8a67c13.herokuapp.com", "http://localhost:8081", "exp://localhost:8081"})
public class OAuth2Controller {
  
   private final UserRepository userRepository;
   private final String GITHUB_CLIENT_ID = System.getenv("GITHUB_CLIENT_ID");
   private final String GITHUB_CLIENT_SECRET = System.getenv("GITHUB_CLIENT_SECRET");
   private final String GOOGLE_CLIENT_ID = System.getenv("GOOGLE_CLIENT_ID");
   private final String GOOGLE_CLIENT_SECRET = System.getenv("GOOGLE_CLIENT_SECRET");


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
           String avatarUrl = (String) githubUser.get("avatar_url");
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
                   newUser.setTeam("Cats"); // Default team - user can change later
                   newUser.setAdmin(false);
                   return userRepository.save(newUser);
               });


           // Return complete user data
           Map<String, Object> userData = new HashMap<>();
           userData.put("username", user.getUsername());
           userData.put("email", user.getEmail());
           userData.put("team", user.getTeam());
           userData.put("photoUri", avatarUrl);
           userData.put("admin", user.getAdmin());


           return ResponseEntity.ok(Map.of(
               "message", "Login successful",
               "user", userData
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
      
       try {
           // Exchange code for access token
           RestTemplate restTemplate = new RestTemplate();
           HttpHeaders tokenHeaders = new HttpHeaders();
           tokenHeaders.set("Content-Type", "application/x-www-form-urlencoded");
          
           Map<String, String> tokenRequest = Map.of(
               "code", code,
               "client_id", GOOGLE_CLIENT_ID,
               "client_secret", GOOGLE_CLIENT_SECRET,
               "redirect_uri", "https://catsvsdogs-web-42f3d8a67c13.herokuapp.com/feed",
               "grant_type", "authorization_code"
           );
          
           // Build URL-encoded body
           StringBuilder body = new StringBuilder();
           for (Map.Entry<String, String> entry : tokenRequest.entrySet()) {
               if (body.length() > 0) body.append("&");
               body.append(entry.getKey()).append("=").append(entry.getValue());
           }
          
           HttpEntity<String> tokenEntity = new HttpEntity<>(body.toString(), tokenHeaders);
          
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
           String email = (String) googleUser.get("email");
           String name = (String) googleUser.get("name");
           String picture = (String) googleUser.get("picture");
           String googleId = (String) googleUser.get("id");
          
           // Use email username part as username if no name provided
           String username = name != null ? name : email.split("@")[0];
          
           // Find or create user
           User user = userRepository.findByEmail(email)
               .orElseGet(() -> {
                   User newUser = new User();
                   newUser.setEmail(email);
                   newUser.setUsername(username);
                   newUser.setPassword("oauth_google_" + googleId); // Mark as OAuth user
                   newUser.setTeam("Dogs"); // Default team - user can change later
                   newUser.setAdmin(false);
                   return userRepository.save(newUser);
               });


           // Return complete user data
           Map<String, Object> userData = new HashMap<>();
           userData.put("username", user.getUsername());
           userData.put("email", user.getEmail());
           userData.put("team", user.getTeam());
           userData.put("photoUri", picture);
           userData.put("admin", user.getAdmin());


           return ResponseEntity.ok(Map.of(
               "message", "Login successful",
               "user", userData
           ));
          
       } catch (Exception e) {
           e.printStackTrace();
           return ResponseEntity.internalServerError()
               .body(Map.of("error", "OAuth authentication failed: " + e.getMessage()));
       }
   }
}
