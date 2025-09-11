import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

//Represents A connected Component of a graph
public class OneGenome extends Genome {

    public static Map<Integer, Integer> orderedMapOfHighestDegrees = new java.util.HashMap<>();

    public static HashSet<Integer> Ids_toFilter = new HashSet<>(); //holds the ids of the nodes that cannot be in a defensive alliance of size k

    //list containing the ids of the neighbours of the nodes in the component
    static Map<Integer,List<Integer>> neighbours = new ConcurrentHashMap<>();

    static int maximumDegreeinAllianceOfSizeK = 0; //maximum degree in a defensive alliance of size k

    //holds the ids of the nodes in the component
    int[] component;
    int[][] graph;
    //worstFitnessPossible is not actually possible but a good supremum
    static int worstFitnessPossible;

    OneGenome(int numberOfNodes,int[][] graph) {
        length = numberOfNodes;
        chromosome = new int[length];
        degrees = new int[length];
        this.graph = graph;

        generate_genome();

        initializeNeighbours();
        calculateDegreesUndirected_Neighborhood(graph, this);
        finalizeNeighbours();


        orderedMapOfHighestDegrees();
        calculateWorstFitnessPossible();
    }

    OneGenome(int numberOfNodes,int[][] graph, int SIZE_OF_DEFENSIVE_ALLIANCE) {
        length = numberOfNodes;
        chromosome = new int[length];
        degrees = new int[length];
        this.graph = graph;
        maximumDegreeinAllianceOfSizeK = (2 * SIZE_OF_DEFENSIVE_ALLIANCE) + 1;

        generate_genome();

        initializeNeighbours();
        calculateDegreesUndirected_Neighborhood(graph, this);
        finalizeNeighbours();


        find_Ids_To_Filter(SIZE_OF_DEFENSIVE_ALLIANCE);
        orderedMapOfHighestDegrees();
        calculateWorstFitnessPossible();
    }

    /* this code was used in a previous version but now the algorithm expects a connected graph from the get go,
     also the code is outdated and doesnt work anymore
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
     */

    /*outdated code
    OneGenome(int[] component, int[] degreesOfComponent, int[][]adjzMatrix, int SIZE_OF_DEFENSIVE_ALLIANCE){
        this.component = component;
        length = component.length;
        genome = new int[length];
        degrees = degreesOfComponent;
        graph = new int[length][length];
        maximumDegreeinAllianceOfSizeK = (2 * SIZE_OF_DEFENSIVE_ALLIANCE) + 1;

        generate_genome();

        generate_MiniGraph(adjzMatrix,component);

        initializeNeighbours();

        calculateDegreesUndirected_Neighborhood(graph, this);

        find_Ids_To_Filter(SIZE_OF_DEFENSIVE_ALLIANCE);
        orderedMapOfHighestDegrees();
        calculateWorstFitnessPossible();
    }

     */

    static Genome calculateDegreesUndirected_Neighborhood(int[][] matrix, Genome g) {
        for (int i = 0; i < g.length; i++) {
            if (g.chromosome[i] == 1) {
                for (int j = i + 1; j < g.length; j++) {
                    if (g.chromosome[j] == 1 && matrix[i][j] == 1) {
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


    //this fitness is not actually achievable since there was a change, resulting in searching for a alliance of specific size
    void calculateWorstFitnessPossible(){
        worstFitnessPossible = Arrays.stream(degrees).sum();
    }

    void generate_genome() {
        for (int i = 0; i < length; i++) {
            chromosome[i] = 1;
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

    /*
    used to be useful in a previous version in which we didnt expect a fully connected graph as argument

    void generate_MiniGraph(int[][] adjzMatrix,int[]component){
        for (int i = 0; i < length; i++) {
            graph[i] = adjzMatrix[component[i]];
        }
    }

     */

    //filter genomes that can not be in a defensive alliance of size k because their degree is too high
    HashSet<Integer> find_Ids_To_Filter(int k) {
        int max_degree = (2*k)+1;
        HashSet<Integer> Ids_toFilter = new HashSet<>();
        
        for(int i = 0; i<length; i++) {
            if (degrees[i] >= max_degree) {
                Ids_toFilter.add(i);
            }
        }

        System.out.println("Ids to filter: " + Ids_toFilter);
        //print amount of nodes to filter
        System.out.println("Amount of nodes to filter: " + Ids_toFilter.size());

        return Ids_toFilter;
    }

}