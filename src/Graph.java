import java.io.IOException;
import java.util.*;

//used to find connected components in a graph
class Graph {
    protected int[][] adjMatrix;
    protected int length;
    protected int[] degrees;

    List<List<Integer>> connected_components = new ArrayList<>();

    Map<int[],int[]> componetsWithDegrees = new HashMap<>();

    public Graph(int numNodes) {
        length = numNodes;
        adjMatrix = new int[numNodes][numNodes]; // Initialized to 0 by default
    }

    public Graph(int[][] adjMatrix) {
        length = adjMatrix.length;
        this.adjMatrix = adjMatrix; // Initialized to 0 by default
        degrees = new int[length];

        calculateDegreesUndirected();
        connected_components = findComponentsBFS();
        //sort components by size
        connected_components.sort((a, b) -> Integer.compare(b.size(), a.size()));

        generate_ComponentsWithDegrees();
    }

    // Add an undirected edge between u and v
    public void addEdge(int u, int v) {
        adjMatrix[u][v] = 1;
        adjMatrix[v][u] = 1; // Symmetric for undirected graphs
    }

    // Find all connected components using BFS
    public List<List<Integer>> findComponentsBFS() {
        boolean[] visited = new boolean[length];
        List<List<Integer>> components = new ArrayList<>();

        for (int node = 0; node < length; node++) {
            if (!visited[node]) {
                List<Integer> component = new ArrayList<>();
                Queue<Integer> queue = new LinkedList<>();
                queue.add(node);
                visited[node] = true;

                while (!queue.isEmpty()) {
                    int current = queue.poll();
                    component.add(current);

                    // Check all nodes to find neighbors (via adjacency matrix)
                    for (int neighbor = 0; neighbor < length; neighbor++) {
                        if (adjMatrix[current][neighbor] == 1 && !visited[neighbor]) {
                            visited[neighbor] = true;
                            queue.add(neighbor);
                        }
                    }
                }
                components.add(component);
            }
        }
        return components;
    }

    //methods that turns IntegerLists into int arrays
    public static int[] convertIntegerListToIntArray(List<Integer> integerList) {
        return integerList.stream().mapToInt(Integer::intValue).toArray();
    }

    void generate_ComponentsWithDegrees(){
        for (List<Integer>component: connected_components) {
            int[] componentArray = convertIntegerListToIntArray(component);
            int[] degreesOfComponent = degreesOfComponent(component);
            componetsWithDegrees.put(componentArray,degreesOfComponent);
        }
    }
    void calculateDegreesUndirected() {
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                if (adjMatrix[i][j]==1){
                    degrees[i]++;
                    degrees[j]++;
                }
            }
        }
    }

     int[] degreesOfComponent(List<Integer> connectedComponent) {
        int[] degreesOfComponent = new int[connectedComponent.size()];
        for (int i=0; i< connectedComponent.size(); i++) {
            int id = connectedComponent.get(i);
            degreesOfComponent[i] = degrees[id];
        }
        return degreesOfComponent;
    }

    public static void main(String[] args) {
        int[][] adjzMatrix;
        int numberOfNodes = 28281;
        final String FILEPATH = "deezer_europe/deezer_europe/deezer_europe_edges.csv";
        Graph graph;

        try {
            adjzMatrix = CsvReader.readCsvEdgesToSymmetricalMatrix(FILEPATH, numberOfNodes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        graph = new Graph(adjzMatrix);
        System.out.println("!amount of connected components: " + graph.connected_components.size());
    }
}


