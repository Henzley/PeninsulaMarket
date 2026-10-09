package za.ac.cput.marketplace.repository;

import za.ac.cput.marketplace.domain.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    @EntityGraph(attributePaths = {"imageUrls", "seller"})
    List<Listing> findByStatus(Listing.Status status);

    @EntityGraph(attributePaths = {"imageUrls", "seller"})
    List<Listing> findByStatusOrderByIdDesc(Listing.Status status);

    @EntityGraph(attributePaths = {"imageUrls", "seller"})
    List<Listing> findAllByOrderByIdDesc();

    long countByStatus(Listing.Status status);

    boolean existsBySeller_Id(Long sellerId);

    @EntityGraph(attributePaths = {"imageUrls", "seller"})
    List<Listing> findBySeller_Id(Long sellerId);
}