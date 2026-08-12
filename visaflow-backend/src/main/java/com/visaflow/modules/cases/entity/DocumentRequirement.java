package com.visaflow.modules.cases.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "document_requirements", schema = "cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visa_type_id", nullable = false)
    private VisaType visaType;

    @Column(name = "document_class", nullable = false, length = 100)
    private String documentClass;

    @Column(name = "label", nullable = false, length = 300)
    private String label;

    @Column(name = "is_mandatory", nullable = false)
    private boolean mandatory;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
