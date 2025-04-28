import java.io.IOException;
import java.util.*;

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


    public static List<Genome> defensiveAlliances = new ArrayList<>();

    //Path to the csv file
    public static final String FILEPATH = "twitch/twitch/FR/musae_FR_edges.csv";

    //Number of Nodes of graph
    public static final int NUMBER_OF_NODES = 6549;

    public static final float NODE_EXISTENCE_PROBABILITY = 0.5F;

    public static final int POPULATION_SIZE = 8192; //Powers of two are best suited for variable tournament selection, use  factors of two for 1v1

    public static final int MAX_NUMBER_OF_NODES_REMOVED_BY_MUTATION = 5; //maximum number of nodes removed by mutation, smaller numbers will probably have higher impact

    public static final int MAXIMUM_NUMBER_OF_ADDITIONAL_MUTATIONS = POPULATION_SIZE/4; //maximum number of additional mutations, smaller numbers will probably have higher impact


    //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
    //Number of children = number of parents*NUMBER_OF_CHILDS_PER_PARENT
    //new population will take over the best from last generation + all new children
    //WARNING: IF NUMBER OF PARENTS IS >= Number of children then the whole population will be replaced
    // new nodes in next population is (NUMBER_OF_CONTESTANTS_PER_ROUND/NUMBER_OF_CHILDS_PER_PARENT) in percent
    public static final int NUMBER_OF_CONTESTANTS_PER_ROUND = 128; // only 1 winner -< lower number ensures more worse parents and probably more diversity

    //increase this number to increase the number of children per parent also resulting in bigger population in each generation, only makes sense when making population a list wont be doing that tho xD
    //higher number -< earlier local maximum because of incest
    public static final int NUMBER_OF_CHILDS_PER_PARENT = 1;

    public static final int MULTIPLIER = POPULATION_SIZE / NUMBER_OF_CONTESTANTS_PER_ROUND; //Multiplier for some selection methods which have an average parent output of 1

    //Explanation of mutation identfieres found in Population.java mutate_Population()
    public static final float MUTATION_RATE = 1 / NUMBER_OF_NODES;
    public static final int NUMBER_OF_ITERATIONS = 100; //number of generations

    public static final int BREAK_FITNESS = NUMBER_OF_NODES - 2;

    public static OneGenome PARENT_GRAPH;

    public static final float PROBABILITY = 0.5f; //probability of intersection, 0.5 means 50% chance of intersection and 50% chance of crossover

    final static int UPPER_BOUND = POPULATION_SIZE/2; //upper bound for ADDITIONAL_amount of genomes to be mutated
    public static int[][] graph;

    public static Map<Integer, Genome> bestGenomes = new HashMap<>();

    static Random random = new Random();

    static void addDefensiveAlliance(Population population) {
        int i = 0;
        while (population.getPopulation()[i].getFitness() >= 0) {
                defensiveAlliances.add(population.getPopulation()[i]);
                i++;
            }
    }

    //when using onepointcrossover the parentgraph should not be included in the population!
    static void geneticAlgorithm(int NUMBER_OF_NODES, float NODE_EXISTENCE_PROBABILITY, int POPULATION_SIZE, int NUMBER_OF_ITERATIONS, int BREAK_FITNESS, OneGenome PARENT_GRAPH, int[][] graph) {
        //Generatess first Population and calculates the Fitness of each Genome
        Population population = new Population(POPULATION_SIZE, NUMBER_OF_NODES, NODE_EXISTENCE_PROBABILITY, graph, PARENT_GRAPH);

        for (int i = 0; i < population.population.length; i++) {

            Genome.calculateDegrees(graph, population.population[i]);
            population.population[i].setFitness(FitnessFunctions.calculateFitnessMIN(population.population[i], PARENT_GRAPH));
        }

        population.sort_Population_by_fitness_and_size_reversed();
        bestGenomes.put(0, population.getPopulation()[0]);

        /*//prints Perfect minimal solutions maybe comment out
        int i =0;
        while (population.getPopulation()[i].fitness == BREAK_FITNESS) {
            population.getPopulation()[i].printGenome();
        }
        if(i>0) return;
        */


        int counter = 0;
        while (Population.generation != NUMBER_OF_ITERATIONS) {
            //change Selection method to whhatever
            //Literatur says that using many different selection methods is better

            //SelectionMethods:
           /*
    static Dictionary<Integer, String> selectionMethods = new Hashtable<>();
    static{
        selectionMethods.put(0, "Stochastic Universal Sampling");
        selectionMethods.put(1, "Tournament Selection Elimination");
        selectionMethods.put(2, "Roulette Wheel Selection");
        selectionMethods.put(3, "Linear Rank Selection");
        selectionMethods.put(4, "Exponential Rank Selection");
        selectionMethods.put(5, "Elitism");
    }
     */


            List<Genome> newGenParents = Selection.select_SelectionMethod(
                    population,
                    NUMBER_OF_CONTESTANTS_PER_ROUND,
                    random.nextInt(Selection.IMPLEMENTED_SELECTION_METHODS - 1));//-2 since i dont want elitism to be selected or exponential_rankedselection


            //Recombnination Identifiers:
             /*
    static Dictionary<Integer,String> recombinationIdentifiers = new Hashtable<Integer,String>();
    static {
        recombinationIdentifiers.put(0,"OnePointCrossover");
        recombinationIdentifiers.put(1, "ProababilityIntersection");
    }
     */
            //create new population
            population = Population.update_Population_Recombination_Identifier(
                    population,
                    graph,
                    NUMBER_OF_NODES,
                    PARENT_GRAPH,
                    MUTATION_RATE,
                    PROBABILITY,
                    NUMBER_OF_CHILDS_PER_PARENT,
                    newGenParents,
                    1);

            //additional Mutations
            population = Population.mutate_Population_RandomAmount_of_RandomlyChoosen(
                    population,
                    graph,
                    NUMBER_OF_NODES,
                    PARENT_GRAPH,
                    MUTATION_RATE,
                    random.nextInt(2) + 2,
                    //random.nextInt(MAX_NUMBER_OF_NODES_REMOVED_BY_MUTATION)+1
                    random.nextInt(10),
                    UPPER_BOUND
            );

            population.sort_Population_by_fitness_and_size_reversed();
            population.setPopulation_fitness(FitnessFunctions.calculate_Population_fitness(population));
            population.setMean_fitness(FitnessFunctions.calculate_Mean_fitness(population));

            //increase counter
            counter++;

            //adds defensive alliance to a list if it has been found
            addDefensiveAlliance(population);

            //Print some stats
            System.out.println('\n'+ "Generation: " + Population.generation);
            System.out.println("Fitness of Population: " + population.population_fitness);
            System.out.println("Mean Fitness of Population: " + population.mean_fitness);
            System.out.println("Best Fitness in Population: " + population.getPopulation()[0].getFitness() + "\t Size: " + population.getPopulation()[0].getSize());
            System.out.println("Second Best Fitness in Population: " + population.getPopulation()[1].getFitness() + "\t Size: " + population.getPopulation()[1].getSize());
            System.out.println("Worst Fitness in Population: " + population.getPopulation()[POPULATION_SIZE - 1].getFitness() + "\t Size: " + population.getPopulation()[POPULATION_SIZE - 1].getSize() + "\n");

            //Since genetic algorithm have the tendency to get stuck in local optima, we can check if the best genome is the same as the previous one
            bestGenomes.put(counter, population.getPopulation()[0]);

            //check difference between best genomes
            int difference = Genome.difference(bestGenomes.get(counter), bestGenomes.get(counter - 1));
            System.out.println("GENETIC Difference between best Genomes of current and past generation: " + difference);
            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------" + '\n');


        }

        //check if best genomes are the same
        boolean b = true;
        for (int i = 0; i < population.getPopulation().length && b; i++) {
            if (population.getPopulation()[0].getGenome()[i] != population.getPopulation()[1].getGenome()[i]) {
                b = false;
            }
        }
        if (b) System.out.println("same Genomes");
    }


    public static void main(String[] args) {


        try {
            graph = CsvReader.readCsvEdgesToMatrix(FILEPATH, NUMBER_OF_NODES);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PARENT_GRAPH = new OneGenome(NUMBER_OF_NODES, graph);
        PARENT_GRAPH.remove_isolated_nodes();

        Genome.calculateDegrees(graph, PARENT_GRAPH);
        PARENT_GRAPH.calculateWorstFitnessPossible();

        geneticAlgorithm(NUMBER_OF_NODES, NODE_EXISTENCE_PROBABILITY, POPULATION_SIZE, NUMBER_OF_ITERATIONS, BREAK_FITNESS, PARENT_GRAPH, graph);


    }

}
