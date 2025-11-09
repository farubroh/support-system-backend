package com.aust.its.controller;

import com.aust.its.annotation.swaggerapidoc.authcontroller.LoginApiDoc;
import com.aust.its.annotation.swaggerapidoc.authcontroller.RefreshTokenApiDoc;
import com.aust.its.annotation.swaggerapidoc.authcontroller.RegisterApiDoc;
import com.aust.its.config.security.CustomUserDetailsService;
import com.aust.its.dto.AuthenticationResponse;
import com.aust.its.dto.LoginPayload;
import com.aust.its.dto.RegisterPayload;
import com.aust.its.dto.RegisterResponse;
import com.aust.its.entity.HelpDeskUser;
import com.aust.its.service.UserService;
import com.aust.its.utils.Commons;
import com.aust.its.utils.Const;
import com.aust.its.utils.JwtUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Auth APIs", description = "Authentication related APIs")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;


    @LoginApiDoc
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody LoginPayload loginPayload) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginPayload.userId(), loginPayload.password())
            );
            return authenticationResponse(loginPayload.userId());
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(null); // return 401 instead of 500
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        } catch (Exception e) {
            logger.error("Login failed", e);
            return ResponseEntity.status(500).body(null);
        }
    }



    @RegisterApiDoc
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterPayload registerPayload) {
        logger.info("registration payload is : {}", registerPayload);
        HelpDeskUser user = userService.register(registerPayload);
        return ResponseEntity.ok(new RegisterResponse(user.getUserId(), user.getRoleId()));
    }


    @RefreshTokenApiDoc
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestHeader("X-Refresh-Token") String refreshTokenHeader) {
        logger.info("Authorization header is : {}", refreshTokenHeader);

        if(Commons.isNullOrEmpty(refreshTokenHeader) || !refreshTokenHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("Invalid Authorization header");
        }

        final String token = refreshTokenHeader.substring(7);

        if(!JwtUtils.validateToken(token)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String userId = JwtUtils.extractUsername(token);
        return authenticationResponse(userId);
    }

    private ResponseEntity<AuthenticationResponse> authenticationResponse(String userId) {
        final UserDetails userDetails = customUserDetailsService.loadUserByUsername(userId);
        final String accessToken = JwtUtils.generateToken(userDetails, Const.Jwt.ACCESS_TOKEN_EXPIRATION_MILISEC);
        final String refreshToken = JwtUtils.generateToken(userDetails, Const.Jwt.REFRESH_TOKEN_EXPIRATION_MILISEC);
        return ResponseEntity.ok(new AuthenticationResponse(accessToken, refreshToken));
    }
}