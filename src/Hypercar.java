public class Hypercar extends Car implements PaymentInterface {
    // Tarif PPN 11% dan PPnBM 95% (Maksimal)
    private final double PPN = 0.11;
    private final double PPNBM = 0.95;

    public Hypercar(int id, String brand, String model, double price) {
        super(id, brand, model, price);
    }

    @Override
    public String getEngineSound() {
        return "W16 Quad-Turbo Monster!";
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
        return "Tipe: HYPERCAR\n" +
               "Mesin: Engineering Masterpiece (W16/Hybrid)\n" +
               "Pajak (PPN+PPnBM): 106%";
    }
}