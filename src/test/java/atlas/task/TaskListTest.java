package atlas.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests adding, retrieving, and removing tasks from a task list. */
public class TaskListTest {

    @Test
    void newTaskListIsEmpty() {
        TaskList taskList = new TaskList();

        assertTrue(taskList.isEmpty());
        assertEquals(0, taskList.size());
    }

    @Test
    void addingTasksPreservesTheirOrder() {
        TaskList taskList = new TaskList();
        Task first = new Todo("first");
        Task second = new Todo("second");

        taskList.add(first);
        taskList.add(second);

        assertEquals(2, taskList.size());
        assertSame(first, taskList.get(0));
        assertSame(second, taskList.get(1));
    }

    @Test
    void removingTaskReturnsItAndClosesTheGap() {
        TaskList taskList = new TaskList();
        Task first = new Todo("first");
        Task second = new Todo("second");
        taskList.add(first);
        taskList.add(second);

        Task removed = taskList.remove(0);

        assertSame(first, removed);
        assertEquals(1, taskList.size());
        assertSame(second, taskList.get(0));
    }
}
