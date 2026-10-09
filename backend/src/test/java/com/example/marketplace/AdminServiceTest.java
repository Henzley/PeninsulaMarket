package com.example.marketplace;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import za.ac.cput.marketplace.domain.AdminDashboardStats;
import za.ac.cput.marketplace.domain.AdminUserSummary;
import za.ac.cput.marketplace.domain.CreateUserRequest;
import za.ac.cput.marketplace.domain.Listing;
import za.ac.cput.marketplace.domain.User;
import za.ac.cput.marketplace.exception.AdminException;
import za.ac.cput.marketplace.repository.AdminRepository;
import za.ac.cput.marketplace.repository.ListingRepository;
import za.ac.cput.marketplace.service.AdminService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;

class AdminServiceTest {
    private final AdminRepository adminRepository = mock(AdminRepository.class);
    private final ListingRepository listingRepository = mock(ListingRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AdminService adminService = new AdminService(adminRepository, listingRepository, passwordEncoder);

    @Test
    void createsStudentAccountWithNormalizedEmailAndEncodedPassword() {
        when(adminRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(adminRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(7L);
            return user;
        });

        AdminUserSummary created = adminService.createUser(
                new CreateUserRequest(" New Student ", " Student@Example.com ", "password123")
        );

        assertEquals(new AdminUserSummary(7L, "New Student", "student@example.com", User.Role.STUDENT), created);
        verify(adminRepository).save(argThat(user ->
                user.getRole() == User.Role.STUDENT && "encoded-password".equals(user.getPassword())));
    }

    @Test
    void refusesToCreateAnAccountWithAnExistingEmail() {
        when(adminRepository.existsByEmail("student@example.com")).thenReturn(true);

        assertThrows(AdminException.class, () -> adminService.createUser(
                new CreateUserRequest("Student", "student@example.com", "password123")
        ));
        verify(adminRepository, never()).save(any(User.class));
    }

    @Test
    void dashboardCountsAllUserAndListingStatuses() {
        when(adminRepository.countByRole(User.Role.STUDENT)).thenReturn(8L);
        when(adminRepository.countByRole(User.Role.ADMIN)).thenReturn(2L);
        when(listingRepository.count()).thenReturn(6L);
        when(listingRepository.countByStatus(Listing.Status.PENDING)).thenReturn(1L);
        when(listingRepository.countByStatus(Listing.Status.APPROVED)).thenReturn(4L);
        when(listingRepository.countByStatus(Listing.Status.REJECTED)).thenReturn(1L);

        assertEquals(new AdminDashboardStats(10, 6, 1, 4, 1), adminService.getDashboardStats());
    }

    @Test
    void refusesToDeleteAdminAccounts() {
        User admin = new User();
        admin.setId(4L);
        admin.setRole(User.Role.ADMIN);
        when(adminRepository.findById(4L)).thenReturn(Optional.of(admin));

        assertThrows(AdminException.class, () -> adminService.deleteUser(4L, 9L));
        verify(adminRepository, never()).delete(admin);
    }

    @Test
    void refusesToDeleteUsersWhoOwnListings() {
        User student = new User();
        student.setId(4L);
        student.setRole(User.Role.STUDENT);
        when(adminRepository.findById(4L)).thenReturn(Optional.of(student));
        when(listingRepository.existsBySeller_Id(4L)).thenReturn(true);

        assertThrows(AdminException.class, () -> adminService.deleteUser(4L, 9L));
        verify(adminRepository, never()).delete(student);
    }

    @Test
    void deletesStudentAccountsThatDoNotOwnListings() {
        User student = new User();
        student.setId(4L);
        student.setRole(User.Role.STUDENT);
        when(adminRepository.findById(4L)).thenReturn(Optional.of(student));
        when(listingRepository.existsBySeller_Id(4L)).thenReturn(false);

        adminService.deleteUser(4L, 9L);

        verify(adminRepository).delete(student);
    }
}
