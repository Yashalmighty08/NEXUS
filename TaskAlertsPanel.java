import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TaskAlertsPanel extends JPanel {
    private User currentUser;
    private TaskManagement taskManager;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel, pendingLabel, completedLabel;

    public TaskAlertsPanel(User user) {
        this.currentUser = user;
        this.taskManager = new TaskManagement();
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("My Tasks");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadData());
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Task table
        String[] columns = {"Description", "Deadline", "Priority", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        taskTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(taskTable);
        add(scrollPane, BorderLayout.CENTER);

        // Summary panel (bottom)
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Task Summary"));
        totalLabel = new JLabel("Total: 0", JLabel.CENTER);
        pendingLabel = new JLabel("Pending: 0", JLabel.CENTER);
        completedLabel = new JLabel("Completed: 0", JLabel.CENTER);
        summaryPanel.add(totalLabel);
        summaryPanel.add(pendingLabel);
        summaryPanel.add(completedLabel);
        add(summaryPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<TaskManagement.TaskEntry> myTasks = taskManager.getTasksForEmployee(currentUser.getUsername());
        int total = 0, pending = 0, completed = 0;
        for (TaskManagement.TaskEntry t : myTasks) {
            tableModel.addRow(new Object[]{t.description, t.deadline, t.priority, t.status});
            total++;
            if (t.status.equalsIgnoreCase("Completed")) completed++;
            else if (t.status.equalsIgnoreCase("Pending") || t.status.equalsIgnoreCase("In Progress")) pending++;
        }
        totalLabel.setText("Total: " + total);
        pendingLabel.setText("Pending/In Progress: " + pending);
        completedLabel.setText("Completed: " + completed);
    }

    // Optional: allow employee to update status inline
    public void addStatusUpdateButton() {
        JButton updateBtn = new JButton("Update Status");
        updateBtn.addActionListener(e -> updateSelectedTaskStatus());
        add(updateBtn, BorderLayout.NORTH); // or place elsewhere
    }

    private void updateSelectedTaskStatus() {
        int row = taskTable.getSelectedRow();
        if (row == -1) return;
        String description = (String) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 3);
        String newStatus = (String) JOptionPane.showInputDialog(this,
                "Update status for:\n" + description,
                "Update Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Pending", "In Progress", "Completed"},
                currentStatus);
        if (newStatus != null && !newStatus.equals(currentStatus)) {
            taskManager.updateTaskStatus(currentUser.getUsername(), description, newStatus);
            loadData();
        }
    }
}