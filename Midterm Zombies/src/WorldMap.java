import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;

import javax.swing.plaf.metal.MetalComboBoxUI.MetalPropertyChangeListener;

import constants.Constants.*;
import constants.Constants.MapConstants.TILE_INFRASTRUCTURE;
import constants.Constants.MapConstants.TILE_INHABITANTS;

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
                mapTiles[coord[0]][coord[1]].addHumans(MapConstants.INIT_INFECTED);
                mapTiles[coord[0]][coord[1]].addInfectedPeople(MapConstants.INIT_INFECTED);
                System.out.println("infection started at "+coord[0]+","+coord[1]);
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
                    // System.out.println("TILE at " + tile.getCoordinates()[0] + ", " + tile.getCoordinates()[1] + " spawned with infrastructure " + infrastructure);
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
        if (humanPopulation + zombiePopulation <= 0 || zombiePopulation <= 0){
            return 0;
        }
        deltaZombie -= SpreadConstants.CZD * humanPopulation * zombiePopulation / (humanPopulation + zombiePopulation); //zombies killed by survivors

        return deltaZombie;
    }

    private double computeInfected(double humanPopulation, double zombiePopulation) {
        double newInfected = 0.0;
        if (humanPopulation + zombiePopulation == 0){
            return 0;
        }
        newInfected += SpreadConstants.CSI * humanPopulation * zombiePopulation / (humanPopulation + zombiePopulation); //humans infected by zombies
        if (Double.isNaN(newInfected)){
            System.out.println("added NAN newInfected in the compute function");
            System.exit(1);
        } else if (newInfected < 0){
            System.out.println("Negative newInfected at"+humanPopulation+","+zombiePopulation);
            System.exit(1);
        }
        return newInfected;
    }

    private double[] calculateZombieSpreadWeghting(int x, int y){
        double[] weighting = new double[2];
        for (int xCord = Math.max(0, x-GeneralConstants.ZOMBIE_WEIGHTING_RADIUS); xCord < Math.min(MapConstants.MAP_WIDTH, x+GeneralConstants.ZOMBIE_WEIGHTING_RADIUS); xCord++){
            for (int yCord = Math.max(0, y-GeneralConstants.ZOMBIE_WEIGHTING_RADIUS); yCord < Math.min(MapConstants.MAP_HEIGHT, y+GeneralConstants.ZOMBIE_WEIGHTING_RADIUS); yCord++){
                int xCoef = 1;
                int yCoef = 1;

                if (x==0 && y == 0){
                    continue;
                }
                if (xCord-x < 0){
                    xCoef = -1;
                } if (yCord-y < 0){
                    yCoef = -1;
                }
                double dx = xCoef * Math.max(Math.abs(xCord-x), .5);
                double dy = yCoef * Math.max(Math.abs(yCord-y), .5);
                double humanPopulation = mapTiles[xCord][yCord].getHumans();
                double zombiePopulation = mapTiles[xCord][yCord].getZombies();
                weighting[0] += humanPopulation/dx;
                weighting[1] += humanPopulation/dy;
                weighting[0] += .0000005 * zombiePopulation/dx;
                weighting[1] += .0000005 * zombiePopulation/dy;
            }
        }
        // make both proportions
        // System.out.println("weighting:"+weighting[0]+","+weighting[1]);
        double total = weighting[0] + weighting[1];
        if (total == 0){
            // if (mapTiles[x][y].getZombies()>0){
            //     GeneralConstants.ZOMBIE_WEIGHTING_RADIUS += 1;
            // }
            return new double[] {0,0};
        }

        weighting[0] = weighting[0] / total;
        weighting[1] = weighting[1] / total;

        if (Double.isNaN(weighting[0]) || Double.isNaN(weighting[1])) {
            return new double[] {0,0};
        }

        return weighting;
    }

    public void periodicUpdate() {
        System.out.println("periodic");
        double startTime = System.currentTimeMillis();
        ArrayList<ArrayList<Map<MapConstants.TILE_INHABITANTS, Double>>> deltaInhabitants = new ArrayList<ArrayList<Map<MapConstants.TILE_INHABITANTS, Double>>>(); //normal array doesn't work with maps, so this mess will have to do instead
        double deltaStartTime = System.currentTimeMillis();
        double increment = SpreadConstants.PERIODIC_INCREMENT; //initialize this at the start, then when we iterate through the tiles the first time, check if it needs to be reduced to prevent negative populations
        for (int x = 0; x < mapTiles.length; x++) { 
            ArrayList<Map<MapConstants.TILE_INHABITANTS, Double>> column = new ArrayList<Map<MapConstants.TILE_INHABITANTS, Double>>(); //compute as tiles, then combine those into colums, then into one large deltaInhabitants array
            for (int y = 0; y < mapTiles[x].length; y++) {
                mapTiles[x][y].progressInfection(); // progress the infection on the tile, turning infected humans into zombies

                Map<MapConstants.TILE_INHABITANTS, Double> deltaInhabitantTile = new HashMap<MapConstants.TILE_INHABITANTS, Double>();
                deltaInhabitantTile.put(MapConstants.TILE_INHABITANTS.HUMAN, 0.0);
                deltaInhabitantTile.put(MapConstants.TILE_INHABITANTS.ZOMBIE, 0.0);
                

                double deltaHuman = computeDeltaHuman(mapTiles[x][y].getHumans(), mapTiles[x][y].getZombies());
                double deltaZombie = computeDeltaZombie(mapTiles[x][y].getHumans(), mapTiles[x][y].getZombies());
                double newInfected = computeInfected(mapTiles[x][y].getHumans(),  mapTiles[x][y].getZombies());
                
                //check if incrememnt needs to be reduced to prevent negative populations, if so reduce it
                double currentHumans = mapTiles[x][y].getHumans();
                double currentZombies = mapTiles[x][y].getZombies();
                double currentInfected = mapTiles[x][y].getTotalInfected();
                if (currentHumans< deltaHuman * increment){
                    if (GeneralConstants.DEBUG){
                    System.out.println("IncrementProtection detected an error with the humans at"+x+","+y+", new increment is "+-currentHumans/deltaHuman);
                    System.out.println("Humans currPop:"+currentHumans+"   deltaPop:"+deltaHuman);}
                    increment = -currentHumans / deltaHuman;
                }
                if (currentZombies < deltaZombie * increment){
                    if (GeneralConstants.DEBUG){
                    System.out.println("IncrementProtection detected an error with the zombies at"+x+","+y+", new increment is "+-currentZombies/deltaZombie);
                    System.out.println("Zombies currPop:"+currentZombies+"   deltaPop:"+deltaZombie);}
                    increment = -currentZombies / deltaZombie;
                } 
                if ((currentHumans - currentInfected) < -newInfected * increment) {
                    if (currentHumans - currentInfected > -GeneralConstants.NEGATIVE_POPULATION_TOLERANCE){
                        mapTiles[x][y].addHumans(-(currentHumans - currentInfected+GeneralConstants.NEGATIVE_POPULATION_TOLERANCE));  
                    }
                    if (GeneralConstants.DEBUG){
                        double safeIncrement = newInfected > 0 ? -(currentHumans - currentInfected) / newInfected : SpreadConstants.PERIODIC_INCREMENT;
                        System.out.println("IncrementProtection detected an error with the infected at"+x+","+y+", new increment is "+safeIncrement);
                        System.out.println("Infected currPop:"+(currentHumans-currentInfected)+"   deltaPop:"+newInfected);
                    }

                    if (newInfected > 0) {
                        increment = -(currentHumans - currentInfected) / newInfected;
                    } else {
                        increment = Math.max(SpreadConstants.PERIODIC_INCREMENT * 0.01, 1e-6);
                    }
                }
                if (increment <= 0 || Double.isNaN(increment) || Double.isInfinite(increment)){
                    System.out.println("ERR Increment = "+increment);
                    System.exit(-1);
                }
                mapTiles[x][y].addInfectedPeople(increment * newInfected);
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
                mapTiles[x][y].addHumans(increment * deltaInhabitantTile.get(MapConstants.TILE_INHABITANTS.HUMAN));
                mapTiles[x][y].addZombies(increment * deltaInhabitantTile.get(MapConstants.TILE_INHABITANTS.ZOMBIE));
            }
        }
        System.out.println("apply updates took "+(System.currentTimeMillis()-applyStartTime)+"ms");
        double moveStartTime = System.currentTimeMillis();
        //have the zombie population move
        for (int x = 0; x < mapTiles.length; x++) {
            for (int y = 0; y < mapTiles[x].length; y++) {
                if (mapTiles[x][y].getZombies() <= 0){
                    continue;
                }
                double[] weight = calculateZombieSpreadWeghting(x, y);
                if (!Double.isFinite(weight[0]) || !Double.isFinite(weight[1]) || weight[0] + weight[1] <= 0) {
                    continue;
                }
                double pop = mapTiles[x][y].getZombies();

                double dCorner = 0;
                double dLeftRight = 0;
                double dUpDown = 0;

                // allow movements in both signs (left/right and up/down), not only positive directions.
                double absX = Math.abs(weight[0]);
                double absY = Math.abs(weight[1]);
                if (absX + absY > 0) {
                    dCorner = pop * Math.pow((absX*absY)/(absX + absY), 1.25) * increment;
                    dLeftRight = pop * Math.pow(absX, 1.5) * increment;
                    dUpDown = pop * Math.pow(absY, 1.5) * increment;
                }

                int dirLR = (int) Math.signum(weight[0]);
                int dirUD = (int) Math.signum(weight[1]);

                if (x + dirLR >= 0 && x + dirLR < mapTiles.length && y + dirUD >= 0 && y + dirUD < mapTiles[x].length) {
                    dCorner *= MapConstants.BIOME_SPREAD_COEFFICIENT.get(mapTiles[x+dirLR][y+dirUD].getBiome());
                } else {
                    dCorner = 0;
                }
                if (x + dirLR >= 0 && x + dirLR < mapTiles.length) {
                    dLeftRight *= MapConstants.BIOME_SPREAD_COEFFICIENT.get(mapTiles[x+dirLR][y].getBiome());
                } else {
                    dLeftRight = 0;
                }
                if (y + dirUD >= 0 && y + dirUD < mapTiles[x].length) {
                    dUpDown *= MapConstants.BIOME_SPREAD_COEFFICIENT.get(mapTiles[x][y+dirUD].getBiome());
                } else {
                    dUpDown = 0;
                }

                double dCurrent = -(dCorner + dLeftRight + dUpDown);
                mapTiles[x][y].addZombies(dCurrent);

                if (dLeftRight > 0 && x + dirLR >= 0 && x + dirLR < mapTiles.length) {
                    mapTiles[x+dirLR][y].addZombies(dLeftRight);
                }
                if (dUpDown > 0 && y + dirUD >= 0 && y + dirUD < mapTiles[x].length) {
                    mapTiles[x][y+dirUD].addZombies(dUpDown);
                }
                if (dCorner > 0 && x + dirLR >= 0 && x + dirLR < mapTiles.length && y + dirUD >= 0 && y + dirUD < mapTiles[x].length) {
                    mapTiles[x+dirLR][y+dirUD].addZombies(dCorner);
                }

            }
        }
        System.out.println("movement updates took "+(System.currentTimeMillis()-moveStartTime)+"ms");


        if (GeneralConstants.DEBUG) {
            double totalHuman = 0;
            double totalZombie = 0;
            double totalInfected = 0;
            for (int x = 0; x < mapTiles.length; x++) {
                for (int y = 0; y < mapTiles[x].length; y++) {
                    if (Double.isNaN(mapTiles[x][y].getHumans()) ||
                        Double.isNaN(mapTiles[x][y].getZombies()) ||
                        Double.isNaN(mapTiles[x][y].getTotalInfected())){
                            System.out.println("SOMETHING HAS GONE HORRIBLY WRONG AT "+x+","+y);
                            System.out.println(Double.isNaN(mapTiles[x][y].getHumans()));
                            System.out.println(Double.isNaN(mapTiles[x][y].getZombies()));
                            System.out.println(Double.isNaN(mapTiles[x][y].getTotalInfected()));

                            
                            System.exit(1);
                    }
                    totalHuman += mapTiles[x][y].getHumans();
                    totalZombie += mapTiles[x][y].getZombies();
                    totalInfected += mapTiles[x][y].getTotalInfected();
                    if (mapTiles[x][y].getHumans() < 0 - GeneralConstants.NEGATIVE_POPULATION_TOLERANCE){
                        System.out.println("Negative humans at " + x + ", " + y);
                        System.out.println("Humans:"+mapTiles[x][y].getHumans()+"    Zombies:"+mapTiles[x][y].getZombies()+"    Infected:"+mapTiles[x][y].getTotalInfected());
                        System.exit(1);
                    } if (mapTiles[x][y].getZombies() < 0 - GeneralConstants.NEGATIVE_POPULATION_TOLERANCE){
                        System.out.println("Negative zombies at " + x + ", " + y);
                        System.out.println("Humans:"+mapTiles[x][y].getHumans()+"    Zombies:"+mapTiles[x][y].getZombies()+"    Infected:"+mapTiles[x][y].getTotalInfected());
                        System.exit(1);
                    } if (mapTiles[x][y].getTotalInfected() < 0 - GeneralConstants.NEGATIVE_POPULATION_TOLERANCE){
                        System.out.println("Negative infected at " + x + ", " + y);
                        System.exit(1);
                    } if (mapTiles[x][y].getTotalInfected() > mapTiles[x][y].getHumans()){
                        if (mapTiles[x][y].getHumans() - mapTiles[x][y].getTotalInfected() > -GeneralConstants.NEGATIVE_POPULATION_TOLERANCE){
                        mapTiles[x][y].addHumans(-(mapTiles[x][y].getHumans() - mapTiles[x][y].getTotalInfected()));  
                        }  else{
                        System.out.println("More infected than humans at " + x + ", " + y);
                        System.out.println("Humans:"+mapTiles[x][y].getHumans()+"    Zombies:"+mapTiles[x][y].getZombies()+"    Infected:"+mapTiles[x][y].getTotalInfected());
                        System.exit(1);
                        }
                    }
                }
            }
        System.out.println("Total Human:"+totalHuman);
        System.out.println("Total Zombie:"+totalZombie);
        System.out.println("Total Infected:"+totalInfected);
        
        System.out.println("Periodic update took " + (System.currentTimeMillis() - startTime) + "ms");
        System.out.println("Periodic update took approxamately" + ((System.currentTimeMillis()-startTime)/(MapConstants.MAP_WIDTH*MapConstants.MAP_HEIGHT)) + "ms per tile");
        }
    }
}
