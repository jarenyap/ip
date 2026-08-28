package atlas.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests the common state and display behaviour of tasks. */
public class TaskTest {

    @Test
    void newTaskStoresItsDescriptionAndStartsIncomplete() {
        Task task = new Task("read the Odyssey");

        assertEquals("read the Odyssey", task.getDescription());
        assertFalse(task.isDone());
        assertEquals("[ ] read the Odyssey", task.toString());
    }

    @Test
    void markingTaskAsDoneChangesItsStatus() {
        Task task = new Task("read the Odyssey");

        task.markAsDone();

        assertTrue(task.isDone());
        assertEquals("[X] read the Odyssey", task.toString());
    }

    @Test
    void markingTaskAsNotDoneRestoresItsIncompleteStatus() {
        Task task = new Task("read the Odyssey");
        task.markAsDone();

        task.markAsNotDone();

        assertFalse(task.isDone());
        assertEquals("[ ] read the Odyssey", task.toString());
    }
}
