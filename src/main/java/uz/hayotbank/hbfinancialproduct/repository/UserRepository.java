package uz.hayotbank.hbfinancialproduct.repository;

import uz.hayotbank.hbfinancialproduct.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.username LIKE %:search% OR u.fullName LIKE %:search% OR u.email LIKE %:search%")
    Page<User> findBySearchTerm(String search, Pageable pageable);
}