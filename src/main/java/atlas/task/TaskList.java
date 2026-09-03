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
     * Returns tasks whose descriptions contain any of the supplied keywords.
     * Matching is case-sensitive, ORs the keywords together, and preserves
     * the original task order. The keywords are passed as varargs, e.g.
     * find("book") or find("book", "paper").
     *
     * @param keywords one or more text fragments to search for.
     * @return matching tasks, or an empty list when no task matches.
     */
    public ArrayList<Task> find(String... keywords) {
        ArrayList<Task> matches = new ArrayList<>();
        for (Task task : tasks) {
            for (String keyword : keywords) {
                if (task.getDescription().contains(keyword)) {
                    matches.add(task);
                    break;
                }
            }
        }
        return matches;
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
