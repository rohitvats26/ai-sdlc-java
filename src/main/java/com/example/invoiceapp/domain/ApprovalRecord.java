package com.example.invoiceapp.domain;

import java.time.Instant;

public record ApprovalRecord(String username, String role, Instant approvedAt) {
}
