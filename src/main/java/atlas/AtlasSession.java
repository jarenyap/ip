package atlas;

import java.util.ArrayList;

import atlas.command.Command;
import atlas.command.Parser;
import atlas.storage.Storage;
import atlas.task.Task;
import atlas.task.TaskList;

/**
 * Executes one user command at a time and reports Atlas's reply through an
 * output sink. Both the text interface and the GUI drive this class, so the
 * two front-ends behave identically.
 */
public class AtlasSession {

    /** Default location of the file used to persist Atlas tasks. */
    public static final String DEFAULT_DATA_FILE = "./data/atlas.txt";
    /** Greeting shown when Atlas starts. */
    public static final String HELLO_MESSAGE = "Hello! I'm Atlas, your personal assistant.";
    /** Prompt shown after the greeting. */
    public static final String PROMPT_MESSAGE = "What can I do for you?";
    /** Farewell shown when the user exits. */
    public static final String GOODBYE_MESSAGE = "Goodbye. Atlas signing off. See you soon!";

    /**
     * Receives the messages Atlas produces while handling a command. A
     * "speak" message is a reply that forms its own speech bubble; a "print"
     * message is a plain line, used for task listings.
     */
    public interface Output {
        /** Reports a reply message that forms its own speech bubble. */
        void speak(String message);

        /** Reports a plain line of output, e.g. one task listing row. */
        void print(String text);
    }

    private final Storage storage;
    private final TaskList tasks;
    private final Output output;

    /**
     * Creates a session that executes commands against the given task list.
     *
     * @param storage storage used to persist task-list changes.
     * @param tasks task list the session operates on.
     * @param output sink that receives Atlas's replies.
     */
    public AtlasSession(Storage storage, TaskList tasks, Output output) {
        this.storage = storage;
        this.tasks = tasks;
        this.output = output;
    }

    /**
     * Handles one user command line: parses it, mutates the task list as
     * needed, and reports the reply through this session's output sink.
     *
     * @param line the full command line typed by the user.
     */
    public void respond(String line) {
        try {
            Command cmd = Parser.parseCommand(line);
            if (cmd == null) {
                throw new AtlasException("The Oracle is silent on that word. "
                        + "Try: todo, deadline, event, list, mark, unmark, delete, find, bye.");
            }
            switch (cmd) {
                case LIST:
                    if (tasks.isEmpty()) {
                        output.speak("Your list is empty.");
                    } else {
                        output.speak("Here are the tasks in your list:");
                        for (int i = 0; i < tasks.size(); i++) {
                            output.print((i + 1) + "." + tasks.get(i));
                        }
                    }
                    break;
                case MARK:
                    if (line.length() == cmd.getWord().length()) {
                        throw new AtlasException("Which labour is complete? Use: mark <number>");
                    }
                    int markIndex = Parser.parseIndex(line, cmd);
                    if (markIndex < 1 || markIndex > tasks.size()) {
                        throw new AtlasException("No such task in the pantheon. Use: mark <number>");
                    }
                    tasks.get(markIndex - 1).markAsDone();
                    storage.save(tasks.all());
                    output.speak("Nice! I've marked this task as done:");
                    output.speak("  " + tasks.get(markIndex - 1));
                    break;
                case UNMARK:
                    if (line.length() == cmd.getWord().length()) {
                        throw new AtlasException("Which labour is not complete? Use: unmark <number>");
                    }
                    int unmarkIndex = Parser.parseIndex(line, cmd);
                    if (unmarkIndex < 1 || unmarkIndex > tasks.size()) {
                        throw new AtlasException("No such task in the pantheon. Use: unmark <number>");
                    }
                    tasks.get(unmarkIndex - 1).markAsNotDone();
                    storage.save(tasks.all());
                    output.speak("OK, I've marked this task as not done yet:");
                    output.speak("  " + tasks.get(unmarkIndex - 1));
                    break;
                case DELETE:
                    if (line.length() == cmd.getWord().length()) {
                        throw new AtlasException("Which labour shall I release? Use: delete <number>");
                    }
                    int deleteIndex = Parser.parseIndex(line, cmd);
                    if (deleteIndex < 1 || deleteIndex > tasks.size()) {
                        throw new AtlasException("No such task in the pantheon. Use: delete <number>");
                    }
                    Task removed = tasks.remove(deleteIndex - 1);
                    storage.save(tasks.all());
                    output.speak("Got it. I've removed this task:");
                    output.speak("  " + removed);
                    output.speak("Now you have " + tasks.size() + " task"
                            + (tasks.size() == 1 ? "" : "s") + " in the list.");
                    break;
                case FIND:
                    String[] keywords = Parser.parseKeywords(line, cmd);
                    ArrayList<Task> matches = tasks.find(keywords);
                    if (matches.isEmpty()) {
                        output.speak("The Oracle found no matching tasks.");
                    } else {
                        output.speak("Here are the matching tasks in your list:");
                        for (int i = 0; i < matches.size(); i++) {
                            output.print((i + 1) + "." + matches.get(i));
                        }
                    }
                    break;
                case TODO:
                case DEADLINE:
                case EVENT:
                    Task t = Parser.parseTask(line, cmd);
                    tasks.add(t);
                    storage.save(tasks.all());
                    output.speak("Got it. I've added this task:");
                    output.speak("  " + t);
                    output.speak("Now you have " + tasks.size() + " task"
                            + (tasks.size() == 1 ? "" : "s") + " in the list.");
                    break;
                case BYE:
                    // Unreachable: callers exit before dispatching "bye".
                    break;
                default:
                    throw new AssertionError("Every command is handled above");
            }
        } catch (AtlasException e) {
            output.speak(e.getMessage());
        }
    }
}
