package za.ac.cput.marketplace.repository;

import za.ac.cput.marketplace.domain.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findByStatus(Listing.Status status);
    List<Listing> findByStatusOrderByIdDesc(Listing.Status status);
    List<Listing> findAllByOrderByIdDesc();
    long countByStatus(Listing.Status status);
    boolean existsBySeller_Id(Long sellerId);
    List<Listing> findBySeller_Id(Long sellerId);
}