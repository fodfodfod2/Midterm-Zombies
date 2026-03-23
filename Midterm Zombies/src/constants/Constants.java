package constants;
import java.lang.classfile.constantpool.DoubleEntry;
import java.util.HashMap;
import java.util.Map;

import constants.Constants.MapConstants.TILE_BIOMES;
import javafx.scene.paint.Color;

public class Constants {
    public static class GeneralConstants {
        /**
         * whether to print debug statements to the console, which can be used for performance testing and debugging
         */
        public static final boolean DEBUG = false;
    }

    public static class MapConstants {   
        /**
         * Enumeration of possible biome types for tiles in the world map.
         */
        public enum TILE_BIOMES {
            WATER, PLAINS, MOUNTAINS, SWAMP
        };

        /**
         * Enumeration of possible inhabitant types that can occupy tiles.
         */
        public enum TILE_INHABITANTS {
            HUMAN, ZOMBIE, INFECTED, EMPTY
        };

        /**
         * Enumeration of possible infrastructure types that can be built on tiles.
         * Currently unused but reserved for future expansion.
         */
        public enum TILE_INFRASTRUCTURE {
            PORT, AIRPORT, CITY, EMPTY
        }

        /**
         * the odds for any given tile to spawn with infrastructure on it, works as a loot pool, where the odds are val/total
         * 
         * infrastructure is rolled in the following order
         * city, airport, port, empty
         */
        public static final Map<TILE_INFRASTRUCTURE, Integer> INFRASTRUCTURE_SPAWN_RATES = Map.of(
            TILE_INFRASTRUCTURE.PORT, 1,
            TILE_INFRASTRUCTURE.AIRPORT, 1,
            TILE_INFRASTRUCTURE.CITY, 5,
            TILE_INFRASTRUCTURE.EMPTY, 100);
            
        

        /**
         * Mapping of biome types to their display colors for GUI rendering.
         */
        public static final Map<TILE_BIOMES, Color> BIOME_COLORS = Map.of(
                TILE_BIOMES.WATER, Color.SLATEBLUE,
                TILE_BIOMES.PLAINS, Color.LIGHTGREEN,
                TILE_BIOMES.MOUNTAINS, Color.GRAY,
                TILE_BIOMES.SWAMP, Color.BURLYWOOD);
        
        public static final Map<TILE_INHABITANTS, Double> INHABITANT_COLOR_WEIGHTING = Map.of(
                TILE_INHABITANTS.HUMAN, 500.0,
                TILE_INHABITANTS.ZOMBIE, 200.0,
                TILE_INHABITANTS.INFECTED, 100.0);
        
        public static final Map<TILE_BIOMES, Double> BIOME_SPREAD_COEFFICIENT = Map.of(
                TILE_BIOMES.WATER, 0.0,
                TILE_BIOMES.PLAINS, 1.0,
                TILE_BIOMES.MOUNTAINS, 0.1,
                TILE_BIOMES.SWAMP, 0.35);

        /**
         * Mapping of inhabitant types to their display colors for GUI rendering.
         */
        public static final Map<TILE_INHABITANTS, Color> INHABITANT_COLORS = Map.of(
                TILE_INHABITANTS.HUMAN, Color.CYAN,
                TILE_INHABITANTS.ZOMBIE, Color.RED,
                TILE_INHABITANTS.INFECTED, Color.ORANGE);

        /**
         * Mapping of inhabitant types to their display names for UI and logging.
         */
        public static final Map<TILE_INHABITANTS, String> INHABITANT_NAMES = Map.of(
                TILE_INHABITANTS.HUMAN, "Humans",
                TILE_INHABITANTS.ZOMBIE, "Zombies",
                TILE_INHABITANTS.INFECTED, "Infected");

        /**
         * Mapping of biome types to their display names for UI and logging.
         */
        public static final Map<TILE_BIOMES, String> BIOME_NAMES = Map.of(
                TILE_BIOMES.WATER, "Water",
                TILE_BIOMES.PLAINS, "Plains",
                TILE_BIOMES.MOUNTAINS, "Mountains",
                TILE_BIOMES.SWAMP, "Swamp");

        
        /**
         * The scale factor for rendering the map (currently unused).
         */
        public static final int MAP_SCALE = 8;

        /**
         * The width of the world map in tiles.
         */
        public static final int MAP_WIDTH = 1920/MAP_SCALE;

        /**
         * The height of the world map in tiles.
         */
        public static final int MAP_HEIGHT = 1080/MAP_SCALE;

        /**
         * Maximum number of groups/clusters allowed for each biome type during map generation.
         */
        public static final Map<TILE_BIOMES, Integer> MAX_GROUPS = Map.of(
            TILE_BIOMES.PLAINS, 2,
            TILE_BIOMES.MOUNTAINS, 25,
            TILE_BIOMES.SWAMP, 2);
        
        /**
         * Number of iterations for the drunkard's walk algorithm for each biome type.
         * Determines how extensively each biome spreads across the map.
         */
        public static final Map<TILE_BIOMES, Integer> DRUNKARD_WALK_ITERATIONS = Map.of(
                TILE_BIOMES.PLAINS, (int) Math.pow((int) (.35*(MAP_WIDTH*MAP_HEIGHT)),1.25),
                TILE_BIOMES.MOUNTAINS, (int) Math.pow((int) (.001*(MAP_WIDTH*MAP_HEIGHT)),1.25),
                TILE_BIOMES.SWAMP, (int) Math.pow((int) (.025*(MAP_WIDTH*MAP_HEIGHT)),1.25));

        /**
         * The base shape template used for the drunkard's walk algorithm.
         * Represents relative coordinates from a starting point to form biome clusters.
         */
        public static final int[][] base_drunkard_shape = new int[][] {
                new int[] { 0, 0 },
                new int[] { 1, 0 },
                new int[] { -1, 2 },
                new int[] { 0, 2 },
                new int[] { 1, 2 },
                new int[] { 2, 3 },
                new int[] { 1, 1 },
                new int[] { 0, 1 },
                new int[] { -1, 1 },
                new int[] { -1, 0 },
                new int[] { -1, -1 },
                new int[] { 0, -1 },
                new int[] { 1, -1 }
        };

        /**
         * Generates a 2D array of coordinates representing a drunkard's walk shape.
         * The shape is created by simulating a random walk starting from the center of
         * the map.
         * 
         * @param x the starting x-coordinate of the walk
         * @param y the starting y-coordinate of the walk
         * 
         * @return a 2D array of coordinates in the form of {{x1, y1}, {x2, y2}, ...}
         */
        public static int[][] getDrunkardsWalkShape(int x, int y) {
            int rotation = (int) (Math.random() * 4);
            int[][] result = new int[base_drunkard_shape.length][2];
            for (int i = 0; i < base_drunkard_shape.length; i++) {
                int[] shape = new int[] { base_drunkard_shape[i][0], base_drunkard_shape[i][1] };
                for (int foo = 0; foo < rotation; foo++) {
                    int temp = shape[0];
                    shape[0] = shape[1];
                    shape[1] = -temp;
                }
                result[i][0] = x + shape[0];
                result[i][1] = y + shape[1];

                if (result[i][0] < 0) {
                    result[i][0] = 0;
                } else if (result[i][0] >= MAP_WIDTH) {
                    result[i][0] = MAP_WIDTH - 1;
                }

                if (result[i][1] < 0) {
                    result[i][1] = 0;
                } else if (result[i][1] >= MAP_HEIGHT) {
                    result[i][1] = MAP_HEIGHT - 1;
                }
            }
            return result;
        }
    }

    public static class SpreadConstants {
        /**
         * what percentage of interactions between humans and zombies result in a infected human, from 0 to 1
         */
        public static final double CSI = 0.7;

        /**
         * the base increment for periodic updates
         */
        public static final double PERIODIC_INCREMENT = .1;

        /**
         * what percentage of interactions between zombies and humans result in a dead zombie, from 0 to 1
         */
        public static final double CZD = 0.5;

        /**
         * what percentage of humans produce 1 child, from 0 to 1, per day
         */
        public static final double CBS = 350000/8000000000.0; // 350k births per day, divided by 8 billion people

        /**
         * what percent of the survivors die each day from non-zombie related causes, from 0 to 1
         */
        public static final double CSD = .05;//160000/8000000000.0; // 160k deaths per day, divided by 8 billion people

        /**
         * the mean time it takes for an infected human to turn into a zombie, in days
         */
        public static final int MEAN_TIME_FROM_INFECTION_TO_ZOMBIE = 3; // in days

        /**
         * the standard deviation of the time it takes for an infected human to turn into a zombie, in days
         */
        public static final int STDEV_TIME_FROM_INFECTION_TO_ZOMBIE = 2; // in days

        /**
         * the number of groups a infected amount is split into when deciding infected time
         */
        public static final int INFECTION_ITERATIONS = 5;

        /**
         * the weight of adjacent humans in determining human how much they matter for interactions
         */
        public static final double ADJAECANT_HUMAN_COLLABORATION_WEIGHT = 0.1;

        /**
         * the weight of adjacent zombies in determining zombie how much they matter for interactions
         */
        public static final double ADJAECANT_ZOMBIE_COLLABORATION_WEIGHT = 0.1;

        /**
         * how fast zombies spread to adjacent tiles
         */
        public static final double ZOMBIE_SPREAD_WEIGHT = 0.5;
    }

    public static class PopulationConstants {
        /**
         * the minimum amount of Zombies that count a tile as occupied(eg if this is set to 10, and there are 15 zombies on a tile, the zombies will gain the food from this tile)
         */
        public static final double ZOMBIE_POPULATION_MIN_CAPACITY = 100;

        /**
         * the average number of calories consumed per zombie per day
         */
        public static final int ZOMBIE_CALORIES_PER_DAY = 500;

        /**
         * the percentage of calories that can be obtained from consuming a human
         */
        public static final int CALORIES_FROM_HUMAN = 125000;

        /**
         * the amount of calories that zombies can harvest/consume from each biome type, per day
         */
        public static final Map<TILE_BIOMES,Integer> ZOMBIE_HARVESTABLE_CALORIES = Map.of(
            TILE_BIOMES.PLAINS, 100000,
            TILE_BIOMES.MOUNTAINS, 50000,
            TILE_BIOMES.SWAMP, 75000);
        }
}
