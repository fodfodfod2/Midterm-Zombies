import constants.Constants.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.io.PrintWriter;


public class App {
    public static WorldMap worldMap;
    public static String resultString = "trialNum,winner,CZD,CSI,INIT_INFECTED,";
    public static int trialNum = 0;
    public static ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(13);
    public static void main(String[] args) throws Exception {
        if (GeneralConstants.DEBUG_MAP){
            
            worldMap = new WorldMap(MapConstants.MAP_WIDTH, MapConstants.MAP_HEIGHT, GeneralConstants.RNG, SpreadConstants.CZD, SpreadConstants.CSI, MapConstants.INIT_INFECTED);
            GUI.main(args, true);
        }
        for (int CZD_IDX = 0; CZD_IDX < 10; CZD_IDX++) {
            for (int CSI_IDX = 0; CSI_IDX < 10; CSI_IDX++){
                for (int INIT_INFECTED_IDX = 0; INIT_INFECTED_IDX < 10; INIT_INFECTED_IDX++){
                    double czd = SpreadConstants.CZD;
                    double csi = SpreadConstants.CSI;
                    double initInfected = MapConstants.INIT_INFECTED;
                    int tNum = ++trialNum;
                    if (tNum <= -1){
                        continue;
                    }
                    executor.submit(() -> simulate(czd, csi, initInfected,tNum));
                    // boolean simActive = true;
                    // GeneralConstants.RNG = new Random(0);
                    // worldMap = new WorldMap(MapConstants.MAP_WIDTH, MapConstants.MAP_HEIGHT);

                    // while (simActive){
                    //     for (int i = 0; i < 10; i++){
                    //         worldMap.periodicUpdate();
                    //     }
                    //     double totalHumans = 0;
                    //     double totalZombies = 0;
                    //     double totalInfected = 0;

                    //     for (int x = 0; x < worldMap.getTiles().length; x++) {
                    //         for (int y = 0; y < worldMap.getTiles()[x].length; y++) {
                    //             Tile tile = worldMap.getTiles()[x][y];
                    //             totalHumans += tile.getHumans();
                    //             totalZombies += tile.getZombies();
                    //             totalInfected += tile.getTotalInfected();
                    //         }
                    //     }
                    //     if (totalHumans - totalInfected <= 1){
                    //         System.out.println("Zombies win!");
                    //         App.logResults(1, SpreadConstants.CZD, SpreadConstants.CSI, MapConstants.INIT_INFECTED);
                    //         simActive = false;
                    //     } else if (totalZombies+totalInfected <= 1){
                    //         System.out.println("Humans win!");
                    //         App.logResults(0, SpreadConstants.CZD, SpreadConstants.CSI, MapConstants.INIT_INFECTED);
                    //         simActive = false;
                    //     }
                    // }

                        
                    MapConstants.INIT_INFECTED += MapConstants.INIT_INFECTED_INCREMENT;
                }
                SpreadConstants.CSI += SpreadConstants.CSI_INCREMENT;
                MapConstants.INIT_INFECTED = 500;
            }
            SpreadConstants.CZD += SpreadConstants.CZD_INCREMENT;
            SpreadConstants.CSI = 0.1;
        }
        executor.awaitTermination(300, java.util.concurrent.TimeUnit.MINUTES);
    }

    public static void periodic(){
            worldMap.periodicUpdate();
    }
    public static void simulate(double CZD, double CSI, double INIT_INFECTED, int tNum){
        System.out.println("Starting simulation"+tNum+" with CZD="+CZD+", CSI="+CSI+", INIT_INFECTED="+INIT_INFECTED);
        try{
        boolean simActive = true;
        Random rng = new Random(0);
        worldMap = new WorldMap(MapConstants.MAP_WIDTH, MapConstants.MAP_HEIGHT, rng, CZD, CSI, INIT_INFECTED);

        while (simActive){
            // for (int i = 0; i < 10; i++){
                worldMap.periodicUpdate();
            // }
            double totalHumans = 0;
            double totalZombies = 0;
            double totalInfected = 0;

            for (int x = 0; x < worldMap.getTiles().length; x++) {
                for (int y = 0; y < worldMap.getTiles()[x].length; y++) {
                    Tile tile = worldMap.getTiles()[x][y];
                    totalHumans += tile.getHumans();
                    totalZombies += tile.getZombies();
                    totalInfected += tile.getTotalInfected();
                }
            }
            if (totalHumans - totalInfected <= 1){
                System.out.println("Zombies win!");
                App.logResults(1, CZD, CSI,INIT_INFECTED,tNum);
                simActive = false;
            } else if (totalZombies+totalInfected <= 1){
                System.out.println("Humans win!");
                App.logResults(0, CZD, CSI, INIT_INFECTED,tNum);
                simActive = false;
            }
        }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void logResults(int winner, double CZD, double CSI, double INIT_INFECTED,int trialNum){
        resultString += "\n"+trialNum+","+winner+","+CZD+","+CSI+","+INIT_INFECTED+",";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new java.awt.datatransfer.StringSelection(resultString), null);
        System.out.println(resultString);
        // try {
        //     PrintWriter pWriter = new PrintWriter("results.csv");
        //     pWriter.write(resultString);
        //     pWriter.close();
        // } catch (Exception e) {periodicUpdate
        //     e.printStackTrace();
        // }

    }
}
