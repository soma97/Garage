package admin;
import user.*;
import vehicles.*;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.ChoiceBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.ComboBox;
import java.io.*;
import java.util.Properties;

public class GUI extends Application{
    
    Stage adminStage;
    public static int currentPlatform=1;
    static TableView<Vehicle> table=new TableView<>();
    
    public static void main(String[] args)
    {
        launch(args);
    }
    
    //Svi fajlovi vezani za ovu aplikaciju se cuvaju u folderu user.home/Documents/GarageFiles
    @Override
    public void start(Stage stage) throws Exception
    {
        File parent=new File(System.getProperty("user.home")+"/Documents/GarageFiles");
        parent.mkdirs();
        
        int platforms=-1;
        Properties properties = new Properties();
        try {
		InputStream input = new FileInputStream(System.getProperty("user.home")+"/Documents/GarageFiles/config.properties");
                properties.load(input);

		platforms=Integer.parseInt(properties.getProperty("platforme"));
	} catch (IOException ex) {LoggerAndParkingPayment.setErrorLog(ex);}
        
        adminStage=stage;
        adminStage.setTitle("Garaza");
        Garage.deserialize();
        
        if(platforms!=-1 && Garage.platforms.isEmpty())
        {
            for(int i=0;i<platforms;++i)
                Garage.platforms.add(new Platform());
        }
        else if(Garage.platforms.isEmpty())
            Garage.platforms.add(new Platform());
        //else setPlatformTable(currentPlatform);
        
        adminStage.setScene(AdminScene());
        adminStage.show();
        
    }
    
    public Scene AdminScene()
    {
        ChoiceBox<String> choice=new ChoiceBox<>();
        
        Button runButton=new Button("Pokreni");
        runButton.setPrefSize(90, 40);
        
        Button addButton=new Button("Dodaj");
        
        Button changeButton=new Button("Izmjeni");
        changeButton.setPrefSize(100,10);
        
        Button imageButton=new Button("Prikazi fotografiju");
        
        Button deleteButton=new Button("Obrisi");
        deleteButton.setPrefSize(100,10);
        
        Button addPlatformButton=new Button("Dodaj novu platformu");
        addPlatformButton.setPrefSize(150,10);
        
        ComboBox comboBox=new ComboBox<>();
        comboBox.setPromptText("Izaberite platformu");
        for(int i=0;i<Garage.platforms.size();++i)
            comboBox.getItems().add(String.valueOf(i+1));
        
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
        
        Label label=new Label("Vozila:");
        
        VBox vBox=new VBox(15);
        vBox.setPadding(new Insets(20,20,20,20));
        vBox.getChildren().addAll(label,choice,addButton);
        
        HBox hBox=new HBox(15);
        hBox.setPadding(new Insets(20,20,20,20));
        hBox.getChildren().addAll(runButton);
        
        HBox hBoxTop=new HBox(15);
        hBoxTop.setPadding(new Insets(20,20,20,20));
        hBoxTop.getChildren().addAll(addPlatformButton,comboBox);
        
        VBox tableBox = new VBox(10);
        tableBox.setPadding(new Insets(20,20,20,20));
        tableBox.getChildren().addAll(table,imageButton,changeButton,deleteButton);
        
        comboBox.setOnAction(e -> setPlatformTable(Integer.parseInt(comboBox.getSelectionModel().getSelectedItem().toString())));
       
        addButton.setOnAction(e -> {   String type=choice.getValue();
            VehicleInfo.display(type,null);
        });
        
        changeButton.setOnAction(e -> {   if(table.getSelectionModel().getSelectedItem()==null) return;
            String type=table.getSelectionModel().getSelectedItem().getType();
            VehicleInfo.display(type,table.getSelectionModel().getSelectedItem());
        });
        
        imageButton.setOnAction(e -> { try{
            VehicleInfo.displayPhoto(table.getSelectionModel().getSelectedItem().imageURI);
        }catch(Exception ex){LoggerAndParkingPayment.setErrorLog(ex);}});
        
        deleteButton.setOnAction(e -> {
            Garage.deleteCar(table.getSelectionModel().getSelectedItem(),true);
            table.setItems(Garage.getPlatformVehicles(currentPlatform));
        });
        
        addPlatformButton.setOnAction(e -> {Garage.platforms.add(new Platform());
            comboBox.getItems().add(String.valueOf(Garage.platforms.size()));
        });
        
        runButton.setOnAction(e -> {Garage.serialize();
                adminStage.close();
                UserProgram.showUserProgram();
                });
       
        BorderPane layout=new BorderPane();
        layout.setLeft(vBox);
        layout.setBottom(hBox);
        layout.setCenter(tableBox);
        layout.setTop(hBoxTop);
        hBoxTop.setAlignment(Pos.TOP_CENTER);
        tableBox.setAlignment(Pos.CENTER);
        hBox.setAlignment(Pos.BOTTOM_RIGHT);
        vBox.setAlignment(Pos.CENTER_LEFT);
        
        Scene scene=new Scene(layout,800,600);
        return scene;
    }

    public void setPlatformTable(int platform)
    {
        currentPlatform=platform;
        TableColumn<Vehicle,String> nameColumn=new TableColumn<>("Ime");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setMinWidth(100);
        
        TableColumn<Vehicle,String> plateColumn=new TableColumn<>("Registarski broj");
        plateColumn.setCellValueFactory(new PropertyValueFactory<>("licensePlate"));
        plateColumn.setMinWidth(100);
        
        TableColumn<Vehicle,String> chassisColumn=new TableColumn<>("Broj sasije");
        chassisColumn.setCellValueFactory(new PropertyValueFactory<>("chassisNumber"));
        chassisColumn.setMinWidth(100);
        
        TableColumn<Vehicle,String> engineColumn=new TableColumn<>("Broj motora");
        engineColumn.setCellValueFactory(new PropertyValueFactory<>("engineNumber"));
        engineColumn.setMinWidth(100);
        
        TableColumn<Vehicle,String> typeColumn=new TableColumn<>("Tip vozila");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setMinWidth(100);
     
        table.setItems(Garage.getPlatformVehicles(platform));
        
        if(table.getColumns().isEmpty())
            table.getColumns().addAll(typeColumn,nameColumn, plateColumn, chassisColumn,engineColumn);
    }
}
