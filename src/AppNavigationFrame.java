import java.awt.*;
import java.io.File;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;

public class AppNavigationFrame extends JFrame {
    private CardLayout cards;
    private JPanel mainPanel, modelContainer;
    private String currentUser, selectedBrand, selectedTier;

    // Footer dihapus karena monitor pindah ke menu sendiri

    public AppNavigationFrame(String user) {
        this.currentUser = user;
        setTitle("AutoAAR - User: " + user);
        setSize(1000, 850); 
        setDefaultCloseOperation(EXIT_ON_CLOSE); 
        setLocationRelativeTo(null);

        // --- 1. HEADER ATAS (MENU BARU) ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(20, 20, 20)); 
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Info User (Kiri)
        JLabel lblUser = new JLabel("Logged in as: " + user.toUpperCase());
        lblUser.setForeground(Color.LIGHT_GRAY);
        lblUser.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Panel Tombol Kanan (Monitor & Logout)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        // A. TOMBOL LIVE MONITOR (BARU)
        JButton btnMonitor = new JButton("LIVE MONITOR");
        btnMonitor.setBackground(new Color(0, 100, 200)); // Biru
        btnMonitor.setForeground(Color.WHITE);
        btnMonitor.setFocusPainted(false);
        btnMonitor.setFont(new Font("Arial", Font.BOLD, 12));
        btnMonitor.addActionListener(e -> new MonitorDialog(this)); // Buka Jendela Monitor

        // B. TOMBOL LOGOUT
        JButton btnLogout = new JButton("LOGOUT");
        btnLogout.setBackground(new Color(200, 50, 50)); // Merah
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setFont(new Font("Arial", Font.BOLD, 12));
        btnLogout.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION) == 0) {
                new LoginFrame(); dispose();
            }
        });

        btnPanel.add(btnMonitor);
        btnPanel.add(btnLogout);

        header.add(lblUser, BorderLayout.WEST);
        header.add(btnPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // --- 2. MAIN CONTENT ---
        cards = new CardLayout();
        mainPanel = new JPanel(cards); 
        mainPanel.setBackground(Color.BLACK); 
        
        initPages(); 
        add(mainPanel, BorderLayout.CENTER);

        // Footer dihapus sesuai request
        
        cards.show(mainPanel, "BRAND");
        setVisible(true);
    }

    private void initPages() {
        // --- PAGE 1: BRAND SELECTION ---
        JPanel pBrand = new JPanel(new BorderLayout()); 
        pBrand.setBackground(Color.BLACK); 
        
        ImagePanel welcomePanel = new ImagePanel("img/main.jpg"); 
        welcomePanel.setLayout(new GridBagLayout()); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10, 10, 10, 10); 
        gbc.anchor = GridBagConstraints.CENTER; 

        JLabel lblTitle = new JLabel("Welcome to AutoAAR");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 48)); 
        lblTitle.setForeground(Color.WHITE); 
        
        JLabel lblDesc = new JLabel("<html><center>" +
                "Showroom mobil sport dan hypercar terlengkap dan terpercaya.<br>" +
                "Temukan kendaraan impian Anda dengan kualitas terbaik dan harga kompetitif.<br>" +
                "<br><i>Speed, Luxury, and Prestige.</i>" +
                "</center></html>", SwingConstants.CENTER); 
        lblDesc.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lblDesc.setForeground(Color.WHITE); 

        JLabel lblSelect = new JLabel("SELECT MANUFACTURER");
        lblSelect.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblSelect.setForeground(Color.WHITE);

        welcomePanel.add(lblTitle, gbc);
        gbc.gridy = 1; welcomePanel.add(lblDesc, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(40, 10, 10, 10); 
        welcomePanel.add(lblSelect, gbc);

        pBrand.add(welcomePanel, BorderLayout.NORTH);

        JPanel gridBrand = new JPanel(new GridLayout(0, 3, 40, 40)); 
        gridBrand.setBackground(Color.BLACK);
        gridBrand.setBorder(BorderFactory.createEmptyBorder(30, 50, 50, 50));
        
        try {
            Connection conn = KoneksiDB.configDB();
            if(conn!=null) {
                ResultSet rs = conn.createStatement().executeQuery("SELECT DISTINCT brand FROM cars ORDER BY brand");
                while(rs.next()) {
                    String b = rs.getString("brand");
                    JButton btn = createBtn(b, "img/"+b+".png", 180, 180); 
                    btn.addActionListener(e -> { selectedBrand=b; cards.show(mainPanel, "TIER"); });
                    gridBrand.add(btn);
                }
            }
        } catch(Exception e) {}
        
        JScrollPane scrollPane = new JScrollPane(gridBrand);
        scrollPane.setBorder(null); 
        scrollPane.getViewport().setBackground(Color.BLACK); 
        pBrand.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(pBrand, "BRAND");

        // --- PAGE 2: TIER ---
        JPanel pTier = new JPanel(new GridBagLayout()); 
        pTier.setBackground(Color.BLACK);
        
        JButton b1 = new JButton("SUPERCAR"); styleBtn(b1, new Color(0, 100, 200));
        b1.addActionListener(e->{ selectedTier="Supercar"; loadModels(); cards.show(mainPanel, "MODEL"); });
        
        JButton b2 = new JButton("HYPERCAR"); styleBtn(b2, new Color(200, 0, 0));
        b2.addActionListener(e->{ selectedTier="Hypercar"; loadModels(); cards.show(mainPanel, "MODEL"); });
        
        JButton bBack = new JButton("BACK"); 
        bBack.addActionListener(e->cards.show(mainPanel,"BRAND"));
        
        GridBagConstraints g = new GridBagConstraints(); g.insets=new Insets(10,10,10,10);
        pTier.add(b1,g); g.gridx=1; pTier.add(b2,g); g.gridy=1; g.gridwidth=2; pTier.add(bBack,g);
        mainPanel.add(pTier, "TIER");

        // --- PAGE 3: MODEL ---
        JPanel pModel = new JPanel(new BorderLayout()); 
        pModel.setBackground(Color.BLACK);
        
        JButton bBackModel = new JButton("BACK"); 
        bBackModel.addActionListener(e->cards.show(mainPanel,"TIER"));
        pModel.add(bBackModel, BorderLayout.NORTH);
        
        modelContainer = new JPanel(new GridLayout(0,2,20,20)); 
        modelContainer.setBackground(Color.BLACK);
        
        JScrollPane scrollModel = new JScrollPane(modelContainer);
        scrollModel.setBorder(null);
        scrollModel.getViewport().setBackground(Color.BLACK);
        pModel.add(scrollModel, BorderLayout.CENTER);
        
        mainPanel.add(pModel, "MODEL");
    }

    private void loadModels() {
        modelContainer.removeAll();
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        try {
            Connection conn = KoneksiDB.configDB();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM cars WHERE brand=? AND tier=?");
            ps.setString(1, selectedBrand); ps.setString(2, selectedTier);
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            while(rs.next()) {
                found = true;
                int id = rs.getInt("id"); String name = rs.getString("model_name");
                double price = rs.getDouble("price"); int stock = rs.getInt("stock");
                String img = rs.getString("image_file");

                JPanel card = new JPanel(new BorderLayout()); 
                card.setBackground(new Color(25, 25, 25)); 
                card.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50)));
                
                JButton btnImg = createBtn("", "img/"+img, 250, 150);
                
                String colorStock = stock > 0 ? "white" : "red";
                String info = "<html><center><font color='white' size='4'>"+name+"</font><br>" +
                              "<font color='yellow'>"+fmt.format(price)+"</font><br>" +
                              "<font color='"+colorStock+"'>Stock: "+stock+"</font></center></html>";
                
                btnImg.addActionListener(e -> {
                    if(stock>0) {
                        Car c = selectedTier.equals("Hypercar") ? new Hypercar(id,selectedBrand,name,price) : new Supercar(id,selectedBrand,name,price);
                        new PurchaseDialog(this, c, currentUser);
                    } else JOptionPane.showMessageDialog(this, "SOLD OUT");
                });

                if(currentUser.equalsIgnoreCase("admin")) {
                    JButton btnAdd = new JButton("[ADMIN] ADD STOCK (+)");
                    btnAdd.setBackground(new Color(0, 150, 0)); 
                    btnAdd.setForeground(Color.WHITE);
                    btnAdd.setFocusPainted(false);
                    btnAdd.addActionListener(ev -> updateStock(id));
                    card.add(btnAdd, BorderLayout.NORTH);
                }

                card.add(btnImg, BorderLayout.CENTER);
                card.add(new JLabel(info, SwingConstants.CENTER), BorderLayout.SOUTH);
                modelContainer.add(card);
            }
            if(!found) {
                JLabel empty = new JLabel("No models available.", SwingConstants.CENTER);
                empty.setForeground(Color.WHITE); modelContainer.add(empty);
            }
        } catch(Exception e) { e.printStackTrace(); }
        modelContainer.revalidate(); modelContainer.repaint();
    }

    private void updateStock(int id) {
        String in = JOptionPane.showInputDialog("Add amount:");
        if(in!=null) {
            try {
                int qty = Integer.parseInt(in);
                if(qty>0) {
                    Connection conn = KoneksiDB.configDB();
                    conn.createStatement().executeUpdate("UPDATE cars SET stock = stock + "+qty+" WHERE id="+id);
                    loadModels();
                }
            } catch(Exception e) {}
        }
    }

    private JButton createBtn(String brandName, String path, int w, int h) {
        JButton btn = new JButton();
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);

        try {
            File f = new File(path);
            if (f.exists()) {
                ImageIcon originalIcon = new ImageIcon(path);
                Image scaledImage = originalIcon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(scaledImage));
            } else {
                btn.setText("<html><center>"+brandName+"<br>(No Image)</center></html>");
                btn.setForeground(Color.GRAY);
            }
        } catch (Exception e) {
            btn.setText(brandName);
            btn.setForeground(Color.GRAY);
        }
        return btn;
    }

    private void styleBtn(JButton b, Color c) { 
        b.setBackground(c); 
        b.setForeground(Color.WHITE); 
        b.setPreferredSize(new Dimension(200,100)); 
        b.setFont(new Font("Arial", Font.BOLD, 18));
        b.setFocusPainted(false);
    }

    class ImagePanel extends JPanel {
        private Image bgImage;
        public ImagePanel(String imagePath) {
            try { bgImage = new ImageIcon(imagePath).getImage(); } catch (Exception e) {}
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) {
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                g.setColor(new Color(0, 0, 0, 180)); 
                g.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }
}