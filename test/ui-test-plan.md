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
| `plain-fallback` | bare text becomes a todo | `plain task`, `list`, `bye` | `[T][ ] plain task` |
| `special-chars` | punctuation and multi-word fields parse | `deadline fix bug /by EOD :-)`, `event team dinner /from 7pm at marina /to 9pm at home`, `list`, `bye` | `(by: EOD :-))`, `(from: 7pm at marina to: 9pm at home)` |
| `uppercase-bye` | only exact `bye` exits | `BYE`, `list`, `bye` | `[T][ ] BYE` added, then normal exit |

## Maintenance rule

After every code update: re-run `test/ui-test.sh`. If behaviour changed
intentionally, update this plan and the case files in the same commit as the
code change. If a case fails, fix the code first — the tests are the contract.
