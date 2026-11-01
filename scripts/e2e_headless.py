#!/usr/bin/env python3
"""Switch `headless` in e2e/playwright.config.ts.

  python scripts/e2e_headless.py true     headless run (default while Copilot works on step 5)
  python scripts/e2e_headless.py false    visible browser (the human's run and the demo video)
  python scripts/e2e_headless.py show     print the current value

Only the `headless:` line changes (slowMo and everything else stay as they are).
"""
import re, sys

PATH = "e2e/playwright.config.ts"
PATTERN = re.compile(r"(^\s*headless:\s*)(true|false)(,?)", re.M)


def main():
    if len(sys.argv) != 2 or sys.argv[1] not in ("true", "false", "show"):
        sys.exit(__doc__)
    text = open(PATH, encoding="utf-8").read()
    match = PATTERN.search(text)
    if not match:
        sys.exit(f"{PATH}: no 'headless: true|false' line found")
    if sys.argv[1] == "show":
        print(f"headless: {match.group(2)}")
        return
    new = PATTERN.sub(lambda m: f"{m.group(1)}{sys.argv[1]}{m.group(3)}", text, count=1)
    with open(PATH, "w", encoding="utf-8", newline="\n") as handle:
        handle.write(new)
    print(f"{PATH}: headless: {sys.argv[1]}")


if __name__ == "__main__":
    main()
