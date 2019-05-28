import com.google.common.collect.Multiset;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class WordsCounter {
    private Map<String, Integer> map = new HashMap<>();
    private static String METHODS_STORAGE_FILE_GOOGLE = "methods_pairs_storage_first_commented";
    private static String METHODS_STORAGE_FILE_ANDROID = "methods_pairs_storage_android";
    private static String METHODS_STORAGE_FILE_CLOUDERA = "methods_pairs_storage_cloudera";
    private static String METHODS_STORAGE_FILE_DIRTYUNICORNS = "methods_pairs_storage_dirtyunicorns";
    private static String METHODS_STORAGE_FILE_LIBREOFFICE = "methods_pairs_storage_libreoffice";
    private static String METHODS_STORAGE_FILE_LINEAGEOS = "methods_pairs_storage_lineageos";
    private static String METHODS_STORAGE_FILE_OMAPZOOM = "methods_pairs_storage_omapzoom";
    private static String METHODS_STORAGE_FILE_OVIRT = "methods_pairs_storage_ovirt";
    private static String METHODS_STORAGE_FILE_SCILAB = "methods_pairs_storage_scilab";
    private static String METHODS_STORAGE_FILE_SLIMROMS = "methods_pairs_storage_slimroms";
    private static String METHODS_STORAGE_FILE_ONOSPROJECT = "methods_pairs_storage_onosproject";
    private static String METHODS_STORAGE_FILE_ASTERIX = "methods_pairs_storage_asterix";
    private static String METHODS_STORAGE_FILE_OMNIROM = "methods_pairs_storage_omnirom";

    public void addWordsFromFile(String filename) throws IOException {
        String text = MethodAbstractorWithExternalTool.readFileAsString(filename);

        String[] words = text.split("\\s+|,|;|\\(|\\)");
        for (String word : words) {
            map.merge(word, 1, (a, b) -> a + b);
        }
    }

    private void addWordsFromRepos() throws IOException {
        for (int i = 1; i <= 15; i++) {
            String filename = METHODS_STORAGE_FILE_GOOGLE + String.valueOf(i) + ".txt";
            addWordsFromFile(filename);
        }
        for (int i = 1; i <= 16; i++) {
            String filename = METHODS_STORAGE_FILE_ANDROID + String.valueOf(i) + ".txt";
            addWordsFromFile(filename);
        }
        for (int i = 11; i <= 15; i++) {
            String filename = METHODS_STORAGE_FILE_CLOUDERA + String.valueOf(i) + ".txt";
            addWordsFromFile(filename);
        }
        for (int i = 15; i <= 15; i++) {
            String filename = METHODS_STORAGE_FILE_DIRTYUNICORNS + String.valueOf(i) + ".txt";
            addWordsFromFile(filename);
        }
        for (int i = 14; i <= 15; i++) {
            String filename = METHODS_STORAGE_FILE_LIBREOFFICE + String.valueOf(i) + ".txt";
            addWordsFromFile(filename);
        }
        for (int i = 7; i <= 15; i++) {
            String filename = METHODS_STORAGE_FILE_LINEAGEOS + String.valueOf(i) + ".txt";
            addWordsFromFile(filename);
        }
        for (int i = 6; i <= 15; i++) {
            String filename = METHODS_STORAGE_FILE_SCILAB + String.valueOf(i) + ".txt";
            addWordsFromFile(filename);
        }
        for (int i = 14; i <= 15; i++) {
            String filename = METHODS_STORAGE_FILE_SLIMROMS + String.valueOf(i) + ".txt";
            addWordsFromFile(filename);
        }
        for (int i = 1; i <= 7; i++) {
            String filename = METHODS_STORAGE_FILE_ONOSPROJECT + String.valueOf(i) + ".txt";
            addWordsFromFile(filename);
        }
        for (int i = 12; i <= 15; i++) {
            String filename = METHODS_STORAGE_FILE_ASTERIX + String.valueOf(i) + ".txt";
            addWordsFromFile(filename);
        }

        for (int i = 14; i <= 15; i++) {
            String filename = METHODS_STORAGE_FILE_OMAPZOOM + String.valueOf(i) + ".txt";
            addWordsFromFile(filename);
        }
        for (int i = 14; i <= 15; i++) {
            String filename = METHODS_STORAGE_FILE_OMNIROM + String.valueOf(i) + ".txt";
            addWordsFromFile(filename);
        }
        for (int i = 1; i <= 15; i++) {
            String filename = METHODS_STORAGE_FILE_ONOSPROJECT + String.valueOf(i) + ".txt";
            addWordsFromFile(filename);
        }
        for (int i = 100; i <= 103; i++) {
            String filename = METHODS_STORAGE_FILE_ONOSPROJECT + String.valueOf(i) + ".txt";
            addWordsFromFile(filename);
        }
        for (int i = 131; i <= 140; i++) {
            String filename = METHODS_STORAGE_FILE_OVIRT + String.valueOf(i) + ".txt";
            addWordsFromFile(filename);
        }
    }

    public static void main(String[] args) throws IOException {
        WordsCounter counter = new WordsCounter();

        counter.addWordsFromRepos();

        LinkedHashMap<String, Integer> sorted = counter.getMap()
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));

        int num = 0;

        for (Map.Entry<String, Integer> entry : sorted.entrySet()) {
            if (num > 800) {
                break;
            }
            System.out.println(entry.getKey() + " " + entry.getValue());
            num++;
        }

    }

    public Map<String, Integer> getMap() {
        return map;
    }
}
