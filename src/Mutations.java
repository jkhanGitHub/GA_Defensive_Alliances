import java.util.*;

public class Mutations {

    public static int implementedMutationMethods = 2;

    /* fully functional but slower because it iterates through the matrix instead of the neighbourhood
    protected static void mutation(float mutationrate, Genome mutatedGenome, int[][] matrix){
        for (int i=0; i<mutatedGenome.length; i++) {
            if (Math.random()<=mutationrate) {
                mutatedGenome.genome[i]=Math.abs(mutatedGenome.genome[i]-1);
                mutatedGenome.bitFlip(matrix,i);
            }
        }
    }

     */

    protected static void mutation(float mutationrate, Genome mutatedGenome){
        for (int i=0; i<mutatedGenome.length; i++) {
            if (Math.random()<=mutationrate) {
                mutatedGenome.genome[i]=Math.abs(mutatedGenome.genome[i]-1);
                mutatedGenome.bitFlip(i);
            }
        }
    }


    //degrees of genome must have been calculated before
    /* fully functional but slower because it iterates through the matrix instead of the neighbourhood
    protected static void mutation_of_vertices_with_high_degree(float mutationrate, Genome mutatedGenome, int[][] matrix){
        int multiplier = 0;
        for (int i=0; i<mutatedGenome.length; i++) {
            if(mutatedGenome.getDegrees()[i]==0){
                multiplier = 1;
            }
            else {
                multiplier = mutatedGenome.getDegrees()[i];
            }
            double randomValue = Math.random();
            if (randomValue<=(mutationrate*multiplier)) {
                mutatedGenome.genome[i]=Math.abs(mutatedGenome.genome[i]-1);
                mutatedGenome.bitFlip(matrix,i);
            }
        }
    }
     */

    protected static void mutation_of_vertices_with_high_degree(float mutationrate, Genome mutatedGenome){
        int multiplier = 0;
        for (int i=0; i<mutatedGenome.length; i++) {
            if(mutatedGenome.getDegrees()[i]==0){
                multiplier = 1;
            }
            else {
                multiplier = mutatedGenome.getDegrees()[i];
            }
            double randomValue = Math.random();
            if (randomValue<=(mutationrate*multiplier)) {
                mutatedGenome.genome[i]=Math.abs(mutatedGenome.genome[i]-1);
                mutatedGenome.bitFlip(i);
            }
        }
    }
}
