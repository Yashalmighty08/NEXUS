
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {

    // ── Palette ──────────────────────────────────────────────
    private static final Color BG_DARK       = new Color(0x1E1E1E);
    private static final Color SIDEBAR_BG    = new Color(0x252525);
    private static final Color SIDEBAR_HOVER = new Color(0x2F2F2F);
    private static final Color SIDEBAR_ACTIVE= new Color(0x003D2E);
    private static final Color ACCENT        = new Color(0x00C896);
    private static final Color ACCENT_DARK   = new Color(0x00A67A);
    private static final Color TEXT_PRIMARY  = new Color(0xF0F0F0);
    private static final Color TEXT_MUTED    = new Color(0x888888);
    private static final Color CARD_BG       = new Color(0x2A2A2A);
    private static final Color DIVIDER       = new Color(0x333333);

    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD, 13);
    private static final Font FONT_BODY    = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 11);
    private static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 22);

    // ── State ─────────────────────────────────────────────────
    private User currentUser;
    private ItemManagement itemManagement;
    private JPanel contentArea;
    private JPanel activeNavBtn = null;

    public MainFrame(User user) {
        this.currentUser = user;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("FOCUS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(BG_DARK);
        add(contentArea, BorderLayout.CENTER);

        showHome();

        ImageIcon icon = new ImageIcon("icon.png");
        setIconImage(icon.getImage());
    }

    // ══════════════════════════════════════════════════════════
    //  SIDEBAR
    // ══════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(SIDEBAR_BG);

        // Logo
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(new EmptyBorder(28, 20, 20, 20));

        JLabel logo = new JLabel("FOCUS");
        logo.setFont(new Font("SansSerif", Font.BOLD, 22));
        logo.setForeground(ACCENT);

        JLabel tagline = new JLabel("Business Management");
        tagline.setFont(FONT_SMALL);
        tagline.setForeground(TEXT_MUTED);

        logoPanel.add(logo);
        logoPanel.add(Box.createVerticalStrut(3));
        logoPanel.add(tagline);

        JSeparator sep1 = new JSeparator();
        sep1.setForeground(DIVIDER);
        sep1.setBackground(DIVIDER);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(logoPanel, BorderLayout.CENTER);
        topSection.add(sep1, BorderLayout.SOUTH);

        // Nav
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(12, 0, 0, 0));

        nav.add(navSectionLabel("GENERAL"));
        nav.add(navItem("  Home",          true,  () -> showHome()));
        nav.add(navItem("  Task Alerts",   true,  () -> showPanel(new TaskAlertsPanel(currentUser))));
        nav.add(navItem("  Orders",        true,  () -> showPanel(buildOrderPanel())));

        nav.add(Box.createVerticalStrut(8));
        nav.add(navSectionLabel("MANAGER"));

        boolean mgr = currentUser.getRole().equals("Manager");
        nav.add(navItem("  Tasks",         mgr,   () -> showPanel(new TaskPanel())));
        nav.add(navItem("  Reporting",     mgr,   () -> showPanel(new ReportPanel())));
        nav.add(navItem("  Inventory",     mgr,   () -> showPanel(buildItemPanel())));

        // Bottom user + logout
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        JSeparator sep2 = new JSeparator();
        sep2.setForeground(DIVIDER);
        sep2.setBackground(DIVIDER);
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JPanel userCard = new JPanel(new BorderLayout(10, 0));
        userCard.setOpaque(false);
        userCard.setBorder(new EmptyBorder(14, 16, 8, 16));

        JLabel avatar = new JLabel(String.valueOf(currentUser.getUsername().charAt(0)).toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setFont(new Font("SansSerif", Font.BOLD, 15));
        avatar.setForeground(new Color(0x1E1E1E));
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setOpaque(false);

        JPanel userInfo = new JPanel();
        userInfo.setOpaque(false);
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        JLabel uName = new JLabel(currentUser.getUsername());
        uName.setFont(FONT_TITLE);
        uName.setForeground(TEXT_PRIMARY);
        JLabel uRole = new JLabel(currentUser.getRole());
        uRole.setFont(FONT_SMALL);
        uRole.setForeground(ACCENT);
        userInfo.add(uName);
        userInfo.add(uRole);

        userCard.add(avatar, BorderLayout.WEST);
        userCard.add(userInfo, BorderLayout.CENTER);

        JPanel logoutWrapper = new JPanel(new BorderLayout());
        logoutWrapper.setOpaque(false);
        logoutWrapper.setBorder(new EmptyBorder(0, 12, 18, 12));
        JButton logoutBtn = buildButton("Logout", false);
        logoutBtn.addActionListener(e -> logout());
        logoutWrapper.add(logoutBtn, BorderLayout.CENTER);

        bottomPanel.add(sep2);
        bottomPanel.add(userCard);
        bottomPanel.add(logoutWrapper);

        sidebar.add(topSection, BorderLayout.NORTH);
        sidebar.add(nav, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);
        return sidebar;
    }

    private JLabel navSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(10, 20, 4, 20));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel navItem(String label, boolean enabled, Runnable action) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(true);
        item.setBackground(SIDEBAR_BG);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        item.setAlignmentX(LEFT_ALIGNMENT);
        item.setCursor(enabled ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());

        JPanel indicator = new JPanel();
        indicator.setPreferredSize(new Dimension(3, 0));
        indicator.setBackground(SIDEBAR_BG);

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(enabled ? TEXT_PRIMARY : TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(10, 12, 10, 8));

        item.add(indicator, BorderLayout.WEST);
        item.add(lbl, BorderLayout.CENTER);

        if (enabled) {
            item.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    if (item != activeNavBtn) item.setBackground(SIDEBAR_HOVER);
                }
                @Override public void mouseExited(MouseEvent e) {
                    if (item != activeNavBtn) item.setBackground(SIDEBAR_BG);
                }
                @Override public void mouseClicked(MouseEvent e) {
                    setActiveNav(item, indicator);
                    action.run();
                }
            });
        }
        return item;
    }

    private void setActiveNav(JPanel item, JPanel indicator) {
        if (activeNavBtn != null) {
            activeNavBtn.setBackground(SIDEBAR_BG);
            for (Component c : activeNavBtn.getComponents()) {
                if (c instanceof JPanel && c.getPreferredSize().width == 3) {
                    c.setBackground(SIDEBAR_BG);
                }
            }
        }
        activeNavBtn = item;
        item.setBackground(SIDEBAR_ACTIVE);
        indicator.setBackground(ACCENT);
    }

    // ══════════════════════════════════════════════════════════
    //  CONTENT SWITCHING
    // ══════════════════════════════════════════════════════════
    private void showHome() {
        contentArea.removeAll();
        contentArea.add(buildHomePanel(), BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    private void showPanel(JComponent panel) {
        contentArea.removeAll();
        panel.setBackground(BG_DARK);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_DARK);
        wrapper.setBorder(new EmptyBorder(24, 28, 24, 28));
        wrapper.add(panel, BorderLayout.CENTER);
        contentArea.add(wrapper, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ══════════════════════════════════════════════════════════
    //  HOME PANEL
    // ══════════════════════════════════════════════════════════
    private JPanel buildHomePanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(36, 36, 36, 36));

        // Greeting
        JPanel greetingPanel = new JPanel();
        greetingPanel.setOpaque(false);
        greetingPanel.setLayout(new BoxLayout(greetingPanel, BoxLayout.Y_AXIS));

        JLabel greeting = new JLabel("Good day, " + currentUser.getUsername());
        greeting.setFont(FONT_HEADING);
        greeting.setForeground(TEXT_PRIMARY);

        JLabel sub = new JLabel("Here's an overview of your workspace.");
        sub.setFont(FONT_BODY);
        sub.setForeground(TEXT_MUTED);

        greetingPanel.add(greeting);
        greetingPanel.add(Box.createVerticalStrut(4));
        greetingPanel.add(sub);

        // Stat cards
        JPanel cardsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        cardsRow.setOpaque(false);
        cardsRow.setBorder(new EmptyBorder(28, 0, 28, 0));
        cardsRow.add(statCard("Total Tasks",     getTaskCount(),     ACCENT));
        cardsRow.add(statCard("Orders Today",    getOrderCount(),    new Color(0x3B9EFF)));
        cardsRow.add(statCard("Low Stock Items", getLowStockCount(), new Color(0xFF6B6B)));

        // Quick actions
        JLabel qaLabel = new JLabel("Quick Actions");
        qaLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        qaLabel.setForeground(TEXT_PRIMARY);
        qaLabel.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel qaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        qaRow.setOpaque(false);

        JButton qaOrder = accentButton("New Order");
        qaOrder.addActionListener(e -> showPanel(buildOrderPanel()));
        qaRow.add(qaOrder);

        if (currentUser.getRole().equals("Manager")) {
            JButton qaTask   = accentButton("Manage Tasks");
            JButton qaReport = accentButton("Run Report");
            qaTask.addActionListener(e   -> showPanel(new TaskPanel()));
            qaReport.addActionListener(e -> showPanel(new ReportPanel()));
            qaRow.add(qaTask);
            qaRow.add(qaReport);
        }

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(greetingPanel);
        center.add(cardsRow);
        center.add(qaLabel);
        center.add(qaRow);

        root.add(center, BorderLayout.NORTH);
        return root;
    }

    private JPanel statCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
            new LineBorder(DIVIDER, 1, true),
            new EmptyBorder(22, 22, 22, 22)
        ));

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("SansSerif", Font.BOLD, 34));
        valueLbl.setForeground(accentColor);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_SMALL);
        titleLbl.setForeground(TEXT_MUTED);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(valueLbl);
        text.add(Box.createVerticalStrut(4));
        text.add(titleLbl);

        card.add(text, BorderLayout.CENTER);
        return card;
    }

    // ── Stat readers ──────────────────────────────────────────
    private String getTaskCount() {
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("tasks.txt"))) {
            return String.valueOf(br.lines().filter(l -> !l.startsWith("Employee") && !l.isBlank()).count());
        } catch (Exception e) { return "—"; }
    }

    private String getOrderCount() {
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("orders.txt"))) {
            String today = java.time.LocalDate.now().toString();
            return String.valueOf(br.lines().filter(l -> l.contains(today)).count());
        } catch (Exception e) { return "—"; }
    }

    private String getLowStockCount() {
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("items.txt"))) {
            return String.valueOf(br.lines()
                .filter(l -> !l.startsWith("Item") && !l.isBlank())
                .filter(l -> {
                    String[] p = l.split(",");
                    if (p.length < 4) return false;
                    try { return Integer.parseInt(p[2].trim()) < Integer.parseInt(p[3].trim()); }
                    catch (Exception ex) { return false; }
                }).count());
        } catch (Exception e) { return "—"; }
    }

    // ── Panel builders ────────────────────────────────────────
    private JPanel buildOrderPanel() {
        OrderPanel orderPanel = new OrderPanel();
        OrderManagement orderManagement = new OrderManagement(orderPanel);
        if (itemManagement != null) orderManagement.setItemManager(itemManagement);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(orderPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildItemPanel() {
        ItemPanel itemPanel = new ItemPanel();
        itemManagement = new ItemManagement(itemPanel);
        return itemPanel;
    }

    // ── Button helpers ────────────────────────────────────────
    private JButton accentButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? ACCENT_DARK : getModel().isRollover() ? ACCENT_DARK : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(new Color(0x1E1E1E));
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 38));
        return btn;
    }

    private JButton buildButton(String text, boolean filled) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(filled ? (getModel().isPressed() ? ACCENT_DARK : ACCENT) : new Color(0x3A3A3A));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(filled ? new Color(0x1E1E1E) : TEXT_PRIMARY);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return btn;
    }

    // ── Logout ────────────────────────────────────────────────
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            Security.main(new String[]{});
        }
    }

    public static void main(String[] args) {
        Security.main(args);
    }
}