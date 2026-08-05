package com.example.api.service.impl;

import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.Department;
import com.example.api.repository.DepartmentRepository;
import com.example.api.service.DepartmentService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "departmentCache", key = "#id")
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    @CachePut(cacheNames = "departmentCache", key = "#id")
    public Department updateDepartment(Long id, Department updateData) {
        Department existing = getDepartmentById(id);
        existing.setName(updateData.getName());
        existing.setCode(updateData.getCode());
        return departmentRepository.saveAndFlush(existing);
    }

    @Override
    @CacheEvict(cacheNames = "departmentCache", key = "#id")
    public void deleteDepartment(Long id) {
        Department existing = getDepartmentById(id);
        departmentRepository.delete(existing);
    }
}
