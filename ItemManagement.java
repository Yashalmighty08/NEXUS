import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;

public class ItemManagement {
    private ItemPanel itemPanel;
    private DefaultTableModel tableModel;
    private static final String DATA_FILE = "items.txt";

    public ItemManagement(ItemPanel panel) {
        this.itemPanel = panel;
        initializeListeners();
        loadItems();
    }

    private void initializeListeners() {
        itemPanel.getAddItemButton().addActionListener(e -> addItem());
        itemPanel.getEditItemButton().addActionListener(e -> editItem());
        itemPanel.getDeleteItemButton().addActionListener(e -> deleteItem());
        itemPanel.getRefreshButton().addActionListener(e -> loadItems());
    }

    public void loadItems() {
        try {
            File file = new File(DATA_FILE);
            if (!file.exists()) {
                createDefaultItems();
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String headerLine = reader.readLine();

            Vector<String> columnNames = new Vector<>();
            columnNames.add("Item Name");
            columnNames.add("Price ($)");
            columnNames.add("Current Stock");
            columnNames.add("Low Stock Threshold");

            tableModel = new DefaultTableModel(columnNames, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
                @Override public Class<?> getColumnClass(int column) {
                    if (column == 1) return Double.class;
                    if (column == 2 || column == 3) return Integer.class;
                    return String.class;
                }
            };

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String name      = parts[0].trim();
                    double price     = Double.parseDouble(parts[1].trim());
                    int stock        = Integer.parseInt(parts[2].trim());
                    int threshold    = Integer.parseInt(parts[3].trim());
                    tableModel.addRow(new Object[]{name, price, stock, threshold});
                }
            }
            reader.close();

            itemPanel.getItemTable().setModel(tableModel);
            highlightLowStockItems();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(itemPanel,
                "Error loading items: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createDefaultItems() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            writer.println("Item Name,Price,Current Stock,Low Stock Threshold");
            writer.println("Milk,2.50,50,10");
            writer.println("Bread,1.80,30,5");
            writer.println("Eggs,3.00,100,20");
            writer.println("Butter,4.50,25,5");
            writer.println("Sugar,1.20,200,15");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(itemPanel,
                "Error creating default items file: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        loadItems();
    }

    private void highlightLowStockItems() {
        itemPanel.getItemTable().setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    try {
                        int stock     = (int) tableModel.getValueAt(row, 2);
                        int threshold = (int) tableModel.getValueAt(row, 3);
                        if (stock < threshold) {
                            c.setBackground(Color.PINK);
                            c.setForeground(Color.RED);
                        } else {
                            c.setBackground(Color.WHITE);
                            c.setForeground(Color.BLACK);
                        }
                    } catch (Exception e) {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        });
    }

    private void addItem() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(itemPanel),
                "Add New Item", true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(itemPanel);

        JTextField nameField      = new JTextField();
        JTextField priceField     = new JTextField();
        JTextField stockField     = new JTextField();
        JTextField thresholdField = new JTextField();

        dialog.add(new JLabel("Item Name:"));       dialog.add(nameField);
        dialog.add(new JLabel("Price ($):"));        dialog.add(priceField);
        dialog.add(new JLabel("Initial Stock:"));    dialog.add(stockField);
        dialog.add(new JLabel("Low Stock Threshold:")); dialog.add(thresholdField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveBtn   = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) throw new Exception("Name required");
                double price     = Double.parseDouble(priceField.getText().trim());
                int stock        = Integer.parseInt(stockField.getText().trim());
                int threshold    = Integer.parseInt(thresholdField.getText().trim());
                if (price < 0 || stock < 0 || threshold < 0) throw new NumberFormatException();

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (tableModel.getValueAt(i, 0).equals(name)) {
                        JOptionPane.showMessageDialog(dialog,
                            "Item already exists!", "Duplicate", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                tableModel.addRow(new Object[]{name, price, stock, threshold});
                saveCurrentTableToFile();
                highlightLowStockItems();
                dialog.dispose();
                JOptionPane.showMessageDialog(itemPanel,
                    "Item '" + name + "' added successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Please enter valid values (name, positive numbers).",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel);
        dialog.setVisible(true);
    }

    private void editItem() {
        int selectedRow = itemPanel.getItemTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(itemPanel, "Please select an item to edit.");
            return;
        }

        String itemName       = (String) tableModel.getValueAt(selectedRow, 0);
        double currentPrice   = (double) tableModel.getValueAt(selectedRow, 1);
        int currentStock      = (int)    tableModel.getValueAt(selectedRow, 2);
        int currentThreshold  = (int)    tableModel.getValueAt(selectedRow, 3);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(itemPanel),
                "Edit Item", true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(itemPanel);

        dialog.add(new JLabel("Item Name:"));
        JTextField nameField = new JTextField(itemName);
        nameField.setEditable(false);
        dialog.add(nameField);

        dialog.add(new JLabel("Price ($):"));
        JTextField priceField = new JTextField(String.format("%.2f", currentPrice));
        dialog.add(priceField);

        dialog.add(new JLabel("Current Stock:"));
        JTextField stockField = new JTextField(String.valueOf(currentStock));
        dialog.add(stockField);

        dialog.add(new JLabel("Low Stock Threshold:"));
        JTextField thresholdField = new JTextField(String.valueOf(currentThreshold));
        dialog.add(thresholdField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveBtn   = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.addActionListener(e -> {
            try {
                double newPrice     = Double.parseDouble(priceField.getText().trim());
                int newStock        = Integer.parseInt(stockField.getText().trim());
                int newThreshold    = Integer.parseInt(thresholdField.getText().trim());
                if (newPrice < 0 || newStock < 0 || newThreshold < 0)
                    throw new NumberFormatException();

                tableModel.setValueAt(newPrice,    selectedRow, 1);
                tableModel.setValueAt(newStock,    selectedRow, 2);
                tableModel.setValueAt(newThreshold, selectedRow, 3);
                saveCurrentTableToFile();
                highlightLowStockItems();
                dialog.dispose();
                JOptionPane.showMessageDialog(itemPanel,
                    "Item '" + itemName + "' updated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Please enter valid non-negative numbers.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        // UC2 alt [Cancel Edit]: closes popup, original values remain unchanged
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel);
        dialog.setVisible(true);
    }

    private void deleteItem() {
        int selectedRow = itemPanel.getItemTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(itemPanel, "Please select an item to delete.");
            return;
        }

        String itemName = (String) tableModel.getValueAt(selectedRow, 0);

        // UC3 alt: confirmation popup — Yes deletes, No closes popup with no changes
        int confirm = JOptionPane.showConfirmDialog(itemPanel,
            "Are you sure you want to delete '" + itemName + "'?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(selectedRow);
            saveCurrentTableToFile();
            JOptionPane.showMessageDialog(itemPanel,
                "Item '" + itemName + "' deleted successfully.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
        // NO_OPTION: dialog closes, table remains unchanged — no action needed
    }

    private void saveCurrentTableToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            writer.println("Item Name,Price,Current Stock,Low Stock Threshold");
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                writer.println(tableModel.getValueAt(i, 0) + "," +
                               tableModel.getValueAt(i, 1) + "," +
                               tableModel.getValueAt(i, 2) + "," +
                               tableModel.getValueAt(i, 3));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(itemPanel,
                "Error saving items: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean updateStock(String itemName, int quantityToDeduct) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(itemName)) {
                int current = (int) tableModel.getValueAt(i, 2);
                if (current < quantityToDeduct) return false;
                tableModel.setValueAt(current - quantityToDeduct, i, 2);
                saveCurrentTableToFile();
                highlightLowStockItems();
                return true;
            }
        }
        return false;
    }

    public double getItemPrice(String itemName) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(itemName))
                return (double) tableModel.getValueAt(i, 1);
        }
        return 0.0;
    }

    public int getItemStock(String itemName) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(itemName))
                return (int) tableModel.getValueAt(i, 2);
        }
        return 0;
    }
}