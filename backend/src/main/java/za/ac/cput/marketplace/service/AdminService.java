package za.ac.cput.marketplace.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import za.ac.cput.marketplace.domain.AdminDashboardStats;
import za.ac.cput.marketplace.domain.AdminListingSummary;
import za.ac.cput.marketplace.domain.AdminUserSummary;
import za.ac.cput.marketplace.domain.CreateUserRequest;
import za.ac.cput.marketplace.domain.Listing;
import za.ac.cput.marketplace.domain.User;
import za.ac.cput.marketplace.exception.AdminException;
import za.ac.cput.marketplace.repository.AdminRepository;
import za.ac.cput.marketplace.repository.ListingRepository;

import java.util.List;
import java.util.Locale;

@Service
public class AdminService {
    private final AdminRepository adminRepository;
    private final ListingRepository listingRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            AdminRepository adminRepository,
            ListingRepository listingRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.adminRepository = adminRepository;
        this.listingRepository = listingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AdminUserSummary> getAllUsers() {
        return adminRepository.findAllByOrderByIdAsc().stream()
                .map(AdminUserSummary::from)
                .toList();
    }

    @Transactional
    public AdminUserSummary createUser(CreateUserRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (adminRepository.existsByEmail(email)) {
            throw AdminException.conflict("An account with this email already exists");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(User.Role.STUDENT);
        return AdminUserSummary.from(adminRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<AdminListingSummary> getAllListings() {
        return listingRepository.findAllByOrderByIdDesc().stream()
                .map(AdminListingSummary::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminListingSummary> getPendingListings() {
        return listingRepository.findByStatusOrderByIdDesc(Listing.Status.PENDING).stream()
                .map(AdminListingSummary::from)
                .toList();
    }

    @Transactional
    public void deleteUser(Long id, Long requestingAdminId) {
        User user = adminRepository.findById(id)
                .orElseThrow(() -> AdminException.notFound("User not found"));

        if (user.getId().equals(requestingAdminId)) {
            throw AdminException.conflict("You cannot delete your own admin account");
        }
        if (user.getRole() == User.Role.ADMIN) {
            throw AdminException.conflict("Admin accounts cannot be deleted from user management");
        }
        if (listingRepository.existsBySeller_Id(id)) {
            throw AdminException.conflict("This account owns listings and cannot be deleted");
        }

        adminRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public AdminDashboardStats getDashboardStats() {
        return new AdminDashboardStats(
                adminRepository.countByRole(User.Role.STUDENT) + adminRepository.countByRole(User.Role.ADMIN),
                listingRepository.count(),
                listingRepository.countByStatus(Listing.Status.PENDING),
                listingRepository.countByStatus(Listing.Status.APPROVED),
                listingRepository.countByStatus(Listing.Status.REJECTED)
        );
    }
}
