import java.util.*;

//all methods expect a sorted Population
//Selection methods are used to select parents for the next generation
public class Selection {

    static final int IMPLEMENTED_SELECTION_METHODS = 6;

    static double base = 0.8; //base for exponential rank selection

    static Dictionary<Integer, String> selectionMethods = new Hashtable<>();
    static{
        selectionMethods.put(0, "Stochastic Universal Sampling");
        selectionMethods.put(1, "Tournament Selection Elimination");
        selectionMethods.put(2, "Roulette Wheel Selection");
        selectionMethods.put(3, "Linear Rank Selection");
        selectionMethods.put(4, "Exponential Rank Selection");
        selectionMethods.put(5, "Elitism");
        selectionMethods.put(6, "Random");
    }

    //adds the members with the highest fitness value in population to a list
    static List<Genome> elitism(Population population, int numberOfElits) {
        List<Genome> elits = new LinkedList<>();
        for (int i = 0; i < numberOfElits; i++) {
            elits.add(population.population[i]);
        }
        return elits;
    }

    //can result in more parents than the population size allows
    static List<Genome> stochasticUniversalSampling(Population population, int numberOfParents) {
        List<Genome> selectedParents = new ArrayList<>();
        int n = population.population.length;

        // Calculate total fitness
        long totalFitness = 0;
        for (Genome genome : population.population) {
            totalFitness += genome.positiveFitness;
        }

        // Calculate distance between pointers
        double pointerDistance = totalFitness / numberOfParents;

        // Generate starting point
        Random random = new Random();
        double start = random.nextDouble() * pointerDistance;

        // Generate pointers
        double[] pointers = new double[numberOfParents];
        for (int i = 0; i < numberOfParents; i++) {
            pointers[i] = start + (i * pointerDistance);
        }

        // Use RWS to select individuals
        return rouletteWheelSelection(population, pointers);
    }

    static List<Genome> rouletteWheelSelection(Population population, double[] pointers) {

        //stores the selected parents positions
        List<Integer> selectedParentIDs = new ArrayList<>();

        List<Genome> selectedParents = new ArrayList<>();
        int n = population.population.length;

        for (double pointer : pointers) {
            long cumulativeFitness = 0;
            int i = 0;
            while (cumulativeFitness < pointer) {
                cumulativeFitness += population.population[i].positiveFitness;
                i++;
                i = i % n; //resets the index to 0 if it reaches the end of the population
            }
            // Select the next genome that has not been selected yet
            while(selectedParentIDs.contains(i)) {
                i++;
                i = i % n; //resets the index to 0 if it reaches the end of the population
            }
            selectedParentIDs.add(i);
            selectedParents.add(population.population[i]);
        }

        return selectedParents;
    }

    //takes in reversed List of fitness values
    static List<Genome> exponential_rankSelection(Population population, int numberOfParents) {

        //stores the selected parents positions
        List<Integer> selectedParentIDs = new ArrayList<>();

        List<Genome> selectedParents = new ArrayList<>();

        int n = population.population.length;

        //Pre-calculate the denominator
        double denominator = 0.0;
        for (int rank = 0; rank < n; rank++) {
            denominator += Math.pow(base, rank);
        }

        // Calculate the probability for each rank
        double[] probabilities = new double[n];
        for (int rank = 0; rank < n; rank++) {
            // Higher rank (lower index) gets higher probability
            probabilities[rank] = Math.pow(base, rank) / denominator;
        }

        // Create a cumulative probability distribution
        // This allows us to use a single random number to select an individual
        double[] cumulativeProbabilities = new double[n];
        cumulativeProbabilities[0] = probabilities[0];
        for (int i = 1; i < n; i++) {
            cumulativeProbabilities[i] = cumulativeProbabilities[i - 1] + probabilities[i];
        }

        // Select the parents using the cumulative distribution
        Random random = new Random();

        for (int i = 0; i < numberOfParents; i++) {
            double r = random.nextDouble();
            // Find the first index where cumulative probability >= r
            int selectedIndex = 0;
            while (selectedIndex < n - 1 && cumulativeProbabilities[selectedIndex] < r) {
                selectedIndex++;
            }
            if (!selectedParentIDs.contains(selectedIndex)){
                selectedParentIDs.add(i);
                selectedParents.add(population.population[selectedIndex]);
            }
        }
        return selectedParents;
    }

    private static List<Genome> linear_rankSelection(Population p, int numberOfParents) {
        //stores the selected parents positions
        List<Integer> selectedParentIDs = new ArrayList<>();

        List<Genome> selectedParents = new ArrayList<>();

        int n = p.population.length;

        Random random = new Random();
        int denominator = n*(n+1)/2;

        //Multiplier so we can have more winners

        int i = 0;// Calculate the probabilities for each genome
        while (selectedParents.size()<numberOfParents) {
            double p_i = (double) (2 * (n - i + 1)) /(denominator);
            if ((p_i > random.nextDouble()) && !selectedParentIDs.contains(i)) {
                selectedParentIDs.add(i);
                // Add the genome to the selected parents list
                selectedParents.add(p.population[i]);
            }
            i++;
            i = i % p.population.length;
        }

        return selectedParents;
    }


    static List<Genome> rouletteWheelSelection(Population population, int numberOfParents) {

        //stores the selected parents positions
        List<Integer> selectedParentIDs = new ArrayList<>();

        List<Genome> selectedParents = new ArrayList<>();

        // Calculate the total fitness of the population
        long totalFitness = 0;
        for (Genome genome : population.population) {
            totalFitness += genome.positiveFitness;
        }

        // Perform selection based on the roulette wheel method
        Random random = new Random();

        int i =0;
        while (selectedParents.size()<numberOfParents) {
            double p_i = population.population[i].positiveFitness / totalFitness;
            double ran = random.nextDouble();
            // Check if the genome has already been selected
            if ((p_i > ran) && !selectedParentIDs.contains(i)) {
                selectedParentIDs.add(i);
                // Add the genome to the selected parents list
                selectedParents.add(population.population[i]);
            }
            i++;
            i = i % population.population.length;
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
        int numberOfParents = p.population.length/numberOfContestantsPerRound;

        if (identifier == 6) {
            identifier = random.nextInt(IMPLEMENTED_SELECTION_METHODS);
            System.out.println("Randomly selected method: " + selectionMethods.get(identifier));
        }

        switch (identifier) {
            case 0 -> selectedParents = stochasticUniversalSampling(p,numberOfParents);
            case 1 -> selectedParents = tournamentSelectionElimination(p, numberOfContestantsPerRound);
            case 2 -> selectedParents = rouletteWheelSelection(p, numberOfParents);
            case 3 -> selectedParents = linear_rankSelection(p, numberOfParents);
            case 4 -> selectedParents = exponential_rankSelection(p, numberOfParents);
            case 5 -> selectedParents = elitism(p, numberOfParents);
            default -> System.out.println("Invalid selection method identifier");
        }

        // Shuffle the selected parents to ensure that not only the best ones are selected sequentially
        //Collections.shuffle(selectedParents);

        System.out.println("Selected parents: " + selectedParents.size());
        System.out.println("amount of new children: " + ((selectedParents.size()/2)*Genetic_Algorithm.NUMBER_OF_CHILDS_PER_PARENT));
        return selectedParents;

    }



}