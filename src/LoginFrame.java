import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class LoginFrame extends JFrame {
    JTextField txtUser = new JTextField(15);
    JPasswordField txtPass = new JPasswordField(15);

    public LoginFrame() {
        setTitle("Elite Login"); setSize(350, 200); 
        setDefaultCloseOperation(EXIT_ON_CLOSE); setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(5,5,5,5);
        g.gridx=0; g.gridy=0; add(new JLabel("User:"), g); g.gridx=1; add(txtUser, g);
        g.gridx=0; g.gridy=1; add(new JLabel("Pass:"), g); g.gridx=1; add(txtPass, g);
        
        JPanel p = new JPanel();
        JButton bLog = new JButton("LOGIN"); bLog.addActionListener(e->login());
        JButton bReg = new JButton("REGISTER"); bReg.addActionListener(e->new RegisterDialog(this));
        p.add(bLog); p.add(bReg);
        g.gridx=0; g.gridy=2; g.gridwidth=2; add(p, g);
        setVisible(true);
    }

    private void login() {
        try {
            Connection conn = KoneksiDB.configDB();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
            ps.setString(1, txtUser.getText()); ps.setString(2, new String(txtPass.getPassword()));
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                new AppNavigationFrame(rs.getString("username")); // Login Berhasil
                dispose();
            } else JOptionPane.showMessageDialog(this, "Invalid!");
        } catch(Exception e) { e.printStackTrace(); }
    }
}