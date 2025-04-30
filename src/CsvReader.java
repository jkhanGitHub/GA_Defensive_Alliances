import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {

    public static List<String[]> readCsv(String filePath) throws IOException {
        List<String[]> rows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                rows.add(columns);
            }
        }
        return rows;
    }

    public static int[][] readCsvEdgesToMatrix(String filePath, int length) throws IOException {
        int [][] graph_matrix = new int[length][length];

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                try {
                    graph_matrix[Integer.parseInt(columns[0])][Integer.parseInt(columns[1])]=1;
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return graph_matrix;
    }

    //returns a matrix filled out by one symmetrical side since its undirectional and ignores [i][i] since its not in csv if it was one would need to adjust the the criteria for defensive alliances to (deg_m(v) +1) to deg_m(v)
    public static int[][] readCsvEdgesToSymmetricalMatrix(String filePath, int length) throws IOException {
        int [][] graph_matrix = new int[length][length];

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                try {
                    graph_matrix[Integer.parseInt(columns[0])][Integer.parseInt(columns[1])]=1;
                    graph_matrix[Integer.parseInt(columns[1])][Integer.parseInt(columns[0])]=1;//comment out if directional
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return graph_matrix;
    }

    public static int[] readCsvEdges(String filePath, Genome g) throws IOException {

        int[] degrees = g.degrees;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                try {
                    if(g.genome[Integer.parseInt(columns[0])]==1 && g.genome[Integer.parseInt(columns[1])]==1){
                        degrees[Integer.parseInt(columns[0])]++;
                        degrees[Integer.parseInt(columns[1])]++;
                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return degrees;
    }
}

