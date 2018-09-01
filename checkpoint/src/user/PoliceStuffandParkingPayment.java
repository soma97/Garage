package user;
import admin.*;
import vehicles.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.time.LocalDateTime;

public class PoliceStuffandParkingPayment {
    
    static HashMap<Vehicle,LocalDateTime> entryDate=new HashMap<>();
    static PrintWriter printTickets;
    
    static{
        try{
            printTickets=new PrintWriter(new File(System.getProperty("user.home")+"/Documents/GarageFiles/Naplata parkinga.csv"));
            printTickets.append(new StringBuilder().append("sep=,"+System.getProperty("line.separator")));
            printTickets.append(new StringBuilder().append("Registarski broj"+','+"Sati"+','+"Platiti[km]"+System.getProperty("line.separator")));
        }catch(Exception e){}
        for(int i=1;i<=Garage.platforms.size();++i)
            Garage.getPlatformVehicles(i).forEach(x -> PoliceStuffandParkingPayment.entryDate.put(x, LocalDateTime.now()));
        
    }
    
    public static void setParkingPayment(Vehicle vehicle) throws Exception
    {
        if(vehicle.type.contains("Policijski") || vehicle.type.contains("Sanitetski") || vehicle.type.contains("Vatrogasni"))
            return;
        
        LocalDateTime now=LocalDateTime.now();
        int toPay=0;
        int hours=0;
        hours+=24*(now.getDayOfMonth()-entryDate.get(vehicle).getDayOfMonth());
        hours+=now.getHour()-entryDate.get(vehicle).getHour();
        
        if(hours<=1) toPay=1;
        else if(hours<=3) toPay=2;
        else toPay=8;
        
        entryDate.remove(vehicle);
       
        StringBuilder sb = new StringBuilder();
        sb.append(vehicle.licensePlate+','+String.valueOf(hours)+','+String.valueOf(toPay)+System.getProperty("line.separator"));
        printTickets.append(sb.toString());
    }
    
    
}
