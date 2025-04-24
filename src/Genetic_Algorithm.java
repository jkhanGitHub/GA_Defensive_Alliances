import java.io.IOException;

public class Genetic_Algorithm {


    /*Generic definition

The following is an example of a generic evolutionary algorithm:

    1.  Randomly generate the initial population of individuals, the first generation.
    2. Evaluate the fitness of each individual in the population.
    3. Check, if the goal is reached and the algorithm can be terminated.
    4. Select individuals as parents, preferably of higher fitness.
    5. Produce offspring with optional crossover (mimicking reproduction).
    6. Apply mutation operations on the offspring.
    7. Select individuals preferably of lower fitness for replacement with new individuals (mimicking natural selection).
    8. Return to 2
     */

    public static final int NUMBER_OF_NODES = 7624;

    public static final float NODE_EXISTENCE_PROBABILITY = 0.5F;

    public static final int POPULATION_SIZE = 1024; //Powers of two are best suited for variable tournament selection, use  factors of two for 1v1

    public static final int NUMBER_OF_CONTESTANTS_PER_ROUND = 16;

    public static final int NUMBER_OF_CHILDS_PER_PARENT = 8;
    public static final float MUTATION_RATE = 1/NUMBER_OF_NODES;
    public static final int NUMBER_OF_ITERATIONS = 100;

    public static final int BREAK_FITNESS = NUMBER_OF_NODES-2;

    public static final String FILEPATH = "lasftm_asia/lastfm_asia_edges.csv";

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
            population = Selection.tournamentSelectionElimination_ProababilityIntersection_Threaded(
                    population,
                    NUMBER_OF_CONTESTANTS_PER_ROUND,
                    graph,
                    NUMBER_OF_NODES,
                    PARENT_GRAPH,
                    MUTATION_RATE,PROBABILITY,
                    NUMBER_OF_CHILDS_PER_PARENT);
            population.sort_Population_by_fitness_and_size_reversed();

            FitnessFunctions.calculate_Population_fitness(population);
            System.out.println(population.generation);
            System.out.println(population.population_fitness);
        }

        //print the top 10 genomes
        for (int i=0; i<10;i++) {
            System.out.println(population.getPopulation()[i].getFitness());
            System.out.println(population.getPopulation()[i].getSize());
            population.getPopulation()[i].printGenome();
            System.out.println(population.getPopulation()[i].isDefensiveAlliance(PARENT_GRAPH));
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
