package com.example.api.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "department")
@AttributeOverride(name = "id", column= @Column(name="department_id"))
public class Department {

    @Column(name="name", nullable = false)
    private String name;

    @Column(name="manager_id", nullable = false)
    private Long managerId;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location locations;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "department")
    private List<Employee> employees;
}
