import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.*;

//Represents Subgraph with graph Ids corresponding to Array entries
// All genomes also calculate their Fitness on init, but not their fitness

public class Genome {

    public void setGenome(int[] genome) {
        this.genome = genome;
    }

    //if positiveFitness is OneGenome.worstFitnessPossible, then the  genome is a defensive alliance
    double positiveFitness; //the higher, the better

    Genome mother = null;
    Genome father = null;

    Genome dominantParent = null;
    Genome nonDominantParent = null;
    List<Integer> changedAllele = new ArrayList<>();

    int crossoverPoint = 0; //for OnePointcrossover
    Map<Integer, Integer> harmfulNodes;
    int[] genome;

    int[] degrees;

    int size;

    public void setSize(int size) {
        this.size = size;
    }

    int length;

    public void setFitness(int fitness) {
        this.fitness = fitness;
        positiveFitness = OneGenome.worstFitnessPossible + fitness;
    }

    int fitness = 0;

    public int[] getGenome() {
        return genome;
    }

    public int[] getDegrees() {
        return degrees;
    }


    public int getSize() {
        return size;
    }


    public int getFitness() {
        return fitness;
    }


    Genome() {
    }

    //dont remove the graph parameter, it is needed for overloading
    protected Genome(int numberOfNodes, float existenceRate) {
        length = numberOfNodes;
        genome = new int[length];
        degrees = new int[length];

        generate_genome(existenceRate);
        calculateSize();
    }

    protected Genome(int[] genetic_data) {
        length = genetic_data.length;
        genome = genetic_data;
        degrees = new int[length];
    }

    protected Genome(int[] genetic_data, Genome mother, Genome father, int crossoverPoint) {
        length = genetic_data.length;
        genome = genetic_data;
        degrees = new int[length];
        this.mother = mother;
        this.father = father;
        this.crossoverPoint = crossoverPoint;
    }

    protected Genome(int[] genetic_data, Genome mother, Genome father, List<Integer> changedAllele) {
        length = genetic_data.length;
        genome = genetic_data;
        degrees = new int[length];
        this.mother = mother;
        this.father = father;
        this.changedAllele = changedAllele;

        //could be either mother or father doesnt matter when it comes to probability Intersection
        degrees = Arrays.copyOf(mother.degrees, mother.degrees.length);
        size = mother.size;
    }


    //for the complement genome
    protected Genome(int[] genetic_data, boolean complement) {
        length = genetic_data.length;
        ;
        genome = complement(genetic_data);
        degrees = new int[length];
    }

    protected Genome(Genome genome) {
        this.genome = Arrays.copyOf(genome.genome, genome.genome.length);
        this.degrees = Arrays.copyOf(genome.degrees, genome.degrees.length);
        this.positiveFitness = genome.positiveFitness;
        this.fitness = genome.fitness;
        this.size = genome.size;
        this.length = genome.length;
    }


    void calculateSize() {
        size = Arrays.stream(genome).sum();
    }


    //calculate the degrees of the genome in an undirected graph
    static Genome calculateDegreesUndirected(int[][] matrix, Genome g) {
        for (int i = 0; i < g.length; i++) {
            if (g.genome[i] == 1) {
                for (int j = i + 1; j < g.length; j++) {
                    if (g.genome[j] == 1 && matrix[i][j] == 1) {
                        g.degrees[i]++;
                        g.degrees[j]++;
                    }
                }
            }
        }
        return g;
    }


    void updateChildDegrees_crossover(int[][] matrix){
        Genome child = this;

        // Determine dominant parent and segment bounds
        Genome dominantParent = (crossoverPoint > (length / 2)) ? mother : father;
        Genome nonDominantParent = (dominantParent == mother) ? father : mother;


        /*
        //test purpose
        System.arraycopy(mother.getGenome(), 0, child.genome, 0, crossoverPoint + 1);
        System.arraycopy(father.getGenome(), crossoverPoint + 1, child.genome, crossoverPoint + 1, mother.length - (crossoverPoint + 1));
        child.calculateSize();
        */

        // Copy dominant parent's degrees into child (O(n))
        System.arraycopy(dominantParent.degrees, 0, child.degrees, 0, child.length);
        System.arraycopy(dominantParent.genome, 0, child.genome, 0, child.length);
        child.size = dominantParent.size;




        // Define non-dominant segment bounds
        int start = (dominantParent == mother) ? crossoverPoint + 1 : 0;
        int end = (dominantParent == mother) ? child.genome.length : crossoverPoint + 1;

        // First pass: Add nodes and update degrees for dominant segment neighbors

        for (int i = start; i < end; i++) {
            if (nonDominantParent.genome[i] != dominantParent.genome[i]) {
                bitFlip(matrix,i);
            }
        }

        //test purpose
        //child.degrees = new int[child.length];calculateDegreesUndirected(matrix,child);
    }


    //expects nxn matrix; carefull check if node exists
    void removeNode(int[][] matrix,int index){
        int n = length;
        genome[index]=0;
        size--;

        for (int i = 0; i < n; i++) {
            if(matrix[i][index]==1 && genome[i]==1){
                degrees[i]--;
            }
        }
        degrees[index] = 0;
    }

    //expects nxn matrix; carefull check if node exists;
    void addNode(int[][] matrix,int index){

        int n = length;
        genome[index]=1;
        degrees[index] = 0;
        size++;

        for (int i = 0; i < n; i++) {
            if(matrix[i][index]==1 && genome[i]==1){
                degrees[i]++;
                degrees[index]++;
            }
        }
    }

    void bitFlip(int[][] matrix,int index){
        if (genome[index]==1){
            removeNode(matrix,index);
        }
        else addNode(matrix,index);
    }

    void bitUpdate(int[][] matrix,int index){
        if (genome[index]==0){
            removeNode(matrix,index);
        }
        else addNode(matrix,index);
    }


    //calculate the degrees of the genome in a directed graph
    static Genome calculateDegreesDirected(int[][] matrix, Genome g) {
        for (int i = 0; i < g.length; i++) {
            if (g.genome[i] == 1) {
                for (int j = 0; j < g.length; j++) {
                    if (g.genome[j] == 1 && matrix[i][j] == 1) {
                        g.degrees[i]++;
                    }
                }
            }
        }
        return g;
    }

    int[] complement(int[] genome) {
        int[] complement = new int[genome.length];
        for (int i = 0; i < genome.length; i++) {
            complement[i] = Math.abs(genome[i] - 1);
        }
        return complement;
    }

    void generate_genome(float existenceRate) {
        for (int i = 0; i < length; i++) {
            if (Math.random() <= existenceRate) {
                genome[i] = 1;
            } else genome[i] = 0;
        }
    }

    void printGenome() {
        System.out.println(Arrays.toString(genome));
    }

    boolean isDefensiveAlliance(OneGenome parent) {
        int sum = 0;
        for (int i = 0; i < genome.length; i++) {
            if (genome[i] == 1) {
                int control = Math.min(0, (2 * degrees[i]) + 1 - parent.degrees[i]);
                sum += control;
            }
        }
        return sum == 0;
    }


    //find the edges in the graph with the highest degree and check against the degree of the subgraph(genome)
    Map<Integer, Integer> orderedMapOfHarmfulNodes(OneGenome parent_graph) {
        int harmfulnessFitness; //initialize the array with the length of the parent graph to store the difference in degrees
        Map<Integer, Integer> mapWithRelativeFitnessOfNode_And_OriginalPosition = new HashMap<>(); //create a map to store the index and value of the difference

        for (int i = 0; i < genome.length; i++) {
            if (genome[i] == 1) {
                harmfulnessFitness = (2 * degrees[i]) + 1 - parent_graph.degrees[i]; //harmfulness>0 means the node is not harmful, harmfulness<0 means the node is harmful
                if (harmfulnessFitness < 0) { //only add the harmful nodes to the map
                    mapWithRelativeFitnessOfNode_And_OriginalPosition.put(i, harmfulnessFitness); //store the index and value of the difference in the map
                }
            }
        }
        //sort the map by value //the smaller the value the more harmful the node is
        Map<Integer, Integer> sortedMap = mapWithRelativeFitnessOfNode_And_OriginalPosition.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // Merge function (not needed here as keys are unique)
                        LinkedHashMap::new // Use LinkedHashMap to preserve the sorted order
                ));
        harmfulNodes = sortedMap; //store the map in the harmfulNodes variable
        return sortedMap;
    }

    //parallelized version of orderedMapOf_harmfulNodes
    /*static Map<Integer, Integer> orderedMapOf_harmfulNodes(OneGenome parent_graph, Genome subgraph) {
        int length = subgraph.genome.length;
        Map<Integer, Integer> resultMap = new ConcurrentHashMap<>();

        ForkJoinPool forkJoinPool = new ForkJoinPool(); // Create a ForkJoinPool for parallel processing
        forkJoinPool.submit(() -> {
            IntStream.range(0, length).parallel().forEach(i -> {
                int difference = 0;
                if (subgraph.genome[i] == 1) {
                    difference = parent_graph.getDegrees()[i] - subgraph.getDegrees()[i];
                }
                resultMap.put(i, difference);
            });
        }).join();

        // Sort the map by value in descending order
        return resultMap.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }*/


    static Genome learn(Genome genome, OneGenome parentGraph, int numberOfChanges) {
        //TODO implement learning
        int fitness = genome.getFitness();
        int size = genome.getSize();
        List<Integer> changedAllele;

        Learning.add_test_high_degree_vertices_mutation(genome, numberOfChanges, parentGraph); //insanely good method wirth even worse operational time(n^3)*(till numberOfChanges Reached)
        Learning.remove_many_harmful_Nodes(genome, parentGraph, numberOfChanges);


        /*
        //test purpose
        int fitness1 = genome.getFitness();
        int size1 = genome.getSize();


        genome.degrees = new int[genome.length];calculateDegreesUndirected(Genetic_Algorithm.PARENT_GRAPH.graph,genome);
        genome.setSize(0);
        genome.calculateSize();
        FitnessFunctions.calculateFitnessMIN(genome,Genetic_Algorithm.PARENT_GRAPH);

        int fitness2 = genome.getFitness();
        int size2 = genome.getSize();

         */
        return genome;
    }

    static Genome learn_test(Genome genome, OneGenome parentGraph, int numberOfChanges) {
        //TODO implement learning
        int fitness = genome.getFitness();
        int size = genome.getSize();

        Learning.add_test_high_degree_vertices_mutation(genome, numberOfChanges, parentGraph); //insanely good method wirth even worse operational time(n^3)*(till numberOfChanges Reached)
        return genome;
    }

    static Genome learn_remove(Genome genome, OneGenome parentGraph, int numberOfChanges) {
        //TODO implement learning
        int fitness = genome.getFitness();
        int size = genome.getSize();

        Learning.remove_many_harmful_Nodes(genome, parentGraph, numberOfChanges);
        return genome;
    }

    //i feel like java takes a shortcut here and compares object ids
    //in order to prevent this bullshit we need to copy the arrey?
    static int difference(Genome a, Genome b) {
        int sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.abs(a.genome[i] - b.genome[i]);
        }
        return sum;
    }

    //expects parentgraph with removed isolated nodes
    static Genome removeIsolatedNodes(Genome g, OneGenome parentGraph) {
        Genome genome = g;
        for (int i = 0; i < genome.length; i++) {
            if (parentGraph.degrees[i] == 0) {
                genome.genome[i] = 0;
            }
        }
        return genome;
    }

    void remove_isolated_nodes() {
        for (int i = 0; i < length; i++) {
            if (degrees[i] == 0) {
                genome[i] = 0;
            }
        }
    }

    void printParameters() {
        System.out.println("Genome: " + Arrays.toString(genome));
        System.out.println("Degrees: " + Arrays.toString(degrees));
        System.out.println("Size: " + size);
        System.out.println("Fitness: " + fitness);
        System.out.println("Positive Fitness: " + positiveFitness);
    }


    public static void main(String[] args) {
        // Test adjacency matrix (undirected graph)
        int[][] matrix = {
                {0, 1, 1, 0},
                {1, 0, 1, 0},
                {1, 1, 0, 1},
                {0, 0, 1, 0}
        };

        // Parent genomes
        int[] motherGenome = {1, 1, 0, 0}; // Nodes 0 and 1
        int[] fatherGenome = {0, 0, 1, 1}; // Nodes 2 and 3

        Genome mother = new Genome(motherGenome);
        Genome father = new Genome(fatherGenome);

        // Initialize parent degrees manually (for demonstration)
        Genome.calculateDegreesUndirected(matrix,mother);
        Genome.calculateDegreesUndirected(matrix,father);



        /*
        System.out.println("father degrees: "+ Arrays.toString(father.degrees));
        father.bitFlip(matrix,1);
        father.bitFlip(matrix,0);
        father.bitFlip(matrix,2);
        father.bitFlip(matrix,3);
        father.bitFlip(matrix,3);


        System.out.println("father genome: "+ Arrays.toString(father.genome));
        System.out.println("father degrees: "+ Arrays.toString(father.degrees));
        father.degrees  = new int[father.length];
        Genome.calculateDegreesUndirected(matrix,father);
        System.out.println("father degrees correct: "+ Arrays.toString(father.degrees));


        // Initialize parent degrees manually (for demonstration)
        mother.degrees = new int[]{1, 1, 0, 0}; // Degrees for nodes 0 and 1
        father.degrees = new int[]{0, 0, 1, 1}; // Degrees for nodes 2 and 3
        */

        // Create child via crossover at point 1
        int crossoverPoint = 4;
        int[] childGenome = new int[4];

        Genome child = Recombinations.onePointCrossoverSingle(mother,father);
        child.updateChildDegrees_crossover(matrix);
        crossoverPoint = child.crossoverPoint;
        System.out.println(crossoverPoint);
        //Genome child = new Genome(childGenome, mother, father, crossoverPoint);
        // Call addChangedAllele to update degrees
        //child.updateChildDegrees_crossover(matrix);
        int[] test = new int[child.length];
        System.arraycopy(child.degrees,0,test,0,child.length);


        Genome child2 = Recombinations.onePointCrossoverSingle(mother,child);
        child2.updateChildDegrees_crossover(matrix);
        int[] test2 = new int[child2.length];
        System.arraycopy(child2.degrees,0,test2,0,child2.length);

        child.degrees = new int[child.length];Genome.calculateDegreesUndirected(matrix,child);
        child2.degrees = new int[child2.length];Genome.calculateDegreesUndirected(matrix,child2);

        // Expected degrees for child genome [1, 1, 1, 1]
        int[] expectedDegrees = {2, 2, 3, 1}; // Correct degrees for full connectivity
        System.out.println(Arrays.toString(test2));
        System.out.println(Arrays.toString(child2.degrees));
        System.out.println("Test Result: " + Arrays.equals(child2.degrees, test2));
    }

}
