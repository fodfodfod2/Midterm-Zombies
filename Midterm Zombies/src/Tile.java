import java.util.HashMap;
import java.util.Map;
import constants.Constants.*;
import constants.Constants.MapConstants.TILE_INFRASTRUCTURE;
import constants.Constants.MapConstants.TILE_INHABITANTS;
/**
 * Represents a tile in the game world with inhabitants, biome, and inhabitant strength.
 */
public class Tile {
    private Map<MapConstants.TILE_INHABITANTS, Double> inhabitants = new HashMap<MapConstants.TILE_INHABITANTS, Double>(Map.of(
            MapConstants.TILE_INHABITANTS.HUMAN, 0.0,
            MapConstants.TILE_INHABITANTS.ZOMBIE, 0.0));

    private double[] infectedPopulations = new double[SpreadConstants.INFECTION_ITERATIONS+SpreadConstants.STDEV_TIME_FROM_INFECTION_TO_ZOMBIE];

    private MapConstants.TILE_BIOMES biome;

    private MapConstants.TILE_INFRASTRUCTURE infrastructure = null;

    public void addInfectedPeople(double amt) {
        for (int i = 0; i < SpreadConstants.INFECTION_ITERATIONS; i++) {
            infectedPopulations[(int) (SpreadConstants.MEAN_TIME_FROM_INFECTION_TO_ZOMBIE+Math.pow(Math.random()*Math.pow(SpreadConstants.STDEV_TIME_FROM_INFECTION_TO_ZOMBIE,2),.5))] += amt / SpreadConstants.INFECTION_ITERATIONS;
        }
    }

    public void progressInfection() {
        double newZombies = infectedPopulations[0];
        for (int i = 1; i < infectedPopulations.length; i++) {
            infectedPopulations[i-1] = infectedPopulations[i];
        }
        infectedPopulations[infectedPopulations.length-1] = 0;
        inhabitants.put(MapConstants.TILE_INHABITANTS.ZOMBIE, inhabitants.get(MapConstants.TILE_INHABITANTS.ZOMBIE) + newZombies);
        inhabitants.put(MapConstants.TILE_INHABITANTS.HUMAN, inhabitants.get(MapConstants.TILE_INHABITANTS.HUMAN) - newZombies);
    }

    private int[] coordinates;
    /**
     * Constructs a new Tile with the specified inhabitants, biome, and inhabitant strength.
     *
     * @param inhabitants the type of inhabitants on this tile
     * @param biome the biome type of this tile
     * @param inhabitantStrength the strength of the inhabitants hold on this tile, 0 to 1
     */
    public Tile(MapConstants.TILE_BIOMES biome, int xCoordinate, int yCoordinate) {
        this.biome = biome;
        this.coordinates = new int[]{xCoordinate, yCoordinate};
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

    public double getTotalInfected() {
        double totalInfected = 0;
        for (double infected : infectedPopulations) {
            totalInfected += infected;
        }
        return totalInfected;
    }

    
}
