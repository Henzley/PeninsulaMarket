package za.ac.cput.marketplace.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.marketplace.domain.AdminUserSummary;
import za.ac.cput.marketplace.domain.User;
import za.ac.cput.marketplace.exception.AdminException;
import za.ac.cput.marketplace.security.JwtUtil;
import za.ac.cput.marketplace.service.UserService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final String adminRegistrationKey;

    public AuthController(
            UserService userService,
            JwtUtil jwtUtil,
            @Value("${marketplace.admin.registration-key:}") String adminRegistrationKey
    ) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.adminRegistrationKey = adminRegistrationKey;
    }

    @PostMapping("/register")
    public AdminUserSummary register(@RequestBody User user) {
        return AdminUserSummary.from(userService.register(user));
    }

    @PostMapping("/register-admin")
    public AdminUserSummary registerAdmin(
            @RequestBody User user,
            @RequestHeader(name = "X-Admin-Registration-Key", required = false) String providedKey
    ) {
        if (adminRegistrationKey.isBlank()) {
            throw AdminException.forbidden("Admin registration is disabled on this server");
        }
        if (providedKey == null || !MessageDigest.isEqual(
                adminRegistrationKey.getBytes(StandardCharsets.UTF_8),
                providedKey.getBytes(StandardCharsets.UTF_8)
        )) {
            throw AdminException.unauthorized("Invalid admin registration key");
        }
        return AdminUserSummary.from(userService.registerAdmin(user));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        User user = userService.login(request.getEmail(), request.getPassword());

        String loginAs;
        if (user.getRole() == User.Role.ADMIN) {
            loginAs = "ADMIN";
        } else {
            if (request.getLoginAs() == null
                    || (!"BUYER".equalsIgnoreCase(request.getLoginAs())
                    && !"SELLER".equalsIgnoreCase(request.getLoginAs()))) {
                throw new IllegalArgumentException("loginAs must be either BUYER or SELLER");
            }
            loginAs = request.getLoginAs().toUpperCase();
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name(), loginAs);
        Map<String, Object> response = new HashMap<>();
        response.put("user", AdminUserSummary.from(user));
        response.put("loginAs", loginAs);
        response.put("token", token);
        return response;
    }

    public static class LoginRequest {
        private String email;
        private String password;
        private String loginAs;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getLoginAs() { return loginAs; }
        public void setLoginAs(String loginAs) { this.loginAs = loginAs; }
    }
}
