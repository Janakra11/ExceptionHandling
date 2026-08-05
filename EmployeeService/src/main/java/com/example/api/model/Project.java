package com.example.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "projects")
@Getter 
@Setter
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(precision = 15, scale = 2)
    private BigDecimal budget;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id) &&
               Objects.equals(name, project.name) &&
               Objects.equals(budget, project.budget);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, budget);
    }

    @Override
    public String toString() {
        return "Project{" + "id=" + id + ", name='" + name + "', budget=" + budget + "}";
    }
}
