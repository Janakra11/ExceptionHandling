package com.example.api.service;

import com.example.api.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface EmployeeService {
    Employee saveEmployee(Employee employee);
    List<Employee> getAllEmployees();
    Employee getEmployeeById(Long id);
    Employee updateEmployee(Long id, Employee updatedEmployee);
    void deleteEmployee(Long id);
    Employee assignDepartment(Long employeeId, Long departmentId);
    Employee assignRoleAndProject(Long employeeId, Long roleId, Long projectId);
    Map<String, Object> getAllEmployeesPaginated(String emailFilter, Pageable pageable);
}
