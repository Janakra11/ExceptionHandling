package com.example.api.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name="country")
@AttributeOverride(name="id", column = @Column(name="country_id"))
public class Country {

    @Column(name="name")
    private String countryName;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "region")
    private List<Region> regions;

}
