package com.example.invoiceapp.domain;

import java.util.Arrays;

public enum ApproverRole {
    MANAGER("Manager"),
    DIRECTOR("Director"),
    UNKNOWN("Unknown");

    private final String label;

    ApproverRole(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static ApproverRole from(String role) {
        return Arrays.stream(values())
                .filter(candidate -> candidate != UNKNOWN && candidate.label.equals(role))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
