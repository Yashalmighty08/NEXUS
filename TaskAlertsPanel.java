import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class TaskAlertsPanel extends JPanel {

    private User currentUser;
    private TaskAlertsManagement alertsManager;
    private DefaultTableModel tableModel;
    private JTable taskTable;

    public TaskAlertsPanel(User user) {
        this.currentUser = user;
        this.alertsManager = new TaskAlertsManagement(user);
        initializeUI();
        loadTasks();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Task Alerts - " + currentUser.getUsername(), JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        String[] cols = {"Description", "Deadline", "Priority", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        taskTable = new JTable(tableModel);
        taskTable.setRowHeight(22);
        add(new JScrollPane(taskTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton updateBtn = new JButton("Update Status");
        JButton refreshBtn = new JButton("Refresh");
        updateBtn.addActionListener(e -> updateStatus());
        refreshBtn.addActionListener(e -> loadTasks());
        bottomPanel.add(updateBtn);
        bottomPanel.add(refreshBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadTasks() {
        tableModel.setRowCount(0);
        boolean isManager = "Manager".equals(currentUser.getRole());
        List<TaskManagement.TaskEntry> tasks = isManager
                ? alertsManager.getAllTasks()
                : alertsManager.getTasksForCurrentUser();
        for (TaskManagement.TaskEntry t : tasks) {
            tableModel.addRow(new Object[]{t.description, t.deadline, t.priority, t.status});
        }
    }

    private void updateStatus() {
        int row = taskTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a task first.");
            return;
        }
        String desc          = (String) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 3);
        String newStatus = (String) JOptionPane.showInputDialog(
                this,
                "Update status for:\n" + desc,
                "Update Status",
                JOptionPane.PLAIN_MESSAGE,
                null,
                new String[]{"Pending", "In Progress", "Completed", "Cancelled"},
                currentStatus
        );
        if (newStatus != null && !newStatus.equals(currentStatus)) {
            new TaskManagement().updateTaskStatus(currentUser.getUsername(), desc, newStatus);
            loadTasks();
        }
    }
}