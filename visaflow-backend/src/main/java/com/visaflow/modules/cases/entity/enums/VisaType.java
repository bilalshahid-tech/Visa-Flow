package com.visaflow.modules.cases.entity.enums;

/**
 * Legacy enum - superseded by the VisaType JPA entity in cases.entity.VisaType.
 * Kept here to avoid compilation errors in case any legacy code references this name.
 */
@Deprecated
public enum LegacyVisaType {
    STUDENT, WORK, BUSINESS, TOURIST, VISIT, FAMILY, MEDICAL, PERMANENT
}
