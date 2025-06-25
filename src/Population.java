import java.util.*;
import  java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;
import java.util.stream.IntStream;

//many methods might have unused parameters, these are just there so that the methods can be interchanged in the genetic algorithm
public class Population {
    static Genome[] population; //Array is used since it is easy to update and we keep its size static

    static int amountOfLearning = Genetic_Algorithm.AmountOfLearnings;

    static int generation = -1;

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

    Population(int sizeOfPopulation, int numberOFNodes, float existenceRate,OneGenome parentGraph, int SIZE_OF_DEFENSIVE_ALLIANCE) {
        population = new Genome[sizeOfPopulation];
        generation++;
        Population.parentGraph = parentGraph;

        Thread[] threads = new Thread[sizeOfPopulation];
        //generation number will be updated in Selection in order to reuse the sorted population
        //Generates Genomes

        //comment out and change loop to start at 0 if parent graph should not be in array makes sense when using OnepointCrossover
        //population[0] = parentGraph;
        for (int i = 0; i < population.length; i++) {
            final int finalI = i;
            threads[finalI] = new Thread(() -> {

                //create a new genome 
                if (parentGraph.Ids_toFilter.isEmpty()) {
                    population[finalI] = new Genome(numberOFNodes, existenceRate);
                } else {
                    population[finalI] = new Genome(numberOFNodes, existenceRate, parentGraph.Ids_toFilter);
                }

                //calculate degrees
                Genome.calculateDegrees_withNeighbourhood(population[finalI]);
                //calculate fitness
                population[finalI].setFitness(FitnessFunctions.calculateFitnessMIN(population[finalI], parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE));
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


    Population(Population oldGeneration, float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents, int mutation_identifier, int recombination_identifier, boolean activateLearning, int SIZE_OF_DEFENSIVE_ALLIANCE) {
        population = new Genome[oldGeneration.population.length];
        bestGenomeFromLastGeneration = new Genome(oldGeneration.getPopulation()[0]);
        offspringsFromPreviousGeneration = generate_nextChildrenListThreaded(mutationrate, proabibility, newChildsPerParents, nextGenParents, mutation_identifier, recombination_identifier, SIZE_OF_DEFENSIVE_ALLIANCE);

        List<Genome> newGeneration;
        newGeneration = createListOfNextGeneration_Boltzmann(offspringsFromPreviousGeneration);

        survivors = getSurvivors(newGeneration);
        if (activateLearning) {
            if (!survivors.isEmpty()){
                survivors_learn(survivors, parentGraph, amountOfLearning, SIZE_OF_DEFENSIVE_ALLIANCE);
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


    Population(Population oldGeneration, float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents, int mutation_identifier, int recombination_identifier, int amountOfLearners, boolean randomizeLearners, int SIZE_OF_DEFENSIVE_ALLIANCE) {
        population = new Genome[oldGeneration.population.length];
        bestGenomeFromLastGeneration = new Genome(oldGeneration.getPopulation()[0]);

        List<Genome> childrenList;
        childrenList = generate_nextChildrenListThreaded(mutationrate, proabibility, newChildsPerParents, nextGenParents, mutation_identifier, recombination_identifier, SIZE_OF_DEFENSIVE_ALLIANCE);

        List<Genome> newGeneration;
        newGeneration = createListOfNextGeneration_Boltzmann(childrenList);


        survivors = getSurvivors(newGeneration);
        if (!survivors.isEmpty()){
            survivors_learn(survivors, parentGraph, amountOfLearning,amountOfLearners,randomizeLearners, SIZE_OF_DEFENSIVE_ALLIANCE);
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

    //used in Genetic Algorithm
    static Genome[] newGeneration(float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents, int mutation_identifier, int recombination_identifier,boolean activateLearning, int amountOfLearners, boolean randomizeLearners, int SIZE_OF_DEFENSIVE_ALLIANCE) {
        bestGenomeFromLastGeneration = new Genome(population[0]);

        List<Genome> childrenList;
        childrenList = generate_nextChildrenListThreaded(mutationrate, proabibility, newChildsPerParents, nextGenParents, mutation_identifier, recombination_identifier, SIZE_OF_DEFENSIVE_ALLIANCE);

        List<Genome> newGeneration;
        newGeneration = createListOfNextGeneration_Boltzmann(childrenList);


        survivors = getSurvivors(newGeneration);
        if (activateLearning) {
            if (!survivors.isEmpty()){
                survivors_learn(survivors, parentGraph, amountOfLearning,amountOfLearners,randomizeLearners, SIZE_OF_DEFENSIVE_ALLIANCE);
                newGeneration.sort(Comparator.comparingInt(Genome::getFitness).reversed());
            }
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
    static Genome[] newGeneration(float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents, int mutation_identifier, int recombination_identifier, boolean activateLearning, int SIZE_OF_DEFENSIVE_ALLIANCE) {
        bestGenomeFromLastGeneration = new Genome(population[0]);
        offspringsFromPreviousGeneration = generate_nextChildrenListThreaded(mutationrate, proabibility, newChildsPerParents, nextGenParents, mutation_identifier, recombination_identifier, SIZE_OF_DEFENSIVE_ALLIANCE);

        List<Genome> newGeneration;
        newGeneration = createListOfNextGeneration_Boltzmann(offspringsFromPreviousGeneration);

        survivors = getSurvivors(newGeneration);
        if (activateLearning) {
            if (!survivors.isEmpty()){
                survivors_learn(survivors, parentGraph, amountOfLearning, SIZE_OF_DEFENSIVE_ALLIANCE);
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

    static void survivors_learn(List<Genome> survivors, OneGenome parentGraph, int amountOfLearning, int SIZE_OF_DEFENSIVE_ALLIANCE) {
        Thread[] threads = new Thread[survivors.size()];

        for (int i = 0; i < survivors.size(); i++) {
            final int index = i;
            threads[index] = new Thread(() -> {
                Genome.learn(survivors.get(index), parentGraph, amountOfLearning, SIZE_OF_DEFENSIVE_ALLIANCE);
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

    static void survivors_learn(List<Genome> survivors, OneGenome parentGraph, int amountOfLearning, int learnerAmount, boolean randomizeLearners, int SIZE_OF_DEFENSIVE_ALLIANCE) {
        /**
         * This method is used to let the surviviors of the last generation learn.
         * @learnerAmount is the amount of survivors that are going to learn.
         * @randomizeLearners true results in the learners being picked at random.
         * @randomizeLearners false results in the best survivors learning. since the list should be sorted.
         * @learnerAmount can be greater than the amount of survivors, in this case all survivors will learn.
         */
        Thread[] threads = new Thread[learnerAmount];

        if (randomizeLearners) {
            Collections.shuffle(survivors);
        }
        for (int i = 0; (i < survivors.size()) && (i < learnerAmount); i++) {
            final int index = i;
            threads[index] = new Thread(() -> {
                Genome.learn(survivors.get(index), parentGraph, amountOfLearning, SIZE_OF_DEFENSIVE_ALLIANCE);
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

    static List<Genome> generate_nextChildrenListThreaded(float mutationrate, float probability, int newChildsPerParents, List<Genome> nextGenParents, int mutation_identifier, int recombination_identifier, int SIZE_OF_DEFENSIVE_ALLIANCE) {

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
                        newChild.updateChildDegrees_crossover();
                    }
                    if (recombination_identifier == 1) {
                        newChild.updateChildDegrees_intersectionWithProbability();
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
                    int fitnessValue = FitnessFunctions.calculateFitnessMIN(newChild, parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE);
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



    static List<Genome> generate_nextChildrenList(float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents, int mutation_identifier, int recombination_identifier, int SIZE_OF_DEFENSIVE_ALLIANCE) {

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
                    newChild.updateChildDegrees_crossover();
                }
                if (recombination_identifier == 1) {
                    newChild.updateChildDegrees_intersectionWithProbability();
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
                newChild.setFitness(FitnessFunctions.calculateFitnessMIN(newChild, parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE));

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
    static Population remove_duplicates(Population population, int numberOFNodes, float existenceRate, OneGenome parentGraph, int SIZE_OF_DEFENSIVE_ALLIANCE) {

        //remove isolated nodes
        //Population temp = remove_isolated_nodes(population, parentGraph);

        Population temp = population;

        boolean found = false;
        int counter = 0;
        for (int i = 0; i < temp.population.length - 1; i++) {
            for (int j = i + 1; j < temp.population.length; j++) {
                int difference = Genome.difference(temp.population[i], temp.population[j]);
                if (difference == 0) {
                    //remove the duplicate
                    temp.population[i] = new Genome(numberOFNodes, existenceRate);

                    //calculate degrees
                    Genome.calculateDegrees_withNeighbourhood(temp.population[i]);
                    //calculate size
                    temp.population[i].calculateSize();
                    //calculate fitness
                    temp.population[i].setFitness(FitnessFunctions.calculateFitnessMIN(temp.population[i], parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE));


                    found = true;
                    counter++;
                    break;
                }
            }
        }
        if (found) {
            System.out.println("\u001B[35m" + "Duplicates found and removed: " + counter + "\u001B[0m");
        } else {
            System.out.println("No duplicates found");
        }
        return temp;
    }

    /*
    static Population remove_duplicates_Threaded(Population population, int numberOFNodes, float existenceRate, OneGenome parentGraph) {

        Population temp = population;

        // Thread-safe map to store updated genomes
        ConcurrentHashMap<Integer, Genome> updatedPopulation = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(temp.population.length);


        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < temp.population.length; i++) {
            final int index = i;
            executor.submit(() -> {
                //remove isolated nodes
                temp.population[index] = Genome.removeIsolatedNodes(temp.population[index], parentGraph);

                //loop wont run if i = population.population.length
                for (int j = index + 1; j < temp.population.length; j++) {
                    int difference = Genome.difference(temp.population[index], temp.population[j]);
                    if (difference == 0) {
                        //create complementary genome
                        Genome newGenome = new Genome(temp.population[index].getGenome(), true);

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
            temp.population[index] = entry.getValue();
        }
        if (found.get()) {
            System.out.println("\u001B[35m" + "Duplicates found and removed: " + counter.get() + "\u001B[0m");
        }
        return population;
    }*/

    static Population remove_duplicates_Threaded(Population population, int numberOFNodes, float existenceRate, OneGenome parentGraph,int SIZE_OF_DEFENSIVE_ALLIANCE) {
        Population temp = population;
        boolean[] toReplace = new boolean[temp.population.length];
        final AtomicInteger counter = new AtomicInteger(0);

        // Parallel duplicate detection: Identify genomes to replace
        IntStream.range(0, temp.population.length - 1).parallel().forEach(i -> {
            int finalI = i;
            boolean isDuplicate = IntStream.range(finalI + 1, temp.population.length)
                    .parallel()
                    .anyMatch(j -> Genome.difference(temp.population[finalI], temp.population[j]) == 0);

            if (isDuplicate) {
                toReplace[finalI] = true;
                counter.incrementAndGet();
            }
        });

        // Parallel replacement: Generate new genomes for marked positions
        IntStream.range(0, temp.population.length).parallel().forEach(i -> {
            if (toReplace[i]) {
                Genome newGenome;
                //create a new genome 
                if (parentGraph.Ids_toFilter.isEmpty()) {
                    newGenome = new Genome(numberOFNodes, existenceRate);
                } else {
                    newGenome = new Genome(numberOFNodes, existenceRate, parentGraph.Ids_toFilter);
                }
                Genome.calculateDegrees_withNeighbourhood(newGenome);
                newGenome.setFitness(FitnessFunctions.calculateFitnessMIN(newGenome, parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE));
                temp.population[i] = newGenome;
            }
        });

        // Print results
        int dupCount = counter.get();
        if (dupCount > 0) {
            System.out.println("\u001B[35m" + "Duplicates found and removed: " + counter.get() + "\u001B[0m");
        } else {
            System.out.println("No duplicates found");
        }
        return temp;
    }

    //test List<Genome> on identical genomes, list has to be sorted by fitness reversed
    static void deleteDuplicates(Genome[] genomes, OneGenome parentGraph, int SIZE_OF_DEFENSIVE_ALLIANCE, float NODE_EXISTENCE_PROBABILITY) {
        // Use LinkedHashSet to track unique genomes BY CONTENT (not reference)
        Map<String, Genome> uniqueGenomes = new LinkedHashMap<>();
    
        for (Genome genome : genomes) {
            // Create unique key from genome content
            String key = genome.length + "#" + Arrays.toString(genome.getGenome());
        
            // Keep first occurrence (highest fitness due to sort)
            uniqueGenomes.putIfAbsent(key, genome);
        }
    
        // Rebuild list with unique genomes in original order
        Arrays.fill(genomes, null);
        // Fill the array with unique genomes
        int index = 0;
        for (Genome uniqueGenome : uniqueGenomes.values()) {
            genomes[index++] = uniqueGenome;
        }

        // create new genomes for null entries
        for (int i = index; i < genomes.length; i++) {
            //create a new genome 
            if (parentGraph.Ids_toFilter.isEmpty()) {
                genomes[i] = new Genome(parentGraph.length, NODE_EXISTENCE_PROBABILITY);
            } else {
                genomes[i] = new Genome(parentGraph.length, NODE_EXISTENCE_PROBABILITY, parentGraph.Ids_toFilter);
            }
            //calculate degrees
            Genome.calculateDegrees_withNeighbourhood(genomes[i]);
            //calculate fitness
            genomes[i].setFitness(FitnessFunctions.calculateFitnessMIN(genomes[i], parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE));
        }

        //document the amount of duplicates
        int dupCount = genomes.length - uniqueGenomes.size();
        if (dupCount > 0) {
            System.out.println("\u001B[35m" + "Duplicates found and removed: " + dupCount + "\u001B[0m");
        } else {
            System.out.println("No duplicates found");
        }
    }

    //calculate total difference in population
    static int calculateTotalDifference(Population population) {
        int totalDifference = 0;
        for (int i = 0; i < population.population.length - 1; i++) {
            for (int j = i + 1; j < population.population.length; j++) {
                totalDifference += Genome.difference(population.population[i], population.population[j]);
            }
        }
        return totalDifference;
    }


    static void printStats(Population population) {
        System.out.println("Generation: " + population.generation);
        System.out.println("Population fitness: " + population.population_fitness);
        System.out.println("Mean fitness: " + population.mean_fitness);
        System.out.println("Mean size: " + population.mean_size);
        System.out.println("Population size: " + population.population.length+"\n");

        System.out.println("First Best Fitness in Population: " + population.population[0].getFitness() + " Size: " + population.population[0].getSize());
        System.out.println("Second Best Fitness in Population: " + population.population[1].getFitness() + " Size: " + population.population[1].getSize());
        System.out.println("Worst Fitness in Population: " + population.population[population.population.length-1].getFitness() + " Size: " + population.population[population.population.length-1].getSize());
        System.out.println("Difference between best and second best genome: " + Genome.difference(population.population[0], population.population[1]));
        System.out.println("Difference between best and worst best genome: " + Genome.difference(population.population[0], population.population[population.population.length-1]));
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------"+'\n');

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
        System.out.println("Difference between best and worst best genome: " + Genome.difference(population[0], population[population.length-1]));
        System.out.println("Genetic Difference between best Genomes of current and past generation: " + Genome.difference(population[0], bestGenomeFromLastGeneration));
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------"+'\n');
    }


    // CSV logging
    private static final String CSV_HEADER = "generation,population_fitness,mean_fitness,mean_size," +
            "survivors,offspring,best_fitness,size_of_fittest," +
            "second_fitness,second_size,worst_fitness,worst_size," +
            "best_second_diff,best_current_vs_last_diff";

    public static void initCSV(String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(CSV_HEADER);
            writer.newLine();
        }
    }

    // Call this after each generation's printStats()
    public static void logGeneration(String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            String line = String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d",
                    generation,
                    population_fitness,
                    mean_fitness,
                    mean_size,
                    survivors.size(),
                    population.length - survivors.size(),
                    population[0].getFitness(),
                    population[0].getSize(),
                    population[1].getFitness(),
                    population[1].getSize(),
                    population[population.length-1].getFitness(),
                    population[population.length-1].getSize(),
                    Genome.difference(population[0], population[1]),
                    Genome.difference(population[0], bestGenomeFromLastGeneration)
            );
            writer.write(line);
            writer.newLine();
        }
    }
}