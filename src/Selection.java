import java.util.*;

//all methods expect a sorted Population
//Selection methods are used to select parents for the next generation
public class Selection {

    static final int IMPLEMENTED_SELECTION_METHODS = 6;

    static double base = 0.8; //base for exponential rank selection

    static Dictionary<Integer, String> selectionMethods = new Hashtable<>();

    static {
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

    static List<Genome> stochasticUniversalSampling_NoDuplicates(Population population, int numberOfParents) {
        int n = population.population.length;

        // Calculate total fitness with double precision
        double totalFitness = 0;
        for (Genome genome : population.population) {
            totalFitness += genome.positiveFitness;
        }

        // Use double precision for pointer distance
        double pointerDistance = totalFitness / numberOfParents;

        // Generate starting point
        Random random = new Random();
        double start = random.nextDouble() * pointerDistance;

        // Generate pointers
        double[] pointers = new double[numberOfParents];
        for (int i = 0; i < numberOfParents; i++) {
            pointers[i] = start + (i * pointerDistance);
        }

        // Use the corrected selection method
        return susSelectionNoDuplicates(population, pointers);
    }

    static List<Genome> susSelectionNoDuplicates(Population population, double[] pointers) {
        List<Genome> selectedParents = new ArrayList<>();
        Set<Integer> selectedParentIDs = new HashSet<>();

        int n = population.population.length;

        // Calculate cumulative fitness
        double[] cumulativeFitness = new double[n];
        cumulativeFitness[0] = population.population[0].positiveFitness;
        for (int i = 1; i < n; i++) {
            cumulativeFitness[i] = cumulativeFitness[i - 1] + population.population[i].positiveFitness;
        }

        // Select parents using SUS
        int currentIndex = 0;
        for (double pointer : pointers) {
            // Find the segment that contains the pointer
            while (currentIndex < n - 1 && cumulativeFitness[currentIndex] < pointer) {
                currentIndex++;
            }
            int selectedIndex = currentIndex;
            while (selectedParentIDs.contains(selectedIndex)) {
                selectedIndex = (selectedIndex + 1) % n; // Wrap around if necessary
            }
            selectedParentIDs.add(selectedIndex);
            selectedParents.add(population.population[selectedIndex]);
        }

        return selectedParents;
    }

    //takes in reversed List of fitness values
    static List<Genome> exponential_rankSelection_NoDuplicates(Population population, int numberOfParents) {

        //stores the selected parents positions
        Set<Integer> selectedParentIDs = new HashSet<>();

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
            if (!selectedParentIDs.contains(selectedIndex)) {
                selectedParentIDs.add(selectedIndex);
                selectedParents.add(population.population[selectedIndex]);
            }
        }
        return selectedParents;
    }


    private static List<Genome> linear_rankSelection_NoDuplicates(Population p, int numberOfParents) {
        Set<Integer> selectedParentIDs = new HashSet<>();
        List<Genome> selectedParents = new ArrayList<>();

        int n = p.population.length;

        // Calculate denominator (sum of ranks)
        // Using standard linear ranking formula: sum = n*(n+1)/2
        double denominator = n * (n + 1) / 2.0;

        // Create cumulative probability distribution
        double[] cumulativeProbabilities = new double[n];
        double cumulativeSum = 0.0;

        for (int rank = 0; rank < n; rank++) {
            // Probability for rank i: (rank + 1) / denominator
            // Higher rank (lower index) gets higher probability
            double probability = (n - rank) / denominator;
            cumulativeSum += probability;
            cumulativeProbabilities[rank] = cumulativeSum;
        }

        // Select parents using cumulative distribution
        Random random = new Random();

        while (selectedParents.size() < numberOfParents) {
            double r = random.nextDouble();
            int selectedIndex = 0;

            // Find the first index where cumulative probability >= r
            while (selectedIndex < n - 1 && cumulativeProbabilities[selectedIndex] < r) {
                selectedIndex++;
            }
            if (!selectedParentIDs.contains(selectedIndex)) {
                selectedParentIDs.add(selectedIndex);
                selectedParents.add(p.population[selectedIndex]);
            }
        }

        return selectedParents;
    }


    static List<Genome> rouletteWheelSelection_NoDuplicates(Population population, int numberOfParents) {

        //stores the selected parents positions
        Set<Integer> selectedParentIDs = new HashSet<>();
        List<Genome> selectedParents = new ArrayList<>();

        // Calculate the total fitness of the population
        long totalFitness = 0;
        for (Genome genome : population.population) {
            totalFitness += genome.positiveFitness;
        }

        // Perform selection based on the roulette wheel method
        Random random = new Random();

        while (selectedParents.size() < numberOfParents) {
            // Generate a random value between 0 and totalFitness
            double randomValue = random.nextDouble() * totalFitness;

            // Find which individual corresponds to this random value
            double cumulativeFitness = 0;
            for (int i = 0; i < population.population.length; i++) {
                cumulativeFitness += population.population[i].positiveFitness;

                // If cumulative fitness exceeds random value, select this individual
                if ((cumulativeFitness >= randomValue) && (!selectedParentIDs.contains(i))) {
                    selectedParents.add(population.population[i]);
                    selectedParentIDs.add(i);
                    break; // Exit the inner loop once we've found our selection
                }
            }
        }
        return selectedParents;
    }


    static List<Genome> tournamentSelection_NoDuplicates(Population population, int tournamentSize, int numberOfParents) {
        List<Genome> selectedParents = new ArrayList<>();
        Set<Integer> selectedParentIDs = new HashSet<>();

        Random random = new Random();
        int n = population.population.length;

        while (selectedParents.size() < numberOfParents) {
            // Select random contestants for this tournament
            Genome bestContestant = null;
            Genome[] tournamentFiled = shuffleArray(population.population);

            int winner = 0;
            for (int j = 0; j < tournamentSize; j++) {
                int randomIndex = random.nextInt(n);
                Genome contestant = population.population[randomIndex];

                if (bestContestant == null || contestant.fitness > bestContestant.fitness) {
                    bestContestant = contestant;
                    winner = randomIndex;
                }
            }

            if (!selectedParentIDs.contains(winner)) {
                selectedParents.add(population.population[winner]);
                selectedParentIDs.add(winner);
            }
        }

        return selectedParents;
    }

    //adds the members with the highest fitness value in population to a list

    static List<Genome> stochasticUniversalSampling(Population population, int numberOfParents) {
        int n = population.population.length;

        // Calculate total fitness with double precision
        double totalFitness = 0;
        for (Genome genome : population.population) {
            totalFitness += genome.positiveFitness;
        }

        // Use double precision for pointer distance
        double pointerDistance = totalFitness / numberOfParents;

        // Generate starting point
        Random random = new Random();
        double start = random.nextDouble() * pointerDistance;

        // Generate pointers
        double[] pointers = new double[numberOfParents];
        for (int i = 0; i < numberOfParents; i++) {
            pointers[i] = start + (i * pointerDistance);
        }

        // Use the corrected selection method
        return susSelection(population, pointers);
    }

    static List<Genome> susSelection(Population population, double[] pointers) {
        List<Genome> selectedParents = new ArrayList<>();
        Set<Integer> selectedParentIDs = new HashSet<>();

        int n = population.population.length;

        // Calculate cumulative fitness
        double[] cumulativeFitness = new double[n];
        cumulativeFitness[0] = population.population[0].positiveFitness;
        for (int i = 1; i < n; i++) {
            cumulativeFitness[i] = cumulativeFitness[i - 1] + population.population[i].positiveFitness;
        }

        // Select parents using SUS
        int currentIndex = 0;
        for (double pointer : pointers) {
            // Find the segment that contains the pointer
            while (currentIndex < n - 1 && cumulativeFitness[currentIndex] < pointer) {
                currentIndex++;
            }
            int selectedIndex = currentIndex;

            selectedParents.add(population.population[selectedIndex]);
        }

        return selectedParents;
    }

    //takes in reversed List of fitness values
    static List<Genome> exponential_rankSelection(Population population, int numberOfParents) {

        //stores the selected parents positions
        Set<Integer> selectedParentIDs = new HashSet<>();

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
            selectedParents.add(population.population[selectedIndex]);
        }
        return selectedParents;
    }


    private static List<Genome> linear_rankSelection(Population p, int numberOfParents) {
        Set<Integer> selectedParentIDs = new HashSet<>();
        List<Genome> selectedParents = new ArrayList<>();

        int n = p.population.length;

        // Calculate denominator (sum of ranks)
        // Using standard linear ranking formula: sum = n*(n+1)/2
        double denominator = n * (n + 1) / 2.0;

        // Create cumulative probability distribution
        double[] cumulativeProbabilities = new double[n];
        double cumulativeSum = 0.0;

        for (int rank = 0; rank < n; rank++) {
            // Probability for rank i: (rank + 1) / denominator
            // Higher rank (lower index) gets higher probability
            double probability = (n - rank) / denominator;
            cumulativeSum += probability;
            cumulativeProbabilities[rank] = cumulativeSum;
        }

        // Select parents using cumulative distribution
        Random random = new Random();

        while (selectedParents.size() < numberOfParents) {
            double r = random.nextDouble();
            int selectedIndex = 0;

            // Find the first index where cumulative probability >= r
            while (selectedIndex < n - 1 && cumulativeProbabilities[selectedIndex] < r) {
                selectedIndex++;
            }
            selectedParents.add(p.population[selectedIndex]);
        }

        return selectedParents;
    }


    static List<Genome> rouletteWheelSelection(Population population, int numberOfParents) {

        //stores the selected parents positions
        List<Genome> selectedParents = new ArrayList<>();

        // Calculate the total fitness of the population
        long totalFitness = 0;
        for (Genome genome : population.population) {
            totalFitness += genome.positiveFitness;
        }

        // Perform selection based on the roulette wheel method
        Random random = new Random();

        while (selectedParents.size() < numberOfParents) {
            // Generate a random value between 0 and totalFitness
            double randomValue = random.nextDouble() * totalFitness;

            // Find which individual corresponds to this random value
            double cumulativeFitness = 0;
            for (int i = 0; i < population.population.length; i++) {
                cumulativeFitness += population.population[i].positiveFitness;

                // If cumulative fitness exceeds random value, select this individual
                if ((cumulativeFitness >= randomValue)) {
                    selectedParents.add(population.population[i]);
                    break; // Exit the inner loop once we've found our selection
                }
            }
        }
        return selectedParents;
    }


    static List<Genome> tournamentSelection(Population population, int tournamentSize, int numberOfParents) {
        List<Genome> selectedParents = new ArrayList<>();


        Random random = new Random();
        int n = population.population.length;

        while (selectedParents.size() < numberOfParents) {
            // Select random contestants for this tournament
            Genome bestContestant = null;
            Genome[] tournamentFiled = shuffleArray(population.population);

            int winner = 0;
            for (int j = 0; j < tournamentSize; j++) {
                int randomIndex = random.nextInt(n);
                Genome contestant = population.population[randomIndex];

                if (bestContestant == null || contestant.fitness > bestContestant.fitness) {
                    bestContestant = contestant;
                    winner = randomIndex;
                }
            }
            selectedParents.add(population.population[winner]);
        }

        return selectedParents;
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


    static List<Genome> select_SelectionMethod(Population p, int numberOfParents, int identifier, boolean dublicatesAllowed) {
        List<Genome> selectedParents = new LinkedList<>();

        System.out.println("Selection method: " + selectionMethods.get(identifier));

        Random random = new Random();
        //the more parents are needed the smaller the tournaments should be
        int numberOfContestantsPerRound = p.population.length / numberOfParents;

        if (identifier == 6) {
            identifier = random.nextInt(IMPLEMENTED_SELECTION_METHODS);
            System.out.println("Randomly selected method: " + selectionMethods.get(identifier));
        }

        if(dublicatesAllowed){
            switch (identifier) {
                case 0 -> selectedParents = stochasticUniversalSampling(p, numberOfParents);
                case 1 -> selectedParents = tournamentSelection(p, numberOfContestantsPerRound, numberOfParents);
                case 2 -> selectedParents = rouletteWheelSelection(p, numberOfParents);
                case 3 -> selectedParents = linear_rankSelection(p, numberOfParents);
                case 4 -> selectedParents = exponential_rankSelection(p, numberOfParents);
                case 5 -> selectedParents = elitism(p, numberOfParents);
                default -> System.out.println("Invalid selection method identifier");
            }
        }
        else {
            switch (identifier) {
                case 0 -> selectedParents = stochasticUniversalSampling_NoDuplicates(p, numberOfParents);
                case 1 -> selectedParents = tournamentSelection_NoDuplicates(p, numberOfContestantsPerRound, numberOfParents);
                case 2 -> selectedParents = rouletteWheelSelection_NoDuplicates(p, numberOfParents);
                case 3 -> selectedParents = linear_rankSelection_NoDuplicates(p, numberOfParents);
                case 4 -> selectedParents = exponential_rankSelection_NoDuplicates(p, numberOfParents);
                case 5 -> selectedParents = elitism(p, numberOfParents);
                default -> System.out.println("Invalid selection method identifier");
            }
        }

        // Shuffle the selected parents to ensure that not only the best ones are selected sequentially
        //Collections.shuffle(selectedParents);

        System.out.println("Selected parents: " + numberOfParents);
        return selectedParents;

    }


}