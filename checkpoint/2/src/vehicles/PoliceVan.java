package vehicles;

import java.io.File;

public class PoliceVan extends Van {

    boolean rotationIsOn;
    final int priority = 1;
    File pursuitVehicles;

    public PoliceVan(String name, String license, String chassis, String engine, String image, double capacity) {
        super(name, license, chassis, engine, image, capacity);
        pursuitVehicles = null;
        type="Policijski kombi";
    }
}