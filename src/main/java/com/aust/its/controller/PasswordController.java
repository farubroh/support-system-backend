package com.aust.its.controller;

import com.aust.its.annotation.swaggerapidoc.passwordcontroller.ChangePasswordApiDoc;
import com.aust.its.annotation.swaggerapidoc.passwordcontroller.ForgetPasswordApiDoc;
import com.aust.its.annotation.swaggerapidoc.passwordcontroller.ValidateTokenApiDoc;
import com.aust.its.dto.ChangePasswordPayload;
import com.aust.its.dto.ForgetPasswordPayload;
import com.aust.its.dto.PasswordValidationTokenPayload;
import com.aust.its.service.PasswordService;
import com.aust.its.utils.Const;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Password APIs", description = "Password token related APIs")
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordController.class);
    private final AuthenticationManager authenticationManager;
    private final PasswordService passwordService;

    @ForgetPasswordApiDoc
    @PostMapping("/forget")
    public ResponseEntity<String> forgetPassword(@RequestBody ForgetPasswordPayload forgetPasswordPayload) {
        return ResponseEntity.ok(passwordService.storeToken(forgetPasswordPayload.userId()));
    }


    @ValidateTokenApiDoc
    @PostMapping("/validate-token")
    public ResponseEntity<String> validateToken(@RequestBody PasswordValidationTokenPayload passwordValidationTokenPayload) {
        boolean isValid = passwordService.isPasswordUpdateTokenValid(passwordValidationTokenPayload.userId(), passwordValidationTokenPayload.token());
        String tokenValidationMessage = isValid ? Const.Token.VALID_TOKEN : Const.Token.INVALID_TOKEN;
        return ResponseEntity.ok(tokenValidationMessage);
    }


    @ChangePasswordApiDoc
    @PostMapping("/change")
    public ResponseEntity<String> changePassword(@RequestBody @Valid ChangePasswordPayload changePasswordPayload,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        String userName = null;

        if(userDetails == null) {
            logger.info("this is an unauthenticated user");
            if(changePasswordPayload.token() == null) {
                throw new RuntimeException("token is null");
            }
            userName = changePasswordPayload.userId();
            boolean isValid = passwordService.isPasswordUpdateTokenValid(userName, changePasswordPayload.token());

            if(!isValid) {
                throw new RuntimeException(Const.Token.INVALID_TOKEN);
            }
        } else {
            userName = userDetails.getUsername();
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userName, changePasswordPayload.oldPassword())
        );

        return ResponseEntity.ok(passwordService.updatePassword(userName, changePasswordPayload.newPassword()));
    }
}
