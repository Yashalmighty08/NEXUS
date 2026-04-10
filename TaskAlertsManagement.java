
import java.util.List;

public class TaskAlertsManagement {
    private TaskManagement taskManager;
    private User currentUser;

    public TaskAlertsManagement(User user) {
        this.currentUser = user;
        this.taskManager = new TaskManagement();
    }

    public List<TaskManagement.TaskEntry> getTasksForCurrentUser() {
        return taskManager.getTasksForEmployee(currentUser.getUsername());
    }

    public List<TaskManagement.TaskEntry> getAllTasks() {
        return taskManager.loadAllTasks();
    }
}