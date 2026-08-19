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

    public static void main(String[] args) {
        String banner = "     _  _____ _        _    ____\n"
                + "    / \\|_   _| |      / \\  / ___|\n"
                + "   / _ \\ | | | |     / _ \\ \\___ \\\n"
                + "  / ___ \\| | | |___ / ___ \\ ___) |\n"
                + " /_/   \\_\\_| |_____/_/   \\_\\____/\n";
        System.out.println(banner);
        speak("Hello! I'm Atlas, your personal assistant.");
        speak("What can I do for you?");

        String[] tasks = new String[100];
        int taskCount = 0;

        Scanner in = new Scanner(System.in);
        String line = in.nextLine();

        while (!line.equals("bye")) {
            if (line.equals("list")) {
                if (taskCount == 0) {
                    speak("Your list is empty.");
                } else {
                    speak("Here are the tasks in your list.");
                    for (int i = 0; i < taskCount; i++) {
                        System.out.println((i + 1) + ". " + tasks[i]);
                    }
                }
            } else {
                tasks[taskCount] = line;
                taskCount++;
                speak("Got it. I've added: " + line);
                speak("You now have " + taskCount + " task" + (taskCount == 1 ? "" : "s") + ".");
            }
            line = in.nextLine();
        }

        speak("Goodbye. Atlas signing off. See you soon!");
    }
}