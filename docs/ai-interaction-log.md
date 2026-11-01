# AI interaction log

Format and commands: `docs/ai-log/SCHEMA.md`. Source of truth: `docs/ai-log/ai-log.jsonl`.

Provenance of the starting repo: the baseline Spring Boot app (a Java port of the .NET demo app's existing behaviour: sign-in, invoices, submit, approve, reject) and the starter scaffolding (AGENTS.md rules table, `.github` prompts, agents and instructions, scripts, git hooks, runbook) were drafted by an AI assistant before development began. The baseline had not been built with Maven by its author. Development of the approval-limits feature is logged below.

_This page is generated from `docs/ai-log/ai-log.jsonl` by `python scripts/ai_log.py render`. Do not edit it by hand._

## At a glance

| | |
| --- | --- |
| AI interactions | **2** (plus 0 automatic commit records) |
| Tools and models | GitHub Copilot · gpt-4o-mini-2024-07-18 |
| Modes | agent × 2 |
| Agent types | built-in × 2 |
| Skills used | ai-log, approval-rules, approval-tests, openspec-propose |
| Sub-agents used | none |
| MCP tools used | none |
| Reviewed by a human | **0 of 2** interactions |
| With checking evidence (tests or validation) | **2 of 2** interactions |
| Outcomes | partially completed × 2 |

## Timeline

| # | When (UTC) | Step | Request | Model | Outcome |
| --- | --- | --- | --- | --- | --- |
| 1 | 2026-11-01 08:22 | spec | Step 1. Check the baseline, then write the OpenSpec change for approv… | gpt-4o-mini-2024-07-18 | 🟡 partially completed |
| 2 | 2026-11-01 08:32 | implement | Step 2. Manager and Director limits only (task group 1) | gpt-4o-mini-2024-07-18 | 🟡 partially completed |

## Interactions in detail

### 1. Step 1. Check the baseline, then write the OpenSpec change for approval limits by role. No code  🟡 partially completed

| | |
| --- | --- |
| **When** | 2026-11-01 08:22:59 UTC (step: spec) |
| **Tool, model, mode** | GitHub Copilot · gpt-4o-mini-2024-07-18 (auto-detected from the Copilot Chat log) · agent |
| **Agent** | Agent (built-in) |
| **Skills · sub-agents · MCP** | ai-log, approval-rules, openspec-propose · none · none |
| **Result** | applied → partially completed |

**Prompt 1:** `/1-spec` (from `.github/prompts/1-spec.prompt.md`)

<details><summary>Show the full prompt text</summary>

~~~~text
Goal: a validated spec for approval limits by role. Spec only, no code.

## Before you start (stop and tell me if a check fails)
- Run `mvn test`. Note: `Baseline: <N> tests, <pass/fail>`. If it is red, stop.
- `openspec --version` works. If not, tell me to run `npm i -g @fission-ai/openspec`.

## Read only these
"Feature being built" in `AGENTS.md`, `src/main/java/**` (skim), `src/test/java/**` (skim), `application.yml`.

## Skills (open the file if your Copilot does not load it for you)
- `approval-rules` (`.github/skills/approval-rules/SKILL.md`): the rules table and traps
- `openspec-propose` (`.github/skills/openspec-propose/SKILL.md`): how to create the OpenSpec change

## Plan first (in the chat)
Max 8 bullets: the requirements you will write and every ambiguity you found.

## Do
- Use OpenSpec to create the change `add-approval-limits-by-role`: proposal, specs, design, tasks.
- Requirements (each uses SHALL, each has WHEN/THEN scenarios): Manager limit (up to 50,000); Director limit (up to 500,000); two distinct approvers above 500,000, the second a Director; same user never counts twice; a refused attempt returns a clear reason and changes nothing (status, amount, decision fields, history); input checks (blank approver, unknown role, invalid amount); approval history (who, role, when, in order).
- Boundary scenarios: 50,000, 50,000.01, 500,000, 500,000.01. API: refusal is HTTP 403 with `{ "error": reason }`.
- `design.md`: per ambiguity write the question, options, the safe option you chose, the consequence. Include at least: may a Manager be first approver above 500,000? Does "same user" ignore case? What happens to the `Approver` and `Admin` roles and `demo.approver`? Add a table: requirement to planned test class.
- `tasks.md` in groups, each task with a "verify" line: (1) single-approver limits, (2) two-approver rule, (3) UI and Playwright, (4) mutation testing and review.

## Rules
Do not touch `src/`. Do not invent rules that are not in `AGENTS.md`; write them as questions.

## Done when
- `openspec validate add-approval-limits-by-role --strict` passes (show it); every requirement has a scenario.
- Reply with `{"baseline":"<N tests, pass>","assumptions":[],"open_questions":[],"validated":true}`.

## Finish (in this order; details in "Finish protocol" in `.github/copilot-instructions.md`)
1. Log: `python scripts/ai_log.py add --prompt-ref 1-spec --action "<one per real thing you did>" --tool-used "<only tools that ran>" --run-tests "mvn -q test" --command-run "openspec validate add-approval-limits-by-role --strict"`
2. Commit: `git add -A`, then `git commit -m "docs: spec for approval limits by role" -m "Why: the spec is written and validated before any code, so every later step has a source of truth"`. Adjust the wording to what you really changed. Skip if only `docs/ai-log/` changed.
3. Tell me the commit hash and message.
~~~~

</details>

**Instructions followed:** `.github/prompts/1-spec.prompt.md`

**What the AI did**
- Ran baseline mvn -q test: 11 tests passed.
- Checked OpenSpec CLI availability and confirmed there were no existing capability specs.
- Created proposal, approval-limits spec delta, design, and tasks artifacts.
- Validated with openspec validate add-approval-limits-by-role --strict: passed.
- Configured AI logging as GitHub Copilot agent after the logger reported missing configuration.

**My review (human oversight)**
- ⚠ Not reviewed yet. Add it: `python scripts/ai_log.py annotate last --human-correction "..." --validation "..." --outcome successful`

**Evidence**
- **Commit at logging time:** `1724ca7` on `feature/approval-limits-by-role`, "chore: headless e2e until step 5 ends, readable AI log, PIT scores in ASSIGNMEN…"
- **Files changed (1):** `openspec/changes/add-approval-limits-by-role/`
- **Unit tests:** 11 of 11 passed, 0 failed, 0 errors (report time 2026-11-01T08:23:15Z)
- **Ran:** `mvn -q test` finished with exit code 0
- **Command recorded:** `openspec validate add-approval-limits-by-role --strict`

### 2. Step 2. Manager and Director limits only (task group 1)  🟡 partially completed

| | |
| --- | --- |
| **When** | 2026-11-01 08:32:44 UTC (step: implement) |
| **Tool, model, mode** | GitHub Copilot · gpt-4o-mini-2024-07-18 (auto-detected from the Copilot Chat log) · agent |
| **Agent** | Agent (built-in) |
| **Skills · sub-agents · MCP** | ai-log, approval-rules, approval-tests · none · none |
| **Result** | applied → partially completed |

**Prompt 1:** `/2-single-approver` (from `.github/prompts/2-single-approver.prompt.md`)

<details><summary>Show the full prompt text</summary>

~~~~text
Goal: task group 1 of the spec only. No two-approver logic yet.

## Before you start (stop and tell me if a check fails)
Step 1 is committed (`openspec validate add-approval-limits-by-role --strict` passes) and `mvn test` is green now.

## Read only these
The spec, design and tasks in `openspec/changes/add-approval-limits-by-role/`, `AGENTS.md`, `src/main/java/**`, `src/test/java/**`.

## Skills (open the file if your Copilot does not load it for you)
- `approval-rules` (`.github/skills/approval-rules/SKILL.md`): rules, design and traps
- `approval-tests` (`.github/skills/approval-tests/SKILL.md`): refusal test recipe and boundaries

## Plan first (in the chat)
The classes you will add or change, and every test case (name and what it proves).

## Build
- Domain types: approver, role (Manager, Director), approval record (who, role, when), a "rejected" exception carrying a clear reason.
- A pure policy class that decides if an approver may approve and never changes the invoice. `ApprovalService` calls it first and changes the invoice only after it accepts.
- Manager up to 50,000, Director up to 500,000; anything above 500,000 is refused for everyone in this step. The refusal message names the limit, the amount and the invoice id; map it to HTTP 403 with `{ "error": reason }`.
- Add `demo.manager`, `demo.director`, `demo.director2` to `application.yml`; let Manager and Director call the approval endpoints.
- Update `README.md` now: the demo accounts table must match `application.yml`; add a short "Approval limits" section.

## Tests
Follow skill `approval-tests`. Cover: the four boundaries (50,000 / 50,000.01 / 500,000 / 500,000.01), blank approver, unknown role, invalid amount, invoice not pending. Every refusal test snapshots the invoice and proves nothing changed.

## Rules
`BigDecimal` with `compareTo`; no Spring in `domain`; update old tests to new signatures but never weaken or delete one; touch only what you need. If a test still fails after 3 attempts, stop and tell me; never change a rule or test to make it pass.

## Done when
`mvn test` passes; your reply has a table (spec requirement of group 1 to the test that proves it); no `TODO` or commented-out code. Do NOT build the two-approver rule, change the UI or edit the spec.

## Finish (in this order; details in "Finish protocol" in `.github/copilot-instructions.md`)
1. Log: `python scripts/ai_log.py add --prompt-ref 2-single-approver --action "<one per real thing you did>" --tool-used "<only tools that ran>" --run-tests "mvn -q test"`
2. Commit: `git add -A`, then `git commit -m "feat: enforce manager and director limits" -m "Why: approval authority must depend on the approver's role"`. Adjust the wording to what you really changed. Skip if only `docs/ai-log/` changed.
3. Tell me the commit hash and message.
~~~~

</details>

**Instructions followed:** `.github/prompts/2-single-approver.prompt.md`

**What the AI did**
- Added pure Manager and Director approval policy, role and approver types, approval records, and validation before invoice mutation.
- Wired authenticated session role and username to approval, mapped policy refusals to HTTP 403 JSON, and preserved legacy rejection authorization.
- Added Manager and Director demo accounts and updated README accounts and limit documentation.
- Added policy, service, and API tests for thresholds, input refusals, unchanged invoice state, and approval history.
- Resolved the test-edit instruction conflict according to the user's choice to follow the step prompt.

**My review (human oversight)**
- ⚠ Not reviewed yet. Add it: `python scripts/ai_log.py annotate last --human-correction "..." --validation "..." --outcome successful`

**Evidence**
- **Commit at logging time:** `11994ba` on `feature/approval-limits-by-role`, "docs: spec for approval limits by role"
- **Files changed (14):** `README.md`, `src/main/java/com/example/invoiceapp/api/ApiExceptionHandler.java`, `src/main/java/com/example/invoiceapp/api/ApprovalsController.java`, `src/main/java/com/example/invoiceapp/domain/ApprovalService.java`, `src/main/java/com/example/invoiceapp/domain/Invoice.java`, `src/main/resources/application.yml`, `src/test/java/com/example/invoiceapp/api/InvoiceApiTest.java`, `src/test/java/com/example/invoiceapp/domain/ApprovalServiceTest.java` and 6 more
- **Unit tests:** 43 of 43 passed, 0 failed, 0 errors (report time 2026-11-01T08:33:04Z)
- **Ran:** `mvn -q test` finished with exit code 0
- **Command recorded:** `openspec validate add-approval-limits-by-role --strict`

