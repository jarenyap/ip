package atlas.gui;

import java.io.IOException;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * One chat message: a wrapping text bubble with no avatar. User messages align
 * to the right with the default label styling; Atlas replies align to the left
 * with the reply-label styling. The layout is defined in view/DialogBox.fxml.
 * The message is a {@link Text} node inside a {@link TextFlow}: unlike a
 * bounded {@link javafx.scene.control.Label}, wrapped text is never
 * ellipsized, so the full message is always shown. The TextFlow reports the
 * width of its longest line, so the bubble chrome hugs the message text
 * instead of spanning the full chat width.
 */
public class DialogBox extends HBox {

    /** Width budget lost to window margins, scrollbar and bubble padding. */
    private static final double WRAP_WIDTH_INSETS = 62.0;

    /** Fraction of the usable width long bubbles may grow to. */
    private static final double BUBBLE_WIDTH_RATIO = 0.62;

    /** Long bubbles never get narrower than this while the window allows. */
    private static final double MIN_BUBBLE_TEXT_WIDTH = 360.0;

    /** Smallest sensible wrap width; narrower windows still read fine. */
    private static final double MIN_WRAP_WIDTH = 140.0;

    /** Horizontal bubble padding, added to the cap for the bubble's own max. */
    private static final double BUBBLE_H_PADDING = 24.0;

    /** Bubble chrome: carries the background, radius, border and padding. */
    @FXML
    private StackPane bubble;

    /** Wraps the message text and lets the chrome hug its longest line. */
    @FXML
    private TextFlow flow;

    @FXML
    private Text dialog;

    /**
     * Builds this control from DialogBox.fxml and fills in the given message.
     *
     * @param text the message text to display.
     * @param isAtlasReply whether this bubble is Atlas's reply (left-aligned)
     *                     or the user's message (right-aligned).
     */
    private DialogBox(String text, boolean isAtlasReply) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(DialogBox.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dialog.setText(text);
        bindWrapWidthToWindow();
        bubble.getStyleClass().add(isAtlasReply ? "reply-label" : "label");
        if (isAtlasReply) {
            this.setAlignment(Pos.TOP_LEFT);
        }
    }

    /**
     * Sizes the message like a chat bubble at every window width. Short text
     * hugs its content; long text wraps at about 62% of the usable width (or
     * the full usable width when the window is narrow), so bubbles grow and
     * shrink with the window instead of keeping a fixed default width. The
     * scene is not known until this row joins the scene graph, so the
     * bindings are installed on first attachment. A TextFlow never
     * ellipsizes, so the full message always stays visible.
     */
    private void bindWrapWidthToWindow() {
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                DoubleBinding usable = newScene.widthProperty().subtract(WRAP_WIDTH_INSETS);
                DoubleBinding cap = Bindings.max(MIN_BUBBLE_TEXT_WIDTH,
                        usable.multiply(BUBBLE_WIDTH_RATIO));
                DoubleBinding wrap = Bindings.max(MIN_WRAP_WIDTH, Bindings.min(usable, cap));
                flow.maxWidthProperty().bind(wrap);
                bubble.maxWidthProperty().bind(wrap.add(BUBBLE_H_PADDING));
            }
        });
    }

    /**
     * Creates a dialog box for the user's message, aligned to the right.
     *
     * @param text the message text.
     * @return a dialog box aligned for the user.
     */
    public static DialogBox getUserDialog(String text) {
        return new DialogBox(text, false);
    }

    /**
     * Creates a dialog box for Atlas's reply, aligned to the left.
     *
     * @param text the reply text.
     * @return a dialog box aligned for Atlas.
     */
    public static DialogBox getAtlasDialog(String text) {
        return new DialogBox(text, true);
    }
}
