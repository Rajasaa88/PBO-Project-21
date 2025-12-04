import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        // Setup Look and Feel (Optional)
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {}

        // --- UBAH DI SINI ---
        // Jalankan Splash Screen, bukan LoginFrame langsung
        SwingUtilities.invokeLater(() -> {
            LoadingScreen splash = new LoadingScreen();
            splash.startApp();
        });
    }
}