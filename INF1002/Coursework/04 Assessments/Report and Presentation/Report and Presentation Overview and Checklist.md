# Report and Presentation - Overview and Checklist

## Contents
1. Scope
2. Report requirements (explicit)
3. Recommended report structure
4. Feedback on the report deliverable
5. Presentation and demonstration requirements (explicit)
6. Feedback on the video deliverable
7. GitHub deliverable requirements
8. Historical deadlines and page/time limits
9. Recommendations

---

## 1. Scope

- The report/presentation guidelines apply to the C project CMS report
  (10% component): they give a length, a recommended structure, and a
  presentation/demonstration structure for the 5% video component.
- Guidance drawn from earlier (Python) submissions describes the expected
  composition standard for the report, video, and GitHub deliverables; it
  applies to both projects.
- The C project also has report requirements and video limits (see
  [04 Assessments/C Project](../C%20Project/C%20Project%20Overview%20and%20Checklist.md)
  for the full content).

## 2. Report requirements (explicit)

Report/presentation guidelines:
- The report should clearly describe how your CMS program works, how it was
  developed, and how your team contributed.
- It should be concise, well-structured, and professional, showing clear
  understanding of your design, logic, and testing.
- Length: 10-15 pages (single column, font size 11 or greater, standard
  margins).
- The page count excludes the title page, table of contents, and appendix.
- The report must be self-contained: if the examiner needs to look into the
  appendix to understand your work, that content will count toward the main
  page limit.
- The structure below is a guide, not an exact template.

C project requirements:
- Must contain: a technical description of programme structure; descriptions
  of data structures used and why; a breakdown of each team member's
  contribution; a list of all test cases and validation.
- The report does not need to contain a user guide.
- There should be sufficient detail that someone else who has also studied
  INF1002 could implement a similar program without referring to your code.
- Formatting: no more than 15 pages, Times New Roman, font size 11 or
  greater, standard margins and borders; page count excludes title page,
  TOC, and appendix; appendix content the examiner MUST look at counts
  toward the page limit.

## 3. Recommended report structure

1. Title Page - project title; team ID and team name; team members (name,
   student ID, email); date of submission.
2. Introduction (1 to 2 pages) - what the CMS does and why it was created; a
   short description of the main features / Enhancement Features (e.g. open,
   insert, update, delete, query, save); a short description of the
   additional unique feature; the scope and goals of your project.
3. System Design and Implementation (6-8 pages):
   - Overall design / program flow: a simple diagram or flowchart showing how
     commands are processed; description of the main modules and their
     relationships (e.g. main loop, file operations, data manipulation
     functions).
   - Data structure design: explain the key structures (e.g.
     `struct StudentRecord`) and why you chose them.
   - Key functions: for each major operation (OPEN, INSERT, QUERY, UPDATE,
     DELETE, SAVE, ETC) explain the purpose of the function, key parameters
     and return values, how it works logically (in words, not full code), and
     any special error handling or validation you added.
   - Enhancement features: describe additional features like sorting,
     statistics, or validation improvements; explain briefly how you
     implemented them and why you chose that design.
4. Test Cases and Validation (3-4 pages) - document the test cases designed
   to verify the CMS works correctly; include a test table with columns:
   Test Case ID, Description, Input Command(s), Expected Output, Reason for
   Test, Actual Result. You should: cover all main functions and edge cases
   (e.g. duplicate IDs, invalid input); include at least 8-10
   meaningful/quality test cases; comment briefly on what you learned from
   testing (e.g. which bugs you discovered and fixed).
5. Team Contributions (1 page) - summarize what each team member worked on.
   You must also reflect on contributions from your AI-Teammate. Example
   table format: Team Member | Major Contributions, with rows for each member
   and a row for AI (e.g. "Logical flow, ideation for unique features").
6. Reflection (No pages!) - this will be a separate form sent to you closer
   to the end of the module.

## 4. Feedback on the report deliverable

- Purpose: the report records the team's academic effort in conceiving and
  constructing software that solves a context problem for a third party -
  "realising the context problem" means producing the software solution.
- Suggested underlying structure: the Software Development Life Cycle (SDLC);
  each SDLC stage should be reflected in the report sections (not necessarily
  as section titles).
- Specific items (explicitly called "Mandatory" or required):
  1. Title Page - Mandatory. Must contain: project type; unique project
     name; author(s); effective date.
  2. Table of Contents - Mandatory.
  3. Abstract / Executive Summary - summarises purpose and content.
  4. Introduction - reflects Requirements Stage of SDLC; briefly specify the
     context problem. Related works/literature optional.
  5. Methods - reflects Analysis & Design Stages; must include at least an
     Analysis subsection and a Design subsection.
     - Analysis Stage: gather and study business processes, algorithms,
       formulas, tools, libraries, etc.; list them.
     - Design Stage: produce high-level designs including at least three
       artefacts: System Diagram (Architecture); Algorithm (Software solution
       process); Main Tasks (Work breakdown).
       - Architecture: modular structure diagram; UML if trained, otherwise
         an illustrative diagram; module names should be nouns (not verbs).
       - Software solution process: steps from preconditions to
         postconditions; text/table or UML Activity Diagram; include data
         pre-processing as a step if applicable.
       - Work breakdown: cross-reference all work units with team members;
         text, table, or Gantt Chart.
  6. GitHub Code and Dataset - reflects Coding Stage; reference the GitHub
     link in the report; DO NOT insert screenshots of code; ensure the
     repository is accessible.
  7. Results and Insights - reflects Testing Stage; explain how the software
     was verified and validated (V&V); discuss testing methods (white/black
     box), performance metrics, accuracy, speed, etc.
  8. Conclusion and References.
  9. Appendices - supporting details not in the main body; must be labelled
     ("Appendix A", "Appendix B"); if only one, label it "Appendix".
     - Declaration of AI Use: SIT requires citation for AI use. Reference:
       `https://libguides.singaporetech.edu.sg/citation/aichatbot#s-lg-box-22537884`
       If AI was used, include citation in the report (preferably in an
       appendix).
     - No code screenshots in appendices either (if GitHub access is
       provided, screenshots are unnecessary).
- Reminder: "Please adhere to page count set in the project guide."

## 5. Presentation and demonstration requirements (explicit)

Report/presentation guidelines:
- Your team must submit a video presentation (15 total) that includes:
  - Participation / Presentation: every member must speak briefly; divide
    sections as you prefer (e.g. each person covers 1-2 functions).
  - System Demonstration: show your CMS running live; demonstrate all
    required functions (OPEN, INSERT, QUERY, UPDATE, DELETE, SHOW, SAVE),
    enhancement features, and unique feature; include a key/crucial test
    case to prove correctness.
  - Code Structure Overview: a clear, high-level walkthrough - file
    organization; key data structures (struct, arrays, etc.); how commands
    are processed; how file reading/writing is implemented. Focus on the
    logic flow - do not explain every line.
  - Time Management: keep it concise but informative - Max of 15 minutes
    total; ensure smooth, clear transitions between presenters.

C project requirements:
- Presentation (max 5 minutes): functions/features, code structure and data
  structures clearly presented; presentation time properly managed.
- Video demo of the developed CMS (max 10 minutes): all required functions
  demonstrated successfully; properly handle comprehensive test-cases
  developed by the team; demo time properly managed.
- For the Python project, the video is a suggested 15-30 minutes and each
  member must speak - see the
  [Python Project checklist](../Python%20Project/Python%20Project%20Overview%20and%20Checklist.md).

## 6. Feedback on the video deliverable

- Purpose: to show the software solution works - no crashing, freezing, or
  nonsense output. Reflects User Acceptance Testing (UAT), part of the
  Testing Stage of SDLC. If the customer is unconvinced the software is
  functional, reliable, and meets requirements, you lose the contract.
- Demonstrate each requirement and how your software meets it; show
  requirement -> demonstration -> next requirement, etc.
- Split-screen format is best; otherwise use stop-start editing.
- Avoid showing: code structures; unit/block testing; features not approved
  by customer.
- Do not add new features unless approved by the customer. Load/stress
  testing should only be shown if explicitly required.
- Video must be no longer than 10 minutes. Extensions require lab supervisor
  approval. (This is the Python-context limit; for the C project the
  15-minute total applies.)

## 7. GitHub deliverable requirements

- Purpose: enable inspection of your code (Coding Stage of SDLC), allowing
  markers to verify programming skills: naming conventions, input handling,
  modularity, maintainability, and extendibility.
- Repository must be accessible to visitors (public or invite academic
  markers).
- Include a README that summarises: project overview; team members;
  repository organisation and file structure; (optional) architecture and
  algorithm diagrams.

## 8. Historical deadlines and page/time limits

These are preserved as historical requirements for the 2025 run of INF1002.

| Limit | Value |
|-------|-------|
| Report length | 10-15 pages |
| Report length | no more than 15 pages (Times New Roman, font size 11+) |
| Report length | 12-15 pages (font size 12, single spacing) |
| Video total | max 15 minutes (presentation max 5 min + demo max 10 min) |
| Video (Python context) | no longer than 10 minutes |
| Video (Python project) | suggested ~15-30 minutes |
| Test cases in report | at least 8-10 meaningful/quality |
| C project final submission | 23:59 on 25 Nov 2025 (Tue) |
| C project peer evaluation | 23:59 on 26 Nov 2025 (Wed) |
| C project late penalty | 20% per day; >4 days not accepted |

## 9. Recommendations

These are recommendations, not explicit requirements:
- Write the report against the SDLC skeleton even though the C guidelines
  give a section list; the two are compatible (Introduction -> Requirements;
  System Design & Implementation -> Analysis/Design/Coding; Test Cases ->
  Testing; Team Contributions -> Work Breakdown).
- Keep the report standalone: anything that only exists in the appendix risks
  being counted toward the page limit.
- Draft the video as a storyboard of requirement -> demo, then cut to keep
  within the 15-minute (C) or suggested 15-30-minute (Python) window, with
  every member speaking.
- Set the GitHub repo to be visible to markers early; test that the link
  works before submission.
