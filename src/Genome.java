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


    int[] genome;

    int[] degrees;

    int size;
    int length;

    public void setFitness(int fitness) {
        this.fitness = fitness;
        positiveFitness = OneGenome.worstFitnessPossible + fitness;
    }

    int fitness;

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
    protected Genome(int numberOfNodes, float existenceRate, int[][] graph) {
        length = numberOfNodes;
        genome = new int[length];
        degrees = new int[length];

        generate_genome(existenceRate);
        calculateSize();

        init_degrees();
    }

    protected Genome(int[] genetic_data) {
        length = genetic_data.length;
        genome = genetic_data;
        degrees = new int[length];

        init_degrees();
    }


    //for the complement genome
    protected Genome(int[] genetic_data, boolean complement) {
        length = genetic_data.length;;
        genome = complement(genetic_data);
        degrees = new int[length];

        init_degrees();
    }

    //needed for DeepCopy never heard about it before me neither :)))))))))))) FUCKING BULLSHIT NEEDED DAYS TO BE FOUND
    protected Genome(Genome genome) {
        this.genome = Arrays.copyOf(genome.genome, genome.genome.length);
        this.degrees = Arrays.copyOf(genome.degrees, genome.degrees.length);
        this.positiveFitness = genome.positiveFitness;
        this.fitness = genome.fitness;
        this.size = genome.size;
        this.length = genome.length;
    }



    void calculateSize(){
        size =  Arrays.stream(genome).sum();
    }


    //calculate the degrees of the genome in an undirected graph
    static Genome calculateDegreesUndirected(int[][] matrix, Genome g){
        for (int i = 0; i < g.length; i++) {
            if(g.genome[i]==1){
                for (int j = i+1; j < g.length; j++) {
                    if(g.genome[j]==1 && matrix[i][j]==1){
                        g.degrees[i]++;
                        g.degrees[j]++;
                    }
                }
            }
        }
        return g;
    }

    //calculate the degrees of the genome in a directed graph
    static Genome calculateDegreesDirected(int[][] matrix,Genome g){
        for (int i = 0; i < g.length; i++) {
            if(g.genome[i]==1){
                for (int j = 0; j < g.length; j++) {
                    if(g.genome[j]==1 && matrix[i][j]==1){
                        g.degrees[i]++;
                    }
                }
            }
        }
        return g;
    }

    /*
    //parallelized version of calculateDegrees
    static Genome calculateDegrees(int[][] matrix, Genome g) {
        IntStream.range(0, g.length).parallel().forEach(i -> {
            if (g.genome[i] == 1) {
                for (int j = 0; j < g.length; j++) {
                    if (g.genome[j] == 1 && matrix[i][j] == 1) {
                        synchronized (g.degrees) {
                            g.degrees[i]++;
                            g.degrees[j]++;
                        }
                    }
                }
            }
        });
        return g;
    }
     */

    int[] complement(int[] genome) {
        int[] complement = new int[genome.length];
        for (int i = 0; i < genome.length; i++) {
            complement[i] = Math.abs(genome[i] - 1);
        }
        return complement;
    }

    void init_degrees(){
        for(int i=0; i<length; i++){
            degrees[i]=0;
        }
    }

    void generate_genome(float existenceRate){
        for(int i=0; i<length; i++){
            if(Math.random()<=existenceRate){
                genome[i] = 1;
            }
            else genome[i] = 0;
        }
    }

    void printGenome(){
        System.out.println(Arrays.toString(genome));
    }

    boolean isDefensiveAlliance(OneGenome parent){
        int sum = 0;
        for(int i=0; i<genome.length;i++){
            if(genome[i]==1){
                int control = Math.min(0,(2*degrees[i])+1-parent.degrees[i]);
                sum += control;
            }
        }
        return sum==0;
    }


    //find the edges in the graph with the highest degree and check against the degree of the subgraph(genmome)
    static Map<Integer, Integer> orderedMapOf_harmfulNodes(OneGenome parent_graph, Genome subgraph){
        int difference; //initialize the array with the length of the parent graph to store the difference in degrees
        int relativeFitness;
        Map<Integer, Integer> mapWithRelativeFitnessOfNode_And_OriginalPosition = new HashMap<>(); //create a map to store the index and value of the difference

        for (int i = 0; i < subgraph.genome.length; i++) {
            if(subgraph.genome[i]==1){
                difference = parent_graph.getDegrees()[i] - subgraph.getDegrees()[i] ; //calculate the difference in degrees between the parent graph and the subgraph; Higher value means bigger difference
                relativeFitness = Math.abs(subgraph.getDegrees()[i] - difference); //the bigger the worse
                mapWithRelativeFitnessOfNode_And_OriginalPosition.put(i, relativeFitness); //put the index and value of the difference in the map
                }
            else{
                relativeFitness = 0;
                mapWithRelativeFitnessOfNode_And_OriginalPosition.put(i, relativeFitness);
            }
        }

        //sort the map by value
        Map<Integer, Integer> sortedMap = mapWithRelativeFitnessOfNode_And_OriginalPosition.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // Merge function (not needed here as keys are unique)
                        LinkedHashMap::new // Use LinkedHashMap to preserve the sorted order
                ));


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
        Genome temp;
        temp = Learning.remove_many_harmful_Nodes(genome, parentGraph, numberOfChanges);
        temp = Learning.test_high_degree_vertices_mutation(temp, numberOfChanges, parentGraph);

        temp = Genome.calculateDegreesUndirected(parentGraph.graph, temp);
        temp.calculateSize();
        temp.setFitness(FitnessFunctions.calculateFitnessMIN(temp, parentGraph));
        return temp;
    }

    //i feel like java takes a shortcut here and compares object ids
    //in order to prevent this bullshit we need to copy the arrey?
    static int difference(Genome a, Genome b){
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
            if (parentGraph.degrees[i]==0){
                genome.genome[i]=0;
            }
        }
        return genome;
    }

}

