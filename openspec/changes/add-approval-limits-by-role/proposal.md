# Proposal

## Why

The current approval flow lets any signed-in approver approve a pending invoice in one step, without checking the invoice amount or recording multiple approvals. Role-based limits and an explicit history are needed so approval authority is enforced consistently and can be verified.

## What Changes

- Add approval rules for Manager and Director roles, including the 50,000 and 500,000 boundaries.
- Require two distinct Director approvals for invoices above 500,000.
- Preserve invoice state and approval history when an attempt is refused, and return a clear HTTP 403 error body.
- Record ordered approval history with each approver's identity, role, and approval time.
- Define validation for approver identity, role, and invoice amount.
- Add Manager and Director demo accounts while documenting how existing `Approver`, `Admin`, and `demo.approver` behavior is handled.

## Capabilities

### New Capabilities
- `approval-limits`: Role-based invoice approval limits, multi-approver decisions, refusal behavior, and approval history.

### Modified Capabilities
- None.

## Impact

- Domain: approval policy and service, invoice approval state, and approval history.
- API: authenticated approval identity and role, approval responses, and HTTP 403 error mapping.
- Configuration: demo role accounts.
- Tests: domain policy/service and API coverage; later UI and Playwright coverage.
