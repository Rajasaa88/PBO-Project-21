import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;

public class AppNavigationFrame extends JFrame {
    private CardLayout cards;
    private JPanel mainPanel, modelContainer;
    private String currentUser, selectedBrand, selectedTier;
    
    private ThreadGifPanel slideshowPanel;

    public AppNavigationFrame(String user) {
        this.currentUser = user;
        setTitle("AutoAAR - User: " + user);
        setSize(1000, 850); 
        setDefaultCloseOperation(EXIT_ON_CLOSE); 
        setLocationRelativeTo(null);

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(20, 20, 20)); 
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblUser = new JLabel("Logged in as: " + user.toUpperCase());
        lblUser.setForeground(Color.LIGHT_GRAY);
        lblUser.setFont(new Font("Arial", Font.BOLD, 14));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        ModernButton btnMonitor = new ModernButton("LIVE MONITOR", new Color(0, 100, 200));
        btnMonitor.setPreferredSize(new Dimension(140, 35));
        btnMonitor.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnMonitor.addActionListener(e -> new MonitorDialog(this));

        ModernButton btnLogout = new ModernButton("LOGOUT", new Color(200, 50, 50));
        btnLogout.setPreferredSize(new Dimension(100, 35));
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
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

        // --- MAIN CONTENT ---
        cards = new CardLayout();
        mainPanel = new JPanel(cards); 
        mainPanel.setBackground(Color.BLACK); 
        
        initPages(); 
        add(mainPanel, BorderLayout.CENTER);
        
        cards.show(mainPanel, "BRAND");
        setVisible(true);
    }

    private void initPages() {
        // --- PAGE 1: BRAND ---
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
        
        JLabel lblDesc = new JLabel("<html><center>Speed, Luxury, and Prestige.</center></html>", SwingConstants.CENTER); 
        lblDesc.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lblDesc.setForeground(Color.WHITE); 

        welcomePanel.add(lblTitle, gbc);
        gbc.gridy = 1; welcomePanel.add(lblDesc, gbc);
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
                    ModernButton btn = createBtn(b, "img/"+b+".png", 180, 180); 
                    btn.addActionListener(e -> { 
                        selectedBrand = b; 
                        if(slideshowPanel != null) slideshowPanel.startSlideshow(b);
                        cards.show(mainPanel, "TIER"); 
                    });
                    gridBrand.add(btn);
                }
            }
        } catch(Exception e) {}
        
        // --- UPDATE: Scroll Pane Invisible Tapi Cepat ---
        JScrollPane scrollBrand = new JScrollPane(gridBrand);
        scrollBrand.setBorder(null); // Hapus garis pinggir
        scrollBrand.getViewport().setBackground(Color.BLACK);
        
        // 1. Sembunyikan Bar (Invisible) tapi mouse wheel tetap jalan
        scrollBrand.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollBrand.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        // 2. Naikkan kecepatan scroll jadi 20 (biar ngebut/halus)
        scrollBrand.getVerticalScrollBar().setUnitIncrement(20);
        
        pBrand.add(scrollBrand, BorderLayout.CENTER);
        mainPanel.add(pBrand, "BRAND");

        // --- PAGE 2: TIER ---
        JLayeredPane layerTier = new JLayeredPane();
        slideshowPanel = new ThreadGifPanel();
        slideshowPanel.setBounds(0, 0, 1000, 850); 
        
        JPanel pTierButtons = new JPanel(new GridBagLayout()); 
        pTierButtons.setOpaque(false); 
        pTierButtons.setBounds(0, 0, 1000, 850);
        
        layerTier.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = layerTier.getWidth(); int h = layerTier.getHeight();
                slideshowPanel.setBounds(0, 0, w, h);
                pTierButtons.setBounds(0, 0, w, h);
            }
        });

        ModernButton b1 = new ModernButton("SUPERCAR", new Color(0, 190, 255));
        b1.setPreferredSize(new Dimension(280, 80));
        b1.setFont(new Font("Segoe UI", Font.BOLD, 24));
        b1.addActionListener(e->{ selectedTier="Supercar"; loadModels(); cards.show(mainPanel, "MODEL"); });
        
        ModernButton b2 = new ModernButton("HYPERCAR", new Color(255, 0, 80));
        b2.setPreferredSize(new Dimension(280, 80));
        b2.setFont(new Font("Segoe UI", Font.BOLD, 24));
        b2.addActionListener(e->{ selectedTier="Hypercar"; loadModels(); cards.show(mainPanel, "MODEL"); });
        
        ModernButton bBack = new ModernButton("BACK", new Color(100, 100, 100));
        bBack.setPreferredSize(new Dimension(150, 50)); 
        bBack.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bBack.addActionListener(e->{
            slideshowPanel.stopSlideshow();
            cards.show(mainPanel,"BRAND");
        });
        
        GridBagConstraints g = new GridBagConstraints(); 
        g.insets=new Insets(20,20,20,20);
        pTierButtons.add(b1,g); g.gridx=1; pTierButtons.add(b2,g); 
        g.gridy=1; g.gridwidth=2; g.insets=new Insets(50,20,20,20);
        pTierButtons.add(bBack,g);
        
        layerTier.add(slideshowPanel, Integer.valueOf(0));
        layerTier.add(pTierButtons, Integer.valueOf(1));
        mainPanel.add(layerTier, "TIER");

        // --- PAGE 3: MODEL ---
        JPanel pModel = new JPanel(new BorderLayout()); 
        pModel.setBackground(Color.BLACK);
        
        JPanel pHeaderModel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pHeaderModel.setBackground(Color.BLACK);
        ModernButton bBackModel = new ModernButton("BACK", new Color(100, 100, 100));
        bBackModel.setPreferredSize(new Dimension(120, 40));
        bBackModel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bBackModel.addActionListener(e->cards.show(mainPanel,"TIER"));
        pHeaderModel.add(bBackModel);
        pModel.add(pHeaderModel, BorderLayout.NORTH);
        
        modelContainer = new JPanel(new GridLayout(0,2,20,20)); 
        modelContainer.setBackground(Color.BLACK);
        
        // Scrollbar Page Model juga dipercepat, tapi Bar-nya dibiarkan terlihat (kecuali mau dihidden juga)
        JScrollPane scrollModel = new JScrollPane(modelContainer);
        scrollModel.setBorder(null);
        scrollModel.getViewport().setBackground(Color.BLACK);
        scrollModel.getVerticalScrollBar().setUnitIncrement(20); // Sama-sama cepat 20
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

                ModernButton btnImg = createBtn("", "img/"+img, 400, 250);
                
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
                    ModernButton btnAdd = new ModernButton("[ADMIN] ADD STOCK (+)", new Color(0, 150, 0));
                    btnAdd.setPreferredSize(new Dimension(200, 30));
                    btnAdd.setFont(new Font("Arial", Font.BOLD, 12));
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

    private ModernButton createBtn(String brandName, String path, int w, int h) {
        ModernButton btn = new ModernButton("", new Color(40, 40, 40)); 
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        try {
            File f = new File(path);
            if (f.exists()) {
                ImageIcon originalIcon = new ImageIcon(path);
                Image scaledImage = originalIcon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(scaledImage));
                btn.setText(brandName.isEmpty() ? "" : brandName);
            } else {
                btn.setText("<html><center>"+brandName+"<br>(No Image)</center></html>");
            }
        } catch (Exception e) {
            btn.setText(brandName);
        }
        return btn;
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
            } else { g.setColor(Color.BLACK); g.fillRect(0, 0, getWidth(), getHeight()); }
        }
    }
}