package org.doi.prmv4p113603.mlops.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that intercepts HTTP requests to validate and authenticate JWT tokens.
 * <p>
 * If a valid token is present in the Authorization header, sets the
 * authentication in the SecurityContext.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Performs JWT validation on incoming requests.
     *
     * Extracts the token from the Authorization header, validates it,
     * and populates the SecurityContext if valid.
     *
     * @param request the incoming HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain to continue processing
     * @throws ServletException if an error occurs in the servlet pipeline
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Only process if Authorization header is present and starts with Bearer
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String jwt = authHeader.substring(7);

            try { // To guard against invalid or malformed JWTs in the next line

                /*
                 * NOTE: If the token is missing periods or is invalid (as in the login request,
                 *  where no JWT should exist), then extractUsername(jwt) throws a
                 *  MalformedJwtException, crashing the filter chain.
                 */
                String username = jwtUtil.extractUsername(jwt);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(jwt, userDetails)) {

                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                        SecurityContextHolder.getContext().setAuthentication(auth);

                    }

                }

            } catch (Exception e) {
                System.out.println("Invalid JWT: " + e.getMessage());
                // TODO: Optionally could send a 401 here to block invalid tokens
            }

        }

        filterChain.doFilter(request, response);

    }

    /**
     * Specifies whether the JWT filter should not be applied to this request.
     *
     * @param request the current HTTP request
     * @return true if the filter should be skipped; false otherwise
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().equals("/auth/login");
    }

}
