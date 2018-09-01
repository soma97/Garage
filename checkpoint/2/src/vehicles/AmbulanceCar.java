package vehicles;

public class AmbulanceCar extends Car {

    boolean rotationIsOn;
    final int priority = 3;

    public AmbulanceCar(String name, String license, String chassis, String engine, String image, int doors) {
        super(name, license, chassis, engine, image, doors);
        type="Sanitetski automobil";
    }
}