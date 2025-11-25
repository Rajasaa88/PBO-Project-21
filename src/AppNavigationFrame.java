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
    private JLabel footer;

    public AppNavigationFrame(String user) {
        this.currentUser = user;
        setTitle("Elite Auto Gallery - User: " + user);
        setSize(1000, 750); 
        setDefaultCloseOperation(EXIT_ON_CLOSE); 
        setLocationRelativeTo(null);

        // --- 1. HEADER (LOGOUT & INFO USER) ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 30, 30)); // Abu Gelap
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding

        // Label User di Kiri
        JLabel lblUser = new JLabel("Welcome, " + user.toUpperCase());
        lblUser.setForeground(Color.CYAN);
        lblUser.setFont(new Font("Arial", Font.BOLD, 16));
        
        // Tombol Logout di Kanan
        JButton btnLogout = new JButton("LOGOUT");
        btnLogout.setBackground(Color.RED);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Aksi Logout
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Apakah Anda yakin ingin Logout?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                new LoginFrame(); // Buka Login lagi
                dispose();        // Tutup Menu Utama
            }
        });

        header.add(lblUser, BorderLayout.WEST);
        header.add(btnLogout, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // --- 2. MAIN CONTENT (Tengah) ---
        cards = new CardLayout();
        mainPanel = new JPanel(cards); 
        mainPanel.setBackground(Color.BLACK);
        
        initPages(); // Setup Brand -> Tier -> Model
        add(mainPanel, BorderLayout.CENTER);

        // --- 3. FOOTER MONITOR (Bawah) ---
        footer = new JLabel("System Ready...", SwingConstants.CENTER);
        footer.setOpaque(true); 
        footer.setBackground(Color.BLACK); 
        footer.setForeground(Color.GREEN);
        footer.setPreferredSize(new Dimension(getWidth(), 40));
        footer.setBorder(BorderFactory.createMatteBorder(1,0,0,0, Color.GRAY));
        add(footer, BorderLayout.SOUTH);

        // --- THREAD START ---
        new SalesMonitor(footer).start(); // Thread Database Check

        cards.show(mainPanel, "BRAND");
        setVisible(true);
    }

    // --- HALAMAN 1: PILIH BRAND ---
    private void initPages() {
        // A. Brand Page
        JPanel pBrand = new JPanel(new BorderLayout()); 
        pBrand.setBackground(Color.DARK_GRAY);
        
        JLabel title = new JLabel("SELECT MANUFACTURER", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28)); 
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20,0,0,0));
        pBrand.add(title, BorderLayout.NORTH);

        JPanel gridBrand = new JPanel(new GridLayout(0,3,20,20)); 
        gridBrand.setBackground(Color.DARK_GRAY);
        gridBrand.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
        
        try {
            Connection conn = KoneksiDB.configDB();
            if(conn!=null) {
                ResultSet rs = conn.createStatement().executeQuery("SELECT DISTINCT brand FROM cars ORDER BY brand");
                while(rs.next()) {
                    String b = rs.getString("brand");
                    JButton btn = createBtn(b, "img/"+b+".png");
                    btn.addActionListener(e -> { selectedBrand=b; cards.show(mainPanel, "TIER"); });
                    gridBrand.add(btn);
                }
            }
        } catch(Exception e) {}
        pBrand.add(new JScrollPane(gridBrand), BorderLayout.CENTER);
        mainPanel.add(pBrand, "BRAND");

        // B. Tier Page
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

        // C. Model Page
        JPanel pModel = new JPanel(new BorderLayout()); 
        pModel.setBackground(Color.BLACK);
        
        JButton bBackModel = new JButton("BACK"); 
        bBackModel.addActionListener(e->cards.show(mainPanel,"TIER"));
        pModel.add(bBackModel, BorderLayout.NORTH);
        
        modelContainer = new JPanel(new GridLayout(0,2,20,20)); 
        modelContainer.setBackground(Color.BLACK);
        pModel.add(new JScrollPane(modelContainer), BorderLayout.CENTER);
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
                card.setBackground(Color.DARK_GRAY);
                card.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                
                JButton btnImg = createBtn("", "img/"+img);
                String colorStock = stock > 0 ? "white" : "red";
                String info = "<html><center><font color='white' size='4'>"+name+"</font><br><font color='yellow'>"+fmt.format(price)+"</font><br><font color='"+colorStock+"'>Stock: "+stock+"</font></center></html>";
                
                btnImg.addActionListener(e -> {
                    if(stock>0) {
                        Car c = selectedTier.equals("Hypercar") ? new Hypercar(id,selectedBrand,name,price) : new Supercar(id,selectedBrand,name,price);
                        new PurchaseDialog(this, c, currentUser);
                    } else JOptionPane.showMessageDialog(this, "SOLD OUT");
                });

                // LOGIKA ADMIN ADD STOCK
                if(currentUser.equalsIgnoreCase("admin")) {
                    JButton btnAdd = new JButton("[ADMIN] ADD STOCK (+)");
                    btnAdd.setBackground(Color.GREEN);
                    btnAdd.setForeground(Color.BLACK);
                    btnAdd.addActionListener(ev -> updateStock(id));
                    card.add(btnAdd, BorderLayout.NORTH);
                }

                card.add(btnImg, BorderLayout.CENTER);
                card.add(new JLabel(info, SwingConstants.CENTER), BorderLayout.SOUTH);
                modelContainer.add(card);
            }
            if(!found) {
                JLabel empty = new JLabel("No models.", SwingConstants.CENTER);
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

    private JButton createBtn(String t, String p) {
        JButton b = new JButton();
        try { File f=new File(p); if(f.exists()) b.setIcon(new ImageIcon(new ImageIcon(p).getImage().getScaledInstance(200,120,4))); else b.setText(t); } catch(Exception e){b.setText(t);}
        b.setContentAreaFilled(false); b.setBorderPainted(false); return b;
    }
    private void styleBtn(JButton b, Color c) { b.setBackground(c); b.setForeground(Color.WHITE); b.setPreferredSize(new Dimension(200,100)); }
}