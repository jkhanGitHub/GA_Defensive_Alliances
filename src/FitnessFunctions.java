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

    static int calculateFitnessMIN(Genome genome, Genome PARENT_GRAPH){
        int sum = 0;
        int sizeFactor = genome.getSize();

        if ((sizeFactor==0) || (sizeFactor==PARENT_GRAPH.getSize())) return 0;

        for(int i=0; i<genome.length;i++){
            if(genome.genome[i]==1){
                int x = (2*genome.degrees[i])+1-PARENT_GRAPH.degrees[i];
                sum += Math.min(0,(x));
            }
        }

        if(sum == 0){
            sum = (PARENT_GRAPH.getSize() - genome.getSize());
        }
        return sum;
    }


    //hat keinen Sinn im Moment
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
