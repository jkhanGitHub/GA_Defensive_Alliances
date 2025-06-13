import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.math.BigDecimal;

public class GeneticLogger {
    private static String directory;
    private static String filename;

    public static String getOutputDirectory() {
        return directory;
    }

    public static String getFilename() {
        return filename;
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
                    Genetic_Algorithm.PROBABILITY));
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
}

