import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

//Represents whole Graph
public class OneGenome extends Genome {

    OneGenome(int numberOfNodes,int[][] graph) {
        length = numberOfNodes;
        genome = new int[length];
        degrees = new int[length];

        generate_genome();
        calculateSize();

        init_degrees();
        readEdges_off_symmetrical_Matrix(graph);
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

    public int[] getDegrees() {
        return degrees;
    }
}