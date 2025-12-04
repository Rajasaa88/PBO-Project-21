import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class LoginFrame extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private ModernButton btnLogin, btnRegister;

    public LoginFrame() {
        setTitle("AutoAAR - Login");
        
        // --- FULL SCREEN SETUP ---
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- ESC TO EXIT APP ---
        KeyStroke escKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKey, "EXIT_APP");
        getRootPane().getActionMap().put("EXIT_APP", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(LoginFrame.this, "Exit Application?", "Confirm Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        // 1. Background Panel
        BackgroundPanel bgPanel = new BackgroundPanel("img/main.jpg");
        bgPanel.setLayout(new GridBagLayout());

        // 2. Glass Login Card
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
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        };
        txtUser.addKeyListener(enterKey);
        txtPass.addKeyListener(enterKey);

        btnLogin = new ModernButton("LOGIN", new Color(0, 120, 255));
        btnLogin.setPreferredSize(new Dimension(200, 50));
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnLogin.addActionListener(e -> performLogin());
        
        g.gridy = 6; g.insets = new Insets(0, 20, 15, 20);
        loginCard.add(btnLogin, g);

        btnRegister = new ModernButton("CREATE ACCOUNT", new Color(100, 100, 100));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setPreferredSize(new Dimension(200, 40));
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRegister.addActionListener(e -> new RegisterDialog(this));
        
        g.gridy = 7;
        loginCard.add(btnRegister, g);

        bgPanel.add(loginCard);
        setContentPane(bgPanel);
        setVisible(true);
    }

    // --- LOGIC BARU: MENGGUNAKAN SWINGWORKER (MULTITHREADING) ---
    private void performLogin() {
        String user = txtUser.getText();
        String pass = new String(txtPass.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        // 1. Kunci UI agar user tahu sistem sedang bekerja
        btnLogin.setEnabled(false);
        btnLogin.setText("AUTHENTICATING...");
        btnRegister.setEnabled(false);
        txtUser.setEnabled(false);
        txtPass.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // 2. Jalankan Worker di Background Thread
        new LoginWorker(user, pass).execute();
    }

    // Inner Class untuk Multithreading Login
    class LoginWorker extends SwingWorker<String, Void> {
        private String username;
        private String password;
        private String errorMsg = null;

        public LoginWorker(String u, String p) {
            this.username = u; this.password = p;
        }

        @Override
        protected String doInBackground() throws Exception {
            // PROSES BERAT DI SINI (Koneksi Database)
            try {
                // Simulasi delay sedikit (opsional, biar efek loading terasa halus)
                Thread.sleep(500); 
                
                Connection conn = KoneksiDB.configDB();
                if (conn == null) {
                    errorMsg = "Database connection failed!";
                    return null;
                }

                PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return rs.getString("username"); // Return username jika sukses
                } else {
                    errorMsg = "Invalid Username or Password!";
                    return null;
                }
            } catch (Exception e) {
                errorMsg = "Error: " + e.getMessage();
                return null;
            }
        }

        @Override
        protected void done() {
            // DIJALANKAN KEMBALI DI UI THREAD (EDT) SETELAH PROSES SELESAI
            try {
                String resultUser = get(); // Ambil hasil dari doInBackground

                if (resultUser != null) {
                    // SUKSES
                    btnLogin.setText("SUCCESS!");
                    // Beri jeda sangat singkat sebelum pindah
                    Timer t = new Timer(200, evt -> {
                        new AppNavigationFrame(resultUser); // Buka Aplikasi Utama
                        dispose(); // Tutup Login
                    });
                    t.setRepeats(false);
                    t.start();
                } else {
                    // GAGAL
                    JOptionPane.showMessageDialog(LoginFrame.this, errorMsg, "Login Failed", JOptionPane.ERROR_MESSAGE);
                    resetUI();
                }
            } catch (Exception e) {
                e.printStackTrace();
                resetUI();
            }
        }
    }

    private void resetUI() {
        btnLogin.setEnabled(true);
        btnLogin.setText("LOGIN");
        btnRegister.setEnabled(true);
        txtUser.setEnabled(true);
        txtPass.setEnabled(true);
        setCursor(Cursor.getDefaultCursor());
        txtPass.setText("");
        txtPass.requestFocus();
    }

    // --- HELPER METHODS (SAMA SEPERTI SEBELUMNYA) ---

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(180, 180, 180)); 
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

        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 150, 255)), 
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(150, 150, 150)),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
        });
    }

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
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            
            g2.setColor(new Color(255, 255, 255, 30));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 40, 40);
            
            g2.dispose();
        }
    }
}