package com.example.api.controller;

import com.example.api.model.Role;
import com.example.api.service.RoleService;
import com.example.api.service.IdempotencyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;
    private final IdempotencyService idempotencyService;

    public RoleController(RoleService roleService, IdempotencyService idempotencyService) {
        this.roleService = roleService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping
    public ResponseEntity<?> createRole(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody Role role) {

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return new ResponseEntity<>(roleService.createRole(role), HttpStatus.CREATED);
        }

        idempotencyService.validateKey(idempotencyKey);
        try {
            Role saved = roleService.createRole(role);
            idempotencyService.saveResponse(idempotencyKey, saved);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            idempotencyService.removeKey(idempotencyKey);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(
            @PathVariable Long id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody Role role) {

        if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
            idempotencyService.validateKey(idempotencyKey);
        }
        try {
            Role updated = roleService.updateRole(id, role);
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
    public ResponseEntity<String> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok("Role access token signature removed successfully.");
    }
}
