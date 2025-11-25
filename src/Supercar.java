public class Supercar extends Car implements PaymentInterface {
    // Tarif PPN 11% dan PPnBM 75%
    private final double PPN = 0.11;
    private final double PPNBM = 0.75; 

    public Supercar(int id, String brand, String model, double price) {
        super(id, brand, model, price);
    }

    @Override
    public String getEngineSound() {
        return "V8/V10 Sport Tuned Exhaust!";
    }

    @Override
    public double calculateTax() {
        // Total Pajak = (Harga * PPN) + (Harga * PPnBM)
        return (this.basePrice * PPN) + (this.basePrice * PPNBM);
    }

    @Override
    public double calculateTotal() {
        return this.basePrice + calculateTax();
    }

    @Override
    public String getSpecs() {
        return "Tipe: SUPERCAR\n" +
               "Mesin: High Performance (V8/V10)\n" +
               "Pajak (PPN+PPnBM): 86%"; 
    }
}