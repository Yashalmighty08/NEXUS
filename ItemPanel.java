
import javax.swing.*;
import java.awt.*;

public class ItemPanel extends JPanel {
    private JTable itemTable;
    private JButton addItemButton;
    private JButton editItemButton;
    private JButton deleteItemButton;
    private JButton refreshButton;

    public ItemPanel() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addItemButton = new JButton("Add Item");
        editItemButton = new JButton("Edit Item");
        deleteItemButton = new JButton("Delete Item");
        refreshButton = new JButton("Refresh");

        buttonPanel.add(addItemButton);
        buttonPanel.add(editItemButton);
        buttonPanel.add(deleteItemButton);
        buttonPanel.add(refreshButton);

        // Table
        itemTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(itemTable);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Getters
    public JTable getItemTable() { return itemTable; }
    public JButton getAddItemButton() { return addItemButton; }
    public JButton getEditItemButton() { return editItemButton; }
    public JButton getDeleteItemButton() { return deleteItemButton; }
    public JButton getRefreshButton() { return refreshButton; }
}