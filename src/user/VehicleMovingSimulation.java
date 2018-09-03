package user;
import admin.*;
import vehicles.*;

import java.util.Random;
import java.io.*;
import java.util.ArrayList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;

public class VehicleMovingSimulation extends Thread {

    boolean findingParking;
    int platformNumber;
    TraversableNode node;
    CrashSite crashSite;
    boolean isInLeftLane;
    int suspectWait;
    boolean isInterrupted;

    VehicleMovingSimulation(boolean findingParking, int platformNumber, TraversableNode node) {
        this.findingParking = findingParking;
        this.platformNumber = platformNumber;
        this.node = node;
        suspectWait=0;
        isInterrupted=false;
    }

    @Override
    public void run() {
        boolean freeSpace = false;
        for (admin.Platform x : Garage.platforms) {
            if (x.freeSpace > 0) {
                freeSpace = true;
                break;
            }
        }
        if (freeSpace == false) {
            UserProgram.threads.remove(this);
            return;
        }

        while (crashSite == null) {
            if (isInterrupted) {
                UserProgram.threads.remove(this);
                return;
            }

            if(suspectWait>1)
            { 
                javafx.application.Platform.runLater(()-> node.label.setText(" S"));
                tSleep(suspectWait); findingParking=false; suspectWait=1;
            }
            
            while (UserProgram.stopTraffic.contains(platformNumber) && UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j].label.getText().contains("R") == false) {
                if(UserProgram.crashes.stream().filter((CrashSite cr) -> cr.platformNumber==this.platformNumber).findAny().isPresent())
                { synchronized(UserProgram.platformSimulation.getTraversePlatform(platformNumber).blockTraffic)
                    { 
                        try{ UserProgram.platformSimulation.getTraversePlatform(platformNumber).blockTraffic.wait(); }catch(Exception e){ LoggerAndParkingPayment.setErrorLog(e);} 
                    }
                }
                else tSleep(2000);
            }

            if (Garage.getPlatform(platformNumber).matrix[node.i][node.j].vehicle instanceof Police && checkForSuspects()) return;
            

            if (findingParking) {
                if (platformNumber == Garage.platforms.size() && Garage.getPlatform(platformNumber).freeSpace == 0) {
                    findingParking = false;
                } else if (Garage.getPlatform(platformNumber).freeSpace == 0) {
                    goToNextPlatform();
                } else {
                    findParking();
                    if (Garage.getPlatform(platformNumber).matrix[node.i][node.j].isParkingPlace == true) {
                        UserProgram.threads.remove(this);
                        return;
                    }
                }
            } else if (exitPlatform() == false) {
                UserProgram.threads.remove(this);
                return;
            }
        }
        //CrashSite crash = UserProgram.crashes.stream().filter((CrashSite cr) -> cr == this.crashSite).findFirst().get();
        goToSolveTheCase(crashSite.platformNumber, crashSite.i, crashSite.j);
        UserProgram.threads.remove(this);
    }

    public boolean checkForSuspects() {
        
        ArrayList<String> suspects = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(Police.PURSUITVEHICLES))) {
            String toAdd;
            while ((toAdd = reader.readLine()) != null) {
                suspects.add(toAdd);
            }
            for (int i = -2; i < 3; ++i) {
                for (int j = -2; j < 3; ++j)
                {
                    if ((node.i + i) >= 0 && (node.i+i) < 10 && (node.j + j)>=0 && (node.j + j)<8 && Garage.getPlatform(platformNumber).matrix[node.i + i][node.j + j].vehicle != null
                            && suspects.contains(Garage.getPlatform(platformNumber).matrix[node.i + i][node.j + j].vehicle.licensePlate)) 
                    {
                        try{
                        catchSuspect(Garage.getPlatform(platformNumber).matrix[node.i + i][node.j + j].vehicle, node.i + i, node.j + j);
                        return true;
                        }catch(Exception e){LoggerAndParkingPayment.setErrorLog(e); return false;}
                    }
                }
            }

        } catch (Exception e) { LoggerAndParkingPayment.setErrorLog(e); }

        return false;
    }

    public void catchSuspect(Vehicle vehicle, int i, int j) throws Exception
    {
        VehicleMovingSimulation suspect = null;
        
        for (VehicleMovingSimulation x : UserProgram.threads) {
            if (x.node.i == i && x.node.j == j) {
                suspect = x;
                break;
            }
        }

        if (suspect == null) {
            suspect = new VehicleMovingSimulation(false, platformNumber, UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[i][j]);
            UserProgram.threads.add(suspect);
            suspect.setDaemon(true);
            suspect.start();
        }
        if(suspect.suspectWait>0) throw new Exception();
        
        javafx.application.Platform.runLater(()-> node.label.setText(node.label.getText()+"R"));
        
        int toSuspectWait=new Random().nextInt(3)*1000;
        
        suspect.suspectWait=3000+toSuspectWait;
        
        try (DataOutputStream crashInfo = new DataOutputStream(new FileOutputStream(System.getProperty("user.home") + "/Documents/GarageFiles/suspectInfo" + vehicle.licensePlate))) {
            crashInfo.writeChars(vehicle.type);
            crashInfo.writeChars(vehicle.getName());
            crashInfo.writeChars(vehicle.licensePlate);
            crashInfo.writeChars(vehicle.getChassisNumber());
            crashInfo.writeChars(vehicle.getEngineNumber());
            try(ByteArrayOutputStream byteArray=new ByteArrayOutputStream())
            {
                Image image=new Image(vehicle.getImage());
                ImageIO.write(SwingFXUtils.fromFXImage(image, null),"jpg",byteArray);
                crashInfo.write(byteArray.toByteArray(),0,byteArray.toByteArray().length);
            }catch(Exception e){ LoggerAndParkingPayment.setErrorLog(e); }
            crashInfo.writeChars(java.time.LocalDateTime.now().toString());
        } catch (Exception e) { LoggerAndParkingPayment.setErrorLog(e); }
        
        tSleep(2000+toSuspectWait);

        while (true) {
            if (exitPlatform() == false) {
                break;
            }
        }
        UserProgram.threads.remove(this);
    }

    public boolean exitPlatform() {
        if (Garage.getPlatform(platformNumber).matrix[node.i][node.j].isParkingPlace == true) {
            tSleep(1000);
            int[] locationArray = new int[]{-2, -1, 1, 2};
            for (int i = 0; i < 4; ++i) {
                if ((node.j + locationArray[i] >= 0 && node.j + locationArray[i] <= 7) && Garage.getPlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].isParkingPlace == false
                        && (UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].toExitPlatform != null || UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].toFindParking != null || UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].toNextPlatform != null)) {

                    if (Garage.getPlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].isFree == false) {
                        if (node.label.getText().contains("R") && crashSite!=null && UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].leftLane.label.getText().contains("V")==false) {
                            moveVehicle(platformNumber, platformNumber, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].leftLane);
                            node = UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].leftLane;
                            isInLeftLane = true;
                            return true;
                        } else if (new Random().nextInt(10) == 0 && node.label.getText().contains("R")==false) {
                            vehicleCrash(Garage.getPlatform(platformNumber).matrix[node.i][node.j].vehicle, Garage.getPlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].vehicle, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]]);
                            UserProgram.threads.remove(this);
                            return false;
                        } else {
                            --i;
                            tSleep(750);
                            continue;
                        }
                    }
                    moveVehicle(platformNumber, platformNumber, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]]);
                    node = UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]];
                    break;
                }
            }
        }

        tSleep(1000);
        if (UserProgram.stopTraffic.contains(platformNumber) && node.label.getText().contains("R") == false) {
            return true;
        }
        if(this.isInterrupted)
            return true;
        
        if (node.toExitPlatform != null) {
            if (UserProgram.platformSimulation.getTraversePlatform(platformNumber).toLeft == node.toExitPlatform && platformNumber != 1) {
                if (Garage.getPlatform(platformNumber - 1).matrix[node.toExitPlatform.i][node.toExitPlatform.j].isFree) {
                    moveVehicle(platformNumber, platformNumber - 1, node, node.toExitPlatform);
                    node = node.toExitPlatform;
                    --platformNumber;
                    return true;
                } else {
                    tSleep(500);
                    return true;
                }
            } else {
                if (node.i == 1 && node.j == 6 && Garage.getPlatform(platformNumber).matrix[node.toExitPlatform.i][node.toExitPlatform.j].isFree == false) {
                    if (new Random().nextInt(10) == 0 && node.label.getText().contains("R") == false && node.label.getText().contains("S")==false && UserProgram.platformSimulation.getTraversePlatform(platformNumber).accidentHappened==false) {
                        vehicleCrash(Garage.getPlatform(platformNumber).matrix[node.i][node.j].vehicle, Garage.getPlatform(platformNumber).matrix[node.toExitPlatform.i][node.toExitPlatform.j].vehicle, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.toExitPlatform.i][node.toExitPlatform.j]);
                        return false;
                    }
                } else if (Garage.getPlatform(platformNumber).matrix[node.toExitPlatform.i][node.toExitPlatform.j].isFree) {
                    if (node.i == 1 && node.j == 6 && Garage.getPlatform(platformNumber).matrix[0][7].isFree == false) {
                        return true;
                    }
                    moveVehicle(platformNumber, platformNumber, node, node.toExitPlatform);
                    node = node.toExitPlatform;
                    if (node.i == 0 && node.j == 0 && platformNumber == 1) {
                        try {
                            LoggerAndParkingPayment.setParkingPayment(Garage.getPlatform(1).matrix[0][0].vehicle);
                            Thread.sleep(500);
                        } catch (Exception ex) { LoggerAndParkingPayment.setErrorLog(ex); }
                        GUI.currentPlatform = 1;
                        Garage.getPlatform(1).matrix[0][0].vehicle = null;
                        Garage.getPlatform(1).matrix[0][0].isFree = true;
                        javafx.application.Platform.runLater(
                                () -> {
                                    UserProgram.platformSimulation.getTraversePlatform(1).matrix[0][0].setLabel(" ");
                                });
                        return false;
                    }
                } else {
                    tSleep(500);
                    return true;
                }

            }
        } else if (node.toFindParking != null) {
            if (Garage.getPlatform(platformNumber).matrix[node.toFindParking.i][node.toFindParking.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, node.toFindParking);
                node = node.toFindParking;
            } else {
                tSleep(500);
                return true;
            }
        } else if (node.toNextPlatform != null) {
            if (Garage.getPlatform(platformNumber).matrix[node.toNextPlatform.i][node.toNextPlatform.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, node.toNextPlatform);
                node = node.toNextPlatform;
            } else {
                tSleep(500);
                return true;
            }
        }
        return true;

    }

    public void findParking() {
        int[] locationArray = new int[]{-2, -1, 1, 2};

        tSleep(1000);
        if (UserProgram.stopTraffic.contains(platformNumber) && node.label.getText().contains("R") == false) {
            return;
        }

        if (node.toFindParking != null) {
            if (Garage.getPlatform(platformNumber).matrix[node.toFindParking.i][node.toFindParking.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, node.toFindParking);
                node = node.toFindParking;
            } else {
                tSleep(500);
                return;
            }
        } else if (node.toNextPlatform != null) {
            int addToPlatformNumber = 0;
            if (node.i == 1 && node.j == 7) {
                addToPlatformNumber = 1;
            }

            if (Garage.getPlatform(platformNumber + addToPlatformNumber).matrix[node.toNextPlatform.i][node.toNextPlatform.j].isFree) {
                moveVehicle(platformNumber, platformNumber + addToPlatformNumber, node, node.toNextPlatform);
                node = node.toNextPlatform;
            } else {
                tSleep(500);
                return;
            }
            if (addToPlatformNumber == 1) {
                ++platformNumber;
            }
        }

        for (int i = 0; i < 4; ++i) {
            if ((node.j + locationArray[i] >= 0 && node.j + locationArray[i] <= 7) && Garage.getPlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].isParkingPlace && Garage.getPlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].isFree) {

                tSleep(1000);

                if (Garage.getPlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].isFree == false) {
                    continue;
                }
                if (locationArray[i] == 2 || locationArray[i] == -2) {
                    parkWithChanceForCrash(locationArray[i]);
                } else {
                    moveVehicle(platformNumber, platformNumber, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]]);
                    node = UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]];
                }
                return;
            }
        }

    }

    public void parkWithChanceForCrash(int space) {
        Random random = new Random();
        int add = 0;
        if (space > 0) {
            add = -1;
        } else {
            add = 1;
        }

        if (Garage.getPlatform(platformNumber).matrix[node.i][node.j + space + add].isFree) {
            moveVehicle(platformNumber, platformNumber, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + space + add]);
            node = UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + space + add];
            tSleep(1000);
            while (Garage.getPlatform(platformNumber).matrix[node.i][node.j + space + add].isFree == false) {
                tSleep(500);
            }

            moveVehicle(platformNumber, platformNumber, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + space + add]);
            node = UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + space + add];
        } else if (random.nextInt(10) == 0) {
            vehicleCrash(Garage.getPlatform(platformNumber).matrix[node.i][node.j].vehicle, Garage.getPlatform(platformNumber).matrix[node.i][node.j + space + add].vehicle, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + space + add]);
        }
    }

    public void vehicleCrash(Vehicle vehicle1, Vehicle vehicle2, TraversableNode node, TraversableNode node2) {
        System.out.println("JA SAM SE SUDARIO: "+Thread.currentThread().getName());
        UserProgram.platformSimulation.getTraversePlatform(platformNumber).accidentHappened=true;
        CrashSite crash = new CrashSite(node.i, node.j, platformNumber);
        UserProgram.crashes.add(crash);
        UserProgram.stopTraffic.add(platformNumber);
        crash.callEmergencyVehicles();

        for (VehicleMovingSimulation x : UserProgram.threads) {
            if (x.node == node2) {
                System.out.println("SA "+x.getName());
                x.isInterrupted=true;
                break;
            }
        }

        try (DataOutputStream crashInfo = new DataOutputStream(new FileOutputStream(System.getProperty("user.home") + "/Documents/GarageFiles/crashInfo" + crash.toString()))) {
          
            crashInfo.writeChars(vehicle1.type);
            crashInfo.writeChars(vehicle1.getName());
            crashInfo.writeChars(vehicle1.licensePlate);
            crashInfo.writeChars(vehicle1.getChassisNumber());
            crashInfo.writeChars(vehicle1.getEngineNumber());
            try(ByteArrayOutputStream byteArray=new ByteArrayOutputStream())
            {
                Image image=new Image(vehicle1.getImage());
                ImageIO.write(SwingFXUtils.fromFXImage(image, null),"jpg",byteArray);
                crashInfo.write(byteArray.toByteArray(),0,byteArray.toByteArray().length);
            }catch(Exception e){ LoggerAndParkingPayment.setErrorLog(e); }
            
            crashInfo.writeChars(vehicle2.type);
            crashInfo.writeChars(vehicle2.getName());
            crashInfo.writeChars(vehicle2.licensePlate);
            crashInfo.writeChars(vehicle2.getChassisNumber());
            crashInfo.writeChars(vehicle2.getEngineNumber());
            try(ByteArrayOutputStream byteArray=new ByteArrayOutputStream())
            {
                Image image=new Image(vehicle2.getImage());
                ImageIO.write(SwingFXUtils.fromFXImage(image, null),"jpg",byteArray);
                crashInfo.write(byteArray.toByteArray(),0,byteArray.toByteArray().length);
            }catch(Exception e){ LoggerAndParkingPayment.setErrorLog(e); }
            
            crashInfo.writeChars(java.time.LocalDateTime.now().toString());
        } catch (Exception e) { LoggerAndParkingPayment.setErrorLog(e); }

        javafx.application.Platform.runLater(() -> {
            UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j].label.setText(" X");
            UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node2.i][node2.j].label.setText(" X");
        });

        while (crash.numberOfEmergencyVehicles > 0) {
            tSleep(1000);
        }

        if (Garage.getPlatform(platformNumber).matrix[node.i][node.j].isParkingPlace == true) {
            javafx.application.Platform.runLater(() -> {
                UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j].label.setText(" *");
                UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node2.i][node2.j].label.setText(" ");
            });
        } else {
            javafx.application.Platform.runLater(() -> {
                UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j].label.setText(" ");
                UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node2.i][node2.j].label.setText(" ");
            });
        }

        GUI.currentPlatform = platformNumber;
        Garage.deleteCar(vehicle1, false);
        Garage.deleteCar(vehicle2, false);
        Garage.getPlatform(platformNumber).matrix[node.i][node.j].isFree=true;
        Garage.getPlatform(platformNumber).matrix[node2.i][node2.j].isFree=true;
        
        if (UserProgram.stopTraffic.contains(platformNumber)) {
            UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber));
        }
        if(UserProgram.crashes.contains(crash))
            UserProgram.crashes.remove(crash);
        
        if (UserProgram.crashes.isEmpty()) {
            UserProgram.stopTraffic.clear();
        }
        synchronized(UserProgram.platformSimulation.getTraversePlatform(platformNumber).blockTraffic)
        {
            UserProgram.platformSimulation.getTraversePlatform(platformNumber).blockTraffic.notifyAll();
        }
     
        UserProgram.threads.remove(this);
    }

    public void goToSolveTheCase(int platform, int i, int j) {
        
        UserProgram.stopTraffic.add(platformNumber);

        exitPlatform();
        int onTheSamePosition=0;
        int iOld=node.i,jOld=node.j;
        
        while (true) {

            if(node.i==iOld && node.j==jOld)
                onTheSamePosition++;
            else onTheSamePosition=0;
            iOld=node.i;jOld=node.j;
            if(onTheSamePosition>8)
                break;
            
            if (platform < platformNumber) {
                exitPlatformWithRotation();
            } else if (platform > platformNumber) {
                goToNextPlatformWithRotation();
            } else {
                tSleep(300);

                if (Math.abs(node.i - crashSite.i) <= 3 && Math.abs(node.j - crashSite.j) <= 3) {
                    break;
                }
                
                TraversableNode currentNode = null;
                
                if (isInLeftLane) {
                    if (node.leftLane.toFindParking != null) {
                        currentNode = node.leftLane.toFindParking;
                    } else if (node.leftLane.toExitPlatform != null) {
                        currentNode = node.leftLane.toExitPlatform;
                    } else {
                        currentNode = node.leftLane.toNextPlatform;
                    }

                    if (currentNode != null) {
                        if (Garage.getPlatform(platformNumber).matrix[currentNode.i][currentNode.j].isFree) {
                            moveVehicle(platformNumber, platformNumber, node, currentNode);
                            node = currentNode;
                            isInLeftLane = false;
                        } else if (Garage.getPlatform(platformNumber).matrix[currentNode.leftLane.i][currentNode.leftLane.j].isFree) {
                            moveVehicle(platformNumber, platformNumber, node, currentNode.leftLane);
                            node = currentNode.leftLane;
                            isInLeftLane = true;
                        }
                    }
                } else {

                    if (node.i == 0 && node.j == 0) {
                        moveForwardOrOvertake(UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[1][1]);
                    } else if (node.i == 1 && node.j == 7) {
                        moveForwardOrOvertake(UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[0][6]);
                        continue;
                    } else if (node.toFindParking != null) {
                        currentNode = node.toFindParking;
                    } else if (node.toExitPlatform != null) {
                        currentNode = node.toExitPlatform;
                    } else if (node.toNextPlatform != null) {
                        currentNode = node.toNextPlatform;
                    }

                    if (currentNode != null) {
                        moveForwardOrOvertake(currentNode);
                    }

                }
            }
        }
        tSleep(3000 + (new Random().nextInt(6) * 1000));

        if (UserProgram.stopTraffic.contains(platformNumber)) {
            UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber));
        }
        crashSite.numberOfEmergencyVehicles--;
        
        crashSite = null;
        
        while (isInLeftLane) {
            
            TraversableNode currentNode;
            if (node.leftLane.toExitPlatform != null) {
                currentNode = node.leftLane.toExitPlatform;
            } else if (node.leftLane.toNextPlatform != null) {
                currentNode = node.leftLane.toNextPlatform;
            } else {
                currentNode = node.leftLane.toFindParking;
            }
            if (currentNode != null) {
                if (Garage.getPlatform(platformNumber).matrix[currentNode.i][currentNode.j].isFree && currentNode.label.getText().endsWith(" ")) {
                    moveVehicle(platformNumber, platformNumber, node, currentNode);
                    node = currentNode;
                    isInLeftLane = false;
                } else if (Garage.getPlatform(platformNumber).matrix[currentNode.leftLane.i][currentNode.leftLane.j].isFree && currentNode.leftLane.label.getText().endsWith(" ")) {
                    moveVehicle(platformNumber, platformNumber, node, currentNode.leftLane);
                    node = currentNode.leftLane;
                } else {
                    tSleep(1000);
                }
            }
        }
        
        javafx.application.Platform.runLater(() -> node.label.setText(node.label.getText().replace("R", "")));
        
        while (true) {
            if (crashSite != null) {
                goToSolveTheCase(crashSite.platformNumber, crashSite.i, crashSite.j);
            }
            if (exitPlatform() == false) {
                break;
            }
        }
        UserProgram.threads.remove(this);
    }

    public boolean setInLeftLane(TraversableNode movingType) {
        if (movingType.leftLane != null && Garage.getPlatform(platformNumber).matrix[movingType.leftLane.i][movingType.leftLane.j].isFree) {
            moveVehicle(platformNumber, platformNumber, node, movingType.leftLane);
            node = movingType.leftLane;
            isInLeftLane = true;
            return true;
        }
        return false;
    }

    public void goToNextPlatform() {

        tSleep(1000);

        if (UserProgram.stopTraffic.contains(platformNumber) && node.label.getText().contains("R") == false) {
            return;
        }

        if (UserProgram.platformSimulation.getTraversePlatform(platformNumber).toRight == node.toNextPlatform && node.toNextPlatform != null) {
            if (Garage.getPlatform(platformNumber + 1).matrix[node.toNextPlatform.i][node.toNextPlatform.j].isFree) {
                moveVehicle(platformNumber, platformNumber + 1, node, node.toNextPlatform);
                node = node.toNextPlatform;
                ++platformNumber;
                if (node.label.getText().contains("R") && UserProgram.stopTraffic.contains(platformNumber - 1)) {
                    UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber - 1));
                    UserProgram.stopTraffic.add(platformNumber);
                }
            } else {
                tSleep(500);
            }

        } else if (node.toNextPlatform != null) {
            if (Garage.getPlatform(platformNumber).matrix[node.toNextPlatform.i][node.toNextPlatform.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, node.toNextPlatform);
                node = node.toNextPlatform;
            } else {
                tSleep(500);
            }

        } else if (node.toFindParking != null) {
            if (Garage.getPlatform(platformNumber).matrix[node.toFindParking.i][node.toFindParking.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, node.toFindParking);
                node = node.toFindParking;
            } else {
                tSleep(500);
            }

        } else if (node.toExitPlatform != null) {

            if (Garage.getPlatform(platformNumber).matrix[node.toExitPlatform.i][node.toExitPlatform.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, node.toExitPlatform);
                node = node.toExitPlatform;
            } else {
                tSleep(500);
            }

        }
    }

    public void goToNextPlatformWithRotation() {
        tSleep(300);
        if (isInLeftLane == false) {
            if (UserProgram.platformSimulation.getTraversePlatform(platformNumber).toRight == node.toNextPlatform && node.toNextPlatform != null) {
                if (Garage.getPlatform(platformNumber + 1).matrix[node.toNextPlatform.i][node.toNextPlatform.j].isFree) {
                    moveVehicle(platformNumber, platformNumber + 1, node, node.toNextPlatform);
                    node = node.toNextPlatform;
                    ++platformNumber;
                    if (UserProgram.stopTraffic.contains(platformNumber - 1)) {
                        UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber - 1));
                        UserProgram.stopTraffic.add(platformNumber);
                    }
                } else if (Garage.getPlatform(platformNumber + 1).matrix[node.toNextPlatform.leftLane.i][node.toNextPlatform.leftLane.j].isFree) {
                    moveVehicle(platformNumber, platformNumber + 1, node, node.toNextPlatform.leftLane);
                    node = node.toNextPlatform.leftLane;
                    ++platformNumber;
                    if (UserProgram.stopTraffic.contains(platformNumber - 1)) {
                        UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber - 1));
                        UserProgram.stopTraffic.add(platformNumber);
                    }
                    isInLeftLane = true;
                } else {
                    tSleep(500);
                }
            } else if (node.toNextPlatform != null) {
                moveForwardOrOvertake(node.toNextPlatform);
            } else if (node.toFindParking != null) {
                moveForwardOrOvertake(node.toFindParking);
            } else if (node.toFindParking == null && node.toNextPlatform == null && node.leftLane != null && Garage.getPlatform(platformNumber).matrix[node.leftLane.i][node.leftLane.j].isFree) {
                setInLeftLane(node);
                isInLeftLane = false;
            } else if (node.toExitPlatform != null) {
                if(node.i==0 && node.j==0)
                    moveForwardOrOvertake(UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[1][1]);
                else moveForwardOrOvertake(node.toExitPlatform);
            }

        } else {
            TraversableNode currentNode;
            if (node.leftLane.toNextPlatform != null) {
                currentNode = node.leftLane.toNextPlatform;
            } else if (node.leftLane.toFindParking != null) {
                currentNode = node.leftLane.toFindParking;
            } else {
                currentNode = node.leftLane.toExitPlatform;
            }

            if (node.i == 0 && node.j == 7) {
                if (Garage.getPlatform(platformNumber + 1).matrix[1][0].isFree) {
                    moveVehicle(platformNumber, platformNumber + 1, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).toRight);
                    node = UserProgram.platformSimulation.getTraversePlatform(platformNumber).toRight;
                    isInLeftLane = false;
                    ++platformNumber;
                    UserProgram.stopTraffic.add(platformNumber);
                    if (UserProgram.stopTraffic.contains(platformNumber - 1)) {
                        UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber - 1));
                    }
                } else if (Garage.getPlatform(platformNumber + 1).matrix[0][0].isFree) {
                    moveVehicle(platformNumber, platformNumber + 1, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber + 1).matrix[0][0]);
                    node = UserProgram.platformSimulation.getTraversePlatform(platformNumber + 1).matrix[0][0];
                    ++platformNumber;
                    UserProgram.stopTraffic.add(platformNumber);
                    if (UserProgram.stopTraffic.contains(platformNumber - 1)) {
                        UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber - 1));
                    }
                }
            } else if (currentNode != null && Garage.getPlatform(platformNumber).matrix[currentNode.i][currentNode.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, currentNode);
                node = currentNode;
                isInLeftLane = false;
            } else if (Garage.getPlatform(platformNumber).matrix[currentNode.leftLane.i][currentNode.leftLane.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, currentNode.leftLane);
                node = currentNode.leftLane;
                isInLeftLane = true;
            }
        }
    }

    public void exitPlatformWithRotation() {
        tSleep(300);
        if (isInLeftLane == false) {
            if (UserProgram.platformSimulation.getTraversePlatform(platformNumber).toLeft == node.toExitPlatform && node.toExitPlatform != null) {
                if (Garage.getPlatform(platformNumber - 1).matrix[node.toExitPlatform.i][node.toExitPlatform.j].isFree && platformNumber!=1) {
                    moveVehicle(platformNumber, platformNumber - 1, node, node.toExitPlatform);
                    node = node.toExitPlatform;
                    --platformNumber;
                    if (UserProgram.stopTraffic.contains(platformNumber + 1)) {
                        UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber + 1));
                        UserProgram.stopTraffic.add(platformNumber);
                    }
                } else if (Garage.getPlatform(platformNumber - 1).matrix[node.toExitPlatform.leftLane.i][node.toExitPlatform.leftLane.j].isFree) {
                    moveVehicle(platformNumber, platformNumber - 1, node, node.toExitPlatform.leftLane);
                    node = node.toExitPlatform.leftLane;
                    --platformNumber;
                    if (UserProgram.stopTraffic.contains(platformNumber + 1)) {
                        UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber + 1));
                        UserProgram.stopTraffic.add(platformNumber);
                    }
                    isInLeftLane = true;
                } else {
                    tSleep(500);
                }
            } else if (node.i == 1 && node.j == 6) {
                moveForwardOrOvertake(UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[0][5]);
            } else if (node.i == 2 && node.j == 6) {
                moveForwardOrOvertake(UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[0][5]);
            } else if (node.toExitPlatform != null) {
                moveForwardOrOvertake(node.toExitPlatform);
            } else if (node.toFindParking != null) {
                moveForwardOrOvertake(node.toFindParking);
            } else if (node.toExitPlatform == null && node.toFindParking == null && node.leftLane != null && Garage.getPlatform(platformNumber).matrix[node.leftLane.i][node.leftLane.j].isFree) {
                setInLeftLane(node);
                isInLeftLane = false;
            } else if (node.toNextPlatform != null) {
                moveForwardOrOvertake(node.toNextPlatform);
            }

        } else {
            TraversableNode currentNode;
            if (node.leftLane.toExitPlatform != null) {
                currentNode = node.leftLane.toExitPlatform;
            } else if (node.leftLane.toFindParking != null) {
                currentNode = node.leftLane.toFindParking;
            } else {
                currentNode = node.leftLane.toNextPlatform;
            }

            if (node.i == 1 && node.j == 0) {
                if (Garage.getPlatform(platformNumber - 1).matrix[0][7].isFree) {
                    moveVehicle(platformNumber, platformNumber - 1, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).toLeft);
                    node = UserProgram.platformSimulation.getTraversePlatform(platformNumber).toLeft;
                    isInLeftLane = false;
                    --platformNumber;
                    UserProgram.stopTraffic.add(platformNumber);
                    if (UserProgram.stopTraffic.contains(platformNumber + 1)) {
                        UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber + 1));
                    }
                } else if (Garage.getPlatform(platformNumber - 1).matrix[1][7].isFree) {
                    moveVehicle(platformNumber, platformNumber - 1, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber - 1).matrix[1][7]);
                    node = UserProgram.platformSimulation.getTraversePlatform(platformNumber - 1).matrix[1][7];
                    --platformNumber;
                    UserProgram.stopTraffic.add(platformNumber);
                    if (UserProgram.stopTraffic.contains(platformNumber + 1)) {
                        UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber + 1));
                    }
                }
            } else if (currentNode != null && Garage.getPlatform(platformNumber).matrix[currentNode.i][currentNode.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, currentNode);
                node = currentNode;
                isInLeftLane = false;
            } else if (Garage.getPlatform(platformNumber).matrix[currentNode.leftLane.i][currentNode.leftLane.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, currentNode.leftLane);
                node = currentNode.leftLane;
                isInLeftLane = true;
            }
        }
    }

    public boolean moveForwardOrOvertake(TraversableNode nextNode) {
        if (nextNode == null) {
            return false;
        }

        if (Garage.getPlatform(platformNumber).matrix[nextNode.i][nextNode.j].isFree) {
            moveVehicle(platformNumber, platformNumber, node, nextNode);
            node = nextNode;
        } else if (node.label.getText().contains("R")) {
            if (setInLeftLane(nextNode)) {
                return true;
            } else {
                return false;
            }
        } else {
            tSleep(500);
            return false;
        }
        return true;
    }

    public static void moveVehicle(int platformNumberSource, int platformNumberDest, TraversableNode source, TraversableNode destination) {

        synchronized(Garage.getPlatform(platformNumberDest).lock)
        {
        admin.Platform platformSource = Garage.getPlatform(platformNumberSource);
        admin.Platform platformDest = Garage.getPlatform(platformNumberDest);
        Traverse traverseSource = UserProgram.platformSimulation.getTraversePlatform(platformNumberSource);
        Traverse traverseDest = UserProgram.platformSimulation.getTraversePlatform(platformNumberDest);

        platformDest.matrix[destination.i][destination.j].isFree = false;

        platformDest.matrix[destination.i][destination.j].vehicle = platformSource.matrix[source.i][source.j].vehicle;
        platformSource.matrix[source.i][source.j].vehicle = null;

        platformSource.matrix[source.i][source.j].isFree = true;

        if (platformDest.matrix[destination.i][destination.j].isParkingPlace) {
            platformDest.freeSpace--;
        }

        javafx.application.Platform.runLater(
                () -> {
                    traverseDest.matrix[destination.i][destination.j].setLabel(traverseSource.matrix[source.i][source.j].label.getText());

                    if (platformSource.matrix[source.i][source.j].isParkingPlace) {
                        platformSource.freeSpace++;
                        traverseSource.matrix[source.i][source.j].setLabel(" *");
                    } else {
                        traverseSource.matrix[source.i][source.j].setLabel(" ");
                    }
                });
        }
    }

    public static void tSleep(int timeInMS) {
        try {
            Thread.sleep(timeInMS);
        } catch (Exception e) { LoggerAndParkingPayment.setErrorLog(e); }
    }
}
