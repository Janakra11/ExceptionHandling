package com.example.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "employee")
@AttributeOverride(name = "id", column = @Column(name = "employee_id"))
public class Employee extends BaseEntity{

    @Column(name="name", nullable = false, length = 200)
    private String name;

    @Column(name="email", nullable = false, length = 100)
    private String email;

    @Column(name="designation", nullable = false, length = 100)
    private String designation;

    @Column(name="salary", nullable = false)
    private Double salary;

    @Column(name="address", nullable = false)
    private String address;

    @ManyToOne
    @JoinColumn(name="job_id")
    private Job job;

    @ManyToOne
    @JoinColumn(name="department_id")
    private Department department;
}
