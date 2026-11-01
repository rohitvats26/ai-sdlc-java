# Design

## Context

See `proposal.md` for the motivation and `specs/approval-limits/spec.md` for the behavior contract. The current service approves a pending invoice in one operation; the repository returns live invoice instances. The API obtains a username and role from an authenticated session, and currently uses `Approver` and `Admin` roles.

## Goals / Non-Goals

**Goals:**
- Keep the approval decision independent of Spring and ensure no invoice mutation occurs before validation succeeds.
- Serialize each invoice's read, policy check, and write so concurrent attempts cannot bypass the two-approver rule.
- Keep approval identity tied to the authenticated session and make accepted approvals auditable.

**Non-Goals:**
- Change invoice persistence, which remains in memory.
- Change invoice rejection behavior or its existing `Approver`/`Admin` authorization.
- Add partial-amount approvals or a configurable approval matrix.

## Decisions

### High-value first approver

- **Question:** May a Manager provide the first approval for an invoice above 500,000 if a Director provides the second?
- **Options:** Allow a Manager as first approver and require a Director second; require both approvers to be Directors.
- **Chosen safe option:** Both approvers must be Directors. A Manager has no authority above 50,000.
- **Consequence:** A Manager cannot contribute to either approval in the high-value two-person workflow.

### Identity comparison

- **Question:** Does the distinct-user check treat case or surrounding whitespace as significant?
- **Options:** Compare usernames exactly; trim surrounding whitespace and ignore case.
- **Chosen safe option:** Normalize by trimming surrounding whitespace and comparing without regard to case.
- **Consequence:** `Demo.Director` and ` demo.director ` are the same approver and cannot satisfy the two-person requirement.

### Existing `Approver`, `Admin`, and `demo.approver`

- **Question:** How do existing roles and the existing demo account participate after introducing Manager and Director?
- **Options:** Map legacy roles to an approval role; preserve approval access for legacy roles; retain their non-approval behavior but require Manager or Director for approvals.
- **Chosen safe option:** Do not map `Approver` or `Admin` to either approval role. Keep the existing accounts and their non-approval behavior; `demo.approver` remains available to sign in, but cannot approve under the new role policy. Add distinct Manager and Director demo accounts as described in `AGENTS.md`.
- **Consequence:** Approval demonstrations use the new role accounts. Admin-only invoice deletion and the existing rejection authorization remain separate from approval limits.

### Invalid amount

- **Question:** What values make an invoice amount invalid for approval?
- **Options:** Reject only a missing amount; also reject zero and negative amounts.
- **Chosen safe option:** Reject a missing amount or an amount that is not greater than zero, consistent with the existing invoice creation API.
- **Consequence:** The approval service also validates the stored amount, including invoices that did not originate from the current creation endpoint.

### Policy and mutation boundary

- **Decision:** Keep the approval policy pure and apply accepted decisions in the domain service. Build the approver from the authenticated session rather than accepting identity or role from the request body. Perform repository read, pending-state and input validation, policy evaluation, and writes under one lock.
- **Alternatives considered:** Put checks in the controller, or mutate the live invoice as each rule is checked. Both make the rule harder to test independently and risk partial changes or races.
- **Rationale:** Domain rules remain framework-independent, and refusals cannot leave a partially changed live invoice.

### Approval history and final decision

- **Decision:** Append a history entry only after a particular approval is accepted. For invoices above 500,000, the first Director's entry leaves the invoice pending and does not populate final decision fields; the second distinct Director's accepted entry completes approval.
- **Alternatives considered:** Treat the first approval as final, or store only the latest approver in the existing decision fields.
- **Rationale:** Neither alternative represents the required two-person decision or preserves an audit of both approvals.

### Requirement-to-test mapping

| Requirement | Planned test class |
| --- | --- |
| Manager and Director approval limits | `ApprovalPolicyTest`, `ApprovalServiceTest` |
| Two Director approvals above 500,000 | `ApprovalServiceTest` |
| Approver identity is distinct | `ApprovalServiceTest` |
| Approval refusals are explicit and leave invoices unchanged | `ApprovalServiceTest`, `InvoiceApiTest` |
| Approval input is valid | `ApprovalPolicyTest`, `ApprovalServiceTest`, `InvoiceApiTest` |
| Approval history records each approval in order | `ApprovalServiceTest`, `InvoiceApiTest` |
| Approval progress and refusal are understandable in the UI | Playwright approval-flow test |

## Risks / Trade-offs

- **[Concurrent requests could both observe one remaining approval]** → Hold a single lock across invoice lookup, validation, policy evaluation, and mutation; add a concurrency test.
- **[Legacy users may expect the old approve access]** → Keep existing accounts and document that approval authority moves to the new Manager/Director accounts; exercise role refusals through the API.
- **[The in-memory history is lost on restart]** → Accept this limitation for the demo application, which has no persistent database.

## Migration Plan

Add the domain approval data and policy, wire the approval endpoint to the authenticated session, configure the Manager and Director demo accounts, and update the UI and tests. No persisted data migration is needed. Roll back by reverting the feature changes; the in-memory approval history has no durable state to migrate.

## Open Questions

None. The behavior-affecting questions above are resolved as explicit assumptions for this change.
