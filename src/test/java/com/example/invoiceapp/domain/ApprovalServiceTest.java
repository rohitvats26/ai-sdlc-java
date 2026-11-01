package com.example.invoiceapp.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

    private static Approver director(String username) {
        return new Approver(username, ApproverRole.DIRECTOR);
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
        "MANAGER, 500000.01, false, 'two distinct Directors'",
        "DIRECTOR, 49999.99, true, ''",
        "DIRECTOR, 50000, true, ''",
        "DIRECTOR, 50000.01, true, ''",
        "DIRECTOR, 500000, true, ''",
        "DIRECTOR, 500000.01, true, ''"
    })
    void approve_enforcesRoleLimitsWithoutPartialMutation(
            String role, String amountText, boolean allowed, String refusalFact) {
        Invoice invoice = invoiceWith("INV-LIMIT", InvoiceStatus.PENDING_APPROVAL, new BigDecimal(amountText));
        ApprovalService service = serviceFor(invoice);
        Approver approver = new Approver("demo." + role.toLowerCase(), ApproverRole.valueOf(role));
        Snapshot before = snapshot(invoice);

        if (allowed) {
            Invoice result = service.approve(invoice.getId(), approver);

            boolean requiresSecond = new BigDecimal(amountText).compareTo(new BigDecimal("500000")) > 0;
            assertThat(result.getStatus())
                    .isEqualTo(requiresSecond ? InvoiceStatus.PENDING_APPROVAL : InvoiceStatus.APPROVED);
            assertThat(result.getAmount()).isEqualByComparingTo(amountText);
            if (requiresSecond) {
                assertThat(result.getDecidedBy()).isNull();
                assertThat(result.getDecidedAt()).isNull();
                assertThat(result.getApprovalsRequired()).isEqualTo(2);
                assertThat(result.getApprovalsRecorded()).isEqualTo(1);
            } else {
                assertThat(result.getDecidedBy()).isEqualTo(approver.username());
                assertThat(result.getDecidedAt()).isNotNull();
            }
            assertThat(result.getApprovals()).hasSize(1);
            assertThat(result.getApprovals().get(0).username()).isEqualTo(approver.username());
            assertThat(result.getApprovals().get(0).role()).isEqualTo(approver.role().label());
            assertThat(result.getApprovals().get(0).approvedAt()).isNotNull();
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
    void approve_highValueInvoiceRequiresTwoDistinctDirectors() {
            Invoice invoice = invoiceWith("INV-TWO", InvoiceStatus.PENDING_APPROVAL, new BigDecimal("500000.01"));
            ApprovalService service = serviceFor(invoice);

            Invoice firstResult = service.approve(invoice.getId(), director("demo.director"));

            assertThat(firstResult.getStatus()).isEqualTo(InvoiceStatus.PENDING_APPROVAL);
            assertThat(firstResult.getDecidedBy()).isNull();
            assertThat(firstResult.getDecidedAt()).isNull();
            assertThat(firstResult.getApprovalsRequired()).isEqualTo(2);
            assertThat(firstResult.getApprovalsRecorded()).isEqualTo(1);
            assertThat(firstResult.getApprovals())
                    .extracting(ApprovalRecord::username)
                    .containsExactly("demo.director");

            Invoice secondResult = service.approve(invoice.getId(), director("demo.director2"));

            assertThat(secondResult.getStatus()).isEqualTo(InvoiceStatus.APPROVED);
            assertThat(secondResult.getDecidedBy()).isEqualTo("demo.director2");
            assertThat(secondResult.getDecidedAt()).isNotNull();
            assertThat(secondResult.getApprovalsRequired()).isEqualTo(2);
            assertThat(secondResult.getApprovalsRecorded()).isEqualTo(2);
            assertThat(secondResult.getApprovals())
                    .extracting(ApprovalRecord::username)
                    .containsExactly("demo.director", "demo.director2");
        }

    @Test
    void approve_managerCannotBeFirstHighValueApprover() {
            Invoice invoice = invoiceWith("INV-FIRST-MANAGER", InvoiceStatus.PENDING_APPROVAL, new BigDecimal("500000.01"));
            ApprovalService service = serviceFor(invoice);
            Snapshot before = snapshot(invoice);

            assertThatThrownBy(() -> service.approve(invoice.getId(), manager()))
                    .isInstanceOf(ApprovalRejectedException.class)
                    .hasMessageContaining("INV-FIRST-MANAGER")
                    .hasMessageContaining("500000.01")
                    .hasMessageContaining("500,000")
                    .hasMessageContaining("two distinct Directors");
            assertThat(snapshot(invoice)).isEqualTo(before);
        }

    @Test
    void approve_managerCannotBeSecondHighValueApprover() {
            Invoice invoice = invoiceWith("INV-SECOND-MANAGER", InvoiceStatus.PENDING_APPROVAL, new BigDecimal("500000.01"));
            ApprovalService service = serviceFor(invoice);
            service.approve(invoice.getId(), director("demo.director"));
            Snapshot before = snapshot(invoice);

            assertThatThrownBy(() -> service.approve(invoice.getId(), manager()))
                    .isInstanceOf(ApprovalRejectedException.class)
                    .hasMessageContaining("INV-SECOND-MANAGER")
                    .hasMessageContaining("500000.01")
                    .hasMessageContaining("second approver must be a Director");
            assertThat(snapshot(invoice)).isEqualTo(before);
        }

    @Test
    void approve_sameDirectorCannotApproveTwiceAfterNormalization() {
            Invoice invoice = invoiceWith("INV-SAME", InvoiceStatus.PENDING_APPROVAL, new BigDecimal("500000.01"));
            ApprovalService service = serviceFor(invoice);
            service.approve(invoice.getId(), director("Demo.Director"));
            Snapshot before = snapshot(invoice);

            assertThatThrownBy(() -> service.approve(invoice.getId(), director("  demo.director ")))
                    .isInstanceOf(ApprovalRejectedException.class)
                    .hasMessageContaining("INV-SAME")
                    .hasMessageContaining("500000.01")
                    .hasMessageContaining("two distinct Directors");
            assertThat(snapshot(invoice)).isEqualTo(before);
        }

    @Test
    void approve_refusesAlreadyApprovedInvoiceWithoutMutation() {
            Invoice invoice = invoiceWith("INV-ALREADY", InvoiceStatus.PENDING_APPROVAL, new BigDecimal("200.00"));
            ApprovalService service = serviceFor(invoice);
            service.approve(invoice.getId(), manager());
            Snapshot before = snapshot(invoice);

            assertThatThrownBy(() -> service.approve(invoice.getId(), director("demo.director")))
                    .isInstanceOf(InvalidInvoiceStateException.class);
            assertThat(snapshot(invoice)).isEqualTo(before);
        }

    @Test
    void approve_concurrentSameUserRecordsExactlyOneApproval() throws Exception {
            int requestCount = 16;
            Invoice invoice = invoiceWith("INV-CONCURRENT", InvoiceStatus.PENDING_APPROVAL, new BigDecimal("500000.01"));
            ApprovalService service = new ApprovalService(
                    new FakeInvoiceRepository(invoice), new SlowClock(Clock.systemUTC(), 30));
            CountDownLatch start = new CountDownLatch(1);
            ExecutorService executor = Executors.newFixedThreadPool(requestCount);
            List<Future<Boolean>> results = new ArrayList<>();

            try {
                for (int i = 0; i < requestCount; i++) {
                    results.add(executor.submit(() -> {
                        start.await();
                        try {
                            service.approve(invoice.getId(), director("Demo.Director"));
                            return true;
                        } catch (ApprovalRejectedException e) {
                            return false;
                        }
                    }));
                }
                start.countDown();
                int accepted = 0;
                for (Future<Boolean> result : results) {
                    if (result.get(5, TimeUnit.SECONDS)) {
                        accepted++;
                    }
                }

                assertThat(accepted).isEqualTo(1);
                assertThat(invoice.getApprovals()).hasSize(1);
                assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PENDING_APPROVAL);
                assertThat(invoice.getApprovalsRecorded()).isEqualTo(1);
            } finally {
                executor.shutdownNow();
            }
        }

    private static final class SlowClock extends Clock {
            private final Clock delegate;
            private final long delayMillis;

            private SlowClock(Clock delegate, long delayMillis) {
                this.delegate = delegate;
                this.delayMillis = delayMillis;
            }

            @Override
            public ZoneId getZone() {
                return delegate.getZone();
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return new SlowClock(delegate.withZone(zone), delayMillis);
            }

            @Override
            public Instant instant() {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while slowing the approval clock.", e);
                }
                return delegate.instant();
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
