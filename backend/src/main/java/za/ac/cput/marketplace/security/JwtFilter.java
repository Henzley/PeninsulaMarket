package za.ac.cput.marketplace.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Claims claims = jwtUtil.validateToken(token);

                // Attach the decoded info to this request so controllers can read it later
                request.setAttribute("userId", claims.get("userId"));
                request.setAttribute("role", claims.get("role"));
                request.setAttribute("loginAs", claims.get("loginAs"));
                request.setAttribute("email", claims.getSubject());

            } catch (JwtException | IllegalArgumentException e) {
                // Invalid or expired token — just leave the request unauthenticated
                // The specific endpoint will decide if that's allowed or not
            }
        }

        filterChain.doFilter(request, response);
    }
}
