
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;


public class OrderPanel extends JPanel {
    private JTable menuTable;
    private JTable cartTable;
    private DefaultTableModel menuModel;
    private DefaultTableModel cartModel;
    private JLabel totalLabel;
    private JLabel orderIdLabel;
    private JTextArea statusArea;
    private OrderManagement orderManager;

    public OrderPanel() {
        orderManager = new OrderManagement(this);
        initializeUI();
        orderManager.loadMenu();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("Order Application", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // Main split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);
        splitPane.setResizeWeight(0.5);

        // Left Panel: Menu Browser
        JPanel leftPanel = createMenuPanel();
        splitPane.setLeftComponent(leftPanel);

        // Right Panel: Order Summary
        JPanel rightPanel = createOrderPanel();
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Menu / Product Browser"));

        // Menu table
        String[] menuColumns = {"Item Name", "Price ($)", "Stock"};
        menuModel = new DefaultTableModel(menuColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        menuTable = new JTable(menuModel);
        menuTable.setRowHeight(22);
        JScrollPane scrollPane = new JScrollPane(menuTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add to cart button
        JButton addButton = new JButton("Add to Order");
        addButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        addButton.addActionListener(e -> orderManager.addToCart());
        panel.add(addButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Current Order"));

        // Cart table
        String[] cartColumns = {"Item Name", "Quantity", "Unit Price ($)", "Subtotal ($)"};
        cartModel = new DefaultTableModel(cartColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(22);
        JScrollPane scrollPane = new JScrollPane(cartTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Order details panel
        JPanel detailsPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Order Details"));
        
        detailsPanel.add(new JLabel("Order ID:"));
        orderIdLabel = new JLabel("---");
        detailsPanel.add(orderIdLabel);
        
        detailsPanel.add(new JLabel("Total Amount:"));
        totalLabel = new JLabel("$0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        detailsPanel.add(totalLabel);
        
        detailsPanel.add(new JLabel("Status:"));
        statusArea = new JTextArea(2, 20);
        statusArea.setEditable(false);
        statusArea.setBackground(panel.getBackground());
        detailsPanel.add(new JScrollPane(statusArea));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        JButton removeBtn = new JButton("Remove Item");
        removeBtn.addActionListener(e -> orderManager.removeFromCart());
        
        JButton updateQtyBtn = new JButton("Update Quantity");
        updateQtyBtn.addActionListener(e -> orderManager.updateQuantity());
        
        JButton placeBtn = new JButton("Place Order");
        placeBtn.setBackground(new Color(50, 200, 50));
        placeBtn.addActionListener(e -> orderManager.placeOrder());
        
        JButton cancelBtn = new JButton("Cancel Order");
        cancelBtn.setBackground(new Color(255, 100, 100));
        cancelBtn.addActionListener(e -> orderManager.cancelOrder());
        
        JButton clearBtn = new JButton("Clear Cart");
        clearBtn.addActionListener(e -> orderManager.clearCart());
        
        //new
        JButton viewOrdersBtn = new JButton("View Orders");
        viewOrdersBtn.addActionListener(e -> showOrdersDialog());

        buttonPanel.add(viewOrdersBtn);

        buttonPanel.add(removeBtn);
        buttonPanel.add(updateQtyBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(placeBtn);

        panel.add(detailsPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Getters for OrderManagement
    public JTable getMenuTable() { return menuTable; }
    public JTable getCartTable() { return cartTable; }
    public DefaultTableModel getMenuModel() { return menuModel; }
    public DefaultTableModel getCartModel() { return cartModel; }
    public JLabel getTotalLabel() { return totalLabel; }
    public JLabel getOrderIdLabel() { return orderIdLabel; }
    public JTextArea getStatusArea() { return statusArea; }

    public void updateTotal(double total) {
        totalLabel.setText(String.format("$%.2f", total));
    }

    public void setOrderId(String id) {
        orderIdLabel.setText(id);
    }

    public void setStatusMessage(String message) {
        statusArea.setText(message);
    }

    public void appendStatusMessage(String message) {
        statusArea.append("\n" + message);
    }

    public void clearCartTable() {
        cartModel.setRowCount(0);
        updateTotal(0);
    }
    
    private void showOrdersDialog() {
    JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Order History", true);
    dialog.setSize(600, 400);
    dialog.setLocationRelativeTo(this);

    String[] cols = {"Order ID", "Item", "Qty", "Total ($)", "Date"};
    DefaultTableModel model = new DefaultTableModel(cols, 0);
    JTable table = new JTable(model);

    java.util.List<OrderManagement.OrderRecord> orders = orderManager.getOrderHistory();

    for (OrderManagement.OrderRecord o : orders) {
        model.addRow(new Object[]{
            o.getId(),
            o.getItem(),
            o.getQuantity(),
            String.format("%.2f", o.getTotal()),
            o.getTimestamp()
        });
    }

    dialog.add(new JScrollPane(table));
    dialog.setVisible(true);
}
}