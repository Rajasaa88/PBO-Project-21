import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ThreadGifPanel extends JPanel implements Runnable {
    private List<Image> gifList = new ArrayList<>();
    private int currentIdx = 0;
    private int nextIdx = 0;
    
    private Thread slideshowThread;
    private volatile boolean isRunning = false;
    
    // Konfigurasi Waktu
    private int displayTime = 4000; // Waktu gambar diam (4 detik)
    private int fadeDuration = 1000; // Durasi transisi (1 detik)
    
    // Variabel Transisi
    private float alpha = 0.0f; // Transparansi gambar berikutnya (0.0 - 1.0)
    private boolean isFading = false;

    public ThreadGifPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
    }

    public void startSlideshow(String brandName) {
        stopSlideshow(); 
        gifList.clear();
        currentIdx = 0;
        alpha = 0.0f;
        isFading = false;

        String folderName = getFolderMapping(brandName);
        File folder = new File("src/vid/" + folderName);
        if (!folder.exists()) folder = new File("vid/" + folderName);

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".gif"));
            if (files != null && files.length > 0) {
                for (File f : files) {
                    Image img = Toolkit.getDefaultToolkit().createImage(f.getAbsolutePath());
                    prepareImage(img, this); 
                    gifList.add(img);
                }
            }
        }

        if (!gifList.isEmpty()) {
            isRunning = true;
            slideshowThread = new Thread(this);
            slideshowThread.start();
        } else {
            repaint();
        }
    }

    public void stopSlideshow() {
        isRunning = false;
        if (slideshowThread != null && slideshowThread.isAlive()) {
            slideshowThread.interrupt(); // Stop paksa agar tombol Back responsif
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                // 1. FASE DIAM (Display)
                isFading = false;
                alpha = 0.0f;
                repaint();
                Thread.sleep(displayTime); 

                // 2. PERSIAPAN FASE TRANSISI
                if (gifList.size() > 1) {
                    nextIdx = (currentIdx + 1) % gifList.size();
                    isFading = true;

                    // 3. FASE FADING (Looping kecil untuk mengubah Alpha)
                    // Kita bagi fadeDuration menjadi 20 langkah kecil agar halus
                    int steps = 20; 
                    long stepDelay = fadeDuration / steps; 

                    for (int i = 0; i <= steps; i++) {
                        if (!isRunning) break;

                        alpha = (float) i / steps; // Naikkan alpha perlahan (0.0 -> 1.0)
                        repaint(); // Gambar ulang dengan alpha baru
                        Thread.sleep(stepDelay);
                    }

                    // 4. SELESAI TRANSISI
                    currentIdx = nextIdx; // Gambar berikutnya resmi jadi gambar sekarang
                    isFading = false;
                }

            } catch (InterruptedException e) {
                break; // Stop thread jika di-interrupt (tombol Back)
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Ubah Graphics jadi Graphics2D agar bisa main transparansi
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!gifList.isEmpty() && currentIdx < gifList.size()) {
            int w = getWidth();
            int h = getHeight();

            // 1. Gambar Layer Bawah (Gambar Saat Ini) - Selalu Solid
            Image img1 = gifList.get(currentIdx);
            if (img1 != null) {
                g2d.drawImage(img1, 0, 0, w, h, this);
            }

            // 2. Gambar Layer Atas (Gambar Berikutnya) - Transparan
            if (isFading && nextIdx < gifList.size()) {
                Image img2 = gifList.get(nextIdx);
                if (img2 != null) {
                    // Set transparansi sesuai variabel 'alpha'
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    g2d.drawImage(img2, 0, 0, w, h, this);
                }
            }

            // 3. Reset Composite (Agar Overlay Hitam tidak ikutan transparan)
            g2d.setComposite(AlphaComposite.SrcOver);

            // 4. Overlay Gelap (Agar tulisan terbaca)
            g2d.setColor(new Color(0, 0, 0, 100)); 
            g2d.fillRect(0, 0, w, h);
        } else {
            g.setColor(Color.WHITE);
            g.drawString("No Signal", getWidth()/2 - 30, getHeight()/2);
        }
    }

    private String getFolderMapping(String dbBrandName) {
        String name = dbBrandName.toLowerCase();
        if (name.contains("aston")) return "aston";
        if (name.contains("mercedes") || name.contains("merc")) return "merc";
        if (name.contains("lamborghini")) return "lambogirni"; 
        if (name.contains("koenigsegg")) return "koenigsegg";
        return name; 
    }
}