package com.example.api.service;

import com.example.api.model.Department;
import java.util.List;

public interface DepartmentService {
    Department createDepartment(Department department);
    Department getDepartmentById(Long id);
    List<Department> getAllDepartments();
    Department updateDepartment(Long id, Department department);
    void deleteDepartment(Long id);
}
