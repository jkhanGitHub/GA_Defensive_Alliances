import java.util.*;
//all methods expect a sorted Population
<<<<<<< HEAD
public class Selection {
=======
public class  Selection {
>>>>>>> 7f7850c (init commit -m "easier to change now")

    public static int getWorstFitness_measured() {
        return worstFitness_measured;
    }

    public static void setWorstFitness_measured(int worstFitness_measured_update) {
        worstFitness_measured = worstFitness_measured_update;
    }

    static int worstFitness_measured = 0; //set to 0 beacuse 0 is the optimum

    //adds the members with highest fitness value in population to a list
    List<Genome> elitism(Population population, int numberOfElits){
        List<Genome> elits = new LinkedList<>();
        for (int i = 0; i < numberOfElits; i++) {
            elits.add(population.population[i]);
        }
        return elits;
    }

<<<<<<< HEAD
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
=======

>>>>>>> 7f7850c (init commit -m "easier to change now")
    void update_worstFitness_measured(Population p){
        int worstFitnessofToday = p.population[p.population.length-1].getFitness();
        int worstFitnessinHistory = getWorstFitness_measured();
        setWorstFitness_measured(Math.max(worstFitnessofToday,worstFitnessinHistory));
    }

    float proabability(){
        float f = 0;
        return f;
    }

    static Population tournamentSelectionElimination_OnePointCrossover(Population p, int numberOfContestantsPerRound, int[][] graph, int numberOfNodes, Genome parentGraph, float mutationrate){
        List<Genome> nextGenParents = new LinkedList<>();
        List<Genome> nextGenChildren = new LinkedList<>();
        //play tournament
        int winnerID;
        Genome[] temp = shuffleArray(p.getPopulation());
        for (int i = 0, j = numberOfContestantsPerRound-1; j < temp.length; i=j+1,j=j+numberOfContestantsPerRound) {
            for (winnerID=i; i < j; i++) {
                if(Math.max(temp[winnerID].fitness,temp[i].fitness)==temp[i].fitness){
                    winnerID = i;
                }
            }
            //add winner to winner list
            nextGenParents.add(temp[winnerID]);
        }
        //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
        for (int i = 0,j = 1; j < nextGenParents.size(); i=i+2,j= j+2) {
            int[][] geneticCodesOfChildrens = Recombinations.onePointCrossover(nextGenParents.get(i).getGenome(),nextGenParents.get(j).getGenome());
<<<<<<< HEAD
            Genome ab = new Genome(numberOfNodes,geneticCodesOfChildrens[0],graph,mutationrate);
            Genome ba = new Genome(numberOfNodes,geneticCodesOfChildrens[1],graph,mutationrate);

=======
            Genome ba = new Genome(numberOfNodes,geneticCodesOfChildrens[1],graph);
            Genome ab = new Genome(numberOfNodes,geneticCodesOfChildrens[0],graph);

            //Mutation
            Mutations.mutation(mutationrate,ba);
            Mutations.mutation(mutationrate,ab);

            //calculate degrees
            Genome.calculateDegrees(graph,ba);
            Genome.calculateDegrees(graph,ab);

            //calculate fitness
>>>>>>> 7f7850c (init commit -m "easier to change now")
            ab.setFitness(FitnessFunctions.calculateFitness(ab,parentGraph));
            ba.setFitness(FitnessFunctions.calculateFitness(ba,parentGraph));

            nextGenChildren.add(ab);
            nextGenChildren.add(ba);
        }

        //Elites of the previous gen stay in the next generation
        //Number of Elites = POPULATION_SIZE - (POPULATION_SIZE/numberOfContestantsPerRound)
<<<<<<< HEAD
        return update_Population(p,nextGenChildren);
=======
        return Population.update_Population(p,nextGenChildren);
>>>>>>> 7f7850c (init commit -m "easier to change now")
    }

    static Population tournamentSelectionElimination_ProababilityIntersection(Population p, int numberOfContestantsPerRound, int[][] graph, int numberOfNodes, Genome parentGraph, float mutationrate, float proabibility,int newChildsPerParents){
        List<Genome> nextGenParents = new LinkedList<>();
        List<Genome> nextGenChildren = new LinkedList<>();
        //play tournament
        int winnerID;
        Genome[] temp = shuffleArray(p.getPopulation());
        for (int i = 0, j = numberOfContestantsPerRound-1; j < temp.length; i=j+1,j=j+numberOfContestantsPerRound) {
            for (winnerID=i; i < j; i++) {
                if(Math.max(temp[winnerID].fitness,temp[i].fitness)==temp[i].fitness){
                    winnerID = i;
                }
            }
            //add winner to winner list
            nextGenParents.add(temp[winnerID]);
        }
        //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
        for (int i = 0,j = 1; j < nextGenParents.size(); i=i+2,j= j+2) {
            int[][] geneticCodesOfChildrens = Recombinations.intersection_with_proabability(nextGenParents.get(i).getGenome(),nextGenParents.get(j).getGenome(),proabibility,newChildsPerParents);
            for (int k = 0; k < geneticCodesOfChildrens.length; k++) {
<<<<<<< HEAD
                Genome newChild = new Genome(numberOfNodes,geneticCodesOfChildrens[k],graph,mutationrate);
=======
                Genome newChild = new Genome(numberOfNodes,geneticCodesOfChildrens[k],graph);

                //Mutation
                Mutations.mutation_of_vertices_with_high_degree(newChild);

                //calculate degrees
                Genome.calculateDegrees(graph,newChild);

                //calculate fitness
>>>>>>> 7f7850c (init commit -m "easier to change now")
                newChild.setFitness(FitnessFunctions.calculateFitness(newChild,parentGraph));
                nextGenChildren.add(newChild);
            }
        }

        //Elites of the previous gen stay in the next generation
        //Number of Elites = POPULATION_SIZE - (POPULATION_SIZE/numberOfContestantsPerRound)
<<<<<<< HEAD
        return update_Population(p,nextGenChildren);
=======
        return Population.update_Population(p,nextGenChildren);
>>>>>>> 7f7850c (init commit -m "easier to change now")
    }

    static Population tournamentSelectionElimination_ProababilityIntersection_Threaded(Population p, int numberOfContestantsPerRound, int[][] graph, int numberOfNodes, Genome parentGraph, float mutationrate, float proabibility, int newChildsPerParents) {
        List<Genome> nextGenParents = new LinkedList<>();
        List<Genome> nextGenChildren = new LinkedList<>();
        //play tournament
        int winnerID;
        Genome[] temp = shuffleArray(p.getPopulation());
        for (int i = 0, j = numberOfContestantsPerRound - 1; j < temp.length; i = j + 1, j = j + numberOfContestantsPerRound) {
            for (winnerID = i; i < j; i++) {
                if (Math.max(temp[winnerID].fitness, temp[i].fitness) == temp[i].fitness) {
                    winnerID = i;
                }
            }
            //add winner to winner list
            nextGenParents.add(temp[winnerID]);
        }
        //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
        for (int i = 0, j = 1; j < nextGenParents.size(); i = i + 2, j = j + 2) {
            int[][] geneticCodesOfChildrens = Recombinations.intersection_with_proabability(nextGenParents.get(i).getGenome(), nextGenParents.get(j).getGenome(), proabibility, newChildsPerParents);
            Thread[] threads = new Thread[newChildsPerParents];
            for (int k = 0; k < geneticCodesOfChildrens.length; k++) {
                final int finalI = k;
                threads[finalI] = new Thread(() -> {
<<<<<<< HEAD
                    Genome newChild = new Genome(numberOfNodes, geneticCodesOfChildrens[finalI], graph, mutationrate);
                    newChild.setFitness(FitnessFunctions.calculateFitness(newChild, parentGraph));

                    System.out.println(Objects.isNull(newChild));
=======
                    Genome newChild = new Genome(numberOfNodes, geneticCodesOfChildrens[finalI], graph);

                    //Mutation
                    Mutations.mutation(mutationrate,newChild);

                    //calculate degrees
                    Genome.calculateDegrees(graph, newChild);

                    //calculate fitness
                    newChild.setFitness(FitnessFunctions.calculateFitness(newChild, parentGraph));

                   // System.out.println(Objects.isNull(newChild));
>>>>>>> 7f7850c (init commit -m "easier to change now")

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
<<<<<<< HEAD
        return update_Population(p, nextGenChildren);
=======
        return Population.update_Population(p, nextGenChildren);
>>>>>>> 7f7850c (init commit -m "easier to change now")
    }

    static Population tournamentSelectionElimination_ProababilityIntersection_cFitness(Population p, int numberOfContestantsPerRound, int[][] graph, int numberOfNodes, Genome parentGraph, float mutationrate, float proabibility){
        List<Genome> nextGenParents = new LinkedList<>();
        List<Genome> nextGenChildren = new LinkedList<>();
        //play tournament
        int winnerID;
        Genome[] temp = shuffleArray(p.getPopulation());
        for (int i = 0, j = numberOfContestantsPerRound-1; j < temp.length; i=j+1,j=j+numberOfContestantsPerRound) {
            for (winnerID=i; i < j; i++) {
                if(Math.max(temp[winnerID].fitness,temp[i].fitness)==temp[i].fitness){
                    winnerID = i;
                }
            }
            //add winner to winner list
            nextGenParents.add(temp[winnerID]);
        }
        //recombine Parents: Number of parents = POPULATION_SIZE/numberOfContestantsPerRound
        for (int i = 0,j = 1; j < nextGenParents.size(); i=i+2,j= j+2) {
            int[][] geneticCodesOfChildrens = Recombinations.intersection_with_proabability(nextGenParents.get(i).getGenome(),nextGenParents.get(j).getGenome(),proabibility,8);
            for (int k = 0; k < geneticCodesOfChildrens.length; k++) {
<<<<<<< HEAD
                Genome newChild = new Genome(numberOfNodes,geneticCodesOfChildrens[k],graph,mutationrate);
=======
                Genome newChild = new Genome(numberOfNodes,geneticCodesOfChildrens[k],graph);
>>>>>>> 7f7850c (init commit -m "easier to change now")
                newChild.setFitness(FitnessFunctions.cFitness(newChild,parentGraph));
                nextGenChildren.add(newChild);
            }
        }

        //Elites of the previous gen stay in the next generation
        //Number of Elites = POPULATION_SIZE - (POPULATION_SIZE/numberOfContestantsPerRound)
<<<<<<< HEAD
        return update_Population(p,nextGenChildren);
=======
        return Population.update_Population(p,nextGenChildren);
>>>>>>> 7f7850c (init commit -m "easier to change now")
    }

    // Implementing Fisher–Yates shuffle
    static Genome[] shuffleArray(Genome[] ar)
    {
        Genome[] temp = ar;
        Random rnd = new Random();
        for (int i = temp.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            Genome a = temp[index];
            temp[index] = temp[i];
            temp[i] = a;
        }
        return temp;
    }

}
