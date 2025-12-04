import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

public class MonitorDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;
    private volatile boolean isRunning = true;

    public MonitorDialog(JFrame parent) {
        super(parent, "Live Sales Monitor", true); 
        
        setUndecorated(true); 
        setSize(900, 600);
        setLocationRelativeTo(parent);
        
        KeyStroke escKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKey, "CLOSE");
        getRootPane().getActionMap().put("CLOSE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isRunning = false;
                dispose();
            }
        });

        // 1. Background Panel
        BackgroundPanel bgPanel = new BackgroundPanel("img/main.jpg");
        bgPanel.setLayout(new BorderLayout());
        bgPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 200), 2)); 

        // 2. Header Title (Glass)
        GlassPanel pHeader = new GlassPanel();
        pHeader.setLayout(new BorderLayout());
        pHeader.setPreferredSize(new Dimension(900, 80));
        pHeader.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("REAL-TIME TRANSACTIONS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        JLabel subtitle = new JLabel("Live Data Stream from Database");
        subtitle.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        subtitle.setForeground(Color.LIGHT_GRAY);
        
        JPanel titleBlock = new JPanel(new GridLayout(2,1));
        titleBlock.setOpaque(false);
        titleBlock.add(title);
        titleBlock.add(subtitle);

        ModernButton btnClose = new ModernButton("CLOSE (ESC)", new Color(200, 50, 50));
        btnClose.setPreferredSize(new Dimension(120, 35));
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnClose.addActionListener(e -> { isRunning = false; dispose(); });

        pHeader.add(titleBlock, BorderLayout.WEST);
        pHeader.add(btnClose, BorderLayout.EAST);
        bgPanel.add(pHeader, BorderLayout.NORTH);

        // --- 3. TABLE SETUP (PREMIUM LOOK) ---
        String[] columns = {"ID", "Buyer Name", "Car Model", "Price (IDR)", "Time"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel) {
            // Override ini agar baris selang-seling warnanya (Zebra Striping)
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    // Baris Ganjil: Hitam Transparan, Baris Genap: Abu Gelap Transparan
                    c.setBackground(row % 2 == 0 ? new Color(20, 20, 20, 200) : new Color(40, 40, 40, 200));
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        };
        
        // Setup Body Table
        table.setOpaque(false); // Biar transparan ikut renderer
        ((DefaultTableCellRenderer)table.getDefaultRenderer(Object.class)).setOpaque(false);
        table.setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(0, 100, 200)); 
        table.setSelectionForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(45); // Baris lebih tinggi biar lega
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(80, 80, 80)); // Garis pemisah tipis

        // --- CUSTOM HEADER RENDERER (BAGIAN PENTING) ---
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Styling Header:
                l.setBackground(new Color(30, 30, 30)); // Warna Header Gelap
                l.setForeground(new Color(0, 190, 255)); // Teks Biru Neon
                l.setFont(new Font("Segoe UI", Font.BOLD, 14));
                l.setHorizontalAlignment(JLabel.CENTER);
                
                // Border Bawah Tebal (Garis Neon)
                l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 190, 255)), 
                    BorderFactory.createEmptyBorder(10, 5, 10, 5)
                ));
                return l;
            }
        });
        
        // Center Alignment untuk Isi Tabel
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        // Pastikan background transparan agar Zebra Striping terlihat
        centerRenderer.setOpaque(false); 
        centerRenderer.setForeground(Color.WHITE);
        
        for(int i=0; i<columns.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(20, 20, 20)); // Warna dasar di balik tabel
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false); 
        
        // Ubah warna pojok kanan atas scrollbar (biasanya abu-abu jelek)
        scrollPane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, createCornerBox());

        bgPanel.add(scrollPane, BorderLayout.CENTER);

        // --- 4. FOOTER ---
        GlassPanel pFooter = new GlassPanel();
        pFooter.setLayout(new FlowLayout(FlowLayout.RIGHT, 30, 20));
        pFooter.setPreferredSize(new Dimension(900, 70));

        lblTotal = new JLabel("Total Units Sold: 0");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotal.setForeground(new Color(0, 255, 100)); 
        
        pFooter.add(lblTotal);
        bgPanel.add(pFooter, BorderLayout.SOUTH);

        add(bgPanel);

        new Thread(this::updateDataLoop).start();
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { isRunning = false; }
        });

        setVisible(true);
    }

    private JPanel createCornerBox() {
        JPanel p = new JPanel();
        p.setBackground(new Color(30, 30, 30)); // Samakan dengan warna Header
        return p;
    }

    private void updateDataLoop() {
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        SimpleDateFormat dateFmt = new SimpleDateFormat("HH:mm:ss");

        while (isRunning) {
            try {
                Connection conn = KoneksiDB.configDB();
                if (conn != null) {
                    String sql = "SELECT s.id, s.buyer_name, c.model_name, c.price, s.date_time " +
                                 "FROM sales s " +
                                 "LEFT JOIN cars c ON s.car_id = c.id " +
                                 "ORDER BY s.date_time DESC";
                                 
                    Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery(sql);

                    ArrayList<Object[]> tempDataList = new ArrayList<>();
                    while (rs.next()) {
                        String id = rs.getString(1);
                        String buyer = rs.getString(2);
                        String car = rs.getString(3);
                        double price = rs.getDouble(4);
                        Timestamp time = rs.getTimestamp(5);
                        
                        tempDataList.add(new Object[]{
                            "TRX-" + id, 
                            buyer != null ? buyer.toUpperCase() : "UNKNOWN", 
                            car != null ? car : "?", 
                            fmt.format(price), 
                            time != null ? dateFmt.format(time) : "-"
                        });
                    }
                    conn.close();

                    SwingUtilities.invokeLater(() -> {
                        tableModel.setRowCount(0); 
                        for (Object[] row : tempDataList) {
                            tableModel.addRow(row);
                        }
                        lblTotal.setText("Total Units Sold: " + tempDataList.size());
                    });
                }
                Thread.sleep(3000); 
            } catch (Exception e) {
                System.out.println("Monitor Error: " + e.getMessage());
            }
        }
    }

    // --- INNER CLASSES ---

    class ModernButton extends JButton {
        private Color baseColor;
        private boolean isHovering = false;
        public ModernButton(String text, Color color) {
            super(text); this.baseColor = color;
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setOpaque(false);
            setForeground(Color.WHITE); setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { isHovering = true; repaint(); }
                public void mouseExited(MouseEvent e) { isHovering = false; repaint(); }
            });
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isHovering ? baseColor.brighter() : baseColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            super.paintComponent(g2); g2.dispose();
        }
    }

    class BackgroundPanel extends JPanel {
        private Image bg;
        public BackgroundPanel(String path) { try { bg = new ImageIcon(path).getImage(); } catch(Exception e){} }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(bg!=null) { 
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                g.setColor(new Color(0, 0, 0, 220)); 
                g.fillRect(0, 0, getWidth(), getHeight());
            } else { g.setColor(Color.BLACK); g.fillRect(0, 0, getWidth(), getHeight()); }
        }
    }

    class GlassPanel extends JPanel {
        public GlassPanel() { setOpaque(false); }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 150)); 
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 0, 0);
            g2.setColor(new Color(255, 255, 255, 20)); 
            g2.drawRect(0, 0, getWidth()-1, getHeight()-1);
            g2.dispose();
        }
    }
}