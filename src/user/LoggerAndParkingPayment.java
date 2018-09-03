package user;
import admin.*;
import vehicles.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.Level;

public class LoggerAndParkingPayment {
    
    public static HashMap<String,LocalDateTime> entryDate=new HashMap<>();
    static PrintWriter printTickets;
    static final Logger LOGGER = Logger.getLogger("Logger");
    static FileHandler handler;
    
    static{
        try{
            handler=new FileHandler(System.getProperty("user.home")+"/Documents/GarageFiles/error.log");
            LOGGER.addHandler(handler);
            printTickets=new PrintWriter(new File(System.getProperty("user.home")+"/Documents/GarageFiles/Naplata parkinga.csv"));
            printTickets.append(new StringBuilder().append("sep=,"+System.getProperty("line.separator")));
            printTickets.append(new StringBuilder().append("Registarski broj"+','+"Sati"+','+"Platiti[km]"+System.getProperty("line.separator")));
        }catch(Exception e){}
        deserializeTime();
            
        for(int i=1;i<=Garage.platforms.size();++i)
                Garage.getPlatformVehicles(i).forEach(x -> {if(x.licensePlate!=null && entryDate.containsKey(x.licensePlate)==false) entryDate.put(x.licensePlate, LocalDateTime.now());});
       
    }
    public static void serializeTime()
    {
        try (FileOutputStream fileOut = new FileOutputStream(System.getProperty("user.home")+"/Documents/GarageFiles/time.ser"); 
                ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                out.writeObject(entryDate);
        }catch(Exception e){ LoggerAndParkingPayment.setErrorLog(e); }
    }
    public static void deserializeTime()
    {
        try (FileInputStream fileIn = new FileInputStream(System.getProperty("user.home")+"/Documents/GarageFiles/time.ser"); 
                ObjectInputStream in = new ObjectInputStream(fileIn)) {
               entryDate = (HashMap<String,LocalDateTime>) in.readObject();
        }catch(Exception e){ LoggerAndParkingPayment.setErrorLog(e); }
    }
    
    public static void setParkingPayment(Vehicle vehicle) throws Exception
    {
        if(vehicle.type.contains("Policijski") || vehicle.type.contains("Sanitetski") || vehicle.type.contains("Vatrogasni") || vehicle.licensePlate==null)
            return;
     
        LocalDateTime now=LocalDateTime.now();
        int toPay=1;
        int hours=0;
        hours+=24*(now.getDayOfMonth()-entryDate.get(vehicle.licensePlate).getDayOfMonth());
        hours+=now.getHour()-entryDate.get(vehicle.licensePlate).getHour();
        
        if(hours<1) toPay=1;
        else if(hours<3) toPay=2;
        else toPay=8;
        
        entryDate.remove(vehicle.licensePlate);
       
        StringBuilder sb = new StringBuilder();
        sb.append(vehicle.licensePlate+','+" >"+String.valueOf(hours)+','+String.valueOf(toPay)+System.getProperty("line.separator"));
        printTickets.append(sb.toString());
    }
    
    public static void setErrorLog(Exception exception)
    {
        StackTraceElement elements[] = exception.getStackTrace();
	for (StackTraceElement element:elements) 
            LOGGER.log(Level.WARNING, element.toString());
    }
}
