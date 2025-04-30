import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

//Represents whole Graph
public class OneGenome extends Genome {

    Map<Integer, Integer> orderedMapOfHighestDegrees = new java.util.HashMap<>();

    Map<Integer, Integer> getOrderedMapOfHighestDegrees() {
        return orderedMapOfHighestDegrees;
    }

    int[][] graph;
    //worstFitnessPossible is not actually possible but a good supremum
    static int worstFitnessPossible;

    OneGenome(int numberOfNodes,int[][] graph) {
        length = numberOfNodes;
        genome = new int[length];
        degrees = new int[length];
        this.graph = graph;

        generate_genome();
        calculateSize();

        init_degrees();
        calculateDegreesUndirected(graph, this);
        orderedMapOfHighestDegrees();
        calculateWorstFitnessPossible();
    }

    void calculateWorstFitnessPossible(){
        worstFitnessPossible = Arrays.stream(degrees).sum();
    }

    void generate_genome() {
        for (int i = 0; i < length; i++) {
            genome[i] = 1;
            calculateSize();
        }
    }

    void remove_isolated_nodes(){
        for (int i = 0; i < length; i++) {
            if (degrees[i]==0){
                genome[i]=0;
            }
        }
    }

    //give a map with the highest degrees and where to find them
    void orderedMapOfHighestDegrees(){
        for (int i = 0; i < degrees.length; i++) {
            orderedMapOfHighestDegrees.put(i, degrees[i]);
        }

        orderedMapOfHighestDegrees = orderedMapOfHighestDegrees.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // Merge function (not needed here as keys are unique)
                        LinkedHashMap::new // Use LinkedHashMap to preserve the sorted order
                ));
    }


    public int[] getDegrees() {
        return degrees;
    }
}