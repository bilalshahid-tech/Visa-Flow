package com.visaflow.modules.cases.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "countries", schema = "cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Country {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "iso_code", nullable = false, unique = true, length = 5)
    private String isoCode;

    @OneToMany(mappedBy = "country", fetch = FetchType.LAZY)
    private List<VisaProgram> visaPrograms;
}
