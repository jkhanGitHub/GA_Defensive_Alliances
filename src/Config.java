import java.util.Properties;

public class Config {
    public String FILEPATH;
    public int NUMBER_OF_NODES;
    public int POPULATION_SIZE;
    public int AMOUNT_OF_LEARNINGS;
    public float NODE_EXISTENCE_PROBABILITY;
    public int NUMBER_OF_CONTESTANTS_PER_ROUND;
    public int NUMBER_OF_CHILDS_PER_PARENT;
    public float MUTATION_RATE;
    public int NUMBER_OF_ITERATIONS;
    public int BREAK_FITNESS;
    public float INTERSECTION_PROBABILITY;
    public String RECOMBINATION_METHOD;
    public String SELECTION_METHOD;
    public String MUTATION_METHOD;
    public boolean ACTIVATE_LEARNING;

    public boolean CAPPED_LEARNING;
    public int AMOUNT_OF_lEARNERS;
    public boolean RANDOMIZE_LEARNERS;


    public Config(Properties props) {
        FILEPATH = props.getProperty("FILEPATH");
        NUMBER_OF_NODES = Integer.parseInt(props.getProperty("NUMBER_OF_NODES"));
        POPULATION_SIZE = Integer.parseInt(props.getProperty("POPULATION_SIZE"));
        AMOUNT_OF_LEARNINGS = Integer.parseInt(props.getProperty("AMOUNT_OF_LEARNINGS"));
        NODE_EXISTENCE_PROBABILITY = Float.parseFloat(props.getProperty("NODE_EXISTENCE_PROBABILITY"));
        NUMBER_OF_CONTESTANTS_PER_ROUND = Integer.parseInt(props.getProperty("NUMBER_OF_CONTESTANTS_PER_ROUND"));
        NUMBER_OF_CHILDS_PER_PARENT = Integer.parseInt(props.getProperty("NUMBER_OF_CHILDS_PER_PARENT"));
        MUTATION_RATE = Float.parseFloat(props.getProperty("MUTATION_RATE"));
        NUMBER_OF_ITERATIONS = Integer.parseInt(props.getProperty("NUMBER_OF_ITERATIONS"));
        BREAK_FITNESS = Integer.parseInt(props.getProperty("BREAK_FITNESS"));
        INTERSECTION_PROBABILITY = Float.parseFloat(props.getProperty("INTERSECTION_PROBABILITY"));
        RECOMBINATION_METHOD = props.getProperty("RECOMBINATION_METHOD");
        SELECTION_METHOD = props.getProperty("SELECTION_METHOD");
        MUTATION_METHOD = props.getProperty("MUTATION_METHOD");
        ACTIVATE_LEARNING = Boolean.parseBoolean(props.getProperty("ACTIVATE_LEARNING"));
        AMOUNT_OF_lEARNERS = Integer.parseInt(props.getProperty("AMOUNT_OF_lEARNERS"));
        RANDOMIZE_LEARNERS = Boolean.parseBoolean(props.getProperty("RANDOMIZE_LEARNERS"));
        CAPPED_LEARNING = Boolean.parseBoolean(props.getProperty("CAPPED_LEARNING"));
    }
}

