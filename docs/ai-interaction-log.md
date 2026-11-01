# AI interaction log

Format and commands: `docs/ai-log/SCHEMA.md`. Source of truth: `docs/ai-log/ai-log.jsonl`.

Provenance of the starting repo: the baseline Spring Boot app (a Java port of the .NET demo app's existing behaviour: sign-in, invoices, submit, approve, reject) and the starter scaffolding (AGENTS.md rules table, `.github` prompts, agents and instructions, scripts, git hooks, runbook) were drafted by an AI assistant before development began. The baseline had not been built with Maven by its author. Development of the approval-limits feature is logged below.

_This page is generated from `docs/ai-log/ai-log.jsonl` by `python scripts/ai_log.py render`. Do not edit it by hand._

## At a glance

| | |
| --- | --- |
| AI interactions | **1** (plus 0 automatic commit records) |
| Tools and models | GitHub Copilot · gpt-4o-mini-2024-07-18 |
| Modes | agent × 1 |
| Agent types | built-in × 1 |
| Skills used | ai-log, approval-rules, openspec-propose |
| Sub-agents used | none |
| MCP tools used | none |
| Reviewed by a human | **0 of 1** interactions |
| With checking evidence (tests or validation) | **1 of 1** interactions |
| Outcomes | partially completed × 1 |

## Timeline

| # | When (UTC) | Step | Request | Model | Outcome |
| --- | --- | --- | --- | --- | --- |
| 1 | 2026-11-01 08:22 | spec | Step 1. Check the baseline, then write the OpenSpec change for approv… | gpt-4o-mini-2024-07-18 | 🟡 partially completed |

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

