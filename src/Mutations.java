import java.util.*;

public class Mutations {

    public static int implementedMutationMethods = 2;

    protected static List<Integer> mutation(float mutationrate, Genome mutatedGenome){
        List<Integer> changedAllele = new ArrayList<>();
        for (int i=0; i<mutatedGenome.length; i++) {
            if (Math.random()<=mutationrate) {
                mutatedGenome.genome[i]=Math.abs(mutatedGenome.genome[i]-1);
                changedAllele.add(i);
            }
        }
        return changedAllele;
    }


    //degrees of genome must have been calculated before
    protected static List<Integer> mutation_of_vertices_with_high_degree(float mutationrate, Genome mutatedGenome){
        List<Integer> changedAllele = new ArrayList<>();
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
                changedAllele.add(i);
            }
        }
        return changedAllele;
    }
}
