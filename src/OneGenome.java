import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

//Represents A connected Component of a graph
public class OneGenome extends Genome {

    Map<Integer, Integer> orderedMapOfHighestDegrees = new java.util.HashMap<>();

    Map<Integer, Integer> getOrderedMapOfHighestDegrees() {
        return orderedMapOfHighestDegrees;
    }

    //list containing the ids of the neighbours of the nodes in the component
    static Map<Integer,List<Integer>> neighbours = new ConcurrentHashMap<>();

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

        initializeNeighbours();
        calculateDegreesUndirected_Neighborhood(graph, this);
        finalizeNeighbours();


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

        initializeNeighbours();

        calculateDegreesUndirected_Neighborhood(graph, this);

        orderedMapOfHighestDegrees();
        calculateWorstFitnessPossible();
    }

    static Genome calculateDegreesUndirected_Neighborhood(int[][] matrix, Genome g) {
        for (int i = 0; i < g.length; i++) {
            if (g.genome[i] == 1) {
                for (int j = i + 1; j < g.length; j++) {
                    if (g.genome[j] == 1 && matrix[i][j] == 1) {
                        g.degrees[i]++;
                        g.degrees[j]++;

                        //add the neighbour to the list of neighbours
                        neighbours.get(i).add(j);
                        neighbours.get(j).add(i);
                    }
                }
            }
        }
        return g;
    }

    void initializeNeighbours() {
        for (int i = 0; i < length; i++) {
            neighbours.put(i, new LinkedList<>());
        }
    }

    //after every Neighbourhood is calculated make all the linked Lists to array Lists
    void finalizeNeighbours() {
        for (Map.Entry<Integer, List<Integer>> entry : neighbours.entrySet()) {
            entry.setValue(new ArrayList<>(entry.getValue()));
        }
        /*
        //print the neighbours for debugging
        for (Map.Entry<Integer, List<Integer>> entry : neighbours.entrySet()) {
            System.out.println("Node " + entry.getKey() + " Neighbours: " + entry.getValue());
        }
         */
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