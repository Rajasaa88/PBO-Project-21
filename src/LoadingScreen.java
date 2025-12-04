import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.Connection;

public class LoadingScreen extends JWindow {
    private JProgressBar progressBar;
    private JLabel lblStatus;
    
    public LoadingScreen() {
        // Setup Tampilan (Transparan & Tengah Layar)
        setSize(600, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // 1. Background Image
        BackgroundPanel bgPanel = new BackgroundPanel("img/main.jpg");
        bgPanel.setLayout(new BorderLayout());
        bgPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 200), 2)); // Border Neon

        // 2. Content (Judul & Logo)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("AutoAAR");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 50));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblSub = new JLabel("PREMIUM SHOWROOM MANAGEMENT");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSub.setForeground(new Color(200, 200, 200));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; centerPanel.add(lblTitle, gbc);
        gbc.gridy = 1; centerPanel.add(lblSub, gbc);
        
        bgPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Panel (Progress & Status)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        lblStatus = new JLabel("Initializing system...");
        lblStatus.setForeground(Color.WHITE);
        lblStatus.setFont(new Font("Consolas", Font.ITALIC, 12));
        
        // Custom Styled Progress Bar
        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(500, 10));
        progressBar.setForeground(new Color(0, 200, 255)); // Warna Bar
        progressBar.setBackground(new Color(30, 30, 30));  // Warna Track
        progressBar.setBorderPainted(false);
        
        bottomPanel.add(lblStatus, BorderLayout.NORTH);
        bottomPanel.add(progressBar, BorderLayout.SOUTH);
        
        bgPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(bgPanel);
    }

    public void startApp() {
        setVisible(true);
        // Menjalankan Multithreading Task
        new LoadingWorker().execute();
    }

    // --- WORKER THREAD (PROSES DI BALIK LAYAR) ---
    class LoadingWorker extends SwingWorker<Void, String> {
        @Override
        protected Void doInBackground() throws Exception {
            // FASE 1: Inisialisasi UI (0-30%)
            publish("Loading core components...");
            Thread.sleep(800); // Simulasi load
            setProgress(30);

            // FASE 2: Cek Resource Gambar (30-50%)
            publish("Checking resources...");
            File folder = new File("img");
            if (!folder.exists()) publish("Warning: img folder not found!");
            Thread.sleep(800);
            setProgress(50);

            // FASE 3: Koneksi Database (50-90%)
            // Ini adalah proses REAL (bukan simulasi)
            publish("Connecting to Database...");
            try {
                Connection conn = KoneksiDB.configDB();
                if (conn != null) {
                    publish("Database Connected!");
                    conn.close();
                } else {
                    publish("Database Connection Failed!");
                    Thread.sleep(1000); // Tahan sebentar biar user baca error
                }
            } catch (Exception e) {
                publish("Error: " + e.getMessage());
            }
            Thread.sleep(800); // Biar transisi smooth tidak kaget
            setProgress(90);

            // FASE 4: Finalizing (90-100%)
            publish("Starting application...");
            Thread.sleep(500);
            setProgress(100);
            
            return null;
        }

        @Override // Update UI saat ada progress/pesan baru
        protected void process(java.util.List<String> chunks) {
            String latestStatus = chunks.get(chunks.size() - 1);
            lblStatus.setText(latestStatus);
            progressBar.setValue(getProgress());
        }

        @Override // Saat proses selesai (100%)
        protected void done() {
            dispose(); // Tutup Splash Screen
            // Buka Login Frame
            SwingUtilities.invokeLater(() -> new LoginFrame());
        }
    }

    // --- HELPER UI ---
    class BackgroundPanel extends JPanel {
        private Image bg;
        public BackgroundPanel(String path) { try { bg = new ImageIcon(path).getImage(); } catch(Exception e){} }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(bg!=null) { 
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                g.setColor(new Color(0,0,0,150)); // Overlay gelap
                g.fillRect(0,0,getWidth(),getHeight());
            } else { g.setColor(Color.BLACK); g.fillRect(0,0,getWidth(),getHeight()); }
        }
    }
}