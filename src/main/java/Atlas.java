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
     * Parses a todo/deadline/event command line into a Task.
     * Lines without a command prefix are treated as plain todos.
     */
    static Task parseTask(String line) {
        if (line.startsWith("todo ")) {
            return new Task("T", line.substring(5));
        }
        if (line.startsWith("deadline ")) {
            int byPos = line.indexOf(" /by ");
            return new Task("D", line.substring(9, byPos), line.substring(byPos + 5));
        }
        if (line.startsWith("event ")) {
            int fromPos = line.indexOf(" /from ");
            int toPos = line.indexOf(" /to ", fromPos);
            return new Task("E", line.substring(6, fromPos),
                    line.substring(fromPos + 7, toPos), line.substring(toPos + 5));
        }
        return new Task("T", line);
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
                int index = Integer.parseInt(line.substring(5)) - 1;
                tasks[index].markAsDone();
                speak("Nice! I've marked this task as done:");
                speak("  " + tasks[index]);
            } else if (line.startsWith("unmark ")) {
                int index = Integer.parseInt(line.substring(7)) - 1;
                tasks[index].markAsNotDone();
                speak("OK, I've marked this task as not done yet:");
                speak("  " + tasks[index]);
            } else {
                Task t = parseTask(line);
                tasks[taskCount] = t;
                taskCount++;
                speak("Got it. I've added this task:");
                speak("  " + t);
                speak("Now you have " + taskCount + " task" + (taskCount == 1 ? "" : "s") + " in the list.");
            }
            line = in.nextLine();
        }

        speak("Goodbye. Atlas signing off. See you soon!");
    }
}
