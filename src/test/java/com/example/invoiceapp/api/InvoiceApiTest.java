package com.example.invoiceapp.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Request-to-response check of sign-in, role checks and the JSON shape the UI relies on. */
@SpringBootTest
@AutoConfigureMockMvc
class InvoiceApiTest {

    @Autowired
    private MockMvc mvc;

    private String tokenFor(String username) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"ChangeMe123!\"}".formatted(username)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    @Test
    void invoicesNeedSignIn() throws Exception {
        mvc.perform(get("/api/invoices")).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordIsRefused() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo.approver\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void seededPendingInvoiceHasTheStatusNameTheUiExpects() throws Exception {
        mvc.perform(get("/api/invoices/INV-1001").header("Authorization", "Bearer " + tokenFor("demo.approver")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PendingApproval"))
                .andExpect(jsonPath("$.customerName").value("Lakeside School District"));
    }

    @Test
    void onlyAdminsMayDelete() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/invoices/INV-1005")
                        .header("Authorization", "Bearer " + tokenFor("demo.approver")))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanApproveWithinTheirLimit() throws Exception {
        mvc.perform(post("/api/approvals/INV-1001/approve")
                        .header("Authorization", "Bearer " + tokenFor("demo.manager")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Approved"))
                .andExpect(jsonPath("$.decidedBy").value("demo.manager"))
                .andExpect(jsonPath("$.approvals[0].role").value("Manager"));
    }

    @Test
    void legacyApproverGetsStructuredApprovalRefusal() throws Exception {
        mvc.perform(post("/api/approvals/INV-1002/approve")
                        .header("Authorization", "Bearer " + tokenFor("demo.approver")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Manager or Director")))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("INV-1002")));
    }

    @Test
    void highValueInvoiceRequiresTwoDirectorsAndExposesApprovalCounts() throws Exception {
        String managerToken = tokenFor("demo.manager");
        String directorToken = tokenFor("demo.director");
        String secondDirectorToken = tokenFor("demo.director2");

        String created = mvc.perform(post("/api/invoices")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Two Director API Test\",\"amount\":500000.01}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String invoiceId = created.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mvc.perform(post("/api/invoices/{id}/submit", invoiceId)
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());

        mvc.perform(post("/api/approvals/{id}/approve", invoiceId)
                        .header("Authorization", "Bearer " + directorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PendingApproval"))
                .andExpect(jsonPath("$.approvalsRequired").value(2))
                .andExpect(jsonPath("$.approvalsRecorded").value(1))
                .andExpect(jsonPath("$.decidedBy").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.approvals[0].username").value("demo.director"));

        mvc.perform(post("/api/approvals/{id}/approve", invoiceId)
                        .header("Authorization", "Bearer " + secondDirectorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Approved"))
                .andExpect(jsonPath("$.approvalsRequired").value(2))
                .andExpect(jsonPath("$.approvalsRecorded").value(2))
                .andExpect(jsonPath("$.decidedBy").value("demo.director2"))
                .andExpect(jsonPath("$.approvals[0].username").value("demo.director"))
                .andExpect(jsonPath("$.approvals[1].username").value("demo.director2"));
    }
}
