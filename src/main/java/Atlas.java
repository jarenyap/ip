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
    static int parseIndex(String line, String command) {
        String rest = line.substring(command.length() + 1).trim();
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
    static Task parseTask(String line) throws AtlasException {
        if (line.equals("todo") || line.startsWith("todo ")) {
            String desc = line.equals("todo") ? "" : line.substring(5);
            if (desc.trim().isEmpty()) {
                throw new AtlasException("Name your labour, mortal: todo <desc>");
            }
            return new Todo(desc);
        }
        if (line.equals("deadline") || line.startsWith("deadline ")) {
            int byPos = line.indexOf(" /by ");
            if (byPos == -1) {
                throw new AtlasException("The Fates weave on schedule. Use: deadline <desc> /by <when>");
            }
            String desc = byPos <= 9 ? "" : line.substring(9, byPos);
            if (desc.trim().isEmpty()) {
                throw new AtlasException("Name your labour, mortal: deadline <desc> /by <when>");
            }
            String by = line.substring(byPos + 5);
            if (by.trim().isEmpty()) {
                throw new AtlasException("The Fates weave on schedule. Use: deadline <desc> /by <when>");
            }
            return new Deadline(desc, by);
        }
        if (line.equals("event") || line.startsWith("event ")) {
            int fromPos = line.indexOf(" /from ");
            if (fromPos == -1) {
                throw new AtlasException("Even Icarus launched from somewhere. Use: event <desc> /from <start> /to <end>");
            }
            int toPos = line.indexOf(" /to ", fromPos);
            if (toPos == -1) {
                throw new AtlasException("Icarus never planned a landing either. Use: event <desc> /from <start> /to <end>");
            }
            String desc = fromPos <= 6 ? "" : line.substring(6, fromPos);
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
        return null;
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

        while (!line.equals("bye")) {
            try {
                if (line.equals("list")) {
                    if (tasks.isEmpty()) {
                        speak("Your list is empty.");
                    } else {
                        speak("Here are the tasks in your list:");
                        for (int i = 0; i < tasks.size(); i++) {
                            System.out.println((i + 1) + "." + tasks.get(i));
                        }
                    }
                } else if (line.startsWith("mark ")) {
                    int index = parseIndex(line, "mark");
                    if (index < 1 || index > tasks.size()) {
                        throw new AtlasException("No such task in the pantheon. Use: mark <number>");
                    }
                    tasks.get(index - 1).markAsDone();
                    speak("Nice! I've marked this task as done:");
                    speak("  " + tasks.get(index - 1));
                } else if (line.startsWith("unmark ")) {
                    int index = parseIndex(line, "unmark");
                    if (index < 1 || index > tasks.size()) {
                        throw new AtlasException("No such task in the pantheon. Use: unmark <number>");
                    }
                    tasks.get(index - 1).markAsNotDone();
                    speak("OK, I've marked this task as not done yet:");
                    speak("  " + tasks.get(index - 1));
                } else if (line.equals("mark")) {
                    throw new AtlasException("Which labour is complete? Use: mark <number>");
                } else if (line.equals("unmark")) {
                    throw new AtlasException("Which labour is not complete? Use: unmark <number>");
                } else if (line.startsWith("delete ")) {
                    int index = parseIndex(line, "delete");
                    if (index < 1 || index > tasks.size()) {
                        throw new AtlasException("No such task in the pantheon. Use: delete <number>");
                    }
                    Task removed = tasks.remove(index - 1);
                    speak("Got it. I've removed this task:");
                    speak("  " + removed);
                    speak("Now you have " + tasks.size() + " task" + (tasks.size() == 1 ? "" : "s") + " in the list.");
                } else if (line.equals("delete")) {
                    throw new AtlasException("Which labour shall I release? Use: delete <number>");
                } else if (line.equals("todo") || line.startsWith("todo ")
                        || line.equals("deadline") || line.startsWith("deadline ")
                        || line.equals("event") || line.startsWith("event ")) {
                    Task t = parseTask(line);
                    tasks.add(t);
                    speak("Got it. I've added this task:");
                    speak("  " + t);
                    speak("Now you have " + tasks.size() + " task" + (tasks.size() == 1 ? "" : "s") + " in the list.");
                } else {
                    throw new AtlasException("The Oracle is silent on that word. Try: todo, deadline, event, list, mark, unmark, delete, bye.");
                }
            } catch (AtlasException e) {
                speak(e.getMessage());
            }
            line = in.nextLine();
        }

        speak("Goodbye. Atlas signing off. See you soon!");
    }
}
