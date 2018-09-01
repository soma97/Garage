package user;
import admin.*;
import vehicles.*;

import static admin.VehicleInfo.currentImageURI;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.ChoiceBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import java.util.Random;
import javafx.scene.layout.GridPane;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.Node;

public class UserProgram {
    
    public static GridPane gridSimulation=new GridPane(); 
    public static TraversePlatforms platformSimulation;
    public static ArrayList<VehicleMovingSimulation> threads=new ArrayList<>();
    public static ArrayList<CrashSite> crashes=new ArrayList<>();
    public static ArrayList<Integer> stopTraffic=new ArrayList<>(); //sluzi za blokadu saobracaja na platformi gdje je vozilo pod rotacijom (u sustini ovo je implementacija neke vrste semafora)

    public static void showUserProgram()
    {
        Label minVehicleLabel=new Label("Unesite minimalan broj vozila u garazi");
        TextField minVehicleNumber=new TextField();
        Button runUserProgram=new Button("Pokreni simulaciju");
        runUserProgram.setMinSize(150, 60);
        
        VBox verticalBox=new VBox(10);
        verticalBox.setPadding(new Insets(20,20,20,20));
        verticalBox.getChildren().addAll(minVehicleLabel,minVehicleNumber,runUserProgram);
        verticalBox.setAlignment(Pos.CENTER);
        
        Scene userScene=new Scene(verticalBox,300,200);
        Stage userStage=new Stage();
        userStage.setTitle("Korisnicki dio");
        userStage.setScene(userScene);
        userStage.show();
        
        runUserProgram.setOnAction(e -> {  
            try{ 
                setMinimumVehicles(Integer.parseInt(minVehicleNumber.getText()));
                userStage.close();
                userSimulation();
            }catch(Exception ex){
             LoggerAndParkingPayment.setErrorLog(ex);
             String message="Unijeli ste neispravan karakter,pokusajte ponovo";
             if(ex.getMessage().contains("maksimalnog"))
                message=ex.getMessage();
             Stage alert=new Stage();
             alert.setTitle("Greska");
             Label alertLabel=new Label(message);
             alertLabel.setAlignment(Pos.CENTER);
             Scene scene=new Scene(alertLabel,300,100);
             alert.setScene(scene);
             alert.show();
        }
        });
    }
    
    private static void setMinimumVehicles(int number) throws Exception
    {
        if(number>Garage.platforms.size()*28)
            throw new Exception("Unijeli ste broj veci od maksimalnog broja mjesta");
        
        int numberOfVehiclesToSet=number;
        
        for(Platform x:Garage.platforms)
           numberOfVehiclesToSet -= Garage.getPlatformVehicles(x.number).size();
        
        
        Random random=new Random();
        String vehicleName="RandomGenerated";
        String license="RandomLicense";
        
        
        for(int i=0;i<numberOfVehiclesToSet;++i)
        {
            while(true)
            {
                GUI.currentPlatform=new Random().nextInt(Garage.platforms.size())+1;
                if(Garage.getPlatform(GUI.currentPlatform).freeSpace>0) break;
            }
            
            if(random.nextInt(10)==0)
            {
                switch(random.nextInt(6))
                {
                  case 0:{Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new AmbulanceVan(vehicleName+String.valueOf(i),license+String.valueOf(random.nextInt(10000)),"//","//",null,0));break;} 
                  case 1:{Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new PoliceVan(vehicleName+String.valueOf(i),license+String.valueOf(random.nextInt(10000)),"//","//",null,0)); break;}
                  case 2:{Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new FireTruck(vehicleName+String.valueOf(i),license+String.valueOf(random.nextInt(10000)),"//","//",null,0)); break;}  
                  case 3:{Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new AmbulanceCar(vehicleName+String.valueOf(i),license+String.valueOf(random.nextInt(10000)),"//","//",null,0));break;}  
                  case 4:{Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new PoliceCar(vehicleName+String.valueOf(i),license+String.valueOf(random.nextInt(10000)),"//","//",null,0));break;}
                  case 5:{Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new PoliceBike(vehicleName+String.valueOf(i),license+String.valueOf(random.nextInt(10000)),"//","//",null)); break;}
                }
            }
            else
            {
                switch(random.nextInt(3))
                {
                    case 0:{Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new Van(vehicleName+String.valueOf(i),license+String.valueOf(random.nextInt(10000)),"//","//",null,0)); break;}
                    case 1:{Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new Car(vehicleName+String.valueOf(i),license+String.valueOf(random.nextInt(10000)),"//","//",null,0)); break;}
                    case 2:{Garage.getPlatform(GUI.currentPlatform).setCarOnFreeSpace(new Bike(vehicleName+String.valueOf(i),license+String.valueOf(random.nextInt(10000)),"//","//",null)); break;}
                }
            }
        }
    }
    
    
    public static void userSimulation()
    {
        platformSimulation=new TraversePlatforms();
        Label platformLabel=new Label("Trenutno se prikazuje platforma sa rednim brojem 1");
        Button addVehicle=new Button("Dodaj vozilo");
        addVehicle.setPrefSize(120, 60);
        ComboBox comboBox=new ComboBox<>();
        comboBox.setPromptText("Izaberite platformu");
        for(int i=0;i<Garage.platforms.size();++i)
            comboBox.getItems().add(String.valueOf(i+1));
        
        ChoiceBox<String> choice=new ChoiceBox<>();
        choice.getItems().add("Sanitetski kombi");
        choice.getItems().add("Policijski kombi");
        choice.getItems().add("Vatrogasni kombi");
        choice.getItems().add("Civilni kombi");
        
        choice.getItems().add("Sanitetski automobil");
        choice.getItems().add("Policijski automobil");
        choice.getItems().add("Civilni automobil");
        choice.setValue("Civilni automobil");
        
        choice.getItems().add("Policijski motocikl");
        choice.getItems().add("Civilni motocikl");
        
        VBox topVBox=new VBox(10);
        topVBox.setPadding(new Insets(20,20,20,20));
        topVBox.getChildren().addAll(comboBox,platformLabel);
        
        VBox bottomVBox=new VBox(10);
        bottomVBox.setPadding(new Insets(20,20,20,20));
        bottomVBox.getChildren().addAll(choice,addVehicle);
        bottomVBox.setAlignment(Pos.BOTTOM_CENTER);
        
        initializeGrid();
        
        BorderPane layout=new BorderPane();
        layout.setTop(topVBox);
        layout.setCenter(gridSimulation);
        layout.setBottom(bottomVBox);
        
        Scene scene=new Scene(layout,800,600);
        Stage window=new Stage();
        window.setTitle("Simulacija");
        window.setScene(scene);
        window.show();
        
        showPlatformSimulation(1);
        
        chooseAndExit();     
        
        Runnable r = () -> {
            while(true)
            {
            if(threads.isEmpty())
               { 
                   Garage.serialize();
                   LoggerAndParkingPayment.printTickets.close();
                   LoggerAndParkingPayment.serializeTime();
                   javafx.application.Platform.runLater(() -> window.close());
                   break;
               }
            else try{ Thread.sleep(2000);}catch(Exception e){}
            }
        };
        Thread closerThread = new Thread(r);
        closerThread.setDaemon(true);
        closerThread.start();
        
        comboBox.setOnAction(e -> { platformLabel.setText("Trenutno se prikazuje platforma sa rednim brojem "+comboBox.getSelectionModel().getSelectedItem().toString());
            showPlatformSimulation(Integer.parseInt(comboBox.getSelectionModel().getSelectedItem().toString()));});
        
        addVehicle.setOnAction(e -> addVehicleOnStart(choice.getValue()));
    }
    
    
    private static void showPlatformSimulation(int platformNumber)
    {
        gridSimulation.getChildren().removeAll(gridSimulation.getChildren().stream().map((Node s) ->{if(s.getClass()==Label.class) return s; 
                                                                                                        else return null;}).collect(Collectors.toList()));
        Traverse x=platformSimulation.getTraversePlatform(platformNumber);
        
        for(int i=0;i<10;++i)
            for(int j=0;j<8;++j)
                gridSimulation.add(x.matrix[i][j].label, j, i);
    }
    
    private static void initializeGrid()
    {
        gridSimulation.setAlignment(Pos.CENTER);
        gridSimulation.setGridLinesVisible(true);
        for(int i=0;i<8;++i)
        {
            ColumnConstraints columnConst=new ColumnConstraints();
            columnConst.setPercentWidth(100.0/16);
            gridSimulation.getColumnConstraints().add(columnConst);
        }
        for(int i=0;i<10;++i)
        {
            RowConstraints rowConst=new RowConstraints();
            rowConst.setPercentHeight(10.0);
            gridSimulation.getRowConstraints().add(rowConst);
        }
    }
    
    private static void addVehicleOnStart(String type)
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
        
        
        choosePhoto.setOnAction( e -> VehicleInfo.photoChooser(window));
       
        add.setOnAction(e -> { 
            try{
            switch(type)
            {       
                case "Sanitetski kombi": { if(Garage.getPlatform(1).setVehicleOnStart(new AmbulanceVan(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI,Double.parseDouble(loadCapField.getText())))==false) throw new Exception("wait"); break;} 
                case "Policijski kombi": { if(Garage.getPlatform(1).setVehicleOnStart(new PoliceVan(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI,Double.parseDouble(loadCapField.getText())))==false) throw new Exception("wait"); break;}
                case "Vatrogasni kombi": { if(Garage.getPlatform(1).setVehicleOnStart(new FireTruck(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI,Double.parseDouble(loadCapField.getText())))==false) throw new Exception("wait"); break;}
                case "Civilni kombi": { if(Garage.getPlatform(1).setVehicleOnStart(new Van(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI,Double.parseDouble(loadCapField.getText())))==false) throw new Exception("wait"); break;}  
                case "Sanitetski automobil": { if(Garage.getPlatform(1).setVehicleOnStart(new AmbulanceCar(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI,Integer.parseInt(doorNumberField.getText())))==false) throw new Exception("wait"); break;}  
                case "Policijski automobil": { if(Garage.getPlatform(1).setVehicleOnStart(new PoliceCar(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI,Integer.parseInt(doorNumberField.getText())))==false) throw new Exception("wait"); break;}
                case "Civilni automobil": { if(Garage.getPlatform(1).setVehicleOnStart(new Car(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI,Integer.parseInt(doorNumberField.getText())))==false) throw new Exception("wait"); break;}
                case "Policijski motocikl": { if(Garage.getPlatform(1).setVehicleOnStart(new PoliceBike(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI))==false) throw new Exception("wait"); break;}
                case "Civilni motocikl": { if(Garage.getPlatform(1).setVehicleOnStart(new Bike(nameField.getText(),licenseField.getText(),chassisField.getText(),engineField.getText(),currentImageURI))==false) throw new Exception("wait"); break;}
            }
            window.close();currentImageURI=null; 
            VehicleMovingSimulation vehicleMove=new VehicleMovingSimulation(true,1,platformSimulation.getTraversePlatform(1).matrix[1][0]);
            vehicleMove.setDaemon(true);
            threads.add(vehicleMove);
            vehicleMove.start();
        }
        catch(Exception ex)
        {
            LoggerAndParkingPayment.setErrorLog(ex);
            String message="Jedno ili vise polja nisu korektno unesena!";
            if(ex.getMessage().contains("wait"))
                message="Vozilo se trenutno nalazi na trazenoj poziciji,pokusajte ponovo";
            Stage alert=new Stage();
            alert.setTitle("Greska");
            Label alertLabel=new Label(message);
            alertLabel.setAlignment(Pos.CENTER);
            Scene scene=new Scene(alertLabel,350,100);
            alert.setScene(scene);
            alert.show();
        }});
        
        info.setAlignment(Pos.CENTER);
        Scene scene=new Scene(info,600,600);
        window.setScene(scene);
        window.show();
    }
    
    private static void chooseAndExit()
    {
        Random random=new Random();
        for(Traverse x:UserProgram.platformSimulation.traversePlatforms)
        {
            if(Garage.getPlatform(x.traversePlatformNumber).freeSpace==28) continue;
            for(int j=0;j<8;++j)
                for(int i=2;i<10;++i)
                {
                        if(Garage.getPlatform(x.traversePlatformNumber).matrix[i][j].isParkingPlace && Garage.getPlatform(x.traversePlatformNumber).matrix[i][j].vehicle!=null)
                            if(random.nextInt(6)==0)
                            {
                                VehicleMovingSimulation vehicleMove=new VehicleMovingSimulation(false,x.traversePlatformNumber,platformSimulation.getTraversePlatform(x.traversePlatformNumber).matrix[i][j]);
                                vehicleMove.setDaemon(true);
                                threads.add(vehicleMove);
                                vehicleMove.start();
                            }
                }
        }
    }
}



