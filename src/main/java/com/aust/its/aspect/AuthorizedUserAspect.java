package com.aust.its.aspect;

import com.aust.its.dto.token.JwtUsrInfo;
import com.aust.its.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizedUserAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizedUserAspect.class);

    private final AuthenticationService authenticationService;

    @Before("@annotation(com.aust.its.annotation.IsAuthorizedPerson)")
    public void checkIfAuthorizedUser() {

        logger.info("** Executing the aspect **");
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();

        String token = Optional.ofNullable(request.getHeader("Authorization"))
                .filter(h -> h.startsWith("Bearer "))
                .map(h -> h.substring(7))
                .orElse(null);

        if (token == null) {
            throw new RuntimeException("Missing Authorization token");
        }

        // Extract user info from JWT token
        try {
            JwtUsrInfo jwtUsrInfo = authenticationService.extractJwtUserInfo(token);
            String adminUserId = jwtUsrInfo.adminUsrId();
            logger.info("AdminUsrId :: {}", adminUserId);
        } catch (Exception e) {
            // Token invalid or expired
            throw new RuntimeException("Invalid or expired token");
        }
    }
}
