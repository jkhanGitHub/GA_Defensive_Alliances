import java.util.*;

public class Learning {
    public static final int implentedmethods = 2;

    static Dictionary learnMethods = new Hashtable();
    static {
        learnMethods.put(2, "try to add vertices with high degree");
        learnMethods.put(3, "Remove harmful nodes");
    }

    protected static List<Integer> add_test_high_degree_vertices_mutation(Genome mutatedGenome, int numberOfNodesToRemove, OneGenome parentGraph){

        //initialize List in which all the changed alleles are stored
        List<Integer> changedAllele = new ArrayList<>();

        Iterator<Map.Entry<Integer, Integer>> iterator = parentGraph.getOrderedMapOfHighestDegrees().entrySet().iterator();
        int oldFitness = mutatedGenome.getFitness();

        while (iterator.hasNext() && numberOfNodesToRemove > 0) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            int index = entry.getKey();
            if(mutatedGenome.getGenome()[index] == 0){

                //Experimental from Here ---------------------------------------------------------------------------
                //calculate the degrees of the mutated genome
                mutatedGenome.addNode(parentGraph.graph, index);

                //calculate the fitness of the mutated genome
                int newFitness = FitnessFunctions.calculateFitnessMIN(mutatedGenome, parentGraph);

                //reject the mutated genome if it is worse than the original genome
                if (oldFitness > newFitness){
                    mutatedGenome.removeNode(parentGraph.graph, index);
                    continue;
                }
                //to Here ---------------------------------------------------------------------------
                //Update fitness
                mutatedGenome.setFitness(newFitness);
                oldFitness = newFitness;
                numberOfNodesToRemove--;
                changedAllele.add(index);
            }
        }

        return changedAllele;
    }

    //parent.getOrderedMapOfHighestDegrees().entrySet().iterator(); in iterator eintragen
    protected static int remove_harmfulNode(Genome subgraph, OneGenome parent){
        //remove the node with the highest degree
        Map<Integer, Integer> map = subgraph.orderedMapOfHarmfulNodes(parent);
        Iterator<Map.Entry<Integer, Integer>> iterator = map.entrySet().iterator();

        //check if the map is empty
        if (!iterator.hasNext()) {
            return -1; // No harmful nodes to remove, return invalid integer
        }
        //get the first entry in the map
        Map.Entry<Integer, Integer> entry = iterator.next();
        int key = entry.getKey(); //index of the node
        int value = entry.getValue(); //difference in degrees; degree change after Operation

        //remove the node from the subgraph
        subgraph.getGenome()[key] = 0;

        return key;
    }

    //remove the node with the highest degree
    protected static List<Integer> remove_many_harmful_Nodes(Genome subgraph, OneGenome parent, int numberOfNodesToRemove){
        //initialize List in which all the changed alleles are stored
        List<Integer> changedAllele = new ArrayList<>();

        //remove the node with the highest degree

        Map<Integer, Integer> map = subgraph.orderedMapOfHarmfulNodes(parent);
        Iterator<Map.Entry<Integer, Integer>> iterator = map.entrySet().iterator();

        for (int i = 0; i < numberOfNodesToRemove && iterator.hasNext(); i++) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            int key = entry.getKey(); //index of the node
            int value = entry.getValue(); //difference in degrees; degree change after Operation

            //remove the node from the subgraph
            subgraph.removeNode(parent.graph, key);
        }

        //Experimental from Here ---------------------------------------------------------------------------
        //calculate the degrees of the mutated genome


        //calculate the fitness of the mutated genome
        subgraph.setFitness(FitnessFunctions.calculateFitnessMIN(subgraph, parent));

        /*//reject the mutated genome if it is worse than the original genome
        if (subgraph.getFitness()< subgraph.getFitness()){
            return changedAllele;
        }
        //to Here ---------------------------------------------------------------------------*/


        return changedAllele;
    }

}
