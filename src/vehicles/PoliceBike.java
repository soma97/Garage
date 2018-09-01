package vehicles;

public class PoliceBike extends Bike implements Police {

    boolean rotationIsOn;
    final int priority = 1;

    public PoliceBike(String name, String license, String chassis, String engine, String image) {
        super(name, license, chassis, engine, image);
        type="Policijski motocikl";
    }
}
