package com.example.api.service.impl;

import com.example.api.exception.EmailAlreadyExistsException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.*;
import com.example.api.repository.*;
import com.example.api.service.EmployeeService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final ProjectRepository projectRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository, 
                               RoleRepository roleRepository, ProjectRepository projectRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    @CacheEvict(cacheNames = "allEmployeesCache", allEntries = true)
    public Employee saveEmployee(Employee employee) {
        if(employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            // Throwing the specialized business exception here
            throw new EmailAlreadyExistsException("Email identifier already registered: "
                    + employee.getEmail());
        }
        return employeeRepository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "allEmployeesCache", key = "'all-employees-list'")
    public List<Employee> getAllEmployees() {
        // CRUCIAL: Must point to our join-fetch repository method to prevent proxy crashes
        return employeeRepository.findAllEmpWithDetails();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "employeeCache", key = "#id")
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Target Employee identifier record not found: " + id));
    }

    // Open com/example/api/service/impl/EmployeeServiceImpl.java
    // Replace your updateEmployee implementation with this version:
    @Override
    @CachePut(cacheNames = "employeeCache", key = "#id")
    @CacheEvict(cacheNames = "allEmployeesCache", allEntries = true)
    public Employee updateEmployee(Long id, Employee targetData) {
        // 1. Fetch the basic target entity record inside the transaction
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Target Employee identifier record not found: " + id));

        // 2. Map mutated data updates
        existingEmployee.setFirstName(targetData.getFirstName());
        existingEmployee.setLastName(targetData.getLastName());
        existingEmployee.setStatus(targetData.getStatus());
        existingEmployee.setHireDate(targetData.getHireDate());

        // 3. Commit changes instantly to MySQL
        employeeRepository.saveAndFlush(existingEmployee);

        // 4. CRUCIAL STEP: Fetch the complete record with its lazy collections initialized
        // before the transactional scope closes and hands the payload to the Redis serializer.
        return employeeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Target Employee record lost during update sequence: " + id));
    }
    @Override
    @CacheEvict(cacheNames = {"employeeCache", "allEmployeesCache"}, key = "#id", allEntries = true)
    //@CacheEvict(cacheNames = "allEmployeesCache", allEntries = true)
    public void deleteEmployee(Long id) {
        Employee targetEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Target Employee identifier record not found: " + id));
        employeeRepository.delete(targetEmployee);
    }

    @Override
    @CacheEvict(cacheNames = {"employeeCache", "allEmployeesCache"}, allEntries = true)
    public Employee assignDepartment(Long employeeId, Long departmentId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Target Employee missing: " + employeeId));
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department context element missing: " + departmentId));
        department.addEmployee(employee);
        return employeeRepository.save(employee);
    }

    @Override
    @CachePut(cacheNames = "employeeCache", key = "#employeeId")
    @CacheEvict(cacheNames = "allEmployeesCache", allEntries = true)
    public Employee assignRoleAndProject(Long employeeId, Long roleId, Long projectId) {
        // 1. Fetch the core employee record inside the transactional boundary
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Target Employee missing: " + employeeId));

        // 2. Fetch parent relational validation structures
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role metadata definition missing: " + roleId));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project entry missing: " + projectId));

        // 3. Update the tracking collections safely
        employee.getRoles().add(role);
        employee.getProjects().add(project);

        // 4. Force Hibernate to push the join-table rows directly down to MySQL
        employeeRepository.saveAndFlush(employee);

        // 5. CRUCIAL STEP FOR HIBERNATE 7: Reload a completely clean,
        // inner-joined representation of the employee object so it doesn't contain uninitialized proxies
        return employeeRepository.findByIdWithDetails(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Target Employee state lost during assignment: " + employeeId));
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "allEmployeesCache",
            key = "'page-' + #pageable.pageNumber + '-size-' + #pageable.pageSize + '-filter-' + (#emailFilter != null ? #emailFilter : 'none')"
    )
    public Map<String, Object> getAllEmployeesPaginated(String emailFilter, Pageable pageable) {
        Page<Employee> employeePage;

        // 1. Run the correct join-fetch query layout matching your selection criteria
        if (emailFilter != null && !emailFilter.trim().isEmpty()) {
            employeePage = employeeRepository.findByEmailContainingIgnoreCase(emailFilter.trim(), pageable);
        } else {
            employeePage = employeeRepository.findAllWithDetailsPaginated(pageable);
        }

        // 2. Map structural components out to a plain, clean serializable HashMap context block
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("content", employeePage.getContent());
        responseMap.put("pageNumber", employeePage.getNumber());
        responseMap.put("pageSize", employeePage.getSize());
        responseMap.put("totalElements", employeePage.getTotalElements());
        responseMap.put("totalPages", employeePage.getTotalPages());
        responseMap.put("isLast", employeePage.isLast());

        return responseMap;
    }
}
