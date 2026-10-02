package com.visaflow.modules.cases.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "visa_program_stages", schema = "cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisaProgramStage {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visa_program_id", nullable = false)
    private VisaProgram visaProgram;

    @Column(name = "sequence_order", nullable = false)
    private int sequenceOrder;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "stage", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("displayOrder ASC")
    private List<StageDocumentRequirement> requirements;
}
