import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
    private Random RNG;
    private MapConstants.TILE_INFRASTRUCTURE infrastructure = null;

    public void addInfectedPeople(double amt) {
        if (Double.isNaN(amt) || Double.isInfinite(amt) || amt < 0){
            if (GeneralConstants.PRINT_STATEMENTS) {
                System.out.println("added "+amt+" infected");
            }
            System.exit(1);
        }
        for (int i = 0; i < SpreadConstants.INFECTION_ITERATIONS; i++) {
            infectedPopulations[(int) (SpreadConstants.MEAN_TIME_FROM_INFECTION_TO_ZOMBIE+Math.pow(RNG.nextDouble()*Math.pow(SpreadConstants.STDEV_TIME_FROM_INFECTION_TO_ZOMBIE,2),.5))] += amt / SpreadConstants.INFECTION_ITERATIONS;
        }
    }
    public void progressInfection() {
        
        double newZombies = infectedPopulations[0];
        if (Double.isNaN(newZombies)){
            if (GeneralConstants.PRINT_STATEMENTS) {
                System.out.println("added NAN newZombies");
            }
            System.exit(1);
        } else if (newZombies < 0){
            if (GeneralConstants.PRINT_STATEMENTS) {
                System.out.println("Negative newZombies at"+coordinates[0]+","+coordinates[1]);
            }
            System.exit(1);
        }
        for (int i = 1; i < infectedPopulations.length; i++) {
            infectedPopulations[i-1] = infectedPopulations[i];
        }
        infectedPopulations[infectedPopulations.length-1] = 0;

        addZombies(newZombies);
        // Conversion removes these humans from total population, but infected queue
        // has already been updated by shifting infectedPopulations.
        double newHumanCount = getHumans() - newZombies;
        if (newHumanCount < 0) {
            newHumanCount = 0;
        }
        inhabitants.put(TILE_INHABITANTS.HUMAN, newHumanCount);
        // System.out.println("h:"+inhabitants.get(TILE_INHABITANTS.HUMAN)+"     z:"+inhabitants.get(TILE_INHABITANTS.ZOMBIE));
    }

    private int[] coordinates;
    /**
     * Constructs a new Tile with the specified inhabitants, biome, and inhabitant strength.
     *
     * @param biome the biome type of this tile
     */
    public Tile(MapConstants.TILE_BIOMES biome, int xCoordinate, int yCoordinate, Random rng) {
        this.biome = biome;
        this.coordinates = new int[]{xCoordinate, yCoordinate};
        this.RNG = rng;

        inhabitants = new HashMap<MapConstants.TILE_INHABITANTS, Double>(Map.of(
            MapConstants.TILE_INHABITANTS.HUMAN, 0.0,
            MapConstants.TILE_INHABITANTS.ZOMBIE, 0.0));
        
        infectedPopulations = new double[SpreadConstants.MEAN_TIME_FROM_INFECTION_TO_ZOMBIE+SpreadConstants.STDEV_TIME_FROM_INFECTION_TO_ZOMBIE];
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
            inhabitants.put(TILE_INHABITANTS.HUMAN, 5000.0);
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

    private Map<MapConstants.TILE_INHABITANTS, Double> getInhabitants() {
        return inhabitants;
    }

    public int[] getCoordinates() {
        return coordinates;
    }
    
    public double getHumans(){
        double h = inhabitants.get(TILE_INHABITANTS.HUMAN);
        if (!Double.isFinite(h) || h < GeneralConstants.NEGATIVE_POPULATION_TOLERANCE){
            h = 0.0;
            inhabitants.put(TILE_INHABITANTS.HUMAN,h);
        }
        return h;
    }

    public double getZombies(){
        double z = inhabitants.get(TILE_INHABITANTS.ZOMBIE);
        if (!Double.isFinite(z) || z < GeneralConstants.NEGATIVE_POPULATION_TOLERANCE){
            z = 0.0;
            inhabitants.put(TILE_INHABITANTS.ZOMBIE,z);
        }
        return z;
    }

    public void addHumans(double amt){
        if (!Double.isFinite(amt)){
            if (GeneralConstants.PRINT_STATEMENTS) {
                System.out.println("added invalid Humans: " + amt);
            }
            System.exit(1);
        }
        if (amt > 0){
            inhabitants.put(TILE_INHABITANTS.HUMAN, inhabitants.get(TILE_INHABITANTS.HUMAN)+amt);
        } else if (amt < 0){
            double totalInfected = getTotalInfected();
            double currentHumans = getHumans();
            double removal = -amt;

            if (removal > currentHumans) {
                removal = currentHumans;
            }

            if (totalInfected > 0 && currentHumans > 0) {
                // Remove infected proportionally for random death/mortality, but leave
                // conversion logic in progressInfection to handle exact infected -> zombie flow.
                double infectedRemoval = Math.min(totalInfected, removal * (totalInfected / currentHumans));
                if (infectedRemoval > 0) {
                    trimInfected(infectedRemoval);
                }
            }

            double newHumanCount = Math.max(0.0, currentHumans - removal);
            inhabitants.put(TILE_INHABITANTS.HUMAN, newHumanCount);
        }
        getTotalInfected();
    }

    public void addZombies(double amt){
        if (!Double.isFinite(amt)){
            if (GeneralConstants.PRINT_STATEMENTS) {
                System.out.println("added invalid Zombies: " + amt);
            }
            System.exit(1);
        }

        double newZombieCount = getZombies()+amt;
        if (!Double.isFinite(newZombieCount)) {
            newZombieCount = 0.0;
        }

        if (newZombieCount < 0){
            newZombieCount = 0.0;
        }

        inhabitants.put(TILE_INHABITANTS.ZOMBIE, newZombieCount);
    }


    public double getTotalInfected() {
        double totalInfected = 0;
        for (int i = 0; i < infectedPopulations.length; i++){
            double infected = infectedPopulations[i];
            if (Double.isNaN(infected) || infected < 0 || Double.isInfinite(infected)){
                if (GeneralConstants.PRINT_STATEMENTS) {
                    System.out.println(infected+" infected population at "+coordinates[0]+","+coordinates[1]+" index "+i);
                }
                System.exit(1);
            }
            totalInfected += infected;
        }
        if (Double.isNaN(totalInfected) || totalInfected < 0 || totalInfected > getHumans()){
            if (getHumans()-totalInfected < GeneralConstants.NEGATIVE_POPULATION_TOLERANCE){
                inhabitants.put(TILE_INHABITANTS.HUMAN, totalInfected);
            } else {
                if (GeneralConstants.PRINT_STATEMENTS) {
                    System.out.println(getHumans());
                }
                if (GeneralConstants.PRINT_STATEMENTS) {
                    System.out.println(totalInfected+" total infected population at"+coordinates[0]+","+coordinates[1]);
                }
                System.exit(1);
            }
        }
        return totalInfected;
        
    }

    /**
     * Removes a specified amount of infected individuals from this tile.
     *
     * The amount is removed proportionally across the infection age buckets based
     * on their share of total infections.
     *
     * @param amount number of infected to remove from the total infected population
     */
    public void trimInfected(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            if (GeneralConstants.PRINT_STATEMENTS) {
                System.out.println("Invalid amount to trim infected."+amount);
            }
            System.exit(1);
        }

        double totalInfected = getTotalInfected();
        if (totalInfected < 0.0) {
            for (int i = 0; i < infectedPopulations.length; i++) {
                infectedPopulations[i] = 0.0;
            }
            if (GeneralConstants.PRINT_STATEMENTS) {
                System.out.println("No infected individuals to trim.");
            }
            return;
        }

        if (amount >= totalInfected) {
            for (int i = 0; i < infectedPopulations.length; i++) {
                infectedPopulations[i] = 0.0;
            }
            return;
        }

        double removed = 0.0;
        for (int i = 0; i < infectedPopulations.length; i++) {
            double cur = infectedPopulations[i];
            if (cur <= 0.0) {
                continue;
            }

            double share = cur / totalInfected;
            double delta = amount * share;
            if (delta > cur) {
                delta = cur;
            }

            infectedPopulations[i] = cur - delta;
            removed += delta;
        }

        // Floating point rounding adjustment: if we still need to drop a tiny remainder, do it from non-empty buckets.
        double remainder = amount - removed;
        if (remainder > 1e-9) {
            for (int i = 0; i < infectedPopulations.length && remainder > 1e-9; i++) {
                if (infectedPopulations[i] <= 0.0) {
                    continue;
                }
                double take = Math.min(remainder, infectedPopulations[i]);
                infectedPopulations[i] -= take;
                remainder -= take;
            }
        }

        // safety clamps
        for (int i = 0; i < infectedPopulations.length; i++) {
            if (infectedPopulations[i] < 0.0) {
                infectedPopulations[i] = 0.0;
            }
        }
    }
    }
