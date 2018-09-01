package admin;
import vehicles.*;

import static admin.GUI.currentPlatform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.io.File;
import java.nio.file.Files;

public class VehicleInfo {
    
    public static String currentImageURI;
    
    public static void display(String type,Vehicle toChange)
    {
        
        Stage window=new Stage();
        window.setTitle("Podaci o vozilu");
        
        Label nameLabel=new Label("Naziv");
        TextField nameField= new TextField();
        nameField.setMaxWidth(250);
        Label chassisLabel=new Label("Broj sasije");
        TextField chassisField= new TextField();
        chassisField.setMaxWidth(250);
        Label engineLabel=new Label("Broj motora");
        TextField engineField= new TextField();
        engineField.setMaxWidth(250);
        Label licenseLabel=new Label("Registarski broj");
        TextField licenseField= new TextField();
        licenseField.setMaxWidth(250);
        Label photoLabel=new Label("Fotografija");
        Button choosePhoto=new Button("Izaberi fotografiju");
        Label doorNumberLabel=new Label("Broj vrata");
        TextField doorNumberField=new TextField();
        doorNumberField.setMaxWidth(250);
        Label loadCapLabel= new Label("Nosivost[kg]");
        TextField loadCapField=new TextField();
        loadCapField.setMaxWidth(250);
        
        Button add=new Button("Potvrdi i izadji");
        add.setPrefSize(120, 40);
        
        VBox info=new VBox(15);
        
        if(type.contains("kombi"))
                info.getChildren().addAll(nameLabel,nameField,chassisLabel,chassisField,engineLabel,engineField,licenseLabel,licenseField,photoLabel,choosePhoto,loadCapLabel,loadCapField,add);
        else if(type.contains("automobil"))
                info.getChildren().addAll(nameLabel,nameField,chassisLabel,chassisField,engineLabel,engineField,licenseLabel,licenseField,photoLabel,choosePhoto,doorNumberLabel,doorNumberField,add);
        else info.getChildren().addAll(nameLabel,nameField,chassisLabel,chassisField,engineLabel,engineField,licenseLabel,licenseField,photoLabel,choosePhoto,add);
        
        if(toChange!=null)
        {
            nameField.setText(toChange.getName());
            chassisField.setText(toChange.getChassisNumber());
            engineField.setText(toChange.getEngineNumber());
            licenseField.setText(toChange.licensePlate);
            currentImageURI=toChange.imageURI;
            if(type.contains("kombi"))
                loadCapField.setText(String.valueOf(((Van)toChange).getLoadCapacity()));
            if(type.contains("automobil"))
                doorNumberField.setText(String.valueOf(((Car)toChange).getDoorNumber()));
        }
        
        choosePhoto.setOnAction( e -> photoChooser(window));
       
        add.setOnAction(e -> { 
            try{
                Garage.deleteCar(toChange,false);
            switch(type)
            {       
                case "Sanitetski kombi": { if(Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new AmbulanceVan(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI,Double.parseDouble(loadCapField.getText())))==false) throw new Exception("Platforma je popunjena!"); break;} 
                case "Policijski kombi": { if(Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new PoliceVan(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI,Double.parseDouble(loadCapField.getText())))==false) throw new Exception("Platforma je popunjena!"); break;}
                case "Vatrogasni kombi": { if(Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new FireTruck(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI,Double.parseDouble(loadCapField.getText())))==false) throw new Exception("Platforma je popunjena!"); break;}
                case "Civilni kombi": { if(Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new Van(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI,Double.parseDouble(loadCapField.getText())))==false) throw new Exception("Platforma je popunjena!"); break;}  
                case "Sanitetski automobil": { if(Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new AmbulanceCar(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI,Integer.parseInt(doorNumberField.getText())))==false) throw new Exception("Platforma je popunjena!"); break;}  
                case "Policijski automobil": { if(Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new PoliceCar(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI,Integer.parseInt(doorNumberField.getText())))==false) throw new Exception("Platforma je popunjena!"); break;}
                case "Civilni automobil": { if(Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new Car(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI,Integer.parseInt(doorNumberField.getText())))==false) throw new Exception("Platforma je popunjena!"); break;}
                case "Policijski motocikl": { if(Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new PoliceBike(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI))==false) throw new Exception("Platforma je popunjena!"); break;}
                case "Civilni motocikl": { if(Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new Bike(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI))==false) throw new Exception("Platforma je popunjena!"); break;}
            }
            window.close(); GUI.table.setItems(Garage.getPlatformVehicles(currentPlatform));currentImageURI=null;  }
        catch(Exception ex)
        {
            String message="Jedno ili vise polja nisu korektno unesena!";
            if(ex.getMessage().contains("popunjena"))
                message=ex.getMessage();
            Stage alert=new Stage();
            alert.setTitle("Greska");
            Label alertLabel=new Label(message);
            alertLabel.setAlignment(Pos.CENTER);
            Scene scene=new Scene(alertLabel,300,100);
            alert.setScene(scene);
            alert.show();
        }});
        
        info.setAlignment(Pos.CENTER);
        Scene scene=new Scene(info,600,600);
        window.setScene(scene);
        window.show();
    }
  
    public static void photoChooser(Stage window)//ova metoda kao i varijabla currentImage napravljeni su zbog nemogucnosti izmjene varijabli unutar lambdi
    {
        FileChooser choose=new FileChooser();
        File path=choose.showOpenDialog(window);
        if(path==null) return;
        File dest = new File(System.getProperty("user.home")+"/Documents/GarageFiles/"+path.getAbsolutePath().substring(path.getAbsolutePath().lastIndexOf('\\')));
        try {
             Files.copy(path.toPath(), dest.toPath());
        } catch (Exception e) { }
        currentImageURI=dest.toURI().toString();
    }
    public static void displayPhoto(String imageURI)
    {
        Image image=new Image(imageURI);
        ImageView imageToShow=new ImageView(image);
        Stage stage=new Stage();
        stage.setTitle("Fotografija");
        VBox box=new VBox(10);
        box.getChildren().add(imageToShow);
        Scene scene=new Scene(box,imageToShow.getFitHeight(),imageToShow.getFitWidth());
        stage.setScene(scene);
        stage.show();
    }
}
