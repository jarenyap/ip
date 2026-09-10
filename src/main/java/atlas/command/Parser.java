package atlas.command;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import atlas.AtlasException;
import atlas.client.Client;
import atlas.task.Deadline;
import atlas.task.Event;
import atlas.task.Task;
import atlas.task.Todo;

/**
 * Makes sense of user commands: recognises the command word, and
 * extracts task details, client details or indexes from the rest of the line.
 */
public class Parser {

    /** Separator that introduces the deadline date in a deadline command. */
    private static final String BY_MARKER = " /by ";
    /** Separator that introduces the start of an event in an event command. */
    private static final String FROM_MARKER = " /from ";
    /** Separator that introduces the end of an event in an event command. */
    private static final String TO_MARKER = " /to ";
    /** Separator that introduces a client's phone number. */
    private static final String PHONE_MARKER = " /phone ";
    /** Separator that introduces a client's email address. */
    private static final String EMAIL_MARKER = " /email ";

    /** Syntax of the client add command, repeated in its error messages. */
    private static final String CLIENT_ADD_SYNTAX = "client add <name> [/phone <number>] [/email <address>]";
    /** Message shown when a client subcommand is missing or unrecognised. */
    private static final String CLIENT_USAGE_MESSAGE = "The Oracle is silent on that client command. Use: "
            + CLIENT_ADD_SYNTAX + ", client list, client find <keyword>, client delete <number>";
    /** Message shown when a client is added without a name. */
    private static final String CLIENT_NAME_MISSING_MESSAGE = "Name your client, mortal: " + CLIENT_ADD_SYNTAX;

    /** Length of the "client " prefix, derived from the command word. */
    private static final int CLIENT_PREFIX_LENGTH = Command.CLIENT.getWord().length() + 1;
    /** Length of the "client add " prefix, derived from the command words. */
    private static final int CLIENT_ADD_PREFIX_LENGTH =
            CLIENT_PREFIX_LENGTH + ClientCommand.ADD.getWord().length() + 1;
    /** Length of the "client find" prefix, derived from the command words. */
    private static final int CLIENT_FIND_PREFIX_LENGTH =
            CLIENT_PREFIX_LENGTH + ClientCommand.FIND.getWord().length();
    /** Length of the "client delete " prefix, derived from the command words. */
    private static final int CLIENT_DELETE_PREFIX_LENGTH =
            CLIENT_PREFIX_LENGTH + ClientCommand.DELETE.getWord().length() + 1;

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
        int prefixLength = cmd.getWord().length() + 1;
        if (line.length() < prefixLength) {
            return -1;
        }
        return parseNumber(line.substring(prefixLength));
    }

    /**
     * Parses a number out of argument text.
     *
     * @param text text that should hold a number.
     * @return the parsed number, or {@code -1} when the text is not a number.
     */
    public static int parseNumber(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Parses the keywords after a find command, one per whitespace-separated
     * word, e.g. "find book paper" yields two keywords.
     *
     * @param line input line containing the find command and keywords.
     * @param cmd find command whose argument should be parsed.
     * @return the keywords to search for.
     * @throws AtlasException if no keyword was supplied.
     */
    public static String[] parseKeywords(String line, Command cmd) throws AtlasException {
        String remainder = line.substring(cmd.getWord().length()).trim();
        if (remainder.isEmpty()) {
            throw new AtlasException("What shall I seek, mortal? Use: find <keyword>");
        }
        return remainder.split("\\s+");
    }

    /**
     * Returns the client subcommand a line asks for, e.g. ADD for
     * "client add Bob".
     *
     * @param line input line containing a client command.
     * @return the client subcommand named in the line.
     * @throws AtlasException if the subcommand is missing or unrecognised.
     */
    public static ClientCommand parseClientSubcommand(String line) throws AtlasException {
        if (line.length() <= CLIENT_PREFIX_LENGTH) {
            throw new AtlasException(CLIENT_USAGE_MESSAGE);
        }
        String remainder = line.substring(CLIENT_PREFIX_LENGTH);
        for (ClientCommand sub : ClientCommand.values()) {
            if (remainder.equals(sub.getWord()) || remainder.startsWith(sub.getWord() + " ")) {
                return sub;
            }
        }
        throw new AtlasException(CLIENT_USAGE_MESSAGE);
    }

    /**
     * Parses a client add command into a Client. The name is required, while
     * " /phone " and " /email " are optional and may appear in either order.
     *
     * @param line input line containing a client add command.
     * @return client represented by the line.
     * @throws AtlasException if the name is missing or a marker has no value.
     */
    public static Client parseClient(String line) throws AtlasException {
        String remainder = line.length() <= CLIENT_ADD_PREFIX_LENGTH
                ? "" : line.substring(CLIENT_ADD_PREFIX_LENGTH);
        // A space on each side lets a marker at either end of the line be
        // recognised, so "client add Bob /phone" reports a missing value
        // instead of treating "/phone" as part of the name, and
        // "client add /phone 1" reports a missing name.
        String text = " " + remainder + " ";
        int phonePos = text.indexOf(PHONE_MARKER);
        int emailPos = text.indexOf(EMAIL_MARKER);
        int firstMarker = earlierMarker(phonePos, emailPos);
        String name = (firstMarker == -1 ? text : text.substring(0, firstMarker)).trim();
        if (name.isEmpty()) {
            throw new AtlasException(CLIENT_NAME_MISSING_MESSAGE);
        }
        String phone = valueAfterMarker(text, phonePos, emailPos, PHONE_MARKER);
        if (phonePos != -1 && phone.isEmpty()) {
            throw new AtlasException("A number must follow /phone. Use: " + CLIENT_ADD_SYNTAX);
        }
        String email = valueAfterMarker(text, emailPos, phonePos, EMAIL_MARKER);
        if (emailPos != -1 && email.isEmpty()) {
            throw new AtlasException("An address must follow /email. Use: " + CLIENT_ADD_SYNTAX);
        }
        return new Client(name, phone, email);
    }

    /**
     * Parses the keywords after a client find command, one per
     * whitespace-separated word.
     *
     * @param line input line containing a client find command.
     * @return the keywords to search for.
     * @throws AtlasException if no keyword was supplied.
     */
    public static String[] parseClientKeywords(String line) throws AtlasException {
        String remainder = line.length() <= CLIENT_FIND_PREFIX_LENGTH
                ? "" : line.substring(CLIENT_FIND_PREFIX_LENGTH).trim();
        if (remainder.isEmpty()) {
            throw new AtlasException("Whom shall I seek, mortal? Use: client find <keyword>");
        }
        return remainder.split("\\s+");
    }

    /**
     * Returns the argument text after a client delete command.
     *
     * @param line input line containing a client delete command.
     * @return the trimmed argument, or an empty string when none was supplied.
     */
    public static String parseClientDeleteArgument(String line) {
        return line.length() <= CLIENT_DELETE_PREFIX_LENGTH
                ? "" : line.substring(CLIENT_DELETE_PREFIX_LENGTH).trim();
    }

    /**
     * Returns whichever marker appears earlier in the text.
     *
     * @param firstPos position of one marker, or -1 when it is absent.
     * @param secondPos position of the other marker, or -1 when it is absent.
     * @return the earlier position, or -1 when neither marker is present.
     */
    private static int earlierMarker(int firstPos, int secondPos) {
        if (firstPos == -1) {
            return secondPos;
        }
        if (secondPos == -1) {
            return firstPos;
        }
        return Math.min(firstPos, secondPos);
    }

    /**
     * Returns the value that follows one marker, running either to the other
     * marker or to the end of the text.
     *
     * @param text text being parsed.
     * @param markerPos position of the marker whose value is wanted, or -1.
     * @param otherMarkerPos position of the other marker, or -1.
     * @param marker the marker whose value is wanted.
     * @return the trimmed value, or an empty string when the marker is absent.
     */
    private static String valueAfterMarker(String text, int markerPos, int otherMarkerPos, String marker) {
        if (markerPos == -1) {
            return "";
        }
        int valueEnd = text.length();
        if (otherMarkerPos > markerPos) {
            // The two markers can share the single space between them, as in
            // "client add B /phone /email x@y.com", where the space that ends
            // " /phone " also starts " /email ". Ending the value at that
            // shared space would place the end before the value began, so the
            // end is never allowed to fall inside the marker itself.
            valueEnd = Math.max(otherMarkerPos, markerPos + marker.length());
        }
        return text.substring(markerPos + marker.length(), valueEnd).trim();
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
                int byPos = line.indexOf(BY_MARKER);
                if (byPos == -1) {
                    throw new AtlasException("The Fates weave on schedule. Use: deadline <desc> /by <when>");
                }
                String desc = byPos <= prefixLen ? "" : line.substring(prefixLen, byPos);
                if (desc.trim().isEmpty()) {
                    throw new AtlasException("Name your labour, mortal: deadline <desc> /by <when>");
                }
                assert byPos > prefixLen : "byPos must point past the command prefix";
                String byText = line.substring(byPos + BY_MARKER.length());
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
                int fromPos = line.indexOf(FROM_MARKER);
                if (fromPos == -1) {
                    throw new AtlasException("Even Icarus launched from somewhere. "
                            + "Use: event <desc> /from <start> /to <end>");
                }
                int toPos = line.indexOf(TO_MARKER, fromPos);
                if (toPos == -1) {
                    throw new AtlasException("Icarus never planned a landing either. "
                            + "Use: event <desc> /from <start> /to <end>");
                }
                String desc = fromPos <= prefixLen ? "" : line.substring(prefixLen, fromPos);
                if (desc.trim().isEmpty()) {
                    throw new AtlasException("Name your labour, mortal: event <desc> /from <start> /to <end>");
                }
                assert fromPos > prefixLen : "fromPos must point past the command prefix";
                assert toPos > fromPos : "/to must come after /from";
                String from = line.substring(fromPos + FROM_MARKER.length(), toPos);
                if (from.trim().isEmpty()) {
                    throw new AtlasException("Even Icarus launched from somewhere. "
                            + "Use: event <desc> /from <start> /to <end>");
                }
                String to = line.substring(toPos + TO_MARKER.length());
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
