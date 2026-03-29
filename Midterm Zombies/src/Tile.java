import java.util.HashMap;
import java.util.Map;
import constants.Constants.*;
import constants.Constants.MapConstants.TILE_INFRASTRUCTURE;
import constants.Constants.MapConstants.TILE_INHABITANTS;
/**
 * Represents a tile in the game world with inhabitants, biome, and inhabitant strength.
 */
public class Tile {
    private Map<MapConstants.TILE_INHABITANTS, Double> inhabitants;

    private double[] infectedPopulations;

    private MapConstants.TILE_BIOMES biome;

    private MapConstants.TILE_INFRASTRUCTURE infrastructure = null;

    public void addInfectedPeople(double amt) {
        if (Double.isNaN(amt)){
            System.out.println("added NAN infected");
            System.exit(1);
        }
        for (int i = 0; i < SpreadConstants.INFECTION_ITERATIONS; i++) {
            infectedPopulations[(int) (SpreadConstants.MEAN_TIME_FROM_INFECTION_TO_ZOMBIE+Math.pow(Math.random()*Math.pow(SpreadConstants.STDEV_TIME_FROM_INFECTION_TO_ZOMBIE,2),.5))] += amt / SpreadConstants.INFECTION_ITERATIONS;
        }
    }

    public void progressInfection() {
        
        double newZombies = infectedPopulations[0];
        if (Double.isNaN(newZombies)){
            System.out.println("added NAN newZombies");
            System.exit(1);
        } else if (newZombies < 0){
            System.out.println("Negative newZombies at"+coordinates[0]+","+coordinates[1]);
            System.exit(1);
        }
        for (int i = 1; i < infectedPopulations.length; i++) {
            infectedPopulations[i-1] = infectedPopulations[i];
        }
        infectedPopulations[infectedPopulations.length-1] = 0;

        addZombies(newZombies);
        addHumans(-newZombies);
        // System.out.println("h:"+inhabitants.get(TILE_INHABITANTS.HUMAN)+"     z:"+inhabitants.get(TILE_INHABITANTS.ZOMBIE));
    }

    private int[] coordinates;
    /**
     * Constructs a new Tile with the specified inhabitants, biome, and inhabitant strength.
     *
     * @param biome the biome type of this tile
     */
    public Tile(MapConstants.TILE_BIOMES biome, int xCoordinate, int yCoordinate) {
        this.biome = biome;
        this.coordinates = new int[]{xCoordinate, yCoordinate};

        inhabitants = new HashMap<MapConstants.TILE_INHABITANTS, Double>(Map.of(
            MapConstants.TILE_INHABITANTS.HUMAN, 0.0,
            MapConstants.TILE_INHABITANTS.ZOMBIE, 0.0));
        
        infectedPopulations = new double[SpreadConstants.INFECTION_ITERATIONS+SpreadConstants.STDEV_TIME_FROM_INFECTION_TO_ZOMBIE];
    }

    /**
     * Returns a string representation of this tile, including biome, inhabitants, and strength.
     *
     * @return a descriptive string of the tile's properties
     */
    @Override
    public String toString(){
        return (MapConstants.BIOME_NAMES.get(biome)+" tile ");
    }

    /**
     * Gets the biome of this tile.
     *
     * @return the biome type
     */
    public MapConstants.TILE_BIOMES getBiome(){
        return biome;
    }

    public MapConstants.TILE_INFRASTRUCTURE getInfrastructure() {
        return infrastructure;
    }

    public void setInfrastructure(MapConstants.TILE_INFRASTRUCTURE infrastructure) {
        this.infrastructure = infrastructure;
        if (infrastructure == TILE_INFRASTRUCTURE.CITY){
            inhabitants.put(TILE_INHABITANTS.HUMAN, 500.0);
        }
    }

    /**
     * Sets the biome of this tile.
     *
     * @param biome the new biome type
     */
    public void setBiome(MapConstants.TILE_BIOMES biome){
        this.biome = biome;
    }

    public Map<MapConstants.TILE_INHABITANTS, Double> getInhabitants() {
        return inhabitants;
    }

    public int[] getCoordinates() {
        return coordinates;
    }
    
    public double getHumans(){
        if (inhabitants.get(TILE_INHABITANTS.HUMAN) < GeneralConstants.NEGATIVE_POPULATION_TOLERANCE){
            inhabitants.put(TILE_INHABITANTS.HUMAN, 0.0);
        }
        return inhabitants.get(TILE_INHABITANTS.HUMAN);
    }

    public double getZombies(){
        if (inhabitants.get(TILE_INHABITANTS.ZOMBIE) < GeneralConstants.NEGATIVE_POPULATION_TOLERANCE){
            inhabitants.put(TILE_INHABITANTS.ZOMBIE, 0.0);
        }
        return inhabitants.get(TILE_INHABITANTS.ZOMBIE);
    }

    public void addHumans(double amt){
        if (Double.isNaN(amt)){
            System.out.println("added NAN Humans");
            System.exit(1);
        }
        if (amt > 0){
            inhabitants.put(TILE_INHABITANTS.HUMAN, inhabitants.get(TILE_INHABITANTS.HUMAN)+amt);
        } else if (amt < 0){
            double proportionInfected = getTotalInfected() / getHumans();
            inhabitants.put(TILE_INHABITANTS.HUMAN, inhabitants.get(TILE_INHABITANTS.HUMAN)+amt / proportionInfected);


            double totalInfected = getTotalInfected();
            for (int i = 0; i < infectedPopulations.length; i++){
                double proportionOfInfected = infectedPopulations[i] / totalInfected;
                infectedPopulations[i] += amt * proportionInfected*proportionOfInfected;
            }
        }
    }

    public void addZombies(double amt){
        if (Double.isNaN(amt)){
            System.out.println("added NAN Zombies");
            System.exit(1);
        }

        inhabitants.put(TILE_INHABITANTS.ZOMBIE, getZombies()+amt);
    }


    public double getTotalInfected() {
        double totalInfected = 0;
        for (double infected : infectedPopulations) {
            if (Double.isNaN(infected)){
                System.out.println("faaaaaaa");
                System.exit(1);
            }
            if  (infected < 0){
                System.out.println("Negative infected at" + coordinates[0]+","+coordinates[1]);
                System.exit(1);
            }
            totalInfected += infected;
        }
        return totalInfected;
    }

    
}
