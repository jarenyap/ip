package atlas;

import java.util.ArrayList;

import atlas.client.Client;
import atlas.client.ClientList;
import atlas.command.ClientCommand;
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

    /** Default location of the file used to persist Atlas tasks and clients. */
    public static final String DEFAULT_DATA_FILE = "./data/atlas.txt";
    /** Greeting shown when Atlas starts. */
    public static final String HELLO_MESSAGE = "Hello! I'm Atlas, your personal assistant.";
    /** Prompt shown after the greeting. */
    public static final String PROMPT_MESSAGE = "What can I do for you?";
    /** Farewell shown when the user exits. */
    public static final String GOODBYE_MESSAGE = "Goodbye. Atlas signing off. See you soon!";
    /** Message shown when a client delete command carries no number. */
    private static final String CLIENT_NUMBER_MISSING_MESSAGE =
            "Which client shall I release? Use: client delete <number>";
    /** Message shown when a client number does not name an existing client. */
    private static final String CLIENT_NUMBER_INVALID_MESSAGE =
            "No such client in the pantheon. Use: client delete <number>";

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
    private final ClientList clients;
    private final Output output;

    /**
     * Creates a session that executes commands against the given lists.
     *
     * @param storage storage used to persist task-list and client-list changes.
     * @param tasks task list the session operates on.
     * @param clients client list the session operates on.
     * @param output sink that receives Atlas's replies.
     */
    public AtlasSession(Storage storage, TaskList tasks, ClientList clients, Output output) {
        this.storage = storage;
        this.tasks = tasks;
        this.clients = clients;
        this.output = output;
    }

    /**
     * Handles one user command line: parses it, mutates the task list or the
     * client list as needed, and reports the reply through this session's
     * output sink.
     *
     * @param line the full command line typed by the user.
     */
    public void respond(String line) {
        try {
            Command cmd = Parser.parseCommand(line);
            if (cmd == null) {
                throw new AtlasException("The Oracle is silent on that word. "
                        + "Try: todo, deadline, event, list, mark, unmark, delete, find, client, bye.");
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
                    saveAll();
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
                    saveAll();
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
                    saveAll();
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
                    saveAll();
                    output.speak("Got it. I've added this task:");
                    output.speak("  " + t);
                    speakTaskCount();
                    break;
                }
                case CLIENT:
                    respondToClient(line);
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

    /**
     * Handles one client command line: add, list, find or delete.
     *
     * @param line the full command line typed by the user.
     * @throws AtlasException if the client command is malformed.
     */
    private void respondToClient(String line) throws AtlasException {
        ClientCommand sub = Parser.parseClientSubcommand(line);
        switch (sub) {
            case ADD: {
                Client client = Parser.parseClient(line);
                clients.add(client);
                saveAll();
                output.speak("Got it. I've added this client:");
                output.speak("  " + client);
                speakClientCount();
                break;
            }
            case LIST:
                if (clients.isEmpty()) {
                    output.speak("Your client list is empty.");
                } else {
                    output.speak("Here are your clients:");
                    for (int i = 0; i < clients.size(); i++) {
                        output.print((i + 1) + "." + clients.get(i));
                    }
                }
                break;
            case FIND: {
                String[] keywords = Parser.parseClientKeywords(line);
                ArrayList<Client> matches = clients.find(keywords);
                if (matches.isEmpty()) {
                    output.speak("The Oracle found no matching clients.");
                } else {
                    output.speak("Here are the matching clients:");
                    for (int i = 0; i < matches.size(); i++) {
                        output.print((i + 1) + "." + matches.get(i));
                    }
                }
                break;
            }
            case DELETE: {
                int index = parseClientNumber(line);
                Client removed = clients.remove(index - 1);
                assert removed != null : "a valid client number always yields a client";
                saveAll();
                output.speak("Got it. I've removed this client:");
                output.speak("  " + removed);
                speakClientCount();
                break;
            }
            default:
                throw new AssertionError("Every client command is handled above");
        }
    }

    /**
     * Returns the client number that follows a client delete command, after
     * checking that the number was supplied and that it refers to an existing
     * client.
     *
     * @param line the full command line typed by the user.
     * @return the validated 1-based client number.
     * @throws AtlasException if the number is missing, is not a number, or
     *     falls outside the current client list.
     */
    private int parseClientNumber(String line) throws AtlasException {
        String argument = Parser.parseClientDeleteArgument(line);
        if (argument.isEmpty()) {
            throw new AtlasException(CLIENT_NUMBER_MISSING_MESSAGE);
        }
        int index = Parser.parseNumber(argument);
        if (index < 1 || index > clients.size()) {
            throw new AtlasException(CLIENT_NUMBER_INVALID_MESSAGE);
        }
        return index;
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
     * Saves both lists, since tasks and clients share one storage file.
     *
     * @throws AtlasException if the file cannot be written.
     */
    private void saveAll() throws AtlasException {
        storage.save(tasks.all(), clients.all());
    }

    /**
     * Reports how many tasks the list holds, with the sentence Atlas uses
     * after a task is added or deleted.
     */
    private void speakTaskCount() {
        output.speak("Now you have " + tasks.size() + " task"
                + (tasks.size() == 1 ? "" : "s") + " in the list.");
    }

    /**
     * Reports how many clients the list holds, with the sentence Atlas uses
     * after a client is added or deleted.
     */
    private void speakClientCount() {
        output.speak("Now you have " + clients.size() + " client"
                + (clients.size() == 1 ? "" : "s") + " in the list.");
    }
}
