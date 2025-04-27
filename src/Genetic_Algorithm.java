import java.io.IOException;
import java.util.List;

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



    //Path to the csv file
    public static final String FILEPATH = "deezer_europe/deezer_europe/deezer_europe_edges.csv";

    //Number of Nodes of graph
    public static final int NUMBER_OF_NODES = 28281;

    public static final float NODE_EXISTENCE_PROBABILITY = 0.5F;

    public static final int POPULATION_SIZE = 1024; //Powers of two are best suited for variable tournament selection, use  factors of two for 1v1


    //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
    //Number of children = number of parents*NUMBER_OF_CHILDS_PER_PARENT
    //new population will take over the best from last generation + all new children
    //WARNING: IF NUMBER OF PARENTS IS >= Number of children then the whole population will be replaced
    // new nodes in next population is (NUMBER_OF_CONTESTANTS_PER_ROUND/NUMBER_OF_CHILDS_PER_PARENT) in percent
    public static final int NUMBER_OF_CONTESTANTS_PER_ROUND = 16; // only 1 winner -< lower number ensures more worse parents and probably more diversity

    public static final int NUMBER_OF_CHILDS_PER_PARENT = 8; //


    public static final float MUTATION_RATE = 1/NUMBER_OF_NODES;
    public static final int NUMBER_OF_ITERATIONS = 100; //number of generations

    public static final int BREAK_FITNESS = NUMBER_OF_NODES-2;

    public static  OneGenome PARENT_GRAPH;

    public static final float PROBABILITY = 0.5f;

    public static int[][] graph;

    static void geneticAlgorithm(int NUMBER_OF_NODES, float NODE_EXISTENCE_PROBABILITY, int POPULATION_SIZE, int NUMBER_OF_ITERATIONS,int BREAK_FITNESS, OneGenome PARENT_GRAPH, int[][] graph){
        //Generatess first Population and calculates the Fitness of each Genome
        Population population = new Population(POPULATION_SIZE, NUMBER_OF_NODES, NODE_EXISTENCE_PROBABILITY, graph, PARENT_GRAPH);

        for (int i = 0; i < population.population.length; i++) {

            Genome.calculateDegrees(graph, population.population[i]);
            population.population[i].setFitness(FitnessFunctions.calculateFitness(population.population[i],PARENT_GRAPH));
        }

        population.sort_Population_by_fitness_and_size_reversed();

        /*//prints Perfect minimal solutions maybe comment out
        int i =0;
        while (population.getPopulation()[i].fitness == BREAK_FITNESS) {
            population.getPopulation()[i].printGenome();
        }
        if(i>0) return;
        */

        int numberofiterations = NUMBER_OF_ITERATIONS;
        while (population.getGeneration()!=NUMBER_OF_ITERATIONS){
            //change Selection method to whhatever
            List<Genome> newGenParents = Selection.tournamentSelectionElimination(
                    population,
                    NUMBER_OF_CONTESTANTS_PER_ROUND);

            //create new population
            population = Population.update_Population_RANDOM(
                    population,
                    graph,
                    NUMBER_OF_NODES,
                    PARENT_GRAPH,
                    MUTATION_RATE,
                    PROBABILITY,
                    NUMBER_OF_CHILDS_PER_PARENT,
                    newGenParents);

            population.sort_Population_by_fitness_and_size_reversed();
            population.setPopulation_fitness(FitnessFunctions.calculate_Population_fitness(population));
            population.setMean_fitness(FitnessFunctions.calculate_Mean_fitness(population));

            //Print some stats
            System.out.println("Generation: "+population.generation);
            System.out.println("Fitness of Population: "+population.population_fitness);
            System.out.println("Mean Fitness of Population: "+population.mean_fitness);
            System.out.println("Best Fitness in Population: "+population.getPopulation()[0].getFitness()+ "\t Size: "+population.getPopulation()[0].getSize());
            System.out.println("Second Best Fitness in Population: "+population.getPopulation()[1].getFitness()+"\t Size: "+population.getPopulation()[0].getSize()+"\n");
        }

        //check if best genomes are the same
        boolean b = true;
        for (int i = 0; i < population.getPopulation().length && b; i++) {
            if(population.getPopulation()[0].getGenome()[i]!=population.getPopulation()[1].getGenome()[i]){
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

        PARENT_GRAPH = new OneGenome(NUMBER_OF_NODES,graph);
        PARENT_GRAPH.remove_isolated_nodes();

        Genome.calculateDegrees(graph,PARENT_GRAPH);

        geneticAlgorithm(NUMBER_OF_NODES, NODE_EXISTENCE_PROBABILITY,POPULATION_SIZE, NUMBER_OF_ITERATIONS,BREAK_FITNESS, PARENT_GRAPH, graph);



    }

}
