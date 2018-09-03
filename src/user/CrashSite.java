package user;
import admin.Garage;

import javafx.application.Platform;
import javafx.scene.control.Label;

public class CrashSite {
    
    int i,j,platformNumber;
    public vehicles.Vehicle fireTruck,policeVehicle,ambulanceVehicle;
    int numberOfEmergencyVehicles=0;
    
    public CrashSite(int i,int j,int platformNumber)
    {
        this.i=i;
        this.j=j;
        this.platformNumber=platformNumber;
    }
    
    public void callEmergencyVehicles()
    {
        boolean policeFounded=false,fireTruckFounded=false,ambulanceFounded=false;
        for(int i=0;i<Garage.platforms.size();++i)
        {
            UserProgram.stopTraffic.add(i);
            if(policeFounded==false && platformNumber+i<=Garage.platforms.size())
                if(findEmergencyVehicle("P",platformNumber+i))
                    policeFounded=true;
            if(policeFounded==false && platformNumber-i>0)
                if(findEmergencyVehicle("P",platformNumber-i))
                    policeFounded=true;
            if(fireTruckFounded==false && platformNumber+i<=Garage.platforms.size())
                if(findEmergencyVehicle("F",platformNumber+i))
                    fireTruckFounded=true;
            if(fireTruckFounded==false && platformNumber-i>0)
                if(findEmergencyVehicle("F",platformNumber-i))
                    fireTruckFounded=true;
            if(ambulanceFounded==false && platformNumber+i<=Garage.platforms.size())
                if(findEmergencyVehicle("H",platformNumber+i))
                    ambulanceFounded=true;
            if(ambulanceFounded==false && platformNumber-i>0)
                if(findEmergencyVehicle("H",platformNumber-i))
                    ambulanceFounded=true;
            UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(i));
        }
            
    }
    private synchronized boolean findEmergencyVehicle(String type,int platformNumber)
    {
        for(int i=0;i<10;++i)
            for(int j=0;j<8;++j)
            {
                if(this.platformNumber==platformNumber && i==this.i && j==this.j) continue;
                Label label=UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[i][j].label;
                try{
                if(Garage.getPlatform(platformNumber).matrix[i][j].isFree==false && label.getText().contains("R")==false && label.getText().contains(type))
                {
                    
                    if(type.contains("P")) policeVehicle=Garage.getPlatform(platformNumber).matrix[i][j].vehicle;
                    else if(type.contains("H")) ambulanceVehicle=Garage.getPlatform(platformNumber).matrix[i][j].vehicle;
                    else fireTruck=Garage.getPlatform(platformNumber).matrix[i][j].vehicle;
                            
                    Platform.runLater(()-> label.setText(label.getText()+"R"));
                    numberOfEmergencyVehicles++;
                    
                    if(Garage.getPlatform(platformNumber).matrix[i][j].isParkingPlace)
                    {
                        VehicleMovingSimulation thread=new VehicleMovingSimulation(false,platformNumber,UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[i][j]);
                        UserProgram.threads.add(thread);
                        thread.setDaemon(true);
                        thread.start();
                        thread.crashSite=this;
                    }
                    else{ 
                        for(VehicleMovingSimulation x:UserProgram.threads)
                            if(x.node==UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[i][j])
                                x.crashSite=this;
                        }
                    
                    return true;
                }
                }catch(Exception e){ LoggerAndParkingPayment.setErrorLog(e); }
            }
        return false;
    }
}
