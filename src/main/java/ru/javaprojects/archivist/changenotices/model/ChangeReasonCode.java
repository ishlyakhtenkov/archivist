package ru.javaprojects.archivist.changenotices.model;

import org.springframework.util.StringUtils;

public enum ChangeReasonCode {
    DESIGN_IMPROVEMENTS(1), TECHNOLOGICAL_IMPROVEMENTS(2), UNIFICATION_REASON(3), STANDARDS_MODIFICATION(4),
    TEST_RESULTS_REASON(5), LETTER_CHANGING(6), TROUBLESHOOTING(7), QUALITY_IMPROVEMENT(8), CUSTOMER_REQUIREMENTS(9),
    SCHEMA_CHANGING(10), ELECTRICAL_INSTALLATION_IMPROVEMENT(11), TECHNOLOGICAL_EQUIPMENT_CHANGING(12),
    WORKING_CONDITIONS_CHANGING(13), NEW_TECHNOLOGICAL_PROCESSES_INTRODUCTION(14), ORIGINAL_WORKPIECE_REPLACEMENT(15),
    MATERIAL_CONSUMPTION_RATES_CHANGING(16), TEXT_IMPROVEMENTS(17), FACTORY_REQUIREMENTS(18);

    private final int code;

    ChangeReasonCode(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code + " - " + StringUtils.capitalize(name().replace('_', ' ').toLowerCase());
    }
}
