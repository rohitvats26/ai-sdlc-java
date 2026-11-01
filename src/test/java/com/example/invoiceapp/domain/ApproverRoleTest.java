package com.example.invoiceapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApproverRoleTest {

    @Test
    void fromRecognizesSupportedRolesAndRejectsLegacyRoles() {
        assertThat(ApproverRole.from("Manager")).isEqualTo(ApproverRole.MANAGER);
        assertThat(ApproverRole.from("Director")).isEqualTo(ApproverRole.DIRECTOR);
        assertThat(ApproverRole.from("Approver")).isEqualTo(ApproverRole.UNKNOWN);
        assertThat(ApproverRole.from("Admin")).isEqualTo(ApproverRole.UNKNOWN);
        assertThat(ApproverRole.from(null)).isEqualTo(ApproverRole.UNKNOWN);
    }
}
