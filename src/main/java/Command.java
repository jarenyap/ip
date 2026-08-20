/**
 * The commands Atlas understands, and how to recognise them in a line of input.
 */
enum Command {
    BYE("bye"),
    LIST("list"),
    MARK("mark"),
    UNMARK("unmark"),
    DELETE("delete"),
    TODO("todo"),
    DEADLINE("deadline"),
    EVENT("event");

    final String word;

    Command(String word) {
        this.word = word;
    }

    /**
     * Returns the command a line starts with, or null if the line is not a command.
     * A line matches a command when it is exactly the command word, or the
     * command word followed by a space and arguments.
     */
    static Command fromLine(String line) {
        for (Command cmd : values()) {
            if (line.equals(cmd.word) || line.startsWith(cmd.word + " ")) {
                return cmd;
            }
        }
        return null;
    }
}
