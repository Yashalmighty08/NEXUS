import java.util.List;

public class TaskAlerts {
    private TaskManagement taskManager;
    private User currentUser;

    public TaskAlerts(User user) {
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