package atlas.task;

/** Represents a task that Atlas can track. */
public class Task {
    /** The text describing the work to be done. */
    private final String description;

    /** Whether the task has been marked as complete. */
    private boolean isDone;

    /**
     * Creates an incomplete task with the given description.
     *
     * @param description text describing the task.
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Returns the status icon used when displaying this task.
     *
     * @return {@code X} for a completed task, or a blank space otherwise.
     */
    public String getStatusIcon() {
        return (isDone ? "X" : " "); // mark done task with X
    }

    /**
     * Returns the task description.
     *
     * @return task description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns whether the task has been marked as done.
     *
     * @return {@code true} when the task is complete.
     */
    public boolean isDone() {
        return isDone;
    }

    /** Marks this task as complete. */
    public void markAsDone() {
        this.isDone = true;
    }

    /** Marks this task as incomplete. */
    public void markAsNotDone() {
        this.isDone = false;
    }

    /**
     * Returns the task in the display format shared by all task types.
     *
     * @return status icon followed by the task description.
     */
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
