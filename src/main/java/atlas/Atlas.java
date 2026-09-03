package atlas;

import java.util.Scanner;

import atlas.command.Command;
import atlas.storage.Storage;
import atlas.task.TaskList;
import atlas.ui.Ui;

/**
 * The text-based entry point of the Atlas chatbot.
 * Prints the banner, then runs the main command loop against an
 * {@link AtlasSession} shared with the GUI front-end.
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
        ui.speak(AtlasSession.HELLO_MESSAGE);
        ui.speak(AtlasSession.PROMPT_MESSAGE);

        Storage storage = new Storage(AtlasSession.DEFAULT_DATA_FILE);
        TaskList tasks;
        try {
            tasks = new TaskList(storage.load());
        } catch (AtlasException e) {
            ui.speak(e.getMessage());
            tasks = new TaskList();
        }

        AtlasSession session = new AtlasSession(storage, tasks, ui);
        String line = ui.readLine();

        while (!line.equals(Command.BYE.getWord())) {
            session.respond(line);
            line = ui.readLine();
        }

        ui.speak(AtlasSession.GOODBYE_MESSAGE);
    }
}
