package com.example.marketplace;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import za.ac.cput.marketplace.domain.User;
import za.ac.cput.marketplace.exception.AdminException;
import za.ac.cput.marketplace.repository.AdminRepository;
import za.ac.cput.marketplace.security.AdminSecurity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminSecurityTest {
    private final AdminRepository adminRepository = mock(AdminRepository.class);
    private final AdminSecurity adminSecurity = new AdminSecurity(adminRepository);
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    @Test
    void authorizesOnlyUsersWhoseCurrentDatabaseRoleIsAdmin() {
        User admin = new User();
        admin.setId(10L);
        admin.setRole(User.Role.ADMIN);
        when(request.getAttribute("userId")).thenReturn(10L);
        when(adminRepository.findById(10L)).thenReturn(Optional.of(admin));

        assertEquals(10L, adminSecurity.requireAdmin(request));
    }

    @Test
    void rejectsAUserWhoseDatabaseRoleIsNotAdmin() {
        User student = new User();
        student.setId(10L);
        student.setRole(User.Role.STUDENT);
        when(request.getAttribute("userId")).thenReturn(10L);
        when(adminRepository.findById(10L)).thenReturn(Optional.of(student));

        assertThrows(AdminException.class, () -> adminSecurity.requireAdmin(request));
    }

    @Test
    void rejectsRequestsWithoutAnAuthenticatedUserId() {
        when(request.getAttribute("userId")).thenReturn(null);

        assertThrows(AdminException.class, () -> adminSecurity.requireAdmin(request));
    }
}
