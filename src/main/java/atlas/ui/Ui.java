package atlas.ui;

import java.util.Scanner;

import atlas.AtlasSession;

/**
 * Deals with interactions with the user: reading commands and printing output.
 */
public class Ui implements AtlasSession.Output {

    private static final String BANNER = "     _  _____ _        _    ____\n"
            + "    / \\|_   _| |      / \\  / ___|\n"
            + "   / _ \\ | | | |     / _ \\ \\___ \\\n"
            + "  / ___ \\| | | |___ / ___ \\ ___) |\n"
            + " /_/   \\_\\_| |_____/_/   \\_\\____/\n";

    private final Scanner in;

    /**
     * Creates a user interface that reads commands from the supplied scanner.
     *
     * @param in scanner providing user input.
     */
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
     *
     * @param message message to display.
     */
    @Override
    public void speak(String message) {
        String border = "─".repeat(message.length() + 2);
        System.out.println("╭" + border + "╮");
        System.out.println("│ " + message + " │");
        System.out.println("╰" + border + "╯");
    }

    /**
     * Prints a line without decoration (used for task listings).
     *
     * @param text text to print.
     */
    @Override
    public void print(String text) {
        System.out.println(text);
    }

    /**
     * Reads the next command line from the user.
     *
     * @return next line supplied by the user.
     */
    public String readLine() {
        return in.nextLine();
    }
}
