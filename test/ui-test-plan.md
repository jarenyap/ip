# UI Test Plan — Atlas

Run with: `test/ui-test.sh` (from the repo root).

Each case has two files under `test/cases/`:

- `<name>.in` — commands fed to Atlas (must end with `bye`)
- `<name>.expected` — lines that must appear in the output (substring match,
  mirroring the course grading scripts; tolerant of bubble formatting)

## Cases

| Case | Aim | Inputs | Expected |
|---|---|---|---|
| `spec-scenario` | Full week-2 flow: add all 3 task types, list, mark, exit | `todo borrow book`, `deadline return book /by Sunday`, `event project meeting /from Mon 2pm /to 4pm`, `deadline do homework /by no idea :-p`, `list`, `mark 1`, `list`, `bye` | All 3 types render with correct markers; list numbered with `[X]` after mark; goodbye on `bye` |
| `empty-list` | `list` with no tasks | `list`, `bye` | `Your list is empty.` |
| `mark-unmark-all-types` | mark/unmark works on every task type | `todo a`, `deadline b /by Mon`, `event c /from 1pm /to 2pm`, `mark 2`, `mark 3`, `list`, `unmark 1`, `list`, `bye` | `[D][X]` and `[E][X]` after mark; `[T][ ]` restored after unmark |
| `special-chars` | punctuation and multi-word fields parse | `deadline fix bug /by EOD :-)`, `event team dinner /from 7pm at marina /to 9pm at home`, `list`, `bye` | `(by: EOD :-))`, `(from: 7pm at marina to: 9pm at home)` |
| `unknown-command` | bare text is rejected, not silently added | `plain task`, `bye` | `The Oracle is silent` |
| `uppercase-bye` | only exact `bye` exits | `BYE`, `list`, `bye` | `BYE` rejected as unknown; session continues |
| `empty-todo` | empty todo description rejected | `todo`, `todo `, `bye` | `Name your labour, mortal: todo <desc>` |
| `missing-by` | deadline without ` /by ` rejected | `deadline do work`, `deadline`, `bye` | `The Fates weave on schedule` |
| `missing-from` | event without ` /from ` rejected | `event party /to 10pm`, `event`, `bye` | `Even Icarus launched` |
| `missing-to` | event without ` /to ` rejected | `event party /from 8pm`, `bye` | `Icarus never planned a landing` |
| `bad-mark` | mark with non-number, zero, out-of-range, or no index | `mark abc`, `mark 0`, `mark 99`, `mark`, `bye` | `No such task in the pantheon. Use: mark <number>` (x3), `Which labour is complete? Use: mark <number>` |
| `full-list` | 101st task rejected | 101 `todo task N` commands, `bye` | `Even my shoulders have a limit.` |

## Maintenance rule

After every code update: re-run `test/ui-test.sh`. If behaviour changed
intentionally, update this plan and the case files in the same commit as the
code change. If a case fails, fix the code first — the tests are the contract.
