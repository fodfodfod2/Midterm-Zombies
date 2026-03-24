import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import constants.Constants.*;
import constants.Constants.MapConstants.TILE_INFRASTRUCTURE;

/**
 * Represents the world map composed of tiles with different biomes and inhabitants.
 * The map is generated using a drunkard's walk algorithm to create varied terrain.
 */
public class WorldMap {
    private Tile[][] mapTiles;
    private TILE_INFRASTRUCTURE[][] infrastructureMap;

    /**
     * Constructs a new WorldMap with the specified width and height.
     * The map is automatically generated upon creation.
     *
     * @param width the width of the map in tiles
     * @param height the height of the map in tiles
     */
    public WorldMap(int width, int height) {
        generateMap(width, height);
    }

    /**
     * Generates the map tiles with biomes using a drunkard's walk algorithm.
     * Starts with all tiles as water, then adds plains, swamp, and mountains.
     *
     * @param width the width of the map
     * @param height the height of the map
     */
    private void generateMap(int width, int height) {
        mapTiles = new Tile[width][height];
        infrastructureMap = new TILE_INFRASTRUCTURE[width][height];

        MapConstants.TILE_BIOMES[][] mapBiomes = new MapConstants.TILE_BIOMES[width][height];
        for (int x = 0; x < mapTiles.length; x++) {
            for (int y = 0; y < mapTiles[x].length; y++) {
                mapBiomes[x][y] = MapConstants.TILE_BIOMES.WATER;
            }
        }
        // do the drunkard walk to generate the biomes
        drunkardWalk(mapBiomes, MapConstants.TILE_BIOMES.PLAINS);
        System.out.println("generated plains");
        drunkardWalk(mapBiomes, MapConstants.TILE_BIOMES.SWAMP);
        System.out.println("generated swamp");
        drunkardWalk(mapBiomes, MapConstants.TILE_BIOMES.MOUNTAINS);
        System.out.println("generated mountains");
        
        //apply the biomes to the tile map
        for (int x = 0; x < mapTiles.length; x++) {
            for (int y = 0; y < mapTiles[x].length; y++) {
                mapTiles[x][y] = new Tile(mapBiomes[x][y], x, y);
            }
        }

        //roll for infrastructure on each tile
        for (int x = 0; x < mapTiles.length; x++) {
            for (int y = 0; y < mapTiles[x].length; y++) {
                MapConstants.TILE_INFRASTRUCTURE infrastructure = rollInfrastructure(mapTiles[x][y]);
                mapTiles[x][y].setInfrastructure(infrastructure);
                infrastructureMap[x][y] = infrastructure;
            }
        }

        startInfection();
                
    }

    /**
     * Performs a drunkard's walk algorithm to place a specific biome on the map.
     * The algorithm starts at random coordinates and expands in predefined shapes.
     *
     * @param mapBiomes the 2D array of biomes to modify
     * @param biome the biome type to place during this walk
     */
    private void drunkardWalk(MapConstants.TILE_BIOMES[][] mapBiomes, MapConstants.TILE_BIOMES biome) {
        double startTime = System.currentTimeMillis();
        for (int i = 0; i < MapConstants.DRUNKARD_WALK_ITERATIONS.get(biome); i++) {
            int[] coord = randomCoordinate();
            // check if the coordinate is valid, if not reroll, repeat until valid coordinate
            while (!((mapBiomes[coord[0]][coord[1]] == MapConstants.TILE_BIOMES.PLAINS 
                    || mapBiomes[coord[0]][coord[1]] == biome) && i < MapConstants.MAX_GROUPS.get(biome)) //if its a plains or biome, and its less than the max groups, valid
                    && !(mapBiomes[coord[0]][coord[1]] == biome && i >= MapConstants.MAX_GROUPS.get(biome)) // if its already the biome and more than max groups, valid
                    && !(biome == MapConstants.TILE_BIOMES.PLAINS && i < MapConstants.MAX_GROUPS.get(biome))) { // if the biome is plains and its less than max groups, valid
                coord = randomCoordinate();
            }
            int[][] shape = MapConstants.getDrunkardsWalkShape(coord[0], coord[1]);
            for (int j = 0; j < shape.length; j++) {
                mapBiomes[shape[j][0]][shape[j][1]] = biome;
            }
        }
        System.out.println("drunkard walk for " + biome + " took " + (System.currentTimeMillis() - startTime) + "ms");
    }

    /**
     * Returns a random coordinate within the map bounds in the form {x, y}.
     *
     * @return an array containing the x and y coordinates
     */
    private int[] randomCoordinate() {
        return new int[] { (int) (Math.random() * MapConstants.MAP_WIDTH), (int) (Math.random() * MapConstants.MAP_HEIGHT) };
    }

    /**
     * Returns the 2D array of tiles representing the world map.
     *
     * @return the map tiles
     */
    public Tile[][] getTiles() {
        return mapTiles;
    }

    private void startInfection(){
        while (true) {
            int[] coord = randomCoordinate();
            if (mapTiles[coord[0]][coord[1]].getBiome() != MapConstants.TILE_BIOMES.WATER){
                mapTiles[coord[0]][coord[1]].addInfectedPeople(50);
                break;
            }
        }
    }
    private MapConstants.TILE_INFRASTRUCTURE rollInfrastructure(Tile tile) {
        Map<MapConstants.TILE_INFRASTRUCTURE, Boolean> isValid = new HashMap<MapConstants.TILE_INFRASTRUCTURE, Boolean>(Map.of(
            MapConstants.TILE_INFRASTRUCTURE.PORT, false,
            MapConstants.TILE_INFRASTRUCTURE.AIRPORT, false,
            MapConstants.TILE_INFRASTRUCTURE.CITY, false,
            MapConstants.TILE_INFRASTRUCTURE.EMPTY, true) );

        if (tile.getBiome() == MapConstants.TILE_BIOMES.WATER) { //ports can only be on water next to plains
            ArrayList<Tile> adjacentTiles = getAdjacentTiles(tile.getCoordinates()[0], tile.getCoordinates()[1]);
            for (int i = 0; i < adjacentTiles.size(); i++) {
                if (adjacentTiles.get(i).getBiome() == MapConstants.TILE_BIOMES.PLAINS) {
                    isValid.put(MapConstants.TILE_INFRASTRUCTURE.PORT, true);
                    break;
                }
            }
        }

        if (tile.getBiome() == MapConstants.TILE_BIOMES.PLAINS) { //airports can only be on plains next to swamps or plains
            ArrayList<Tile> adjacentTiles = getAdjacentTiles(tile.getCoordinates()[0], tile.getCoordinates()[1]);
            for (int i = 0; i < adjacentTiles.size(); i++) {
                isValid.put(MapConstants.TILE_INFRASTRUCTURE.AIRPORT, true);
                if (!(adjacentTiles.get(i).getBiome() == MapConstants.TILE_BIOMES.PLAINS || adjacentTiles.get(i).getBiome() == MapConstants.TILE_BIOMES.SWAMP)) {
                    isValid.put(MapConstants.TILE_INFRASTRUCTURE.AIRPORT, false);
                    break;
                }
            }
        }

        if (tile.getBiome() == MapConstants.TILE_BIOMES.PLAINS || tile.getBiome() == MapConstants.TILE_BIOMES.SWAMP) { //cities can only be on plains or swamps
            isValid.put(MapConstants.TILE_INFRASTRUCTURE.CITY, true);
        }

        int total = 0;
        for (MapConstants.TILE_INFRASTRUCTURE infrastructure : MapConstants.TILE_INFRASTRUCTURE.values()) {
            if (isValid.get(infrastructure)) {
                total += MapConstants.INFRASTRUCTURE_SPAWN_RATES.get(infrastructure);
            }
        }

        double roll = Math.random() * total;
        for (MapConstants.TILE_INFRASTRUCTURE infrastructure : MapConstants.TILE_INFRASTRUCTURE.values()) {
            if (isValid.get(infrastructure)) {
                if (roll < MapConstants.INFRASTRUCTURE_SPAWN_RATES.get(infrastructure)) {
                    if (infrastructure != MapConstants.TILE_INFRASTRUCTURE.EMPTY){
                    System.out.println("TILE at " + tile.getCoordinates()[0] + ", " + tile.getCoordinates()[1] + " spawned with infrastructure " + infrastructure);
                    }
                    return infrastructure;
                } else {
                    roll -= MapConstants.INFRASTRUCTURE_SPAWN_RATES.get(infrastructure);
                }
            }
        }
        throw new IllegalStateException("Should never reach here, check infrastructure spawn rates and validity");

    }

    public ArrayList<Tile> getAdjacentTiles(int x, int y){
        ArrayList<Tile> adjacentTiles = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue; // skip the current tile
                if (x + dx < 0 || x + dx >= mapTiles.length || y + dy < 0 || y + dy >= mapTiles[0].length) continue; // skip out of bounds
                adjacentTiles.add(mapTiles[x + dx][y + dy]);
            }
        }
        return adjacentTiles;
    }

    private double computeDeltaHuman(double humanPopulation, double zombiePopulation) {
        double deltaHuman = 0.0;
        deltaHuman += SpreadConstants.CBS * humanPopulation; //humans born
        deltaHuman -= SpreadConstants.CSD * humanPopulation; //humans die naturally

        return deltaHuman;

    }

    private double computeDeltaZombie(double humanPopulation, double zombiePopulation) {
        double deltaZombie = 0.0;
        deltaZombie += SpreadConstants.CZD * humanPopulation * zombiePopulation / (humanPopulation + zombiePopulation); //zombies killed by survivors

        return deltaZombie;
    }

    private double computeInfected(double humanPopulation, double zombiePopulation) {
        double newInfected = 0.0;
        newInfected += SpreadConstants.CSI * humanPopulation * zombiePopulation / (humanPopulation + zombiePopulation); //humans infected by zombies

        return newInfected;
    }

    private double[] calculateZombieSpreadWeghting(int x, int y){
        double[] weighting = new double[2];
        for (int xCord = Math.max(0, x-5); xCord < Math.min(MapConstants.MAP_WIDTH,x+5); xCord++){
            for (int yCord = Math.max(0, y-5); yCord < Math.min(MapConstants.MAP_HEIGHT,y+5); yCord++){
                double dx = Math.max(xCord-x,.5);
                double dy = Math.max(yCord-y,.5);
                double pop = mapTiles[xCord][yCord].getInhabitants().get(MapConstants.TILE_INHABITANTS.HUMAN);
                weighting[0] += pop/dx;
                weighting[1] += pop/dy;
            }
        }
        // make both proportions
        weighting[0] = weighting[0] / (weighting[0]+weighting[1]);
        weighting[1] = weighting[1] / (weighting[0]+weighting[1]);
        return weighting;
    }

    public void periodicUpdate() {
        System.out.println("periodic");
        double startTime = System.currentTimeMillis();
        ArrayList<ArrayList<Map<MapConstants.TILE_INHABITANTS, Double>>> deltaInhabitants = new ArrayList<ArrayList<Map<MapConstants.TILE_INHABITANTS, Double>>>(); //normal array doesn't work with maps, so this mess will have to do instead
        double deltaStartTime = System.currentTimeMillis();
        for (int x = 0; x < mapTiles.length; x++) { 
            ArrayList<Map<MapConstants.TILE_INHABITANTS, Double>> column = new ArrayList<Map<MapConstants.TILE_INHABITANTS, Double>>(); //compute as tiles, then combine those into colums, then into one large deltaInhabitants array
            double increment = SpreadConstants.PERIODIC_INCREMENT; //initialize this at the start, then when we iterate through the tiles the first time, check if it needs to be reduced to prevent negative populations
            for (int y = 0; y < mapTiles[x].length; y++) {
                mapTiles[x][y].progressInfection(); // progress the infection on the tile, turning infected humans into zombies

                Map<MapConstants.TILE_INHABITANTS, Double> deltaInhabitantTile = new HashMap<MapConstants.TILE_INHABITANTS, Double>();
                deltaInhabitantTile.put(MapConstants.TILE_INHABITANTS.HUMAN, 0.0);
                deltaInhabitantTile.put(MapConstants.TILE_INHABITANTS.ZOMBIE, 0.0);
                
                Map<MapConstants.TILE_INHABITANTS, Double> currentInhabitants = new HashMap<MapConstants.TILE_INHABITANTS, Double>();
                currentInhabitants.put(MapConstants.TILE_INHABITANTS.HUMAN, 0.0);
                currentInhabitants.put(MapConstants.TILE_INHABITANTS.ZOMBIE, 0.0);
                for (Tile adjacentTile : getAdjacentTiles(x, y)) { //sum all the adjacent inhabitants and add them to the current tile's inhabitants with a weight
                    Map<MapConstants.TILE_INHABITANTS, Double> adjacentInhabitants = adjacentTile.getInhabitants();
                    // System.out.println(adjacentTile.getCoordinates()[0] + ", " + adjacentTile.getCoordinates()[1]);
                    currentInhabitants.put(MapConstants.TILE_INHABITANTS.HUMAN, currentInhabitants.get(MapConstants.TILE_INHABITANTS.HUMAN) + adjacentInhabitants.get(MapConstants.TILE_INHABITANTS.HUMAN)*SpreadConstants.ADJAECANT_HUMAN_COLLABORATION_WEIGHT);
                    currentInhabitants.put(MapConstants.TILE_INHABITANTS.ZOMBIE, currentInhabitants.get(MapConstants.TILE_INHABITANTS.ZOMBIE) + adjacentInhabitants.get(MapConstants.TILE_INHABITANTS.ZOMBIE)*SpreadConstants.ADJAECANT_ZOMBIE_COLLABORATION_WEIGHT);
                }

                double deltaHuman = computeDeltaHuman(currentInhabitants.get(MapConstants.TILE_INHABITANTS.HUMAN), currentInhabitants.get(MapConstants.TILE_INHABITANTS.ZOMBIE));
                double deltaZombie = computeDeltaZombie(currentInhabitants.get(MapConstants.TILE_INHABITANTS.HUMAN), currentInhabitants.get(MapConstants.TILE_INHABITANTS.ZOMBIE));
                double newInfected = computeInfected(currentInhabitants.get(MapConstants.TILE_INHABITANTS.HUMAN), currentInhabitants.get(MapConstants.TILE_INHABITANTS.ZOMBIE));
                
                //check if incrememnt needs to be reduced to prevent negative populations, if so reduce it
                double currentHumans = mapTiles[x][y].getInhabitants().get(MapConstants.TILE_INHABITANTS.HUMAN);
                double currentZombies = mapTiles[x][y].getInhabitants().get(MapConstants.TILE_INHABITANTS.ZOMBIE);
                double currentInfected = mapTiles[x][y].getTotalInfected();
                if (currentHumans < -deltaHuman * increment){
                    deltaHuman = -currentHumans / increment;
                } else if (currentZombies < -deltaZombie * increment){
                    deltaZombie = -currentZombies / increment;
                } else if ((currentHumans - currentInfected) < -newInfected * increment) {
                    newInfected = (currentHumans - currentInfected) / increment;
                }
                mapTiles[x][y].addInfectedPeople(newInfected);
                deltaInhabitantTile.put(MapConstants.TILE_INHABITANTS.HUMAN, deltaHuman);
                deltaInhabitantTile.put(MapConstants.TILE_INHABITANTS.ZOMBIE, deltaZombie);
                column.add(deltaInhabitantTile);
            }
            deltaInhabitants.add(column);
        }
        System.out.println("delta updates took "+(System.currentTimeMillis()-deltaStartTime)+"ms");
        
        double applyStartTime = System.currentTimeMillis();
        for (int x = 0; x < mapTiles.length; x++) { //apply the delta inhabitants to the map tiles
            for (int y = 0; y < mapTiles[x].length; y++) {
               
                Map<MapConstants.TILE_INHABITANTS, Double> deltaInhabitantTile = deltaInhabitants.get(x).get(y);
                mapTiles[x][y].getInhabitants().put(MapConstants.TILE_INHABITANTS.HUMAN, mapTiles[x][y].getInhabitants().get(MapConstants.TILE_INHABITANTS.HUMAN) + deltaInhabitantTile.get(MapConstants.TILE_INHABITANTS.HUMAN));
                mapTiles[x][y].getInhabitants().put(MapConstants.TILE_INHABITANTS.ZOMBIE, mapTiles[x][y].getInhabitants().get(MapConstants.TILE_INHABITANTS.ZOMBIE) + deltaInhabitantTile.get(MapConstants.TILE_INHABITANTS.ZOMBIE));
            }
        }
        System.out.println("apply updates took "+(System.currentTimeMillis()-applyStartTime)+"ms");
        double moveStartTime = System.currentTimeMillis();
        //have the zombie population move
        for (int x = 0; x < mapTiles.length; x++) {
            for (int y = 0; y < mapTiles[x].length; y++) {
                double[] weight = calculateZombieSpreadWeghting(x, y);
                double pop = mapTiles[x][y].getInhabitants().get(MapConstants.TILE_INHABITANTS.ZOMBIE);

                double dCorner = pop * Math.pow((weight[0]*weight[1])/(weight[0]+weight[1]),1.25);
                double dLeftRight = pop *  Math.pow(weight[0], 1.5);
                double dUpDown = pop *  Math.pow(weight[1], 1.5);
                double dCurrent = -(dCorner+dLeftRight+dUpDown);

                mapTiles[x][y].getInhabitants().put(MapConstants.TILE_INHABITANTS.ZOMBIE, mapTiles[x][y].getInhabitants().get(MapConstants.TILE_INHABITANTS.ZOMBIE)+dCurrent);
                mapTiles[x+(int) (dLeftRight/Math.abs(dLeftRight))][y].getInhabitants().put(MapConstants.TILE_INHABITANTS.ZOMBIE, mapTiles[x][y].getInhabitants().get(MapConstants.TILE_INHABITANTS.ZOMBIE)+dLeftRight);
                mapTiles[x][y+(int) (dUpDown/Math.abs(dUpDown))].getInhabitants().put(MapConstants.TILE_INHABITANTS.ZOMBIE, mapTiles[x][y].getInhabitants().get(MapConstants.TILE_INHABITANTS.ZOMBIE)+dUpDown);
                mapTiles[x+(int) (dLeftRight/Math.abs(dLeftRight))][y+(int) (dUpDown/Math.abs(dUpDown))].getInhabitants().put(MapConstants.TILE_INHABITANTS.ZOMBIE, mapTiles[x][y].getInhabitants().get(MapConstants.TILE_INHABITANTS.ZOMBIE)+dCorner);

            }
        }
        System.out.println("movement updates took "+(System.currentTimeMillis()-moveStartTime)+"ms");


        if (GeneralConstants.DEBUG) {
            for (int x = 0; x < mapTiles.length; x++) {
                for (int y = 0; y < mapTiles[x].length; y++) {
                    Map<MapConstants.TILE_INHABITANTS, Double> inhabitants = mapTiles[x][y].getInhabitants();
                    if (inhabitants.get(MapConstants.TILE_INHABITANTS.HUMAN) < 0){
                        System.out.println("Negative humans at " + x + ", " + y);
                    } if (inhabitants.get(MapConstants.TILE_INHABITANTS.ZOMBIE) < 0){
                        System.out.println("Negative zombies at " + x + ", " + y);
                    } if (mapTiles[x][y].getTotalInfected() < 0){
                        System.out.println("Negative infected at " + x + ", " + y);
                    } if (mapTiles[x][y].getTotalInfected() > inhabitants.get(MapConstants.TILE_INHABITANTS.HUMAN)){
                        System.out.println("More infected than humans at " + x + ", " + y);
                    }
                }
            }
        
        System.out.println("Periodic update took " + (System.currentTimeMillis() - startTime) + "ms");
        System.out.println("Periodic update took approxamately" + ((System.currentTimeMillis()-startTime)/(MapConstants.MAP_WIDTH*MapConstants.MAP_HEIGHT)) + "ms per tile");
        }
    }
}
