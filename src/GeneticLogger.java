import java.text.SimpleDateFormat;
import java.util.Date;

public class GeneticLogger {
    private static String filename;

    // Call this once at program start
    public static void initCSV() {
        // Generate timestamped filename
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        filename = "LOG_Files/Genetic_Algorithm_logs_" + timestamp + ".csv";

        // Write header to new file
        try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(
                java.nio.file.Paths.get(filename),
                java.nio.charset.StandardCharsets.UTF_8)) {

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

