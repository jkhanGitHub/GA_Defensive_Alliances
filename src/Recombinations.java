import java.util.Random;

public class Recombinations {


    //change when implementing new Recombination methods
    final static int implementedRecombinationMethods = 4;


    static final float INTERSECTION_PROBABILITY = 0.9f;
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
                intersected_array[i] = 0;
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

    // Example usage
       /* public static void main(String[] args) {
            int[] parentA = {1, 2, 3, 4, 5};
            int[] parentB = {6, 7, 8, 9, 10};

            int[][] offspring = onePointCrossover(parentA, parentB);

            System.out.println("AB offspring: " + java.util.Arrays.toString(offspring[0]));
            System.out.println("BA offspring: " + java.util.Arrays.toString(offspring[1]));
        }*/
}

