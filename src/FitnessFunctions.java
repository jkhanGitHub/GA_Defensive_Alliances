import java.util.Arrays;
import java.util.Comparator;

public class FitnessFunctions {
    /*static int calculateFitness(Genome genome){
        int sum = 0;
        for(int i=0; i<genome.length;i++){
            if(genome.genome[i]==1){
                sum += Math.min(0,(2*genome.degrees[i])+1-genome.parentGraph.degrees[i]);
            }
        }
        return sum;
    }*/

    /*static int calculate_abs_Fitness(Genome genome){
        int sum = 0;
        for(int i=0; i<genome.length;i++){
            if(genome.genome[i]==1){
                sum += Math.min(0,(2*genome.degrees[i])+1-genome.parentGraph.degrees[i]);
            }
        }
        return Math.abs(sum);
    }*/

    //ignore nur Ansatz
    static int cFitness(Genome genome, Genome PARENT_GRAPH){
        double sum = 0;
        for(int i=0; i<genome.length;i++){
            if(genome.genome[i]==1){
                double fewNeighbors = (double) (PARENT_GRAPH.getSize() - genome.degrees[i]) /PARENT_GRAPH.getSize();
                sum += fewNeighbors;
            }
        }
        int sizeFactor = PARENT_GRAPH.getSize()- genome.getSize();
        double fitness = sum * sizeFactor;
        return (int) fitness;
    }

    static int calculateFitness(Genome genome, Genome PARENT_GRAPH){
        int sum = 0;
        for(int i=0; i<genome.length;i++){
            if(genome.genome[i]==1){
                int x = (2*genome.degrees[i])+1-PARENT_GRAPH.degrees[i];
                sum += Math.min(0,(x));
            }
        }
        if(sum == 0){
            sum = 1 * (genome.length- genome.getSize());
        }
        else {
            sum = sum * (genome.length-genome.getSize());
        }
        return sum;
    }

    static public int calculate_Population_fitness(Population population) {
        int sum =  0;
        for (Genome genom:
                population.population) {
            sum += genom.fitness;
        };
        return sum;
    }

    static public int calculate_Mean_fitness(Population population){
        return population.population_fitness/population.population.length;
    }

}
