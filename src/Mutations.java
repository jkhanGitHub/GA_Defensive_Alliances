public class Mutations {

    protected static int[] mutation(float mutationrate, Genome g){
        Genome mutatedGenome = g;
        for (int i: mutatedGenome.genome) {
            if (Math.random()<=mutationrate) i=Math.abs(i-1);
        }
        return mutatedGenome.genome;
    }
<<<<<<< HEAD
=======

    protected static int[] mutation_of_vertices_with_high_degree(Genome g){
        Genome mutatedGenome = g;
        for (int i: mutatedGenome.genome) {
            if (Math.random()<=mutatedGenome.getDegrees()[i]/g.length) i=Math.abs(i-1);
        }
        return mutatedGenome.genome;
    }
>>>>>>> 7f7850c (init commit -m "easier to change now")
}
