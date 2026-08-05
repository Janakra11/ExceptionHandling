package com.example.api.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "job")
@AttributeOverride(name = "id", column = @Column(name="job_id"))
public class Job extends BaseEntity {

    @Column(name = "job_title", nullable = false, length = 200)
    private String jobTitle;

    @Column(name = "min_salary", nullable = false)
    private Double minimumSalary;

    @Column(name = "max_salary", nullable = false)
    private Double maximumSalary;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private List<Employee> employees;
}
