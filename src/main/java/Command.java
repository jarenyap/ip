/**
 * The commands Atlas understands.
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
}
