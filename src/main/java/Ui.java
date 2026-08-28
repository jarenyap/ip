import java.util.Scanner;

/**
 * Deals with interactions with the user: reading commands and printing output.
 */
public class Ui {

    private static final String BANNER = "     _  _____ _        _    ____\n"
            + "    / \\|_   _| |      / \\  / ___|\n"
            + "   / _ \\ | | | |     / _ \\ \\___ \\\n"
            + "  / ___ \\| | | |___ / ___ \\ ___) |\n"
            + " /_/   \\_\\_| |_____/_/   \\_\\____/\n";

    private final Scanner in;

    public Ui(Scanner in) {
        this.in = in;
    }

    /** Prints the startup banner. */
    public void printBanner() {
        System.out.println(BANNER);
    }

    /**
     * Prints a message inside a speech bubble.
     * The bubble width adapts to the message length.
     */
    public void speak(String message) {
        String border = "─".repeat(message.length() + 2);
        System.out.println("╭" + border + "╮");
        System.out.println("│ " + message + " │");
        System.out.println("╰" + border + "╯");
    }

    /** Prints a line without decoration (used for task listings). */
    public void print(String text) {
        System.out.println(text);
    }

    /** Reads the next command line from the user. */
    public String readLine() {
        return in.nextLine();
    }
}
