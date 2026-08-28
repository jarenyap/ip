package atlas;

import atlas.command.Command;
import atlas.command.Parser;
import atlas.storage.Storage;
import atlas.task.Task;
import atlas.task.TaskList;
import atlas.ui.Ui;
import java.util.Scanner;

/**
 * The entry point of the Atlas chatbot.
 * Wires together the Ui, Parser, TaskList and Storage classes, and
 * runs the main command loop.
 */
public class Atlas {

    /**
     * Starts Atlas and processes commands until the user exits.
     *
     * @param args command-line arguments, which Atlas does not currently use.
     */
    public static void main(String[] args) {
        Ui ui = new Ui(new Scanner(System.in));
        ui.printBanner();
        ui.speak("Hello! I'm Atlas, your personal assistant.");
        ui.speak("What can I do for you?");

        Storage storage = new Storage("./data/atlas.txt");
        TaskList tasks;
        try {
            tasks = new TaskList(storage.load());
        } catch (AtlasException e) {
            ui.speak(e.getMessage());
            tasks = new TaskList();
        }

        String line = ui.readLine();

        while (!line.equals(Command.BYE.getWord())) {
            try {
                Command cmd = Parser.parseCommand(line);
                if (cmd == null) {
                    throw new AtlasException("The Oracle is silent on that word. "
                            + "Try: todo, deadline, event, list, mark, unmark, delete, bye.");
                }
                switch (cmd) {
                    case LIST:
                        if (tasks.isEmpty()) {
                            ui.speak("Your list is empty.");
                        } else {
                            ui.speak("Here are the tasks in your list:");
                            for (int i = 0; i < tasks.size(); i++) {
                                ui.print((i + 1) + "." + tasks.get(i));
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
                        ui.speak("Nice! I've marked this task as done:");
                        ui.speak("  " + tasks.get(markIndex - 1));
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
                        ui.speak("OK, I've marked this task as not done yet:");
                        ui.speak("  " + tasks.get(unmarkIndex - 1));
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
                        ui.speak("Got it. I've removed this task:");
                        ui.speak("  " + removed);
                        ui.speak("Now you have " + tasks.size() + " task"
                                + (tasks.size() == 1 ? "" : "s") + " in the list.");
                        break;
                    case TODO:
                    case DEADLINE:
                    case EVENT:
                        Task t = Parser.parseTask(line, cmd);
                        tasks.add(t);
                        storage.save(tasks.all());
                        ui.speak("Got it. I've added this task:");
                        ui.speak("  " + t);
                        ui.speak("Now you have " + tasks.size() + " task"
                                + (tasks.size() == 1 ? "" : "s") + " in the list.");
                        break;
                    case BYE:
                        // Unreachable: the loop condition exits on "bye" before dispatch.
                        break;
                }
            } catch (AtlasException e) {
                ui.speak(e.getMessage());
            }
            line = ui.readLine();
        }

        ui.speak("Goodbye. Atlas signing off. See you soon!");
    }
}
