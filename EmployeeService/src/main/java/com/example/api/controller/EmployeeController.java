package com.example.api.controller;

import com.example.api.model.Employee;
import com.example.api.service.EmployeeService;
import com.example.api.service.IdempotencyService;
import com.example.api.service.RedisUtilityService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);

    private final EmployeeService employeeService;
    private final RedisUtilityService redisUtilityService;
    private final IdempotencyService idempotencyService;

    // CRUCIAL: Removed the old ObjectMapper property and constructor parameters to prevent Jackson v3 crashes
    public EmployeeController(EmployeeService employeeService,
                              RedisUtilityService redisUtilityService,
                              IdempotencyService idempotencyService) {
        this.employeeService = employeeService;
        this.redisUtilityService = redisUtilityService;
        this.idempotencyService = idempotencyService;
    }

    // --- POST METHOD ---
    @PostMapping
    public ResponseEntity<?> createEmployee(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody Employee employee) {

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return new ResponseEntity<>(employeeService.saveEmployee(employee), HttpStatus.CREATED);
        }

        ResponseEntity<Employee> cachedResponse = handleIdempotentLookup(idempotencyKey, Employee.class, HttpStatus.CREATED);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        idempotencyService.validateKey(idempotencyKey);

        try {
            log.info("Processing original POST mutation execution for Idempotency-Key [{}]", idempotencyKey);
            Employee savedEmployee = employeeService.saveEmployee(employee);
            idempotencyService.saveResponse(idempotencyKey, savedEmployee);
            return new ResponseEntity<>(savedEmployee, HttpStatus.CREATED);
        } catch (Exception e) {
            idempotencyService.removeKey(idempotencyKey);
            throw e;
        }
    }

    // --- PUT METHOD 1: Basic Profile Update ---
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(
            @PathVariable Long id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody Employee employee) {

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return ResponseEntity.ok(employeeService.updateEmployee(id, employee));
        }

        ResponseEntity<Employee> cachedResponse = handleIdempotentLookup(idempotencyKey, Employee.class, HttpStatus.OK);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        idempotencyService.validateKey(idempotencyKey);

        try {
            log.info("Processing original PUT update execution for Idempotency-Key [{}] on Employee [{}]", idempotencyKey, id);
            Employee updatedEmployee = employeeService.updateEmployee(id, employee);
            idempotencyService.saveResponse(idempotencyKey, updatedEmployee);
            return ResponseEntity.ok(updatedEmployee);
        } catch (Exception e) {
            idempotencyService.removeKey(idempotencyKey);
            throw e;
        }
    }

    // --- PUT METHOD 2: Department Assignment ---
    @PutMapping("/{id}/department/{deptId}")
    public ResponseEntity<?> setEmployeeDepartment(
            @PathVariable Long id,
            @PathVariable Long deptId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return ResponseEntity.ok(employeeService.assignDepartment(id, deptId));
        }

        ResponseEntity<Employee> cachedResponse = handleIdempotentLookup(idempotencyKey, Employee.class, HttpStatus.OK);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        idempotencyService.validateKey(idempotencyKey);

        try {
            log.info("Processing original PUT department assignment for Idempotency-Key [{}] on Employee [{}]", idempotencyKey, id);
            Employee updatedEmployee = employeeService.assignDepartment(id, deptId);
            idempotencyService.saveResponse(idempotencyKey, updatedEmployee);
            return ResponseEntity.ok(updatedEmployee);
        } catch (Exception e) {
            idempotencyService.removeKey(idempotencyKey);
            throw e;
        }
    }

    // --- PUT METHOD 3: Full Structural Roles/Projects Assignment ---
    @PutMapping("/{id}/assign")
    public ResponseEntity<?> completeStructuralAssignment(
            @PathVariable Long id,
            @RequestParam Long roleId,
            @RequestParam Long projectId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return ResponseEntity.ok(employeeService.assignRoleAndProject(id, roleId, projectId));
        }

        ResponseEntity<Employee> cachedResponse = handleIdempotentLookup(idempotencyKey, Employee.class, HttpStatus.OK);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        idempotencyService.validateKey(idempotencyKey);

        try {
            log.info("Processing original PUT structural assignment for Idempotency-Key [{}] on Employee [{}]", idempotencyKey, id);
            Employee updatedEmployee = employeeService.assignRoleAndProject(id, roleId, projectId);
            idempotencyService.saveResponse(idempotencyKey, updatedEmployee);
            return ResponseEntity.ok(updatedEmployee);
        } catch (Exception e) {
            idempotencyService.removeKey(idempotencyKey);
            throw e;
        }
    }

    // --- GET METHODS (Safe / Non-Mutating: Idempotent by Specification) ---
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getAllEmployeesPaginated(
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        log.info("Incoming Paginated Grid Read Request Intercepted. Filter Query Pattern: [email: {}], Page Index: [{}], Segment Bound Size: [{}]",
                email != null ? email : "NONE", page, size);

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Returns the safe, cached map payload block straight to the client nodes
        return ResponseEntity.ok(employeeService.getAllEmployeesPaginated(email, pageable));
    }

    // Fully restored cut-off lookups method
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();

        if (!redisUtilityService.isRateLimitPermissible(clientIp, 60)) {
            redisUtilityService.incrementMetricCounter("lookup_failures_throttled");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded. Maximum 60 lookups per minute permitted.");
        }

        redisUtilityService.incrementMetricCounter("successful_employee_lookups");
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    // --- DELETE METHOD ---
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Employee record removed successfully.");
    }

    /**
     * Shared generic look-up handler using Spring Boot 4 native object casting rules.
     */
    private <T> ResponseEntity<T> handleIdempotentLookup(String key, Class<T> targetClass, HttpStatus successStatus) {
        Object cachedData = idempotencyService.getCachedResponse(key);
        if (cachedData != null && targetClass.isInstance(cachedData)) {
            log.info("Idempotent cache hit for key [{}]. Returning identical saved result.", key);

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Cache-Lookup", "HIT-IDEMPOTENT");

            return new ResponseEntity<>(targetClass.cast(cachedData), headers, successStatus);
        }
        return null;
    }
}
