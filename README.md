# Invoice approval demo (Spring Boot)

A small invoice approval app used for the AI in SDLC assignment. Java 17, Spring Boot 3, in-memory data, plain HTML/JS front end.

Stack: Java 21 / Spring Boot 3. Spec tool: OpenSpec. AI tool: GitHub Copilot.

## Project Demo & Pull Request

| Description | Link |
|---|---|
| 🎥 Demo Video | https://drive.google.com/file/d/1h5ppywi3nC_m0GgfmvWi3NcAa7Gqc2fC/view?usp=sharing |
| 🔀 Pull Request | https://github.com/rohitvats26/ai-sdlc-java/pull/1 |

## Run

```
mvn spring-boot:run
```
Open http://localhost:5000 and sign in with one of the demo accounts below.

## Demo accounts

All demo accounts use the password `ChangeMe123!`. These are local demo credentials, not real ones. They live in `src/main/resources/application.yml`.

| Username | Role | What it can do | Available |
| --- | --- | --- | --- |
| `demo.approver` | Approver | Reject invoices; cannot approve under the role limits | Baseline |
| `demo.admin` | Admin | Reject and delete invoices; cannot approve under the role limits | Baseline |
| `demo.manager` | Manager | Approve invoices up to 50,000 | Step 2 |
| `demo.director` | Director | First approver above 500,000; also approves up to 500,000 | Step 2 |
| `demo.director2` | Director | Second approver above 500,000 | Step 2 account; step 3 flow |

Keep this table in step with `application.yml`: if you add, rename or remove an account, update it in the same commit.

## What it does

Create an invoice, submit it for approval, then an approver approves it or rejects it with a reason. Invoices move Draft, Pending Approval, then Approved or Rejected.

## Approval limits

Amounts use `BigDecimal` and are compared exactly.

| Invoice amount | Who may approve | Approvals needed |
| --- | --- | --- |
| up to and including 50,000 | Manager or Director | 1 |
| above 50,000, up to and including 500,000 | Director only | 1 |
| above 500,000 | two distinct Directors | 2 |

- Above 500,000, `demo.director` can record the first approval and `demo.director2` can complete it. Both approvals must be by different Directors.
- A refused approval returns a clear reason (HTTP 403 with `{ "error": ... }`) and changes nothing on the invoice.


## Tests

```
mvn test                                   # unit and API tests
cd e2e && npm install && npx playwright install chromium && npm test   # end-to-end (starts the app itself)
python scripts/pit.py before    # mutation testing (PIT): run BEFORE strengthening tests, then:
python scripts/pit.py after     # scores and reports are saved in docs/mutation/ and written (before, after, change) into ASSIGNMENT.md; `python scripts/pit.py check` verifies it (plain: mvn test-compile org.pitest:pitest-maven:mutationCoverage)
```

**End-to-end tests open a real browser window** and run slowly on purpose (`headless: false`, `slowMo: 1000`, one second per action), so you can watch and record them. They need a desktop session. The config starts as `headless: true`; the `5-e2e-ui` prompt switches it to `false` when it finishes (`python scripts/e2e_headless.py false|true|show` flips it by hand). To run one test only, use `npx playwright test -g "<part of the test title>"`. For a faster, invisible run (for example on a build server), run `python scripts/e2e_headless.py true` and remove `slowMo` in `e2e/playwright.config.ts`.

## Layout

```
src/main/java/com/example/invoiceapp/domain   business rules (no Spring)
src/main/java/com/example/invoiceapp/api      controllers, security, error mapping
src/main/java/com/example/invoiceapp/infra    in-memory repository
src/main/resources/static                     web UI
src/test/java                                 JUnit 5 tests
e2e/                                          Playwright tests
openspec/                                     spec-driven change (created in step 1)
AGENTS.md                                     conventions for AI coding agents
docs/                                         use cases, AI log, review findings, Copilot runbook
```

## Working on the approval-limits feature

See [AGENTS.md](AGENTS.md) for the rules and [docs/copilot-runbook.md](docs/copilot-runbook.md) for the workflow with GitHub Copilot. AI usage is logged per prompt in `docs/ai-log/ai-log.jsonl` (readable summary: `docs/ai-interaction-log.md`; indented JSON: `docs/ai-log/ai-log.pretty.json`). Both readable files are rebuilt automatically after every `ai_log.py add` or `annotate`.
