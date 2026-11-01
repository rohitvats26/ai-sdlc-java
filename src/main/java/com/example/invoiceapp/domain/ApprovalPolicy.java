package com.example.invoiceapp.domain;

import java.math.BigDecimal;

public final class ApprovalPolicy {
    private static final BigDecimal MANAGER_LIMIT = new BigDecimal("50000");
    private static final BigDecimal DIRECTOR_LIMIT = new BigDecimal("500000");

    private ApprovalPolicy() {
    }

    public static void ensureMayApprove(String invoiceId, BigDecimal amount, Approver approver) {
        if (approver == null || approver.username() == null || approver.username().isBlank()) {
            throw new ApprovalRejectedException(
                    "Invoice %s requires a valid Manager or Director username.".formatted(invoiceId));
        }
        if (amount == null || amount.signum() <= 0) {
            throw new ApprovalRejectedException(
                    "Invoice %s has an invalid amount; a positive amount is required.".formatted(invoiceId));
        }
        if (approver.role() == null || approver.role() == ApproverRole.UNKNOWN) {
            throw new ApprovalRejectedException(
                    "Invoice %s with amount %s requires an approver with the Manager or Director role."
                            .formatted(invoiceId, amount.toPlainString()));
        }
        if (amount.compareTo(DIRECTOR_LIMIT) > 0) {
            throw new ApprovalRejectedException(
                    ("Invoice %s with amount %s exceeds the 500,000 limit; two distinct Directors are required, "
                            + "and approval above this limit is unavailable in this step.")
                            .formatted(invoiceId, amount.toPlainString()));
        }
        if (approver.role() == ApproverRole.MANAGER && amount.compareTo(MANAGER_LIMIT) > 0) {
            throw new ApprovalRejectedException(
                    "Invoice %s with amount %s exceeds the 50,000 Manager limit; a Director is required."
                            .formatted(invoiceId, amount.toPlainString()));
        }
    }
}
