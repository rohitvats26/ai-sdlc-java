# Spec Delta

## Purpose

Defines the authority and approval sequence for invoices by amount, including safe refusal behavior and an auditable record of each approval.

## ADDED Requirements

### Requirement: Manager and Director approval limits
The system SHALL allow a Manager or Director to approve invoices of 50,000 or less, and SHALL allow only a Director to approve invoices above 50,000 and up to and including 500,000.

#### Scenario: Manager approves at the Manager limit
- **WHEN** a Manager approves a pending invoice for 50,000
- **THEN** the invoice is approved

#### Scenario: Manager is refused above the Manager limit
- **WHEN** a Manager attempts to approve a pending invoice for 50,000.01
- **THEN** the attempt is refused and the invoice remains unchanged

#### Scenario: Director approves at the Manager limit
- **WHEN** a Director approves a pending invoice for 50,000
- **THEN** the invoice is approved

#### Scenario: Director approves at the Director limit
- **WHEN** a Director approves a pending invoice for 500,000
- **THEN** the invoice is approved

#### Scenario: Director approval is required above the Manager limit
- **WHEN** a Manager attempts to approve a pending invoice for 50,000.01 or 500,000
- **THEN** the attempt is refused and the invoice remains unchanged

### Requirement: Two Director approvals above 500,000
The system SHALL require two approvals from distinct Directors for an invoice above 500,000, and SHALL complete approval only after the second qualifying approval.

#### Scenario: First Director approval leaves the invoice pending
- **WHEN** a Director gives the first approval to an invoice for 500,000.01
- **THEN** the approval is recorded and the invoice remains pending without final decision fields

#### Scenario: Second distinct Director completes approval
- **WHEN** a different Director approves an invoice for 500,000.01 that has one Director approval
- **THEN** the second approval is recorded and the invoice becomes approved

#### Scenario: Manager cannot provide the first high-value approval
- **WHEN** a Manager attempts the first approval of an invoice for 500,000.01
- **THEN** the attempt is refused and the invoice remains unchanged

#### Scenario: Manager cannot provide the second high-value approval
- **WHEN** a Manager attempts the second approval of an invoice for 500,000.01
- **THEN** the attempt is refused and the invoice remains unchanged

### Requirement: Approver identity is distinct
The system SHALL treat approver usernames as the same identity when they match after trimming surrounding whitespace and ignoring letter case, and SHALL NOT count one identity as both required approvers.

#### Scenario: Same Director attempts to approve twice
- **WHEN** the first Director attempts a second approval using a username that differs only by letter case or surrounding whitespace
- **THEN** the second attempt is refused and the invoice remains unchanged

#### Scenario: Different Directors are distinct approvers
- **WHEN** two Directors with different normalized usernames approve an invoice above 500,000
- **THEN** they count as two distinct approvers

### Requirement: Approval refusals are explicit and leave invoices unchanged
The system SHALL refuse an approval that violates an approval rule with HTTP 403 and a JSON body containing an `error` reason, and SHALL leave the invoice status, amount, decision fields, and approval history unchanged.

#### Scenario: Refusal explains the required authority
- **WHEN** an approver attempts an approval they are not authorized to make
- **THEN** the response is HTTP 403 with an `error` reason that identifies the invoice, amount, applicable limit, and required approver authority

#### Scenario: Refusal preserves invoice data and history
- **WHEN** an approval attempt is refused for any approval-rule violation
- **THEN** the invoice status, amount, decision fields, and existing approval history are unchanged

### Requirement: Approval input is valid
The system SHALL refuse an approval when the authenticated approver has a blank username, has a role other than Manager or Director, or the invoice amount is missing or not greater than zero.

#### Scenario: Blank approver username
- **WHEN** an approval is attempted with a blank authenticated username
- **THEN** the attempt is refused with HTTP 403 and the invoice remains unchanged

#### Scenario: Unknown or legacy role
- **WHEN** an approval is attempted by a user whose role is not Manager or Director
- **THEN** the attempt is refused with HTTP 403 and the invoice remains unchanged

#### Scenario: Missing or non-positive invoice amount
- **WHEN** an approval is attempted for an invoice whose amount is missing, zero, or negative
- **THEN** the attempt is refused with HTTP 403 and the invoice remains unchanged

### Requirement: Approval history records each approval in order
The system SHALL retain an ordered history entry for each accepted approval, containing the approver username, role, and approval time.

#### Scenario: Record first high-value approval
- **WHEN** the first Director approves an invoice above 500,000
- **THEN** the history contains one entry with that Director's username, role, and approval time

#### Scenario: Append second high-value approval
- **WHEN** a distinct Director completes approval of an invoice above 500,000
- **THEN** the history contains both entries in the order the approvals were accepted

#### Scenario: Refusal adds no approval history
- **WHEN** an approval attempt is refused
- **THEN** no approval history entry is added
