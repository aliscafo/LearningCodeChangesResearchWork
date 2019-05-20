import java.io.*;
import java.util.List;

public class GettingPerfectPredictions {
    private static String testBuggyFile = "dataset/test/buggy.txt";
    private static String testFixedFile = "dataset/test/fixed.txt";
    private static String testPred1File = "model/pred/predictions.txt";
    private static String testPred5File = "model/pred_5/predictions.beam.txt";
    private static String testPred10File = "model/pred_10/predictions.beam.mul.txt";
    private static String testPred5MulFile = "model/pred_5/predictions.beam.mul.txt";
    private static String testPred10MulFile = "model/pred_10/predictions.beam.mul.txt";

    private static boolean isMul = true;
    private static String testPredFile = testPred10MulFile;

    private static String perfectPredictionsBeforeFile = "perfect_before.txt";
    private static String perfectPredictionsAfterFile = "perfect_after.txt";

    private static void clearFile(String fileName) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(fileName);
        writer.print("");
        writer.close();
    }

    public static void main(String[] args) throws FileNotFoundException {
        clearFile(perfectPredictionsBeforeFile);
        clearFile(perfectPredictionsAfterFile);

        BufferedReader testBuggyReader;
        BufferedReader testFixedReader;
        BufferedReader testPredReader;

        try {
            testBuggyReader = new BufferedReader(new FileReader(testBuggyFile));
            testFixedReader = new BufferedReader(new FileReader(testFixedFile));
            testPredReader = new BufferedReader(new FileReader(testPredFile));

            while (true) {
                String buggyMethod = testBuggyReader.readLine();
                String fixedMethod = testFixedReader.readLine();
                String predMethod = testPredReader.readLine();

                if (buggyMethod == null) {
                    break;
                }

                if (isMul) {
                    String[] listPredMethods = predMethod.split("\\s+<SEP>\\s+");

                    for (String elem : listPredMethods) {
                        System.out.println("|" + elem + "|");

                        if (fixedMethod.equals(elem)) {
                            GerritCrawler.appendUsingPrintWriter(perfectPredictionsBeforeFile, buggyMethod + "\n");
                            GerritCrawler.appendUsingPrintWriter(perfectPredictionsAfterFile, elem + "\n");
                        }
                    }

                    System.out.println("_________________________________________________");

                } else {
                    if (fixedMethod.equals(predMethod)) {
                        GerritCrawler.appendUsingPrintWriter(perfectPredictionsBeforeFile, buggyMethod + "\n");
                        GerritCrawler.appendUsingPrintWriter(perfectPredictionsAfterFile, predMethod + "\n");
                    }
                }

            }
            testBuggyReader.close();
            testFixedReader.close();
            testPredReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
