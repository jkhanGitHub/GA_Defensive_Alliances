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

    static Dictionary<Integer,String> mutationIdentifiers = new Hashtable<Integer,String>();
    static {
        mutationIdentifiers.put(0,"Mutation");
        mutationIdentifiers.put(1, "Mutation of vertices with high degree");
        mutationIdentifiers.put(2, "try to add Mutation of vertices with high degree");
        mutationIdentifiers.put(3, "Remove harmful nodes");
    }

    static Dictionary<Integer,String> recombinationIdentifiers = new Hashtable<Integer,String>();
    static {
        recombinationIdentifiers.put(0,"OnePointCrossoverThreaded");
        recombinationIdentifiers.put(1, "ProababilityIntersectionThreaded");
        recombinationIdentifiers.put(2, "OnePointCrossover");
        recombinationIdentifiers.put(3, "ProababilityIntersection");
    }

    public  Genome[] getPopulation() {
        return population;
    }

    public int getGeneration() {
        return generation;
    }

    public static void updateGeneration() {
        generation = generation+1;
    }

    static int generation = 0;

    public long getPopulation_fitness() {
        return population_fitness;
    }

    public void setPopulation_fitness(int population_fitness) {
        this.population_fitness = population_fitness;
    }

    long population_fitness;

    int population_fitness_positive;

    public int getMean_fitness() {
        return mean_fitness;
    }

    public void setMean_fitness(int mean_fitness) {
        this.mean_fitness = mean_fitness;
    }

    int mean_fitness;

    public void setMean_size(int mean_size) {
        this.mean_size = mean_size;
    }

    int mean_size;

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
    Population(int sizeOfPopulation, int numberOFNodes, float existenceRate, int[][] graph, OneGenome parentGraph){
        population = new Genome[sizeOfPopulation];
        Thread[] threads = new Thread[sizeOfPopulation];
        //generation number will be updated in Selection in order to reuse the sorted population
        //Generates Genomes

        //comment out and change loop to start at 0 if parent graph should not be in array makes sense when using OnepointCrossover
        population[0] = parentGraph;
        for (int i = 0; i < population.length; i++) {
            final int finalI = i;
            threads[finalI] = new Thread(()-> population[finalI] = new Genome(numberOFNodes,existenceRate,graph));
            threads[finalI].start();
        }
        for (Thread t:
                threads) {
            if (t == null) continue; //happens if parent graph is in array
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //best fitness to worst
    public void sort_Population_by_fitness_and_size_reversed(){
        Arrays.sort(population, Comparator.comparingInt(Genome::getFitness).reversed());
    }


    static Population update_Population_OnePointCrossover(Population population, int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents, int amountOfMutations, int mutation_identifier){

        System.out.println("Recombination Method: OnePointCrossover");

        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list

        //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
        for (int i = 0,j = 1; j < nextGenParents.size(); i=i+2,j= j+2) {
            int[][] geneticCodesOfChildrens = Recombinations.onePointCrossover(nextGenParents.get(i).getGenome(),nextGenParents.get(j).getGenome(),newChildsPerParents);
            for (int k = 0; k < geneticCodesOfChildrens.length; k++) {
                Genome newChild = new Genome(numberOfNodes,geneticCodesOfChildrens[k],graph);

                //Mutation
                int[] mutated = Mutations.mutation(mutationrate,newChild);
                newChild.setGenome(mutated);

                //calculate degrees
                Genome.calculateDegrees(graph,newChild);

                //calculate fitness
                newChild.setFitness(FitnessFunctions.calculateFitnessMIN(newChild,parentGraph));
                nextGenChildren.add(newChild);

                //calculate size
                newChild.calculateSize();
            }
        }

        //Elites of the previous gen stay in the next generation
        //Number of Elites = POPULATION_SIZE - (POPULATION_SIZE/numberOfContestantsPerRound)
        return update_Population(population,nextGenChildren);
    }

    static Population update_Population_OnePointCrossover_Threaded(Population population, int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents, int amountOfMutations, int mutation_identifier){

        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list

        //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
        for (int i = 0, j = 1; j < nextGenParents.size(); i = i + 2, j = j + 2) {
            int[][] geneticCodesOfChildrens = Recombinations.onePointCrossover(nextGenParents.get(i).getGenome(),nextGenParents.get(j).getGenome(),newChildsPerParents);
            Thread[] threads = new Thread[newChildsPerParents];
            for (int k = 0; k < geneticCodesOfChildrens.length; k++) {
                final int finalI = k;
                threads[finalI] = new Thread(() -> {
                    Genome newChild = new Genome(numberOfNodes, geneticCodesOfChildrens[finalI], graph);

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
                        case 2:
                            //Mutation of vertices with high degree
                            newChild = Mutations.test_high_degree_vertices_mutation(newChild,amountOfMutations,parentGraph);
                            break;
                        case 3:
                            //remove harmful node
                            Genome.calculateDegrees(graph,newChild);
                            newChild = Mutations.remove_many_harmful_Nodes(newChild,parentGraph,amountOfMutations);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + mutation_identifier);
                    }

                    //calculate degrees
                    Genome.calculateDegrees(graph, newChild);

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

        return update_Population(population, nextGenChildren);
    }



    static Population update_Population_ProababilityIntersection(Population population, int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents) {

        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list

        //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
        for (int i = 0,j = 1; j < nextGenParents.size(); i=i+2,j= j+2) {
            int[][] geneticCodesOfChildrens = Recombinations.intersection_with_proabability(nextGenParents.get(i).getGenome(),nextGenParents.get(j).getGenome(),proabibility,newChildsPerParents);
            for (int k = 0; k < geneticCodesOfChildrens.length; k++) {
                Genome newChild = new Genome(numberOfNodes,geneticCodesOfChildrens[k],graph);

                //Mutation
                int[] mutated = Mutations.mutation(mutationrate,newChild);
                newChild.setGenome(mutated);

                //calculate degrees
                Genome.calculateDegrees(graph,newChild);

                //calculate fitness
                newChild.setFitness(FitnessFunctions.calculateFitnessMIN(newChild,parentGraph));
                nextGenChildren.add(newChild);

                //calculate size
                newChild.calculateSize();
            }
        }

        //Elites of the previous gen stay in the next generation
        //Number of Elites = POPULATION_SIZE - (POPULATION_SIZE/numberOfContestantsPerRound)
        return update_Population(population,nextGenChildren);
    }


    static Population update_Population_ProababilityIntersection_Threaded(Population population, int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents, int amountOfMutations, int mutation_identifier) {

        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list

        //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
        for (int i = 0, j = 1; j < nextGenParents.size(); i = i + 2, j = j + 2) {
            int[][] geneticCodesOfChildrens = Recombinations.intersection_with_proabability(nextGenParents.get(i).getGenome(), nextGenParents.get(j).getGenome(), proabibility, newChildsPerParents);
            Thread[] threads = new Thread[newChildsPerParents];
            for (int k = 0; k < geneticCodesOfChildrens.length; k++) {
                final int finalI = k;
                threads[finalI] = new Thread(() -> {
                    Genome newChild = new Genome(numberOfNodes, geneticCodesOfChildrens[finalI], graph);

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
                        case 2:
                            //Mutation of vertices with high degree
                            newChild = Mutations.test_high_degree_vertices_mutation(newChild,amountOfMutations,parentGraph);
                            break;
                        case 3:
                            //remove harmful node
                            Genome.calculateDegrees(graph,newChild);
                            newChild = Mutations.remove_many_harmful_Nodes(newChild,parentGraph,amountOfMutations);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + mutation_identifier);
                    }

                    //calculate degrees
                    Genome.calculateDegrees(graph, newChild);

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

        return update_Population(population, nextGenChildren);
    }

    // add new cases when implementing new REcombination methods
    static Population update_Population_Recombination_Identifier(Population population, int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents, int recombination_identifier, int amountOfMutations, int mutation_identifier) {

        System.out.println("Recombination Method: " + recombinationIdentifiers.get(recombination_identifier));

        switch (recombination_identifier) {
            case 0:
                return update_Population_OnePointCrossover_Threaded(population, graph, numberOfNodes, parentGraph, mutationrate, proabibility, newChildsPerParents, nextGenParents, amountOfMutations, mutation_identifier);
            case 1:
                return update_Population_ProababilityIntersection_Threaded(population, graph, numberOfNodes, parentGraph, mutationrate, proabibility, newChildsPerParents, nextGenParents, amountOfMutations, mutation_identifier);
            case 2:
                return update_Population_OnePointCrossover(population, graph, numberOfNodes, parentGraph, mutationrate, proabibility, newChildsPerParents, nextGenParents, amountOfMutations, mutation_identifier);
            case 3:
                return update_Population_ProababilityIntersection(population, graph, numberOfNodes, parentGraph, mutationrate, proabibility, newChildsPerParents, nextGenParents);
            default:
                throw new IllegalStateException("Unexpected value: " + recombination_identifier);
        }
    }

    static Population mutate_Population(Population population, int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, int mutation_identifier, int amountOfMutations, int fillerForInterchangeablity) {
        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list

        System.out.println("Mutation: " + mutationIdentifiers.get(mutation_identifier));
        System.out.println("Maximum amountOfGenomes to be mutated: " + population.population.length);

        int count = 0;
        Random random = new Random();

        Thread[] threads = new Thread[population.population.length];

        while(count < population.population.length) {
            threads[count] = new Thread(()->{
                //randomly select a genome to mutate
                final int i = random.nextInt(population.population.length);
                Genome newChild = new Genome(population.population[i]);

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
                    case 2:
                        //Mutation of vertices with high degree
                        newChild = Mutations.test_high_degree_vertices_mutation(newChild,amountOfMutations,parentGraph);
                        break;
                    case 3:
                        //remove harmful node
                        Genome.calculateDegrees(graph,newChild);
                        newChild = Mutations.remove_many_harmful_Nodes(newChild,parentGraph,amountOfMutations);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + mutation_identifier);
                }
                //calculate degrees
                Genome.calculateDegrees(graph,newChild);
                //calculate fitness
                newChild.setFitness(FitnessFunctions.calculateFitnessMIN(newChild,parentGraph));
                //calculate size
                newChild.calculateSize();

                nextGenChildren.add(newChild);
            });
            threads[count].start();
            count++;
        }
        for (Thread t :
                threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return update_Population_without_GenerationIncrease(population,nextGenChildren);
    }

    static Population mutate_Population_fixedAmount_of_RandomlyChoosen(Population population, int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, int mutation_identifier, int amountOfMutations, int amountOfGenomes){
        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list

        System.out.println("Mutation: " + mutationIdentifiers.get(mutation_identifier));
        System.out.println("Maximum amountOfGenomes to be mutated: " + amountOfGenomes);

        int count = 0;
        Random random = new Random();

        Thread[] threads = new Thread[amountOfGenomes];

        while(count < amountOfGenomes) {
            threads[count] = new Thread(()->{
                //randomly select a genome to mutate
                final int i = random.nextInt(population.population.length);
                Genome newChild = new Genome(population.population[i]);

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
                    case 2:
                        //Mutation of vertices with high degree
                        newChild = Mutations.test_high_degree_vertices_mutation(newChild,amountOfMutations,parentGraph);
                        break;
                    case 3:
                        //remove harmful node
                        Genome.calculateDegrees(graph,newChild);
                        newChild = Mutations.remove_many_harmful_Nodes(newChild,parentGraph,amountOfMutations);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + mutation_identifier);
                }
                //calculate degrees
                Genome.calculateDegrees(graph,newChild);
                //calculate fitness
                newChild.setFitness(FitnessFunctions.calculateFitnessMIN(newChild,parentGraph));
                //calculate size
                newChild.calculateSize();

                nextGenChildren.add(newChild);
            });
            threads[count].start();
            count++;
        }
        for (Thread t :
                threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return update_Population_without_GenerationIncrease(population,nextGenChildren);
    }


    static Population mutate_Population_fixedAmount_of_Best(Population population, int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, int mutation_identifier, int amountOfMutations, int amountOfGenomes){
        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list

        System.out.println("Mutation: " + mutationIdentifiers.get(mutation_identifier));
        System.out.println("Maximum amountOfGenomes to be mutated: " + amountOfGenomes);

        Thread[] threads = new Thread[amountOfGenomes];

        for(int j =0; j < amountOfGenomes; j++) {
            final int i = j;
            threads[i] = new Thread(()->{
                //randomly select a genome to mutate
                Genome newChild = new Genome(population.population[i]);

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
                    case 2:
                        //Mutation of vertices with high degree
                        newChild = Mutations.test_high_degree_vertices_mutation(newChild,amountOfMutations,parentGraph);
                        break;
                    case 3:
                        //remove harmful node
                        Genome.calculateDegrees(graph,newChild);
                        newChild = Mutations.remove_many_harmful_Nodes(newChild,parentGraph,amountOfMutations);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + mutation_identifier);
                }
                //calculate degrees
                Genome.calculateDegrees(graph,newChild);
                //calculate fitness
                newChild.setFitness(FitnessFunctions.calculateFitnessMIN(newChild,parentGraph));
                //calculate size
                newChild.calculateSize();

                nextGenChildren.add(newChild);
            });
            threads[i].start();
        }
        for (Thread t :
                threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return update_Population_without_GenerationIncrease(population,nextGenChildren);
    }

    static Population mutate_Population_fixedAmount_of_Worst(Population population, int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, int mutation_identifier, int amountOfMutations, int amountOfGenomes){
        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list

        System.out.println("Mutation: " + mutationIdentifiers.get(mutation_identifier));
        System.out.println("Maximum amountOfGenomes to be mutated: " + amountOfGenomes);

        Thread[] threads = new Thread[amountOfGenomes];
        int n = population.population.length-1;

        for(int j =0; j < amountOfGenomes; j++) {
            final int i = j;
            threads[i] = new Thread(()->{
                //randomly select a genome to mutate
                Genome newChild = new Genome(population.population[n-i]);

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
                    case 2:
                        //Mutation of vertices with high degree
                        newChild = Mutations.test_high_degree_vertices_mutation(newChild,amountOfMutations,parentGraph);
                        break;
                    case 3:
                        //remove harmful node
                        Genome.calculateDegrees(graph,newChild);
                        newChild = Mutations.remove_many_harmful_Nodes(newChild,parentGraph,amountOfMutations);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + mutation_identifier);
                }
                //calculate degrees
                Genome.calculateDegrees(graph,newChild);
                //calculate fitness
                newChild.setFitness(FitnessFunctions.calculateFitnessMIN(newChild,parentGraph));
                //calculate size
                newChild.calculateSize();

                nextGenChildren.add(newChild);
            });
            threads[i].start();
        }
        for (Thread t :
                threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return update_Population_without_GenerationIncrease(population,nextGenChildren);
    }

    static Population mutate_Population_fixedAmount_of_Best_and_Worst(Population population, int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, int mutation_identifier, int amountOfMutations, int amountOfGenomes){
        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list

        System.out.println("Mutation: " + mutationIdentifiers.get(mutation_identifier));
        System.out.println("Maximum amountOfGenomes to be mutated: " + amountOfGenomes);

        Thread[] threads = new Thread[amountOfGenomes/2];
        int n = population.population.length;

        for(int j =0, k=1; j < amountOfGenomes/2; j++,k++) {
            final int i = j;
            final int x = k;
            threads[i] = new Thread(()->{
                //randomly select a genome to mutate
                Genome newChild = new Genome(population.population[i]);
                Genome worseChild = new Genome(population.population[n-x]);

                //Mutation
                switch (mutation_identifier) {
                    case 0:
                        //Mutation
                        int[] mutated = Mutations.mutation(mutationrate,newChild);
                        int[] mutated_worse = Mutations.mutation(mutationrate,worseChild);
                        newChild.setGenome(mutated);
                        worseChild.setGenome(mutated_worse);
                        break;
                    case 1:
                        //Mutation of vertices with high degree
                        int[] mutated_high_degree = Mutations.mutation_of_vertices_with_high_degree(mutationrate,newChild);
                        int[] mutated_high_degree_worse = Mutations.mutation_of_vertices_with_high_degree(mutationrate,worseChild);
                        newChild.setGenome(mutated_high_degree);
                        worseChild.setGenome(mutated_high_degree_worse);
                        break;
                    case 2:
                        //Mutation of vertices with high degree
                        newChild = Mutations.test_high_degree_vertices_mutation(newChild,amountOfMutations,parentGraph);
                        worseChild = Mutations.test_high_degree_vertices_mutation(worseChild,amountOfMutations,parentGraph);
                        break;
                    case 3:
                        //remove harmful node
                        Genome.calculateDegrees(graph,newChild);
                        Genome.calculateDegrees(graph,worseChild);
                        newChild = Mutations.remove_many_harmful_Nodes(newChild,parentGraph,amountOfMutations);
                        worseChild = Mutations.remove_many_harmful_Nodes(worseChild,parentGraph,amountOfMutations);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + mutation_identifier);
                }
                //calculate degrees
                Genome.calculateDegrees(graph,newChild);
                Genome.calculateDegrees(graph,worseChild);
                //calculate fitness
                newChild.setFitness(FitnessFunctions.calculateFitnessMIN(newChild,parentGraph));
                worseChild.setFitness(FitnessFunctions.calculateFitnessMIN(worseChild,parentGraph));
                //calculate size
                newChild.calculateSize();
                worseChild.calculateSize();

                nextGenChildren.add(newChild);
                nextGenChildren.add(worseChild);
            });
            threads[i].start();
        }
        for (Thread t :
                threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return update_Population_without_GenerationIncrease(population,nextGenChildren);
    }

    static Population mutate_Population_RandomAmount_of_RandomlyChoosen(Population population, int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, int mutation_identifier, int amountOfMutations, int upperBound){
        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list

        System.out.println("Mutation: " + mutationIdentifiers.get(mutation_identifier));


        int count = 0;
        Random random = new Random();
        int amountOfGenomes = random.nextInt(upperBound); //random number between 0 and population size
        System.out.println("Maximum amountOfGenomes to be mutated: " + amountOfGenomes);

        Thread[] threads = new Thread[amountOfGenomes];

        while(count < amountOfGenomes) {
            threads[count] = new Thread(()->{
                //randomly select a genome to mutate
                int i = random.nextInt(population.population.length);
                Genome newChild = new Genome(population.population[i]);

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
                    case 2:
                        //Mutation of vertices with high degree
                        newChild = Mutations.test_high_degree_vertices_mutation(newChild,amountOfMutations,parentGraph);
                        break;
                    case 3:
                        //remove harmful node
                        Genome.calculateDegrees(graph,newChild);
                        newChild = Mutations.remove_many_harmful_Nodes(newChild,parentGraph,amountOfMutations);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + mutation_identifier);
                }
                //calculate degrees
                Genome.calculateDegrees(graph,newChild);
                //calculate fitness
                newChild.setFitness(FitnessFunctions.calculateFitnessMIN(newChild,parentGraph));
                //calculate size
                newChild.calculateSize();

                nextGenChildren.add(newChild);
            });
            threads[count].start();
            count++;
        }
        for (Thread t :
                threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return update_Population_without_GenerationIncrease(population,nextGenChildren);
    }

    static Population update_Population(Population population, List<Genome> newGenomes){
        Population p = population;
        int counter=0;

        int n = population.population.length-1;

        //overwrites the worst entries by the amount of newGenomes.size()
        for (Genome genome : newGenomes) {
            if (counter >= n) break; // Prevents ArrayIndexOutOfBoundsException
            p.population[n-counter] = genome;
            counter++;
        }
        Population.updateGeneration();
        return p;
    }

    static Population update_Population_without_GenerationIncrease(Population population, List<Genome> newGenomes){
        Population p = population;
        int counter=0;

        int n = population.population.length-1;

        //overwrites the worst entries by the amount of newGenomes.size()
        for (Genome genome : newGenomes) {
            if (counter >= n) break; // Prevents ArrayIndexOutOfBoundsException
            p.population[n-counter] = genome;
            counter++;
        }
        return p;
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
                    Genome.calculateDegrees(graph,temp.population[i]);
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

        //remove isolated nodes
        Population temp = remove_isolated_nodes(population, parentGraph);

        // Thread-safe map to store updated genomes
        ConcurrentHashMap<Integer, Genome> updatedPopulation = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < temp.population.length-1; i++) {
            final int index = i;
            executor.submit(() -> {
                for (int j = index+1; j < temp.population.length; j++) {
                    int difference = Genome.difference(temp.population[index],temp.population[j]);
                    if (difference==0){
                        //complementary genome
                        Genome newGenome = new Genome(temp.population[index].getGenome());
                        Genome.calculateDegrees(graph, newGenome);
                        newGenome.setFitness(FitnessFunctions.calculateFitnessMIN(newGenome, parentGraph));
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
            temp.population[index] = entry.getValue();
        }
        if (found.get()) {
            System.out.println("\u001B[35m"+"Duplicates found and removed: " + counter.get()+"\u001B[0m");
        }
        return temp;
    }
}