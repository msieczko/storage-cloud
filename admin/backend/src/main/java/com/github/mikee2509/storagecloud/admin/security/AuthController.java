package com.github.mikee2509.storagecloud.admin.security;

import com.github.mikee2509.storagecloud.admin.domain.dto.AuthStatusDto;
import com.github.mikee2509.storagecloud.admin.domain.dto.UserAuthDto;
import com.github.mikee2509.storagecloud.admin.domain.security.SessionDetails;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
class AuthController {
    private static final Logger logger = LogManager.getLogger(AuthController.class);

    private AuthenticationManager authenticationManager;

    @GetMapping("/logout")
    String logout(HttpSession session) {
        SecurityContextHolder.clearContext();
        session.invalidate();
        return "OK";
    }

    @PostMapping("/login")
    String login(@RequestBody UserAuthDto userAuthDto) {
        try {
            Authentication request = new UsernamePasswordAuthenticationToken(
                    userAuthDto.getUsername(), userAuthDto.getPassword()
            );
            Authentication result = authenticationManager.authenticate(request);
            SecurityContextHolder.getContext().setAuthentication(result);
        } catch (Exception e) {
            logger.info("Authentication failed: " + e.getMessage());
            return "ERROR";
        }
        logger.info("Successfully authenticated. Security context contains: " +
                SecurityContextHolder.getContext().getAuthentication()
        );
        return "OK";

    }

    @GetMapping("/login")
    AuthStatusDto login() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof AnonymousAuthenticationToken) {
            return AuthStatusDto.notAuthenticated();
        }

        SessionDetails sessionDetails = (SessionDetails) auth.getPrincipal();
        return AuthStatusDto.authenticated(sessionDetails.dto());
    }
}
