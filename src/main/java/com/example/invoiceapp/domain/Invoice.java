package com.example.invoiceapp.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Invoice {
    private String id = "";
    private String customerName = "";
    private BigDecimal amount = BigDecimal.ZERO;
    private InvoiceStatus status = InvoiceStatus.DRAFT;
    private Instant createdAt = Instant.now();
    private String decidedBy;
    private Instant decidedAt;
    private String decisionNote;
    private final List<ApprovalRecord> approvals = new ArrayList<>();

    public Invoice() {
    }

    public Invoice(String id, String customerName, BigDecimal amount, InvoiceStatus status) {
        this.id = id;
        this.customerName = customerName;
        this.amount = amount;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getDecidedBy() { return decidedBy; }
    public void setDecidedBy(String decidedBy) { this.decidedBy = decidedBy; }
    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }
    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }
    public List<ApprovalRecord> getApprovals() { return List.copyOf(approvals); }
    public int getApprovalsRequired() {
        return amount != null && amount.compareTo(new BigDecimal("500000")) > 0 ? 2 : 1;
    }
    public int getApprovalsRecorded() { return approvals.size(); }

    public void addApproval(ApprovalRecord approval) {
        approvals.add(approval);
    }
}
