import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Random;

public class Recombinations {


    //change when implementing new Recombination methods
    final static int implementedRecombinationMethods = 4;

    static Dictionary<Integer,String> recombinationIdentifiers = new Hashtable<Integer,String>();
    static {
        recombinationIdentifiers.put(0,"OnePointCrossover");
        recombinationIdentifiers.put(1, "ProababilityIntersection");
    }


    public static int[][] onePointCrossover(int[] a, int[] b) {
        // Validate input arrays
        if (a.length != b.length) {
            throw new IllegalArgumentException("Parent arrays must be of equal length");
        }

        Random random = new Random();
        // Select random crossover point (0 to length-1 inclusive)
        int crossoverPoint = random.nextInt(a.length);

        // Create offspring arrays
        int[] ab = new int[a.length];
        int[] ba = new int[a.length];

        // Perform crossover for AB (a head + b tail)
        System.arraycopy(a, 0, ab, 0, crossoverPoint + 1);
        System.arraycopy(b, crossoverPoint + 1, ab, crossoverPoint + 1, a.length - (crossoverPoint + 1));

        // Perform crossover for BA (b head + a tail)
        System.arraycopy(b, 0, ba, 0, crossoverPoint + 1);
        System.arraycopy(a, crossoverPoint + 1, ba, crossoverPoint + 1, a.length - (crossoverPoint + 1));

        return new int[][]{ab, ba};
    }

    public static int[][] onePointCrossover(int[] a, int[] b, int childrenAmount) {

        int[][] listOfChildren = new int[childrenAmount][1];
        // Validate input arrays
        if (a.length != b.length) {
            throw new IllegalArgumentException("Parent arrays must be of equal length");
        }

        Random random = new Random();

        for (int k = 0; k < childrenAmount; k++) {
            // Select random crossover point (0 to length-1 inclusive)
            int crossoverPoint = random.nextInt(a.length);

            // Create offspring array
            int[] child = new int[a.length];
            System.arraycopy(a, 0, child, 0, crossoverPoint + 1);
            System.arraycopy(b, crossoverPoint + 1, child, crossoverPoint + 1, a.length - (crossoverPoint + 1));
            listOfChildren[k] = child;
        }
        return listOfChildren;
    }

    static int[][] intersection_with_proabability(int[] genome1, int[] genome2, float proabibility,int childrenAmount) {
        int[][] listOfChildren = new int[childrenAmount][1];
        //intersection
        for (int k = 0; k < childrenAmount; k++) {
            int[] intersected_array = new int[genome1.length];
            for (int i = 0; i < genome1.length; i++) {
                if (genome1[i] == genome2[i]) {
                    //if (Math.random()<=INTERSECTION_PROBABILITY) //commentn out if its bad
                    intersected_array[i] = genome1[i];
                } else { //probability part of intersection
                    double x = Math.random();
                    if (x <= proabibility) {
                        intersected_array[i] = 1;
                    }
                }
            }
            listOfChildren[k] = intersected_array;
        }
        return listOfChildren;
    }

    static int[][] recombination_with_identifier(int[] genome1, int[] genome2, float proabibility,int childrenAmount, int recombinationIdentifier){
        switch (recombinationIdentifier) {
            case 0:
                return onePointCrossover(genome1, genome2, childrenAmount);
            case 1:
                return intersection_with_proabability(genome1, genome2, proabibility, childrenAmount);
            default:
                throw new IllegalArgumentException("Invalid recombination identifier");
        }
    }
}

