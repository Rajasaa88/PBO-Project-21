import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class KoneksiDB {
    
    public static Connection configDB() throws SQLException {
        try {
            String url = "jdbc:mysql://localhost:3306/showroom_sportcar";
            String user = "root";
            String pass = "";
            
            // --- BAGIAN INI YANG DIPERBAIKI ---
            // Menggunakan Class.forName (Cara Modern & Aman)
            // Ini tidak akan merah karena dianggap teks biasa oleh editor
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            return DriverManager.getConnection(url, user, pass);
            
        } catch (Exception e) { 
            // Kita ganti 'SQLException' jadi 'Exception' agar bisa menangkap error Driver juga
            System.err.println("Koneksi Gagal: " + e.getMessage());
            return null;
        }
    }
}