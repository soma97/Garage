package admin;
import vehicles.*;

import java.io.Serializable;

public class PlatformPlace implements Serializable
{
    public boolean isFree;
    public boolean isParkingPlace;
    public Vehicle vehicle;
    PlatformPlace()
    {
        isFree=true;
        isParkingPlace=false;
        vehicle=null;
    }
}
