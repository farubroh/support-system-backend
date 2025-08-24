package com.aust.its.controller;

import com.aust.its.dto.LoginPayload;
import com.aust.its.dto.token.JwtUsrInfo;
import com.aust.its.entity.User;
import com.aust.its.repository.UserRepository;
import com.aust.its.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    // Login method for just returning user data (if needed for other purposes)
    @PostMapping("/login")
    public User login(@RequestBody LoginPayload loginPayload) {

        logger.info("login payload is : {}", loginPayload);

        User user = userRepository.findByUsernameAndPassword(loginPayload.username(), loginPayload.password())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        logger.info("Currently login user is : {}", user);
        return user;
    }

    // Authenticate method: Return both the user info and the JWT token
    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(@RequestBody LoginPayload loginPayload) {

        logger.info("login payload is : {}", loginPayload);

        User user = userRepository.findByUsernameAndPassword(loginPayload.username(), loginPayload.password())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        JwtUsrInfo jwtUsrInfo;
        if (loginPayload.isAdmin()) {
            jwtUsrInfo = JwtUsrInfo.withAdminUsrId(user.getUsername(), user.getId(), user.getRole(), "admin: ".concat(String.valueOf(user.getId())));
        } else {
            jwtUsrInfo = JwtUsrInfo.of(user.getUsername(), user.getId(), user.getRole());
        }

        String jwt = authenticationService.generateToken(jwtUsrInfo);
        logger.info("Jwt Token is : {}", jwt);

        // Return both user info and JWT token in the response
        return new AuthenticationResponse(user, jwt);
    }

    // Response class to hold user info and JWT token
    public static class AuthenticationResponse {
        private User user;
        private String token;

        public AuthenticationResponse(User user, String token) {
            this.user = user;
            this.token = token;
        }

        // Getters
        public User getUser() {
            return user;
        }

        public String getToken() {
            return token;
        }
    }
}
