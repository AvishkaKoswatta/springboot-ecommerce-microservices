package com.microservice.productservice.repository;

import com.microservice.productservice.entity.InventoryTransaction;
import com.microservice.productservice.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    //inventory history for one product
    Page<InventoryTransaction> findAllByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    List<InventoryTransaction> findAllByProductIdAndType(Long productId, TransactionType type);

    //Gets inventory history between two dates
    @Query("""
            SELECT t FROM InventoryTransaction t
            WHERE t.product.id = :productId
            AND t.createdAt BETWEEN :from AND :to
            ORDER BY t.createdAt DESC
            """)
    List<InventoryTransaction> findByProductIdAndDateRange(
            @Param("productId") Long productId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
