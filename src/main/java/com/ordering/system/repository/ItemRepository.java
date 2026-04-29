package com.ordering.system.repository;

import com.ordering.system.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByNameContainingIgnoreCase(String name);
    List<Item> findByCategory(String category);
    Optional<Item> findBySku(String sku);
    Optional<Item> findByNameIgnoreCase(String name);

    @Query("SELECT COALESCE(SUM(i.price * i.quantity), 0) FROM Item i")
    Double calculateTotalInventoryValue();

    @Query("SELECT COUNT(i) FROM Item i WHERE i.quantity < 5")
    Long countLowStockItems();

    @Query("SELECT COALESCE(AVG(i.price), 0) FROM Item i")
    Double averagePrice();
}