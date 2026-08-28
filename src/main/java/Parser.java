import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Makes sense of user commands: recognises the command word, and
 * extracts task details or indexes from the rest of the line.
 */
public class Parser {

    /**
     * Returns the command a line starts with, or null if the line is not a command.
     * A line matches a command when it is exactly the command word, or the
     * command word followed by a space and arguments.
     */
    static Command parseCommand(String line) {
        for (Command cmd : Command.values()) {
            if (line.equals(cmd.word) || line.startsWith(cmd.word + " ")) {
                return cmd;
            }
        }
        return null;
    }

    /**
     * Parses the number after a mark/unmark/delete command.
     * Returns the 1-based task number, or -1 if it is not a number.
     */
    static int parseIndex(String line, Command cmd) {
        String rest = line.substring(cmd.word.length() + 1).trim();
        try {
            return Integer.parseInt(rest);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Parses a todo/deadline/event command line into a Task.
     * Throws AtlasException (with an explanation) if the input is malformed.
     */
    static Task parseTask(String line, Command cmd) throws AtlasException {
        int prefixLen = cmd.word.length() + 1; // word plus the separating space, e.g. "todo "
        switch (cmd) {
        case TODO: {
            String desc = line.length() == cmd.word.length() ? "" : line.substring(prefixLen);
            if (desc.trim().isEmpty()) {
                throw new AtlasException("Name your labour, mortal: todo <desc>");
            }
            return new Todo(desc);
        }
        case DEADLINE: {
            int byPos = line.indexOf(" /by ");
            if (byPos == -1) {
                throw new AtlasException("The Fates weave on schedule. Use: deadline <desc> /by <when>");
            }
            String desc = byPos <= prefixLen ? "" : line.substring(prefixLen, byPos);
            if (desc.trim().isEmpty()) {
                throw new AtlasException("Name your labour, mortal: deadline <desc> /by <when>");
            }
            String byText = line.substring(byPos + 5);
            if (byText.trim().isEmpty()) {
                throw new AtlasException("The Fates weave on schedule. Use: deadline <desc> /by <when>");
            }
            LocalDate by;
            try {
                by = LocalDate.parse(byText.trim());
            } catch (DateTimeParseException e) {
                throw new AtlasException("The Fates cannot read that date, mortal. "
                        + "Use: deadline <desc> /by yyyy-mm-dd");
            }
            return new Deadline(desc, by);
        }
        case EVENT: {
            int fromPos = line.indexOf(" /from ");
            if (fromPos == -1) {
                throw new AtlasException("Even Icarus launched from somewhere. "
                        + "Use: event <desc> /from <start> /to <end>");
            }
            int toPos = line.indexOf(" /to ", fromPos);
            if (toPos == -1) {
                throw new AtlasException("Icarus never planned a landing either. "
                        + "Use: event <desc> /from <start> /to <end>");
            }
            String desc = fromPos <= prefixLen ? "" : line.substring(prefixLen, fromPos);
            if (desc.trim().isEmpty()) {
                throw new AtlasException("Name your labour, mortal: event <desc> /from <start> /to <end>");
            }
            String from = line.substring(fromPos + 7, toPos);
            if (from.trim().isEmpty()) {
                throw new AtlasException("Even Icarus launched from somewhere. "
                        + "Use: event <desc> /from <start> /to <end>");
            }
            String to = line.substring(toPos + 5);
            if (to.trim().isEmpty()) {
                throw new AtlasException("Icarus never planned a landing either. "
                        + "Use: event <desc> /from <start> /to <end>");
            }
            return new Event(desc, from, to);
        }
        default:
            throw new AssertionError("Not a task command: " + cmd);
        }
    }
}
