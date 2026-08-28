# Git and GitHub - Study Notes

## Contents
1. Why Git (version control)
2. What GitHub is; what a repository is
3. Setting up (Git, account, VS Code extensions)
4. The five basic workflow commands
5. Creating a repository
6. Cloning into VS Code
7. Branching
8. Push and pull
9. Rules and guidelines
10. Additional resources

---

## 1. Why Git (version control)

- Git is a version control tool. It solves real problems:
  - "Forget what has been changed two weeks ago" - you can look back at the
    history of changes.
  - "How to combine many people's work" - merging contributions from a team.
- Bonus: if the team starts arguing, everyone's contributions can be checked
  from the history.

## 2. What GitHub is; what a repository is

- GitHub is a cloud-based platform where you can store, share, and work
  together with others to write code.
- Storing your code in a "repository" on GitHub lets you:
  - showcase or share your work;
  - track and manage changes to your code over time.
- A repository is the most basic element of GitHub: a place to store your
  code, your files, and each file's revision history (think of it as a
  library to store your files).

## 3. Setting up

1. Download Git (64 bit) before starting, with default settings:
   `https://git-scm.com/download/win`
2. Create a GitHub account using your student email.
3. Install these VS Code extensions:
   - GitHub Actions
   - GitHub Pull request and issues
- Note: GitHub may ask you to log in at some points, or the terminal may ask
  you to run some commands - just follow the prompts.

## 4. The five basic workflow commands

1. Creating a new repository
2. Cloning into VS Code
3. Branching
4. Push
5. Pull

## 5. Creating a repository

1. Access GitHub in the browser and go to the dashboard.
2. Click "New".
3. Follow the template and fill in the necessary fields.
4. Use the settings button and go to collaborators to add your group mates.
   Additional options under "General" can be explored freely.

## 6. Cloning a repository into VS Code

1. Click "Clone repository" and follow the flow accordingly.
2. Choose a folder to clone into - preferably your work folder that you can
   easily access for your projects.

## 7. Branching

- A branch is a pointer to a place in the project's history.
- Branches exist in both the local repository and the remote repository
  (e.g. main, feature1).
- You can create your own branch for new work; it is separate from main, but
  beginners usually only need to use main.
- Branches allow you to develop features, fix bugs, or safely experiment with
  new ideas in a contained area of your repository.
- You always create a branch from an existing branch; typically you create a
  new branch from the default branch (main) of the repository.

## 8. Push and pull

- Push: move changes from the local repo to the remote (GitHub).
- Pull: bring changes from the remote to the local.
- If you made changes on GitHub and want them locally, use git pull; if you
  made changes locally and want them on GitHub, use git push.

## 9. Rules and guidelines

- Check you are in the correct branch, and always pull your code from main
  before starting work on your branch.
- Save your work and commit your code before pushing.
- If there is a conflict, resolve it before merging work into the main branch
  or into your branch.
- Always push your work back into the main branch when you finish working on
  your code.

## 10. Additional resources

- Official GitHub guide (Hello World): `https://docs.github.com/en/get-started/start-your-journey/hello-world`
- The basics above are a starting point; search and explore further
  according to your requirements.

---

## Key takeaways for the INF1002 projects

- The Python and C projects require team collaboration on a shared codebase
  (GitHub access was a deliverable for the
  [Python project](../../../Projects/stalkingstocks/)).
- Minimum workflow for a team: each member clones the repo, pulls from main
  before starting work, works on their own branch, pushes, and the team
  merges after resolving conflicts.
