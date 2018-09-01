package admin;
import user.*;
import vehicles.*;

import java.io.Serializable;
import java.util.Random;

public class Platform implements Serializable
{
    public PlatformPlace[][] matrix=new PlatformPlace[10][8]; 
    public int number;
    public int freeSpace;
    public Platform()
    {
        freeSpace=28;
        for(int i=0;i<10;++i)
            for(int j=0;j<8;++j)
            {
                matrix[i][j]=new PlatformPlace();
            }
        number=Garage.platforms.size()+1;
        for(int i=2;i<8;i++)
        {
            matrix[i][0].isParkingPlace=true;
            matrix[i][3].isParkingPlace=true;
            matrix[i][4].isParkingPlace=true;
            matrix[i][7].isParkingPlace=true;
        }
        for(int i=8;i<10;++i)
        {
            matrix[i][0].isParkingPlace=true;
            matrix[i][7].isParkingPlace=true;
        }
    }
    
    public boolean setCarOnFreeSpace(Vehicle vehicle)
    {
        if(freeSpace==0) return false;
        for(int i=2;i<10;++i)
            for(int j=0;j<8;++j)
            {
                if(matrix[i][j].isFree && matrix[i][j].isParkingPlace)
                {
                    if(i<6 && j<4 && new Random().nextInt(10)<8) continue;//sluzi za postavljanje vozila na random mjestu
                    freeSpace--;
                    matrix[i][j].isFree=false;
                    matrix[i][j].vehicle=vehicle;
                    return true;
                }
            }
        return false;
    }
    public boolean setVehicleOnStart(Vehicle vehicle)
    {
        if(matrix[1][0].isFree)
        {
            matrix[1][0].isFree=false;
            matrix[1][0].vehicle=vehicle;
            
            if(matrix[1][0].vehicle.type.contains("Policijski"))
                UserProgram.platformSimulation.getTraversePlatform(number).matrix[1][0].setLabel(" P");
            else if(matrix[1][0].vehicle.type.contains("Sanitetski"))
                UserProgram.platformSimulation.getTraversePlatform(number).matrix[1][0].setLabel(" H");
            else if(matrix[1][0].vehicle.type.contains("Vatrogasni"))
                UserProgram.platformSimulation.getTraversePlatform(number).matrix[1][0].setLabel(" F");
            else UserProgram.platformSimulation.getTraversePlatform(number).matrix[1][0].setLabel(" V");
 
            return true;
        }
        return false;
    }
    
}
    
