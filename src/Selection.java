import java.util.*;
//all methods expect a sorted Population
public class Selection {



    //adds the members with the highest fitness value in population to a list
    static List<Genome> elitism(Population population, int numberOfElits){
        List<Genome> elits = new LinkedList<>();
        for (int i = 0; i < numberOfElits; i++) {
            elits.add(population.population[i]);
        }
        return elits;
    }

    static List<Genome> tournamentSelectionElimination(Population p, int numberOfContestantsPerRound){
        List<Genome> nextGenParents = new LinkedList<>();

        //play tournament
        int winnerID;
        Genome[] temp = shuffleArray(p.getPopulation());
        for (int i = 0, j = numberOfContestantsPerRound-1; j < temp.length; i=j+1,j=j+numberOfContestantsPerRound) {
            for (winnerID=i; i < j; i++) {
                if(Math.max(temp[winnerID].fitness,temp[i].fitness)==temp[i].fitness){
                    winnerID = i;
                }
            }
            //add winner to winner list
            nextGenParents.add(temp[winnerID]);
        }
        return nextGenParents;
    }



    // Implementing Fisher–Yates shuffle
    static Genome[] shuffleArray(Genome[] ar)
    {
        Genome[] temp = ar;
        Random rnd = new Random();
        for (int i = temp.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            Genome a = temp[index];
            temp[index] = temp[i];
            temp[i] = a;
        }
        return temp;
    }

}