# Python Project - Overview and Checklist

## Contents
1. Objectives
2. Historical timeline and deliverables
3. How to submit
4. Video requirements
5. Report format and structure
6. Reflection (mandatory)
7. Teamwork tips
8. Plagiarism policy
9. Rubrics
10. Project topics
11. FAQ (explicit statements)
12. Recommendations

---

## 1. Objectives

- Practice abstracting real-world problems into logical structures and
  implementing them in code.
- Develop skills in writing modular, efficient, and maintainable programs.
- Apply appropriate frameworks and libraries for problem-solving.
- Coordinate teamwork and unify coding styles in collaborative development.
- Strengthen project management skills, including communication and conflict
  resolution.

## 2. Historical timeline and deliverables

Deadlines are week-day Mondays at 8:00 AM. Preserved here as historical
requirements for the 2025 run.

| Week | Deliverable                                            | Deadline (historical) |
|------|--------------------------------------------------------|-----------------------|
| 1    | Proposal: team list, project title, short description, initial task allocation | Week 2 Monday 8:00 AM |
| 2    | System Design: system/module breakdown, core feature list, dataset description, updated task allocation | Week 3 Monday 8:00 AM |
| 3-4  | Development: core code implementation, working prototype, GitHub Link | Week 5 Monday 8:00 AM |
| 5-6  | Testing & Final Report: testing report, final documentation, demo video, updated code (if needed) | Week 7 Monday 8:00 AM |

## 3. How to submit

- Submit your documents in xSITE Dropbox ("Python Project xxx") in PDF format.

## 4. Video requirements

Explicit requirements:
- Presentation (slides optional): the video must include a report on the
  entire system. You can present based on your report without creating
  slides, but the presentation must cover key aspects: objectives, design,
  functionality, results, etc.
- Participation: each member must speak to practice presentation skills;
  the team decides who presents which part.
- System demonstration: the video should include a demonstration of the
  system in action.
- Fully functional system: the submitted system must be fully functional and
  able to run without issues.
- Code structure explanation: provide an overview of the code structure,
  explaining the purpose of each module; walk through the main parts of the
  code without going into detail on every line.
- Time management: flexibility in overall time; the video should not be
  overly simple, with a suggested duration of around 15 to 30 minutes.

## 5. Report format and structure

Explicit requirements:
- The final report must be between 12 to 15 pages (single column, font size
  12, single spacing). Reports longer or shorter may be penalized.
- To reinforce logical reasoning and problem decomposition, the report should
  primarily explain the project's Key Functions. If helpful, use these
  guidelines (not every item must be followed strictly) for each chosen
  function:
  - Context & Purpose: what problem this function solves (e.g. Best Time to
    Buy and Sell Stock).
  - Signature: function name, parameters, and return type
    (e.g. `best_profit(prices: list[int]) -> int`).
  - Inputs & Constraints: data types, valid ranges, edge conditions.
  - Outputs: exact meaning and format.
  - Algorithm & Rationale: the core idea and why it works (in your own words).
  - Complexity: Big-O time and space.
  - Edge Cases: how the function handles them.
  - Example Trace: a small, concrete example showing step-by-step state
    changes.
  - Unit Tests (samples): 3-5 illustrative tests (including edge cases).

Standard report structure (for reference):
1. Title and team information - first page: project title, team and member
   information including team ID, member names, student IDs, and emails.
2. Abstract - summary of the project: the problem/purpose, the proposed
   approach/method, and major findings including key quantitative results
   and interpretations.
3. Introduction - detailed context, the purpose stated as a question/problem,
   rationale and approach, and outcome/results/conclusion.
4. Related works or Literature - brief related works with citations;
   discuss whether other papers/tools/systems address the same problem and
   the differences.
5. Methods - detailed implementation: dataset used, system diagram (main
   components/technology), data pre-processing details, main tasks with very
   detailed algorithms/approaches, rationale, and discussion.
6. Result and Insight - results, outcomes, and insight from analysing the
   data, with tables/figures and interpretations and discussion.
7. Conclusion - concise summary with potential future work.
8. References - all references, cited papers or URLs used.
9. Appendix - additional information, results, screenshots as needed.
   NOTE: a reflection is mandatory.

## 6. Reflection (mandatory)

- Each report must include a reflection section in the appendix, and each
  group member must write their own reflection.
- Answer the following three question groups briefly:
  - Time Management: How did you manage your time? Anything you would do
    differently next time?
  - Technical Challenge: Which part was the most difficult technically and
    how did you solve (or try to solve) it? Examples of good programming
    practices? Bad practices or behaviors to avoid?
  - Other Reflections: anything else (e.g. what you learned, surprises, or
    teamwork)?
- There are no length requirements for this section.

## 7. Teamwork tips - questions to ask yourself

- What have I contributed to the team?
- Did I speak up and share my ideas during team meetings?
- Have I helped someone else in the group?
- When I encountered problems, was I open and honest in seeking help?
- Did I make an effort to understand different perspectives?
- When I noticed issues in others' work, did I give constructive suggestions?
- Was my communication style respectful and acceptable to others?
- When the team made decisions, what criteria did we use to accept or reject
  suggestions?
- How does our team communicate? Do we meet regularly, chat in a group?
- When I feel communication is needed, did I take the initiative to start a
  conversation?

## 8. Plagiarism policy

- SIT policy does not allow copying software or assessment solutions from
  another person. Students must ensure others cannot access their work.
- Where plagiarism is detected, both assessments involved receive ZERO mark.

## 9. Rubrics

Weights and band descriptions. Bands: Excellent (10-8.5),
Good (8.4-7), Average (6.9-6), Fail to Meet Expectations (5.9-0).

| Category (weight)                       | Key points |
|-----------------------------------------|------------|
| Programming: Correct logic, complete functionality, efficiency (0.25) | all required features correct, efficient, well-tested logic |
| Programming: Modular design, reusable functions (0.25) | well-structured, reusable, modular functions, clear separation of concerns |
| Programming: Code readability and comments (0.15) | clean, well-formatted, clear variable names, helpful comments |
| Reflection: Individual learning reflections (0.10) | detailed, thoughtful reflection showing understanding and lessons learned |
| On-time submission: Milestones and final delivery (0.10) | deduct 2 points per late submission |
| Peer review: Contribution to team and feedback to peers (0.15) | evaluated through the peer evaluation system |

## 10. Project topics

Each project lets you add extra features. Keep in mind the focus is NOT on
stacking as many features as possible but on solving well-defined "smaller"
problems with clear logic.

Example: Simple Moving Average SMA(5) - the naive approach is
O(n x k) (recomputing the sum for each window), the sliding window approach
is O(n) (subtract the outgoing element, add the incoming one). Same result,
much less redundant computation. Handling corner cases (fewer than 5 points,
missing values) makes the solution stronger.

### Project 1: Stock Market Trend Analysis
- Dataset: daily stock prices (open, high, low, close, volume at minimum)
  for a period of up to three years.
- Core functionalities:
  1. Simple Moving Average (SMA) for a given window size (e.g. 5 days).
  2. Upward and downward runs: count occurrences of consecutive upward/
     downward days (based on close-to-close changes) and identify longest
     streaks in each direction.
  3. Daily returns: simple daily returns `r_t = (P_t - P_(t-1)) / P_(t-1)`.
  4. Max profit: Best Time to Buy and Sell Stock II
     (multiple transactions allowed) - LeetCode problem.
- Visualization: plot daily closing price vs SMA on the same chart; highlight
  upward and downward runs on the price chart.
- Validation results: include comparisons with trusted or manual calculations
  for AT LEAST 5 test cases. A test case = one situation comparing your
  implementation against a trusted source or manual calculation (e.g. compare
  your SMA against pandas `.rolling().mean()` or manual Excel calculation).
  Design corner cases (data shorter than SMA window, one day missing).
  This is a flexible requirement: more if useful, fewer if it really does
  not add value.

### Project 2: Sentiment Analysis System
- Objective: rule/dictionary-based sentiment analysis over a text document
  (paragraphs and sentences); backend logic plus a simple frontend
  visualization.
- Dataset: sufficient size to test/demonstrate. If using machine learning,
  you must have a training set and a separate non-overlapping test set.
  Example dataset: IMDb Movie Reviews (50,000 labelled reviews).
- Requirements:
  1. Calculate the sentiment score of each sentence using the provided
     dictionary.
  2. Identify the most positive and most negative sentences.
  3. Sliding window over paragraphs (e.g. 3 sentences per window) to find
     the most positive/negative paragraph segments. Follow-up: how to modify
     to get the exact sentences in these segments?
  4. Without fixing window size, find the most positive/negative continuous
     segments of arbitrary length (same follow-up).
  5. Re-insert spaces into a space-removed sentence (e.g. "thisisapen") to
     find a valid segmentation. Follow-up: if multiple valid segmentations
     exist, how to return all combinations?
- Provided resources: text samples and the AFINN sentiment dictionary
  (`https://github.com/fnielsen/afinn/blob/master/afinn/data/AFINN-en-165.txt`).

### Project 3: Phishing Email Detection
- Objective: rule-based system to detect phishing emails using string
  processing and logical techniques; must use a separate test set for
  evaluation.
- Example datasets: Enron Email Dataset
  (`https://www.cs.cmu.edu/~enron/`), SpamAssassin Public Corpus
  (`https://www.kaggle.com/datasets/beatoa/spamassassin-public-corpus`).
- Requirements:
  1. Whitelist check: verify the sender's email domain is on a predefined
     safe list.
  2. Keyword detection: scan subject and body for suspicious keywords
     (e.g. 'urgent', 'verify', 'account').
  3. Keyword position scoring: higher risk for suspicious keywords in subject
     lines or early in the message.
  4. Edit distance check: compare domains/sender names against known
     legitimate ones to detect visually similar fakes.
  5. Suspicious URL detection: links that do not match the claimed domain or
     contain IP addresses instead of domains.
  6. Final risk scoring: combine results from all rules to classify emails
     as Safe or Phishing.

## 11. FAQ (explicit statements)

- Q1 How can we make our project stand out? Demonstrate efficiency and clear
  logic (e.g. sliding window O(n) vs naive O(n x k) for SMA-5). Projects are
  also compared against each other to decide the final score.
- Q2 Should we use machine learning models? Not necessary. The focus is not
  on stacking features but on solving well-defined "smaller" problems with
  clear logic (e.g. a word-count function with a Dictionary data structure is
  valued more than just calling `model_x.predict(article)`).
- Q3 Can we modify or design new features? Yes; follow Q2 guidance.
  Efficiency and clarity are more important than quantity.
- Q4 Do we need to build a web interface? Strongly encouraged, but it does
  not need to be complex (a basic web framework is enough). A more complex
  web interface will not earn extra marks.
- Q5 Can we use our own dataset? How large? Yes. Since the focus is on
  programming practice rather than complex models, a reasonable set of test
  cases is enough.
- Q6 Our group has only 4 members. Is that a problem? No. Many groups have
  4 members; having 5 does not necessarily mean higher productivity.

## 12. Recommendations

These are recommendations, not explicit requirements:
- Review the [Report and Presentation checklist](../Report%20and%20Presentation/Report%20and%20Presentation%20Overview%20and%20Checklist.md)
  for what a high-quality report/video/GitHub deliverable looks like.
- For each key function in the report, actually walk through the
  signature/algorithm/complexity/edge-case/trace/unit-test chain from section
  5; it maps directly to the rubric's "correct logic" and "clear logic"
  criteria.
- Use GitHub from the start (see
  [03 Misc/Development Tools](../../03%20Misc/Development%20Tools/Git%20and%20GitHub%20Study%20Notes.md)); the
  Development milestone requires a GitHub link by Week 5 Monday.
