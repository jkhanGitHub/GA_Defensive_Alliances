import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FitnessFunctions {

    //THIS IS THE ONLY ONE THAT IS USED AND MATTERS I COULD DELETE THE REST OTHER two
    static int calculateFitnessMIN(Genome genome, Genome PARENT_GRAPH, int SIZE_OF_DEFENSIVE_ALLIANCE){
        int sum = 0;
        Map<Integer, Integer> mapWithRelativeFitnessOfNode_And_OriginalPosition = new HashMap<>(); //create a map to store the index and value of the difference

        for(int i=0; i<genome.length;i++){
            if(genome.genome[i]==1){
                int x = (2*genome.degrees[i])+1-PARENT_GRAPH.degrees[i];
                sum += Math.min(0,(x));

                if (x<0){
                    mapWithRelativeFitnessOfNode_And_OriginalPosition.put(i, x); //store the index and value of the difference in the map
                }
            }
        }
        genome.harmfulNodes = mapWithRelativeFitnessOfNode_And_OriginalPosition; //store the map in the harmfulNodes variable

        //if sum is 0 then the genome is a defensive alliance
        if(sum == 0){
            sum = (PARENT_GRAPH.getSize() - distanceFunction(SIZE_OF_DEFENSIVE_ALLIANCE,genome.getSize()));
        }

        return sum;
    }

    static int distanceFunction(int mainPoint, int testPoint){
        return Math.abs(mainPoint - testPoint);
    }

    //we can skip the first part of the fitness calculation since we are only interested in the size of the defensive alliance, 
    // since we already know that the genome is a defensive alliance
    static int calculateFitnessForDA(Genome genome, Genome PARENT_GRAPH, int SIZE_OF_DEFENSIVE_ALLIANCE){
        int sum = (PARENT_GRAPH.getSize() - distanceFunction(SIZE_OF_DEFENSIVE_ALLIANCE,genome.getSize()));
        return sum;
    }


    //hat keinen Sinn im Moment idee was es mir math.max zu arbeiten statt math.min um mit den fitness values besser arbeiten zu können
    static int calculateFitnessMAX(Genome genome, Genome PARENT_GRAPH){
        int sum = 0;
        for(int i=0; i<genome.length;i++){
            if(genome.genome[i]==1){
                int x = PARENT_GRAPH.degrees[i]-(2*genome.degrees[i]+1);
                sum += Math.max(0,(x));
            }
        }

        if(sum == 0){
            sum = (PARENT_GRAPH.getSize() - genome.getSize());
        }
        return sum;
    }

    //not needed anymore
    static int normalizedFitness(Genome genome){
        int normalizedFitness = OneGenome.worstFitnessPossible + genome.getFitness();
        return normalizedFitness;
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
        return (int) population.population_fitness/population.population.length;
    }

    static public int calculate_Mean_fitnessPositive(Population population){
        //TODO: maybe implemtnt it maybe dont not really necessary
        return 0;
    }

}
