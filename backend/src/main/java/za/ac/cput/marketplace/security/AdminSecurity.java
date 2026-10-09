package za.ac.cput.marketplace.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import za.ac.cput.marketplace.domain.User;
import za.ac.cput.marketplace.exception.AdminException;
import za.ac.cput.marketplace.repository.AdminRepository;

@Component
public class AdminSecurity {
    private final AdminRepository adminRepository;

    public AdminSecurity(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Long requireAdmin(HttpServletRequest request) {
        Object rawUserId = request.getAttribute("userId");
        if (!(rawUserId instanceof Number userId)) {
            throw AdminException.unauthorized("Valid authentication is required");
        }

        User user = adminRepository.findById(userId.longValue())
                .orElseThrow(() -> AdminException.unauthorized("Authentication is no longer valid"));
        if (user.getRole() != User.Role.ADMIN) {
            throw AdminException.forbidden("Access denied: requires ADMIN role");
        }
        return user.getId();
    }
}
