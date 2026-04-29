package com.ordering.system.repository;

import com.ordering.system.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerNameContainingIgnoreCase(String customerName);
    List<Order> findByStatus(String status);
    List<Order> findTop5ByOrderByOrderDateDesc();
    List<Order> findByOrderDateBetweenOrderByOrderDateDesc(
        LocalDateTime start, LocalDateTime end);
    List<Order> findByStatusAndOrderDateBetweenOrderByOrderDateDesc(
        String status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE " +
           "LOWER(o.customerName) LIKE LOWER(CONCAT('%', :name, '%')) AND " +
           "o.orderDate BETWEEN :start AND :end ORDER BY o.orderDate DESC")
    List<Order> findByCustomerNameContainingAndDateRange(
        @Param("name") String name,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

    @Query("SELECT o.orderDate, SUM(o.totalPrice) FROM Order o WHERE " +
           "o.status = 'Completed' AND o.orderDate BETWEEN :start AND :end " +
           "GROUP BY o.orderDate")
    List<Object[]> findDailyRevenue(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE " +
           "o.orderDate BETWEEN :start AND :end")
    Long countByDateRange(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND " +
           "o.orderDate BETWEEN :start AND :end")
    Long countByStatusAndDateRange(
        @Param("status") String status,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") String status);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE " +
           "o.status = 'Completed' AND o.orderDate BETWEEN :start AND :end")
    Double sumCompletedRevenue(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

    @Query("SELECT o.itemOrdered, SUM(o.quantity) as total FROM Order o " +
           "GROUP BY o.itemOrdered ORDER BY total DESC")
    List<Object[]> findTopSellingItems(Pageable pageable);
}