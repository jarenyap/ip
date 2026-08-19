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

        Scanner in = new Scanner(System.in);
        String line = in.nextLine();

        while (!line.equals("bye")) {
            speak(line);
            line = in.nextLine();
        }

        speak("Goodbye. Atlas signing off. See you soon!");
    }
}