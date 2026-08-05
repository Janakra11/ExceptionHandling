package com.example.api.repository;

import com.example.api.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    // Joint-fetch optimization to prevent performance-killing N+1 execution queries
    @Query("SELECT DISTINCT e FROM Employee e " +
            "LEFT JOIN FETCH e.department " +
            "LEFT JOIN FETCH e.roles " +
            "LEFT JOIN FETCH e.projects")
    Optional<Employee> findByIdWithDetails(@Param("id") Long id);

    @Query(value = "SELECT DISTINCT e FROM Employee e " +
            "LEFT JOIN FETCH e.department " +
            "LEFT JOIN FETCH e.roles " +
            "LEFT JOIN FETCH e.projects",
            countQuery = "SELECT COUNT(e) FROM Employee e")
    List<Employee> findAllEmpWithDetails();

    List<Employee> findByStatus(String status);

    // CRUCIAL HIBERNATE 7 FIX: Force join fetches inside the email filter query
    @Query(value = "SELECT DISTINCT e FROM Employee e " +
            "LEFT JOIN FETCH e.department " +
            "LEFT JOIN FETCH e.roles " +
            "LEFT JOIN FETCH e.projects " +
            "WHERE LOWER(e.email) LIKE LOWER(CONCAT('%', :email, '%'))",
            countQuery = "SELECT COUNT(DISTINCT e) FROM Employee e WHERE LOWER(e.email) " +
                    " LIKE LOWER(CONCAT('%', :email, '%'))")
    Page<Employee> findByEmailContainingIgnoreCase(@Param("email") String email, Pageable pageable);


    @Query(value = "SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.department LEFT JOIN FETCH e.roles LEFT JOIN FETCH e.projects",
            countQuery = "SELECT COUNT(e) FROM Employee e")
    Page<Employee> findAllWithDetailsPaginated(Pageable pageable);


}

