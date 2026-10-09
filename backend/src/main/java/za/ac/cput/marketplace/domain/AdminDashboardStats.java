package za.ac.cput.marketplace.domain;

public record AdminDashboardStats(
        long totalUsers,
        long totalListings,
        long pendingListings,
        long approvedListings,
        long rejectedListings
) {}
