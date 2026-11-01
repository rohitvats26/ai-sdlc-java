# Pull request: role-based invoice approval limits

**Status:** Write-up prepared; pull request URL and demo video are Pending.

## Summary

- Enforce Manager and Director approval limits in the framework-free domain layer.
- Require two distinct Directors for invoices above 500,000; retain ordered approval history and leave the invoice pending after the first approval.
- Return HTTP 403 with a JSON `error` reason for approval-policy refusals. Validate before mutating invoice state.
- Show approval progress and history in the web UI, with role-based Playwright coverage.
- Clarify in the README that high-value invoices need two Directors.

## Business-rule coverage

| Rule | Evidence |
| --- | --- |
| Manager or Director may approve up to and including 50,000; only a Director may approve above 50,000 through 500,000. | `src/main/java/com/example/invoiceapp/domain/ApprovalPolicy.java`; `ApprovalPolicyTest`; `ApprovalServiceTest` |
| Above 500,000, two distinct Directors are required; the first approval is non-final. | `ApprovalService.java`; `ApprovalServiceTest`; `InvoiceApiTest` |
| Usernames are compared after trimming and ignoring case. | `ApprovalPolicy.java`; `ApprovalServiceTest` |
| Refusals preserve invoice state and history; invalid identity, role, or amount is rejected. | `ApprovalServiceTest`; `ApprovalPolicyTest`; `ApiExceptionHandler.java` |
| Each accepted approval is retained in order with username, role, and time. | `ApprovalRecord.java`; `ApprovalServiceTest`; `InvoiceApiTest` |

## Verification

- `mvn -q test`: 60 passed; 0 failed, errors, or skipped.
- `python scripts/pit.py check`: passed. Before: 53 of 72 mutants killed (73.6%); after: 72 of 72 (100.0%).
- `python scripts/hooks/validate-findings.py --final docs/review/findings.json`: passed.
- `npx playwright test --list` from `e2e`: three tests discovered. Browser tests were not run; the human must run them.

## AI tools and workflow

- GitHub Copilot, agent mode; model recorded in the AI log: `gpt-4o-mini-2024-07-18`.
- Skills recorded in the AI log: `ai-log`, `approval-rules`, `approval-tests`, `openspec-propose`, `playwright-e2e`, and `review-findings`.
- No sub-agents or MCP tools were recorded.
- The AI log contains six feature-step interactions and has no human annotations yet. See `docs/ai-interaction-log.md`.

## Review findings

| ID | Severity | Title | Recorded verdict | Reviewer note |
| --- | --- | --- | --- | --- |
| F-001 | Medium | API refusal test does not verify invoice immutability | Dismissed | The recorded rationale says domain service tests cover immutability. |
| F-002 | Low | Approval table implies a Manager may approve first above 500,000 | Dismissed | The recorded rationale repeats the API-immutability rationale and does not address this documentation issue. The README table is corrected here; confirm the human verdict rationale. |

Findings are in `docs/review/findings.json`. The verdicts and reasons are preserved as recorded by the reviewer.

## Assumptions and known limits

- Both approvals above 500,000 must be from Directors.
- Identity comparison trims surrounding whitespace and ignores letter case.
- Legacy `Approver` and `Admin` accounts do not gain approval authority.
- Missing, zero, and negative invoice amounts are invalid for approval.
- Approval history is in memory and is lost when the application restarts.
- Playwright tests have only been listed, not executed in a browser.
- Demo video and pull request links remain Pending until supplied.
