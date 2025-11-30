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
    
    // Konfigurasi Waktu & Transisi
    private int displayTime = 4000; 
    private int fadeDuration = 1200; 
    private float alpha = 0.0f; 
    private boolean isFading = false;
    private boolean enableUpscaling = true;

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
            slideshowThread.interrupt(); 
        }
    }

    private void applyHighQualityUpscaling(Graphics2D g2d) {
        if (enableUpscaling) {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                // 1. FASE DIAM
                isFading = false;
                alpha = 0.0f;
                repaint();
                Thread.sleep(displayTime); 

                // 2. FASE TRANSISI (ULTRA SMOOTH FADE)
                if (gifList.size() > 1) {
                    nextIdx = (currentIdx + 1) % gifList.size();
                    isFading = true;

                    int fps = 60;
                    int steps = (fadeDuration / 1000) * fps; 
                    if (steps < 1) steps = 1;
                    long stepDelay = fadeDuration / steps; 

                    for (int i = 0; i <= steps; i++) {
                        if (!isRunning) break;
                        float t = (float) i / steps;
                        // Rumus SmoothStep (Ease-In-Out)
                        alpha = t * t * (3 - 2 * t);
                        repaint();
                        Thread.sleep(stepDelay);
                    }
                    currentIdx = nextIdx; 
                    isFading = false;
                }
            } catch (InterruptedException e) {
                break; 
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // 1. PANGGIL SUPER (Penting untuk reset)
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        
        // 2. BERSIHKAN LAYAR DENGAN WARNA HITAM PEKAT (Mencegah Ghosting)
        // Ini akan menghapus sisa-sisa frame sebelumnya
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Aktifkan Upscaling
        applyHighQualityUpscaling(g2d);

        if (!gifList.isEmpty() && currentIdx < gifList.size()) {
            int w = getWidth();
            int h = getHeight();

            Image imgOld = gifList.get(currentIdx);
            if (imgOld != null) {
                // Gambar Frame Saat Ini
                g2d.drawImage(imgOld, 0, 0, w, h, this);
            }

            // ... (Kode transisi fade tetap sama di bawah ini) ...
            if (isFading && nextIdx < gifList.size()) {
                Image imgNew = gifList.get(nextIdx);
                if (imgNew != null) {
                    float safeAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, safeAlpha));
                    g2d.drawImage(imgNew, 0, 0, w, h, this);
                }
            }

            // Reset Composite & Overlay Gelap
            g2d.setComposite(AlphaComposite.SrcOver);
            g2d.setColor(new Color(0, 0, 0, 100)); 
            g2d.fillRect(0, 0, w, h);
            
        } else {
            g.setColor(Color.WHITE);
            g.drawString("No Signal", getWidth()/2 - 30, getHeight()/2);
        }
        
        // SANGAT PENTING:
        // Toolkit.createImage kadang butuh 'bantuan' agar animasinya jalan mulus di Swing
        Toolkit.getDefaultToolkit().sync(); 
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