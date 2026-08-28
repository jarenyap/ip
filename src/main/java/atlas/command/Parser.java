package atlas.command;

import atlas.AtlasException;
import atlas.task.Deadline;
import atlas.task.Event;
import atlas.task.Task;
import atlas.task.Todo;
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
     *
     * @param line input line to inspect.
     * @return command identified at the start of the line, or {@code null}.
     */
    public static Command parseCommand(String line) {
        for (Command cmd : Command.values()) {
            if (line.equals(cmd.getWord()) || line.startsWith(cmd.getWord() + " ")) {
                return cmd;
            }
        }
        return null;
    }

    /**
     * Parses the number after a mark/unmark/delete command.
     * Returns the 1-based task number, or -1 if it is not a number.
     *
     * @param line input line containing the command and task number.
     * @param cmd command whose argument should be parsed.
     * @return parsed 1-based task number, or {@code -1} when invalid.
     */
    public static int parseIndex(String line, Command cmd) {
        String rest = line.substring(cmd.getWord().length() + 1).trim();
        try {
            return Integer.parseInt(rest);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Parses the keyword after a find command.
     *
     * @param line input line containing the find command and keyword.
     * @param cmd find command whose argument should be parsed.
     * @return trimmed keyword to search for.
     * @throws AtlasException if no keyword was supplied.
     */
    public static String parseKeyword(String line, Command cmd) throws AtlasException {
        String keyword = line.substring(cmd.getWord().length()).trim();
        if (keyword.isEmpty()) {
            throw new AtlasException("What shall I seek, mortal? Use: find <keyword>");
        }
        return keyword;
    }

    /**
     * Parses a todo/deadline/event command line into a Task.
     * Throws AtlasException (with an explanation) if the input is malformed.
     *
     * @param line input line containing a task command.
     * @param cmd task command to parse.
     * @return task represented by the line.
     * @throws AtlasException if the input is malformed.
     */
    public static Task parseTask(String line, Command cmd) throws AtlasException {
        int prefixLen = cmd.getWord().length() + 1; // word plus the separating space, e.g. "todo "
        switch (cmd) {
            case TODO: {
                String desc = line.length() == cmd.getWord().length() ? "" : line.substring(prefixLen);
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
