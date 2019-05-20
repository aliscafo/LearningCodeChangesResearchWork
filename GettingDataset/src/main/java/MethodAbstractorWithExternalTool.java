
import java.io.*;
import java.util.*;

public class MethodAbstractorWithExternalTool {
    // first_commented
    // android1-16
    // cloudera
    // dirtyunicorns
    // libreoffice
    // lineageos
    // scilab
    // opendev
    // ovirt1-15
    // android101-110
    // asterix
    // omapzoom
    // omnirom
    // onosproject1-15, 100-103
    // ovirt101-124
    // ovirt125-127
    // ovirt128-129
    // ovirt131-141
    // ovirt142-144
    // ovirt145-148
    // ovirt149-150
    // ovirt151-152
    // ovirt153
    // ovirt154-155
    // ovirt156-169
    // google5-9
    // ovirt170-175
    // google10-11
    // ovirt176-178
    // google12-13
    // ovirt179
    // google15-16
    // google25-32
    // ovirt180-184
    // google40-47
    // google48-49
    // google50
    // aospa
    // opencord
    // typo3
    // gzospgzr
    // google51-52
    private static String METHODS_STORAGE_FILE = "methods_pairs_storage_google";

    private static String BEFORE_LABEL = "#method_before";
    private static String AFTER_LABEL = "#method_after";
    private static String END_BLOCK_LABEL = "#end_block";

    private static String inputFileBeforeMethod = "before.txt";
    private static String inputFileAfterMethod = "after.txt";

    private static String outputFileBeforeMethod = "./before_output.txt";
    private static String outputFileAfterMethod = "./after_output.txt";

    private static String targetFilePathBuggy50 = "buggy50.txt";
    private static String targetFilePathFixed50 = "fixed50.txt";

    private static String targetFilePathBuggy100 = "buggy100.txt";
    private static String targetFilePathFixed100 = "fixed100.txt";

    private static String logFile50 = "log_methods50.txt";
    private static String logFile100 = "log_methods100.txt";

    private static String[] listIDs = {"TYPE_", "METHOD_", "VAR_", "INT_", "FLOAT_", "CHAR_", "STRING_"};

    private static void clearFiles() throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(targetFilePathBuggy50);
        writer.print("");
        writer.close();

        writer = new PrintWriter(targetFilePathFixed50);
        writer.print("");
        writer.close();

        writer = new PrintWriter(targetFilePathBuggy100);
        writer.print("");
        writer.close();

        writer = new PrintWriter(targetFilePathFixed100);
        writer.print("");
        writer.close();
    }

    public static void main(String[] args) throws IOException {

        for (int i = 52; i <= 52; i++) {
            String fileText = readFileAsString(METHODS_STORAGE_FILE + String.valueOf(i) + ".txt");
            System.out.println(METHODS_STORAGE_FILE + String.valueOf(i) + ".txt");

            //clearFiles();

            List<String> commandAndArgs = new ArrayList<>();
            commandAndArgs.add("java");
            commandAndArgs.addAll(Arrays.asList("-jar", "src2abs-0.1-jar-with-dependencies.jar", "pair", "method"));
            commandAndArgs.addAll(Arrays.asList(inputFileBeforeMethod, inputFileAfterMethod));
            commandAndArgs.addAll(Arrays.asList(outputFileBeforeMethod, outputFileAfterMethod));
            commandAndArgs.add("idioms/final_idioms.csv");

            int fromIndex = 0;
            int n = fileText.length();

            while (true) {
                int beforeLabelIndex = fileText.indexOf(BEFORE_LABEL, fromIndex);
                int afterLabelIndex = fileText.indexOf(AFTER_LABEL, fromIndex);
                int endBlockLabelIndex = fileText.indexOf(END_BLOCK_LABEL, fromIndex);

                if (beforeLabelIndex == -1 || afterLabelIndex == -1 || endBlockLabelIndex == -1) {
                    break;
                }

                String beforeMethodCode = fileText.substring(beforeLabelIndex + BEFORE_LABEL.length(), afterLabelIndex);
                String afterMethodCode = fileText.substring(afterLabelIndex + AFTER_LABEL.length(), endBlockLabelIndex);

                writeUsingOutputStream("before.txt", beforeMethodCode);
                writeUsingOutputStream("after.txt", afterMethodCode);

                ProcessBuilder processBuilder = new ProcessBuilder(commandAndArgs)
                        .directory(new File(System.getProperty("user.dir")));

                Process process = processBuilder.start();
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted while executing");
                }

                process.destroy();

                String absractedBeforeMethodCode = readFileAsString(outputFileBeforeMethod);
                String absractedAfterMethodCode = readFileAsString(outputFileAfterMethod);

                String[] tokensBefore = absractedBeforeMethodCode.split("\\s+");
                String[] tokensAfter = absractedAfterMethodCode.split("\\s+");

                if (!absractedBeforeMethodCode.equals(absractedAfterMethodCode) &&
                        ifPossibleToGetAfterMethod(tokensBefore, tokensAfter)) {
                    if (tokensBefore.length <= 50 && tokensAfter.length <= 50) {
                        GerritCrawler.appendUsingPrintWriter(targetFilePathBuggy50, absractedBeforeMethodCode);
                        GerritCrawler.appendUsingPrintWriter(targetFilePathFixed50, absractedAfterMethodCode);

                        GerritCrawler.appendUsingPrintWriter(logFile50, absractedBeforeMethodCode + "\n");
                        GerritCrawler.appendUsingPrintWriter(logFile50, beforeMethodCode + "\n");
                        GerritCrawler.appendUsingPrintWriter(logFile50, absractedAfterMethodCode + "\n");
                        GerritCrawler.appendUsingPrintWriter(logFile50, afterMethodCode + "\n");
                    } else if (tokensBefore.length <= 100 && tokensAfter.length <= 100) {
                        GerritCrawler.appendUsingPrintWriter(targetFilePathBuggy100, absractedBeforeMethodCode);
                        GerritCrawler.appendUsingPrintWriter(targetFilePathFixed100, absractedAfterMethodCode);

                        GerritCrawler.appendUsingPrintWriter(logFile100, absractedBeforeMethodCode + "\n");
                        GerritCrawler.appendUsingPrintWriter(logFile100, beforeMethodCode + "\n");
                        GerritCrawler.appendUsingPrintWriter(logFile100, absractedAfterMethodCode + "\n");
                        GerritCrawler.appendUsingPrintWriter(logFile100, afterMethodCode + "\n");
                    }
                }

                fromIndex = endBlockLabelIndex + END_BLOCK_LABEL.length();

                System.out.println("OK " + fromIndex + "/" + n);
            }
        }

        //manager.abstractCodePair(Parser.CodeGranularity.METHOD, );
    }

    private static boolean isID(String token) {
        for (String ID : listIDs) {
            if (token.startsWith(ID)) {
                return true;
            }
        }

        return false;
    }

    private static boolean ifPossibleToGetAfterMethod(String[] tokensBefore, String[] tokensAfter) {
        Set<String> beforeTokens = new HashSet<>();

        for (String token : tokensBefore) {
            if (token.contains("_")) {
                if (isID(token)) {
                    beforeTokens.add(token);
                }
            }
        }

        for (String token : tokensAfter) {
            if (token.contains("_")) {
                if (isID(token)) {
                    if (!beforeTokens.contains(token)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static String readFileAsString(String fileName) throws IOException {
        InputStream is = new FileInputStream(fileName);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();
        while(line != null){
            sb.append(line).append("\n");
            line = buf.readLine();
        }

        String fileAsString = sb.toString();

        is.close();
        buf.close();

        return fileAsString;
    }

    private static void writeUsingOutputStream(String fileName, String data) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(new File(fileName));
            os.write(data.getBytes(), 0, data.length());
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main555(String[] args) {
        String absractedBeforeMethodCode = "protected void display ( TYPE_1 account ) { VAR_1 = account . email ( ) ; VAR_2 . METHOD_1 ( account . name ( ) ) ; VAR_3 . METHOD_2 ( false ) ; new TYPE_2 ( VAR_3 , VAR_2 ) ; }\n";
        String absractedAfterMethodCode = "protected void display ( TYPE_1 account ) { VAR_1 = account . email ( ) ; VAR_2 . METHOD_1 ( account . name ( ) ) ; VAR_3 . METHOD_2 ( false ) ; VAR_2 . METHOD_2 ( VAR_2 ) ; }\n";

        String[] tokensBefore = absractedBeforeMethodCode.split("\\s+");
        String[] tokensAfter = absractedAfterMethodCode.split("\\s+");

        System.out.println(ifPossibleToGetAfterMethod(tokensBefore, tokensAfter));
    }

}
