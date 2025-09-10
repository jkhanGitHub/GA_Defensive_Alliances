import java.util.*;

public class Learning {
    public static final int implentedmethods = 2;

    static Dictionary learnMethods = new Hashtable();
    static {
        learnMethods.put(2, "try to add vertices with high degree");
        learnMethods.put(3, "Remove harmful nodes");
    }

    //tries to add nodes with high degree to the genome if they are not already in the genome and not in the filter list e.g it could possibly be in a defensive alliance of searched size
    protected static List<Integer> add_test_high_degree_vertices_mutation(Genome mutatedGenome, int numberOfNodesToRemove, OneGenome parentGraph, int SIZE_OF_DEFENSIVE_ALLIANCE) {

        //initialize List in which all the changed alleles are stored
        List<Integer> changedAllele = new ArrayList<>();

        Iterator<Map.Entry<Integer, Integer>> iterator = parentGraph.getOrderedMapOfHighestDegrees().entrySet().iterator();
        int oldFitness = mutatedGenome.getFitness();

        while (iterator.hasNext() && numberOfNodesToRemove > 0) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            int index = entry.getKey();
            // check if the node is not already in the genome and also not in the filter list e.g it could possibly be in a defensive alliance of searched size
            if(mutatedGenome.getChromosome()[index] == 0 && !parentGraph.Ids_toFilter.contains(index)) {

                //Experimental from Here ---------------------------------------------------------------------------
                //calculate the degrees of the mutated genome
                mutatedGenome.addNode(index);

                //calculate the fitness of the mutated genome
                int newFitness = FitnessFunctions.calculateFitnessMIN(mutatedGenome, parentGraph, SIZE_OF_DEFENSIVE_ALLIANCE);

                //reject the mutated genome if it is worse than the original genome
                if (oldFitness > newFitness){
                    mutatedGenome.removeNode(index);
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

    // this method only removes one node  the idea was the call this method multiple times if needed but now only remove_many_harmful_Nodes is used
    //parent.getOrderedMapOfHighestDegrees().entrySet().iterator(); in iterator eintragen
    protected static int remove_harmfulNode(Genome subgraph, OneGenome parent){
        //remove the node with the highest degree
        Map<Integer, Integer> map = subgraph.harmfulNodes;
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
        subgraph.removeNode(key);

        return key;
    }

    //remove the node with the highest degree
    protected static List<Integer> remove_many_harmful_Nodes(Genome subgraph, OneGenome parent, int numberOfNodesToRemove,int SIZE_OF_DEFENSIVE_ALLIANCE){
        //initialize List in which all the changed alleles are stored
        List<Integer> changedAllele = new ArrayList<>();

        //remove the node with the highest degree

        Map<Integer, Integer> map = subgraph.harmfulNodes;
        Iterator<Map.Entry<Integer, Integer>> iterator = map.entrySet().iterator();

        for (int i = 0; i < numberOfNodesToRemove && iterator.hasNext(); i++) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            int key = entry.getKey(); //index of the node
            int value = entry.getValue(); //difference in degrees; degree change after Operation

            //remove the node from the subgraph
            subgraph.removeNode(key);
        }

        //Experimental from Here ---------------------------------------------------------------------------
        //calculate the degrees of the mutated genome


        //calculate the fitness of the mutated genome
        subgraph.setFitness(FitnessFunctions.calculateFitnessMIN(subgraph, parent, SIZE_OF_DEFENSIVE_ALLIANCE));

        /*//reject the mutated genome if it is worse than the original genome
        if (subgraph.getFitness()< subgraph.getFitness()){
            return changedAllele;
        }
        //to Here ---------------------------------------------------------------------------*/


        return changedAllele;
    }

}
