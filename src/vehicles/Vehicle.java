package vehicles;

import java.io.*;

public class Vehicle implements Serializable{

    String name, chassisNumber, engineNumber;
    public String type,licensePlate,imageURI;
    private static final long serialVersionUID=2397049261030729248L;

    public Vehicle() {}

    public Vehicle(String name, String license, String chassis, String engine, String imageURI) {
        this.name = name;
        licensePlate = license;
        chassisNumber = chassis;
        engineNumber = engine;
        this.imageURI = imageURI;
    }

    public String getName() {
        return name;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getChassisNumber() {
        return chassisNumber;
    }

    public String getEngineNumber() {
        return engineNumber;
    }

    public String getImage() {
        return imageURI;
    }
    
    public String getType(){
        return type;
    }
    
    public int getDoorNumber(){
        return 0;
    }
  
    public double getLoadCapacity() {
        return 0;
    }
}