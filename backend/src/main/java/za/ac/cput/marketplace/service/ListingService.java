package za.ac.cput.marketplace.service;

import za.ac.cput.marketplace.domain.Listing;
import za.ac.cput.marketplace.domain.User;
import za.ac.cput.marketplace.exception.AdminException;
import za.ac.cput.marketplace.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListingService {

    @Autowired
    private ListingRepository listingRepository;

    public Listing createListing(Listing listing, User seller) {
        listing.setSeller(seller);
        listing.setStatus(Listing.Status.PENDING);
        return listingRepository.save(listing);
    }

    public List<Listing> getApprovedListings() {
        return listingRepository.findByStatus(Listing.Status.APPROVED);
    }

    public List<Listing> getPendingListings() {
        return listingRepository.findByStatus(Listing.Status.PENDING);
    }

    public List<Listing> getListingsBySeller(Long sellerId) {
        return listingRepository.findBySeller_Id(sellerId);
    }

    public Listing approveListing(Long id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> AdminException.notFound("Listing not found"));
        requirePending(listing);
        listing.setStatus(Listing.Status.APPROVED);
        return listingRepository.save(listing);
    }

    public Listing rejectListing(Long id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> AdminException.notFound("Listing not found"));
        requirePending(listing);
        listing.setStatus(Listing.Status.REJECTED);
        return listingRepository.save(listing);
    }

    private void requirePending(Listing listing) {
        if (listing.getStatus() != Listing.Status.PENDING) {
            throw AdminException.conflict("Only pending listings can be moderated");
        }
    }
}
