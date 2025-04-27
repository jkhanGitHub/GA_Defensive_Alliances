import java.util.*;

//many methods might have unused parameters, these are just there so that the methods can be interchanged in the genetic algorithm
public class Population{
    Genome[] population; //Array is used since it is easy to update and we keep its size static

    public  Genome[] getPopulation() {
        return population;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    int generation = 0;

    public int getPopulation_fitness() {
        return population_fitness;
    }

    public void setPopulation_fitness(int population_fitness) {
        this.population_fitness = population_fitness;
    }

    int population_fitness;

    public int getMean_fitness() {
        return mean_fitness;
    }

    public void setMean_fitness(int mean_fitness) {
        this.mean_fitness = mean_fitness;
    }

    int mean_fitness;

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

        //comment out and change loop to start at 0 if parent graph should not be in array
        population[0] = parentGraph;
        for (int i = 1; i < population.length; i++) {
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
            ab.setFitness(FitnessFunctions.calculateFitness(ab,parentGraph));
            ba.setFitness(FitnessFunctions.calculateFitness(ba,parentGraph));

            nextGenChildren.add(ab);
            nextGenChildren.add(ba);
        }

        return update_Population(population, nextGenChildren);
    }

    static Population update_Population_OnePointCrossover_Threaded(Population population, int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents){

        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list

        //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
        for (int i = 0, j = 1; j < nextGenParents.size(); i = i + 2, j = j + 2) {
            final int parent1Index = i;
            final int parent2Index = j;

            Thread thread = new Thread(() -> {
                int[][] geneticCodesOfChildrens = Recombinations.onePointCrossover(
                        nextGenParents.get(parent1Index).getGenome(),
                        nextGenParents.get(parent2Index).getGenome()
                );

                Genome ba = new Genome(numberOfNodes, geneticCodesOfChildrens[1], graph);
                Genome ab = new Genome(numberOfNodes, geneticCodesOfChildrens[0], graph);

                // Mutation
                //Mutation
                int[] mutated_ba = Mutations.mutation(mutationrate,ba);
                int[] mutated_ab = Mutations.mutation(mutationrate,ab);
                ba.setGenome(mutated_ba);
                ab.setGenome(mutated_ab);

                // Calculate degrees
                Genome.calculateDegrees(graph, ba);
                Genome.calculateDegrees(graph, ab);

                // Calculate fitness
                ab.setFitness(FitnessFunctions.calculateFitness(ab, parentGraph));
                ba.setFitness(FitnessFunctions.calculateFitness(ba, parentGraph));

                // Add to the thread-safe list
                synchronized (nextGenChildren) {
                    nextGenChildren.add(ab);
                    nextGenChildren.add(ba);
                }
            });

            thread.start();

            try {
                thread.join(); // Ensure the thread completes before moving to the next pair
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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
                newChild.setFitness(FitnessFunctions.calculateFitness(newChild,parentGraph));
                nextGenChildren.add(newChild);
            }
        }

        //Elites of the previous gen stay in the next generation
        //Number of Elites = POPULATION_SIZE - (POPULATION_SIZE/numberOfContestantsPerRound)
        return update_Population(population,nextGenChildren);
    }


    static Population update_Population_ProababilityIntersection_Threaded(Population population, int[][] graph, int numberOfNodes, Genome parentGraph, float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents) {

        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list

        //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
        for (int i = 0, j = 1; j < nextGenParents.size(); i = i + 2, j = j + 2) {
        int[][] geneticCodesOfChildrens = Recombinations.intersection_with_proabability(nextGenParents.get(i).getGenome(), nextGenParents.get(j).getGenome(), proabibility, newChildsPerParents);
        Thread[] threads = new Thread[newChildsPerParents];
        for (int k = 0; k < geneticCodesOfChildrens.length; k++) {
            final int finalI = k;
            threads[finalI] = new Thread(() -> {
                Genome newChild = new Genome(numberOfNodes, geneticCodesOfChildrens[finalI], graph);

                //Mutation
                int[] mutated = Mutations.mutation(mutationrate,newChild);
                newChild.setGenome(mutated);

                //calculate degrees
                Genome.calculateDegrees(graph, newChild);

                //calculate fitness
                newChild.setFitness(FitnessFunctions.calculateFitness(newChild, parentGraph));

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
static Population update_Population_RANDOM(Population population, int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, float proabibility, int newChildsPerParents, List<Genome> nextGenParents){
        Random random = new Random();
        int randomIndex = random.nextInt(Recombinations.implementedRecombinationMethods);

        switch (randomIndex) {
            case 0:
                return update_Population_OnePointCrossover_Threaded(population, graph, numberOfNodes, parentGraph, mutationrate, proabibility, newChildsPerParents, nextGenParents);
            case 1:
                return update_Population_ProababilityIntersection_Threaded(population, graph, numberOfNodes, parentGraph, mutationrate, proabibility, newChildsPerParents, nextGenParents);
            default:
                throw new IllegalStateException("Unexpected value: " + randomIndex);
        }
}

static Population mutate_Population(Population population, int[][] graph, int numberOfNodes, OneGenome parentGraph, float mutationrate, float proabibility, int newChildsPerParents){
        List<Genome> nextGenChildren = Collections.synchronizedList(new LinkedList<>()); // Thread-safe list

        for (int i = 0; i < population.population.length; i++) {
            Genome newChild = new Genome(numberOfNodes,population.population[i].getGenome(),graph);

            //Mutation
            int[] mutated = Mutations.mutation(mutationrate,newChild);
            newChild.setGenome(mutated);

            //calculate degrees
            Genome.calculateDegrees(graph,newChild);

            //calculate fitness
            newChild.setFitness(FitnessFunctions.calculateFitness(newChild,parentGraph));
            nextGenChildren.add(newChild);
        }
        population.setGeneration(population.getGeneration()-1); //so that the generation wont be changed in update
        return update_Population(population,nextGenChildren);
    }

    static Population update_Population(Population population, List<Genome> newGenomes){
        Population p = population;
        int counter=0;
        //overwrites the old entries by the amount of newGenomes.size()
        for (int i = p.population.length-(newGenomes.size()+1);  counter < newGenomes.size() ; counter++,i++) {
            p.population[i]= newGenomes.get(counter);
        }
        p.setGeneration(p.getGeneration()+1);
        return p;
    }
}
