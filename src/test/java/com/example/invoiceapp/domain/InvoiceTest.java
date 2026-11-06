package com.example.invoiceapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class InvoiceTest {

    @Test
    void exposesCreatedAtAndCustomerName() {
        Invoice invoice = new Invoice();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        invoice.setCreatedAt(createdAt);
        invoice.setCustomerName("Test School");

        assertThat(invoice.getCreatedAt()).isEqualTo(createdAt);
        assertThat(invoice.getCustomerName()).isEqualTo("Test School");
    }
}
