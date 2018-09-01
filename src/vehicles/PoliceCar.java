package vehicles;

public class PoliceCar extends Car implements Police {

    boolean rotationIsOn;
    final int priority = 1;

    public PoliceCar(String name, String license, String chassis, String engine, String image, int doors) {
        super(name, license, chassis, engine, image, doors);
        type="Policijski automobil";
    }
}