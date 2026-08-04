package com.example.api.model;

import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.List;

@Data
public class Location {

    private String streetAddress;

    private Integer postalCode;

    private String city;

    private String state;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "country")
    private List<Country> countries;

}
