package za.ac.cput.marketplace.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.marketplace.domain.AdminDashboardStats;
import za.ac.cput.marketplace.domain.AdminListingSummary;
import za.ac.cput.marketplace.domain.AdminUserSummary;
import za.ac.cput.marketplace.domain.CreateUserRequest;
import za.ac.cput.marketplace.security.AdminSecurity;
import za.ac.cput.marketplace.service.AdminService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    private final AdminService adminService;
    private final AdminSecurity adminSecurity;

    public AdminController(AdminService adminService, AdminSecurity adminSecurity) {
        this.adminService = adminService;
        this.adminSecurity = adminSecurity;
    }

    @GetMapping("/users")
    public List<AdminUserSummary> getAllUsers(HttpServletRequest request) {
        adminSecurity.requireAdmin(request);
        return adminService.getAllUsers();
    }

    @PostMapping("/users")
    public AdminUserSummary createUser(
            @Valid @RequestBody CreateUserRequest createUserRequest,
            HttpServletRequest request
    ) {
        adminSecurity.requireAdmin(request);
        return adminService.createUser(createUserRequest);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id, HttpServletRequest request) {
        Long requestingAdminId = adminSecurity.requireAdmin(request);
        adminService.deleteUser(id, requestingAdminId);
    }

    @GetMapping("/listings")
    public List<AdminListingSummary> getAllListings(HttpServletRequest request) {
        adminSecurity.requireAdmin(request);
        return adminService.getAllListings();
    }

    @GetMapping("/listings/pending")
    public List<AdminListingSummary> getPendingListings(HttpServletRequest request) {
        adminSecurity.requireAdmin(request);
        return adminService.getPendingListings();
    }

    @GetMapping("/dashboard")
    public AdminDashboardStats getDashboardStats(HttpServletRequest request) {
        adminSecurity.requireAdmin(request);
        return adminService.getDashboardStats();
    }
}
