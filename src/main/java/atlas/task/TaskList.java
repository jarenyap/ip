package atlas.task;

import java.util.ArrayList;

/**
 * Contains the task list and the operations that change it.
 */
public class TaskList {

    /** Tasks in their user-visible order. */
    private final ArrayList<Task> tasks;

    /**
     * Creates a task list backed by the supplied tasks.
     *
     * @param tasks tasks to place in the list.
     */
    public TaskList(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    /** Creates an empty task list. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Returns whether this list contains no tasks.
     *
     * @return {@code true} when the list is empty.
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Returns the number of tasks in this list.
     *
     * @return number of tasks.
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns the task at the specified zero-based position.
     *
     * @param index zero-based position of the task.
     * @return task at the specified position.
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Adds a task to the end of this list.
     *
     * @param task task to add.
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Removes and returns the task at the specified zero-based position.
     *
     * @param index zero-based position of the task to remove.
     * @return the removed task.
     */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    /**
     * Returns all tasks in their current order for persistence.
     *
     * @return the underlying task list.
     */
    public ArrayList<Task> all() {
        return tasks;
    }
}
