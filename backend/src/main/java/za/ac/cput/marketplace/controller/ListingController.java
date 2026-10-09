package za.ac.cput.marketplace.controller;

import za.ac.cput.marketplace.domain.Listing;
import za.ac.cput.marketplace.domain.AdminListingSummary;
import za.ac.cput.marketplace.domain.User;
import za.ac.cput.marketplace.repository.UserRepository;
import za.ac.cput.marketplace.security.AdminSecurity;
import za.ac.cput.marketplace.service.ListingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@CrossOrigin(origins = "*")
public class ListingController {

    @Autowired
    private ListingService listingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminSecurity adminSecurity;

    @Autowired
    private za.ac.cput.marketplace.service.AdminService adminService;

    // Anyone can browse approved listings — no login needed
    @GetMapping
    public List<Listing> getApprovedListings() {
        return listingService.getApprovedListings();
    }

    // Only someone logged in as SELLER can create a listing
    @PostMapping
    public Listing createListing(@RequestBody Listing listing, HttpServletRequest request) {
        requireLoginAs(request, "SELLER");

        Long sellerId = Long.valueOf(request.getAttribute("userId").toString());
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        return listingService.createListing(listing, seller);
    }

    // A seller views their own listings — must be logged in as SELLER
    @GetMapping("/seller/{sellerId}")
    public List<Listing> getListingsBySeller(@PathVariable Long sellerId, HttpServletRequest request) {
        requireLoginAs(request, "SELLER");
        return listingService.getListingsBySeller(sellerId);
    }

    // Admin only: view listings awaiting approval
    @GetMapping("/pending")
    public List<AdminListingSummary> getPendingListings(HttpServletRequest request) {
        adminSecurity.requireAdmin(request);
        return adminService.getPendingListings();
    }

    // Admin only: approve a listing
    @PutMapping("/{id}/approve")
    public java.util.Map<String, Object> approveListing(@PathVariable Long id, HttpServletRequest request) {
        adminSecurity.requireAdmin(request);
        Listing listing = listingService.approveListing(id);
        return java.util.Map.of("id", listing.getId(), "status", listing.getStatus());
    }

    // Admin only: reject a listing
    @PutMapping("/{id}/reject")
    public java.util.Map<String, Object> rejectListing(@PathVariable Long id, HttpServletRequest request) {
        adminSecurity.requireAdmin(request);
        Listing listing = listingService.rejectListing(id);
        return java.util.Map.of("id", listing.getId(), "status", listing.getStatus());
    }

    // --- Helper checks ---

    private void requireLoginAs(HttpServletRequest request, String requiredMode) {
        Object loginAs = request.getAttribute("loginAs");
        if (loginAs == null) {
            throw za.ac.cput.marketplace.exception.AdminException.unauthorized("Valid authentication is required");
        }
        if (!requiredMode.equalsIgnoreCase(loginAs.toString())) {
            throw za.ac.cput.marketplace.exception.AdminException.forbidden(
                    "Access denied: you must be logged in as " + requiredMode);
        }
    }
}