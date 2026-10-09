package za.ac.cput.marketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.marketplace.domain.User;

import java.util.List;

public interface AdminRepository extends JpaRepository<User, Long> {
    List<User> findAllByOrderByIdAsc();
    boolean existsByEmail(String email);
    long countByRole(User.Role role);
}
