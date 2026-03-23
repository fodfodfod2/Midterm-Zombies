import constants.Constants.*;
import javafx.application.Platform;
public class App {
    public static WorldMap worldMap;

    public static void main(String[] args) throws Exception {
        worldMap = new WorldMap(MapConstants.MAP_WIDTH, MapConstants.MAP_HEIGHT);
        GUI.main(args, true);
        // Thread.sleep(500);
        System.out.println("ahauhdj");
        // periodic();


    }

    public static void periodic(){
            Platform.runLater(()->worldMap.periodicUpdate());
            Platform.runLater(() -> GUI.updateMap());
    }
}
