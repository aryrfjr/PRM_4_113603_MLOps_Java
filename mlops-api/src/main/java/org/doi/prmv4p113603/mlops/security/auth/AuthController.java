package org.doi.prmv4p113603.mlops.security.auth;

import org.doi.prmv4p113603.mlops.data.request.AuthRequest;
import org.doi.prmv4p113603.mlops.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager; // To validate credentials
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsService userDetailsService; // To load user details

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {

        try {

            System.out.println("Login attempt: " + request.getUsername());

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            UserDetails user = userDetailsService.loadUserByUsername(request.getUsername());

            // To return a JWT token wrapped in a JSON structure on success
            return ResponseEntity.ok(Map.of("token", jwtUtil.generateToken(user)));

        } catch (Exception e) { // TODO: review exception handling here

            System.out.println("Authentication failed: " + e.getMessage());

            return ResponseEntity.status(403).body("Invalid username or password");

        }

    }

}
