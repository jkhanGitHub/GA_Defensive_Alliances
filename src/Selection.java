import java.util.*;

//all methods expect a sorted Population
//Selection methods are used to select parents for the next generation
public class Selection {


    static final int IMPLEMENTED_SELECTION_METHODS = 4;

    static Dictionary<Integer, String> selectionMethods = new Hashtable<>();
    static{
        selectionMethods.put(0, "elitism");
        selectionMethods.put(1, "stochasticUniversalSampling");
        selectionMethods.put(2, "rankSelection");
        selectionMethods.put(3, "rouletteWheelSelection");
        selectionMethods.put(4, "tournamentSelectionElimination");
    }

    //adds the members with the highest fitness value in population to a list
    static List<Genome> elitism(Population population, int numberOfElits) {
        List<Genome> elits = new LinkedList<>();
        for (int i = 0; i < numberOfElits; i++) {
            elits.add(population.population[i]);
        }
        return elits;
    }

    static List<Genome> stochasticUniversalSampling(Population population) {
        List<Genome> selectedParents = new ArrayList<>();

        int n = population.population.length;

        // Calculate mean fitness
        double meanFitness = 0.0;
        for (Genome genome : population.population) {
            meanFitness += genome.positiveFitness;
        }
        meanFitness = meanFitness / n;

        // Generate a random starting point in [0, meanFitness)
        Random random = new Random();
        double alpha = random.nextDouble() * meanFitness;

        // Initialize pointers
        double delta = alpha;
        double cumulativeSum = 0;
        int j = 0;

        // Perform SUS

        while (delta > cumulativeSum) {
            cumulativeSum += population.population[j].positiveFitness;
            if (delta <= cumulativeSum) {
                selectedParents.add(population.population[j]);
                delta += meanFitness;
            } else {
                j++;
                if (j >= n) {
                    j = 0; // Wrap around if necessary
                }
            }
        }


        return selectedParents;
    }

    static List<Genome> rankSelection(Population population) {
        List<Genome> selectedParents = new ArrayList<>();

        // Calculate the rank-based selection probabilities
        double[] selectionProbabilities = new double[population.population.length];
        for (int i = 0; i < population.population.length; i++) {
            selectionProbabilities[i] = (double) (population.population.length - i) / (population.population.length * (population.population.length + 1) / 2);
        }

        // Perform selection based on the calculated probabilities
        Random random = new Random();

        double rand = random.nextDouble();
        double cumulativeProbability = 0.0;
        for (int j = 0; j < population.population.length; j++) {
            cumulativeProbability += selectionProbabilities[j];
            if (rand <= cumulativeProbability) {
                selectedParents.add(population.population[j]);
                break;
            }
        }


        return selectedParents;
    }


    static List<Genome> rouletteWheelSelection(Population population) {
        List<Genome> selectedParents = new ArrayList<>();

        // Calculate the total fitness of the population
        double totalFitness = 0.0;
        for (Genome genome : population.population) {
            totalFitness += genome.positiveFitness;
        }

        // Perform selection based on the roulette wheel method
        Random random = new Random();

        double rand = random.nextDouble() * totalFitness;
        double cumulativeFitness = 0.0;
        for (Genome genome : population.population) {
            cumulativeFitness += genome.positiveFitness;
            if (rand <= cumulativeFitness) {
                selectedParents.add(genome);
                break;
            }
        }

        return selectedParents;
    }


    static List<Genome> tournamentSelectionElimination(Population p, int numberOfContestantsPerRound) {
        List<Genome> nextGenParents = new LinkedList<>();

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
        return nextGenParents;
    }


    // Implementing Fisher–Yates shuffle
    static Genome[] shuffleArray(Genome[] ar) {
        Genome[] temp = ar;
        Random rnd = new Random();
        for (int i = temp.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            Genome a = temp[index];
            temp[index] = temp[i];
            temp[i] = a;
        }
        return temp;
    }


    static List<Genome> select_SelectionMethod(Population p, int numberOfContestantsPerRound, int identifier) {
        List<Genome> selectedParents = new LinkedList<>();

        System.out.println("Selection method: " + selectionMethods.get(identifier));

        Random random = new Random();

        switch (identifier) {
            case 0 -> selectedParents = elitism(p, random.nextInt(Genetic_Algorithm.POPULATION_SIZE));
            case 1 -> selectedParents = stochasticUniversalSampling(p);
            case 2 -> selectedParents = rankSelection(p);
            case 3 -> selectedParents = rouletteWheelSelection(p);
            case 4 -> selectedParents = tournamentSelectionElimination(p, numberOfContestantsPerRound);
            default -> System.out.println("Invalid selection method identifier");
        }

        // Shuffle the selected parents to ensure that not only the best ones are selected sequentially
        Collections.shuffle(selectedParents);

        return selectedParents;

    }

}