package vehicles;

public class Bike extends Vehicle {

    public Bike(String name, String license, String chassis, String engine, String image) {
        super(name, license, chassis, engine, image);
        type="Civilni motocikl";
    }
}