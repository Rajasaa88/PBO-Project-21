import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;

public class AppNavigationFrame extends JFrame {
    private CardLayout cards;
    private JPanel mainPanel, modelContainer;
    private String currentUser, selectedBrand, selectedTier;
    
    private JLabel lblPageTitle;
    private ThreadGifPanel slideshowPanel;
    private JLayeredPane globalLayer;
    private JPanel headerPanel;
    private JPanel footerPanel;

    public AppNavigationFrame(String user) {
        this.currentUser = user;
        setTitle("AutoAAR - User: " + user);
        
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE); 
        setLocationRelativeTo(null);

        KeyStroke escKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKey, "EXIT_APP");
        getRootPane().getActionMap().put("EXIT_APP", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(AppNavigationFrame.this, 
                        "Close Application?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0); 
                }
            }
        });

        setLayout(new BorderLayout());

        globalLayer = new JLayeredPane();
        add(globalLayer, BorderLayout.CENTER);

        footerPanel = createFooter();
        add(footerPanel, BorderLayout.SOUTH); 

        headerPanel = createHeader(user);
        
        cards = new CardLayout();
        mainPanel = new JPanel(cards); 
        mainPanel.setBackground(Color.BLACK); 
        
        initPages(); 
        
        globalLayer.add(mainPanel, Integer.valueOf(0));
        globalLayer.add(headerPanel, Integer.valueOf(1));

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = globalLayer.getWidth(); 
                int h = globalLayer.getHeight();
                mainPanel.setBounds(0, 0, w, h);
                headerPanel.setBounds(0, 0, w, 70); 
                globalLayer.revalidate();
            }
        });

        cards.show(mainPanel, "BRAND");
        setVisible(true);
    }

    private JPanel createFooter() {
        JPanel f = new JPanel();
        f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS)); 
        f.setBackground(Color.BLACK); 
        
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 50, 50)), 
            BorderFactory.createEmptyBorder(15, 0, 15, 0)
        ));

        JLabel lblCopy = new JLabel("© 2025 AutoAAR Showroom");
        lblCopy.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblCopy.setForeground(new Color(220, 220, 220)); 
        lblCopy.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDisclaimer = new JLabel("All information applies to Indonesia vehicles only");
        lblDisclaimer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDisclaimer.setForeground(new Color(150, 150, 150)); 
        lblDisclaimer.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblLinks = new JLabel("Privacy Policy   |   Legal Cookie   |   Contact Us");
        lblLinks.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLinks.setForeground(new Color(100, 180, 255)); 
        lblLinks.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLinks.setCursor(new Cursor(Cursor.HAND_CURSOR));

        f.add(lblCopy);
        f.add(Box.createVerticalStrut(5)); 
        f.add(lblDisclaimer);
        f.add(Box.createVerticalStrut(5)); 
        f.add(lblLinks);
        
        return f;
    }

    private JPanel createHeader(String user) {
        JPanel h = new JPanel(new BorderLayout());
        h.setOpaque(false); h.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 25)); 
        
        JLabel lblUser = new JLabel("Logged in as: " + user.toUpperCase());
        lblUser.setForeground(new Color(220, 220, 220)); 
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); 
        btnPanel.setOpaque(false);
        
        ModernButton btnMonitor = new ModernButton("LIVE MONITOR", new Color(0, 100, 200));
        btnMonitor.setPreferredSize(new Dimension(130, 35)); 
        btnMonitor.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnMonitor.addActionListener(e -> new MonitorDialog(this));
        
        ModernButton btnLogout = new ModernButton("LOGOUT", new Color(200, 50, 50));
        btnLogout.setPreferredSize(new Dimension(90, 35)); 
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLogout.addActionListener(e -> { 
            if (JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", 0) == 0) { 
                new LoginFrame(); dispose(); 
            } 
        });
        
        btnPanel.add(btnMonitor); 
        btnPanel.add(btnLogout);
        h.add(lblUser, BorderLayout.WEST); 
        h.add(btnPanel, BorderLayout.EAST);
        return h;
    }

    private void initPages() {
        // --- PAGE 1: BRAND ---
        JPanel pBrand = new JPanel(new BorderLayout()); 
        pBrand.setBackground(Color.BLACK); 
        
        ImagePanel welcomePanel = new ImagePanel("img/main.jpg"); 
        welcomePanel.setLayout(new GridBagLayout()); 
        GridBagConstraints gbc = new GridBagConstraints(); 
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(60, 10, 10, 10); 
        
        JLabel lblTitle = new JLabel("Welcome to AutoAAR"); 
        lblTitle.setFont(new Font("Serif", Font.BOLD, 48)); 
        lblTitle.setForeground(Color.WHITE); 
        
        JLabel lblDesc = new JLabel("<html><center>Speed, Luxury, and Prestige.</center></html>", 0); 
        lblDesc.setFont(new Font("SansSerif", Font.PLAIN, 18)); 
        lblDesc.setForeground(Color.WHITE); 
        
        welcomePanel.add(lblTitle, gbc); 
        gbc.gridy = 1; gbc.insets = new Insets(10, 10, 10, 10); 
        welcomePanel.add(lblDesc, gbc);
        pBrand.add(welcomePanel, BorderLayout.NORTH);

        JPanel gridBrand = new JPanel(new GridLayout(0, 3, 40, 40)); 
        gridBrand.setBackground(Color.BLACK); 
        gridBrand.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50)); 
        
        try {
            Connection conn = KoneksiDB.configDB();
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
        } catch(Exception e) {}
        
        JScrollPane scrollBrand = new JScrollPane(gridBrand); 
        scrollBrand.setBorder(null); 
        scrollBrand.getViewport().setBackground(Color.BLACK);
        scrollBrand.setVerticalScrollBarPolicy(21); 
        scrollBrand.setHorizontalScrollBarPolicy(31); 
        scrollBrand.getVerticalScrollBar().setUnitIncrement(20);
        
        pBrand.add(scrollBrand, BorderLayout.CENTER);
        mainPanel.add(pBrand, "BRAND");

        // --- PAGE 2: TIER ---
        JPanel tierPanel = new JPanel(); 
        tierPanel.setLayout(new OverlayLayout(tierPanel));
        
        slideshowPanel = new ThreadGifPanel(); 
        slideshowPanel.setAlignmentX(0.5f); slideshowPanel.setAlignmentY(0.5f);
        
        JPanel pTierButtons = new JPanel(new GridBagLayout()); 
        pTierButtons.setOpaque(false); 
        pTierButtons.setAlignmentX(0.5f); pTierButtons.setAlignmentY(0.5f);
        
        ModernButton b1 = new ModernButton("SUPERCAR", new Color(0, 190, 255)); 
        b1.setPreferredSize(new Dimension(280, 80)); b1.setFont(new Font("Segoe UI", Font.BOLD, 24));
        b1.addActionListener(e->{ selectedTier="Supercar"; loadModels(); cards.show(mainPanel, "MODEL"); });
        
        ModernButton b2 = new ModernButton("HYPERCAR", new Color(255, 0, 80)); 
        b2.setPreferredSize(new Dimension(280, 80)); b2.setFont(new Font("Segoe UI", Font.BOLD, 24));
        b2.addActionListener(e->{ selectedTier="Hypercar"; loadModels(); cards.show(mainPanel, "MODEL"); });
        
        ModernButton bBack = new ModernButton("BACK", new Color(100, 100, 100)); 
        bBack.setPreferredSize(new Dimension(150, 50)); bBack.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bBack.addActionListener(e->{ slideshowPanel.stopSlideshow(); cards.show(mainPanel,"BRAND"); });
        
        GridBagConstraints g = new GridBagConstraints(); g.insets=new Insets(20,20,20,20);
        pTierButtons.add(b1,g); g.gridx=1; pTierButtons.add(b2,g); 
        g.gridy=1; g.gridwidth=2; g.insets=new Insets(50,20,20,20); pTierButtons.add(bBack,g);
        
        tierPanel.add(pTierButtons); 
        tierPanel.add(slideshowPanel);
        mainPanel.add(tierPanel, "TIER");

        // --- PAGE 3: MODEL ---
        JPanel pModel = new JPanel(new BorderLayout()); 
        pModel.setBackground(Color.BLACK);
        
        JPanel pHeaderModel = new JPanel(new BorderLayout());
        pHeaderModel.setBackground(Color.BLACK);
        pHeaderModel.setBorder(BorderFactory.createEmptyBorder(80, 30, 20, 30)); 

        ModernButton bBackModel = new ModernButton("BACK", new Color(100, 100, 100));
        bBackModel.setPreferredSize(new Dimension(100, 40)); 
        bBackModel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bBackModel.addActionListener(e->cards.show(mainPanel,"TIER"));

        lblPageTitle = new JLabel("AVAILABLE MODELS");
        lblPageTitle.setFont(new Font("Serif", Font.BOLD, 30));
        lblPageTitle.setForeground(Color.WHITE);
        lblPageTitle.setHorizontalAlignment(SwingConstants.RIGHT);

        pHeaderModel.add(bBackModel, BorderLayout.WEST);
        pHeaderModel.add(lblPageTitle, BorderLayout.EAST);
        pModel.add(pHeaderModel, BorderLayout.NORTH);
        
        modelContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 30)); 
        modelContainer.setBackground(Color.BLACK);
        modelContainer.setPreferredSize(new Dimension(950, 3000)); 

        JScrollPane scrollModel = new JScrollPane(modelContainer);
        scrollModel.setBorder(null);
        scrollModel.getViewport().setBackground(Color.BLACK);
        scrollModel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollModel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollModel.getVerticalScrollBar().setUnitIncrement(25);
        
        pModel.add(scrollModel, BorderLayout.CENTER);
        mainPanel.add(pModel, "MODEL");
    }

    private void loadModels() {
        modelContainer.removeAll();
        lblPageTitle.setText(selectedBrand.toUpperCase() + " " + selectedTier.toUpperCase() + "S");
        
        int itemCount = 0;

        try {
            Connection conn = KoneksiDB.configDB();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM cars WHERE brand=? AND tier=?");
            ps.setString(1, selectedBrand); ps.setString(2, selectedTier);
            ResultSet rs = ps.executeQuery();
            boolean found = false;
            while(rs.next()) {
                found = true;
                itemCount++;
                int id = rs.getInt("id"); String name = rs.getString("model_name");
                double price = rs.getDouble("price"); int stock = rs.getInt("stock");
                String img = rs.getString("image_file");
                modelContainer.add(new CarCard(id, name, price, stock, img));
            }
            if(!found) {
                JLabel empty = new JLabel("No models available.", SwingConstants.CENTER);
                empty.setForeground(Color.GRAY); empty.setFont(new Font("Segoe UI", Font.ITALIC, 18));
                modelContainer.add(empty);
                itemCount = 1;
            }
        } catch(Exception e) { e.printStackTrace(); }

        int rows = (int) Math.ceil((double) itemCount / 3.0); 
        int dynamicHeight = (rows * 460) + 100; 
        modelContainer.setPreferredSize(new Dimension(950, dynamicHeight));

        modelContainer.revalidate(); modelContainer.repaint();
    }

    private void updateStock(int id) {
        String in = JOptionPane.showInputDialog("Add amount:");
        if(in!=null) { try { int qty = Integer.parseInt(in); if(qty>0) { KoneksiDB.configDB().createStatement().executeUpdate("UPDATE cars SET stock = stock + "+qty+" WHERE id="+id); loadModels(); } } catch(Exception e) {} }
    }

    private ModernButton createBtn(String brandName, String path, int w, int h) {
        ModernButton btn = new ModernButton("", new Color(40, 40, 40)); 
        btn.setHorizontalTextPosition(0); btn.setVerticalTextPosition(3);
        try { File f = new File(path); if (f.exists()) { ImageIcon icon = new ImageIcon(path); btn.setIcon(new ImageIcon(icon.getImage().getScaledInstance(w, h, 4))); btn.setText(brandName.isEmpty()?"":brandName); } else btn.setText("<html><center>"+brandName+"<br>(No Image)</center></html>"); } catch (Exception e) { btn.setText(brandName); }
        return btn;
    }

    class ImagePanel extends JPanel {
        private Image bg; public ImagePanel(String p) { try { bg = new ImageIcon(p).getImage(); } catch (Exception e) {} }
        protected void paintComponent(Graphics g) { super.paintComponent(g); if (bg != null) { g.drawImage(bg, 0, 0, getWidth(), getHeight(), this); g.setColor(new Color(0, 0, 0, 180)); g.fillRect(0, 0, getWidth(), getHeight()); } else { g.setColor(Color.BLACK); g.fillRect(0, 0, getWidth(), getHeight()); } }
    }

    // --- CAR CARD (FIXED ADMIN BUTTON) ---
    class CarCard extends JPanel {
        private int carId; 
        private String carName; 
        private double carPrice; 
        private int carStock; 
        private Image carImage; 
        private boolean isHover = false;

        public CarCard(int id, String name, double price, int stock, String imgFile) {
            this.carId = id; 
            this.carName = name; 
            this.carPrice = price; 
            this.carStock = stock;
            
            try { 
                File f = new File("img/" + imgFile); 
                if(f.exists()) carImage = new ImageIcon("img/" + imgFile).getImage(); 
            } catch(Exception e){}
            
            setLayout(new GridBagLayout()); 
            setOpaque(false);
            setPreferredSize(new Dimension(320, 420)); 
            
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { isHover = true; repaint(); }
                public void mouseExited(MouseEvent e) { isHover = false; repaint(); }
            });
            initLayout();
        }

        private void initLayout() {
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(5, 10, 5, 10); 
            g.fill = GridBagConstraints.HORIZONTAL; 
            g.anchor = GridBagConstraints.CENTER;

            g.gridx = 0; g.gridy = 0; g.weighty = 1.0; 
            add(Box.createVerticalStrut(200), g); 

            JLabel lblName = new JLabel(carName.toUpperCase());
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 20)); 
            lblName.setForeground(Color.WHITE); 
            lblName.setHorizontalAlignment(SwingConstants.CENTER);
            g.gridy = 1; g.weighty = 0; 
            add(lblName, g);

            NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            JLabel lblPrice = new JLabel(fmt.format(carPrice));
            lblPrice.setFont(new Font("Consolas", Font.BOLD, 16)); 
            lblPrice.setForeground(new Color(255, 215, 0)); 
            lblPrice.setHorizontalAlignment(SwingConstants.CENTER);
            g.gridy = 2; 
            add(lblPrice, g);

            JPanel bottomPanel = new JPanel(new BorderLayout()); 
            bottomPanel.setOpaque(false);
            
            // Container Stok di Kiri
            JLabel lblStock = new JLabel(carStock > 0 ? "Stock: " + carStock : "SOLD OUT");
            lblStock.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblStock.setForeground(carStock > 0 ? new Color(0, 255, 100) : Color.RED);
            bottomPanel.add(lblStock, BorderLayout.WEST); 

            // --- LOGIKA TOMBOL UTAMA (MODIFIKASI DI SINI) ---
            ModernButton btnAction;
            
            if (currentUser.equalsIgnoreCase("admin")) {
                // JIKA ADMIN: TOMBOL JADI "ADD STOCK" (Hijau)
                btnAction = new ModernButton("ADD STOCK", new Color(0, 150, 0)); 
                btnAction.addActionListener(ev -> updateStock(carId));
            } else {
                // JIKA CUSTOMER: TOMBOL NORMAL "PURCHASE"
                if (carStock > 0) {
                    btnAction = new ModernButton("PURCHASE", new Color(0, 100, 200));
                } else {
                    btnAction = new ModernButton("EMPTY", new Color(100, 100, 100));
                    btnAction.setEnabled(false);
                }
                // Listener hanya untuk Customer
                btnAction.addActionListener(e -> {
                     Car c = selectedTier.equals("Hypercar") ? new Hypercar(carId,selectedBrand,carName,carPrice) : new Supercar(carId,selectedBrand,carName,carPrice);
                     new PurchaseDialog(AppNavigationFrame.this, c, currentUser);
                });
            }

            btnAction.setPreferredSize(new Dimension(110, 35)); 
            btnAction.setFont(new Font("Segoe UI", Font.BOLD, 11)); 
            bottomPanel.add(btnAction, BorderLayout.EAST);
            
            g.gridy = 3; g.insets = new Insets(15, 10, 20, 10); 
            add(bottomPanel, g);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight();
            
            g2d.setColor(new Color(30, 30, 30, 230)); 
            g2d.fillRoundRect(0, 0, w, h, 25, 25);

            if (carImage != null) {
                int imgH = 180; int imgW = (int) ((double)carImage.getWidth(null) / carImage.getHeight(null) * imgH);
                if(imgW > w - 20) { imgW = w - 20; imgH = (int) ((double)carImage.getHeight(null) / carImage.getWidth(null) * imgW); }
                int imgX = (w - imgW) / 2; int imgY = 20; 
                g2d.drawImage(carImage, imgX, imgY, imgW, imgH, this);
            }

            // Hover Border Effect: Hanya untuk Customer (Admin tidak perlu border warna warni)
            boolean canBuy = !currentUser.equalsIgnoreCase("admin") && carStock > 0;
            
            if (isHover && canBuy) {
                g2d.setColor(selectedTier.equals("Hypercar") ? new Color(255, 0, 80) : new Color(0, 190, 255));
                g2d.setStroke(new BasicStroke(2f)); 
                g2d.drawRoundRect(1, 1, w-2, h-2, 25, 25);
                g2d.setColor(new Color(255, 255, 255, 20)); 
                g2d.fillRoundRect(0, 0, w, h, 25, 25);
            } else {
                g2d.setColor(new Color(60, 60, 60)); 
                g2d.drawRoundRect(0, 0, w-1, h-1, 25, 25);
            }
            g2d.dispose();
        }
    }
}