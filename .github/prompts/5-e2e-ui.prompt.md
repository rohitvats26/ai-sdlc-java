---
description: Step 5. Show approval progress in the UI and add the Playwright tests.
mode: agent
---
Goal: make approvals visible in the UI and prove a refused approval through the browser.

## Before you start (stop and tell me if a check fails)
Steps 2 to 4 are committed and `mvn test` is green. (I install the browser and run the Playwright tests myself.)

## Read only these
`src/main/resources/static/*`, `e2e/playwright.config.ts`, `e2e/package.json`, `AGENTS.md`.

## Skills (open the file if your Copilot does not load it for you)
- `playwright-e2e` (`.github/skills/playwright-e2e/SKILL.md`): page objects, locators, the three tests

## Plan first (in the chat)
The UI changes, and the title and steps of the three tests.

## UI (`src/main/resources/static`)
Add an "Approvals" column to the invoices table ("recorded of required", for example "1 of 2"); show who approved so far in the pending list; show the refusal reason in the existing alert (if a 403 has no message, show "You do not have permission to do that."). Keep existing element ids and accessible names.

## Playwright (`e2e/`)
Follow skill `playwright-e2e` (page objects, role-based locators, each test creates its own invoice).
- Test 1 (required): the title must contain the word **refused**. A Manager tries 50,000.01. Check the reason text, status still "Pending Approval", amount "$50,000.01", approvals "0 of 1".
- Test 2 (control): a Director approves the same amount.
- Test 3: a 600,000 invoice needs two different Directors; the same Director twice is refused; the second completes it.

## Rules
- Do NOT run the Playwright tests: not `npm test`, not `npx playwright test` (they open a visible browser; I run them myself). The only Playwright commands you may run are `cd e2e && npm install` and `npx playwright test --list`.
- `e2e/playwright.config.ts` is `headless: true` while you work: leave it. Do not change the API or domain.
- Only when everything below is done, as the first Finish action, run `python scripts/e2e_headless.py false` so the config is `headless: false` (visible browser) for my run and the demo. Keep `slowMo: 1000`.

## Done when
- `cd e2e && npx playwright test --list` shows the 3 tests and one title contains "refused" (show the output). `--list` loads the files, so syntax errors show up; it does not run anything.
- `mvn test` still passes.
- `e2e/playwright.config.ts` ends as `headless: false`.
- Reply with: files changed, the `--list` output, and these two commands for me to run: `cd e2e; npx playwright test -g "refused"` and then `npx playwright test`.
- In the log, say the tests were written but NOT run by you (`--action "Wrote 3 Playwright tests; not run, the human runs them"`).

## Finish (in this order; details in "Finish protocol" in `.github/copilot-instructions.md`)
0. Switch the browser to visible: `python scripts/e2e_headless.py false`, then `python scripts/e2e_headless.py show` must print `headless: false`.
1. Log: `python scripts/ai_log.py add --prompt-ref 5-e2e-ui --action "<one per real thing you did>" --tool-used "<only tools that ran>" --command-run "npx playwright test --list" --command-run "python scripts/e2e_headless.py false"`
2. Commit: `git add -A`, then `git commit -m "feat: show approval progress and add Playwright tests" -m "Why: users must see the refusal reason and approvals so far, and the browser test proves it"`. Adjust the wording to what you really changed. Skip if only `docs/ai-log/` changed.
3. Tell me the commit hash and message.
