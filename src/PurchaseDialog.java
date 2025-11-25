import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;

public class PurchaseDialog extends JDialog {
    private Car car;
    private String user;

    public PurchaseDialog(JFrame parent, Car car, String user) {
        super(parent, "Confirm Purchase", true);
        this.car = car; this.user = user;
        setSize(400, 300); setLocationRelativeTo(parent); setLayout(new GridLayout(5,1));
        
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        PaymentInterface pay = (PaymentInterface) car;

        add(new JLabel("MODEL: " + car.getModel(), SwingConstants.CENTER));
        add(new JLabel("PRICE: " + fmt.format(car.getBasePrice()), SwingConstants.CENTER));
        add(new JLabel("TAX: " + fmt.format(pay.calculateTax()), SwingConstants.CENTER));
        
        JLabel lTotal = new JLabel("TOTAL: " + fmt.format(pay.calculateTotal()), SwingConstants.CENTER);
        lTotal.setForeground(Color.BLACK); lTotal.setFont(new Font("Arial", Font.BOLD, 20));
        add(lTotal);

        JButton btn = new JButton("PAY NOW");
        btn.setBackground(Color.RED); btn.setForeground(Color.WHITE);
        btn.addActionListener(e -> process());
        add(btn); setVisible(true);
    }

    private void process() {
        try {
            Connection conn = KoneksiDB.configDB();
            conn.createStatement().executeUpdate("INSERT INTO sales (car_id, buyer_name) VALUES ("+car.getId()+", '"+user+"')");
            conn.createStatement().executeUpdate("UPDATE cars SET stock = stock - 1 WHERE id="+car.getId());
            JOptionPane.showMessageDialog(this, "Congratulation! Transaction Success.");
            dispose();
        } catch(Exception e) { e.printStackTrace(); }
    }
}