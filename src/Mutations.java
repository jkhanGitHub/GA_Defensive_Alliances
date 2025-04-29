import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class Mutations {

    public static int implementedMutationMethods = 4;

    protected static int[] mutation(float mutationrate, Genome g){
        Genome mutatedGenome = g;
        for (int i=0; i<mutatedGenome.length; i++) {
            if (Math.random()<=mutationrate) mutatedGenome.genome[i]=Math.abs(mutatedGenome.genome[i]-1);
        }
        return mutatedGenome.genome;
    }


    //degrees of genome must have been calculated before
    protected static int[] mutation_of_vertices_with_high_degree(float mutationrate, Genome g){
        Genome mutatedGenome = g;
        for (int i=0; i<mutatedGenome.length; i++) {
            if (Math.random()<=(mutationrate*mutatedGenome.getDegrees()[i])) mutatedGenome.genome[i]=Math.abs(mutatedGenome.genome[i]-1);
        }
        return mutatedGenome.genome;
    }


    protected static Genome test_high_degree_vertices_mutation(Genome g, int numberOfVerticesTested, OneGenome parentGraph){
        Genome mutatedGenome = g;
        Iterator<Map.Entry<Integer, Integer>> iterator = parentGraph.getOrderedMapOfHighestDegrees().entrySet().iterator();

        boolean bool = true;
        Random random = new Random();

        int numberOfNodes = random.nextInt(numberOfVerticesTested+1);

        while (iterator.hasNext() && bool && numberOfNodes > 0) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            int index = entry.getKey();
            if(mutatedGenome.getGenome()[index] == 0){
               mutatedGenome.getGenome()[index] = 1;

                //Experimental from Here ---------------------------------------------------------------------------
                //calculate the degrees of the mutated genome
                Genome.calculateDegrees(Genetic_Algorithm.graph, mutatedGenome);

                //calculate the fitness of the mutated genome
                mutatedGenome.setFitness(FitnessFunctions.calculateFitnessMIN(mutatedGenome, parentGraph));

                //reject the mutated genome if it is worse than the original genome
                if (mutatedGenome.getFitness()< g.getFitness()){
                    mutatedGenome.getGenome()[index] = 0;
                }
                //to Here ---------------------------------------------------------------------------

            }
            else {
                continue;
            }
            numberOfVerticesTested--;
        }

        return mutatedGenome;
    }

    //parent.getOrderedMapOfHighestDegrees().entrySet().iterator(); in iterator eintragen
    protected static Genome remove_harmfulNode(Genome subgraph, OneGenome parent){
        //remove the node with the highest degree
        Map<Integer, Integer> map = Genome.orderedMapOf_harmfulNodes(parent, subgraph);
        Iterator<Map.Entry<Integer, Integer>> iterator = map.entrySet().iterator();
        //get the first entry
        Map.Entry<Integer, Integer> entry = iterator.next();
        int key = entry.getKey(); //index of the node
        int value = entry.getValue(); //difference in degrees; degree change after Operation

        if (value > 0) {
            //remove the node from the subgraph
            subgraph.getGenome()[key] = 0;
        }
        else return subgraph;

        return subgraph;
    }

    protected static Genome remove_many_harmful_Nodes(Genome subgraph, OneGenome parent, int numberOfNodesToRemove){
        //remove the node with the highest degree
        Genome mutatedGenome = subgraph;

        Random random = new Random();

        int numberOfNodes = random.nextInt(numberOfNodesToRemove+1);

        Map<Integer, Integer> map = Genome.orderedMapOf_harmfulNodes(parent, subgraph);
        Iterator<Map.Entry<Integer, Integer>> iterator = map.entrySet().iterator();

        for (int i = 0; i < numberOfNodes; i++) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            int key = entry.getKey(); //index of the node
            int value = entry.getValue(); //difference in degrees; degree change after Operation

            if (value > 0) {
                //remove the node from the subgraph
                mutatedGenome.getGenome()[key] = 0;
            }
        }

        /*//Experimental from Here ---------------------------------------------------------------------------
        //calculate the degrees of the mutated genome
        Genome.calculateDegrees(Genetic_Algorithm.graph, mutatedGenome);

        //calculate the fitness of the mutated genome
        mutatedGenome.setFitness(FitnessFunctions.calculateFitness(mutatedGenome, parent));

        //reject the mutated genome if it is worse than the original genome
        if (mutatedGenome.getFitness()< subgraph.getFitness()){
            return subgraph;
        }
        //to Here ---------------------------------------------------------------------------*/


        return mutatedGenome;
    }

}
