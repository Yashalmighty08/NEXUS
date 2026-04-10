
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;

public class Security {

    // ── Palette ───────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(0x1E1E1E);
    private static final Color CARD_BG      = new Color(0x2A2A2A);
    private static final Color DIVIDER      = new Color(0x333333);
    private static final Color ACCENT       = new Color(0x00C896);
    private static final Color ACCENT_DARK  = new Color(0x00A67A);
    private static final Color TEXT_PRIMARY = new Color(0xF0F0F0);
    private static final Color TEXT_MUTED   = new Color(0x888888);
    private static final Color FIELD_BG     = new Color(0x333333);
    private static final Color ERROR_RED    = new Color(0xFF6B6B);

    private static final String USER_DATA_FILE = "users.txt";
    private static HashMap<String, User> users = new HashMap<>();

    public static void main(String[] args) {
        loadUsers();
        SwingUtilities.invokeLater(Security::showAuthScreen);
    }

    // ══════════════════════════════════════════════════════════
    //  MAIN AUTH SCREEN
    // ══════════════════════════════════════════════════════════
    private static void showAuthScreen() {
        JFrame frame = new JFrame("FOCUS — Authentication");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(480, 620);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.getContentPane().setBackground(BG_DARK);
        frame.setLayout(new GridBagLayout());

        // Outer card
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(
            new LineBorder(DIVIDER, 1, true),
            new EmptyBorder(40, 44, 40, 44)
        ));

        // Logo
        JLabel logo = new JLabel("FOCUS");
        logo.setFont(new Font("SansSerif", Font.BOLD, 30));
        logo.setForeground(ACCENT);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Business Management System");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tab bar
        JPanel tabBar = new JPanel(new GridLayout(1, 2, 0, 0));
        tabBar.setOpaque(false);
        tabBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        tabBar.setBorder(new MatteBorder(0, 0, 2, 0, DIVIDER));

        JLabel signInTab = makeTab("Sign In");
        JLabel signUpTab = makeTab("Sign Up");
        tabBar.add(signInTab);
        tabBar.add(signUpTab);

        // Form stack
        JPanel formStack = new JPanel(new CardLayout());
        formStack.setOpaque(false);

        JLabel signInError = errorLabel();
        JLabel signUpError = errorLabel();

        JPanel signInForm = buildSignInForm(frame, signInError);
        JPanel signUpForm = buildSignUpForm(frame, signUpError);

        formStack.add(signInForm, "signin");
        formStack.add(signUpForm, "signup");

        // Default active tab
        setActiveTab(signInTab, true);
        setActiveTab(signUpTab, false);

        signInTab.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                ((CardLayout) formStack.getLayout()).show(formStack, "signin");
                setActiveTab(signInTab, true);
                setActiveTab(signUpTab, false);
            }
        });
        signUpTab.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                ((CardLayout) formStack.getLayout()).show(formStack, "signup");
                setActiveTab(signInTab, false);
                setActiveTab(signUpTab, true);
            }
        });

        card.add(logo);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(28));
        card.add(tabBar);
        card.add(Box.createVerticalStrut(28));
        card.add(formStack);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(40, 40, 40, 40);
        frame.add(card, gbc);
        frame.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════
    //  SIGN IN FORM
    // ══════════════════════════════════════════════════════════
    private static JPanel buildSignInForm(JFrame frame, JLabel errorLabel) {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JTextField usernameField  = styledField();
        JPasswordField passField  = styledPasswordField();

        JButton signInBtn = accentButton("Sign In");
        signInBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passField.getPassword());
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please fill in all fields.");
                return;
            }
            if (authenticateUser(username, password)) {
                frame.dispose();
                launchMainApplication(users.get(username));
            } else {
                errorLabel.setText("Invalid username or password.");
                passField.setText("");
            }
        });

        form.add(fieldLabel("Username"));
        form.add(Box.createVerticalStrut(7));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(18));
        form.add(fieldLabel("Password"));
        form.add(Box.createVerticalStrut(7));
        form.add(passField);
        form.add(Box.createVerticalStrut(10));
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(22));
        form.add(signInBtn);

        return form;
    }

    // ══════════════════════════════════════════════════════════
    //  SIGN UP FORM
    // ══════════════════════════════════════════════════════════
    private static JPanel buildSignUpForm(JFrame frame, JLabel errorLabel) {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JTextField usernameField = styledField();
        JPasswordField passField = styledPasswordField();

        // Role toggles — use full-width panel with proper sizing
        JToggleButton empBtn = roleToggle("Employee");
        JToggleButton mgrBtn = roleToggle("Manager");
        empBtn.setSelected(true);
        styleRoleToggle(empBtn, true);
        styleRoleToggle(mgrBtn, false);

        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(empBtn);
        roleGroup.add(mgrBtn);

        empBtn.addActionListener(e -> { styleRoleToggle(empBtn, true);  styleRoleToggle(mgrBtn, false); });
        mgrBtn.addActionListener(e -> { styleRoleToggle(empBtn, false); styleRoleToggle(mgrBtn, true);  });

        // Role row — use JPanel with equal columns and fixed height
        JPanel roleRow = new JPanel(new GridLayout(1, 2, 10, 0));
        roleRow.setOpaque(false);
        roleRow.add(empBtn);
        roleRow.add(mgrBtn);
        roleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        roleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton signUpBtn = accentButton("Create Account");
        signUpBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passField.getPassword());
            String role     = empBtn.isSelected() ? "Employee" : "Manager";

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please fill in all fields.");
                return;
            }
            if (password.length() < 4) {
                errorLabel.setText("Password must be at least 4 characters.");
                return;
            }
            if (users.containsKey(username)) {
                errorLabel.setText("Username already exists.");
                return;
            }
            users.put(username, new User(username, password, role));
            saveUsers();
            errorLabel.setForeground(ACCENT);
            errorLabel.setText("Account created! You can now sign in.");
            usernameField.setText("");
            passField.setText("");
        });

        form.add(fieldLabel("Username"));
        form.add(Box.createVerticalStrut(7));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(18));
        form.add(fieldLabel("Password"));
        form.add(Box.createVerticalStrut(7));
        form.add(passField);
        form.add(Box.createVerticalStrut(18));
        form.add(fieldLabel("Role"));
        form.add(Box.createVerticalStrut(7));
        form.add(roleRow);
        form.add(Box.createVerticalStrut(10));
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(18));
        form.add(signUpBtn);

        return form;
    }

    // ══════════════════════════════════════════════════════════
    //  UI COMPONENT HELPERS
    // ══════════════════════════════════════════════════════════
    private static JLabel makeTab(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setForeground(TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(8, 0, 12, 0));
        lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return lbl;
    }

    private static void setActiveTab(JLabel tab, boolean active) {
        tab.setForeground(active ? ACCENT : TEXT_MUTED);
        tab.setBorder(active
            ? new MatteBorder(0, 0, 2, 0, ACCENT)
            : new EmptyBorder(0, 0, 2, 0));
    }

    private static JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private static JTextField styledField() {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(new EmptyBorder(10, 14, 10, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private static JPasswordField styledPasswordField() {
        JPasswordField field = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(new EmptyBorder(10, 14, 10, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private static JToggleButton roleToggle(String text) {
        JToggleButton btn = new JToggleButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected() ? ACCENT : FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setPreferredSize(new Dimension(0, 44));
        btn.setMinimumSize(new Dimension(80, 44));
        return btn;
    }

    private static void styleRoleToggle(JToggleButton btn, boolean active) {
        btn.setForeground(active ? new Color(0x1E1E1E) : TEXT_MUTED);
        btn.repaint();
    }

    private static JButton accentButton(String text) {
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
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }

    private static JLabel errorLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(ERROR_RED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    // ══════════════════════════════════════════════════════════
    //  DATA METHODS
    // ══════════════════════════════════════════════════════════
    private static void loadUsers() {
        try {
            File file = new File(USER_DATA_FILE);
            if (!file.exists()) {
                users.put("admin", new User("admin", "admin123", "Manager"));
                saveUsers();
                return;
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 3)
                    users.put(parts[0], new User(parts[0], parts[1], parts[2]));
            }
            reader.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading user data: " + e.getMessage());
        }
    }

    private static void saveUsers() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(USER_DATA_FILE));
            for (User user : users.values())
                writer.println(user.getUsername() + " " + user.getPassword() + " " + user.getRole());
            writer.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving user data: " + e.getMessage());
        }
    }

    private static boolean authenticateUser(String username, String password) {
        User user = users.get(username);
        return user != null && user.getPassword().equals(password);
    }

    private static void launchMainApplication(User user) {
        SwingUtilities.invokeLater(() -> new MainFrame(user).setVisible(true));
    }
}