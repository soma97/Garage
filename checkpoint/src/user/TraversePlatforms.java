package user;
import admin.*;

import java.util.ArrayList;

public class TraversePlatforms{
    public ArrayList<Traverse> traversePlatforms=new ArrayList<Traverse>();
    TraversePlatforms()
    {
        for(Platform x:Garage.platforms)
        {
            traversePlatforms.add(new Traverse(x.number));
            if(x.number==1)
                getTraversePlatform(x.number).toLeft=null;
            else
            {
                getTraversePlatform(x.number-1).toRight=getTraversePlatform(x.number).matrix[1][0];
                getTraversePlatform(x.number).toLeft=getTraversePlatform(x.number-1).matrix[0][7];
                getTraversePlatform(x.number).toRight=null;
            }
        }
        for(Traverse x:traversePlatforms)
            x.setNodesPath();
    }
    
    public final Traverse getTraversePlatform(int platformNumber)
    {
        for(Traverse x: traversePlatforms)
            if(x.traversePlatformNumber==platformNumber)
                return x;
        return null;
    }
}

