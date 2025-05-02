import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//Represents A connected Component of a graph
public class OneGenome extends Genome {

    Map<Integer, Integer> orderedMapOfHighestDegrees = new java.util.HashMap<>();

    Map<Integer, Integer> getOrderedMapOfHighestDegrees() {
        return orderedMapOfHighestDegrees;
    }

    //holds the ids of the nodes in the component
    int[] component;
    int[][] graph;
    //worstFitnessPossible is not actually possible but a good supremum
    static int worstFitnessPossible;

    OneGenome(int numberOfNodes,int[][] graph) {
        length = numberOfNodes;
        genome = new int[length];
        degrees = new int[length];
        this.graph = graph;

        generate_genome();

        calculateDegreesUndirected(graph, this);
        orderedMapOfHighestDegrees();
        calculateWorstFitnessPossible();
    }

    //component received by Graph.java
    OneGenome(int[] component, int[] degreesOfComponent, int[][]adjzMatrix){
        this.component = component;
        length = component.length;
        genome = new int[length];
        degrees = degreesOfComponent;
        graph = new int[length][length];

        generate_genome();

        generate_MiniGraph(adjzMatrix,component);
        orderedMapOfHighestDegrees();
        calculateWorstFitnessPossible();
    }

    void calculateWorstFitnessPossible(){
        worstFitnessPossible = Arrays.stream(degrees).sum();
    }

    void generate_genome() {
        for (int i = 0; i < length; i++) {
            genome[i] = 1;
        }
        calculateSize();
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

    void remove_isolated_nodes(){
        for (int i = 0; i < length; i++) {
            if (degrees[i]==0){
                size = size-1;
            }
        }
    }

    void generate_MiniGraph(int[][] adjzMatrix,int[]component){
        for (int i = 0; i < length; i++) {
            graph[i] = adjzMatrix[component[i]];
        }
    }

}