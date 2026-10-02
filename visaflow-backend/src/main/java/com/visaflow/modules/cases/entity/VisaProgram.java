package com.visaflow.modules.cases.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "visa_programs", schema = "cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisaProgram {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visa_category_id", nullable = false)
    private VisaCategory visaCategory;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "visaProgram", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("sequenceOrder ASC")
    private List<VisaProgramStage> stages;
}
