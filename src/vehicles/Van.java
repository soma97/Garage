package vehicles;

public class Van extends Vehicle {

    double loadCapacity;

    public Van(String name, String license, String chassis, String engine, String image, double capacity) {
        super(name, license, chassis, engine, image);
        loadCapacity = capacity;
        type="Civilni kombi";
    }
    @Override
    public double getLoadCapacity() {
        return loadCapacity;
    }
}