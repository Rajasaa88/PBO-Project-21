import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class ModernButton extends JButton {
    private Color baseColor;
    private boolean isHovering = false;
    private boolean isPressed = false;

    public ModernButton(String text, Color themeColor) {
        super(text);
        this.baseColor = themeColor;
        
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        
        setForeground(Color.WHITE);
        // Default Font (bisa diubah dari luar)
        setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { isHovering = true; repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { isHovering = false; repaint(); }
            @Override
            public void mousePressed(MouseEvent e) { isPressed = true; repaint(); }
            @Override
            public void mouseReleased(MouseEvent e) { isPressed = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(); int h = getHeight();

        // Background Glass (Hitam Transparan)
        Color colorStart = new Color(0, 0, 0, 150); 
        Color colorEnd = new Color(0, 0, 0, 100);
        
        // Hover Effect (Warna Tema)
        if (isHovering) {
            colorStart = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 200);
            colorEnd = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 100);
        }
        if (isPressed) {
            colorStart = baseColor.darker();
            colorEnd = baseColor.darker().darker();
        }

        GradientPaint gp = new GradientPaint(0, 0, colorStart, 0, h, colorEnd);
        g2.setPaint(gp);
        // Radius sudut sedikit disesuaikan agar pas untuk tombol kecil maupun besar
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 20, 20)); 

        // Border Neon
        if (isHovering) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.5f));
        } else {
            g2.setColor(baseColor);
            g2.setStroke(new BasicStroke(1.5f));
        }
        g2.draw(new RoundRectangle2D.Double(1, 1, w-2, h-2, 20, 20));

        // Efek Kilau (Shine)
        if (!isPressed) {
            g2.setPaint(new GradientPaint(0, 0, new Color(255,255,255,30), 0, h/2, new Color(255,255,255,0)));
            g2.fill(new RoundRectangle2D.Double(2, 2, w-4, h/2, 15, 15));
        }

        super.paintComponent(g2);
        g2.dispose();
    }
}