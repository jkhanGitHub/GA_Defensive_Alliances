import java.util.*;

public class Learning {
    public static final int implentedmethods = 2;

    static Dictionary learnMethods = new Hashtable();
    static {
        learnMethods.put(2, "try to add vertices with high degree");
        learnMethods.put(3, "Remove harmful nodes");
    }

    protected static Genome test_high_degree_vertices_mutation(Genome mutatedGenome, int numberOfNodesToRemove, OneGenome parentGraph){

        Iterator<Map.Entry<Integer, Integer>> iterator = parentGraph.getOrderedMapOfHighestDegrees().entrySet().iterator();
        int oldFitness = mutatedGenome.getFitness();

        while (iterator.hasNext() && numberOfNodesToRemove > 0) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            int index = entry.getKey();
            if(mutatedGenome.getGenome()[index] == 0){
                mutatedGenome.getGenome()[index] = 1;

                //Experimental from Here ---------------------------------------------------------------------------
                //calculate the degrees of the mutated genome
                Genome.calculateDegreesUndirected(Genetic_Algorithm.graph, mutatedGenome);

                //calculate the fitness of the mutated genome
                int newFitness = FitnessFunctions.calculateFitnessMIN(mutatedGenome, parentGraph);

                //reject the mutated genome if it is worse than the original genome
                if (oldFitness > newFitness){
                    mutatedGenome.getGenome()[index] = 0;
                    continue;
                }
                //to Here ---------------------------------------------------------------------------
                //Update fitness
                mutatedGenome.setFitness(newFitness);
                oldFitness = newFitness;
                numberOfNodesToRemove--;

            }
        }

        return mutatedGenome;
    }

    //parent.getOrderedMapOfHighestDegrees().entrySet().iterator(); in iterator eintragen
    protected static Genome remove_harmfulNode(Genome subgraph, OneGenome parent){
        //remove the node with the highest degree
        Map<Integer, Integer> map = subgraph.orderedMapOfHarmfulNodes(parent);
        Iterator<Map.Entry<Integer, Integer>> iterator = map.entrySet().iterator();

        //check if the map is empty
        if (!iterator.hasNext()) {
            return subgraph; // No harmful nodes to remove
        }
        //get the first entry in the map
        Map.Entry<Integer, Integer> entry = iterator.next();
        int key = entry.getKey(); //index of the node
        int value = entry.getValue(); //difference in degrees; degree change after Operation

        //remove the node from the subgraph
        subgraph.getGenome()[key] = 0;

        return subgraph;
    }

    //remove the node with the highest degree
    protected static Genome remove_many_harmful_Nodes(Genome subgraph, OneGenome parent, int numberOfNodesToRemove){
        //remove the node with the highest degree
        Genome mutatedGenome = subgraph;

        Map<Integer, Integer> map = subgraph.orderedMapOfHarmfulNodes(parent);
        Iterator<Map.Entry<Integer, Integer>> iterator = map.entrySet().iterator();

        for (int i = 0; i < numberOfNodesToRemove && iterator.hasNext(); i++) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            int key = entry.getKey(); //index of the node
            int value = entry.getValue(); //difference in degrees; degree change after Operation

            //remove the node from the subgraph
            mutatedGenome.getGenome()[key] = 0;
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
