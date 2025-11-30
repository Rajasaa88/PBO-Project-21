import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class LoginFrame extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;

    public LoginFrame() {
        setTitle("AutoAAR - Login");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        BackgroundPanel bgPanel = new BackgroundPanel("img/main.jpg");
        bgPanel.setLayout(new GridBagLayout());

        GlassPanel loginCard = new GlassPanel();
        loginCard.setLayout(new GridBagLayout());
        loginCard.setPreferredSize(new Dimension(400, 500));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 20, 10, 20);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("AutoAAR");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 40));
        lblTitle.setForeground(Color.WHITE); 
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel lblSubtitle = new JLabel("PREMIUM SHOWROOM");
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(200, 200, 200)); 
        lblSubtitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblSubtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        g.gridx = 0; g.gridy = 0; loginCard.add(lblTitle, g);
        g.gridy = 1; loginCard.add(lblSubtitle, g);

        g.gridy = 2; loginCard.add(createLabel("USERNAME"), g);
        txtUser = new JTextField(20);
        styleField(txtUser); 
        g.gridy = 3; loginCard.add(txtUser, g);

        g.gridy = 4; g.insets = new Insets(20, 20, 10, 20);
        loginCard.add(createLabel("PASSWORD"), g);
        txtPass = new JPasswordField(20);
        styleField(txtPass);
        g.gridy = 5; g.insets = new Insets(10, 20, 30, 20);
        loginCard.add(txtPass, g);
        
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { if(e.getKeyCode() == KeyEvent.VK_ENTER) login(); }
        };
        txtUser.addKeyListener(enterKey); txtPass.addKeyListener(enterKey);

        ModernButton btnLogin = new ModernButton("LOGIN", new Color(0, 120, 255));
        btnLogin.setPreferredSize(new Dimension(200, 50));
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnLogin.addActionListener(e -> login());
        
        g.gridy = 6; g.insets = new Insets(0, 20, 15, 20);
        loginCard.add(btnLogin, g);

        ModernButton btnRegister = new ModernButton("CREATE ACCOUNT", new Color(100, 100, 100));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setPreferredSize(new Dimension(200, 40));
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRegister.addActionListener(e -> new RegisterDialog(this));
        g.gridy = 7; loginCard.add(btnRegister, g);

        bgPanel.add(loginCard);
        setContentPane(bgPanel);
        setVisible(true);
    }

    private void login() {
        String user = txtUser.getText();
        String pass = new String(txtPass.getPassword());
        if (user.isEmpty() || pass.isEmpty()) { JOptionPane.showMessageDialog(this, "Empty fields!"); return; }
        try {
            Connection conn = KoneksiDB.configDB();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
            ps.setString(1, user); ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { new AppNavigationFrame(rs.getString("username")); dispose(); } 
            else { JOptionPane.showMessageDialog(this, "Invalid Login!"); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(200, 200, 200)); 
        return lbl;
    }

    private void styleField(JTextField field) {
        field.setBackground(new Color(255, 255, 255, 200)); 
        field.setOpaque(true); 
        field.setForeground(Color.BLACK); 
        field.setCaretColor(Color.BLACK); 
        field.setFont(new Font("Segoe UI", Font.BOLD, 16)); 
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(150, 150, 150)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    class BackgroundPanel extends JPanel {
        private Image bg;
        public BackgroundPanel(String path) { try { bg = new ImageIcon(path).getImage(); } catch(Exception e){} }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(bg!=null) { g.drawImage(bg, 0, 0, getWidth(), getHeight(), this); g.setColor(new Color(0, 0, 0, 100)); g.fillRect(0, 0, getWidth(), getHeight()); }
        }
    }

    class GlassPanel extends JPanel {
        public GlassPanel() { setOpaque(false); }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 150)); 
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            g2.setColor(new Color(255, 255, 255, 30));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 40, 40);
            g2.dispose();
        }
    }
}