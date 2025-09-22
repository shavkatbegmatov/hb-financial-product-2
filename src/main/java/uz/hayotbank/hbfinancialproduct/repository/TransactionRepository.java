package uz.hayotbank.hbfinancialproduct.repository;

import uz.hayotbank.hbfinancialproduct.entity.Transaction;
import uz.hayotbank.hbfinancialproduct.entity.TransactionStatus;
import uz.hayotbank.hbfinancialproduct.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    List<Transaction> findByUserIdAndStatus(Long userId, TransactionStatus status);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
        "AND t.createdAt BETWEEN :startDate AND :endDate")
    Page<Transaction> findByUserIdAndDateRange(@Param("userId") Long userId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               Pageable pageable);

    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);

    Page<Transaction> findByType(TransactionType type, Pageable pageable);

    Page<Transaction> findByUserIdAndType(Long userId, TransactionType type, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE " +
           "(:userId IS NULL OR t.user.id = :userId) AND " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:startDate IS NULL OR t.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR t.createdAt <= :endDate)")
    Page<Transaction> findWithFilters(@Param("userId") Long userId,
                                    @Param("type") TransactionType type,
                                    @Param("status") TransactionStatus status,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    Pageable pageable);

    List<Transaction> findByToUserIdAndStatusAndType(Long toUserId, TransactionStatus status, TransactionType type);
}