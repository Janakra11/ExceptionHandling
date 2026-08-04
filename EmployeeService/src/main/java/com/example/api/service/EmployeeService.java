package com.example.api.service;

import com.example.api.model.Employee;
import com.example.api.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> getEmployees(){
        List<Employee> employees = employeeRepository.findAll();
        return employees;
    }

    public Employee addEmployee(Employee employee){
        return employeeRepository.save(employee);
    }

    public void deleteEmployeeById(Long id){
        employeeRepository.deleteById(id);
    }

    public void deleteEmployeeByEmail(String email){
        employeeRepository.deleteEmployeeByEmail(email);
    }

    public Employee updateEmployee(Employee employee){

        Employee dbEmp = employeeRepository.findEmployeeByEmail(employee.getEmail());

        if(dbEmp != null){
            dbEmp.setName(employee.getName());
            dbEmp.setDesignation(employee.getDesignation());
            dbEmp.setAddress(employee.getAddress());
            dbEmp.setSalary(employee.getSalary());
        }

        return employeeRepository.save(dbEmp);
    }
}
