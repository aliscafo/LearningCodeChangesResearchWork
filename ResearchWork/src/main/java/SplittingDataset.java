import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SplittingDataset {
    private static List<MethodPair> pairs = new ArrayList<>();

    private static String buggyFile = "buggy100_unique.txt";
    private static String fixedFile = "fixed100_unique.txt";

    private static void clearFile(String fileName) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(fileName);
        writer.print("");
        writer.close();
    }

    public static void main(String[] args) throws FileNotFoundException {
        clearFile("dataset2/train/buggy.txt");
        clearFile("dataset2/train/fixed.txt");
        clearFile("dataset2/test/buggy.txt");
        clearFile("dataset2/test/fixed.txt");
        clearFile("dataset2/eval/buggy.txt");
        clearFile("dataset2/eval/fixed.txt");

        BufferedReader readerBuggy;
        BufferedReader readerFixed;

        try {
            readerBuggy = new BufferedReader(new FileReader(buggyFile));
            readerFixed = new BufferedReader(new FileReader(fixedFile));

            while (true) {
                String buggyMethod = readerBuggy.readLine();
                String fixedMethod = readerFixed.readLine();

                if (buggyMethod == null) {
                    break;
                }

                buggyMethod += "\n";
                fixedMethod += "\n";

                pairs.add(new MethodPair(buggyMethod, fixedMethod));
            }
            readerBuggy.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.shuffle(pairs);

        int num_train = (int) (0.8 * pairs.size());
        int num_test = (int) (0.1 * pairs.size());
        int num_eval = pairs.size() - num_test - num_train;

        for (int i = 0; i < num_train; i++) {
            GerritCrawler.appendUsingPrintWriter("dataset2/train/buggy.txt", pairs.get(i).methodBefore);
            GerritCrawler.appendUsingPrintWriter("dataset2/train/fixed.txt", pairs.get(i).methodAfter);
        }

        for (int i = num_train; i < num_train + num_test; i++) {
            GerritCrawler.appendUsingPrintWriter("dataset2/test/buggy.txt", pairs.get(i).methodBefore);
            GerritCrawler.appendUsingPrintWriter("dataset2/test/fixed.txt", pairs.get(i).methodAfter);
        }

        for (int i = num_train + num_test; i < pairs.size(); i++) {
            GerritCrawler.appendUsingPrintWriter("dataset2/eval/buggy.txt", pairs.get(i).methodBefore);
            GerritCrawler.appendUsingPrintWriter("dataset2/eval/fixed.txt", pairs.get(i).methodAfter);
        }
    }

    public static class MethodPair {
        private String methodBefore;
        private String methodAfter;

        MethodPair(String methodBefore, String methodAfter) {
            this.methodBefore = methodBefore;
            this.methodAfter = methodAfter;
        }

        public String getMethodBefore() {
            return methodBefore;
        }

        public String getMethodAfter() {
            return methodAfter;
        }
    }
}
