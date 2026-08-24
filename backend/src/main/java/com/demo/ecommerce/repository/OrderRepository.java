package com.demo.ecommerce.repository;

import com.demo.ecommerce.model.Order;
import com.demo.ecommerce.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    List<Order> findAllByOrderByCreatedAtDesc();

    // --------------------------------------------------
    // Atomic compare-and-swap on the status column.
    // Returns 0 when another transaction already moved
    // the status away from expectedStatus, so only ONE
    // concurrent transition (e.g. cancellation with its
    // stock restoration) can ever win.
    // --------------------------------------------------
    @Modifying
    @Query("update Order o set o.status = :newStatus "
            + "where o.id = :id and o.status = :expectedStatus")
    int updateStatusIfCurrent(@Param("id") Long id,
                              @Param("expectedStatus") OrderStatus expectedStatus,
                              @Param("newStatus") OrderStatus newStatus);
}
