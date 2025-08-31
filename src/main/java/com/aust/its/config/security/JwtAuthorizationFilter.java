package com.aust.its.config.security;

import com.aust.its.dto.token.JwtUsrInfo;
import com.aust.its.service.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    private final AuthenticationService authenticationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        final String header = request.getHeader("Authorization");
        logger.debug("Auth header on {} {} = {}", request.getMethod(), request.getRequestURI(), header);

        try {
            if (header != null && header.startsWith("Bearer ")) {
                final String token = header.substring(7);
                logger.debug("JWT present ({} chars)", token.length());

                // JwtAuthorizationFilter.java
                JwtUsrInfo info = authenticationService.extractJwtUserInfo(token);

                String role = info.role();
                if (role == null || role.isBlank()) role = "USER";
                if (role.startsWith("ROLE_")) role = role.substring(5);
                role = role.toUpperCase();

                var auth = new UsernamePasswordAuthenticationToken(
                        info.username(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

                request.setAttribute("jwtUserInfo", info);
                logger.debug("JWT valid for user={} role={}", info.username(), info.role());
            } else {
                logger.debug("No Bearer token for {}", request.getRequestURI());
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            logger.warn("JWT validation failed: {}", e.getMessage());
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
        }
    }
}
