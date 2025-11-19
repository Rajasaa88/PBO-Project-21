import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        try {
            // Agar tampilan mengikuti tema sistem operasi (opsional)
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}