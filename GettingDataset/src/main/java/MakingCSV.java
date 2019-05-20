import java.io.*;

public class MakingCSV {
    private static String perfectPredictionsBeforeFile = "perfect_before.txt";
    private static String perfectPredictionsAfterFile = "perfect_after.txt";

    private static String resultCSVfile = "result.csv";

    private static void clearFile(String fileName) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(fileName);
        writer.print("");
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        clearFile(resultCSVfile);

        FileWriter csvWriter = new FileWriter(resultCSVfile);

        csvWriter.append("step_id,user_id,submission_code,is_passed,timestamp\n");

        BufferedReader beforeReader;
        BufferedReader afterReader;

        try {
            beforeReader = new BufferedReader(new FileReader(perfectPredictionsBeforeFile));
            afterReader = new BufferedReader(new FileReader(perfectPredictionsAfterFile));

            int userId = 1;

            while (true) {
                String buggyMethod = beforeReader.readLine();
                String fixedMethod = afterReader.readLine();

                if (buggyMethod == null) {
                    break;
                }

                String newDataPrefix = "1," + userId + ",";

                csvWriter.append("1," + userId + "," + "\"" + buggyMethod + "\"," + "False," + "0\n");
                csvWriter.append("1," + userId + "," + "\"" + fixedMethod + "\"," + "True," + "1\n");

                userId++;

            }
            beforeReader.close();
            afterReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        csvWriter.flush();
        csvWriter.close();
    }
}
