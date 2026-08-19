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
     * Parses the number after a mark/unmark command.
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
     * Returns null (after explaining the problem) if the input is malformed.
     */
    static Task parseTask(String line) {
        if (line.equals("todo") || line.startsWith("todo ")) {
            String desc = line.equals("todo") ? "" : line.substring(5);
            if (desc.isEmpty()) {
                speak("Name your labour, mortal: todo <desc>");
                return null;
            }
            return new Todo(desc);
        }
        if (line.equals("deadline") || line.startsWith("deadline ")) {
            int byPos = line.indexOf(" /by ");
            if (byPos == -1) {
                speak("The Fates weave on schedule. Use: deadline <desc> /by <when>");
                return null;
            }
            String desc = byPos <= 9 ? "" : line.substring(9, byPos);
            if (desc.isEmpty()) {
                speak("Name your labour, mortal: deadline <desc> /by <when>");
                return null;
            }
            return new Deadline(desc, line.substring(byPos + 5));
        }
        if (line.equals("event") || line.startsWith("event ")) {
            int fromPos = line.indexOf(" /from ");
            if (fromPos == -1) {
                speak("Even Icarus launched from somewhere. Use: event <desc> /from <start> /to <end>");
                return null;
            }
            int toPos = line.indexOf(" /to ", fromPos);
            if (toPos == -1) {
                speak("Icarus never planned a landing either. Use: event <desc> /from <start> /to <end>");
                return null;
            }
            String desc = fromPos <= 6 ? "" : line.substring(6, fromPos);
            if (desc.isEmpty()) {
                speak("Name your labour, mortal: event <desc> /from <start> /to <end>");
                return null;
            }
            return new Event(desc, line.substring(fromPos + 7, toPos), line.substring(toPos + 5));
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

        Task[] tasks = new Task[100];
        int taskCount = 0;

        Scanner in = new Scanner(System.in);
        String line = in.nextLine();

        while (!line.equals("bye")) {
            if (line.equals("list")) {
                if (taskCount == 0) {
                    speak("Your list is empty.");
                } else {
                    speak("Here are the tasks in your list:");
                    for (int i = 0; i < taskCount; i++) {
                        System.out.println((i + 1) + "." + tasks[i]);
                    }
                }
            } else if (line.startsWith("mark ")) {
                int index = parseIndex(line, "mark");
                if (index < 1 || index > taskCount) {
                    speak("No such task in the pantheon. Use: mark <number>");
                } else {
                    tasks[index - 1].markAsDone();
                    speak("Nice! I've marked this task as done:");
                    speak("  " + tasks[index - 1]);
                }
            } else if (line.startsWith("unmark ")) {
                int index = parseIndex(line, "unmark");
                if (index < 1 || index > taskCount) {
                    speak("No such task in the pantheon. Use: unmark <number>");
                } else {
                    tasks[index - 1].markAsNotDone();
                    speak("OK, I've marked this task as not done yet:");
                    speak("  " + tasks[index - 1]);
                }
            } else if (line.equals("mark")) {
                speak("Which labour is complete? Use: mark <number>");
            } else if (line.equals("unmark")) {
                speak("Which labour is not complete? Use: unmark <number>");
            } else if (line.equals("todo") || line.startsWith("todo ")
                    || line.equals("deadline") || line.startsWith("deadline ")
                    || line.equals("event") || line.startsWith("event ")) {
                Task t = parseTask(line);
                if (t != null) {
                    if (taskCount == 100) {
                        speak("Even my shoulders have a limit. Finish or delete a task first.");
                    } else {
                        tasks[taskCount] = t;
                        taskCount++;
                        speak("Got it. I've added this task:");
                        speak("  " + t);
                        speak("Now you have " + taskCount + " task" + (taskCount == 1 ? "" : "s") + " in the list.");
                    }
                }
            } else {
                speak("The Oracle is silent on that word. Try: todo, deadline, event, list, mark, unmark, bye.");
            }
            line = in.nextLine();
        }

        speak("Goodbye. Atlas signing off. See you soon!");
    }
}
