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
                case MARK: {
                    int index = parseTaskNumber(line, cmd, "Which labour is complete? Use: mark <number>");
                    tasks.get(index - 1).markAsDone();
                    storage.save(tasks.all());
                    output.speak("Nice! I've marked this task as done:");
                    assert index >= 1 && index <= tasks.size()
                            : "the marked task must still be in the list";
                    output.speak("  " + tasks.get(index - 1));
                    break;
                }
                case UNMARK: {
                    int index = parseTaskNumber(line, cmd,
                            "Which labour is not complete? Use: unmark <number>");
                    tasks.get(index - 1).markAsNotDone();
                    storage.save(tasks.all());
                    output.speak("OK, I've marked this task as not done yet:");
                    assert index >= 1 && index <= tasks.size()
                            : "the unmarked task must still be in the list";
                    output.speak("  " + tasks.get(index - 1));
                    break;
                }
                case DELETE: {
                    int index = parseTaskNumber(line, cmd, "Which labour shall I release? Use: delete <number>");
                    Task removed = tasks.remove(index - 1);
                    assert removed != null : "a valid task number always yields a task";
                    storage.save(tasks.all());
                    output.speak("Got it. I've removed this task:");
                    output.speak("  " + removed);
                    speakTaskCount();
                    break;
                }
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
                case EVENT: {
                    Task t = Parser.parseTask(line, cmd);
                    tasks.add(t);
                    storage.save(tasks.all());
                    output.speak("Got it. I've added this task:");
                    output.speak("  " + t);
                    speakTaskCount();
                    break;
                }
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

    /**
     * Returns the task number that follows a command, after checking that the
     * number was supplied and that it refers to an existing task.
     *
     * @param line the full command line typed by the user.
     * @param cmd the command whose task number is being parsed.
     * @param missingHint message to report when no number was supplied.
     * @return the validated 1-based task number.
     * @throws AtlasException if the number is missing, is not a number, or
     *     falls outside the current task list.
     */
    private int parseTaskNumber(String line, Command cmd, String missingHint) throws AtlasException {
        if (line.length() == cmd.getWord().length()) {
            throw new AtlasException(missingHint);
        }
        int index = Parser.parseIndex(line, cmd);
        if (index < 1 || index > tasks.size()) {
            throw new AtlasException("No such task in the pantheon. Use: " + cmd.getWord() + " <number>");
        }
        return index;
    }

    /**
     * Reports how many tasks the list holds, with the sentence Atlas uses
     * after a task is added or deleted.
     */
    private void speakTaskCount() {
        output.speak("Now you have " + tasks.size() + " task"
                + (tasks.size() == 1 ? "" : "s") + " in the list.");
    }
}
