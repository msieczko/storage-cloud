package com.github.mikee2509.storagecloud.admin.security;

import com.github.mikee2509.storagecloud.admin.client.TcpClientService;
import com.github.mikee2509.storagecloud.admin.domain.dto.AuthStatusDto;
import com.github.mikee2509.storagecloud.admin.domain.dto.AuthResponseDto;
import com.github.mikee2509.storagecloud.admin.domain.dto.UserAuthDto;
import com.github.mikee2509.storagecloud.admin.domain.exception.LoginException;
import com.github.mikee2509.storagecloud.admin.domain.security.SessionDetails;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;

@Log
@RestController
@AllArgsConstructor
@RequestMapping("/api")
class AuthController {
    private AuthenticationManager authenticationManager;
    private TcpClientService tcpClientService;
    private ServerAuthenticationProvider serverAuthenticationProvider;

    @GetMapping("/logout")
    String logout(HttpSession session) {
        try {
            serverAuthenticationProvider.logout();
            tcpClientService.closeConnection();
        } catch (LoginException e) {
            log.info("Failed to log out: " + e.getMessage());
        } catch (IOException e) {
            log.info("Failed to close connection: " + e.getMessage());
        }
        SecurityContextHolder.clearContext();
        session.invalidate();
        return "OK";
    }

    @PostMapping("/login")
    AuthResponseDto login(@RequestBody UserAuthDto userAuthDto) {
        try {
            Authentication request = new UsernamePasswordAuthenticationToken(
                    userAuthDto.getUsername(), userAuthDto.getPassword()
            );
            Authentication result = authenticationManager.authenticate(request);
            SecurityContextHolder.getContext().setAuthentication(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return AuthResponseDto.error(e.getMessage());
        }
        log.info("Successfully authenticated. Security context contains: " +
                SecurityContextHolder.getContext().getAuthentication()
        );
        return AuthResponseDto.success();

    }

    @GetMapping("/login")
    AuthStatusDto login() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof AnonymousAuthenticationToken) {
            return AuthStatusDto.notAuthenticated();
        }
        SessionDetails sessionDetails = (SessionDetails) auth.getPrincipal();

        if (!tcpClientService.isConnected()) {
            SessionDetails newSession;
            try {
                newSession = serverAuthenticationProvider.relogin(sessionDetails);
            } catch (LoginException e) {
                log.info("Failed to relogin: " + e.getMessage());
                return AuthStatusDto.notAuthenticated();
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(newSession, null, newSession.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            sessionDetails = newSession;
        }

        return AuthStatusDto.authenticated(sessionDetails.dto());
    }
}
