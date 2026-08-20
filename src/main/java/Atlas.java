import java.util.ArrayList;
import java.util.Scanner;

public class Atlas {

    /**
     * Prints a message inside a speech bubble.
     * The bubble width adapts to the message length.
     */
    static void speak(String message) {
        String border = "─".repeat(message.length() + 2);
        System.out.println("╭" + border + "╮");
        System.out.println("│ " + message + " │");
        System.out.println("╰" + border + "╯");
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
            String by = line.substring(byPos + 5);
            if (by.trim().isEmpty()) {
                throw new AtlasException("The Fates weave on schedule. Use: deadline <desc> /by <when>");
            }
            return new Deadline(desc, by);
        }
        case EVENT: {
            int fromPos = line.indexOf(" /from ");
            if (fromPos == -1) {
                throw new AtlasException("Even Icarus launched from somewhere. Use: event <desc> /from <start> /to <end>");
            }
            int toPos = line.indexOf(" /to ", fromPos);
            if (toPos == -1) {
                throw new AtlasException("Icarus never planned a landing either. Use: event <desc> /from <start> /to <end>");
            }
            String desc = fromPos <= prefixLen ? "" : line.substring(prefixLen, fromPos);
            if (desc.trim().isEmpty()) {
                throw new AtlasException("Name your labour, mortal: event <desc> /from <start> /to <end>");
            }
            String from = line.substring(fromPos + 7, toPos);
            if (from.trim().isEmpty()) {
                throw new AtlasException("Even Icarus launched from somewhere. Use: event <desc> /from <start> /to <end>");
            }
            String to = line.substring(toPos + 5);
            if (to.trim().isEmpty()) {
                throw new AtlasException("Icarus never planned a landing either. Use: event <desc> /from <start> /to <end>");
            }
            return new Event(desc, from, to);
        }
        default:
            throw new AssertionError("Not a task command: " + cmd);
        }
    }

    public static void main(String[] args) {
        String banner = "     _  _____ _        _    ____\n"
                + "    / \\|_   _| |      / \\  / ___|\n"
                + "   / _ \\ | | | |     / _ \\ \\___ \\\n"
                + "  / ___ \\| | | |___ / ___ \\ ___) |\n"
                + " /_/   \\_\\_| |_____/_/   \\_\\____/\n";
        System.out.println(banner);
        speak("Hello! I'm Atlas, your personal assistant.");
        speak("What can I do for you?");

        ArrayList<Task> tasks = new ArrayList<>();

        Scanner in = new Scanner(System.in);
        String line = in.nextLine();

        while (!line.equals(Command.BYE.word)) {
            try {
                Command cmd = Command.fromLine(line);
                if (cmd == null) {
                    throw new AtlasException("The Oracle is silent on that word. Try: todo, deadline, event, list, mark, unmark, delete, bye.");
                }
                switch (cmd) {
                case LIST:
                    if (tasks.isEmpty()) {
                        speak("Your list is empty.");
                    } else {
                        speak("Here are the tasks in your list:");
                        for (int i = 0; i < tasks.size(); i++) {
                            System.out.println((i + 1) + "." + tasks.get(i));
                        }
                    }
                    break;
                case MARK:
                    if (line.length() == cmd.word.length()) {
                        throw new AtlasException("Which labour is complete? Use: mark <number>");
                    }
                    int markIndex = parseIndex(line, cmd);
                    if (markIndex < 1 || markIndex > tasks.size()) {
                        throw new AtlasException("No such task in the pantheon. Use: mark <number>");
                    }
                    tasks.get(markIndex - 1).markAsDone();
                    speak("Nice! I've marked this task as done:");
                    speak("  " + tasks.get(markIndex - 1));
                    break;
                case UNMARK:
                    if (line.length() == cmd.word.length()) {
                        throw new AtlasException("Which labour is not complete? Use: unmark <number>");
                    }
                    int unmarkIndex = parseIndex(line, cmd);
                    if (unmarkIndex < 1 || unmarkIndex > tasks.size()) {
                        throw new AtlasException("No such task in the pantheon. Use: unmark <number>");
                    }
                    tasks.get(unmarkIndex - 1).markAsNotDone();
                    speak("OK, I've marked this task as not done yet:");
                    speak("  " + tasks.get(unmarkIndex - 1));
                    break;
                case DELETE:
                    if (line.length() == cmd.word.length()) {
                        throw new AtlasException("Which labour shall I release? Use: delete <number>");
                    }
                    int deleteIndex = parseIndex(line, cmd);
                    if (deleteIndex < 1 || deleteIndex > tasks.size()) {
                        throw new AtlasException("No such task in the pantheon. Use: delete <number>");
                    }
                    Task removed = tasks.remove(deleteIndex - 1);
                    speak("Got it. I've removed this task:");
                    speak("  " + removed);
                    speak("Now you have " + tasks.size() + " task" + (tasks.size() == 1 ? "" : "s") + " in the list.");
                    break;
                case TODO:
                case DEADLINE:
                case EVENT:
                    Task t = parseTask(line, cmd);
                    tasks.add(t);
                    speak("Got it. I've added this task:");
                    speak("  " + t);
                    speak("Now you have " + tasks.size() + " task" + (tasks.size() == 1 ? "" : "s") + " in the list.");
                    break;
                case BYE:
                    // Unreachable: the loop condition exits on "bye" before dispatch.
                    break;
                }
            } catch (AtlasException e) {
                speak(e.getMessage());
            }
            line = in.nextLine();
        }

        speak("Goodbye. Atlas signing off. See you soon!");
    }
}
