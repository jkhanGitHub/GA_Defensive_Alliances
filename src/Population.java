import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;

//many methods might have unused parameters, these are just there so that the methods can be interchanged in the genetic algorithm
public class Population{
    Genome[] population; //Array is used since it is easy to update and we keep its size static

    int amountOfLearning = Genetic_Algorithm.AmountOfLearnings;

    int generation = 0;

    long population_fitness;

    int population_fitness_positive;

    int mean_fitness;

    int mean_size;


    static Dictionary<Integer,String> mutationIdentifiers = new Hashtable<Integer,String>();
    static {
        mutationIdentifiers.put(0,"Mutation");
        mutationIdentifiers.put(1, "Mutation of vertices with high degree");
    }

    public  Genome[] getPopulation() {
        return population;
    }

    public int getGeneration() {
        return generation;
    }

    public long getPopulation_fitness() {
        return population_fitness;
    }

    public void setPopulation_fitness(int population_fitness) {
        this.population_fitness = population_fitness;
    }

    public int getMean_fitness() {
        return mean_fitness;
    }

    public void setMean_fitness(int mean_fitness) {
        this.mean_fitness = mean_fitness;
    }


    public void setMean_size(int mean_size) {
        this.mean_size = mean_size;
    }

    Population(int sizeOfPopulation, int numberOFNodes, float existenceRate, int[][] graph, OneGenome parentGraph){
        population = new Genome[sizeOfPopulation];
        generation++;
        Thread[] threads = new Thread[sizeOfPopulation];
        //generation number will be updated in Selection in order to reuse the sorted population
        //Generates Genomes

        //comment out and change loop to start at 0 if parent graph should not be in array makes sense when using OnepointCrossover
        population[0] = parentGraph;
        for (int i = 0; i < population.length; i++) {
            final int finalI = i;
            threads[finalI] = new Thread(()-> {
                population[finalI] = new Genome(numberOFNodes,existenceRate,graph);
                //calculate degrees
                Genome.calculateDegreesUndirected(graph, population[finalI]);
                //calculate fitness
                population[finalI].setFitness(FitnessFunctions.calculateFitnessMIN(population[finalI], parentGraph));
            });
            threads[finalI].start();
        }

        for (Thread t:
             threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        sort_Population_by_fitness_and_size_reversed();
        population_fitness = FitnessFunctions.calculate_Population_fitness(this);
        mean_fitness = FitnessFunctions.calculate_Mean_fitness(this);
        mean_size = calculateMeanSize(this);
    }


    Population(Population oldGeneration, int[][] graph, OneGenome parentGraph, float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents, int mutation_identifier, int recombination_identifier, boolean activateLearning){
        population = new Genome[oldGeneration.population.length];
        List<Genome> childrenList;
        childrenList = generate_nextChildrenListThreaded(graph, parentGraph, mutationrate, proabibility, newChildsPerParents, nextGenParents, mutation_identifier, recombination_identifier);

        List<Genome> newGeneration;
        newGeneration = createListOfNextGeneration_Boltzmann(oldGeneration, childrenList);

        if(activateLearning){
            survivors_learn(oldGeneration, newGeneration, parentGraph, amountOfLearning);
            newGeneration.sort(Comparator.comparingInt(Genome::getFitness).reversed());
        }

        //add Entries to the new population
        for (int i = 0; i < newGeneration.size(); i++) {
            population[i] = newGeneration.get(i);
        }

        generation = oldGeneration.getGeneration()+1;
        population_fitness = FitnessFunctions.calculate_Population_fitness(this);
        mean_fitness = FitnessFunctions.calculate_Mean_fitness(this);
        mean_size = calculateMeanSize(this);
    }


    static int calculateMeanSize(Population population){
        int sum = 0;
        for (Genome genom:
                population.population) {
            sum += genom.getSize();
        };
        return sum/population.population.length;
    }

    /*Population(int sizeOfPopulation, int numberOFNodes, float existenceRate, int[][] graph, OneGenome parentGraph){

        population = new Genome[sizeOfPopulation];
        Thread[] threads = new Thread[sizeOfPopulation];
        //generation number will be updated in Selection in order to reuse the sorted population
        //Generates Genomes


        //comment out and change loop to start at 0 if parent graph should not be in array
        //population[0] = parentGraph;

        for (int i = 0; i < population.length; i++) {
            final int finalI = i;
            threads[finalI] = new Thread(()-> population[finalI] = new Genome(numberOFNodes,existenceRate,graph));
            threads[finalI].start();
        }
        for (Thread t:
             threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }*/
    //comment out and change loop to start at 0 if parent graph should not be in array

    //best fitness to worst
    public void sort_Population_by_fitness_and_size_reversed(){
        Arrays.sort(population, Comparator.comparingInt(Genome::getFitness).reversed());
    }

    void survivors_learn(Population oldGeneration, List<Genome> newGeneration, OneGenome parentGraph, int amountOfLearning) {
        List<Genome> survivors = getSurvivors(oldGeneration, newGeneration);
        Thread[] threads = new Thread[survivors.size()];

        for (int i = 0; i < survivors.size(); i++) {
            final int index = i;
            threads[index] = new Thread(() -> {
                Genome.learn(survivors.get(index), parentGraph, amountOfLearning);
            });
            threads[index].start();
        }
        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
                throw new RuntimeException(e);
            }
        }
    }
    static List<Genome> generate_nextChildrenListThreaded(int[][] graph, OneGenome parentGraph, float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents,int mutation_identifier, int recombination_identifier) {

        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list
        System.out.println("Recombination Method: " + Recombinations.recombinationIdentifiers.get(recombination_identifier));

        //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
        for (int i = 0, j = 1; j < nextGenParents.size(); i = i + 2, j = j + 2) {
            int[][] geneticCodesOfChildrens = Recombinations.recombination_with_identifier(nextGenParents.get(i).getGenome(),nextGenParents.get(j).getGenome(),proabibility,newChildsPerParents,recombination_identifier);
            Thread[] threads = new Thread[newChildsPerParents];
            for (int k = 0; k < geneticCodesOfChildrens.length; k++) {
                final int finalI = k;
                threads[finalI] = new Thread(() -> {
                    Genome newChild = new Genome(geneticCodesOfChildrens[finalI]);

                    /*
                    //easiest way to mutate the genome
                    int[] mutation = Mutations.mutation(mutationrate,newChild);
                    newChild.setGenome(mutation);
                    */

                    //Mutation
                    switch (mutation_identifier) {
                        case 0:
                            //Mutation
                            int[] mutated = Mutations.mutation(mutationrate,newChild);
                            newChild.setGenome(mutated);
                            break;
                        case 1:
                            //Mutation of vertices with high degree
                            int[] mutated_high_degree = Mutations.mutation_of_vertices_with_high_degree(mutationrate,newChild);
                            newChild.setGenome(mutated_high_degree);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + mutation_identifier);
                    }

                    //calculate degrees
                    Genome.calculateDegreesUndirected(graph, newChild);

                    //calculate fitness
                    newChild.setFitness(FitnessFunctions.calculateFitnessMIN(newChild, parentGraph));

                    //calculate size
                    newChild.calculateSize();

                    //add to the thread-safe list
                    nextGenChildren.add(newChild);
                });
                threads[finalI].start();
            }
            for (Thread t :
                    threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        //Elites of the previous gen stay in the next generation
        //Number of Elites = POPULATION_SIZE - (POPULATION_SIZE/numberOfContestantsPerRound)

        return nextGenChildren;
    }



    static List<Genome> generate_nextChildrenList(int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents, int mutation_identifier, int recombination_identifier) {

        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list
        System.out.println("Recombination Method: " + Recombinations.recombinationIdentifiers.get(recombination_identifier));

        //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
        for (int i = 0,j = 1; j < nextGenParents.size(); i=i+2,j= j+2) {
            int[][] geneticCodesOfChildrens = Recombinations.recombination_with_identifier(nextGenParents.get(i).getGenome(),nextGenParents.get(j).getGenome(),proabibility,newChildsPerParents,recombination_identifier);
            for (int k = 0; k < geneticCodesOfChildrens.length; k++) {
                Genome newChild = new Genome(geneticCodesOfChildrens[k]);

                //Mutation
                switch (mutation_identifier) {
                    case 0:
                        //Mutation
                        int[] mutated = Mutations.mutation(mutationrate,newChild);
                        newChild.setGenome(mutated);
                        break;
                    case 1:
                        //Mutation of vertices with high degree
                        int[] mutated_high_degree = Mutations.mutation_of_vertices_with_high_degree(mutationrate,newChild);
                        newChild.setGenome(mutated_high_degree);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + mutation_identifier);
                }

                //calculate degrees
                Genome.calculateDegreesUndirected(graph,newChild);

                //calculate fitness
                newChild.setFitness(FitnessFunctions.calculateFitnessMIN(newChild,parentGraph));
                nextGenChildren.add(newChild);

                //calculate size
                newChild.calculateSize();
            }
        }

        //Elites of the previous gen stay in the next generation
        //Number of Elites = POPULATION_SIZE - (POPULATION_SIZE/numberOfContestantsPerRound)
        return nextGenChildren;
    }


    List<Genome> createListOfNextGeneration_Boltzmann(Population population, List<Genome> newGenomes){
        int counter=0;

        //add OldGeneration to the list
        newGenomes.addAll(Arrays.asList(population.population));

        //sort the newGenomes by fitness
        newGenomes.sort(Comparator.comparingInt(Genome::getFitness).reversed());

        //cut the list to the size of the population
        newGenomes = newGenomes.subList(0, population.population.length);

        return newGenomes;
    }


    static List<Genome> getSurvivors(Population population, List<Genome> newGenomes){
        //get List of survivors
        List<Genome> survivors = new ArrayList<>();
        for (Genome oldTimer : population.getPopulation()) {
            if(newGenomes.contains(oldTimer)){
                survivors.add(oldTimer);
            }
        }
        return survivors;
    }

    static Population remove_isolated_nodes(Population population,OneGenome parentGraph){
        for (int i=0; i<population.population.length; i++){
                //remove the isolated node
                population.population[i] = Genome.removeIsolatedNodes(population.population[i],parentGraph);
        }
        return population;
    }
    //o(n) = (n^2)
    static Population remove_duplicates(Population population, int numberOFNodes, float existenceRate, int[][] graph, OneGenome parentGraph){

        //remove isolated nodes
        Population temp = remove_isolated_nodes(population, parentGraph);

        boolean found = false;
        int counter = 0;
        for (int i = 0; i < temp.population.length-1; i++) {
            for (int j = i+1; j < temp.population.length; j++) {
                int difference = Genome.difference(temp.population[i],temp.population[j]);
                if (difference==0){
                    //remove the duplicate
                    temp.population[i] = new Genome(numberOFNodes,existenceRate,graph);

                    //calculate degrees
                    Genome.calculateDegreesUndirected(graph,temp.population[i]);
                    //calculate fitness
                    temp.population[i].setFitness(FitnessFunctions.calculateFitnessMIN(temp.population[i],parentGraph));
                    //calculate size
                    temp.population[i].calculateSize();

                    found = true;
                    counter++;
                    break;
                }
            }
        }
        if (found) {
            System.out.println("Duplicates found and removed: " + counter);
        }
        else {
            System.out.println("No duplicates found");
        }
        return temp;
    }

    static Population remove_duplicates_Threaded(Population population, int numberOFNodes, float existenceRate, int[][] graph, OneGenome parentGraph){

        // Thread-safe map to store updated genomes
        ConcurrentHashMap<Integer, Genome> updatedPopulation = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(population.population.length);


        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < population.population.length; i++) {
            final int index = i;
            executor.submit(() -> {
                //remove isolated nodes
                population.population[index] = Genome.removeIsolatedNodes(population.population[index], parentGraph);

                //loop wont run if i = population.population.length
                for (int j = index+1; j < population.population.length; j++) {
                    int difference = Genome.difference(population.population[index],population.population[j]);
                    if (difference==0){
                        //create complementary genome
                        Genome newGenome = new Genome(population.population[index].getGenome(),true);

                        //calculate degrees
                        Genome.calculateDegreesUndirected(graph, newGenome);
                        //calculate fitness
                        newGenome.setFitness(FitnessFunctions.calculateFitnessMIN(newGenome, parentGraph));
                        //calculate size
                        newGenome.calculateSize();
                        updatedPopulation.put(index, newGenome);
                        found.set(true);
                        counter.incrementAndGet();
                        break;
                    }
                }
            });
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (Map.Entry<Integer, Genome> entry : updatedPopulation.entrySet()) {
            int index = entry.getKey();
            population.population[index] = entry.getValue();
        }
        if (found.get()) {
            System.out.println("\u001B[35m"+"Duplicates found and removed: " + counter.get()+"\u001B[0m");
        }
        return population;
    }
}