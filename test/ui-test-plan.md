# UI Test Plan — Atlas

Run with: `test/ui-test.sh` (from the repo root).

Each case has these files under `test/cases/`:

- `<name>.in` — commands fed to Atlas (must end with `bye`)
- `<name>.expected` — lines that must appear in the output (substring match,
  mirroring the course grading scripts; tolerant of bubble formatting).
  Indexed list lines (e.g. `1.[T][ ] task`) only occur in `list` output, so
  they prove stored state rather than acknowledgement text.
- `<name>.in2` (optional) — a second session run in the same working
  directory after the first, used to test persistence across restarts.
  Expected substrings cover both runs' combined output.

Each case runs in its own temporary working directory, so Level-7 data files
are isolated and every case starts with a fresh task list.

## Level-8 note

Deadline `/by` values are dates since Level-8: input `yyyy-mm-dd`, output
`MMM dd yyyy`. Non-date values are rejected with the Fates message. The data
file stores the ISO date (e.g. `D | 0 | x | 2019-10-15`); old files with
non-date `by` values are skipped as corrupted on load.

## A-Varargs note

Since A-Varargs, `find` takes any number of whitespace-separated keywords
(e.g. `find book paper`). A task matches when its description contains ANY
of the keywords (case-sensitive OR), in insertion order. Bare `find` still
shows the missing-keyword message.

## Cases

| Case | Aim | Inputs | Expected |
|---|---|---|---|
| `spec-scenario` | Full week-2 flow: add all 3 task types, list, mark, exit | `todo borrow book`, `deadline return book /by 2019-10-15`, `event project meeting /from Mon 2pm /to 4pm`, `deadline do homework /by 2019-12-02`, `list`, `mark 1`, `list`, `bye` | Indexed list snapshots before and after mark; goodbye on `bye` |
| `empty-list` | `list` with no tasks | `list`, `bye` | `Your list is empty.` |
| `mark-unmark-all-types` | mark AND unmark on every task type | `todo a`, `deadline b /by 2019-12-02`, `event c /from 1pm /to 2pm`, `mark 1`, `mark 2`, `mark 3`, `list`, `unmark 1`, `unmark 2`, `unmark 3`, `list`, `bye` | All three `[X]` after mark; all three `[ ]` restored after unmark |
| `special-chars` | punctuation and multi-word fields parse | `deadline fix bug :-) /by 2019-10-15`, `event team dinner /from 7pm at marina /to 9pm at home`, `list`, `bye` | Indexed list lines with `(by: Oct 15 2019)` and `(from: 7pm at marina to: 9pm at home)` |
| `unknown-command` | bare text is rejected, not silently added | `plain task`, `bye` | Full Oracle message with command list |
| `uppercase-bye` | only exact `bye` exits | `BYE`, `list`, `bye` | `BYE` rejected as unknown; session continues |
| `empty-todo` | empty todo description rejected | `todo`, `todo `, `bye` | `Name your labour, mortal: todo <desc>` |
| `missing-by` | deadline without ` /by ` rejected | `deadline do work`, `deadline`, `bye` | Full Fates message with syntax hint |
| `missing-from` | event without ` /from ` rejected | `event party /to 10pm`, `event`, `bye` | Full Icarus-from message with syntax hint |
| `missing-to` | event without ` /to ` rejected | `event party /from 8pm`, `bye` | Full Icarus-to message with syntax hint |
| `bad-mark` | mark/unmark with non-number, zero, out-of-range, or no index | `mark abc`, `mark 0`, `mark 99`, `mark`, `unmark`, `unmark 99`, `bye` | Full pantheon messages for both commands; bare-command prompts |
| `empty-desc` | task command with marker but no description | `deadline /by Mon`, `event /from 2pm /to 3pm`, `todo`, `bye` | All three full `Name your labour` messages |
| `empty-values` | whitespace-only descriptions and empty date fields rejected | `todo  `, `deadline task /by `, `event party /from  /to end`, `event party /from start /to `, `list`, `bye` | All four full error messages; list still empty |
| `delete-case` | delete removes the task and renumbers | `todo a`, `deadline b /by 2019-12-02`, `event c /from 1pm /to 2pm`, `delete 2`, `list`, `delete 1`, `list`, `bye` | Removed task shown `[D][ ] b (by: Dec 02 2019)`; count drops; list renumbers without gaps |
| `delete-bad` | delete with non-number, zero, out-of-range, or no index | `delete abc`, `delete 0`, `delete 99`, `delete`, `bye` | Full pantheon message; bare-command prompt |
| `delete-only` | deleting the last task empties the list cleanly | `todo only`, `delete 1`, `list`, `bye` | Removed shown; `Now you have 0 tasks in the list.`; `Your list is empty.` |
| `find-case` | Level-9: find tasks by a case-sensitive description keyword | add two tasks containing `book`, add an unrelated task, `find book`, `find missing`, bare `find` | Matching tasks are listed in insertion order; no-match and missing-keyword messages are shown |
| `find-multi-keyword` | A-Varargs: find accepts several whitespace-separated keywords; any keyword matches (OR) | `todo read book`, `deadline return book /by 2019-10-15`, `todo visit museum`, `find book paper`, `find missing`, `find museum`, bare `find`, `bye` | Both `book` rows listed in order for `book paper`; no-match message; `museum` row listed alone; missing-keyword message |
| `state-recovery` | errors leave the list untouched; mark/delete after errors work | `todo keep`, `deadline due /by 2019-12-02`, `event meet /from 10am /to 11am`, `deadline broken`, `mark 99`, `delete 99`, `list`, `mark 2`, `delete 1`, `list`, `unmark 1`, `list`, `bye` | Errors do not add tasks; indexed snapshots prove keep removed, due stays marked through the delete, then unmarks |
| `large-list` | dynamic sizing and storage correctness beyond 100 tasks | 101 `todo task N` commands, `list`, `mark 101`, `delete 1`, `list`, `bye` | Indexed lines for tasks 1/100/101; 101st markable; after delete: renumbered, 101st still marked, count 100 |
| `level7-persistence` | Level-7: save on change, load on restart | run 1: add todo/deadline/event, `mark 2`, `list`, `bye`; run 2: `list`, `unmark 2`, `list`, `delete 1`, `list`, `bye` | Run 2 lists run 1's tasks loaded from disk with mark intact; unmark and delete persist and renumber |
| `level8-dates` | Level-8: deadline dates parse (yyyy-mm-dd) and print formatted (MMM dd yyyy); non-dates rejected | `deadline submit /by 2019-10-15`, `deadline newyear /by 2020-01-05`, `list`, `deadline bad /by 15/10/2019`, `list`, `bye` | Formatted lines `(by: Oct 15 2019)` and `(by: Jan 05 2020)`; full rejection message for `15/10/2019`; list unchanged after rejection |

## Maintenance rule

After every code update: re-run `test/ui-test.sh`. If behaviour changed
intentionally, update this plan and the case files in the same commit as the
code change. If a case fails, fix the code first — the tests are the contract.
