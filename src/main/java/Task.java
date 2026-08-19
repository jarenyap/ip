public class Task {
    protected String description;
    protected boolean isDone;
    protected String type;   // "T", "D", or "E"
    protected String by;     // deadline due time; null if not a deadline
    protected String from;   // event start; null if not an event
    protected String to;     // event end; null if not an event

    public Task(String type, String description) {
        this.type = type;
        this.description = description;
        this.isDone = false;
    }

    public Task(String type, String description, String by) {
        this.type = type;
        this.description = description;
        this.isDone = false;
        this.by = by;
    }

    public Task(String type, String description, String from, String to) {
        this.type = type;
        this.description = description;
        this.isDone = false;
        this.from = from;
        this.to = to;
    }

    public String getStatusIcon() {
        return (isDone ? "X" : " "); // mark done task with X
    }

    public void markAsDone() {
        this.isDone = true;
    }

    public void markAsNotDone() {
        this.isDone = false;
    }

    public String toString() {
        String result = "[" + type + "][" + getStatusIcon() + "] " + description;
        if (by != null) {
            result += " (by: " + by + ")";
        }
        if (from != null) {
            result += " (from: " + from + " to: " + to + ")";
        }
        return result;
    }
}
