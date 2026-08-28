package atlas.task;

import java.util.ArrayList;

/**
 * Contains the task list and the operations that change it.
 */
public class TaskList {

    private final ArrayList<Task> tasks;

    public TaskList(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    /** Creates an empty task list. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    public int size() {
        return tasks.size();
    }

    public Task get(int index) {
        return tasks.get(index);
    }

    public void add(Task task) {
        tasks.add(task);
    }

    public Task remove(int index) {
        return tasks.remove(index);
    }

    /**
     * Returns tasks whose descriptions contain the supplied keyword.
     * Matching is case-sensitive and preserves the original task order.
     *
     * @param keyword text to search for in each task description.
     * @return matching tasks, or an empty list when no task matches.
     */
    public ArrayList<Task> find(String keyword) {
        ArrayList<Task> matches = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getDescription().contains(keyword)) {
                matches.add(task);
            }
        }
        return matches;
    }

    /** Returns the underlying list, for saving to disk. */
    public ArrayList<Task> all() {
        return tasks;
    }
}
