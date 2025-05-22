import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;

//many methods might have unused parameters, these are just there so that the methods can be interchanged in the genetic algorithm
public class Population {
    static Genome[] population; //Array is used since it is easy to update and we keep its size static

    static int amountOfLearning = Genetic_Algorithm.AmountOfLearnings;

    static int generation = 0;

    static long population_fitness;

    int population_fitness_positive;

    static int mean_fitness;

    static int mean_size;
    static OneGenome parentGraph; //every Population has the same parentgraph

    static List<Genome> survivors = new ArrayList<>();
    static List<Genome> offspringsFromPreviousGeneration = new ArrayList<>();

    static Genome bestGenomeFromLastGeneration;
    static Dictionary<Integer, String> mutationIdentifiers = new Hashtable<Integer, String>();

    static {
        mutationIdentifiers.put(0, "Mutation");
        mutationIdentifiers.put(1, "Mutation of vertices with high degree");
    }

    public static Genome[] getPopulation() {
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

    Population(int sizeOfPopulation, int numberOFNodes, float existenceRate,OneGenome parentGraph) {
        population = new Genome[sizeOfPopulation];
        generation++;
        Population.parentGraph = parentGraph;

        Thread[] threads = new Thread[sizeOfPopulation];
        //generation number will be updated in Selection in order to reuse the sorted population
        //Generates Genomes

        //comment out and change loop to start at 0 if parent graph should not be in array makes sense when using OnepointCrossover
        population[0] = parentGraph;
        for (int i = 0; i < population.length; i++) {
            final int finalI = i;
            threads[finalI] = new Thread(() -> {
                population[finalI] = new Genome(numberOFNodes, existenceRate);
                //calculate degrees
                Genome.calculateDegreesUndirected(parentGraph.graph, population[finalI]);
                //calculate fitness
                population[finalI].setFitness(FitnessFunctions.calculateFitnessMIN(population[finalI], parentGraph));
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

        sort_Population_by_fitness_and_size_reversed();
        population_fitness = FitnessFunctions.calculate_Population_fitness(this);
        mean_fitness = FitnessFunctions.calculate_Mean_fitness(this);
        mean_size = calculateMeanSize();
    }


    Population(Population oldGeneration, float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents, int mutation_identifier, int recombination_identifier, boolean activateLearning) {
        population = new Genome[oldGeneration.population.length];
        bestGenomeFromLastGeneration = oldGeneration.getPopulation()[0];
        offspringsFromPreviousGeneration = generate_nextChildrenListThreaded(mutationrate, proabibility, newChildsPerParents, nextGenParents, mutation_identifier, recombination_identifier);

        List<Genome> newGeneration;
        newGeneration = createListOfNextGeneration_Boltzmann(offspringsFromPreviousGeneration);

        survivors = getSurvivors(newGeneration);
        if (activateLearning) {
            if (!survivors.isEmpty()){
                survivors_learn(survivors, parentGraph, amountOfLearning);
                newGeneration.sort(Comparator.comparingInt(Genome::getFitness).reversed());
            }
        }

        //add Entries to the new population
        for (int i = 0; i < newGeneration.size(); i++) {
            population[i] = newGeneration.get(i);
        }

        generation = oldGeneration.getGeneration() + 1;
        population_fitness = FitnessFunctions.calculate_Population_fitness(this);
        mean_fitness = FitnessFunctions.calculate_Mean_fitness(this);
        generation++;
        mean_size = calculateMeanSize();
    }

    //used in Genetic Algorithm
    static Genome[] newGeneration(float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents, int mutation_identifier, int recombination_identifier, int amountOfLearners, boolean randomizeLearners) {
        bestGenomeFromLastGeneration = getPopulation()[0];

        List<Genome> childrenList;
        childrenList = generate_nextChildrenListThreaded(mutationrate, proabibility, newChildsPerParents, nextGenParents, mutation_identifier, recombination_identifier);

        List<Genome> newGeneration;
        newGeneration = createListOfNextGeneration_Boltzmann(childrenList);


        survivors = getSurvivors(newGeneration);
        if (!survivors.isEmpty()){
            survivors_learn(survivors, parentGraph, amountOfLearning,amountOfLearners,randomizeLearners);
            newGeneration.sort(Comparator.comparingInt(Genome::getFitness).reversed());
        }
        newGeneration.sort(Comparator.comparingInt(Genome::getFitness).reversed());


        //add Entries to the new population
        for (int i = 0; i < newGeneration.size(); i++) {
            population[i] = newGeneration.get(i);
        }

        generation++;
        population_fitness = calculate_Population_fitness();
        mean_fitness = calculate_Mean_fitness();
        mean_size = calculateMeanSize();

        return population;
    }


    //you can change generate_nextChildrenList to generate_nextChildrenListThreaded if you want to use the threaded version
    static Genome[] newGeneration(float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents, int mutation_identifier, int recombination_identifier, boolean activateLearning) {
        bestGenomeFromLastGeneration = getPopulation()[0];
        offspringsFromPreviousGeneration = generate_nextChildrenListThreaded(mutationrate, proabibility, newChildsPerParents, nextGenParents, mutation_identifier, recombination_identifier);

        List<Genome> newGeneration;
        newGeneration = createListOfNextGeneration_Boltzmann(offspringsFromPreviousGeneration);

        survivors = getSurvivors(newGeneration);
        if (activateLearning) {
            if (!survivors.isEmpty()){
                survivors_learn(survivors, parentGraph, amountOfLearning);
                newGeneration.sort(Comparator.comparingInt(Genome::getFitness).reversed());
            }
        }

        //add Entries to the new population
        for (int i = 0; i < newGeneration.size(); i++) {
            population[i] = newGeneration.get(i);
        }

        generation++;
        population_fitness = calculate_Population_fitness();
        mean_fitness = calculate_Mean_fitness();
        mean_size = calculateMeanSize();

        return population;
    }

    static public int calculate_Population_fitness() {
        int sum =  0;
        for (Genome genom:
                population) {
            sum += genom.fitness;
        };
        return sum;
    }

    static public int calculate_Mean_fitness(){
        return (int) population_fitness/population.length;
    }


    Population(Population oldGeneration, float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents, int mutation_identifier, int recombination_identifier, int amountOfLearners, boolean randomizeLearners) {
        population = new Genome[oldGeneration.population.length];
        bestGenomeFromLastGeneration = oldGeneration.getPopulation()[0];
        offspringsFromPreviousGeneration = generate_nextChildrenListThreaded(mutationrate, proabibility, newChildsPerParents, nextGenParents, mutation_identifier, recombination_identifier);

        List<Genome> childrenList;
        childrenList = generate_nextChildrenListThreaded(mutationrate, proabibility, newChildsPerParents, nextGenParents, mutation_identifier, recombination_identifier);

        List<Genome> newGeneration;
        newGeneration = createListOfNextGeneration_Boltzmann(childrenList);


        survivors = getSurvivors(newGeneration);
        if (!survivors.isEmpty()){
            survivors_learn(survivors, parentGraph, amountOfLearning,amountOfLearners,randomizeLearners);
            newGeneration.sort(Comparator.comparingInt(Genome::getFitness).reversed());
        }
        newGeneration.sort(Comparator.comparingInt(Genome::getFitness).reversed());


        //add Entries to the new population
        for (int i = 0; i < newGeneration.size(); i++) {
            population[i] = newGeneration.get(i);
        }

        generation = oldGeneration.getGeneration() + 1;
        population_fitness = FitnessFunctions.calculate_Population_fitness(this);
        mean_fitness = FitnessFunctions.calculate_Mean_fitness(this);
        mean_size = calculateMeanSize();
    }

    static int calculateMeanSize() {
        int sum = 0;
        for (Genome genom :
                population) {
            sum += genom.getSize();
        }
        return sum / population.length;
    }

    //best fitness to worst
    public void sort_Population_by_fitness_and_size_reversed() {
        Arrays.sort(population, Comparator.comparingInt(Genome::getFitness).reversed());
    }

    int getAmountOfRejectedChildren(int amountOfChildren, int amountOfAccepted) {
        return amountOfChildren - amountOfAccepted;

    }

    static void survivors_learn(List<Genome> survivors, OneGenome parentGraph, int amountOfLearning) {
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

    static void survivors_learn(List<Genome> survivors, OneGenome parentGraph, int amountOfLearning, int learnerAmount, boolean randomizeLearners) {
        /**
         * This method is used to let the surviviors of the last generation learn.
         * @learnerAmount is the amount of survivors that are going to learn.
         * @randomizeLearners true results in the learners being picked at random.
         * @randomizeLearners false results in the best survivors learning. since the list should be sorted.
         */
        Thread[] threads = new Thread[learnerAmount];

        if (randomizeLearners) {
            Collections.shuffle(survivors);
        }
        for (int i = 0; (i < survivors.size()) && (i < learnerAmount); i++) {
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

    static List<Genome> generate_nextChildrenListThreaded(float mutationrate, float probability, int newChildsPerParents, List<Genome> nextGenParents, int mutation_identifier, int recombination_identifier) {

        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list
        System.out.println(Recombinations.recombinationIdentifiers.get(recombination_identifier));

        //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
        for (int i = 0, j = 1; j < nextGenParents.size(); i = i + 2, j = j + 2) {
            Genome[] childrens = Recombinations.recombination_with_identifier(nextGenParents.get(i), nextGenParents.get(j), probability, newChildsPerParents, recombination_identifier);
            Thread[] threads = new Thread[newChildsPerParents];
            for (int k = 0; k < childrens.length; k++) {
                final int finalI = k;
                threads[finalI] = new Thread(() -> {
                    Genome newChild = childrens[finalI];

                    //mother always give first part of the Genome and father gives the second part
                    //calculate degrees
                    //iterate through changedAllele use update degrees on every int value in changedAllele
                    if (recombination_identifier == 0){
                        newChild.updateChildDegrees_crossover(parentGraph.graph);
                    }
                    if (recombination_identifier == 1) {
                        for (int x = 0; x < newChild.changedAllele.size(); x++) {
                            int index = newChild.changedAllele.get(x);
                            newChild.addNode(parentGraph.graph, index);
                        }
                    }

                    //test
                    /*
                    int degrees[] = new int[newChild.length];
                    degrees = Arrays.copyOf(newChild.degrees,newChild.length);

                    newChild.degrees = new int[newChild.length];
                    Genome.calculateDegreesUndirected(parentGraph.graph, newChild);
                    int t = 0;
                    t++;

                     */

                    //Mutation
                    switch (mutation_identifier) {
                        case 0:
                            //Mutation //mutation works on reference so the Genome will already be changed
                            Mutations.mutation(mutationrate, newChild, parentGraph.graph);
                            break;
                        case 1:
                            //Mutation of vertices with high degree
                            Mutations.mutation_of_vertices_with_high_degree(mutationrate, newChild, parentGraph.graph);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + mutation_identifier);
                    }

                    //calculate fitness
                    int fitnessValue = FitnessFunctions.calculateFitnessMIN(newChild, parentGraph);
                    if (fitnessValue==0)System.out.println("\u001B[31m"+fitnessValue);
                    newChild.setFitness(fitnessValue);

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



    static List<Genome> generate_nextChildrenList(float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents, int mutation_identifier, int recombination_identifier) {

        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list

        //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
        for (int i = 0, j = 1; j < nextGenParents.size(); i = i + 2, j = j + 2) {
            Genome[] childrens = Recombinations.recombination_with_identifier(nextGenParents.get(i), nextGenParents.get(j), proabibility, newChildsPerParents, recombination_identifier);
            for (int k = 0; k < childrens.length; k++) {
                Genome newChild = childrens[k];

                //mother always give first part of the Genome and father gives the second part
                //calculate degrees
                //iterate through changedAllele use update degrees on every int value in changedAllele
                if (recombination_identifier == 0){
                    newChild.updateChildDegrees_crossover(parentGraph.graph);
                }
                if (recombination_identifier == 1) {
                    for (int x = 0; x < newChild.changedAllele.size(); x++) {
                        int index = newChild.changedAllele.get(x);
                        newChild.addNode(parentGraph.graph, index);
                    }
                }

                //Mutation
                switch (mutation_identifier) {
                    case 0:
                        //Mutation //mutation works on reference so the Genome will already be changed
                        Mutations.mutation(mutationrate, newChild,parentGraph.graph);
                        break;
                    case 1:
                        //Mutation of vertices with high degree
                        Mutations.mutation_of_vertices_with_high_degree(mutationrate, newChild, parentGraph.graph);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + mutation_identifier);
                }

                //calculate fitness
                newChild.setFitness(FitnessFunctions.calculateFitnessMIN(newChild, parentGraph));

                nextGenChildren.add(newChild);
            }
        }

        //Elites of the previous gen stay in the next generation
        //Number of Elites = POPULATION_SIZE - (POPULATION_SIZE/numberOfContestantsPerRound)
        return nextGenChildren;
    }


    static List<Genome> createListOfNextGeneration_Boltzmann(List<Genome> newGenomes) {
        int counter = 0;

        //add OldGeneration to the list
        newGenomes.addAll(Arrays.asList(population));

        //sort the newGenomes by fitness
        newGenomes.sort(Comparator.comparingInt(Genome::getFitness).reversed());

        //cut the list to the size of the population
        newGenomes = newGenomes.subList(0, population.length);

        return newGenomes;
    }


    static List<Genome> getSurvivors(List<Genome> newGenomes) {
        //get List of survivors
        List<Genome> survivors = new ArrayList<>();
        for (Genome oldTimer : getPopulation()) {
            if (newGenomes.contains(oldTimer)) {
                survivors.add(oldTimer);
            }
        }
        return survivors;
    }

    static Population remove_isolated_nodes(Population population, OneGenome parentGraph) {
        for (int i = 0; i < population.population.length; i++) {
            //remove the isolated node
            population.population[i] = Genome.removeIsolatedNodes(population.population[i], parentGraph);
        }
        return population;
    }

    //o(n) = (n^2)
    static Population remove_duplicates(Population population, int numberOFNodes, float existenceRate, OneGenome parentGraph) {

        //remove isolated nodes
        Population temp = remove_isolated_nodes(population, parentGraph);

        boolean found = false;
        int counter = 0;
        for (int i = 0; i < temp.population.length - 1; i++) {
            for (int j = i + 1; j < temp.population.length; j++) {
                int difference = Genome.difference(temp.population[i], temp.population[j]);
                if (difference == 0) {
                    //remove the duplicate
                    temp.population[i] = new Genome(numberOFNodes, existenceRate);

                    //calculate degrees
                    Genome.calculateDegreesUndirected(parentGraph.graph, temp.population[i]);
                    //calculate size
                    temp.population[i].calculateSize();
                    //calculate fitness
                    temp.population[i].setFitness(FitnessFunctions.calculateFitnessMIN(temp.population[i], parentGraph));


                    found = true;
                    counter++;
                    break;
                }
            }
        }
        if (found) {
            System.out.println("Duplicates found and removed: " + counter);
        } else {
            System.out.println("No duplicates found");
        }
        return temp;
    }

    static Genome[] remove_duplicates_Threaded(int numberOFNodes, float existenceRate, OneGenome parentGraph) {

        // Thread-safe map to store updated genomes
        ConcurrentHashMap<Integer, Genome> updatedPopulation = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(population.length);


        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < population.length; i++) {
            final int index = i;
            executor.submit(() -> {
                //remove isolated nodes
                population[index] = Genome.removeIsolatedNodes(population[index], parentGraph);

                //loop wont run if i = population.population.length
                for (int j = index + 1; j < population.length; j++) {
                    int difference = Genome.difference(population[index], population[j]);
                    if (difference == 0) {
                        //create complementary genome
                        Genome newGenome = new Genome(population[index].getGenome(), true);

                        //calculate size
                        newGenome.calculateSize();
                        //calculate degrees
                        Genome.calculateDegreesUndirected(parentGraph.graph, newGenome);
                        //calculate fitness
                        newGenome.setFitness(FitnessFunctions.calculateFitnessMIN(newGenome, parentGraph));

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
            population[index] = entry.getValue();
        }
        if (found.get()) {
            System.out.println("\u001B[35m" + "Duplicates found and removed: " + counter.get() + "\u001B[0m");
        }
        return population;
    }

    static void printStats(Population population) {
        System.out.println("Generation: " + population.generation);
        System.out.println("Population fitness: " + population.population_fitness);
        System.out.println("Mean fitness: " + population.mean_fitness);
        System.out.println("Mean size: " + population.mean_size);
        System.out.println("Population size: " + population.population.length);
    }

    void printStats() {
        System.out.println("Generation: " + generation);
        System.out.println("Population fitness: " + population_fitness);
        System.out.println("Mean fitness: " + mean_fitness);
        System.out.println("Mean size: " + mean_size);
        System.out.println("Population size: " + population.length +'\n');

        System.out.println("Amount of Genomes that survived: " + survivors.size());
        System.out.println("Amount of Accepted Children: " + (population.length - survivors.size())+"\n");

        System.out.println("First Best Fitness in Population: " + population[0].getFitness() + " Size: " + population[0].getSize());
        System.out.println("Second Best Fitness in Population: " + population[1].getFitness() + " Size: " + population[1].getSize());
        System.out.println("Worst Fitness in Population: " + population[population.length-1].getFitness() + " Size: " + population[population.length-1].getSize());
        System.out.println("Difference between best and second best genome: " + Genome.difference(population[0], population[1]));
        System.out.println("Genetic Difference between best Genomes of current and past generation: " + Genome.difference(population[0], bestGenomeFromLastGeneration));
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------"+'\n');
    }
}