import constants.Constants.*;
public class App {
    public static WorldMap worldMap;

    public static void main(String[] args) throws Exception {
        worldMap = new WorldMap(MapConstants.MAP_WIDTH, MapConstants.MAP_HEIGHT);
        GUI.main(args, true);
    }

    public static void periodic(){
            worldMap.periodicUpdate();
    }
}
