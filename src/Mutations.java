import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class Mutations {

    public static int implementedMutationMethods = 2;

    protected static int[] mutation(float mutationrate, Genome g){
        Genome mutatedGenome = g;
        for (int i=0; i<mutatedGenome.length; i++) {
            if (Math.random()<=mutationrate) {
                mutatedGenome.genome[i]=Math.abs(mutatedGenome.genome[i]-1);
            }
        }
        return mutatedGenome.genome;
    }


    //degrees of genome must have been calculated before
    protected static int[] mutation_of_vertices_with_high_degree(float mutationrate, Genome g){
        Genome mutatedGenome = g;
        for (int i=0; i<mutatedGenome.length; i++) {
            if (Math.random()<=(mutationrate*mutatedGenome.getDegrees()[i])) {
                mutatedGenome.genome[i]=Math.abs(mutatedGenome.genome[i]-1);
            }
        }
        return mutatedGenome.genome;
    }
}
