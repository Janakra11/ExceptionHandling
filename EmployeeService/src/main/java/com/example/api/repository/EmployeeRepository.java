package com.example.api.repository;

import com.example.api.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long> {

    public void deleteEmployeeByEmail(String email);

    public Employee findEmployeeByEmail(String email);

}
