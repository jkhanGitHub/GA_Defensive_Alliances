import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

//Represents Subgraph with graph Ids corresponding to Array entries
// All genomes also calcuilate their Fitness on init, but not their fitness

public class Genome {

    int[] genome;

    int[] degrees;

    int size;
    int length;

    public void setFitness(int fitness) {
        this.fitness = fitness;
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


    Genome(){}
    protected Genome(int numberOfNodes,float existenceRate,int [][] graph){
        length = numberOfNodes;
        genome = new int[length];
        degrees = new int[length];

        generate_genome(existenceRate);
        calculateSize();

        init_degrees();
        calculateDegrees(graph);
    }

    protected Genome(int numberOfNodes,int[] genetic_data,int [][] graph){
        length = numberOfNodes;
        genome = genetic_data;
        degrees = new int[length];

        calculateSize();

        init_degrees();
        calculateDegrees(graph);
    }


    protected Genome(int numberOfNodes,int[] genetic_data,int [][] graph, float mutationrate){
        length = numberOfNodes;
        genome = genetic_data;
        degrees = new int[length];

        calculateSize();

        init_degrees();
        this.genome = Mutations.mutation(mutationrate,this);
        calculateDegrees(graph);
    }



    void calculateSize(){
        size =  Arrays.stream(genome).sum();
    }

    void calculateDegrees(int[][] matrix){
        for (int i = 0; i < length; i++) {
            if(genome[i]==1){
                for (int j = 0; j < length; j++) {
                    if(genome[j]==1 && matrix[i][j]==1){
                        degrees[i]++;
                        degrees[j]++;
                    }
                }
            }
        }
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

    void readEdges_off_symmetrical_Matrix(int[][] matrix){
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                if(matrix[i][j]==1){
                    degrees[i]++;
                    degrees[j]++;
                }
            }
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

}
