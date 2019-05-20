import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.google.gerrit.extensions.api.GerritApi;
import com.google.gerrit.extensions.api.changes.ChangeApi;
import com.google.gerrit.extensions.api.changes.FileApi;
import com.google.gerrit.extensions.api.changes.RevisionApi;
import com.google.gerrit.extensions.common.*;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.GerritRestApiFactory;
import com.urswolfer.gerrit.client.rest.http.HttpStatusException;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

/**
 * Seen:
 * 5600 + 400 1
 * 5200 + 400 2
 * 4800 + 400 3
 * 4400 + 400 4
 * 4000 + 400 5
 * 3600 + 400 6
 * 3200 + 400 7
 * 2800 + 400 8
 * 2400 + 400 9
 * 2000 + 400 10
 * 1600 + 400 11
 * 1200 + 400 12
 * 800 + 400 13
 * 400 + 400 14
 * 0 + 400 15
 */


// comment:"yes"
    // try android

public class GerritCrawler {
    private static String QUERY = "status:merged is:reviewed java";
    private static String HOST_NAME = "https://review.gzospgzr.com";
    private static int START = 0;
    private static int LIMIT = 400;
    private static String METHODS_STORAGE_FILE = "methods_pairs_storage_gzospgzr";

    public List<MethodsPair> getPairsOfMethods() throws IOException, RestApiException {
        List<MethodsPair> pairs = new ArrayList<>();
        List<MinedPR> minedPRS = getMinedPRs();

        int k = 1;

        for (MinedPR pr : minedPRS) {
            Map<String, String> filesBefore = pr.getFilesBefore();
            Map<String, String> filesAfter = pr.getFilesAfter();

            System.out.println(k);
            k++;

            for (String fileBefore : filesBefore.keySet()) {
                System.out.println(fileBefore);

                if (fileBefore.startsWith("platform%2Fdalvik~master~Ifd49e3e81bccb3a0317e9f5677f73d4c5445965e_dx/tests")) {
                    continue;
                }

                Map<String, String> beforeMethodsStorage = new HashMap<>();
                Map<String, String> beforeMethodsStorageWithParams = new HashMap<>();

                CompilationUnit compilationUnitBefore = null;
                try {
                    compilationUnitBefore = StaticJavaParser.parse(filesBefore.get(fileBefore));
                } catch (ParseProblemException exception) {
                    System.out.println("PARSE PROBLEM EXCEPTION:\n_________________ ");
                    System.out.println(filesBefore.get(fileBefore));
                    System.out.println("__________________");
                    continue;
                }
                addBeforeMethodsToStorage(compilationUnitBefore.findRootNode(),
                        beforeMethodsStorage, beforeMethodsStorageWithParams);

                if (!filesAfter.containsKey(fileBefore)) {
                    continue;
                }

                List<MethodsPair> ansPart = new ArrayList<>();

                try {
                    CompilationUnit compilationUnitAfter = StaticJavaParser.parse(filesAfter.get(fileBefore));
                    addMethodPairsToList(compilationUnitAfter.findRootNode(),
                            beforeMethodsStorage, beforeMethodsStorageWithParams, ansPart);

                    pairs.addAll(ansPart);
                } catch (ParseProblemException e) {
                    continue;
                }
            }
        }

        return pairs;
    }

    private static void clearFile(String path) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(path);
        writer.print("");
        writer.close();
    }

    public static void main(String[] args) throws IOException, RestApiException {
        int fileNum = 1;

        //QUERY = "status:merged is:reviewed java comment:\"does\"";


        for (START = 0; START >= 0; START -= LIMIT) {
            GerritCrawler crawler = new GerritCrawler();
            List<MethodsPair> res = crawler.getPairsOfMethods();

            String path = System.getProperty("user.dir") +
                    File.separator + METHODS_STORAGE_FILE + String.valueOf(fileNum) + ".txt";
            fileNum++;

            if (res.size() == 0) {
                System.out.println("__________________________________________________________RES = 0");
                //break;
                continue;
            }

            clearFile(path);
            appendUsingPrintWriter(path, String.valueOf(res.size()) + "\n");

            //int i = 0;

            for (MethodsPair pair : res) {
                //System.out.println(pair.getBeforeMethod());
                //System.out.println("\n");
                //System.out.println(pair.getAfterMethod());
                //System.out.println("___________________________________");

                //appendUsingPrintWriter(path, String.valueOf(i) + "\n");
                appendUsingPrintWriter(path, "#method_before\n");
                appendUsingPrintWriter(path, pair.getBeforeMethod() + "\n");
                appendUsingPrintWriter(path, "#method_after\n");
                appendUsingPrintWriter(path, pair.getAfterMethod() + "\n");
                appendUsingPrintWriter(path, "#end_block\n\n");

                //i++;
            }
        }
    }

    public static void appendUsingPrintWriter(String filePath, String text) {
        File file = new File(filePath);
        FileWriter fr = null;
        BufferedWriter br = null;
        PrintWriter pr = null;
        try {
            // to append to file, you need to initialize FileWriter using below constructor
            fr = new FileWriter(file, true);
            br = new BufferedWriter(fr);
            pr = new PrintWriter(br);
            pr.print(text);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                pr.close();
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main55(String[] args) {
        CompilationUnit compilationUnit = StaticJavaParser.parse("interface MyInterface\n" +
                "{\n" +
                "   /* compiler will treat them as: \n" +
                "    * public abstract void method1();\n" +
                "    * public abstract void method2();\n" +
                "    */\n" +
                "   public void method1();\n" +
                "   public void method2();\n" +
                "}");

        Node root = compilationUnit.findRootNode();
        Map<String, String> storage = new HashMap<>();

        dfs(root, storage);

        System.out.println("SIZE: " + storage.size());
    }

    private static void dfs(Node curNode, Map<String, String> storage) {
        if (curNode instanceof MethodDeclaration) {
            if (curNode.getParentNode().isPresent() &&
                    curNode.getParentNode().get() instanceof ClassOrInterfaceDeclaration) {
                System.out.println(((ClassOrInterfaceDeclaration) curNode.getParentNode().get()).getNameAsString()
                        + " " + ((MethodDeclaration) curNode).getNameAsString());
            }
            System.out.println(curNode.toString());
            System.out.println("\n");

            storage.put(((MethodDeclaration) curNode).getNameAsString(), curNode.toString());
        }

        Node[] nodes = new Node[curNode.getChildNodes().size()];
        curNode.getChildNodes().toArray(nodes);

        for (Node node : nodes) {
            dfs(node, storage);
        }
    }

    private void addBeforeMethodsToStorage(Node curNode, Map<String, String> storage, Map<String, String> storageWithParams) {
        if (curNode instanceof MethodDeclaration) {
            if (curNode.getComment().isPresent()) {
                curNode.removeComment();
            }

            System.out.println(((MethodDeclaration) curNode).getNameAsString());

            if (curNode.getParentNode().isPresent() &&
                    curNode.getParentNode().get() instanceof ClassOrInterfaceDeclaration) {
                if (!curNode.findFirst(BlockStmt.class).isPresent()) {
                    return;
                }

                List<Parameter> list = curNode.findAll(Parameter.class, new Predicate<Parameter>() {
                    @Override
                    public boolean test(Parameter parameter) {
                        Optional<Node> parent = parameter.getParentNode();
                        if (!parent.isPresent()) {
                            return false;
                        }
                        return parent.get() == curNode;

                    }
                });
                String[] parametersTypesArray = list.stream().map(elem -> elem.getType().asString()).toArray(String[]::new);
                String parametersString = String.join("_", parametersTypesArray);

                String methodID = ((ClassOrInterfaceDeclaration) curNode.getParentNode().get()).getNameAsString()
                        + " " + ((MethodDeclaration) curNode).getNameAsString();
                String methodIDwithParams = methodID + " " + parametersString;

                storage.put(methodID, curNode.toString());
                storageWithParams.put(methodIDwithParams, curNode.toString());
            }
        }

        Node[] nodes = new Node[curNode.getChildNodes().size()];
        curNode.getChildNodes().toArray(nodes);

        for (Node node : nodes) {
            addBeforeMethodsToStorage(node, storage, storageWithParams);
        }
    }

    private void addMethodPairsToList(Node curNode, Map<String, String> storageBeforeMethods,
                                      Map<String, String> storageBeforeMethodsWithParams,
                                             List<MethodsPair> res) {
        if (curNode instanceof MethodDeclaration) {
            if (curNode.getComment().isPresent()) {
                curNode.removeComment();
            }

            if (curNode.getParentNode().isPresent() &&
                    curNode.getParentNode().get() instanceof ClassOrInterfaceDeclaration) {

                List<Parameter> list = curNode.findAll(Parameter.class, new Predicate<Parameter>() {
                    @Override
                    public boolean test(Parameter parameter) {
                        Optional<Node> parent = parameter.getParentNode();
                        if (!parent.isPresent()) {
                            return false;
                        }
                        return parent.get() == curNode;

                    }
                });
                String[] parametersTypesArray = list.stream().map(elem -> elem.getType().asString()).toArray(String[]::new);
                String parametersString = String.join("_", parametersTypesArray);

                String methodID = ((ClassOrInterfaceDeclaration) curNode.getParentNode().get()).getNameAsString()
                        + " " + ((MethodDeclaration) curNode).getNameAsString();
                String methodIDwithParams = methodID + " " + parametersString;

                if (storageBeforeMethodsWithParams.containsKey(methodIDwithParams)) {
                    if (!curNode.findFirst(BlockStmt.class).isPresent()) {
                        storageBeforeMethodsWithParams.remove(methodIDwithParams);
                        return;
                    }
                    if (!storageBeforeMethodsWithParams.get(methodIDwithParams).equals(curNode.toString())) {
                        res.add(new MethodsPair(storageBeforeMethodsWithParams.get(methodIDwithParams), curNode.toString()));
                    }
                    storageBeforeMethodsWithParams.remove(methodIDwithParams);
                    return;
                } else if (storageBeforeMethods.containsKey(methodID)) {
                    if (!curNode.findFirst(BlockStmt.class).isPresent()) {
                        storageBeforeMethods.remove(methodID);
                        return;
                    }

                    // if method before and method after are different
                    if (!storageBeforeMethods.get(methodID).equals(curNode.toString())) {
                        res.add(new MethodsPair(storageBeforeMethods.get(methodID), curNode.toString()));
                        storageBeforeMethods.remove(methodID);
                        return;
                    }
                }
            }
        }

        Node[] nodes = new Node[curNode.getChildNodes().size()];
        curNode.getChildNodes().toArray(nodes);

        for (Node node : nodes) {
            addMethodPairsToList(node, storageBeforeMethods, storageBeforeMethodsWithParams, res);
        }
    }

    /**
     * Return the list of mined PRs - the list of pairs <before, after>.
     */
    public List<MinedPR> getMinedPRs() throws IOException, RestApiException {
        List<MinedPR> minedPRS;

        GerritApi gerritApi = getGerritApi(HOST_NAME);
        List<ChangeInfo> changes = getChanges(gerritApi,  START, LIMIT);

        minedPRS = getPRsFromChanges(gerritApi, changes);

        return minedPRS;
    }

    private GerritApi getGerritApi(String host) {
        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        GerritAuthData.Basic authData = new GerritAuthData.Basic(host);
        return gerritRestApiFactory.create(authData);
    }

    private List<ChangeInfo> getChanges(GerritApi gerritApi, int start, int limit) throws UnsupportedEncodingException, RestApiException {
        String query = QUERY;

        //int n = result.size();
        //System.out.println(n);

        return gerritApi.changes()
                .query(URLEncoder.encode(query, "UTF-8")).withLimit(limit).withStart(start).get();
    }

    /**
     * Chooses useful changes from the list of changes and return the list of mined PRs.
     * Here we choose the first commented patchset and the last (merged) patchset.
     */
    private List<MinedPR> getPRsFromChanges(GerritApi gerritApi, List<ChangeInfo> changes) throws RestApiException, IOException {
        List<MinedPR> res = new ArrayList<>();

        int i = 1;

        for (ChangeInfo changeInfo : changes) {
            System.out.println(i + " " + changeInfo.subject);
            i++;

            ChangeApi changeApi = gerritApi.changes().id(changeInfo.id);

            int revisionsNum;
            int firstCommented = -1;

            for (revisionsNum = 1; revisionsNum <= 200; revisionsNum++) {
                RevisionApi revisionApi = changeApi.revision(revisionsNum);

                try {
                    Map<String, FileInfo> fileInfoMap = revisionApi.files();
                } catch (HttpStatusException e) {
                    revisionsNum = revisionsNum - 1;
                    break;
                }

                if (!revisionApi.comments().isEmpty() && firstCommented == -1) {
                    firstCommented = revisionsNum;
                }
            }

            if (firstCommented == -1 || firstCommented == revisionsNum) {
                continue;
            }

            MinedPR minedPR = makeMinedPR(gerritApi, changeInfo.id, firstCommented, revisionsNum);

            res.add(minedPR);
        }

        return res;
    }

    /**
     * Chooses useful changes from the list of changes and return the list of mined PRs.
     * Here we choose the first commented patchset and the last (merged) patchset.
     */
    private List<MinedPR> getPRsFromChanges4(GerritApi gerritApi, List<ChangeInfo> changes) throws RestApiException, IOException {
        List<MinedPR> res = new ArrayList<>();

        int i = 1;

        for (ChangeInfo changeInfo : changes) {
            System.out.println(i + " " + changeInfo.subject);
            i++;

            ChangeApi changeApi = gerritApi.changes().id(changeInfo.id);

            try {
                changeInfo = changeApi.get();
            } catch (HttpStatusException e) {
                e.printStackTrace();
                continue;
            }

            if (changeInfo.revisions == null) {
                continue;
            }

            /* SECOND WAY TO DETERMINE LAST_COMMENTED (using many get-queries)

            int lastCommented = changeInfo.revisions.size();

            while (lastCommented >= 1) {
                System.out.println("     lastCommented: " + lastCommented);

                RevisionApi revisionApi = gerritApi.changes().id(changeInfo.id).revision(lastCommented);

                if (!revisionApi.comments().isEmpty()) {
                    break;
                }

                lastCommented --;
            }

            if (lastCommented == 0 || lastCommented == changeInfo.revisions.size()) {
                continue;
            }
            */

            int firstCommented = -1;
            Map<String, List<CommentInfo>> commentInfos = changeApi.comments();
            for (String elem : commentInfos.keySet()) {
                List<CommentInfo> commentInfoList = commentInfos.get(elem);

                for (CommentInfo info : commentInfoList) {
                    if (firstCommented == -1 || info.patchSet < firstCommented)
                        firstCommented = info.patchSet;
                }
            }

            if (firstCommented == -1 || firstCommented == changeInfo.revisions.size()) {
                continue;
            }

            MinedPR minedPR = makeMinedPR(gerritApi, changeInfo.id, firstCommented, changeInfo.revisions.size());

            res.add(minedPR);
        }

        return res;
    }

    /**
     * Chooses useful changes from the list of changes and return the list of mined PRs.
     * Here we choose the first commented patchset and the last (merged) patchset.
     */
    private List<MinedPR> getPRsFromChanges1(GerritApi gerritApi, List<ChangeInfo> changes) throws RestApiException, IOException {
        List<MinedPR> res = new ArrayList<>();

        int i = 1;

        for (ChangeInfo changeInfo : changes) {
            System.out.println(i + " " + changeInfo.subject);
            i++;

            ChangeApi changeApi = gerritApi.changes().id(changeInfo.id);

            int revisionsNum;

            for (revisionsNum = 1; revisionsNum <= 200; revisionsNum++) {
                RevisionApi revisionApi = changeApi.revision(revisionsNum);

                try {
                    Map<String, FileInfo> fileInfoMap = revisionApi.files();
                } catch (HttpStatusException e) {
                    revisionsNum = revisionsNum - 1;
                    break;
                } catch (RestApiException e) {
                    e.printStackTrace();
                    continue;
                }
            }

            int firstCommented = -1;
            Map<String, List<CommentInfo>> commentInfos;

            try {
                commentInfos = changeApi.comments();
            } catch (HttpStatusException e) {
                e.printStackTrace();
                continue;
            }

            for (String elem : commentInfos.keySet()) {
                List<CommentInfo> commentInfoList = commentInfos.get(elem);

                for (CommentInfo info : commentInfoList) {
                    if (firstCommented == -1 || info.patchSet < firstCommented)
                        firstCommented = info.patchSet;
                }
            }

            if (firstCommented == -1 || firstCommented == revisionsNum) {
                continue;
            }

            MinedPR minedPR = makeMinedPR(gerritApi, changeInfo.id, firstCommented, revisionsNum);

            res.add(minedPR);
        }

        return res;
    }

    private MinedPR makeMinedPR(GerritApi gerritApi, String changeId, int beforeIndex, int afterIndex) throws RestApiException, IOException {
        RevisionApi revisionApiBefore = gerritApi.changes().id(changeId).revision(beforeIndex);
        Map<String, String> beforeMap = makeMapFromRevision(changeId, revisionApiBefore);

        RevisionApi revisionApiAfter = gerritApi.changes().id(changeId).revision(afterIndex);
        Map<String, String> afterMap = makeMapFromRevision(changeId, revisionApiAfter);

        return new MinedPR(beforeMap, afterMap);
    }


    private Map<String, String> makeMapFromRevision(String changeId, RevisionApi revisionApi) throws IOException {
        Map<String, String> res = new HashMap<>();

        Map<String, FileInfo> fileInfoMap = null;
        try {
            fileInfoMap = revisionApi.files();
        } catch (RestApiException e) {
            return res;
        }

        for (String elem : fileInfoMap.keySet()) {
            if (!elem.endsWith(".java")) {
                continue;
            }

            System.out.println("            FILE:" + elem);
            FileApi fileApi = revisionApi.file(elem);
            BinaryResult binaryResult = null;
            try {
                binaryResult = fileApi.content();
            } catch (RestApiException e) {
                continue;
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            binaryResult.writeTo(byteArrayOutputStream);
            String actualContent = new String(Base64.decodeBase64(byteArrayOutputStream.toString()));

            res.put(changeId + "_" + elem, actualContent);
        }

        return res;
    }

    public static void main7(String[] args) throws RestApiException, IOException {
        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        GerritAuthData.Basic authData = new GerritAuthData.Basic("https://gerrit.ovirt.org");
        GerritApi gerritApi = gerritRestApiFactory.create(authData);
        String query = "status:merged is:reviewed java";

        List<ChangeInfo> changes = gerritApi.changes().query(URLEncoder.encode(query, "UTF-8")).withLimit(30).get();

        ChangeInfo changeInfo = changes.get(17);

        ChangeApi changeApi = gerritApi.changes().id(changeInfo.id);

        System.out.println(changeInfo.branch + " " + changeInfo.subject + " ");

        Map<String, List<CommentInfo>> commentInfos = changeApi.comments();

        if (commentInfos == null) {
            System.out.println("NULL :(");
        } else {
            System.out.println("SIZE " + commentInfos.size());
            for (String elem : commentInfos.keySet()) {
                System.out.println("        COMMENT KEY: " + elem);

                List<CommentInfo> commentInfoList = commentInfos.get(elem);

                for (CommentInfo info : commentInfoList) {
                    System.out.println("                 COMMENT INFO: _______________\n"
                            + info.message + "\n         _____________ PATCHSET: " + info.patchSet + "\n");
                }
            }
        }

        /*RevisionApi revisionApi = changeApi.revision(9);

        if (revisionApi == null) {
            System.out.println("     IT'S NULL");
        }

        Map<String, FileInfo> fileInfoMap = revisionApi.files();
        for (String file : fileInfoMap.keySet()) {
            System.out.println("FILE:" + file);
        }*/

        int i;

        for (i = 1; i <= 100; i++) {
            RevisionApi revisionApi = changeApi.revision(i);

            try {
                Map<String, FileInfo> fileInfoMap = revisionApi.files();
            } catch (HttpStatusException e) {
                System.out.println(i - 1);
                break;
            }
        }

        //System.out.println(changeApi.);

        /*if (info.revisions == null) {
            System.out.println("     IT'S NULL");
        } else {
            //System.out.println(revisionApi.);

            /*Map<String, FileInfo> fileInfoMap = revisionApi.files();
            for (String file : fileInfoMap.keySet()) {
                System.out.println("FILE:" + file);
            }*/

        /*
        RevisionApi revisionApi = gerritApi.changes().id(changeInfo.id).revision(1);

        Map<String, List<CommentInfo>> commentInfos = revisionApi.comments();

        if (commentInfos == null) {
            System.out.println("NULL :(");
        } else {
            for (String elem : commentInfos.keySet()) {
                System.out.println("        COMMENT KEY: " + elem);

                List<CommentInfo> commentInfoList = commentInfos.get(elem);

                for (CommentInfo info : commentInfoList) {
                    System.out.println("                 COMMENT INFO: " + info.message);
                }
            }
        }

        System.out.println("ID: " + changeInfo.id);
        */

        /*
        Map<String, FileInfo> fileInfoMap = revisionApi.files();

        for (String elem : fileInfoMap.keySet()) {
            System.out.println("   KEY: " + elem);
            FileApi fileApi = revisionApi.file(elem);
            BinaryResult binaryResult = fileApi.content();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            binaryResult.writeTo(byteArrayOutputStream);
            String actualContent = new String(Base64.decodeBase64(byteArrayOutputStream.toString()));

            System.out.println("       Content: " + actualContent);
        }
        */

        /*changeInfo = changeApi.get();

        System.out.println("ID: " + changeInfo.id);

        Map<String, RevisionInfo> revisionInfoMap = changeInfo.revisions;

        if (revisionInfoMap == null) {
            System.out.println("     IT'S NULL");
        } else {

            for (String elem : revisionInfoMap.keySet()) {
                System.out.println("        KEY: " + elem + " " + revisionInfoMap.get(elem)._number);
                Map<String, FileInfo> map = revisionInfoMap.get(elem).files;

                for (String file : map.keySet()) {
                    System.out.println("               FILENAME: " + file);
                }
            }
        }

        System.out.println("___________________\n");

        List<RevisionInfo> revisions = new ArrayList<>(changeInfo.revisions.values());
        revisions.sort(Comparator.comparingInt(o -> o._number));

        for (RevisionInfo info : revisions) {
            System.out.println(info._number);
        }
*/
    }

    public class MinedPR {
        private Map<String, String> filesBefore;
        private Map<String, String> filesAfter;

        public MinedPR(Map<String, String> filesBefore, Map<String, String> filesAfter) {
            this.filesBefore = filesBefore;
            this.filesAfter = filesAfter;
        }

        public Map<String, String> getFilesBefore() {
            return filesBefore;
        }

        public void setFilesBefore(Map<String, String> filesBefore) {
            this.filesBefore = filesBefore;
        }

        public Map<String, String> getFilesAfter() {
            return filesAfter;
        }

        public void setFilesAfter(Map<String, String> filesAfter) {
            this.filesAfter = filesAfter;
        }
    }

    public class MethodsPair {
        private String beforeMethod;
        private String afterMethod;

        public MethodsPair(String beforeMethod, String afterMethod) {
            this.beforeMethod = beforeMethod;
            this.afterMethod = afterMethod;
        }

        public String getBeforeMethod() {
            return beforeMethod;
        }

        public void setBeforeMethod(String beforeMethod) {
            this.beforeMethod = beforeMethod;
        }

        public String getAfterMethod() {
            return afterMethod;
        }

        public void setAfterMethod(String afterMethod) {
            this.afterMethod = afterMethod;
        }
    }

    public static void main2(String[] args) throws IOException, RestApiException {
        GerritCrawler gerritCrawler = new GerritCrawler();
        List<MinedPR> list = gerritCrawler.getMinedPRs();

        System.out.println("__________________\n\n RESULT: " + list.size());

    }
}
