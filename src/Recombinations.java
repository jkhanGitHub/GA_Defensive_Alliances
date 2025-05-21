import java.util.*;

public class Recombinations {


    //change when implementing new Recombination methods
    final static int implementedRecombinationMethods = 4;

    static Dictionary<Integer,String> recombinationIdentifiers = new Hashtable<Integer,String>();
    static {
        recombinationIdentifiers.put(0,"OnePointCrossover");
        recombinationIdentifiers.put(1, "ProababilityIntersection");
        recombinationIdentifiers.put(2,"OnePointCrossoverGenome");
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
            int crossoverPoint = random.nextInt(a.length);

            // Create offspring array
            int[] child = new int[a.length];
            System.arraycopy(a.getGenome(), 0, child, 0, crossoverPoint + 1);
            System.arraycopy(b.getGenome(), crossoverPoint + 1, child, crossoverPoint + 1, a.length - (crossoverPoint + 1));
            listOfChildren[k] = new Genome(child,a,b,crossoverPoint);
        }
        return listOfChildren;
    }

    static Genome[] intersection_with_proabability(Genome genome1, Genome genome2, float proabibility,int childrenAmount) {
        Genome[] listOfChildren = new Genome[childrenAmount];
        List<Integer> changedAllele = new ArrayList<>();
        //intersection
        for (int k = 0; k < childrenAmount; k++) {
            int[] intersected_array = new int[genome1.length];
            for (int i = 0; i < genome1.length; i++) {
                if (genome1.getGenome()[i] == genome2.getGenome()[i]) {
                    //if (Math.random()<=INTERSECTION_PROBABILITY) //commentn out if its bad
                    intersected_array[i] = genome1.getGenome()[i];
                } else { //probability part of intersection
                    double x = Math.random();
                    if (x <= proabibility) {
                        intersected_array[i] = 1;
                        changedAllele.add(i);
                    }
                }
            }
            listOfChildren[k] = new Genome(intersected_array,genome1,genome2,changedAllele);
        }
        return listOfChildren;
    }

    static Genome[] recombination_with_identifier(Genome genome1, Genome genome2, float proabibility,int childrenAmount, int recombinationIdentifier){
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

