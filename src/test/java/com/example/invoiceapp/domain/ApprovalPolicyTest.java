package com.example.invoiceapp.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ApprovalPolicyTest {

    @ParameterizedTest
    @CsvSource({
        "MANAGER, 49999.99, true",
        "MANAGER, 50000, true",
        "MANAGER, 50000.01, false",
        "MANAGER, 500000, false",
        "MANAGER, 500000.01, false",
        "DIRECTOR, 49999.99, true",
        "DIRECTOR, 50000, true",
        "DIRECTOR, 50000.01, true",
        "DIRECTOR, 500000, true",
        "DIRECTOR, 500000.01, true"
    })
    void policyEnforcesSingleApproverLimits(String role, String amount, boolean allowed) {
        Approver approver = new Approver("demo.user", ApproverRole.valueOf(role));

        if (allowed) {
            ApprovalPolicy.ensureMayApprove("INV-1", new BigDecimal(amount), approver);
        } else {
            assertThatThrownBy(() -> ApprovalPolicy.ensureMayApprove("INV-1", new BigDecimal(amount), approver))
                    .isInstanceOf(ApprovalRejectedException.class)
                    .hasMessageContaining("INV-1");
        }
    }

    @ParameterizedTest
    @CsvSource({"' ', MANAGER", "demo.user, UNKNOWN"})
    void policyRejectsBlankUsernamesAndUnknownRoles(String username, String role) {
        Approver approver = new Approver(username, ApproverRole.valueOf(role));

        assertThatThrownBy(() -> ApprovalPolicy.ensureMayApprove("INV-2", new BigDecimal("100.00"), approver))
                .isInstanceOf(ApprovalRejectedException.class)
                .hasMessageContaining("INV-2");
    }

    @ParameterizedTest
    @CsvSource({"0", "-1"})
    void policyRejectsNonPositiveAmounts(String amount) {
        Approver approver = new Approver("demo.user", ApproverRole.MANAGER);

        assertThatThrownBy(() -> ApprovalPolicy.ensureMayApprove("INV-3", new BigDecimal(amount), approver))
                .isInstanceOf(ApprovalRejectedException.class)
                .hasMessageContaining("INV-3");
    }

    @Test
    void policyAcceptsScaleDifferentAmountsAtTheLimit() {
        Approver approver = new Approver("demo.manager", ApproverRole.MANAGER);

        assertThatCode(() -> ApprovalPolicy.ensureMayApprove("INV-4", new BigDecimal("50000.00"), approver))
                .doesNotThrowAnyException();
    }

    @Test
    void policyRejectsApprovalWhenTwoRecordsAlreadyExist() {
        List<ApprovalRecord> approvals = List.of(
                new ApprovalRecord("demo.director", "Director", Instant.parse("2026-01-01T00:00:00Z")),
                new ApprovalRecord("demo.director2", "Director", Instant.parse("2026-01-02T00:00:00Z")));
        Approver thirdDirector = new Approver("demo.director3", ApproverRole.DIRECTOR);

        assertThatThrownBy(() -> ApprovalPolicy.ensureMayApprove(
                        "INV-OVER-APPROVED", new BigDecimal("500000.01"), thirdDirector, approvals))
                .isInstanceOf(ApprovalRejectedException.class)
                .hasMessageContaining("INV-OVER-APPROVED")
                .hasMessageContaining("two required Director approvals");
    }

    @Test
    void policyAllowsDistinctDirectorAsSecondApprover() {
        List<ApprovalRecord> approvals = List.of(
                new ApprovalRecord("Demo.Director", "Director", Instant.parse("2026-01-01T00:00:00Z")));
        Approver secondDirector = new Approver("demo.director2", ApproverRole.DIRECTOR);

        assertThatCode(() -> ApprovalPolicy.ensureMayApprove(
                        "INV-SECOND", new BigDecimal("500000.01"), secondDirector, approvals))
                .doesNotThrowAnyException();
    }
}
