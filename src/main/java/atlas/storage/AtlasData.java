package atlas.storage;

import java.util.ArrayList;

import atlas.client.Client;
import atlas.task.Task;

/**
 * One reading of the storage file: the tasks and the clients it holds.
 */
public class AtlasData {

    /** Tasks loaded from the file. */
    private final ArrayList<Task> tasks;
    /** Clients loaded from the file. */
    private final ArrayList<Client> clients;

    /**
     * Creates a snapshot of the loaded data.
     *
     * @param tasks tasks loaded from the file.
     * @param clients clients loaded from the file.
     */
    public AtlasData(ArrayList<Task> tasks, ArrayList<Client> clients) {
        assert tasks != null : "loaded tasks must not be null";
        assert clients != null : "loaded clients must not be null";
        this.tasks = tasks;
        this.clients = clients;
    }

    /**
     * Returns the tasks held in this snapshot.
     *
     * @return loaded tasks.
     */
    public ArrayList<Task> getTasks() {
        return tasks;
    }

    /**
     * Returns the clients held in this snapshot.
     *
     * @return loaded clients.
     */
    public ArrayList<Client> getClients() {
        return clients;
    }
}
