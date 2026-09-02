package atlas.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import atlas.AtlasException;
import atlas.task.Deadline;
import atlas.task.Event;
import atlas.task.Task;
import atlas.task.Todo;

/** Tests persistence of task data and recovery from malformed storage lines. */
public class StorageTest {

    @TempDir
    private Path temporaryDirectory;

    @Test
    void missingFileLoadsAsAnEmptyList() throws AtlasException {
        Storage storage = new Storage(temporaryDirectory.resolve("atlas.txt").toString());

        assertTrue(storage.load().isEmpty());
    }

    @Test
    void savesAndLoadsAllTaskTypesAndTheirCompletionState() throws AtlasException {
        Path storagePath = temporaryDirectory.resolve("data/atlas.txt");
        Storage storage = new Storage(storagePath.toString());
        Todo todo = new Todo("read the Odyssey");
        todo.markAsDone();
        Deadline deadline = new Deadline("return book", LocalDate.of(2026, 9, 1));
        Event event = new Event("project meeting", "2pm", "4pm");
        ArrayList<Task> tasks = new ArrayList<>(List.of(todo, deadline, event));

        storage.save(tasks);
        ArrayList<Task> loaded = storage.load();

        assertEquals(3, loaded.size());
        assertEquals("read the Odyssey", loaded.get(0).getDescription());
        assertTrue(loaded.get(0).isDone());
        Deadline loadedDeadline = assertInstanceOf(Deadline.class, loaded.get(1));
        assertEquals("return book", loadedDeadline.getDescription());
        assertEquals(LocalDate.of(2026, 9, 1), loadedDeadline.getBy());
        Event loadedEvent = assertInstanceOf(Event.class, loaded.get(2));
        assertEquals("project meeting", loadedEvent.getDescription());
        assertEquals("2pm", loadedEvent.getFrom());
        assertEquals("4pm", loadedEvent.getTo());
        assertFalse(loadedEvent.isDone());
    }

    @Test
    void preservesSpecialCharactersDuringRoundTrip() throws AtlasException {
        Path storagePath = temporaryDirectory.resolve("atlas.txt");
        Storage storage = new Storage(storagePath.toString());
        Event event = new Event("review | notes \\ soon", "Mon | 2pm \\ start", "Mon 4pm | end \\");

        storage.save(new ArrayList<>(List.of(event)));
        Event loaded = assertInstanceOf(Event.class, storage.load().get(0));

        assertEquals("review | notes \\ soon", loaded.getDescription());
        assertEquals("Mon | 2pm \\ start", loaded.getFrom());
        assertEquals("Mon 4pm | end \\", loaded.getTo());
    }

    @Test
    void skipsMalformedLinesAndLoadsTheValidLines() throws IOException, AtlasException {
        Path storagePath = temporaryDirectory.resolve("atlas.txt");
        Files.writeString(storagePath, String.join(System.lineSeparator(),
                "T | 0 | keep",
                "not a valid task",
                "E | 1 | meeting | 10am | 11am"));

        ArrayList<Task> loaded = new Storage(storagePath.toString()).load();

        assertEquals(2, loaded.size());
        assertEquals("keep", loaded.get(0).getDescription());
        Event event = assertInstanceOf(Event.class, loaded.get(1));
        assertTrue(event.isDone());
        assertEquals("meeting", event.getDescription());
    }
}
