package com.example.invoiceapp.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ApprovalServiceTest {

    private static Invoice invoiceWith(String id, InvoiceStatus status) {
        return invoiceWith(id, status, new BigDecimal("100.00"));
    }

    private static Invoice invoiceWith(String id, InvoiceStatus status, BigDecimal amount) {
        return new Invoice(id, "Test School", amount, status);
    }

    private static ApprovalService serviceFor(Invoice... invoices) {
        return new ApprovalService(new FakeInvoiceRepository(invoices));
    }

    private static Approver manager() {
        return new Approver("demo.manager", ApproverRole.MANAGER);
    }

    private static Snapshot snapshot(Invoice invoice) {
        return new Snapshot(
                invoice.getStatus(),
                invoice.getAmount(),
                invoice.getDecidedBy(),
                invoice.getDecidedAt(),
                invoice.getDecisionNote(),
                invoice.getApprovals());
    }

    private record Snapshot(
            InvoiceStatus status,
            BigDecimal amount,
            String decidedBy,
            Instant decidedAt,
            String decisionNote,
            List<ApprovalRecord> approvals) {
    }

    @Test
    void submit_movesDraftToPendingApproval() {
        Invoice result = serviceFor(invoiceWith("INV-1", InvoiceStatus.DRAFT)).submit("INV-1");

        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.PENDING_APPROVAL);
    }

    @Test
    void submit_rejectsInvoiceThatIsNotADraft() {
        ApprovalService service = serviceFor(invoiceWith("INV-1", InvoiceStatus.APPROVED));

        assertThatThrownBy(() -> service.submit("INV-1")).isInstanceOf(InvalidInvoiceStateException.class);
    }

    @Test
    void approve_setsDecisionAndApprovalHistory() {
        ApprovalService service = serviceFor(invoiceWith("INV-1", InvoiceStatus.PENDING_APPROVAL));

        Invoice result = service.approve("INV-1", manager());

        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.APPROVED);
        assertThat(result.getDecidedBy()).isEqualTo("demo.manager");
        assertThat(result.getDecidedAt()).isNotNull();
        assertThat(result.getApprovals()).containsExactly(
                new ApprovalRecord("demo.manager", "Manager", result.getDecidedAt()));
    }

    @ParameterizedTest
    @CsvSource({
        "MANAGER, 49999.99, true, ''",
        "MANAGER, 50000, true, ''",
        "MANAGER, 50000.01, false, '50,000'",
        "MANAGER, 500000, false, '50,000'",
        "MANAGER, 500000.01, false, '500,000'",
        "DIRECTOR, 49999.99, true, ''",
        "DIRECTOR, 50000, true, ''",
        "DIRECTOR, 50000.01, true, ''",
        "DIRECTOR, 500000, true, ''",
        "DIRECTOR, 500000.01, false, two distinct Directors"
    })
    void approve_enforcesRoleLimitsWithoutPartialMutation(
            String role, String amountText, boolean allowed, String refusalFact) {
        Invoice invoice = invoiceWith("INV-LIMIT", InvoiceStatus.PENDING_APPROVAL, new BigDecimal(amountText));
        ApprovalService service = serviceFor(invoice);
        Approver approver = new Approver("demo." + role.toLowerCase(), ApproverRole.valueOf(role));
        Snapshot before = snapshot(invoice);

        if (allowed) {
            Invoice result = service.approve(invoice.getId(), approver);

            assertThat(result.getStatus()).isEqualTo(InvoiceStatus.APPROVED);
            assertThat(result.getAmount()).isEqualByComparingTo(amountText);
            assertThat(result.getDecidedBy()).isEqualTo(approver.username());
            assertThat(result.getDecidedAt()).isNotNull();
            assertThat(result.getApprovals()).containsExactly(
                    new ApprovalRecord(approver.username(), approver.role().label(), result.getDecidedAt()));
        } else {
            assertThatThrownBy(() -> service.approve(invoice.getId(), approver))
                    .isInstanceOf(ApprovalRejectedException.class)
                    .hasMessageContaining("INV-LIMIT")
                    .hasMessageContaining(amountText)
                    .hasMessageContaining(refusalFact);
            assertThat(snapshot(invoice)).isEqualTo(before);
        }
    }

    @Test
    void approve_rejectsInvoiceThatIsStillADraft() {
        Invoice invoice = invoiceWith("INV-1", InvoiceStatus.DRAFT);
        ApprovalService service = serviceFor(invoice);
        Snapshot before = snapshot(invoice);

        assertThatThrownBy(() -> service.approve("INV-1", manager())).isInstanceOf(InvalidInvoiceStateException.class);
        assertThat(snapshot(invoice)).isEqualTo(before);
    }

    @Test
    void approve_rejectsBlankApproverWithoutMutation() {
        Invoice invoice = invoiceWith("INV-BLANK", InvoiceStatus.PENDING_APPROVAL);
        ApprovalService service = serviceFor(invoice);
        Snapshot before = snapshot(invoice);

        assertThatThrownBy(() -> service.approve("INV-BLANK", new Approver("  ", ApproverRole.MANAGER)))
                .isInstanceOf(ApprovalRejectedException.class)
                .hasMessageContaining("INV-BLANK");
        assertThat(snapshot(invoice)).isEqualTo(before);
    }

    @Test
    void approve_rejectsUnknownRoleWithoutMutation() {
        Invoice invoice = invoiceWith("INV-ROLE", InvoiceStatus.PENDING_APPROVAL);
        ApprovalService service = serviceFor(invoice);
        Snapshot before = snapshot(invoice);

        assertThatThrownBy(() -> service.approve("INV-ROLE", new Approver("demo.legacy", ApproverRole.UNKNOWN)))
                .isInstanceOf(ApprovalRejectedException.class)
                .hasMessageContaining("INV-ROLE")
                .hasMessageContaining("Manager or Director");
        assertThat(snapshot(invoice)).isEqualTo(before);
    }

    @ParameterizedTest
    @CsvSource({"0", "-1"})
    void approve_rejectsInvalidAmountWithoutMutation(String amount) {
        Invoice invoice = invoiceWith("INV-AMOUNT", InvoiceStatus.PENDING_APPROVAL, new BigDecimal(amount));
        ApprovalService service = serviceFor(invoice);
        Snapshot before = snapshot(invoice);

        assertThatThrownBy(() -> service.approve("INV-AMOUNT", manager()))
                .isInstanceOf(ApprovalRejectedException.class)
                .hasMessageContaining("INV-AMOUNT");
        assertThat(snapshot(invoice)).isEqualTo(before);
    }

    @Test
    void approve_rejectsMissingAmountWithoutMutation() {
        Invoice invoice = invoiceWith("INV-AMOUNT", InvoiceStatus.PENDING_APPROVAL, null);
        ApprovalService service = serviceFor(invoice);
        Snapshot before = snapshot(invoice);

        assertThatThrownBy(() -> service.approve("INV-AMOUNT", manager()))
                .isInstanceOf(ApprovalRejectedException.class)
                .hasMessageContaining("INV-AMOUNT");
        assertThat(snapshot(invoice)).isEqualTo(before);
    }

    @Test
    void approve_throwsWhenInvoiceDoesNotExist() {
        ApprovalService service = serviceFor();

        assertThatThrownBy(() -> service.approve("INV-404", manager())).isInstanceOf(InvoiceNotFoundException.class);
    }

    @Test
    void reject_recordsReasonAndApprover() {
        ApprovalService service = serviceFor(invoiceWith("INV-1", InvoiceStatus.PENDING_APPROVAL));

        Invoice result = service.reject("INV-1", "demo.approver", "Duplicate invoice");

        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.REJECTED);
        assertThat(result.getDecisionNote()).isEqualTo("Duplicate invoice");
    }

    @Test
    void reject_requiresAReason() {
        ApprovalService service = serviceFor(invoiceWith("INV-1", InvoiceStatus.PENDING_APPROVAL));

        assertThatThrownBy(() -> service.reject("INV-1", "demo.approver", "  ")).isInstanceOf(IllegalArgumentException.class);
    }
}
