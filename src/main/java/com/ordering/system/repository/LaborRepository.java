package com.ordering.system.repository;

import com.ordering.system.entity.Labor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface LaborRepository extends JpaRepository<Labor, Long> {

    List<Labor> findByNameContainingIgnoreCase(String name);
    List<Labor> findByRole(String role);
    List<Labor> findByShift(String shift);
    List<Labor> findByStatus(String status);
    List<Labor> findAllByOrderByIdDesc();
    List<Labor> findByNameContainingIgnoreCaseOrderByIdDesc(String name);
    List<Labor> findByRoleIgnoreCaseOrderByIdDesc(String role);
    List<Labor> findByStatusIgnoreCaseOrderByIdDesc(String status);

    @Query("SELECT COUNT(l) FROM Labor l WHERE LOWER(l.status) = 'active'")
    Long countActive();

    @Query("SELECT COUNT(l) FROM Labor l WHERE LOWER(l.status) = 'inactive'")
    Long countInactive();

    @Query("SELECT COALESCE(SUM(l.salary), 0) FROM Labor l WHERE LOWER(l.status) = 'active'")
    Double sumActiveSalary();

    @Query("SELECT COALESCE(SUM(l.salary), 0) FROM Labor l WHERE l.status = 'Active'")
    Double totalActiveSalary();
}