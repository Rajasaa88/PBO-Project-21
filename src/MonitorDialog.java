import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class MonitorDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;
    private volatile boolean isRunning = true;

    public MonitorDialog(JFrame parent) {
        super(parent, "Live Sales Monitor", false);
        setSize(900, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // --- HEADER ---
        JPanel pHeader = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pHeader.setBackground(new Color(20, 20, 20)); // Header tetap gelap biar elegan
        JLabel title = new JLabel("REAL-TIME TRANSACTION HISTORY");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        pHeader.add(title);
        add(pHeader, BorderLayout.NORTH);

        // --- TABLE ---
        String[] columns = {"ID", "Buyer Name", "Car Model", "Price (IDR)", "Transaction Time"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        
        // === STYLING AGAR MUDAH DIBACA (BLACK TEXT ON WHITE) ===
        table.setBackground(Color.WHITE);           // Background Putih Bersih
        table.setForeground(Color.BLACK);           // Teks Hitam Pekat
        table.setSelectionBackground(new Color(220, 220, 220)); // Abu muda saat diklik
        table.setSelectionForeground(Color.BLACK);  // Teks tetap hitam saat diklik
        table.setFont(new Font("SansSerif", Font.PLAIN, 14)); // Font agak besar biar jelas
        table.setRowHeight(35);                     // Baris lebih tinggi biar lega
        table.setGridColor(Color.LIGHT_GRAY);       // Garis tabel abu-abu
        table.setShowGrid(true);

        // Styling Header Tabel (Judul Kolom)
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(new Color(50, 50, 50)); // Header tabel gelap
        header.setForeground(Color.WHITE);           // Teks header putih
        
        // Perbaiki Renderer agar Teks di Tengah & Warna Konsisten
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(Color.WHITE); // Paksa cell putih
        centerRenderer.setForeground(Color.BLACK); // Paksa teks hitam
        
        for(int i=0; i<columns.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- FOOTER ---
        JPanel pFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pFooter.setBackground(Color.BLACK);
        lblTotal = new JLabel("Total Units Sold: 0   ");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotal.setForeground(Color.GREEN);
        pFooter.add(lblTotal);
        add(pFooter, BorderLayout.SOUTH);

        // --- JALANKAN THREAD ---
        new Thread(this::updateDataLoop).start();
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                isRunning = false;
            }
        });

        setVisible(true);
    }

    private void updateDataLoop() {
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        while (isRunning) {
            try {
                Connection conn = KoneksiDB.configDB();
                if (conn != null) {
                    // SQL Tetap sama (LEFT JOIN)
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
                        if (car == null) car = "Unknown Car";
                        
                        double price = rs.getDouble(4);
                        Timestamp time = rs.getTimestamp(5);
                        String timeStr = (time != null) ? dateFmt.format(time) : "N/A";
                        
                        tempDataList.add(new Object[]{
                            id, 
                            buyer != null ? buyer.toUpperCase() : "UNKNOWN", 
                            car, 
                            fmt.format(price), 
                            timeStr
                        });
                    }

                    conn.close();

                    SwingUtilities.invokeLater(() -> {
                        tableModel.setRowCount(0);
                        for (Object[] row : tempDataList) {
                            tableModel.addRow(row);
                        }
                        lblTotal.setText("Total Units Sold: " + tempDataList.size() + "   ");
                    });
                }
                Thread.sleep(3000); 
            } catch (Exception e) {
                System.out.println("Monitor Error: " + e.getMessage());
            }
        }
    }
}