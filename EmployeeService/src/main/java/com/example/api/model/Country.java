package com.example.api.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name="country")
@AttributeOverride(name="id", column = @Column(name="country_id"))
public class Country {

    @Column(name="name", nullable = false, length = 200)
    private String countryName;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "country")
    private List<Region> regions;
}
