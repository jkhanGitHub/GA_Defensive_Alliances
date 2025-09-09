import java.util.Properties;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    public String FILEPATH;
    public int NUMBER_OF_NODES;
    public int POPULATION_SIZE;
    public int AMOUNT_OF_LEARNINGS;
    public float NODE_EXISTENCE_PROBABILITY;
    public int NUMBER_OF_PARENTS;
    public int NUMBER_OF_CHILDS_PER_PARENT;
    public float MUTATION_RATE;
    public int NUMBER_OF_ITERATIONS;
    public int SIZE_OF_DEFENSIVE_ALLIANCE;
    public boolean FILTER_NODES_THAT_CANNOT_BE_IN_A_DEFENSIVE_ALLIANCE_OF_SIZE_K;
    public int BREAK_FITNESS;
    public float INTERSECTION_PROBABILITY;
    public String RECOMBINATION_METHOD;
    public String SELECTION_METHOD;

    public boolean ALLOW_DUPLICATE_PARENTS;

    public String MUTATION_METHOD;
    public boolean ACTIVATE_LEARNING;

    public boolean CAPPED_LEARNING;
    public int AMOUNT_OF_LEARNERS;
    public boolean RANDOMIZE_LEARNERS;
    


    public Config(Properties props) {
        FILEPATH = props.getProperty("FILEPATH");
        NUMBER_OF_NODES = Integer.parseInt(props.getProperty("NUMBER_OF_NODES"));
        POPULATION_SIZE = Integer.parseInt(props.getProperty("POPULATION_SIZE"));
        AMOUNT_OF_LEARNINGS = Integer.parseInt(props.getProperty("AMOUNT_OF_LEARNINGS"));
        NODE_EXISTENCE_PROBABILITY = Float.parseFloat(props.getProperty("NODE_EXISTENCE_PROBABILITY"));
        NUMBER_OF_PARENTS = Integer.parseInt(props.getProperty("NUMBER_OF_PARENTS"));
        NUMBER_OF_CHILDS_PER_PARENT = Integer.parseInt(props.getProperty("NUMBER_OF_CHILDS_PER_PARENT"));
        MUTATION_RATE = Float.parseFloat(props.getProperty("MUTATION_RATE"));
        NUMBER_OF_ITERATIONS = Integer.parseInt(props.getProperty("NUMBER_OF_ITERATIONS"));
        SIZE_OF_DEFENSIVE_ALLIANCE = Integer.parseInt(props.getProperty("SIZE_OF_DEFENSIVE_ALLIANCE"));
        BREAK_FITNESS = Integer.parseInt(props.getProperty("BREAK_FITNESS"));
        INTERSECTION_PROBABILITY = Float.parseFloat(props.getProperty("INTERSECTION_PROBABILITY"));
        RECOMBINATION_METHOD = props.getProperty("RECOMBINATION_METHOD");
        SELECTION_METHOD = props.getProperty("SELECTION_METHOD");
        ALLOW_DUPLICATE_PARENTS = Boolean.parseBoolean(props.getProperty("ALLOW_DUPLICATE_PARENTS"));
        MUTATION_METHOD = props.getProperty("MUTATION_METHOD");
        ACTIVATE_LEARNING = Boolean.parseBoolean(props.getProperty("ACTIVATE_LEARNING"));
        AMOUNT_OF_LEARNERS = Integer.parseInt(props.getProperty("AMOUNT_OF_LEARNERS"));
        RANDOMIZE_LEARNERS = Boolean.parseBoolean(props.getProperty("RANDOMIZE_LEARNERS"));
        CAPPED_LEARNING = Boolean.parseBoolean(props.getProperty("CAPPED_LEARNING"));
        FILTER_NODES_THAT_CANNOT_BE_IN_A_DEFENSIVE_ALLIANCE_OF_SIZE_K = Boolean.parseBoolean(props.getProperty("FILTER_NODES_THAT_CANNOT_BE_IN_A_DEFENSIVE_ALLIANCE_OF_SIZE_K"));
    }


    public void writeToFile(String outputPath) throws IOException {
        Path filePath = Paths.get(outputPath);
        
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write("Genetic Algorithm Configuration");
            writer.newLine();
            writer.write("=".repeat(80));
            writer.newLine();
            writer.newLine();
            
            writeField(writer, "FILEPATH", FILEPATH);
            writeField(writer, "NUMBER_OF_NODES", NUMBER_OF_NODES);
            writeField(writer, "POPULATION_SIZE", POPULATION_SIZE);
            writeField(writer, "NODE_EXISTENCE_PROBABILITY", NODE_EXISTENCE_PROBABILITY);
            writeField(writer, "NUMBER_OF_PARENTS", NUMBER_OF_PARENTS);
            writeField(writer, "NUMBER_OF_CHILDS_PER_PARENT", NUMBER_OF_CHILDS_PER_PARENT);
            writeField(writer, "MUTATION_RATE", MUTATION_RATE);
            writeField(writer, "NUMBER_OF_ITERATIONS", NUMBER_OF_ITERATIONS);
            writeField(writer, "SIZE_OF_DEFENSIVE_ALLIANCE", SIZE_OF_DEFENSIVE_ALLIANCE);
            writeField(writer, "FILTER_NODES_THAT_CANNOT_BE_IN_A_DEFENSIVE_ALLIANCE_OF_SIZE_K", FILTER_NODES_THAT_CANNOT_BE_IN_A_DEFENSIVE_ALLIANCE_OF_SIZE_K);
            writeField(writer, "BREAK_FITNESS", BREAK_FITNESS);
            writeField(writer, "INTERSECTION_PROBABILITY", INTERSECTION_PROBABILITY);
            writeField(writer, "RECOMBINATION_METHOD", RECOMBINATION_METHOD);
            writeField(writer, "SELECTION_METHOD", SELECTION_METHOD);
            writeField(writer, "ALLOW_DUPLICATE_PARENTS", ALLOW_DUPLICATE_PARENTS);
            writeField(writer, "MUTATION_METHOD", MUTATION_METHOD);
            writeField(writer, "ACTIVATE_LEARNING", ACTIVATE_LEARNING);
            writeField(writer, "AMOUNT_OF_LEARNINGS", AMOUNT_OF_LEARNINGS);
            writeField(writer, "CAPPED_LEARNING", CAPPED_LEARNING);
            writeField(writer, "AMOUNT_OF_LEARNERS", AMOUNT_OF_LEARNERS);
            writeField(writer, "RANDOMIZE_LEARNERS", RANDOMIZE_LEARNERS);
        }
    }

    private void writeField(BufferedWriter writer, String name, Object value) throws IOException {
        String formattedName = String.format("%-" + 40 + "s", name);
        writer.write(formattedName + " : " + value);
        writer.newLine();
    }

}

