# Git and GitHub - Quick Reference

See the full study notes in [`Git and GitHub Study Notes.md`](../../../Notes/misc/development-tools/git-and-github-study-notes.md).

## Setup

1. Install Git (64 bit), default settings: `https://git-scm.com/download/win`
2. Create a GitHub account with your student email.
3. VS Code extensions to install:
   - GitHub Actions
   - GitHub Pull request and issues

## Core concepts

- Repository: where your code, files, and revision history live (the most
  basic element of GitHub).
- Branch: a pointer to a place in the project's history; a contained area to
  develop features, fix bugs, or experiment. You always branch off an
  existing branch (usually main). Beginners usually only need main.
- Commit: save your work before pushing.
- Conflict: when changes clash; resolve before merging into main or your
  branch.

## Workflow commands

| Step   | What you do |
|--------|-------------|
| Create | On github.com: dashboard -> New -> fill template; add collaborators in Settings -> Collaborators |
| Clone  | In VS Code: "Clone repository", follow the flow, pick your work folder |
| Pull   | Update local copy with remote changes (remote -> local) |
| Commit | Save your work before pushing |
| Push   | Move local changes to the remote (local -> remote) |
| Merge  | Bring branch work into main after resolving conflicts |

## Push / pull (in one line)

- Push = local to remote.
- Pull = remote to local.

## Good habits (rules and guidelines)

- Be in the correct branch; always pull from main before starting work.
- Save your work and commit before pushing.
- Resolve conflicts before merging into main or into your branch.
- Always push your work back into main when you finish.

## Resources

- GitHub official guide: `https://docs.github.com/en/get-started/start-your-journey/hello-world`
