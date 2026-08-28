package atlas.command;

/**
 * The commands Atlas understands.
 */
public enum Command {
    BYE("bye"),
    LIST("list"),
    MARK("mark"),
    UNMARK("unmark"),
    DELETE("delete"),
    FIND("find"),
    TODO("todo"),
    DEADLINE("deadline"),
    EVENT("event");

    private final String word;

    Command(String word) {
        this.word = word;
    }

    /** Returns the text users type to invoke this command. */
    public String getWord() {
        return word;
    }
}
