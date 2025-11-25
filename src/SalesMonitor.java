import java.sql.*;
import javax.swing.JLabel;

public class SalesMonitor extends Thread {
    private JLabel label;
    private volatile boolean running = true;

    public SalesMonitor(JLabel label) { this.label = label; }

    @Override
    public void run() {
        while (running) {
            try {
                Connection conn = KoneksiDB.configDB();
                if (conn != null) {
                    Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM sales");
                    if (rs.next()) {
                        int sold = rs.getInt(1);
                        label.setText(" LIVE MONITOR: " + sold + " Unit(s) Sold Globally");
                    }
                    conn.close();
                }
                Thread.sleep(4000); // Update tiap 4 detik
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}