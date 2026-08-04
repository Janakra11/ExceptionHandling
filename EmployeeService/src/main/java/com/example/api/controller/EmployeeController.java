package com.example.api.controller;

import com.example.api.model.Employee;
import com.example.api.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping(value = "/employees", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<Employee>> allEmployees() {
        List<Employee> employees = employeeService.getEmployees();
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    @PostMapping(value="/employee", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Employee> saveEmployee(@RequestBody Employee employee) {

        Employee dbEmp = employeeService.addEmployee(employee);
        return new ResponseEntity<>(dbEmp, HttpStatus.OK);
    }

    @PutMapping(value="/employee", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Employee> updateEmployee(@RequestBody Employee employee){
        Employee dbEmp = employeeService.updateEmployee(employee);
        return new ResponseEntity<>(dbEmp, HttpStatus.OK);
    }

    @DeleteMapping(value = "/employee/{id}", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> deleteEmployee(@PathVariable ("id") Long employeeId){
        employeeService.deleteEmployeeById(employeeId);
        return new ResponseEntity<>("Employee deleted successfully", HttpStatus.OK);
    }

    @DeleteMapping(value = "/employee/{email}", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> deleteEmployee(@PathVariable ("email") String email){
        employeeService.deleteEmployeeByEmail(email);
        return new ResponseEntity<>("Employee deleted successfully", HttpStatus.OK);
    }
}
