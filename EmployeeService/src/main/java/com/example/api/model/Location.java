package com.example.api.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "location")
@AttributeOverride(name = "id", column = @Column(name = "location_id"))
public class Location {

    @Column(name = "street_address", nullable = false, length = 500)
    private String streetAddress;

    @Column(name = "postal_code", nullable = false, length = 5)
    private Integer postalCode;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "location")
    private List<Country> countries;
}
