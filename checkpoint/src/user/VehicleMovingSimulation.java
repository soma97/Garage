package user;

import admin.*;
import vehicles.*;

import java.util.Random;
import java.io.*;

public class VehicleMovingSimulation extends Thread {

    boolean findingParking;
    int platformNumber;
    TraversableNode node;
    CrashSite crashSite;
    boolean isInLeftLane;

    VehicleMovingSimulation(boolean findingParking, int platformNumber, TraversableNode node) {
        this.findingParking = findingParking;
        this.platformNumber = platformNumber;
        this.node = node;
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
            if(Thread.currentThread().isInterrupted()) { UserProgram.threads.remove(this); return;}
            
            while (UserProgram.stopTraffic.contains(platformNumber) && UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j].label.getText().contains("R") == false)
                tSleep(2000);
            
            if(node.label.getText().contains("P")) checkForSuspects();
            
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
        System.out.println("JA SAM ZAVRSIO U RUNU : "+this);
        UserProgram.threads.remove(this);
    }

    public void checkForSuspects()
    {
       // BufferedReader reader=new BufferedReader((Garage.getPlatform(platformNumber).matrix[node.i][node.j].vehicle.getClass())Garage.getPlatform(pla);
        
    }
    public synchronized boolean exitPlatform()
    {
        if (Garage.getPlatform(platformNumber).matrix[node.i][node.j].isParkingPlace == true) {
            tSleep(1000);
            int[] locationArray = new int[]{-2, -1, 1, 2};
            for (int i = 0; i < 4; ++i) {
                if ((node.j + locationArray[i] >= 0 && node.j + locationArray[i] <= 7) && Garage.getPlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].isParkingPlace == false
                        && (UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].toExitPlatform != null || UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].toFindParking != null || UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].toNextPlatform != null)) {

                    if (Garage.getPlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].isFree == false) {
                        if (node.label.getText().contains("R")) {
                            moveVehicle(platformNumber, platformNumber, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].leftLane);
                            node = UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].leftLane;
                            isInLeftLane = true;
                            return true;
                        }
                        else if (new Random().nextInt(10) == 0) {
                            vehicleCrash(Garage.getPlatform(platformNumber).matrix[node.i][node.j].vehicle, Garage.getPlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].vehicle, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]]);
                            UserProgram.threads.remove(this);
                            return false;
                        } else {
                            --i;
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
        if (UserProgram.stopTraffic.contains(platformNumber) && node.label.getText().contains("R") == false) 
            return true;
        

        if (node.toExitPlatform != null) {
            if (UserProgram.platformSimulation.getTraversePlatform(platformNumber).toLeft == node.toExitPlatform && platformNumber != 1) {
                if (Garage.getPlatform(platformNumber - 1).matrix[node.toExitPlatform.i][node.toExitPlatform.j].isFree) {
                    moveVehicle(platformNumber, platformNumber - 1, node, node.toExitPlatform);
                    node = node.toExitPlatform;
                    --platformNumber;
                    return true;
                }
                else {
                    tSleep(500);
                    return true;
                }
            } else {
                if (node.i == 1 && node.j == 6 && Garage.getPlatform(platformNumber).matrix[node.toExitPlatform.i][node.toExitPlatform.j].isFree == false) {
                    if (new Random().nextInt(10)==0 && node.label.getText().contains("R")==false) {
                        vehicleCrash(Garage.getPlatform(platformNumber).matrix[node.i][node.j].vehicle, Garage.getPlatform(platformNumber).matrix[node.toExitPlatform.i][node.toExitPlatform.j].vehicle, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.toExitPlatform.i][node.toExitPlatform.j]);
                        return true;
                    }
                }
                else if (Garage.getPlatform(platformNumber).matrix[node.toExitPlatform.i][node.toExitPlatform.j].isFree) {
                    if(node.i==1 && node.j==6 && Garage.getPlatform(platformNumber).matrix[0][7].isFree==false) return true;
                    moveVehicle(platformNumber, platformNumber, node, node.toExitPlatform);
                    node = node.toExitPlatform;
                    if (node.i == 0 && node.j == 0 && platformNumber == 1) {
                        try {
                            PoliceStuffandParkingPayment.setParkingPayment(Garage.getPlatform(1).matrix[0][0].vehicle);
                            Thread.sleep(500);
                        } catch (Exception ex) {
                        }
                        GUI.currentPlatform = 1;
                        Garage.getPlatform(1).matrix[0][0].vehicle = null;
                        Garage.getPlatform(1).matrix[0][0].isFree = true;
                        javafx.application.Platform.runLater(
                                () -> {
                                    UserProgram.platformSimulation.getTraversePlatform(1).matrix[0][0].setLabel(" ");
                                });
                        return false;
                    }
                } else tSleep(500);
                
            }
        } else if (node.toFindParking != null) {
            if (Garage.getPlatform(platformNumber).matrix[node.toFindParking.i][node.toFindParking.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, node.toFindParking);
                node = node.toFindParking;
            } 
            else {
                tSleep(500);
                return true;
            }
        } else if (node.toNextPlatform != null) {
            if (Garage.getPlatform(platformNumber).matrix[node.toNextPlatform.i][node.toNextPlatform.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, node.toNextPlatform);
                node = node.toNextPlatform;
            } 
            else {
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
                    ParkWithChanceForCrash(locationArray[i]);
                } else {
                    moveVehicle(platformNumber, platformNumber, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]]);
                    node = UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]];
                }
                return;
            }
        }

    }

    public void ParkWithChanceForCrash(int space) {
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

    public synchronized void vehicleCrash(Vehicle vehicle1, Vehicle vehicle2, TraversableNode node, TraversableNode node2) {
        CrashSite crash = new CrashSite(node.i, node.j, platformNumber);
        UserProgram.crashes.add(crash);
        UserProgram.stopTraffic.add(platformNumber);
        System.out.println("DESIO SE UDES!!!" + node.i + node.j + " " + node2.i + node2.j + "NA PLATFORMI: " + platformNumber);
        crash.callEmergencyVehicles();

        for (VehicleMovingSimulation x : UserProgram.threads) {
            if (x.node == node2) {
                x.interrupt();
                break;
            }
        }

        try (FileOutputStream crashInfo = new FileOutputStream(new File(System.getProperty("user.home") + "/Documents/GarageFiles/crashInfo" + crash.toString()))) {
            crashInfo.write(vehicle1.type.getBytes());
            crashInfo.write(vehicle1.getName().getBytes());
            crashInfo.write(vehicle1.licensePlate.getBytes());
            crashInfo.write(vehicle1.getChassisNumber().getBytes());
            crashInfo.write(vehicle1.getEngineNumber().getBytes());
            crashInfo.write(vehicle1.getImage().getBytes());
            crashInfo.write(vehicle2.type.getBytes());
            crashInfo.write(vehicle2.getName().getBytes());
            crashInfo.write(vehicle2.licensePlate.getBytes());
            crashInfo.write(vehicle2.getChassisNumber().getBytes());
            crashInfo.write(vehicle2.getEngineNumber().getBytes());
            crashInfo.write(vehicle2.getImage().getBytes());
            crashInfo.write(java.time.LocalDateTime.now().toString().getBytes());
        } catch (Exception e) {
        }

        javafx.application.Platform.runLater(() -> {
            node.label.setText(" X");
            node2.label.setText(" X");
        });

        while (crash.numberOfEmergencyVehicles > 0)
            tSleep(1000);

        
        if(Garage.getPlatform(platformNumber).matrix[node.i][node.j].isParkingPlace==true)
            javafx.application.Platform.runLater(() -> {
                node.label.setText(" *");
                node2.label.setText(" ");
            });
        else javafx.application.Platform.runLater(() -> {
                node.label.setText(" ");
                node2.label.setText(" ");
            });
        

        System.out.println("JOINOVALO JEEEEEE"+UserProgram.crashes.get(0).ambulanceVehicle+" "+UserProgram.crashes.get(0).policeVehicle);
        
        if (UserProgram.stopTraffic.contains(platformNumber)) {
            UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber));
        }
        UserProgram.crashes.remove(this.crashSite);
        if(UserProgram.crashes.isEmpty())
            UserProgram.stopTraffic.clear();
        GUI.currentPlatform = platformNumber;
        Garage.deleteCar(vehicle1, false);
        Garage.deleteCar(vehicle2, false);
        UserProgram.threads.remove(this);
    }

    public void goToSolveTheCase(int platform, int i, int j) {

        System.out.println("IDEM RJESITI SLUCAJ: " + Garage.getPlatform(platformNumber).matrix[node.i][node.j].vehicle.type + "  " + node.i + node.j + " " + platformNumber);
        UserProgram.stopTraffic.add(platformNumber);

        exitPlatform();

        while (true) {
            
            if (platform < platformNumber) {
                exitPlatformWithRotation();
            } else if (platform > platformNumber) 
                goToNextPlatformWithRotation();
            else {
                tSleep(300);
               
                if (Math.abs(node.i - crashSite.i) <= 3 && Math.abs(node.j - crashSite.j) <= 3)
                    break;

                TraversableNode currentNode=null;
                System.out.println("NA PLATFORMI SAM I KRECEM SE :" + node.label.getText()+isInLeftLane+node.i+node.j);
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
                        }
                        else if (Garage.getPlatform(platformNumber).matrix[currentNode.leftLane.i][currentNode.leftLane.j].isFree) {
                            moveVehicle(platformNumber, platformNumber, node, currentNode.leftLane);
                            node = currentNode.leftLane;
                            isInLeftLane = true;
                        }
                    }
                } else {
                    
                    if(node.i==0 && node.j==0 && Garage.getPlatform(platformNumber).matrix[1][0].isFree) 
                    {
                        moveVehicle(platformNumber,platformNumber,node,UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[1][0]);
                        node=UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[1][0];continue;
                    }
                    else if(node.i==1 && node.j==7 && Garage.getPlatform(platformNumber).matrix[0][7].isFree)
                    {
                        moveVehicle(platformNumber,platformNumber,node,UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[0][7]);
                        node=UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[0][7];continue;
                    }
                    else if(node.toFindParking!=null)
                        currentNode=node.toFindParking;
                    else if(node.toExitPlatform!=null)
                        currentNode=node.toExitPlatform;
                    else if(node.toNextPlatform!=null)
                        currentNode=node.toNextPlatform;
                    
                    if(currentNode!=null)
                        moveForwardOrOvertake(currentNode);
                    
                }
            }
        }
        tSleep(5000 + (new Random().nextInt(6) * 1000));

        if (UserProgram.stopTraffic.contains(platformNumber)) {
            UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber));
        }
        crashSite.numberOfEmergencyVehicles--;
        System.out.println("SPEC VOZILA: "+crashSite.numberOfEmergencyVehicles);
        javafx.application.Platform.runLater(() -> node.label.setText(node.label.getText().replace("R", "")));
        crashSite=null;
        while (isInLeftLane) {
            System.out.println("JA SAM U LIJEVOJ TRACI: "+node.label.getText());
            TraversableNode currentNode;
            if (node.leftLane.toExitPlatform != null) {
                currentNode = node.leftLane.toExitPlatform;
            } else if (node.leftLane.toNextPlatform != null) {
                currentNode = node.leftLane.toNextPlatform;
            } else {
                currentNode = node.leftLane.toFindParking;
            }
            if(currentNode!=null){
            if (Garage.getPlatform(platformNumber).matrix[currentNode.i][currentNode.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, currentNode);
                node = currentNode;
                isInLeftLane = false;
            } else if(Garage.getPlatform(platformNumber).matrix[currentNode.leftLane.i][currentNode.leftLane.j].isFree){
                moveVehicle(platformNumber, platformNumber, node, currentNode.leftLane);
                node = currentNode.leftLane;
            }else tSleep(1000);
        }
        }
        while (true) {
            if(crashSite!=null)
                goToSolveTheCase(crashSite.platformNumber,crashSite.i,crashSite.j);
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

        if (UserProgram.platformSimulation.getTraversePlatform(platformNumber).toRight == node.toNextPlatform && node.toNextPlatform!=null) {
            if (Garage.getPlatform(platformNumber + 1).matrix[node.toNextPlatform.i][node.toNextPlatform.j].isFree) {
                moveVehicle(platformNumber, platformNumber + 1, node, node.toNextPlatform);
                node = node.toNextPlatform;
                ++platformNumber;
                if (node.label.getText().contains("R") && UserProgram.stopTraffic.contains(platformNumber - 1)) {
                    UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber - 1));
                    UserProgram.stopTraffic.add(platformNumber);
                }
            }  else tSleep(500);
            
        } else if (node.toNextPlatform != null) {
            if (Garage.getPlatform(platformNumber).matrix[node.toNextPlatform.i][node.toNextPlatform.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, node.toNextPlatform);
                node = node.toNextPlatform;
            } else tSleep(500);
            
        } else if (node.toFindParking != null) {
            if (Garage.getPlatform(platformNumber).matrix[node.toFindParking.i][node.toFindParking.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, node.toFindParking);
                node = node.toFindParking;
            }
            else tSleep(500);
            
        } else if (node.toExitPlatform != null) {

            if (Garage.getPlatform(platformNumber).matrix[node.toExitPlatform.i][node.toExitPlatform.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, node.toExitPlatform);
                node = node.toExitPlatform;
            } else tSleep(500);

        }
    }

    public void goToNextPlatformWithRotation() {
        tSleep(300);
        if (isInLeftLane == false) 
        {
            if (UserProgram.platformSimulation.getTraversePlatform(platformNumber).toRight == node.toNextPlatform && node.toNextPlatform!=null) {
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
            } else tSleep(500);
            }
            else if(node.toNextPlatform!=null) moveForwardOrOvertake(node.toNextPlatform);
            else if(node.toFindParking!=null) moveForwardOrOvertake(node.toFindParking);
            else if(node.toFindParking==null && node.toNextPlatform==null && node.leftLane!=null && Garage.getPlatform(platformNumber).matrix[node.leftLane.i][node.leftLane.j].isFree)
                {setInLeftLane(node);isInLeftLane=false;}
            else if (node.toExitPlatform != null) moveForwardOrOvertake(node.toExitPlatform);
            
        }
        else {
            TraversableNode currentNode;
            if (node.leftLane.toNextPlatform != null) {
                currentNode = node.leftLane.toNextPlatform;
            } else if (node.leftLane.toFindParking != null) {
                currentNode = node.leftLane.toFindParking;
            } else {
                currentNode = node.leftLane.toExitPlatform;
            }

            if (node.i == 0 && node.j == 7) {
                if (Garage.getPlatform(platformNumber + 1).matrix[1][0].isFree) 
                {
                    moveVehicle(platformNumber, platformNumber + 1, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).toRight);
                    node = UserProgram.platformSimulation.getTraversePlatform(platformNumber).toRight;
                    isInLeftLane = false;
                    ++platformNumber;
                    UserProgram.stopTraffic.add(platformNumber);
                    if(UserProgram.stopTraffic.contains(platformNumber-1)) UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber - 1));
                } else if (Garage.getPlatform(platformNumber + 1).matrix[0][0].isFree) 
                {
                    moveVehicle(platformNumber, platformNumber + 1, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber + 1).matrix[0][0]);
                    node = UserProgram.platformSimulation.getTraversePlatform(platformNumber + 1).matrix[0][0];
                    ++platformNumber;
                    UserProgram.stopTraffic.add(platformNumber);
                    if(UserProgram.stopTraffic.contains(platformNumber-1)) UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber - 1));
                }
            } else if (currentNode != null && Garage.getPlatform(platformNumber).matrix[currentNode.i][currentNode.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, currentNode);
                node = currentNode;
                isInLeftLane = false;
            }
            else if (Garage.getPlatform(platformNumber).matrix[currentNode.leftLane.i][currentNode.leftLane.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, currentNode.leftLane);
                node = currentNode.leftLane;
                isInLeftLane = true;
            }
        }
    }
    
    public void exitPlatformWithRotation()
    {
        tSleep(300);
        if (isInLeftLane == false) 
        {
            if (UserProgram.platformSimulation.getTraversePlatform(platformNumber).toLeft == node.toExitPlatform && node.toExitPlatform!=null) {
            if (Garage.getPlatform(platformNumber - 1).matrix[node.toExitPlatform.i][node.toExitPlatform.j].isFree) {
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
            } else tSleep(500);
            }
            else if(node.i==1 && node.j==6) moveForwardOrOvertake(UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[0][5]);
            else if(node.i==2 && node.j==6) moveForwardOrOvertake(UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[0][5]);
            else if(node.toExitPlatform!=null) moveForwardOrOvertake(node.toExitPlatform);
            else if(node.toFindParking!=null) moveForwardOrOvertake(node.toFindParking);
            else if(node.toExitPlatform == null && node.toFindParking == null && node.leftLane!=null && Garage.getPlatform(platformNumber).matrix[node.leftLane.i][node.leftLane.j].isFree)
                {setInLeftLane(node);isInLeftLane=false;}
            else if (node.toNextPlatform != null) moveForwardOrOvertake(node.toNextPlatform);
            
        }
        else {
            TraversableNode currentNode;
            if (node.leftLane.toExitPlatform != null) {
                currentNode = node.leftLane.toExitPlatform;
            } else if (node.leftLane.toFindParking != null) {
                currentNode = node.leftLane.toFindParking;
            } else {
                currentNode = node.leftLane.toNextPlatform;
            }

            if (node.i == 1 && node.j == 0) {
                if (Garage.getPlatform(platformNumber - 1).matrix[0][7].isFree) 
                {
                    moveVehicle(platformNumber, platformNumber - 1, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).toLeft);
                    node = UserProgram.platformSimulation.getTraversePlatform(platformNumber).toLeft;
                    isInLeftLane = false;
                    --platformNumber;
                    UserProgram.stopTraffic.add(platformNumber);
                    if(UserProgram.stopTraffic.contains(platformNumber+1)) UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber + 1));
                } else if (Garage.getPlatform(platformNumber - 1).matrix[1][7].isFree) 
                {
                    moveVehicle(platformNumber, platformNumber - 1, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber - 1).matrix[1][7]);
                    node = UserProgram.platformSimulation.getTraversePlatform(platformNumber - 1).matrix[1][7];
                    --platformNumber;
                    UserProgram.stopTraffic.add(platformNumber);
                    if(UserProgram.stopTraffic.contains(platformNumber+1)) UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber + 1));
                }
            } else if (currentNode != null && Garage.getPlatform(platformNumber).matrix[currentNode.i][currentNode.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, currentNode);
                node = currentNode;
                isInLeftLane = false;
            }
            else if (Garage.getPlatform(platformNumber).matrix[currentNode.leftLane.i][currentNode.leftLane.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, currentNode.leftLane);
                node = currentNode.leftLane;
                isInLeftLane = true;
            }
        }
    }
    public boolean moveForwardOrOvertake(TraversableNode nextNode)
    {
        if (nextNode == null) return false;
        
            if (Garage.getPlatform(platformNumber).matrix[nextNode.i][nextNode.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, nextNode);
                node = nextNode;
            } else if (node.label.getText().contains("R"))
            {
                if(setInLeftLane(nextNode)) return true;
                else return false;
            }
            else {    tSleep(500); return false; }
        return true;    
    }
    public static synchronized void moveVehicle(int platformNumberSource, int platformNumberDest, TraversableNode source, TraversableNode destination) {
       
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

    public static void tSleep(int timeInMS) 
    {
        try 
        {
            Thread.sleep(timeInMS);
        } catch (Exception e) {}
    }
}
