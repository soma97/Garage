package vehicles;

import java.io.File;

public class PoliceCar extends Car {

    boolean rotationIsOn;
    final int priority = 1;
    File pursuitVehicles;

    public PoliceCar(String name, String license, String chassis, String engine, String image, int doors) {
        super(name, license, chassis, engine, image, doors);
        pursuitVehicles = null;
        type="Policijski automobil";
        pursuitVehicles=new File(System.getProperty("user.home")+"/Documents/GarageFiles/Potjera.txt");
    }
}