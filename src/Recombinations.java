import java.util.*;

public class Recombinations {


    //change when implementing new Recombination methods
    final static int implementedRecombinationMethods = 4;

    static Dictionary<Integer, String> recombinationIdentifiers = new Hashtable<Integer, String>();

    static {
        recombinationIdentifiers.put(0, "OnePointCrossover");
        recombinationIdentifiers.put(1, "ProababilityIntersection");
    }


    public static Genome[] onePointCrossover(Genome a, Genome b, int childrenAmount) {

        Genome[] listOfChildren = new Genome[childrenAmount];
        // Validate input arrays
        if (a.length != b.length) {
            throw new IllegalArgumentException("Parent arrays must be of equal length");
        }

        Random random = new Random();

        for (int k = 0; k < childrenAmount; k++) {
            // Select random crossover point (0 to length-1 inclusive)
            int crossoverPoint = random.nextInt(1, a.length);

            // Create offspring array
            int[] child = new int[a.length];
            Genome newChild = new Genome(child, a, b, crossoverPoint);

            listOfChildren[k] = newChild;
        }
        return listOfChildren;
    }

    public static Genome onePointCrossoverSingle(Genome a, Genome b) {

        // Validate input arrays
        if (a.length != b.length) {
            throw new IllegalArgumentException("Parent arrays must be of equal length");
        }

        Random random = new Random();


        // Select random crossover point (0 to length-1 inclusive)
        int crossoverPoint = random.nextInt(1, a.length);

        // Create offspring array
        int[] child = new int[a.length];
        Genome newChild = new Genome(child, a, b, crossoverPoint);

        return newChild;
    }

    static Genome[] intersection_with_proabability(Genome genome1, Genome genome2, float proabibility, int childrenAmount) {
        Genome[] listOfChildren = new Genome[childrenAmount];

        //intersection
        for (int k = 0; k < childrenAmount; k++) {
            List<Integer> changedAllele = new ArrayList<>();
            int[] intersected_array = new int[genome1.length];
            for (int i = 0; i < genome1.length; i++) {
                if (genome1.getGenome()[i] == genome2.getGenome()[i]) {
                    //if (Math.random()<=INTERSECTION_PROBABILITY) //commentn out if its bad
                    intersected_array[i] = genome1.getGenome()[i];
                } else { //probability part of intersection
                    double x = Math.random();
                    if (x <= proabibility) {
                        changedAllele.add(i);
                    }
                }
            }
            Genome newChild = new Genome(intersected_array, genome1, genome2, changedAllele);
            listOfChildren[k] = newChild;
        }
        return listOfChildren;
    }

    static Genome intersection_with_proababilitySingle(Genome genome1, Genome genome2, float proabibility) {

        List<Integer> changedAllele = new ArrayList<>();
        //intersection

        int[] intersected_array = new int[genome1.length];
        for (int i = 0; i < genome1.length; i++) {
            if (genome1.getGenome()[i] == genome2.getGenome()[i]) {
                //if (Math.random()<=INTERSECTION_PROBABILITY) //commentn out if its bad
                intersected_array[i] = genome1.getGenome()[i];
            } else { //probability part of intersection
                double x = Math.random();
                if (x <= proabibility) {
                    changedAllele.add(i);
                }
            }
        }
        Genome newChild = new Genome(intersected_array, genome1, genome2, changedAllele);

        return newChild;
    }

    static Genome[] recombination_with_identifier(Genome genome1, Genome genome2, float proabibility, int childrenAmount, int recombinationIdentifier) {
        switch (recombinationIdentifier) {
            case 0:
                return onePointCrossover(genome1, genome2, childrenAmount);
            case 1:
                return intersection_with_proabability(genome1, genome2, proabibility, childrenAmount);
            default:
                throw new IllegalArgumentException("Invalid recombination identifier");
        }
    }

    static Genome recombination_with_identifierSingle(Genome genome1, Genome genome2, float proabibility, int recombinationIdentifier) {
        switch (recombinationIdentifier) {
            case 0:
                return onePointCrossoverSingle(genome1, genome2);
            case 1:
                return intersection_with_proababilitySingle(genome1, genome2, proabibility);
            default:
                throw new IllegalArgumentException("Invalid recombination identifier");
        }
    }
}

