import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class LoginFrame extends JFrame {
    // Komponen input dideklarasikan di sini agar bisa diakses method login()
    private JTextField txtUser = new JTextField(15);
    private JPasswordField txtPass = new JPasswordField(15);

    public LoginFrame() {
        setTitle("AutoAAR Login");
        setSize(500, 400); // Ukuran diperbesar sedikit agar background terlihat
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Buat Panel Background Custom (Mewarisi JPanel & Override paintComponent)
        // Pastikan Anda punya gambar 'login_bg.jpg' di folder img, atau ganti namanya
        BackgroundPanel bgPanel = new BackgroundPanel("img/main.jpg"); 
        bgPanel.setLayout(new GridBagLayout()); // Gunakan GridBag untuk menengahkan panel login

        // 2. Buat Panel Login (Panel Depan)
        JPanel panelLogin = new JPanel();
        panelLogin.setLayout(new GridBagLayout());
        // Warna Putih dengan Alpha 200 (Semi-Transparan: 0-255)
        panelLogin.setBackground(new Color(255, 255, 255, 220)); 
        panelLogin.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20) // Padding dalam
        ));

        // Menyusun komponen ke dalam Panel Login
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.anchor = GridBagConstraints.WEST;

        // Judul Form
        JLabel lblTitle = new JLabel("USER LOGIN");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setForeground(new Color(0, 50, 100));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; 
        g.anchor = GridBagConstraints.CENTER;
        panelLogin.add(lblTitle, g);

        // Reset width & anchor untuk input
        g.gridwidth = 1; g.anchor = GridBagConstraints.WEST;

        // Input User
        g.gridx = 0; g.gridy = 1; panelLogin.add(new JLabel("Username:"), g);
        g.gridx = 1; panelLogin.add(txtUser, g);

        // Input Pass
        g.gridx = 0; g.gridy = 2; panelLogin.add(new JLabel("Password:"), g);
        g.gridx = 1; panelLogin.add(txtPass, g);

        // Tombol
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        btnPanel.setOpaque(false); // Agar warna panelLogin tembus
        
        JButton bLog = new JButton("LOGIN");
        bLog.setBackground(new Color(0, 100, 200));
        bLog.setForeground(Color.WHITE);
        bLog.addActionListener(e -> login());

        JButton bReg = new JButton("REGISTER");
        bReg.setBackground(new Color(50, 50, 50));
        bReg.setForeground(Color.WHITE);
        bReg.addActionListener(e -> new RegisterDialog(this));

        btnPanel.add(bLog);
        btnPanel.add(bReg);

        g.gridx = 0; g.gridy = 3; g.gridwidth = 2; g.anchor = GridBagConstraints.CENTER;
        g.insets = new Insets(15, 5, 5, 5); // Jarak lebih besar ke tombol
        panelLogin.add(btnPanel, g);

        // 3. Masukkan Panel Login ke dalam Panel Background
        bgPanel.add(panelLogin);

        // 4. Set ContentPane JFrame menjadi bgPanel
        setContentPane(bgPanel);
        setVisible(true);
    }

    private void login() {
        try {
            Connection conn = KoneksiDB.configDB();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
            ps.setString(1, txtUser.getText());
            ps.setString(2, new String(txtPass.getPassword()));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                new AppNavigationFrame(rs.getString("username")); // Pindah ke menu utama
                dispose(); // Tutup login
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    // --- INNER CLASS UNTUK BACKGROUND ---
    class BackgroundPanel extends JPanel {
        private Image bgImage;

        public BackgroundPanel(String imagePath) {
            try {
                bgImage = new ImageIcon(imagePath).getImage();
            } catch (Exception e) {
                System.err.println("Gambar tidak ditemukan: " + imagePath);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) {
                // Gambar akan di-stretch memenuhi panel
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                // Warna cadangan jika gambar gagal load
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }
}