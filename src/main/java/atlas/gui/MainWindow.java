package atlas.gui;

import atlas.AtlasSession;
import atlas.command.Command;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller for the main chat window of the Atlas GUI.
 * The window layout is defined in view/MainWindow.fxml.
 */
public class MainWindow extends AnchorPane {

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private AtlasSession session;
    private ReplyCollector collector;
    private Stage stage;

    /**
     * Runs after all @FXML fields are injected. Follows the newest message
     * only while the user is already at the bottom, so reading older
     * messages with the mouse wheel is never fought.
     */
    @FXML
    private void initialize() {
        dialogContainer.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            if (scrollPane.getVvalue() >= 1.0 - 1e-3) {
                scrollPane.setVvalue(1.0);
            }
        });
    }

    /**
     * Connects this window to a chat session and shows the opening messages.
     *
     * @param session session that answers the user's messages.
     * @param collector collector that receives each reply before it is shown.
     * @param stage the window to close when the user says bye.
     * @param warning load-failure warning, or null when tasks loaded cleanly.
     */
    public void setUp(AtlasSession session, ReplyCollector collector, Stage stage, String warning) {
        this.session = session;
        this.collector = collector;
        this.stage = stage;
        if (warning != null) {
            addAtlasDialog(warning);
        }
        addAtlasDialog(AtlasSession.HELLO_MESSAGE);
        addAtlasDialog(AtlasSession.PROMPT_MESSAGE);
    }

    /**
     * Handles a Send click or Enter key press: shows the user's message, runs
     * the command, and shows Atlas's reply bubble.
     */
    @FXML
    private void handleUserInput() {
        String userText = userInput.getText();
        userInput.clear();
        if (userText.isBlank()) {
            return;
        }
        addUserDialog(userText);
        if (userText.equals(Command.BYE.getWord())) {
            addAtlasDialog(AtlasSession.GOODBYE_MESSAGE);
            PauseTransition pause = new PauseTransition(Duration.millis(700));
            pause.setOnFinished(event -> stage.close());
            pause.play();
            return;
        }
        collector.clear();
        session.respond(userText);
        addAtlasDialog(collector.getReply());
    }

    /**
     * Appends one user message bubble to the chat.
     *
     * @param text the message text to show.
     */
    private void addUserDialog(String text) {
        dialogContainer.getChildren().add(DialogBox.getUserDialog(text));
    }

    /**
     * Appends one Atlas reply bubble to the chat.
     *
     * @param text the reply text to show.
     */
    private void addAtlasDialog(String text) {
        dialogContainer.getChildren().add(DialogBox.getAtlasDialog(text));
    }
}
