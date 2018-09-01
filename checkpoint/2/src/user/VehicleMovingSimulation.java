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

        while (crashSite==null) 
        {
            
            while(UserProgram.stopTraffic.contains(platformNumber) && UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j].label.getText().contains("R")==false)
                {try{Thread.sleep(2000);}catch(Exception e){}}
            
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
        CrashSite crash=UserProgram.crashes.stream().filter((CrashSite cr) -> cr==this.crashSite).findFirst().get(); 
        goToSolveTheCase(crash.platformNumber, crash.i, crash.j);
        UserProgram.threads.remove(this);
    }

    public boolean exitPlatform()//vratiti sanse za udes na 10
    {
        if (Garage.getPlatform(platformNumber).matrix[node.i][node.j].isParkingPlace == true) {
            int[] locationArray = new int[]{-2, -1, 1, 2};
            for (int i = 0; i < 4; ++i) {
                if ((node.j + locationArray[i] >= 0 && node.j + locationArray[i] <= 7) && Garage.getPlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].isParkingPlace == false && Garage.getPlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].isFree
                        && (UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].toExitPlatform != null || UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].toFindParking != null || UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].toNextPlatform != null)) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                    }
                    if (Garage.getPlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].isFree == false) {
                        if (new Random().nextInt(10) == 0) {
                            vehicleCrash(Garage.getPlatform(platformNumber).matrix[node.i][node.j].vehicle, Garage.getPlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].vehicle, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]]);
                            UserProgram.threads.remove(this);
                            return false;
                        } else {
                            --i;continue;
                        }
                    }
                    moveVehicle(platformNumber, platformNumber, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]]);
                    node = UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.i][node.j + locationArray[i]];
                    break;
                }
            }
        }

        try {
            Thread.sleep(1000);
        } catch (Exception ex) {
        }
        if(UserProgram.stopTraffic.contains(platformNumber) && node.label.getText().contains("R")==false) return true;
        
        if (node.toExitPlatform != null) {
            if (UserProgram.platformSimulation.getTraversePlatform(platformNumber).toLeft == node.toExitPlatform && platformNumber != 1) {
                if (Garage.getPlatform(platformNumber - 1).matrix[node.toExitPlatform.i][node.toExitPlatform.j].isFree) {
                    moveVehicle(platformNumber, platformNumber - 1, node, node.toExitPlatform);
                    node = node.toExitPlatform;
                    --platformNumber;
                    return true;
                } else {
                    try {
                        Thread.sleep(500);
                        return true;
                    } catch (Exception ex) {
                    }
                }
            } else {
                if (node.i == 1 && node.j == 6 && Garage.getPlatform(platformNumber).matrix[node.toExitPlatform.i][node.toExitPlatform.j].isFree == false) {
                    if (new Random().nextInt(2) == 0) {
                        vehicleCrash(Garage.getPlatform(platformNumber).matrix[node.i][node.j].vehicle, Garage.getPlatform(platformNumber).matrix[node.toExitPlatform.i][node.toExitPlatform.j].vehicle, node, UserProgram.platformSimulation.getTraversePlatform(platformNumber).matrix[node.toExitPlatform.i][node.toExitPlatform.j]);
                        return true;
                    }
                }
                if (Garage.getPlatform(platformNumber).matrix[node.toExitPlatform.i][node.toExitPlatform.j].isFree) {
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
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ex) {
                    }
                }
            }
        } else if (node.toFindParking != null) {
            if (Garage.getPlatform(platformNumber).matrix[node.toFindParking.i][node.toFindParking.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, node.toFindParking);
                node = node.toFindParking;
            } else {
                try {
                    Thread.sleep(500);
                    return true;
                } catch (Exception ex) {
                }
            }
        } else if (node.toNextPlatform != null) {
            if (Garage.getPlatform(platformNumber).matrix[node.toNextPlatform.i][node.toNextPlatform.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, node.toNextPlatform);
                node = node.toNextPlatform;
            } else {
                try {
                    Thread.sleep(500);
                    return true;
                } catch (Exception ex) {
                }
            }
        }
        return true;
    }

    public void findParking() {
        int[] locationArray = new int[]{-2, -1, 1, 2};

        try {
            Thread.sleep(1000);
        } catch (Exception ex) {
        }
        if(UserProgram.stopTraffic.contains(platformNumber) && node.label.getText().contains("R")==false) return;
        
        if (node.toFindParking != null) {
            if (Garage.getPlatform(platformNumber).matrix[node.toFindParking.i][node.toFindParking.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, node.toFindParking);
                node = node.toFindParking;
            } else {
                try {
                    Thread.sleep(500);
                    return;
                } catch (Exception ex) {
                }
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
                try {
                    Thread.sleep(500);
                    return;
                } catch (Exception ex) {
                }
            }
            if (addToPlatformNumber == 1) {
                ++platformNumber;
            }
        }

        for (int i = 0; i < 4; ++i) {
            if ((node.j + locationArray[i] >= 0 && node.j + locationArray[i] <= 7) && Garage.getPlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].isParkingPlace && Garage.getPlatform(platformNumber).matrix[node.i][node.j + locationArray[i]].isFree) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                }
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
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {
            }
            while (Garage.getPlatform(platformNumber).matrix[node.i][node.j + space + add].isFree == false) {
                try {
                    Thread.sleep(500);
                } catch (Exception ex) {
                }
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
        if(UserProgram.stopTraffic.contains(platformNumber)==false) UserProgram.stopTraffic.add(platformNumber);
        System.out.println("DESIO SE UDES!!!" + node.i + node.j + " " + node2.i + node2.j + "NA PLATFORMI: " + platformNumber);
        crash.callEmergencyVehicles();

        
        for(VehicleMovingSimulation x:UserProgram.threads)
            if(x.node==node2)  { x.stop();break; }
        
       
        try(FileOutputStream crashInfo=new FileOutputStream(new File(System.getProperty("user.home")+"/Documents/GarageFiles/crashInfo"+crash.toString())))
        {
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
            crashInfo.write(vehicle1.type.getBytes());
            crashInfo.write(vehicle1.getName().getBytes());
            crashInfo.write(vehicle1.licensePlate.getBytes());
            crashInfo.write(vehicle1.getChassisNumber().getBytes());
            crashInfo.write(vehicle1.getEngineNumber().getBytes());
            crashInfo.write(java.time.LocalDateTime.now().toString().getBytes());
        }
        catch(Exception e){}
        
        
        try{ Thread.sleep(5000+(new Random().nextInt(6)*1000));}catch(Exception e){}
        
        for(VehicleMovingSimulation x:UserProgram.threads)
            if(crash==x.crashSite) try{ x.join();}catch(Exception e){System.out.println("NECE DA JOINUJEEEEEEEEEEEEEEE");}
                
        UserProgram.stopTraffic.remove(UserProgram.stopTraffic.indexOf(platformNumber));
        
        javafx.application.Platform.runLater(()->{
                node.label.setText(" ");
                node2.label.setText(" ");});
        
        System.out.println("JOINOVALO JEEEEEE");
        
        GUI.currentPlatform=platformNumber;
        Garage.deleteCar(vehicle1, false);
        Garage.deleteCar(vehicle2, false);
    }

    public void goToSolveTheCase(int platform, int i, int j) {
        //u zavisnosti da li je hitna policija ili vatrogasci treba ovu metodu napisati
        System.out.println("IDEM RJESITI SLUCAJ: " + Garage.getPlatform(platformNumber).matrix[node.i][node.j].vehicle.type+"  "+node.i+node.j+" "+platformNumber);
        if(UserProgram.stopTraffic.contains(platformNumber)==false) UserProgram.stopTraffic.add(platformNumber);
       
        exitPlatform();
        
        while(true)
        {
            if(platform<this.platformNumber)
            {
               TraversableNode currentNode;
               if(node.toExitPlatform!=null) currentNode=node.toExitPlatform;
               else if(node.toFindParking!=null) currentNode=node.toFindParking;
               else currentNode=node.toNextPlatform;
               
               if(currentNode!=null)
               {
                   if(Garage.getPlatform(platformNumber).matrix[currentNode.i][currentNode.j].isFree)
                    { moveVehicle(platformNumber,platformNumber,node,currentNode);continue;}
                   
                   setInLeftLane(node,currentNode);
                       
                   if(node.toExitPlatform!=null) currentNode=node.toExitPlatform;
                   else if(node.toFindParking!=null) currentNode=node.toFindParking;
                   else currentNode=node.toNextPlatform;
                   
                   if(currentNode!=null && Garage.getPlatform(platformNumber).matrix[currentNode.i][currentNode.j].isFree)
                   {
                       moveVehicle(platformNumber,platformNumber,node.leftLane,currentNode);
                       node=currentNode;
                   }
               }
            }
        }
        
        
    }
    public boolean setInLeftLane(TraversableNode node,TraversableNode movingType)
    {
        if(movingType.leftLane!=null && Garage.getPlatform(platformNumber).matrix[movingType.leftLane.i][movingType.leftLane.j].isFree)
        {
            moveVehicle(platformNumber,platformNumber,node,movingType.leftLane);
            node=movingType;
            return true;     
        }
        return false;
    }

    public void goToNextPlatform() {
        try {
            Thread.sleep(1000);
        } catch (Exception ex) {}
        if(UserProgram.stopTraffic.contains(platformNumber) && node.label.getText().contains("R")==false) return;
        
        if (UserProgram.platformSimulation.getTraversePlatform(platformNumber).toRight == node.toNextPlatform) {
            if (Garage.getPlatform(platformNumber + 1).matrix[node.toNextPlatform.i][node.toNextPlatform.j].isFree) {
                moveVehicle(platformNumber, platformNumber + 1, node, node.toNextPlatform);
                node = node.toNextPlatform;
                ++platformNumber;
            } else {
                try {
                    Thread.sleep(500);
                } catch (Exception ex) { }
            }
        } else if (node.toNextPlatform != null) {
            if (Garage.getPlatform(platformNumber).matrix[node.toNextPlatform.i][node.toNextPlatform.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, node.toNextPlatform);
                node = node.toNextPlatform;
            } else {
                try {
                    Thread.sleep(500);
                } catch (Exception ex) {}
            }
        } else if (node.toFindParking != null) {
            if (Garage.getPlatform(platformNumber).matrix[node.toFindParking.i][node.toFindParking.j].isFree) {
                moveVehicle(platformNumber, platformNumber, node, node.toFindParking);
                node = node.toFindParking;
            } else {
                try {
                    Thread.sleep(500);
                } catch (Exception ex) { }
            }
        }
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

        if (platformDest.matrix[destination.i][destination.j].isParkingPlace)
            platformDest.freeSpace--;

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
