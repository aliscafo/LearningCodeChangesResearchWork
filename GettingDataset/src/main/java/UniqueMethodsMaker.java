import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UniqueMethodsMaker {
    private static Set<String> set = new HashSet<>();

    private static String buggyFile = "buggy100.txt";
    private static String fixedFile = "fixed100.txt";

    private static String uniqueBuggyFile = "buggy100_unique.txt";
    private static String uniqueFixedFile = "fixed100_unique.txt";

    private static void clearFiles() throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(uniqueBuggyFile);
        writer.print("");
        writer.close();

        writer = new PrintWriter(uniqueFixedFile);
        writer.print("");
        writer.close();
    }

    public static void main(String[] args) throws FileNotFoundException {
        clearFiles();

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

                if (!buggyMethod.endsWith("}")) {
                    System.out.println(buggyMethod);
                    continue;
                }

                if (!fixedMethod.endsWith("}")) {
                    System.out.println(buggyMethod);
                    continue;
                }


                buggyMethod += "\n";
                fixedMethod += "\n";

                if (set.contains(buggyMethod/* + fixedMethod*/)) {
                    continue;
                }

                GerritCrawler.appendUsingPrintWriter(uniqueBuggyFile, buggyMethod);
                GerritCrawler.appendUsingPrintWriter(uniqueFixedFile, fixedMethod);
                set.add(buggyMethod /*+ fixedMethod*/);
            }

            readerBuggy.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
