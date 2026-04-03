import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private User currentUser;
    private ItemManagement itemManagement;
    private JTabbedPane tabbedPane;

    public MainFrame(User user) {
        this.currentUser = user;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("FOCUS - Fully Operational Control Unified System (" + currentUser.getRole() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel headerPanel = createHeaderPanel();
        tabbedPane = createTabbedPane();

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setPreferredSize(new Dimension(1200, 100));
        headerPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("FOCUS - Business Management System", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(25, 0, 25, 0));

        JLabel userLabel = new JLabel("Logged in as: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        userLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(userLabel, BorderLayout.WEST);
        return headerPanel;
    }

    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Common tabs
        tabbedPane.addTab("Home", createHomePanel());
        tabbedPane.addTab("Task Alerts", new TaskAlertsPanel(currentUser));
        tabbedPane.addTab("Order Application", createOrderPanel());

        // Manager-only tabs
        if (currentUser.getRole().equals("Manager")) {
            tabbedPane.addTab("Task Management", new TaskPanel());
            tabbedPane.addTab("Reporting", new ReportingPanel());
            tabbedPane.addTab("Item Management", createItemPanel());
        } else {
            addDisabledTab(tabbedPane, "Task Management", "Managers only");
            addDisabledTab(tabbedPane, "Reporting", "Managers only");
            addDisabledTab(tabbedPane, "Item Management", "Managers only");
        }
        return tabbedPane;
    }

    private JPanel createHomePanel() {
        JPanel homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(Color.WHITE);
        JLabel welcomeLabel = new JLabel("Welcome to FOCUS", JLabel.CENTER);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(70, 130, 180));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
        homePanel.add(welcomeLabel, BorderLayout.NORTH);
        return homePanel;
    }

    private JPanel createOrderPanel() {
    OrderPanel orderPanel = new OrderPanel();
    OrderManagement orderManagement = new OrderManagement(orderPanel);

    if (itemManagement != null) {
        orderManagement.setItemManager(itemManagement);
    }
    
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(orderPanel, BorderLayout.CENTER);
    return panel;
}

    private JPanel createItemPanel() {
        ItemPanel itemPanel = new ItemPanel();
        itemManagement = new ItemManagement(itemPanel); // controller attaches itself
        return itemPanel;
    }



    private void addDisabledTab(JTabbedPane tabbedPane, String title, String tooltip) {
        JPanel disabledPanel = new JPanel();
        disabledPanel.setBackground(Color.LIGHT_GRAY);
        JLabel label = new JLabel(title + " - Access Restricted", JLabel.CENTER);
        label.setForeground(Color.RED);
        disabledPanel.add(label);
        tabbedPane.addTab(title, disabledPanel);
        int idx = tabbedPane.indexOfTab(title);
        tabbedPane.setEnabledAt(idx, false);
        tabbedPane.setToolTipTextAt(idx, tooltip);
    }

    public static void main(String[] args) {
        Security.main(args);
    }
}