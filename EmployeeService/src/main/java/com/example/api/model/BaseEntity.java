package com.example.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Data;

@Data
@MappedSuperclass
public class BaseEntity extends AbstractPersistable<Long> {

    @Version
    @Column(name="LCKG_VER_NUM", nullable = false, columnDefinition = "int default 0")
    private int version;

    @Column(name="CRT_USR_ID", nullable = false, length = 100)
    private String createdById;

    @Column(name="CHG_USR_ID", nullable = false, length = 100)
    private String changedById;

}
