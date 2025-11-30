import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RegisterDialog extends JDialog {
    private JTextField txtUser;
    private JPasswordField txtPass, txtPassConf;

    public RegisterDialog(JFrame parent) {
        super(parent, "Register", true);
        // UKURAN DIPERKECIL (Lebih ramping)
        setSize(400, 580);
        setLocationRelativeTo(parent);
        
        BackgroundPanel bgPanel = new BackgroundPanel("img/main.jpg");
        bgPanel.setLayout(new GridBagLayout());

        GlassPanel card = new GlassPanel();
        card.setLayout(new GridBagLayout());
        // Ukuran Card disesuaikan agar pas di window kecil
        card.setPreferredSize(new Dimension(340, 480));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 15, 5, 15); // Margin kiri-kanan diperkecil
        g.fill = GridBagConstraints.HORIZONTAL;

        // --- HEADER ---
        // TEKS DIUBAH: AUTOAAR -> AutoAAR
        JLabel lblTitle = new JLabel("JOIN AutoAAR"); 
        lblTitle.setFont(new Font("Serif", Font.BOLD, 28)); // Font sedikit diperkecil
        lblTitle.setForeground(Color.WHITE); 
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        g.gridx = 0; g.gridy = 0; card.add(lblTitle, g);

        JLabel lblSubtitle = new JLabel("CREATE YOUR ACCOUNT");
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblSubtitle.setForeground(new Color(200, 200, 200));
        lblSubtitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblSubtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        g.gridy = 1; card.add(lblSubtitle, g);

        // --- FORM INPUTS ---
        
        // 1. Username
        g.gridy = 2; card.add(createLabel("USERNAME"), g);
        txtUser = new JTextField(15);
        styleField(txtUser);
        g.gridy = 3; card.add(txtUser, g);

        // 2. Password
        g.gridy = 4; g.insets = new Insets(10, 15, 5, 15); // Jarak antar field dirapatkan
        card.add(createLabel("PASSWORD"), g);
        txtPass = new JPasswordField(15);
        styleField(txtPass);
        g.gridy = 5; g.insets = new Insets(5, 15, 5, 15);
        card.add(txtPass, g);

        // 3. Confirm Password
        g.gridy = 6; g.insets = new Insets(10, 15, 5, 15);
        card.add(createLabel("CONFIRM PASSWORD"), g);
        txtPassConf = new JPasswordField(15);
        styleField(txtPassConf);
        g.gridy = 7; g.insets = new Insets(5, 15, 15, 15);
        card.add(txtPassConf, g);

        // --- TOMBOL ---
        
        // Register Button
        ModernButton btnReg = new ModernButton("REGISTER NOW", new Color(0, 180, 100));
        btnReg.setPreferredSize(new Dimension(180, 40)); // Ukuran tombol disesuaikan
        btnReg.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnReg.addActionListener(e -> register());
        
        g.gridy = 8;
        card.add(btnReg, g);

        // Cancel Button
        ModernButton btnCancel = new ModernButton("CANCEL", new Color(200, 50, 50));
        btnCancel.setPreferredSize(new Dimension(180, 35));
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCancel.addActionListener(e -> dispose());
        
        g.gridy = 9; g.insets = new Insets(10, 15, 15, 15);
        card.add(btnCancel, g);

        bgPanel.add(card);
        setContentPane(bgPanel);
        setVisible(true);
    }

    private void register() {
        String u = txtUser.getText();
        String p = new String(txtPass.getPassword());
        String pc = new String(txtPassConf.getPassword());

        if(u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        if(!p.equals(pc)) { 
            JOptionPane.showMessageDialog(this, "Passwords do not match!"); 
            return; 
        }

        try {
            Connection conn = KoneksiDB.configDB();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=?");
            ps.setString(1, u);
            if(ps.executeQuery().next()) {
                JOptionPane.showMessageDialog(this, "Username already taken!");
            } else {
                conn.createStatement().executeUpdate("INSERT INTO users VALUES ('"+u+"', '"+p+"', 'customer')");
                JOptionPane.showMessageDialog(this, "Registration Success! Please Login.");
                dispose();
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    // --- HELPER METHODS ---

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Font label diperkecil sedikit
        lbl.setForeground(new Color(200, 200, 200)); 
        return lbl;
    }

    private void styleField(JTextField field) {
        field.setBackground(new Color(255, 255, 255, 200)); 
        field.setOpaque(true); 
        
        field.setForeground(Color.BLACK); 
        field.setCaretColor(Color.BLACK); 
        field.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(150, 150, 150)),
            BorderFactory.createEmptyBorder(3, 5, 3, 5) // Padding diperkecil
        ));

        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 150, 255)), 
                    BorderFactory.createEmptyBorder(3, 5, 3, 5)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(150, 150, 150)),
                    BorderFactory.createEmptyBorder(3, 5, 3, 5)
                ));
            }
        });
    }

    // --- INNER CLASSES ---

    class BackgroundPanel extends JPanel {
        private Image bg;
        public BackgroundPanel(String path) {
            try { bg = new ImageIcon(path).getImage(); } catch(Exception e){}
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(bg!=null) {
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                g.setColor(new Color(0, 0, 0, 100)); 
                g.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    class GlassPanel extends JPanel {
        public GlassPanel() { setOpaque(false); } 
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(new Color(0, 0, 0, 150)); 
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30); // Radius sudut diperkecil dikit
            
            g2.setColor(new Color(255, 255, 255, 30));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
            
            g2.dispose();
        }
    }
}