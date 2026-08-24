package com.demo.ecommerce.repository;

import com.demo.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // --------------------------------------------------
    // Scalar projection of the fields checkout needs.
    // Deliberately NOT an entity projection: entities
    // already present in the persistence context would
    // be served from the first-level cache and hide
    // concurrent commits. Scalar rows always reflect
    // the actual database row.
    // --------------------------------------------------

    interface ProductStockRow {
        Long getId();

        String getName();

        Integer getStock();

        Double getPrice();
    }

    // Native SELECT ... FOR UPDATE (locking current read).
    // Concurrent checkouts serialize here: the second
    // transaction waits for the first to commit and then
    // reads the freshly reduced stock.
    // ORDER BY id gives a deterministic lock order so
    // multi-product carts cannot deadlock each other.
    @Query(value = "SELECT id AS id, name AS name, stock AS stock, price AS price "
            + "FROM products WHERE id IN (:ids) ORDER BY id FOR UPDATE",
            nativeQuery = true)
    List<ProductStockRow> findStockForUpdate(@Param("ids") Collection<Long> ids);

    // Atomic compare-and-decrement. Returns 0 when the
    // remaining stock is insufficient, preventing any
    // oversell regardless of isolation level.
    @Modifying
    @Query("update Product p set p.stock = p.stock - :quantity "
            + "where p.id = :id and p.stock >= :quantity")
    int reduceStock(@Param("id") Long id, @Param("quantity") int quantity);

    // Atomic increment used ONLY by order cancellation to
    // give back what a purchase consumed. Inverse of
    // reduceStock so cancellation never forks a second
    // stock-management mechanism.
    @Modifying
    @Query("update Product p set p.stock = p.stock + :quantity "
            + "where p.id = :id")
    int restoreStock(@Param("id") Long id, @Param("quantity") int quantity);
}
