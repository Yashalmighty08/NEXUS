
import java.io.*;
import java.util.*;

public class TaskManagement {
    private static final String TASKS_FILE = "tasks.txt";

    public static class TaskEntry {
        public String employee;
        public String description;
        public String deadline;
        public String priority;
        public String status;

        public TaskEntry(String employee, String description, String deadline, String priority, String status) {
            this.employee = employee;
            this.description = description;
            this.deadline = deadline;
            this.priority = priority;
            this.status = status;
        }
    }

    // Load all tasks from file
    public List<TaskEntry> loadAllTasks() {
        List<TaskEntry> tasks = new ArrayList<>();
        File file = new File(TASKS_FILE);
        if (!file.exists()) return tasks;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine && line.startsWith("Employee")) {
                    firstLine = false;
                    continue; // skip header
                }
                firstLine = false;
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    tasks.add(new TaskEntry(
                        parts[0].trim(),
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim(),
                        parts[4].trim()
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    // Save all tasks back to file
    public void saveAllTasks(List<TaskEntry> tasks) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(TASKS_FILE))) {
            pw.println("Employee,Description,Deadline,Priority,Status");
            for (TaskEntry t : tasks) {
                pw.println(String.join(",",
                    t.employee,
                    t.description,
                    t.deadline,
                    t.priority,
                    t.status
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Create a new task (appends to file)
    public boolean createTask(String employee, String description, String deadline, String priority) {
        if (employee == null || employee.trim().isEmpty() ||
            description == null || description.trim().isEmpty() ||
            deadline == null || deadline.trim().isEmpty()) {
            return false; // caller shows error
        }
        if (!deadline.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(TASKS_FILE, true))) {
            pw.println(String.join(",", employee, description, deadline, priority, "Pending"));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update status of a specific task (find by employee and description)
    public void updateTaskStatus(String employee, String description, String newStatus) {
        List<TaskEntry> tasks = loadAllTasks();
        for (TaskEntry t : tasks) {
            if (t.employee.equalsIgnoreCase(employee) && t.description.equals(description)) {
                t.status = newStatus;
                break;
            }
        }
        saveAllTasks(tasks);
    }

    // Get tasks for a specific employee
    public List<TaskEntry> getTasksForEmployee(String employeeName) {
        List<TaskEntry> all = loadAllTasks();
        List<TaskEntry> result = new ArrayList<>();
        for (TaskEntry t : all) {
            if (t.employee.equalsIgnoreCase(employeeName)) {
                result.add(t);
            }
        }
        return result;
    }
}