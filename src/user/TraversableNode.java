package user;
import admin.*;

import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class TraversableNode{
    int i,j;
    Label label;
    TraversableNode toExitPlatform,toFindParking,toNextPlatform,leftLane;
    
    
    public TraversableNode(String type,int i,int j)
    {
        this.i=i;
        this.j=j;
        label=new Label(" "+type);
        label.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD,30));
        if(Garage.getPlatform(1).matrix[i][j].isParkingPlace)
            label.setStyle("-fx-text-fill: orange;");
    }
    public void setLabel(String type)
    {
        label.setText(type);
    }
}
