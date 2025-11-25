public abstract class Car {
    protected int id;
    protected String brand, model;
    protected double basePrice;

    public Car(int id, String brand, String model, double price) {
        this.id=id; this.brand=brand; this.model=model; this.basePrice=price;
    }

    public abstract String getEngineSound();

    public String getBrand() {
         return brand; 
    }

    public String getModel() { 
        return model; 
    }

    public double getBasePrice() { 
        return basePrice; 
    }
    
    public int getId() { 
        return id; 
    }
}