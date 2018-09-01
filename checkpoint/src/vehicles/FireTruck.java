package vehicles;

public class FireTruck extends Van {

    boolean rotationIsOn;
    final int priority = 2;

    public FireTruck(String name, String license, String chassis, String engine, String image, double capacity) {
        super(name, license, chassis, engine, image, capacity);
        type="Vatrogasni kombi";
    }
}
