# Stage 08 — Git init, commit, push and remote verification

This stage is **explicitly authorized by the user's instructions**: publish the completed and validated demo to the existing PUBLIC repository `https://github.com/rodrigotriani/bookstore-api.git`. Do not publish anywhere else.

## Pre-push safety checks
1. Confirm Stage 07 passed and the working tree is the intended Bookstore workspace. Run `pwd`, `git status` (when available), and inspect files to be committed.
2. Verify `.gitignore` excludes build output, secrets (`.env`, tokens, private keys, `*.jks`, `*.p12`) and editor/private machine configuration. Scan staged filenames and content reasonably. Do not include any secret; do not use data from unrelated projects.
3. Inspect remote GitHub state with `git ls-remote https://github.com/rodrigotriani/bookstore-api.git HEAD refs/heads/main`. The repository was empty at plan preparation, **but check again now**. If it now has commits or `main` content, STOP and report the situation; do not overwrite, force-push, rebase, merge or change history without user approval.
4. If `git` is already initialized locally and has commits, assess safely. Never reset or delete history; if conflicting/nontrivial, STOP.

## Publish clean initial history
5. If Git is not initialized: `git init -b main`. Then `git remote add origin https://github.com/rodrigotriani/bookstore-api.git` (or confirm existing origin matches EXACT target). Never overwrite a different remote silently.
6. `git add .` only after review. Include README, pom.xml, src/, .gitignore and `prompts/` for reproducibility. Recheck `git diff --cached --stat` and `git diff --cached --name-only`; make sure no `target/`, secret or local certificate was staged.
7. Commit with message `feat: build bookstore REST API with IBM Bob` (use an existing safe local Git author identity; do not fabricate one). If git author not configured, stop and ask for a legitimate identity.
8. Perform a normal `git push -u origin main` (NO `--force`). Use existing Git credential manager or user's interactive GitHub authorization if needed. Never print credentials or request password/token in the agent chat.
9. Verify success with `git ls-remote origin refs/heads/main` and local `git rev-parse HEAD`; they must match. Ideally open/read the repository's README or head through GitHub and confirm the commit exists.
10. If authentication/permissions fail or any non-fast-forward occurs, STOP and provide exact error and safe next step. Never retry with force push.

## Single consolidated final report
Report:
- Features/modules implemented and principal files.
- Actual Java/Maven/Spring Boot versions and `mvn clean verify` result.
- Actual test totals (executed, passed, failed, errors, skipped).
- Verified HTTP requests/status codes and any unverified scenarios.
- GitHub repository URL, target branch, **local and remote commit SHA** and push verification status; say `not published` if not verified.
- Significant warnings, blockers, known limitations and video readiness.

Do not claim a successful GitHub push without verified remote SHA.

## Existing Git repository and non-interactive publication

The Git repository is already initialized and configured.

- Preserve the existing `.git` directory and all local Git configuration.
- Do not execute `git init`, recreate the repository, or replace the existing SSH remote.
- Use the existing `origin` remote and `main` branch.
- Do not modify `core.sshCommand` or the configured SSH identity.
- Never request a GitHub username, password, token, or SSH passphrase.
- Use the existing non-interactive SSH authentication.
- Never use `git push --force` or `git push --force-with-lease`.
- Do not amend or reset the existing initial commit.
- Include the complete application, README.md, `.gitignore`, and all nine `prompts/*.md` files in the new commit.
- Run `mvn clean verify` successfully before committing.
- Push the new commit using `git push -u origin main`.
- After pushing, compare `git rev-parse HEAD` with `git ls-remote origin refs/heads/main`.
- Report publication as successful only when both commit hashes match.
- If authentication or publication fails, report the exact error and stop without changing Git credentials.