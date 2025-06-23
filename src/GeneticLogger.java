import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.math.BigDecimal;
import java.util.*;

public class GeneticLogger {
    private static String directory;
    private static String filename;

    public static String getOutputDirectory() {
        return directory;
    }

    public static String getFilename() {
        return filename;
    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
                is = new FileInputStream(source);
                os = new FileOutputStream(dest);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                }
        } finally {
                is.close();
                os.close();
        }
}

    // Call this once at program start
    public static void initCSV() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        directory = "LOG_Files/run_" + timestamp + "/";
        new File(directory).mkdirs();  // Create directory

        filename = directory + "ga_stats.csv";

        String scientificMutationRate = new BigDecimal(Genetic_Algorithm.MUTATION_RATE).toString();

        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(filename),
                StandardCharsets.UTF_8)) {

            // Write configuration header
            writer.write("# Genetic Algorithm Configuration");
            writer.newLine();
            writer.write("# Graph: " + Genetic_Algorithm.FILEPATH);
            writer.newLine();
            writer.write(String.format("# Nodes: %d, Population: %d, Generations: %d",
                    Genetic_Algorithm.NUMBER_OF_NODES,
                    Genetic_Algorithm.POPULATION_SIZE,
                    Genetic_Algorithm.NUMBER_OF_ITERATIONS));
            writer.newLine();
            writer.write(String.format("# Node Probability: %.2f, Mutation Rate: %s",
                    Genetic_Algorithm.NODE_EXISTENCE_PROBABILITY,
                    scientificMutationRate));
            writer.newLine();
            writer.write(String.format("# Selection: Tournament (n=%d), Children/Parent: %d",
                    Genetic_Algorithm.NUMBER_OF_CONTESTANTS_PER_ROUND,
                    Genetic_Algorithm.NUMBER_OF_CHILDS_PER_PARENT));
            writer.newLine();
            writer.write(String.format("# Learning: %d, Recombination Prob: %.2f",
                    Genetic_Algorithm.AmountOfLearnings,
                    Genetic_Algorithm.Intersection_PROBABILITY));
            writer.newLine();
            writer.write("# Break Fitness: " + Genetic_Algorithm.BREAK_FITNESS);
            writer.newLine();
            writer.write("# Worst Possible Fitness: " + OneGenome.worstFitnessPossible);
            writer.newLine();
            writer.newLine();

            // Write CSV header
            writer.write("generation,population_fitness,mean_fitness,mean_size,"
                    + "survivors,offspring,best_fitness,best_size,"
                    + "second_fitness,second_size,worst_fitness,worst_size,"
                    + "best_second_diff,best_worst_diff,best_current_vs_last_diff");
            writer.newLine();
        } catch (Exception e) {
            System.err.println("CSV initialization failed: " + e.getMessage());
        }
    }

    /**
     * Initialize CSV logging using values from Config.
     * @param cfg Configuration object containing all GA parameters
     */
    public static void initCSV(Config cfg) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        directory = "LOG_Files/run_" + timestamp + "/";
        new File(directory).mkdirs();
        filename = directory + "ga_stats.csv";

        boolean activateCappedLearning = cfg.CAPPED_LEARNING;

        // Prepare scientific notation for mutation rate if needed
        String scientificMutationRate = new BigDecimal(cfg.MUTATION_RATE).toString();

        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(filename),
                StandardCharsets.UTF_8)) {

            // Configuration header
            writer.write("# Genetic Algorithm Configuration"); writer.newLine();
            writer.write("# Graph: " + cfg.FILEPATH); writer.newLine();
            writer.write(String.format("# Nodes: %d, Population: %d, Generations: %d, Searched DA Size: %d",
                    cfg.NUMBER_OF_NODES,
                    cfg.POPULATION_SIZE,
                    cfg.NUMBER_OF_ITERATIONS,
                    cfg.SIZE_OF_DEFENSIVE_ALLIANCE)); writer.newLine();
            writer.write(String.format("# Node Probability: %.2f, Mutation Rate: %s",
                    cfg.NODE_EXISTENCE_PROBABILITY,
                    scientificMutationRate)); writer.newLine();
            writer.write(String.format("# Selection Method: %s (n=%d)",
                    cfg.SELECTION_METHOD,
                    cfg.NUMBER_OF_CONTESTANTS_PER_ROUND)); writer.newLine();
            writer.write(String.format("# Recombination Method: %s, Intersection Prob: %.2f",
                    cfg.RECOMBINATION_METHOD,
                    cfg.INTERSECTION_PROBABILITY)); writer.newLine();
            writer.write(String.format("# Mutation Method: %s, Children/Parent: %d",
                    cfg.MUTATION_METHOD,
                    cfg.NUMBER_OF_CHILDS_PER_PARENT)); writer.newLine();
            writer.write("# Activate Learning: " + cfg.ACTIVATE_LEARNING); writer.newLine();

            if(activateCappedLearning){
                writer.write("# Amount of Learners: " + cfg.AMOUNT_OF_LEARNINGS); writer.newLine();
                writer.write("# Randomize Learners: " + cfg.RANDOMIZE_LEARNERS); writer.newLine();
            }

            writer.write("# Break Fitness: " + cfg.BREAK_FITNESS); writer.newLine();
            writer.write("# Worst Possible Fitness: " + OneGenome.worstFitnessPossible); writer.newLine();
            writer.newLine();

            // CSV header row
            writer.write("generation,population_fitness,mean_fitness,mean_size,"
                    + "survivors,offspring,best_fitness,best_size,"
                    + "second_fitness,second_size,worst_fitness,worst_size,"
                    + "best_second_diff,best_worst_diff,best_current_vs_last_diff");
            writer.newLine();
        } catch (Exception e) {
            System.err.println("CSV initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Call after each generation
    public static void logGeneration() {
        // Format data row
        String data = String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d",
                Population.generation,
                Population.population_fitness,
                Population.mean_fitness,
                Population.mean_size,
                Population.survivors.size(),
                Population.population.length - Population.survivors.size(),
                Population.population[0].getFitness(),
                Population.population[0].getSize(),
                Population.population[1].getFitness(),
                Population.population[1].getSize(),
                Population.population[Population.population.length - 1].getFitness(),
                Population.population[Population.population.length - 1].getSize(),
                Genome.difference(Population.population[0], Population.population[1]),
                Genome.difference(Population.population[0], Population.population[Population.population.length - 1]),
                Genome.difference(Population.population[0], Population.bestGenomeFromLastGeneration)
        );

        // Append to file
        try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(
                java.nio.file.Paths.get(filename),
                java.nio.charset.StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND)) {

            writer.write(data);
            writer.newLine();
        } catch (Exception e) {
            System.err.println("Generation logging failed: " + e.getMessage());
        }
    }

    
    public static void printDefensiveAlliances(File file, List<Genome> defensiveAlliances) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Genome da : defensiveAlliances) {
                writer.write(da.info());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("ERROR: Failed to write defensive alliances to file: " + e.getMessage());
        }
    }
}

