package vehicles;

public class Car extends Vehicle {
    
    int doorNumber;
    
    public Car(String name, String license, String chassis, String engine, String image, Integer doors) {
        super(name, license, chassis, engine, image);
        doorNumber=doors;
        type="Civilni automobil";
    } 
    @Override
    public int getDoorNumber()
    {
        return doorNumber;
    }
}