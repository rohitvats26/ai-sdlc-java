# Tasks

## 1. Single-Approver Limits

- [ ] 1.1 Add Manager/Director approver types and a pure policy for the 50,000 and 500,000 limits; verify boundary and role cases with `mvn -q -Dtest=ApprovalPolicyTest test`.
- [ ] 1.2 Wire single-approval decisions to the authenticated session, preserve existing non-approval role behavior, and add Manager/Director demo accounts; verify role authorization and HTTP error bodies with `mvn -q -Dtest=ApprovalServiceTest,InvoiceApiTest test`.
- [ ] 1.3 Document the new demo accounts and approval roles in the README; verify the documented usernames and roles match `application.yml`.

## 2. Two-Approver Rule

- [ ] 2.1 Require two distinct Directors above 500,000, keep the invoice pending after the first approval, and complete it only after the second; verify 500,000.01 behavior, normalized identity checks, history order, and refusal immutability with `mvn -q -Dtest=ApprovalPolicyTest,ApprovalServiceTest test`.
- [ ] 2.2 Serialize each approval's lookup, validation, policy evaluation, and mutation; verify concurrent attempts cannot reuse one Director with the focused concurrency tests in `ApprovalServiceTest`.
- [ ] 2.3 Cover invalid approver names, unknown roles, non-positive amounts, and clear HTTP 403 `{ "error": reason }` responses; verify with `mvn -q -Dtest=ApprovalServiceTest,InvoiceApiTest test`.

## 3. UI and Playwright

- [ ] 3.1 Show approval progress and actionable refusal reasons in the UI without changing established element IDs or accessible names; verify the app builds and the existing UI selectors remain available.
- [ ] 3.2 Add self-contained Playwright coverage for a refused attempt and a valid control path using page objects and role-based locators; verify discovery with `cd e2e; npx playwright test --list` (the human runs the browser tests).

## 4. Mutation Testing and Review

- [ ] 4.1 Run `python scripts/pit.py before` before strengthening tests and `python scripts/pit.py after` after; verify saved before/after reports and score text with `python scripts/pit.py check`.
- [ ] 4.2 Review the completed diff against the spec and record a verdict for every finding; verify the final findings file with `python3 scripts/hooks/validate-findings.py --final docs/review/findings.json`.
