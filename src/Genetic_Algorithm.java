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
        recombinationIdentifiers.put("OnePointCrossover", 0);
        recombinationIdentifiers.put("ProbabilityIntersection", 1);
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

    public static List<Genome> defensiveAlliances = new LinkedList<>();
    public static List<Genome> connected_defensiveAlliances = new LinkedList<>();
    public static final int MAXIMUM_AMOUNT_OF_STORED_DEFENSIVE_ALLIANCES = 1000; //maximum amount of stored defensive alliances, if this is reached, the worst defensive alliances will be removed


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

    static boolean addDefensiveAlliance(Population population, OneGenome parentGraph, int SIZE_OF_DEFENSIVE_ALLIANCE) {
        boolean foundnewDefensiveAlliance = false;
        for (int i = 0; (i < population.getPopulation().length && population.getPopulation()[i].getFitness() > 0); i++) {
            Genome g = population.getPopulation()[i];
            //if the genome is already in the defensive alliances, skip it
            if (!Genome.checkIfListContainsGenome(defensiveAlliances, g)) {
                foundnewDefensiveAlliance = true; //found a new defensive alliance

                defensiveAlliances.add(new Genome(g)); //add a deep copy of the genome to the defensive alliances
                // all conected components of a DA are also a DA by definition
                List<Genome> connectedComponents = g.getConnectedSubgraphs(parentGraph);

                //calculate fitness of each connected component
                for (Genome component : connectedComponents) {
                    component.setFitness(FitnessFunctions.calculateFitnessForDA(component, parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE));
                }

                //sort connectedComponents by fitness
                connectedComponents.sort(Comparator.comparingInt(Genome::getFitness).reversed());

                //add defensive alliances to the population by replacing worst genomes
                for (int j = 0; j < connectedComponents.size(); j++) {
                    Genome da = connectedComponents.get(j);
                    population.population[population.getPopulation().length - 1 - j] = da;
                }

                //add new defensive alliances to the list of connected defensive alliances
                defensiveAlliances.addAll(connectedComponents);
                connected_defensiveAlliances.addAll(connectedComponents); //add all connected components to the connected defensive alliances

                //important step to avoid java.lang.OutOfMemoryError: Java heap space
                //remove as many defensive alliances as needed to not exceed the maximum amount of stored defensive alliances
                if (defensiveAlliances.size() > MAXIMUM_AMOUNT_OF_STORED_DEFENSIVE_ALLIANCES) {
                    //sort defensive alliances by fitness
                    defensiveAlliances.sort(Comparator.comparingInt(Genome::getFitness).reversed());
                    connected_defensiveAlliances.sort(Comparator.comparingInt(Genome::getFitness).reversed());
                    //remove the worst defensive alliances
                    for (int j = 0; j < defensiveAlliances.size() - MAXIMUM_AMOUNT_OF_STORED_DEFENSIVE_ALLIANCES; j++) {
                        defensiveAlliances.remove(defensiveAlliances.size() - 1);
                    }
                    //remove the worst defensive alliances
                    for (int j = 0; j < connected_defensiveAlliances.size() - MAXIMUM_AMOUNT_OF_STORED_DEFENSIVE_ALLIANCES; j++) {
                        connected_defensiveAlliances.remove(connected_defensiveAlliances.size() - 1);
                    }
                }
            }
        }
        if (foundnewDefensiveAlliance){
            //remove duplicates from the population
            //necessary to avoid duplicates in the population
            //this is important because otherwise the algorithm will get stuck in local optima way faster, sinc eeverything is filled with duplicates
            Population.deleteDuplicates(population.getPopulation(),parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE, cfg.NODE_EXISTENCE_PROBABILITY); 

            Genome.deleteDuplicates(defensiveAlliances);
            Genome.deleteDuplicates(connected_defensiveAlliances);
            System.out.println("\u001B[31m" + "new Defensive Alliances found: "+ "\u001B[0m"); 
            return true;
        }
        return false;         
    }
        

    //when using onepointcrossover the parentgraph should not be included in the population!
    // GeneticAlgorithm.java: updated method signature
    static void geneticAlgorithm(
            int NUMBER_OF_NODES,
            float NODE_EXISTENCE_PROBABILITY,
            int POPULATION_SIZE,
            int NUMBER_OF_ITERATIONS,
            int SIZE_OF_DEFENSIVE_ALLIANCE,
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

        Population population = new Population(POPULATION_SIZE, NUMBER_OF_NODES, NODE_EXISTENCE_PROBABILITY, parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE);
        population.sort_Population_by_fitness_and_size_reversed();


        int counter = 0;


        GeneticLogger.initCSV(cfg);
        redirectConsoleOutput();

        //adds the best genome to the list of best genomes
        bestGenomes.put(counter, population.getPopulation()[0]);

        //Print some stats and log to file
        population.printStats();
        GeneticLogger.logGeneration();

        while ((population.generation != NUMBER_OF_ITERATIONS) && (population.getPopulation()[0].getFitness() < BREAK_FITNESS)) {
            List<Genome> newGenParents = Selection.select_SelectionMethod(
                    population,
                    numberOfContestantsPerRound,
                    random.nextInt(Selection.IMPLEMENTED_SELECTION_METHODS)); // selectionMethod if you want to use a specific selection method, otherwise random
            
            Population.population = Population.newGeneration(
                    mutationRate,
                    intersectionProbability,
                    numberOfChildsPerParent,
                    newGenParents,
                    mutationMethod,
                    recombinationMethod,
                    activateLearning,
                    SIZE_OF_DEFENSIVE_ALLIANCE
            );

            //adds defensive alliance to a list if it has been found
            boolean additionalSort = addDefensiveAlliance(population, parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE);

            if (++counter % 10 == 0) {
                population = Population.remove_duplicates_Threaded(
                        population,
                        NUMBER_OF_NODES,
                        NODE_EXISTENCE_PROBABILITY,
                        parentGraph,
                        SIZE_OF_DEFENSIVE_ALLIANCE
                );
                additionalSort = true; //if duplicates are removed, sort the population again
            }
            //if additionalSort is true, sort the population again
            if (additionalSort) {
                population.sort_Population_by_fitness_and_size_reversed();
            }

            //adds the best genome to the list of best genomes
            bestGenomes.put(counter, population.getPopulation()[0]);
            //Print some stats and log to file
            population.printStats();
            GeneticLogger.logGeneration();
        }
        resetConsoleOutput();
    }

    static void geneticAlgorithm(
            int NUMBER_OF_NODES,
            float NODE_EXISTENCE_PROBABILITY,
            int POPULATION_SIZE,
            int NUMBER_OF_ITERATIONS,
            int SIZE_OF_DEFENSIVE_ALLIANCE,
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
        Population population = new Population(POPULATION_SIZE, NUMBER_OF_NODES, NODE_EXISTENCE_PROBABILITY, parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE);
        population.sort_Population_by_fitness_and_size_reversed();

        /*
        //intial learn to improve the initial population
        for (Genome genome: population.getPopulation()) {
            Genome.learn(genome,PARENT_GRAPH,10);
        }*/

        //init css file
        GeneticLogger.initCSV(cfg);
        redirectConsoleOutput();
        
        int counter = 0;

        //adds the best genome to the list of best genomes
        bestGenomes.put(counter, population.getPopulation()[0]);

        //Print some stats and log to file
        population.printStats();
        GeneticLogger.logGeneration();


        while ((population.generation != NUMBER_OF_ITERATIONS) && (population.getPopulation()[0].getFitness() < BREAK_FITNESS)) {
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
                    randomizeLearners,
                    SIZE_OF_DEFENSIVE_ALLIANCE
            );

            //adds defensive alliance to a list if it has been found
            boolean additionalSort = addDefensiveAlliance(population, parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE);

            //remove isolated nodes from population, implemented inside remove_duplicates
            //remove duplicates from population and replace them with random generated genomes
            //this is important because otherwise the algorithm will get stuck in local optima way faster
            //can potentially result in duplicates in the population but chances are low
            // remove_duplicates is really slow
            // right now it exchanges a duplicate with its complement
            if (++counter % 10 == 0) {
                population = Population.remove_duplicates_Threaded(population, NUMBER_OF_NODES, NODE_EXISTENCE_PROBABILITY, parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE);
                additionalSort = true; //if duplicates are removed, sort the population again
            }
            //if additionalSort is true, sort the population again
            if (additionalSort) {
                population.sort_Population_by_fitness_and_size_reversed();
            }

            //adds the best genome to the list of best genomes
            bestGenomes.put(counter, population.getPopulation()[0]);
            //Print some stats and log to file
            population.printStats();
            GeneticLogger.logGeneration();
        }
        resetConsoleOutput();
    }


    // Add this at the very beginning
    private static PrintStream originalOut = System.out;
    private static PrintStream originalErr = System.err;

    public static void redirectConsoleOutput() {
        try {
            PrintStream logStream = new PrintStream(new FileOutputStream(GeneticLogger.getOutputDirectory() + "console_output.log"));
            System.setOut(logStream);
            System.setErr(logStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void resetConsoleOutput() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }



    public static void deleteDuplicates(List<Genome> genomes) {
        Set<Genome> uniqueGenomes = new HashSet<>(genomes);
        genomes.clear();
        genomes.addAll(uniqueGenomes);
    }




    /**
     * Main method to run the genetic algorithm.
     * Loads configuration, reads graph data, initializes the population,
     * a connected graph is expected
     * and starts the genetic algorithm process.
     *
     * @param args command line arguments (not used)
     * @throws IOException if there is an error reading the graph data
     */

    public static void main(String[] args) throws IOException {

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
        OneGenome parentGraph;
        if (cfg.FILTER_NODES_THAT_CANNOT_BE_IN_A_DEFENSIVE_ALLIANCE_OF_SIZE_K){
            //parentGraph = new OneGenome(pairWithLargestComponent.getKey(), pairWithLargestComponent.getValue(),g.adjMatrix, cfg.SIZE_OF_DEFENSIVE_ALLIANCE);
            parentGraph = new OneGenome(numberOfNodes, graph, cfg.SIZE_OF_DEFENSIVE_ALLIANCE);
        }
        else{
            //parentGraph = new OneGenome(pairWithLargestComponent.getKey(), pairWithLargestComponent.getValue(),g.adjMatrix);
            parentGraph = new OneGenome(numberOfNodes, graph);
        }
        


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
                    cfg.SIZE_OF_DEFENSIVE_ALLIANCE,
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
                    cfg.AMOUNT_OF_LEARNERS,
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
                    cfg.SIZE_OF_DEFENSIVE_ALLIANCE,
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
        cfg.writeToFile(GeneticLogger.getOutputDirectory() +"runConfiguration.txt");
        String csvPath = GeneticLogger.getOutputDirectory() + "ga_stats.csv";
        
        //create document containing defensive alliances
        if (!defensiveAlliances.isEmpty()) {
            // Write defensive alliances to file
            File foundDefensiveAlliancesFile = new File(GeneticLogger.getOutputDirectory() + "distinctDefensiveAlliances.txt");
            GeneticLogger.printDefensiveAlliances(foundDefensiveAlliancesFile, defensiveAlliances);
        }

        //create document containing connected defensive alliances
        if (!connected_defensiveAlliances.isEmpty()) {
            File foundDefensiveAlliancesFile = new File(GeneticLogger.getOutputDirectory() + "connected_distinctDefensiveAlliances.txt");
            GeneticLogger.printDefensiveAlliances(foundDefensiveAlliancesFile, connected_defensiveAlliances);
        }
        
        System.out.println("CSV_PATH:" + csvPath);  // Special marker for batch file
    }

}
