package vehicles;

import java.io.File;

public class PoliceBike extends Bike {

    boolean rotationIsOn;
    final int priority = 1;
    File pursuitVehicles;

    public PoliceBike(String name, String license, String chassis, String engine, String image) {
        super(name, license, chassis, engine, image);
        pursuitVehicles = null;
        type="Policijski motocikl";
        pursuitVehicles=new File(System.getProperty("user.home")+"/Documents/GarageFiles/Potjera.txt");
    }
}
