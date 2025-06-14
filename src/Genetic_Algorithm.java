import java.io.IOException;
import java.util.*;
import java.io.*;
import java.util.Properties;

public class Genetic_Algorithm {


    /*Generic definition

The following is an example of a generic evolutionary algorithm:

    1. Randomly generate the initial population of individuals, the first generation.
    2. Evaluate the fitness of each individual in the population.
    3. Check, if the goal is reached and the algorithm can be terminated.
    4. Select individuals as parents, preferably of higher fitness.
    5. Produce offspring with optional crossover (mimicking reproduction).
    6. Apply mutation operations on the offspring.
    7. Select individuals preferably of lower fitness for replacement with new individuals (mimicking natural selection).
    8. Return to 2
     */

    static Config cfg;
    static Dictionary<String, Integer> recombinationIdentifiers = new Hashtable<>();

    static {
        recombinationIdentifiers.put("OnePointCrossoverThreaded", 0);
        recombinationIdentifiers.put("ProbabilityIntersectionThreaded", 1);
        recombinationIdentifiers.put("OnePointCrossover", 2);
        recombinationIdentifiers.put("ProababilityIntersection", 3);
    }

    static Dictionary<String, Integer> selectionMethods = new Hashtable<>();

    static {
        selectionMethods.put("Stochastic Universal Sampling", 0);
        selectionMethods.put("Tournament Selection Elimination", 1);
        selectionMethods.put("Roulette Wheel Selection", 2);
        selectionMethods.put("Linear Rank Selection", 3);
        selectionMethods.put("Exponential Rank Selection", 4);
        selectionMethods.put("Elitism", 5);
    }

    static Dictionary<String, Integer> mutationIdentifiers = new Hashtable<>();

    static {
        mutationIdentifiers.put("Mutation", 0);
        mutationIdentifiers.put("Mutation of vertices with high degree", 1);
    }

    public static List<Genome> defensiveAlliances = new ArrayList<>();


    //Path to the csv file
    public static final String FILEPATH = "lasftm_asia/lastfm_asia_edges.csv";
    //Number of Nodes of graph
    public static final int NUMBER_OF_NODES = 7624;

    public static final int POPULATION_SIZE = (int) Math.pow(2, 11); //Powers of two are best suited for variable tournament selection, use  factors of two for 1v1

    public static final int AmountOfLearnings = 5;

    public static final float NODE_EXISTENCE_PROBABILITY = 0.5F;

    static boolean activateLearning = true; //if true, survivors will learn

    static int AMOUNT_OF_lEARNERS = 10; //amount of survivors that learn, this is only used when activateLearning is true

    static boolean randomizeLearners = true; //if true, the learners will be randomly selected from the survivors, otherwise the first AMOUNT_OF_lEARNERS survivors will learn


    //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
    //Number of children = number of parents*NUMBER_OF_CHILDS_PER_PARENT
    //new population will take over the best from last generation + all new children
    //WARNING: IF NUMBER OF PARENTS IS >= Number of children then the whole population will be replaced
    // new nodes in next population is (NUMBER_OF_CONTESTANTS_PER_ROUND/NUMBER_OF_CHILDS_PER_PARENT) in percent
    public static final int NUMBER_OF_CONTESTANTS_PER_ROUND = (int) Math.pow(2, 2); // only 1 winner -< lower number ensures more worse parents and probably more diversity

    //increase this number to increase the number of children per parent also resulting in bigger population in each generation, only makes sense when making population a list wont be doing that tho xD
    //higher number -< earlier local maximum because of incest
    public static final int NUMBER_OF_CHILDS_PER_PARENT = 2;

    public static final int MULTIPLIER = POPULATION_SIZE / NUMBER_OF_CONTESTANTS_PER_ROUND; //Multiplier for some selection methods which have an average parent output of 1

    //Explanation of mutation identfieres found in Population.java mutate_Population()
    public static final float MUTATION_RATE = 1 / NUMBER_OF_NODES; //mutation rate, 0.01 means 1% chance of mutation per node, 0.1 means 10% chance of mutation per node
    public static final int NUMBER_OF_ITERATIONS = 150; //number of generations

    public static final int BREAK_FITNESS = Integer.MAX_VALUE;
    public static final float Intersection_PROBABILITY = 0.5f; //probability of intersection, 0.5 means 50% chance of intersection and 50% chance of crossover


    public static OneGenome PARENT_GRAPH;

    final static int UPPER_BOUND_OF_LEARNERS = POPULATION_SIZE / NUMBER_OF_CONTESTANTS_PER_ROUND; //upper bound for ADDITIONAL_amount of genomes to be mutated
    public static int[][] graph;

    public static Map<Integer, Genome> bestGenomes = new HashMap<>();

    static Random random = new Random();

    static void addDefensiveAlliance(Population population) {
        int i = 0;
        while (i < population.getPopulation().length && population.getPopulation()[i].getFitness() > 0) {
            if (!defensiveAlliances.contains(population.getPopulation()[i])) {
                Genome g = population.getPopulation()[i];
                //g = Genome.removeIsolatedNodes(population.getPopulation()[i], PARENT_GRAPH);
                defensiveAlliances.add(g);
            }
            i++;
        }
        if (i > 0) System.out.println("\u001B[31m" + "new Defensive Alliances found: " + i + "\u001B[0m");
    }

    //when using onepointcrossover the parentgraph should not be included in the population!
    // GeneticAlgorithm.java: updated method signature
    static void geneticAlgorithm(
            int NUMBER_OF_NODES,
            float NODE_EXISTENCE_PROBABILITY,
            int POPULATION_SIZE,
            int NUMBER_OF_ITERATIONS,
            int BREAK_FITNESS,
            int numberOfContestantsPerRound,
            float mutationRate,
            OneGenome parentGraph,
            int recombinationMethod,
            int numberOfChildsPerParent,
            float intersectionProbability,
            int selectionMethod,
            int mutationMethod,
            boolean activateLearning
    ) throws IOException {
        Population population = new Population(POPULATION_SIZE, NUMBER_OF_NODES, NODE_EXISTENCE_PROBABILITY, parentGraph);
        population.sort_Population_by_fitness_and_size_reversed();


        GeneticLogger.initCSV(cfg);
        int counter = 0;

        while (population.generation != NUMBER_OF_ITERATIONS) {
            addDefensiveAlliance(population);
            bestGenomes.put(counter, population.getPopulation()[0]);
            population.printStats();
            GeneticLogger.logGeneration();
            List<Genome> newGenParents = Selection.select_SelectionMethod(
                    population,
                    numberOfContestantsPerRound,
                    selectionMethod
            );
            Population.population = Population.newGeneration(
                    mutationRate,
                    intersectionProbability,
                    numberOfChildsPerParent,
                    newGenParents,
                    mutationMethod,
                    recombinationMethod,
                    activateLearning
            );
            if (++counter % 10 == 0) {
                population = Population.remove_duplicates_Threaded(
                        population,
                        NUMBER_OF_NODES,
                        NODE_EXISTENCE_PROBABILITY,
                        parentGraph
                );
            }
        }
    }

    static void geneticAlgorithm(
            int NUMBER_OF_NODES,
            float NODE_EXISTENCE_PROBABILITY,
            int POPULATION_SIZE,
            int NUMBER_OF_ITERATIONS,
            int BREAK_FITNESS,
            int numberOfContestantsPerRound,
            float mutationrate,
            OneGenome parentGraph,
            int recombinationMethod,
            int numberOfChildsPerParent,
            float intersection_probability,
            int selectionMethod,
            int mutationMethod,
            boolean activateLearning,
            int amountOfLearners,
            boolean randomizeLearners

    ) throws IOException {
        //Generatess first Population and calculates the Fitness of each Genome
        Population population = new Population(POPULATION_SIZE, NUMBER_OF_NODES, NODE_EXISTENCE_PROBABILITY, parentGraph);
        population.sort_Population_by_fitness_and_size_reversed();

        /*
        //intial learn to improve the initial population
        for (Genome genome: population.getPopulation()) {
            Genome.learn(genome,PARENT_GRAPH,10);
        }*/

        //init css file
        GeneticLogger.initCSV(cfg);
        int counter = 0;


        while (population.generation != NUMBER_OF_ITERATIONS) {
            //adds defensive alliance to a list if it has been found
            addDefensiveAlliance(population);
            //adds the best genome to the list of best genomes
            bestGenomes.put(counter, population.getPopulation()[0]);

            //Print some stats and log to file
            population.printStats();
            GeneticLogger.logGeneration();

            //change Selection method to whhatever
            //Literatur says that using many different selection methods is better
            List<Genome> newGenParents = Selection.select_SelectionMethod(
                    population,
                    numberOfContestantsPerRound,
                    random.nextInt(Selection.IMPLEMENTED_SELECTION_METHODS));// selectionMethod if you want to use a specific selection method, otherwise random

            //create new population
            Population.population = Population.newGeneration(
                    mutationrate,
                    intersection_probability,
                    numberOfChildsPerParent,
                    newGenParents,
                    mutationMethod,
                    recombinationMethod,
                    activateLearning,
                    amountOfLearners,
                    randomizeLearners
            );

            //remove isolated nodes from population, implemented inside remove_duplicates
            //remove duplicates from population and replace them with random generated genomes
            //this is important because otherwise the algorithm will get stuck in local optima way faster
            //can potentially result in duplicates in the population but chances are low
            // remove_duplicates is really slow
            // right now it exchanges a duplicate with its complement
            if (++counter % 10 == 0) {
                population = Population.remove_duplicates_Threaded(population, NUMBER_OF_NODES, NODE_EXISTENCE_PROBABILITY, parentGraph);
            }
        }
    }


    public static void main(String[] args) throws IOException {

        // Add this at the very beginning
        // Load configuration with exception handling

        try {
            cfg = ConfigLoader.load("run_config.properties");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load configuration: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        System.setOut(new PrintStream(System.out, true, "UTF-8"));

        String filePath = FILEPATH;
        int numberOfNodes = cfg.NUMBER_OF_NODES;


        try {
            graph = CsvReader.readCsvEdgesToSymmetricalMatrix(filePath, numberOfNodes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Graph g = new Graph(graph);
        Map.Entry<int[], int[]> pairWithLargestComponent = g.componetsWithDegrees.entrySet().iterator().next();

        //the first constructor ensures a connected graph
        //PARENT_GRAPH = new OneGenome(pairWithLargestComponent.getKey(), pairWithLargestComponent.getValue(),g.adjMatrix);
        OneGenome parentGraph = new OneGenome(numberOfNodes, graph);


        // Map method strings to IDs
        int selectionId = selectionMethods.get(cfg.SELECTION_METHOD);
        int recombId = recombinationIdentifiers.get(cfg.RECOMBINATION_METHOD);
        int mutId = mutationIdentifiers.get(cfg.MUTATION_METHOD);

        if(cfg.CAPPED_LEARNING){
            geneticAlgorithm(
                    cfg.NUMBER_OF_NODES,
                    cfg.NODE_EXISTENCE_PROBABILITY,
                    cfg.POPULATION_SIZE,
                    cfg.NUMBER_OF_ITERATIONS,
                    cfg.BREAK_FITNESS,
                    cfg.NUMBER_OF_CONTESTANTS_PER_ROUND,
                    cfg.MUTATION_RATE,
                    parentGraph,
                    recombId,
                    cfg.NUMBER_OF_CHILDS_PER_PARENT,
                    cfg.INTERSECTION_PROBABILITY,
                    selectionId,
                    mutId,
                    cfg.ACTIVATE_LEARNING,
                    cfg.AMOUNT_OF_lEARNERS,
                    cfg.RANDOMIZE_LEARNERS
            );
        }
        else {
            // Run GA without capped amount of learners
            // Invoke GA with full signature
            geneticAlgorithm(
                    cfg.NUMBER_OF_NODES,
                    cfg.NODE_EXISTENCE_PROBABILITY,
                    cfg.POPULATION_SIZE,
                    cfg.NUMBER_OF_ITERATIONS,
                    cfg.BREAK_FITNESS,
                    cfg.NUMBER_OF_CONTESTANTS_PER_ROUND,
                    cfg.MUTATION_RATE,
                    parentGraph,
                    recombId,
                    cfg.NUMBER_OF_CHILDS_PER_PARENT,
                    cfg.INTERSECTION_PROBABILITY,
                    selectionId,
                    mutId,
                    cfg.ACTIVATE_LEARNING
            );
        }



        // At end of execution
        String csvPath = GeneticLogger.getOutputDirectory() + "ga_stats.csv";
        System.out.println("CSV_PATH:" + csvPath);  // Special marker for batch file
    }

}
