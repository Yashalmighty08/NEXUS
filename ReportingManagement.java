
import java.io.*;
import java.util.*;

public class ReportingManagement {
    private static final String TASKS_FILE = "tasks.txt";

    public static class ReportRow {
        public String employee;
        public int assigned;
        public int inProgress;
        public int completed;
        public double efficiency;

        public ReportRow(String employee) {
            this.employee = employee;
            this.assigned = 0;
            this.inProgress = 0;
            this.completed = 0;
            this.efficiency = 0.0;
        }
    }

    public List<ReportRow> generateTeamReport() {
        Map<String, ReportRow> summary = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(TASKS_FILE))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine && line.startsWith("Employee")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;
                String[] parts = line.split(",");
                if (parts.length < 5) continue;
                String employee = parts[0].trim();
                String status = parts[4].trim().toLowerCase();
                summary.putIfAbsent(employee, new ReportRow(employee));
                ReportRow row = summary.get(employee);
                row.assigned++;
                if (status.equals("pending") || status.equals("in progress")) {
                    row.inProgress++;
                } else if (status.equals("completed")) {
                    row.completed++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (ReportRow row : summary.values()) {
            if (row.assigned > 0) {
                row.efficiency = (row.completed * 100.0) / row.assigned;
            }
        }
        List<ReportRow> result = new ArrayList<>(summary.values());
        result.sort(Comparator.comparing(r -> r.employee.toLowerCase()));
        return result;
    }

    public ReportRow generateIndividualReport(String employeeName) {
        List<ReportRow> team = generateTeamReport();
        for (ReportRow row : team) {
            if (row.employee.equalsIgnoreCase(employeeName)) {
                return row;
            }
        }
        return new ReportRow(employeeName);
    }
}