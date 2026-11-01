package com.example.invoiceapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InvoiceStatusTest {

    @Test
    void labelsMatchTheJsonContract() {
        assertThat(InvoiceStatus.DRAFT.label()).isEqualTo("Draft");
        assertThat(InvoiceStatus.PENDING_APPROVAL.label()).isEqualTo("PendingApproval");
        assertThat(InvoiceStatus.APPROVED.label()).isEqualTo("Approved");
        assertThat(InvoiceStatus.REJECTED.label()).isEqualTo("Rejected");
    }

    @Test
    void parseAcceptsLabelsAndEnumNamesIgnoringCaseSeparators() {
        assertThat(InvoiceStatus.parse("pending approval")).contains(InvoiceStatus.PENDING_APPROVAL);
        assertThat(InvoiceStatus.parse("PENDING_APPROVAL")).contains(InvoiceStatus.PENDING_APPROVAL);
        assertThat(InvoiceStatus.parse("approved")).contains(InvoiceStatus.APPROVED);
    }

    @Test
    void parseRejectsNullAndUnknownValues() {
        assertThat(InvoiceStatus.parse(null)).isEmpty();
        assertThat(InvoiceStatus.parse("unrecognized")).isEmpty();
    }
}
