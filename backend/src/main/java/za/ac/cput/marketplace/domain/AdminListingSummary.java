package za.ac.cput.marketplace.domain;

import java.math.BigDecimal;
import java.util.List;

public record AdminListingSummary(
        Long id,
        String title,
        String description,
        BigDecimal price,
        String location,
        String contactInfo,
        Listing.Status status,
        List<String> imageUrls,
        AdminUserSummary seller
) {
    public static AdminListingSummary from(Listing listing) {
        return new AdminListingSummary(
                listing.getId(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getPrice(),
                listing.getLocation(),
                listing.getContactInfo(),
                listing.getStatus(),
                listing.getImageUrls(),
                listing.getSeller() == null ? null : AdminUserSummary.from(listing.getSeller())
        );
    }
}
