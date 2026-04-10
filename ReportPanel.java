
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportPanel extends JPanel {
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private ReportingManagement reportSystem;
    private JComboBox<String> employeeCombo;
    private JPanel detailPanel;

    public ReportPanel() {
        reportSystem = new ReportingManagement();
        initializeUI();
        loadTeamReport();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton teamReportBtn = new JButton("Team Report");
        JButton individualReportBtn = new JButton("Individual Report");
        JButton dashboardBtn = new JButton("Performance Dashboard");
        JButton exportBtn = new JButton("Export Report");

        employeeCombo = new JComboBox<>();
        refreshEmployeeList();

        controlPanel.add(teamReportBtn);
        controlPanel.add(individualReportBtn);
        controlPanel.add(new JLabel("Select Employee:"));
        controlPanel.add(employeeCombo);
        controlPanel.add(dashboardBtn);
        controlPanel.add(exportBtn);

        tableModel = new DefaultTableModel(new String[]{"Employee", "Assigned", "In Progress", "Completed", "Efficiency (%)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        reportTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reportTable);

        detailPanel = new JPanel(new BorderLayout());
        detailPanel.setVisible(false);

        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(detailPanel, BorderLayout.SOUTH);

        teamReportBtn.addActionListener(e -> loadTeamReport());
        individualReportBtn.addActionListener(e -> loadIndividualReport());
        dashboardBtn.addActionListener(e -> showDashboard());
        exportBtn.addActionListener(e -> exportReport());
    }

    private void refreshEmployeeList() {
        employeeCombo.removeAllItems();
        List<ReportingManagement.ReportRow> team = reportSystem.generateTeamReport();
        for (ReportingManagement.ReportRow row : team) {
            employeeCombo.addItem(row.employee);
        }
    }

    private void loadTeamReport() {
        detailPanel.setVisible(false);
        tableModel.setRowCount(0);
        List<ReportingManagement.ReportRow> team = reportSystem.generateTeamReport();
        for (ReportingManagement.ReportRow row : team) {
            tableModel.addRow(new Object[]{
                row.employee,
                row.assigned,
                row.inProgress,
                row.completed,
                String.format("%.1f", row.efficiency)
            });
        }
    }

    private void loadIndividualReport() {
        String selected = (String) employeeCombo.getSelectedItem();
        if (selected == null) return;
        ReportingManagement.ReportRow row = reportSystem.generateIndividualReport(selected);
        detailPanel.setVisible(false);
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{
            row.employee,
            row.assigned,
            row.inProgress,
            row.completed,
            String.format("%.1f", row.efficiency)
        });
    }

    private void showDashboard() {
        detailPanel.setVisible(true);
        detailPanel.removeAll();
        List<ReportingManagement.ReportRow> team = reportSystem.generateTeamReport();
        int totalAssigned = 0, totalCompleted = 0, totalInProgress = 0;
        for (ReportingManagement.ReportRow row : team) {
            totalAssigned += row.assigned;
            totalCompleted += row.completed;
            totalInProgress += row.inProgress;
        }
        JTextArea dashboard = new JTextArea();
        dashboard.setEditable(false);
        dashboard.setFont(new Font("Monospaced", Font.PLAIN, 14));
        dashboard.setText(String.format(
            "=== PERFORMANCE DASHBOARD ===\n\n" +
            "Overall Statistics:\n" +
            "  Total Tasks Assigned: %d\n" +
            "  Completed: %d (%.1f%%)\n" +
            "  In Progress: %d\n" +
            "  Pending: %d\n\n" +
            "Employee Breakdown:\n",
            totalAssigned, totalCompleted,
            totalAssigned == 0 ? 0 : (totalCompleted * 100.0 / totalAssigned),
            totalInProgress,
            totalAssigned - totalCompleted - totalInProgress
        ));
        for (ReportingManagement.ReportRow row : team) {
            dashboard.append(String.format("  %-15s | Assigned: %3d | Completed: %3d | Efficiency: %5.1f%%\n",
                row.employee, row.assigned, row.completed, row.efficiency));
        }
        JScrollPane sp = new JScrollPane(dashboard);
        sp.setPreferredSize(new Dimension(800, 300));
        detailPanel.add(sp, BorderLayout.CENTER);
    }

    private void exportReport() {
        int rowCount = tableModel.getRowCount();
        if (rowCount == 0) {
            JOptionPane.showMessageDialog(this, "No report data to export.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("report_" +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(chooser.getSelectedFile())) {
                pw.println("FOCUS Report - Generated: " + LocalDateTime.now());
                pw.println("--------------------------------------------------");
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    pw.print(tableModel.getColumnName(i) + "\t");
                }
                pw.println();
                for (int i = 0; i < rowCount; i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        pw.print(tableModel.getValueAt(i, j) + "\t");
                    }
                    pw.println();
                }
                JOptionPane.showMessageDialog(this, "Report exported successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
            }
        }
    }
}