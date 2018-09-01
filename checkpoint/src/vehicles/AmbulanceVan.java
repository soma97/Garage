package vehicles;

public class AmbulanceVan extends Van {

    boolean rotationIsOn;
    final int priority = 3;

    public AmbulanceVan(String name, String license, String chassis, String engine, String image, double capacity) {
        super(name, license, chassis, engine, image, capacity);
        type="Sanitetski kombi";
    }
}