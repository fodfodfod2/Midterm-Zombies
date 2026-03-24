import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import constants.Constants.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * The graphical user interface for the Midterm Zombies game.
 * This class uses JavaFX to render the world map as a grid of colored rectangles,
 * where each rectangle represents a tile with its biome color.
 */
public class GUI extends Application {
    private static Rectangle[][] biomePixelMap;
    private static Circle[][] inhabitantPixelMap;
    
    private static Stage mainStage;
    
    private ScheduledExecutorService executor;

    /**
     * A Fake main function for to trick javaFX, is called by the real main function in App.java
     * Launches the GUI with the provided command line arguments.
     * The boolean parameter is unused and exists only to stop vs code from thinking this is the main function
     *
     * @param args command line arguments passed to the application
     * @param THIS_IS_JUST_TO_TRICK_VSCODE_THIS_IS_A_IMPOSTER unused boolean parameter
     */
    public static void main(String[] args, boolean THIS_IS_JUST_TO_TRICK_VSCODE_THIS_IS_A_IMPOSTER) {
        launch(args);
    }

    /**
     * Starts the JavaFX application by setting up the main scene and displaying the world map.
     * Creates a grid of rectangles where each rectangle represents a tile in the world map,
     * colored according to the tile's biome using the predefined color mappings.
     *
     * @param stage the primary stage for this application, onto which the application scene is set
     * @throws Exception if there is an error during application startup
     */
    @Override
    public void start(Stage stage) throws Exception {
        biomePixelMap = new Rectangle[MapConstants.MAP_WIDTH][MapConstants.MAP_HEIGHT];
        inhabitantPixelMap = new Circle[MapConstants.MAP_WIDTH][MapConstants.MAP_HEIGHT];
        mainStage = stage;
        buildMap(stage);

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            App.periodic();;
            Platform.runLater(() -> updateMap());
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    private static void buildMap(Stage stage) throws Exception{
        Tile[][] tileMap = App.worldMap.getTiles();
        
        Group base = new Group();
        Scene scene = new Scene(base, MapConstants.MAP_WIDTH * MapConstants.MAP_SCALE,
                MapConstants.MAP_HEIGHT * MapConstants.MAP_SCALE);
        scene.setFill(Color.RED);

        for (int x = 0; x < tileMap.length; x++) {
            for (int y = 0; y < tileMap[x].length; y++) {
                Rectangle pixel = new Rectangle(x * MapConstants.MAP_SCALE, y * MapConstants.MAP_SCALE, MapConstants.MAP_SCALE,
                        MapConstants.MAP_SCALE);
                pixel.setFill(MapConstants.BIOME_COLORS.get(tileMap[x][y].getBiome()));
                base.getChildren().add(pixel);
                biomePixelMap[x][y] = pixel;
            }
        }

        for (int x = 0; x < tileMap.length; x++) {
            for (int y = 0; y < tileMap[x].length; y++) {
                Circle pixel = new Circle(x * MapConstants.MAP_SCALE, y * MapConstants.MAP_SCALE, MapConstants.MAP_SCALE*.5);
                double humanAmt = tileMap[x][y].getInhabitants().get(MapConstants.TILE_INHABITANTS.HUMAN);
                double infectedAmt = tileMap[x][y].getTotalInfected();
                humanAmt -= infectedAmt;
                double zombieAmt = tileMap[x][y].getInhabitants().get(MapConstants.TILE_INHABITANTS.ZOMBIE);

                double total = humanAmt+infectedAmt+zombieAmt;
                double b =  humanAmt/MapConstants.INHABITANT_COLOR_WEIGHTING.get(MapConstants.TILE_INHABITANTS.HUMAN);
                double g = infectedAmt/MapConstants.INHABITANT_COLOR_WEIGHTING.get(MapConstants.TILE_INHABITANTS.INFECTED);
                double r =  zombieAmt/MapConstants.INHABITANT_COLOR_WEIGHTING.get(MapConstants.TILE_INHABITANTS.ZOMBIE);
                double a;

                r = Math.clamp(r, 0, 1);
                g = Math.clamp(g, 0, 1);
                b = Math.clamp(b, 0, 1);
                a = Math.max(Math.max(r, g),b);

                pixel.setFill(new Color(r, g, b, a));
                base.getChildren().add(pixel);
                inhabitantPixelMap[x][y] = pixel;
            }
        }
        stage.setScene(scene);
        stage.setWidth(MapConstants.MAP_WIDTH * MapConstants.MAP_SCALE);
        stage.setHeight(MapConstants.MAP_HEIGHT * MapConstants.MAP_SCALE);
        stage.show();
    }

    public static void updateMap(){
        double startTime = System.currentTimeMillis();
        Tile[][] tileMap = App.worldMap.getTiles();
        // Group base = new Group();
        // Scene scene = new Scene(base, MapConstants.MAP_WIDTH * MapConstants.MAP_SCALE,
        //         MapConstants.MAP_HEIGHT * MapConstants.MAP_SCALE);
        // scene.setFill(Color.RED);
        // for (int x = 0; x < tileMap.length; x++) {
        //     for (int y = 0; y < tileMap[x].length; y++) {
        //         Rectangle pixel = biomePixelMap[x][y];
        //         pixel.setFill(MapConstants.BIOME_COLORS.get(tileMap[x][y].getBiome()));
        //         base.getChildren().add(pixel);
        //         biomePixelMap[x][y] = pixel;
        //     }
        // }

        for (int x = 0; x < tileMap.length; x++) {
            for (int y = 0; y < tileMap[x].length; y++) {
                Circle pixel = inhabitantPixelMap[x][y];
                double humanAmt = tileMap[x][y].getInhabitants().get(MapConstants.TILE_INHABITANTS.HUMAN);
                double infectedAmt = tileMap[x][y].getTotalInfected();
                humanAmt -= infectedAmt;
                double zombieAmt = tileMap[x][y].getInhabitants().get(MapConstants.TILE_INHABITANTS.ZOMBIE);

                double total = humanAmt+infectedAmt+zombieAmt;
                double b =  humanAmt/MapConstants.INHABITANT_COLOR_WEIGHTING.get(MapConstants.TILE_INHABITANTS.HUMAN);
                double g = infectedAmt/MapConstants.INHABITANT_COLOR_WEIGHTING.get(MapConstants.TILE_INHABITANTS.INFECTED);
                double r =  zombieAmt/MapConstants.INHABITANT_COLOR_WEIGHTING.get(MapConstants.TILE_INHABITANTS.ZOMBIE);
                double a;

                r = Math.clamp(r, 0, 1);
                g = Math.clamp(g, 0, 1);
                b = Math.clamp(b, 0, 1);
                a = Math.max(Math.max(r, g),b);

                // pixel.setFill(new Color(r, g, b, a));
                pixel.setFill(1,1,1,1);
                // base.getChildren().add(pixel);
                // inhabitantPixelMap[x][y] = pixel;
            }
        }
        System.out.println("GUI update took "+(System.currentTimeMillis() - startTime)+"ms");
        // mainStage.setScene(scene);
        
    }
}
