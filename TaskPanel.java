
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TaskPanel extends JPanel {
    private TaskManagement taskManager;
    private JTable taskTable;
    private DefaultTableModel tableModel;

    public TaskPanel() {
        taskManager = new TaskManagement();
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Top panel with title and refresh
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Task Management (Manager View)");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadData());
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Employee", "Description", "Deadline", "Priority", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        taskTable = new JTable(tableModel);
        taskTable.setRowHeight(22);
        JScrollPane scrollPane = new JScrollPane(taskTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton createBtn = new JButton("Create Task");
        JButton editStatusBtn = new JButton("Edit Status");
        createBtn.addActionListener(e -> openCreateTaskDialog());
        editStatusBtn.addActionListener(e -> editTaskStatus());
        bottomPanel.add(createBtn);
        bottomPanel.add(editStatusBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<TaskManagement.TaskEntry> tasks = taskManager.loadAllTasks();
        for (TaskManagement.TaskEntry t : tasks) {
            tableModel.addRow(new Object[]{
                t.employee, t.description, t.deadline, t.priority, t.status
            });
        }
    }

    private void openCreateTaskDialog() {
        JTextField empField      = new JTextField();
        JTextField descField     = new JTextField();
        JTextField deadlineField = new JTextField();
        JComboBox<String> priorityCombo = new JComboBox<>(new String[]{"High", "Medium", "Low"});

        Object[] fields = {
            "Employee Name:", empField,
            "Description:", descField,
            "Deadline (YYYY-MM-DD):", deadlineField,
            "Priority:", priorityCombo
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Create Task",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String emp      = empField.getText().trim();
            String desc     = descField.getText().trim();
            String deadline = deadlineField.getText().trim();
            String priority = (String) priorityCombo.getSelectedItem();

            // UC5 alt [Validation fails]: show error, keep dialog open via loop or re-prompt
            if (emp.isEmpty() || desc.isEmpty() || deadline.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "All fields are required. Please correct the inputs.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!deadline.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(this,
                    "Deadline must be in format YYYY-MM-DD. Please correct the input.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean saved = taskManager.createTask(emp, desc, deadline, priority);
            if (saved) {
                JOptionPane.showMessageDialog(this,
                    "Task created successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Task could not be saved. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editTaskStatus() {
        int row = taskTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a task first.");
            return;
        }
        String employee = (String) tableModel.getValueAt(row, 0);
        String description = (String) tableModel.getValueAt(row, 1);
        String currentStatus = (String) tableModel.getValueAt(row, 4);
        String newStatus = (String) JOptionPane.showInputDialog(this,
                "Change status for:\n" + description,
                "Edit Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Pending", "In Progress", "Completed", "Cancelled"},
                currentStatus);
        if (newStatus != null && !newStatus.equals(currentStatus)) {
            taskManager.updateTaskStatus(employee, description, newStatus);
            loadData();
        }
    }
}