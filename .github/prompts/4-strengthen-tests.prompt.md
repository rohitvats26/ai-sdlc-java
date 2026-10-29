---
description: Step 4. Review the tests and strengthen them using PIT mutation testing.
mode: agent
---
Goal: find tests that would still pass if a rule were wrong, and fix them. Tests only.

## Before you start (stop and tell me if a check fails)
Steps 2 and 3 are committed and `mvn test` is green (PIT needs green tests). Do not change any test until the "before" run in step 1 below is saved.

## Read only these
`src/test/java/**`, the domain classes PIT mutates, and `docs/mutation/before.txt` once it exists.

## Skills (open the file if your Copilot does not load it for you)
- `approval-tests` (`.github/skills/approval-tests/SKILL.md`): what to add for each surviving mutant

## Do, in order
1. Run `python scripts/pit.py before`. It runs PIT, saves the report in `docs/mutation/before/`, writes `docs/mutation/before.txt` (score and the list of surviving mutants), updates `docs/mutation-testing.md` and the "Mutation scores" block in `ASSIGNMENT.md`. If it fails, stop and show me the error. If step 4 was already done earlier, run `python scripts/pit.py before --at <commit of step 3>` instead: it rebuilds "before" from that commit.
2. Read `docs/mutation/before.txt` and the tests. Give a table of weak tests: name, what is weak (exception type only, no unchanged-state check, no message check, missing boundary), what to add.
3. For each surviving mutant add the missing assertion or test. If a mutant is equivalent (cannot change behaviour), say so and skip it; do not chase 100 percent.
4. Run `python scripts/pit.py after`. It saves `docs/mutation/after/` and `after.txt`, updates the table in `docs/mutation-testing.md` and writes both scores as text into `ASSIGNMENT.md`.

## Rules
- Never type, estimate or copy a score by hand: only `pit.py` writes them.
- Change tests only; if you find a production bug, report it and stop. Never delete or loosen a test.
- If `pit.py` says "before already exists", do not use `--force`: tell me.

## Done when
`mvn test` passes; `docs/mutation/before.txt`, `docs/mutation/after.txt` exist and the "Mutation scores" block in `ASSIGNMENT.md` shows both scores. Reply: both scores (copied from the script output), mutants killed per new test, mutants still alive and why.

## Finish (in this order; details in "Finish protocol" in `.github/copilot-instructions.md`)
1. Log: `python scripts/ai_log.py add --prompt-ref 4-strengthen-tests --action "<one per real thing you did>" --tool-used "<only tools that ran>" --run-tests "mvn -q test" --command-run "python scripts/pit.py before" --command-run "python scripts/pit.py after"`
2. Commit: `git add -A`, then `git commit -m "test: strengthen approval tests" -m "Why: the mutation run showed tests that would still pass if a rule were wrong"`. Adjust the wording to what you really changed. Skip if only `docs/ai-log/` changed.
3. Tell me the commit hash and message.
