import java.util.*;

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
        recombinationIdentifiers.put(0,"OnePointCrossover");
        recombinationIdentifiers.put(1, "ProababilityIntersection");
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

    int mean_fitness_positive;

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


    static Population update_Population_OnePointCrossover(Population population, int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents){
        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list

        //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
        for (int i = 0,j = 1; j < nextGenParents.size(); i=i+2,j= j+2) {
            int[][] geneticCodesOfChildrens = Recombinations.onePointCrossover(nextGenParents.get(i).getGenome(),nextGenParents.get(j).getGenome());

            Genome ba = new Genome(numberOfNodes,geneticCodesOfChildrens[1],graph);
            Genome ab = new Genome(numberOfNodes,geneticCodesOfChildrens[0],graph);

            //Mutation
            int[] mutated_ba = Mutations.mutation(mutationrate,ba);
            int[] mutated_ab = Mutations.mutation(mutationrate,ab);
            ba.setGenome(mutated_ba);
            ab.setGenome(mutated_ab);

            //calculate degrees
            Genome.calculateDegrees(graph,ba);
            Genome.calculateDegrees(graph,ab);

            //calculate fitness
            ab.setFitness(FitnessFunctions.calculateFitnessMIN(ab,parentGraph));
            ba.setFitness(FitnessFunctions.calculateFitnessMIN(ba,parentGraph));


            //calculate size
            ab.calculateSize();
            ba.calculateSize();

            nextGenChildren.add(ab);
            nextGenChildren.add(ba);
        }

        return update_Population(population, nextGenChildren);
    }

    static Population update_Population_OnePointCrossover_Threaded(Population population, int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents, int amountOfMutations, int mutation_identifier){

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

        System.out.println("Recombination Method: ProababilityIntersection");

        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list

        //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
        for (int i = 0, j = 1; j < nextGenParents.size(); i = i + 2, j = j + 2) {
        int[][] geneticCodesOfChildrens = Recombinations.intersection_with_proabability(nextGenParents.get(i).getGenome(), nextGenParents.get(j).getGenome(), proabibility, newChildsPerParents);
        Thread[] threads = new Thread[newChildsPerParents];
        for (int k = 0; k < geneticCodesOfChildrens.length; k++) {
            final int finalI = k;
            threads[finalI] = new Thread(() -> {
                Genome newChild = new Genome(numberOfNodes, geneticCodesOfChildrens[finalI], graph);

                //easiest way to mutate the genome
                int[] mutation = Mutations.mutation(mutationrate,newChild);
                newChild.setGenome(mutation);

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

        switch (recombination_identifier) {
            case 0:
                return update_Population_OnePointCrossover_Threaded(population, graph, numberOfNodes, parentGraph, mutationrate, proabibility, newChildsPerParents, nextGenParents, amountOfMutations, mutation_identifier);
            case 1:
                return update_Population_ProababilityIntersection_Threaded(population, graph, numberOfNodes, parentGraph, mutationrate, proabibility, newChildsPerParents, nextGenParents, amountOfMutations, mutation_identifier);
            default:
                throw new IllegalStateException("Unexpected value: " + recombination_identifier);
        }
}

static Population mutate_Population(Population population, int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, int mutation_identifier, int amountOfMutations, int fillerForInterchangeablity) {
        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list

        System.out.println("Mutation: mutation" + mutationIdentifiers.get(mutation_identifier) + '\t' +amountOfMutations);


    int count = 0;
    Random random = new Random();

    Thread[] threads = new Thread[population.population.length];

    while(count < population.population.length) {
        threads[count] = new Thread(()->{
            //randomly select a genome to mutate
            int i = random.nextInt(population.population.length);
            Genome newChild = new Genome(numberOfNodes,population.population[i].getGenome(),graph);

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
                    newChild = Mutations.test_high_degree_vertices_mutation(population.population[i],amountOfMutations,parentGraph);
                    break;
                case 3:
                    //remove harmful node
                    Genome.calculateDegrees(graph,newChild);
                    newChild = Mutations.remove_many_harmful_Nodes(population.population[i],parentGraph,amountOfMutations);
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

        System.out.println("Mutation: mutation" + mutationIdentifiers.get(mutation_identifier) + '\t' +amountOfMutations);


        int count = 0;
        Random random = new Random();

        Thread[] threads = new Thread[amountOfGenomes];

        while(count < amountOfGenomes) {
            threads[count] = new Thread(()->{
                //randomly select a genome to mutate
                int i = random.nextInt(population.population.length);
                Genome newChild = new Genome(numberOfNodes,population.population[i].getGenome(),graph);

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
                        newChild = Mutations.test_high_degree_vertices_mutation(population.population[i],amountOfMutations,parentGraph);
                        break;
                    case 3:
                        //remove harmful node
                        Genome.calculateDegrees(graph,newChild);
                        newChild = Mutations.remove_many_harmful_Nodes(population.population[i],parentGraph,amountOfMutations);
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

        System.out.println("Mutation: mutation" + mutationIdentifiers.get(mutation_identifier) + '\t' +amountOfMutations);


        Thread[] threads = new Thread[amountOfGenomes];

        for(int j =0; j < amountOfGenomes; j++) {
            final int i = j;
            threads[i] = new Thread(()->{
                //randomly select a genome to mutate
                Genome newChild = new Genome(numberOfNodes,population.population[i].getGenome(),graph);

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
                        newChild = Mutations.test_high_degree_vertices_mutation(population.population[i],amountOfMutations,parentGraph);
                        break;
                    case 3:
                        //remove harmful node
                        Genome.calculateDegrees(graph,newChild);
                        newChild = Mutations.remove_many_harmful_Nodes(population.population[i],parentGraph,amountOfMutations);
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

        System.out.println("Mutation: mutation" + mutationIdentifiers.get(mutation_identifier) + '\t' +amountOfMutations);


        Thread[] threads = new Thread[amountOfGenomes];
        int n = population.population.length-1;

        for(int j =0; j < amountOfGenomes; j++) {
            final int i = j;
            threads[i] = new Thread(()->{
                //randomly select a genome to mutate
                Genome newChild = new Genome(numberOfNodes,population.population[n-i].getGenome(),graph);

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
                        newChild = Mutations.test_high_degree_vertices_mutation(population.population[n-i],amountOfMutations,parentGraph);
                        break;
                    case 3:
                        //remove harmful node
                        Genome.calculateDegrees(graph,newChild);
                        newChild = Mutations.remove_many_harmful_Nodes(population.population[n-i],parentGraph,amountOfMutations);
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

        System.out.println("Mutation: mutation" + mutationIdentifiers.get(mutation_identifier) + '\t' +amountOfMutations);


        Thread[] threads = new Thread[amountOfGenomes/2];
        int n = population.population.length;

        for(int j =0, k=1; j < amountOfGenomes/2; j++,k++) {
            final int i = j;
            final int x = k;
            threads[i] = new Thread(()->{
                //randomly select a genome to mutate
                Genome newChild = new Genome(numberOfNodes,population.population[i].getGenome(),graph);
                Genome worseChild = new Genome(numberOfNodes,population.population[n-x].getGenome(),graph);

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
                        newChild = Mutations.test_high_degree_vertices_mutation(population.population[i],amountOfMutations,parentGraph);
                        worseChild = Mutations.test_high_degree_vertices_mutation(population.population[n-x],amountOfMutations,parentGraph);
                        break;
                    case 3:
                        //remove harmful node
                        Genome.calculateDegrees(graph,newChild);
                        Genome.calculateDegrees(graph,worseChild);
                        newChild = Mutations.remove_many_harmful_Nodes(population.population[i],parentGraph,amountOfMutations);
                        worseChild = Mutations.remove_many_harmful_Nodes(population.population[n-x],parentGraph,amountOfMutations);
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

        System.out.println("Mutation: mutation" + mutationIdentifiers.get(mutation_identifier) + '\t' +amountOfMutations);


        int count = 0;
        Random random = new Random();
        int amountOfGenomes = random.nextInt(upperBound); //random number between 0 and population size
        System.out.println("amountOfGenomes to be mutated: " + amountOfGenomes);

        Thread[] threads = new Thread[amountOfGenomes];

        while(count < amountOfGenomes) {
            threads[count] = new Thread(()->{
                //randomly select a genome to mutate
                int i = random.nextInt(population.population.length);
                Genome newChild = new Genome(numberOfNodes,population.population[i].getGenome(),graph);

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
                        newChild = Mutations.test_high_degree_vertices_mutation(population.population[i],amountOfMutations,parentGraph);
                        break;
                    case 3:
                        //remove harmful node
                        Genome.calculateDegrees(graph,newChild);
                        newChild = Mutations.remove_many_harmful_Nodes(population.population[i],parentGraph,amountOfMutations);
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


        //some selectioons Methods can Result in more new Parents, thus more new children than the size of the population allows
        int controlDamage = p.population.length - newGenomes.size();
        if (controlDamage < 0) {
           controlDamage =p.population.length;
        }
        else {
            controlDamage = newGenomes.size();
        }

        //overwrites the old entries by the amount of newGenomes.size()
        for (int i = p.population.length-controlDamage;  counter < newGenomes.size() && counter<p.population.length ; counter++,i++) {
            p.population[i]= newGenomes.get(counter);
        }
        Population.updateGeneration();
        return p;
    }

    static Population update_Population_without_GenerationIncrease(Population population, List<Genome> newGenomes){
        Population p = population;
        int counter=0;

        //some selectioons Methods can Result in more new Parents, thus more new children than the size of the population allows
        int controlDamage = p.population.length - newGenomes.size();
        if (controlDamage < 0) {
            controlDamage =p.population.length;
        }
        else {
            controlDamage = newGenomes.size();
        }

        //overwrites the old entries by the amount of newGenomes.size()
        for (int i = p.population.length-controlDamage;  counter < newGenomes.size() && counter<p.population.length ; counter++,i++) {
            p.population[i]= newGenomes.get(counter);
        }
        return p;
    }

    static Population remove_duplicates(Population population, int numberOFNodes, float existenceRate, int[][] graph, OneGenome parentGraph){

        boolean found = false;
        for (int i = 0; i < population.population.length-1; i++) {
            for (int j = i+1; j < population.population_fitness; j++) {
                int difference = Genome.difference(population.population[i],population.population[j]);
                if (difference==0){
                    //remove the duplicate
                    population.population[i] = new Genome(numberOFNodes,existenceRate,graph);

                    Genome.calculateDegrees(graph,population.population[i]);
                    population.population[i].setFitness(FitnessFunctions.calculateFitnessMIN(population.population[i],parentGraph));
                    population.population[i].calculateSize();
                    found = true;
                }
            }
        }
        if (found) {
            System.out.println("Duplicates found and removed");
        }
        else {
            System.out.println("No duplicates found");
        }
        return population;
    }
}
