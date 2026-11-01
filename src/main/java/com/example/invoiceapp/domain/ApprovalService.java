package com.example.invoiceapp.domain;

import java.time.Clock;
import java.time.Instant;

/**
 * Moves invoices through their lifecycle. Approval checks and mutations are serialized.
 */
public class ApprovalService {

    private final InvoiceRepository repository;
    private final Object approvalLock = new Object();
    private final Clock clock;

    public ApprovalService(InvoiceRepository repository) {
        this(repository, Clock.systemUTC());
    }

    ApprovalService(InvoiceRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public Invoice submit(String invoiceId) {
        Invoice invoice = find(invoiceId);

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new InvalidInvoiceStateException(
                    "Invoice %s is %s; only drafts can be submitted.".formatted(invoiceId, invoice.getStatus().label()));
        }

        invoice.setStatus(InvoiceStatus.PENDING_APPROVAL);
        repository.update(invoice);
        return invoice;
    }

    public Invoice approve(String invoiceId, Approver approver) {
        synchronized (approvalLock) {
            Invoice invoice = find(invoiceId);
            ensurePending(invoice);
            ApprovalPolicy.ensureMayApprove(invoiceId, invoice.getAmount(), approver, invoice.getApprovals());

            String username = approver.username().strip();
            Instant approvedAt = clock.instant();
            invoice.addApproval(new ApprovalRecord(username, approver.role().label(), approvedAt));
            if (invoice.getApprovalsRequired() == invoice.getApprovalsRecorded()) {
                invoice.setStatus(InvoiceStatus.APPROVED);
                invoice.setDecidedBy(username);
                invoice.setDecidedAt(approvedAt);
            }
            repository.update(invoice);
            return invoice;
        }
    }

    public Invoice reject(String invoiceId, String approver, String reason) {
        synchronized (approvalLock) {
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("A reason is required to reject an invoice.");
            }

            Invoice invoice = find(invoiceId);
            ensurePending(invoice);

            invoice.setStatus(InvoiceStatus.REJECTED);
            invoice.setDecidedBy(approver);
            invoice.setDecidedAt(clock.instant());
            invoice.setDecisionNote(reason);
            repository.update(invoice);
            return invoice;
        }
    }

    private Invoice find(String invoiceId) {
        return repository.getById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice %s was not found.".formatted(invoiceId)));
    }

    private static void ensurePending(Invoice invoice) {
        if (invoice.getStatus() != InvoiceStatus.PENDING_APPROVAL) {
            throw new InvalidInvoiceStateException(
                    "Invoice %s is %s; only pending invoices can be decided.".formatted(invoice.getId(), invoice.getStatus().label()));
        }
    }
}
