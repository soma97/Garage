package admin;
import vehicles.*;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.nio.file.Files;
import java.net.URI;
import java.nio.file.Paths;

public class Garage{
    
    public static ArrayList<Platform> platforms=new ArrayList<>();
    
    public static ObservableList<Vehicle> getPlatformVehicles(int platformNumber)
    {
        ObservableList<Vehicle> vehicles=FXCollections.observableArrayList();
        Platform current=getPlatform(platformNumber);
        
        if(current==null) return null;
            
        for(int i=0;i<10;++i)
            for(int j=0;j<8;++j)
            {
                if(current.matrix[i][j].isFree==false)
                    vehicles.add(current.matrix[i][j].vehicle);
            }
        return vehicles;
    }
    
    public static Platform getPlatform(int platformNumber)
    {
        Platform current=null;
        for(Platform x: platforms)
            if(x.number==platformNumber)
            {
                current=x;break;
            }
        return current;
    }
    
    public static int getNextFreePlatformNumber()
    {
        while(getPlatform(GUI.currentPlatform).freeSpace==0)
            GUI.currentPlatform=(GUI.currentPlatform)%Garage.platforms.size()+1;
        return GUI.currentPlatform;
    }
            
    
    public static void deleteCar(Vehicle vehicle,boolean deleteImageFromDir)
    {
        if(vehicle==null) return;
        Platform current=getPlatform(GUI.currentPlatform);
        for(int i=0;i<10;++i)
            for(int j=0;j<8;++j)
            {
                if(current.matrix[i][j].vehicle==vehicle)
                {
                    if(current.matrix[i][j].isParkingPlace)
                        current.freeSpace++;
                    
                    if(deleteImageFromDir)
                    {
                        try{
                            URI uri=new URI(vehicle.imageURI);
                            Files.delete(Paths.get(uri));
                         }catch(Exception e) {}
                    }
                    current.matrix[i][j].isFree=true;
                    current.matrix[i][j].vehicle=null;
                }
            }
    }
    
    public static void serialize()
    {
        try (FileOutputStream fileOut = new FileOutputStream(System.getProperty("user.home")+"/Documents/GarageFiles/garage.ser"); 
                ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                out.writeObject(Garage.platforms);
        }catch(Exception e){System.out.println(e.getMessage());}
    }
    public static void deserialize()
    {
        try (FileInputStream fileIn = new FileInputStream(System.getProperty("user.home")+"/Documents/GarageFiles/garage.ser"); 
                ObjectInputStream in = new ObjectInputStream(fileIn)) {
                Garage.platforms = (ArrayList<Platform>) in.readObject();
        }catch(Exception e){System.out.println(e.getMessage());}
    }
    
}

