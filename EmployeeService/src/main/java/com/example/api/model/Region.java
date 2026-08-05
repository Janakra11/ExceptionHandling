package com.example.api.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "region")
@AttributeOverride(name ="id", column= @Column(name="region_id"))
public class Region {

    @Column(name = "name", nullable = false, length = 200)
    private String regionName;

    @ManyToOne
    @JoinColumn(name ="country_id")
    private Country country;
}
