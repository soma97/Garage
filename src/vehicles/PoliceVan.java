package vehicles;

public class PoliceVan extends Van implements Police {

    boolean rotationIsOn;
    final int priority = 1;

    public PoliceVan(String name, String license, String chassis, String engine, String image, double capacity) {
        super(name, license, chassis, engine, image, capacity);
        type="Policijski kombi";
    }
}