public class Mutations {

    protected static int[] mutation(float mutationrate, Genome g){
        Genome mutatedGenome = g;
        for (int i: mutatedGenome.genome) {
            if (Math.random()<=mutationrate) i=Math.abs(i-1);
        }
        return mutatedGenome.genome;
    }
}
