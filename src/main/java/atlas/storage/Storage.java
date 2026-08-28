package atlas.storage;

import atlas.AtlasException;
import atlas.task.Deadline;
import atlas.task.Event;
import atlas.task.Task;
import atlas.task.Todo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and saves the task list from and to a text file on disk.
 *
 * <p>File format: one task per line, fields separated by '|':
 * <pre>
 *   Todo:     T | 1 | description
 *   Deadline: D | 1 | description | by
 *   Event:    E | 1 | description | from | to
 * </pre>
 * The second field is 1 if the task is done and 0 otherwise. Literal '|' and
 * '\' characters inside stored text are escaped as '\|' and '\\', so any user
 * input round-trips through the file unchanged.
 */
public class Storage {

    private final Path filePath;

    public Storage(String filePath) {
        this.filePath = Paths.get(filePath);
    }

    /**
     * Loads tasks from the data file.
     * Returns an empty list when the file does not exist yet (first run).
     * Lines that cannot be parsed are skipped with a warning, so one corrupted
     * line does not destroy the rest of the list.
     *
     * @throws AtlasException if the file exists but cannot be read
     */
    public ArrayList<Task> load() throws AtlasException {
        ArrayList<Task> tasks = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return tasks;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new AtlasException("The scroll of tasks could not be read: " + e.getMessage());
        }
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                tasks.add(parseLine(line));
            } catch (AtlasException e) {
                System.out.println("Atlas skips a corrupted line " + (i + 1) + ": " + e.getMessage());
            }
        }
        return tasks;
    }

    /**
     * Saves every task to the data file, creating the data folder first if it
     * does not exist.
     *
     * @throws AtlasException if the file cannot be written
     */
    public void save(ArrayList<Task> tasks) throws AtlasException {
        StringBuilder content = new StringBuilder();
        for (Task task : tasks) {
            content.append(toFileLine(task)).append(System.lineSeparator());
        }
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            Files.writeString(filePath, content.toString());
        } catch (IOException e) {
            throw new AtlasException("The scroll of tasks could not be saved: " + e.getMessage());
        }
    }

    private String toFileLine(Task task) {
        String done = task.isDone() ? "1" : "0";
        String description = escape(task.getDescription());
        if (task instanceof Todo) {
            return "T | " + done + " | " + description;
        }
        if (task instanceof Deadline) {
            Deadline deadline = (Deadline) task;
            return "D | " + done + " | " + description + " | " + deadline.getBy().toString();
        }
        if (task instanceof Event) {
            Event event = (Event) task;
            return "E | " + done + " | " + description + " | " + escape(event.getFrom())
                    + " | " + escape(event.getTo());
        }
        throw new AssertionError("Unknown task type: " + task);
    }

    private Task parseLine(String line) throws AtlasException {
        String[] parts = line.split("(?<!\\\\)\\|", -1);
        if (parts.length < 3) {
            throw new AtlasException("too few fields");
        }
        String type = parts[0].trim();
        String doneField = parts[1].trim();
        if (!doneField.equals("0") && !doneField.equals("1")) {
            throw new AtlasException("done flag is not 0 or 1");
        }
        boolean isDone = doneField.equals("1");
        String description = unescape(parts[2].trim());
        switch (type) {
            case "T":
                if (parts.length != 3) {
                    throw new AtlasException("todo has extra fields");
                }
                Todo todo = new Todo(description);
                if (isDone) {
                    todo.markAsDone();
                }
                return todo;
            case "D":
                if (parts.length != 4) {
                    throw new AtlasException("deadline needs a by field");
                }
                LocalDate by;
                try {
                    by = LocalDate.parse(unescape(parts[3].trim()));
                } catch (DateTimeParseException e) {
                    throw new AtlasException("deadline by is not a date");
                }
                Deadline deadline = new Deadline(description, by);
                if (isDone) {
                    deadline.markAsDone();
                }
                return deadline;
            case "E":
                if (parts.length != 5) {
                    throw new AtlasException("event needs from and to fields");
                }
                Event event = new Event(description, unescape(parts[3].trim()), unescape(parts[4].trim()));
                if (isDone) {
                    event.markAsDone();
                }
                return event;
            default:
                throw new AtlasException("unknown task type '" + type + "'");
        }
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("|", "\\|");
    }

    private static String unescape(String text) {
        return text.replace("\\\\", "\\").replace("\\|", "|");
    }
}
