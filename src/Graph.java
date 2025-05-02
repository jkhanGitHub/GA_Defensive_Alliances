import java.io.IOException;
import java.util.*;

//used to find connected components in a graph
class Graph extends Genome{
    private int[][] adjMatrix;

    public Graph(int numNodes) {
        length = numNodes;
        adjMatrix = new int[numNodes][numNodes]; // Initialized to 0 by default
    }

    public Graph(int numberOfNodes, int[][] adjMatrix) {
        length = numberOfNodes;
        this.adjMatrix = adjMatrix; // Initialized to 0 by default
        genome = new int[length];
        degrees = new int[length];
        generate_genome();
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

    void generate_genome() {
        for (int i = 0; i < length; i++) {
            genome[i] = 1;
            calculateSize();
        }
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

        graph = new Graph(numberOfNodes, adjzMatrix);
        int[] degrees = new int[numberOfNodes];

        List<List<Integer>> components = graph.findComponentsBFS();
        System.out.println("!amount of connected components: " + components.size());
    }
}


