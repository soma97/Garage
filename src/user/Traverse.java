package user;
import admin.*;

public class Traverse {
    public TraversableNode matrix[][]=new TraversableNode[10][8];
    public int traversePlatformNumber;
    TraversableNode toLeft,toRight;
    public final Object blockTraffic=new Object(); //sluzi za blokadu saobracaja na platformi u slucaju sudara
    
    Traverse(int platform)
    {
        traversePlatformNumber=platform;
        updateTraversableNodes(platform);
    }
    
    private final void updateTraversableNodes(int platformNumber){
        
        Platform platform=Garage.getPlatform(platformNumber);
        for(int i=0;i<10;++i)
            for(int j=0;j<8;++j)
            {
                if(platform.matrix[i][j].isParkingPlace==true && platform.matrix[i][j].isFree==true)
                    matrix[i][j]=new TraversableNode("*",i,j);
                else if(platform.matrix[i][j].isParkingPlace==false && platform.matrix[i][j].isFree==true)
                    matrix[i][j]=new TraversableNode(" ",i,j);
                else if(platform.matrix[i][j].isFree==false)
                {
                    if(platform.matrix[i][j].vehicle.type.contains("Policijski"))
                        matrix[i][j]=new TraversableNode("P",i,j);
                    else if(platform.matrix[i][j].vehicle.type.contains("Sanitetski"))
                        matrix[i][j]=new TraversableNode("H",i,j);
                    else if(platform.matrix[i][j].vehicle.type.contains("Vatrogasni"))
                        matrix[i][j]=new TraversableNode("F",i,j);
                    else matrix[i][j]=new TraversableNode("V",i,j);
                }
            }
    }
    
    public void setNodesPath(){
        matrix[0][0].toExitPlatform=toLeft;
        matrix[1][7].toNextPlatform=toRight;
        for(int j=1;j<8;++j)
            matrix[0][j].toExitPlatform=matrix[0][j-1];
        for(int j=0;j<7;++j)
            matrix[1][j].toNextPlatform=matrix[1][j+1];
        matrix[1][1].toFindParking=matrix[2][1];
        for(int i=2;i<9;++i)
            matrix[i][1].toFindParking=matrix[i+1][1];
        matrix[9][1].toFindParking=matrix[9][2];
        for(int j=2;j<6;++j)
            matrix[9][j].toFindParking=matrix[9][j+1];
        matrix[9][6].toFindParking=matrix[8][6];
        for(int i=8;i>1;--i)
            matrix[i][6].toFindParking=matrix[i-1][6];
        matrix[1][6].toExitPlatform=matrix[0][6];
        

        for(int j=0;j<8;++j)
        {
            matrix[0][j].leftLane=matrix[1][j];
            matrix[1][j].leftLane=matrix[0][j];
        }
        for(int i=2;i<9;++i)
        {
            matrix[i][1].leftLane=matrix[i][2];
            matrix[i][2].leftLane=matrix[i][1];
        }
        matrix[9][1].leftLane=matrix[8][2];
        for(int j=2;j<6;++j)
        {
            matrix[9][j].leftLane=matrix[8][j];
            matrix[8][j].leftLane=matrix[9][j];
        }
        matrix[9][6].leftLane=matrix[8][5];
        for(int i=8;i>1;--i)
        {
            matrix[i][6].leftLane=matrix[i][5];
            matrix[i][5].leftLane=matrix[i][6];
        }
    }
    
}

