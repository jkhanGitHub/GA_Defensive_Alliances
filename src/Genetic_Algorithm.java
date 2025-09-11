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

    //This Table needs to be uptodate when adding new selection methods alsways check if both tables are the same or change the implementation when time to spare
    static {
        selectionMethods.put("Stochastic Universal Sampling", 0);
        selectionMethods.put("Tournament Selection Elimination", 1);
        selectionMethods.put("Roulette Wheel Selection", 2);
        selectionMethods.put("Linear Rank Selection", 3);
        selectionMethods.put("Exponential Rank Selection", 4);
        selectionMethods.put("Elitism", 5);
        selectionMethods.put("Random", 6);
    }

    static Dictionary<String, Integer> mutationIdentifiers = new Hashtable<>();

    static {
        mutationIdentifiers.put("Mutation", 0);
        mutationIdentifiers.put("Mutation of vertices with high degree", 1);
    }

    public static List<Genome> defensiveAlliances = new LinkedList<>();
    public static List<Genome> connected_defensiveAlliances = new LinkedList<>();
    public static final int MAXIMUM_AMOUNT_OF_STORED_DEFENSIVE_ALLIANCES = 1000; //maximum amount of stored defensive alliances, if this is reached, the worst defensive alliances will be removed

    public static int[][] graph;

    public static Map<Integer, Genome> bestGenomes = new HashMap<>();

    static Random random = new Random();

    static boolean addDefensiveAlliance(Population population, OneGenome parentGraph, int SIZE_OF_DEFENSIVE_ALLIANCE) {
        boolean foundnewDefensiveAlliance = false;
        List<Genome> connected_DA = new LinkedList<>();
        for (int i = 0; (i < population.getPopulation().length && population.getPopulation()[i].getFitness() > 0); i++) {
            Genome g = population.getPopulation()[i];
            //if the genome is already in the defensive alliances, skip it
            if (!Genome.checkIfListContainsGenome(defensiveAlliances, g)) {
                foundnewDefensiveAlliance = true; //found a new defensive alliance

                defensiveAlliances.add(new Genome(g)); //add a deep copy of the genome to the defensive alliances
                // all conected components of a DA are also a DA by definition
                List<Genome> connectedComponents = g.getConnectedSubgraphs(parentGraph);
                connected_DA.addAll(connectedComponents);


                //add new defensive alliances to the list of connected defensive alliances
                defensiveAlliances.addAll(connectedComponents);
                connected_defensiveAlliances.addAll(connectedComponents); //add all connected components to the connected defensive alliances
            }
        }
        //calculate fitness of each connected component
        for (Genome component : connected_DA) {
            component.setFitness(FitnessFunctions.calculateFitnessForDA(component, parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE));
        }

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

        if (foundnewDefensiveAlliance){
            //add population to connected_DA
            connected_DA.addAll(Arrays.asList(population.getPopulation()));
            //delete duplicates from connected_DA
            Genome.deleteDuplicates(connected_DA);
            //sort connectedComponents by fitness
            connected_DA.sort(Comparator.comparingInt(Genome::getFitness).reversed());
            // split connected_DA at population.size()
            Population.population = connected_DA.subList(0, population.getPopulation().length).toArray(new Genome[0]);

            Genome.deleteDuplicates(defensiveAlliances);
            Genome.deleteDuplicates(connected_defensiveAlliances);
            System.out.println("\u001B[31m" + "new Defensive Alliances found: "+ "\u001B[0m"); 
            return false;
        }
        return true;
    }
        

    //when using onepointcrossover the parentgraph should not be included in the population!
    // GeneticAlgorithm.java: updated method signature
    //Genetic Algorithm run without capped learners
    static void geneticAlgorithm(
            int NUMBER_OF_NODES,
            float NODE_EXISTENCE_PROBABILITY,
            int POPULATION_SIZE,
            int NUMBER_OF_ITERATIONS,
            int SIZE_OF_DEFENSIVE_ALLIANCE,
            int BREAK_FITNESS,
            int NUMBER_OF_PARENTS,
            float mutationRate,
            OneGenome parentGraph,
            int recombinationMethod,
            int numberOfChildsPerParent,
            float intersectionProbability,
            int selectionMethod,
            boolean duplicatesAllowed,
            int mutationMethod,
            boolean activateLearning,
            int amountOfLearnings
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
                    NUMBER_OF_PARENTS,
                    selectionMethod, duplicatesAllowed); // selectionMethod if you want to use a specific selection method, otherwise random
            
            Population.population = Population.newGeneration(
                    mutationRate,
                    intersectionProbability,
                    numberOfChildsPerParent,
                    newGenParents,
                    mutationMethod,
                    recombinationMethod,
                    activateLearning,
                    amountOfLearnings,
                    SIZE_OF_DEFENSIVE_ALLIANCE
            );

            boolean additionalSort = addDefensiveAlliance(population, parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE);

            if (++counter % 10 == 0 && additionalSort) {
                Population.deleteDuplicates(population.getPopulation(),parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE, NODE_EXISTENCE_PROBABILITY);
                population.sort_Population_by_fitness_and_size_reversed(); //if duplicates are removed, sort the population again
            }

            //adds the best genome to the list of best genomes
            bestGenomes.put(counter, population.getPopulation()[0]);
            //Print some stats and log to file
            population.printStats();
            GeneticLogger.logGeneration();
        }
        resetConsoleOutput();
    }

    // geneticAlgorithm ran with capped Learners
    static void geneticAlgorithm(
            int NUMBER_OF_NODES,
            float NODE_EXISTENCE_PROBABILITY,
            int POPULATION_SIZE,
            int NUMBER_OF_ITERATIONS,
            int SIZE_OF_DEFENSIVE_ALLIANCE,
            int BREAK_FITNESS,
            int NUMBER_OF_PARENTS,
            float mutationrate,
            OneGenome parentGraph,
            int recombinationMethod,
            int numberOfChildsPerParent,
            float intersection_probability,
            int selectionMethod,
            boolean duplicalesAllowed,
            int mutationMethod,
            boolean activateLearning,
            int amountOfLearners,
            int amountOfLearnings,
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
                    NUMBER_OF_PARENTS,
                    selectionMethod, duplicalesAllowed);// selectionMethod if you want to use a specific selection method, otherwise random

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
                    amountOfLearnings,
                    randomizeLearners,
                    SIZE_OF_DEFENSIVE_ALLIANCE
            );

            boolean additionalSort = addDefensiveAlliance(population, parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE);

            if (++counter % 10 == 0 && additionalSort) {
                Population.deleteDuplicates(population.getPopulation(),parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE, NODE_EXISTENCE_PROBABILITY);
                population.sort_Population_by_fitness_and_size_reversed(); //if duplicates are removed, sort the population again
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

        String filePath = cfg.FILEPATH;
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
                    cfg.NUMBER_OF_PARENTS,
                    cfg.MUTATION_RATE,
                    parentGraph,
                    recombId,
                    cfg.NUMBER_OF_CHILDS_PER_PARENT,
                    cfg.INTERSECTION_PROBABILITY,
                    selectionId,
                    cfg.ALLOW_DUPLICATE_PARENTS,
                    mutId,
                    cfg.ACTIVATE_LEARNING,
                    cfg.AMOUNT_OF_LEARNERS,
                    cfg.AMOUNT_OF_LEARNINGS,
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
                    cfg.NUMBER_OF_PARENTS,
                    cfg.MUTATION_RATE,
                    parentGraph,
                    recombId,
                    cfg.NUMBER_OF_CHILDS_PER_PARENT,
                    cfg.INTERSECTION_PROBABILITY,
                    selectionId,
                    cfg.ALLOW_DUPLICATE_PARENTS,
                    mutId,
                    cfg.ACTIVATE_LEARNING,
                    cfg.AMOUNT_OF_LEARNINGS
            );
        }



        // At end of execution
        cfg.writeToFile(GeneticLogger.getOutputDirectory() +"runConfiguration.txt");
        String csvPath = GeneticLogger.getOutputDirectory() + "ga_stats.csv";
        
        //create document containing defensive alliances
        if (!defensiveAlliances.isEmpty()) {
            // Write defensive alliances to file
            File foundDefensiveAlliancesFile = new File(GeneticLogger.getOutputDirectory() + "DefensiveAlliances.txt");
            GeneticLogger.printDefensiveAlliances(foundDefensiveAlliancesFile, defensiveAlliances);
        }

        //create document containing connected defensive alliances
        if (!connected_defensiveAlliances.isEmpty()) {
            File foundDefensiveAlliancesFile = new File(GeneticLogger.getOutputDirectory() + "connected_DefensiveAlliances.txt");
            GeneticLogger.printDefensiveAlliances(foundDefensiveAlliancesFile, connected_defensiveAlliances);
        }
        
        System.out.println("CSV_PATH:" + csvPath);  // Special marker for batch file
    }

}
