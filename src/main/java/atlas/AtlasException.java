package atlas;

/**
 * Represents an error caused by invalid user input to Atlas.
 */
public class AtlasException extends Exception {
    private static final long serialVersionUID = 1L;

    public AtlasException(String message) {
        super(message);
    }
}
