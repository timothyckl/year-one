# C Project - Overview and Checklist

## Contents
1. Project brief
2. File database specification
3. Required operations
4. Enhancement features
5. Test case design and validation
6. Declaration of non-plagiarism
7. Report requirements
8. Historical timeline and deliverables
9. Late submission rules
10. Assessment criteria
11. Appendix A: sample command responses
12. Recommendations

---

## 1. Project brief

- Your team must implement a Class Management System (CMS), a simple database
  management system in C, using a command-line-like interface. No graphical
  UI is required.
- Background: a database is a structured collection of data organized for
  efficient storage, retrieval, and manipulation. A DBMS is software
  providing an interface for users interacting with databases; fundamental
  operations include Insert (Add), Query, Update (Edit), and Delete.

## 2. File database specification

- A file is used as the database to store all data records.
- Only ONE data table, "StudentRecords", needs to be implemented, with 4
  columns:
  - ID (integer), Name (string), Programme (string), Mark (float)
- A sample database file is provided ("Sample-CMS.txt").
- Name the database file using the convention `TeamName-CMS.txt`
  (e.g. `P1_1-CMS.txt`).

## 3. Required operations

The program must accept these commands from the user:

| Command    | Behaviour |
|------------|-----------|
| OPEN       | Open the database file and read in all the records. |
| SHOW ALL   | Display all current records in the read-in data. |
| INSERT     | Insert a new record. If a record with the same student ID already exists, display an error message and cancel. Otherwise ask the user for data for each column of the new record. |
| QUERY      | Search for a record with a given student ID. If found, display the record; if not, display a warning message. |
| UPDATE     | Update the data for a record with a given student ID. If no such record, display a warning; otherwise ask for the new data for each column and update. |
| DELETE     | Delete the record with a given student ID. If no such record, display a warning. Otherwise double-confirm with the user (Y/N); delete only if confirmed. |
| SAVE       | Save all current records into the database file. |

## 4. Enhancement features

- Sorting features: sort student records by Student ID (ascending or
  descending) and by Mark (ascending or descending). Sorted output should
  display correctly for commands such as `SHOW ALL SORT BY ID` or
  `SHOW ALL SORT BY MARK`.
- Summary statistics: summary commands, e.g. `SHOW SUMMARY` displays:
  - total number of students
  - average mark
  - highest and lowest mark (with student names)
- Unique feature: a unique feature derived by your team that demonstrates a
  number of features of C and fits naturally into your CMS system.

## 5. Test case design and validation

- Your team must come up with a comprehensive list of test cases to ensure
  the application works without errors.
- Sample format for reference:

| Test Case ID | Description | Input Commands(s) | Expected Output | Reason for Test | Actual Result |
|--------------|-------------|-------------------|-----------------|-----------------|---------------|
| TC05 | Update Record Successfully | `UPDATE ID=250001 Mark=88.0` | `CSM: The record with ID=250001 is successfully updated` | Verifies Update Logic | Pass |

- Identify any area in which the application may fail and test that the case
  has been accounted for.
- Test cases should be practically used while creating the application, and
  should also be listed in the report and executed in the video presentation.

## 6. Declaration of non-plagiarism

- The CMS program must output a declaration when it is started, covering the
  SIT plagiarism policy, statements that no code was copied from others or
  from AI-generated sources, that code was not shared/uploaded, and agreement
  to receive ZERO mark if plagiarism is detected.
- It must include: Declared by: Group Name; Team members (1-5, with XXX
  placeholders); Date of submission.

## 7. Report requirements

In addition to the code, submit a brief technical report that describes how
the program works and how it was constructed. It should contain:
- A technical description of how the programme is structured.
- Technical descriptions of what data structures (if any) were used and why
  these were chosen.
- A breakdown of the contribution made by each team member.
- A list of all test cases and validation (sample in section 2.4).

Formatting (explicit):
- No more than 15 pages, Times New Roman, font size 11 or greater, standard
  margins and borders.
- Page limit does not include the title page, table of contents, or appendix.
- The appendix can hold reference items, but the report should be standalone
  without the appendix: if the examiner MUST look at something in the
  appendix, it will be counted towards the page count.

## 8. Historical timeline and deliverables

Historical requirements for the 2025 run:

- By 23:59 on 25th November 2025 (Tuesday) - Final Submission:
  submit a zip file to your group's xSiTe Dropbox, including:
  - All C source code
  - The compiled and runnable .exe file
  - Report / Reflection
  - Presentation slides
  - The zip file must be named in the format `INF1002C-TeamX.zip`
    (e.g. `INF1002C-P1_1.zip`).
  - For the video presentation: one video uploaded to YouTube or a similar
    platform (can be marked unlinked/private to the module), with the link
    attached in your report and your zip file (URL link). Ensure your
    instructor has access before submitting the link.

- By 23:59 on 26th November 2025 (Wednesday) - Peer Evaluation:
  complete your peer evaluation on TEAMMATES.
  - Otherwise: a 10% penalty on your own peer evaluation mark, and the
    contribution credits will be evenly distributed among the other team
    members.

## 9. Late submission rules

- A penalty of 20% per day for each deliverable will be imposed for late
  submission unless extension has been granted prior to the submission date.
- Requests for extension are granted on a case-by-case basis.
- Any work submitted more than 4 days after the submission date will not be
  accepted and no mark will be awarded.

## 10. Assessment criteria

Each requirement/function is evaluated against the following criteria:

| Component | Weight | Details |
|-----------|--------|---------|
| Code | 80% | - Completeness and correctness (45%): all requirements implemented; correct results and expected behaviours; data structure properly chosen and used efficiently.<br>- Clarity (5%): well structured with good modularity; easy to read with clear and sufficient comments; meaningful variable/function names.<br>- Reliability (10%): no bugs or errors; sufficient data validation with error/boundary cases handled; proper error messages.<br>- Enhancement features (10%): to achieve the highest grade, all enhancement features must be implemented successfully.<br>- Test cases and validation (10%): comprehensive test cases verifying each function and edge condition; clear demonstration of coverage/correctness; well-documented test case tables with expected and actual results in the report. |
| Report | 10% | Logic flow with all required information (Section 3 of the brief); code structure and data structures clearly elaborated; well organized, easy to read, clear concise writing; free from grammatical errors and typos; list of all test cases; formatting limits as in section 7 above. |
| Individual Reflection | 5% | Clear and honest reflection of your own contributions; critical evaluation of the project (what went right/wrong); honest reflection of how AI and AI-Teammate was used. |
| Video Project presentation and demonstration | 5% | Presentation (max 5 minutes): functions/features, code structure and data structures clearly presented; presentation time properly managed.<br>Video demo of the developed CMS (max 10 minutes): all required functions demonstrated successfully; comprehensive test-cases developed by the team handled properly; demo time properly managed. |

- The grade for each individual member is weighted based on contribution and
  peer evaluation.

## 11. Appendix A: sample command responses

Conventions: program named "CMS", user named "P1_1", database file
"P1_1-CMS.txt". Example interactions:

- OPEN: `CMS: The database file "P1_1-CMS.txt" is successfully opened.`
- SHOW ALL: `CMS: Here are all the records found in the table "StudentRecords".`
  followed by a table of ID / Name / Programme / Mark rows.
- INSERT: duplicate ID -> `CMS: The record with ID=2301234 already exists.`
  new ID -> `CMS: A new record with ID=2401234 is successfully inserted.`
- QUERY: missing ID -> `CMS: The record with ID=2501234 does not exist.`
  found -> `CMS: The record with ID=2401234 is found in the data table.` plus
  the record row.
- UPDATE: missing ID -> does not exist message; found ->
  `CMS: The record with ID=2401234 is successfully updated.`
- DELETE: missing ID -> does not exist message; found -> confirm prompt:
  `CMS: Are you sure you want to delete record with ID=2401234? Type "Y" to
  Confirm or type "N" to cancel.` On N -> `CMS: The deletion is cancelled.`
  On Y -> `CMS: The record with ID=2301234 is successfully deleted.`
- SAVE: `CMS: The database file "P1_1-CMS.txt" is successfully saved.`

## 12. Recommendations

These are recommendations, not explicit requirements:
- Use the report/presentation structure recommended in the
  [Report and Presentation checklist](../Report%20and%20Presentation/Report%20and%20Presentation%20Overview%20and%20Checklist.md):
  an Introduction (1-2 pages), System Design and Implementation (6-8 pages),
  Test Cases and Validation (3-4 pages), Team Contributions (1 page), and a
  Reflection submitted separately, and the video total of 15 minutes.
- Include at least 8-10 meaningful/quality test cases in the report; the
  minimum requirement is "a comprehensive list" (see the
  [Report and Presentation checklist](../Report%20and%20Presentation/Report%20and%20Presentation%20Overview%20and%20Checklist.md)).
- The following guidance (see the
  [Report and Presentation checklist](../Report%20and%20Presentation/Report%20and%20Presentation%20Overview%20and%20Checklist.md))
  also applies to this project: no
  code screenshots in the report, no code-structure/unit-test content in the
  demo video, and accessible shared repos.
