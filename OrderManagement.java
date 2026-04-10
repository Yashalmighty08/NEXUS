
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class OrderManagement {
    private OrderPanel panel;
    private ItemManagement itemManager;
    private DecimalFormat df = new DecimalFormat("#0.00");
    private static final String ORDERS_FILE = "orders.txt";

    public OrderManagement(OrderPanel panel) {
        this.panel = panel;
        // Link to existing ItemManagement instance (will be set via method)
    }

    // Set the ItemManagement reference (called from MainFrame after both are created)
    public void setItemManager(ItemManagement manager) {
        this.itemManager = manager;
    }

    public void loadMenu() {
        DefaultTableModel model = panel.getMenuModel();
        model.setRowCount(0);
        
        File file = new File("items.txt");
        if (!file.exists()) {
            panel.setStatusMessage("Items file not found. Please add items via Item Management.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine && line.startsWith("Item Name")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String name = parts[0].trim();
                    double price = Double.parseDouble(parts[1].trim());
                    int stock = Integer.parseInt(parts[2].trim());
                    if (stock > 0) { // Only show items in stock
                        model.addRow(new Object[]{name, df.format(price), stock});
                    }
                }
            }
        } catch (IOException e) {
            panel.setStatusMessage("Error loading menu: " + e.getMessage());
        }
    }

    public void addToCart() {
        int selectedRow = panel.getMenuTable().getSelectedRow();
        if (selectedRow == -1) {
            panel.setStatusMessage("Please select an item from the menu.");
            return;
        }

        String itemName = (String) panel.getMenuModel().getValueAt(selectedRow, 0);
        String priceStr = (String) panel.getMenuModel().getValueAt(selectedRow, 1);
        double price = Double.parseDouble(priceStr);
        int availableStock = (int) panel.getMenuModel().getValueAt(selectedRow, 2);

        // Check if already in cart
        DefaultTableModel cartModel = panel.getCartModel();
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if (cartModel.getValueAt(i, 0).equals(itemName)) {
                int currentQty = (int) cartModel.getValueAt(i, 1);
                if (currentQty + 1 > availableStock) {
                    panel.setStatusMessage("Cannot add more. Only " + availableStock + " " + itemName + " in stock.");
                    return;
                }
                cartModel.setValueAt(currentQty + 1, i, 1);
                updateSubtotal(i);
                updateTotal();
                panel.setStatusMessage("Added 1 more " + itemName + " to cart.");
                return;
            }
        }

        // Add new row
        if (availableStock >= 1) {
            cartModel.addRow(new Object[]{itemName, 1, df.format(price), df.format(price)});
            updateTotal();
            panel.setStatusMessage("Added " + itemName + " to cart.");
        } else {
            panel.setStatusMessage("Sorry, " + itemName + " is out of stock.");
        }
    }

    public void removeFromCart() {
        int selectedRow = panel.getCartTable().getSelectedRow();
        if (selectedRow == -1) {
            panel.setStatusMessage("Please select an item to remove.");
            return;
        }
        String itemName = (String) panel.getCartModel().getValueAt(selectedRow, 0);
        panel.getCartModel().removeRow(selectedRow);
        updateTotal();
        panel.setStatusMessage("Removed " + itemName + " from cart.");
    }

    public void updateQuantity() {
        int selectedRow = panel.getCartTable().getSelectedRow();
        if (selectedRow == -1) {
            panel.setStatusMessage("Please select an item to update.");
            return;
        }

        String itemName = (String) panel.getCartModel().getValueAt(selectedRow, 0);
        int currentQty = (int) panel.getCartModel().getValueAt(selectedRow, 1);
        
        String input = JOptionPane.showInputDialog(panel, 
            "Enter new quantity for " + itemName + ":", currentQty);
        if (input == null) return;

        try {
            int newQty = Integer.parseInt(input.trim());
            if (newQty <= 0) {
                removeFromCart();
                return;
            }
            
            // Check stock availability
            int availableStock = getStockFromFile(itemName);
            if (newQty > availableStock) {
                panel.setStatusMessage("Only " + availableStock + " " + itemName + " available in stock.");
                return;
            }
            
            panel.getCartModel().setValueAt(newQty, selectedRow, 1);
            updateSubtotal(selectedRow);
            updateTotal();
            panel.setStatusMessage("Updated " + itemName + " quantity to " + newQty);
        } catch (NumberFormatException e) {
            panel.setStatusMessage("Invalid quantity entered.");
        }
    }

    public void placeOrder() {
        DefaultTableModel cartModel = panel.getCartModel();
        if (cartModel.getRowCount() == 0) {
            panel.setStatusMessage("Cart is empty. Add items before placing order.");
            return;
        }

        // Check stock for all items before placing order
        Map<String, Integer> orderItems = new HashMap<>();
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String itemName = (String) cartModel.getValueAt(i, 0);
            int qty = (int) cartModel.getValueAt(i, 1);
            orderItems.put(itemName, qty);
        }

        // Verify stock
        StringBuilder stockIssues = new StringBuilder();
        for (Map.Entry<String, Integer> entry : orderItems.entrySet()) {
            int available = getStockFromFile(entry.getKey());
            if (available < entry.getValue()) {
                stockIssues.append("• ").append(entry.getKey())
                          .append(": need ").append(entry.getValue())
                          .append(", only ").append(available).append(" available\n");
            }
        }

        if (stockIssues.length() > 0) {
            panel.setStatusMessage("Cannot place order due to stock issues:\n" + stockIssues.toString());
            return;
        }

        // Deduct stock and save order
        double total = calculateTotal();
        String orderId = generateOrderId();
        
        // Deduct stock from items.txt via ItemManagement
        boolean allDeducted = true;
        for (Map.Entry<String, Integer> entry : orderItems.entrySet()) {
            if (itemManager != null) {
                boolean success = itemManager.updateStock(entry.getKey(), entry.getValue());
                if (!success) {
                    allDeducted = false;
                    panel.setStatusMessage("Failed to deduct stock for " + entry.getKey());
                }
            } else {
                // Fallback: direct file update
                deductStockDirect(entry.getKey(), entry.getValue());
            }
        }

        if (allDeducted || itemManager == null) {
            // Save order to file
            saveOrder(orderId, orderItems, total);
            panel.setOrderId(orderId);
            panel.setStatusMessage("Order placed successfully! Order ID: " + orderId);
            panel.appendStatusMessage("Total: $" + df.format(total));
            
            // Refresh menu to show updated stock
            loadMenu();
            clearCart();
        }
    }

    public void cancelOrder() {
        // UC16 alt [Order already confirmed]: in this implementation orders are placed
        // immediately and saved — once placed they are confirmed. Block cancellation
        // of the active (just-placed) order only if cart is empty (nothing pending).
        if (panel.getCartModel().getRowCount() == 0) {
            JOptionPane.showMessageDialog(panel,
                "Cannot cancel order. No active order in cart.\n" +
                "Orders already placed and confirmed cannot be cancelled.",
                "Cannot Cancel Confirmed Order", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(panel,
            "Are you sure you want to cancel the current order?",
            "Cancel Order", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            clearCart();
            panel.setStatusMessage("Order cancelled.");
        }
    }

    public void clearCart() {
        panel.clearCartTable();
        panel.setOrderId("---");
    }

    private void updateSubtotal(int row) {
        DefaultTableModel model = panel.getCartModel();
        int qty = (int) model.getValueAt(row, 1);
        String priceStr = (String) model.getValueAt(row, 2);
        double price = Double.parseDouble(priceStr);
        double subtotal = price * qty;
        model.setValueAt(df.format(subtotal), row, 3);
    }

    private void updateTotal() {
        double total = calculateTotal();
        panel.updateTotal(total);
    }

    private double calculateTotal() {
        double total = 0;
        DefaultTableModel model = panel.getCartModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            String subtotalStr = (String) model.getValueAt(i, 3);
            total += Double.parseDouble(subtotalStr);
        }
        return total;
    }

    private int getStockFromFile(String itemName) {
        File file = new File("items.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine && line.startsWith("Item Name")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].trim().equals(itemName)) {
                    return Integer.parseInt(parts[2].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void deductStockDirect(String itemName, int qty) {
        try {
            List<String> lines = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader("items.txt"));
            String header = br.readLine();
            lines.add(header);
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].trim().equals(itemName)) {
                    int newStock = Integer.parseInt(parts[2].trim()) - qty;
                    lines.add(parts[0] + "," + parts[1] + "," + newStock + "," + parts[3]);
                } else {
                    lines.add(line);
                }
            }
            br.close();
            PrintWriter pw = new PrintWriter(new FileWriter("items.txt"));
            for (String l : lines) pw.println(l);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateOrderId() {
        return "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    }

    private void saveOrder(String orderId, Map<String, Integer> items, double total) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ORDERS_FILE, true))) {
            StringBuilder itemsStr = new StringBuilder();
            for (Map.Entry<String, Integer> entry : items.entrySet()) {
                if (itemsStr.length() > 0) itemsStr.append("|");
                itemsStr.append(entry.getKey()).append(":").append(entry.getValue());
            }
            pw.println(orderId + "," + itemsStr.toString() + "," + df.format(total) + "," + 
                      LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static class OrderRecord {
    private String id;
    private String item;
    private int quantity;
    private double total;
    private String timestamp;

    public OrderRecord(String id, String item, int quantity, double total, String timestamp) {
        this.id = id;
        this.item = item;
        this.quantity = quantity;
        this.total = total;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getItem() { return item; }
    public int getQuantity() { return quantity; }
    public double getTotal() { return total; }
    public String getTimestamp() { return timestamp; }
    }
    
    public List<OrderRecord> getOrderHistory() {
    List<OrderRecord> orders = new ArrayList<>();

    File file = new File(ORDERS_FILE);
    if (!file.exists()) return orders;

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        String line;

        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");

            if (parts.length < 4) continue;

            String id = parts[0];
            String itemPart = parts[1];
            double total = Double.parseDouble(parts[2]);
            String timestamp = parts[3];

            // itemPart = "Milk:5"
            String[] itemSplit = itemPart.split(":");
            String item = itemSplit[0];
            int qty = Integer.parseInt(itemSplit[1]);

            orders.add(new OrderRecord(id, item, qty, total, timestamp));
        }

    } catch (Exception e) {
        panel.setStatusMessage("Error loading order history.");
    }

    return orders;
    }
}