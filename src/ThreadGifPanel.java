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
    
    // Konfigurasi Visual
    private int displayTime = 4000; 
    private int fadeDuration = 1200; 
    private float alpha = 0.0f; 
    private boolean isFading = false;
    
    // --- SETTING CINEMATIC ---
    private boolean enableCinematicBars = true;
    private double cinematicBarRatio = 0.15; // 15% layar untuk bar Atas (sedikit saya pertebal agar pas untuk Header)

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
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

                // 2. FASE TRANSISI
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
                        alpha = t * t * (3 - 2 * t); 
                        repaint();
                        Thread.sleep(stepDelay);
                    }
                    currentIdx = nextIdx; 
                    isFading = false;
                }
            } catch (InterruptedException e) { break; }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        int w = getWidth();
        int h = getHeight();

        // 1. Clear Screen
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, w, h);

        applyHighQualityUpscaling(g2d);

        if (!gifList.isEmpty() && currentIdx < gifList.size()) {
            // Gambar Utama
            Image imgOld = gifList.get(currentIdx);
            if (imgOld != null) {
                drawImageCover(g2d, imgOld, w, h, 1.0f);
            }

            // Gambar Transisi
            if (isFading && nextIdx < gifList.size()) {
                Image imgNew = gifList.get(nextIdx);
                if (imgNew != null) {
                    float safeAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
                    drawImageCover(g2d, imgNew, w, h, safeAlpha);
                }
            }
            
            drawVignette(g2d, w, h);

        } else {
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("Segoe UI", Font.ITALIC, 18));
            String msg = "Select a Brand to View Gallery";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(msg, (w - fm.stringWidth(msg))/2, h/2);
        }

        // 2. GAMBAR PEMBATAS (HANYA ATAS)
        if (enableCinematicBars) {
            drawCinematicBars(g2d, w, h);
        }
        
        Toolkit.getDefaultToolkit().sync(); 
    }

    // --- HELPER METHODS ---

    private void drawImageCover(Graphics2D g2d, Image img, int panelW, int panelH, float alpha) {
        int imgW = img.getWidth(null);
        int imgH = img.getHeight(null);
        if (imgW <= 0 || imgH <= 0) return;

        double scaleW = (double) panelW / imgW;
        double scaleH = (double) panelH / imgH;
        double scale = Math.max(scaleW, scaleH); 

        int newW = (int) (imgW * scale);
        int newH = (int) (imgH * scale);
        int x = (panelW - newW) / 2;
        int y = (panelH - newH) / 2;

        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.drawImage(img, x, y, newW, newH, null);
        g2d.setComposite(originalComposite);
    }

    private void drawVignette(Graphics2D g2d, int w, int h) {
        RadialGradientPaint p = new RadialGradientPaint(
            new Point(w / 2, h / 2), 
            (float) Math.max(w, h) / 1.2f,
            new float[] { 0.3f, 1.0f }, 
            new Color[] { new Color(0,0,0,0), new Color(0,0,0,150) }
        );
        g2d.setPaint(p);
        g2d.fillRect(0, 0, w, h);
    }

    // --- PERUBAHAN DI SINI (HANYA BAR ATAS) ---
    private void drawCinematicBars(Graphics2D g2d, int w, int h) {
        int barHeight = (int) (h * cinematicBarRatio);
        
        g2d.setColor(Color.BLACK);
        // Bar Atas Saja
        g2d.fillRect(0, 0, w, barHeight);

        // Garis Pembatas Tipis (Aksen Premium di bawah bar hitam)
        g2d.setColor(new Color(40, 40, 40));
        g2d.drawLine(0, barHeight, w, barHeight);
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