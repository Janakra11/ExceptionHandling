package com.example.api.controller;

import com.example.api.model.Department;
import com.example.api.service.DepartmentService;
import com.example.api.service.IdempotencyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final IdempotencyService idempotencyService;

    public DepartmentController(DepartmentService departmentService, IdempotencyService idempotencyService) {
        this.departmentService = departmentService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping
    public ResponseEntity<?> createDepartment(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody Department department) {

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return new ResponseEntity<>(departmentService.createDepartment(department), HttpStatus.CREATED);
        }

        idempotencyService.validateKey(idempotencyKey);
        try {
            Department saved = departmentService.createDepartment(department);
            idempotencyService.saveResponse(idempotencyKey, saved);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            idempotencyService.removeKey(idempotencyKey);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(
            @PathVariable Long id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody Department department) {

        if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
            idempotencyService.validateKey(idempotencyKey);
        }
        try {
            Department updated = departmentService.updateDepartment(id, department);
            if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
                idempotencyService.saveResponse(idempotencyKey, updated);
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
                idempotencyService.removeKey(idempotencyKey);
            }
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok("Department metadata removed successfully.");
    }
}
