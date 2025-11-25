import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterDialog extends JDialog {
    private JTextField txtUser = new JTextField(15);
    private JPasswordField txtPass = new JPasswordField(15);
    private JPasswordField txtPassConf = new JPasswordField(15);

    public RegisterDialog(JFrame parent) {
        super(parent, "Register New User", true);
        setSize(350, 280);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);

        gbc.gridx=0; gbc.gridy=0; add(new JLabel("Username:"), gbc);
        gbc.gridx=1; gbc.gridy=0; add(txtUser, gbc);
        gbc.gridx=0; gbc.gridy=1; add(new JLabel("Password:"), gbc);
        gbc.gridx=1; gbc.gridy=1; add(txtPass, gbc);
        gbc.gridx=0; gbc.gridy=2; add(new JLabel("Confirm:"), gbc);
        gbc.gridx=1; gbc.gridy=2; add(txtPassConf, gbc);

        JButton btn = new JButton("CREATE ACCOUNT");
        btn.setBackground(Color.ORANGE);
        btn.addActionListener(e -> register());
        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2; add(btn, gbc);
        
        setVisible(true);
    }

    private void register() {
        String u = txtUser.getText();
        String p = new String(txtPass.getPassword());
        String pc = new String(txtPassConf.getPassword());

        if(!p.equals(pc)) { JOptionPane.showMessageDialog(this, "Passwords do not match!"); return; }

        try {
            Connection conn = KoneksiDB.configDB();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=?");
            ps.setString(1, u);
            if(ps.executeQuery().next()) {
                JOptionPane.showMessageDialog(this, "Username already taken!");
            } else {
                conn.createStatement().executeUpdate("INSERT INTO users VALUES ('"+u+"', '"+p+"', 'customer')");
                JOptionPane.showMessageDialog(this, "Success! Please login.");
                dispose();
            }
        } catch(Exception e) { e.printStackTrace(); }
    }
}