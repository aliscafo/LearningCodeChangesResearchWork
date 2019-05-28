import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MethodAbstractionTool {
    private Set<String> topMostFrequent = new HashSet<>();
    private Map<String, Integer> mapIDsNum = new HashMap<>();
    Map<String, String> fromWordToID = new HashMap<>();
    Map<String, String> fromIDtoWord = new HashMap<>();

    public MethodAbstractionTool() {
        mapIDsNum.put("TYPE", 1);
        mapIDsNum.put("METHOD", 1);
        mapIDsNum.put("VAR", 1);
        mapIDsNum.put("STRING", 1);
        mapIDsNum.put("DOUBLE", 1);
        mapIDsNum.put("INT", 1);
        mapIDsNum.put("CHAR", 1);
    }

    public static void testMain1(String[] args) {
        Optional<ClassOrInterfaceType> classOrInterfaceType =
                new JavaParser().parseClassOrInterfaceType("catKop").getResult();
        System.out.println(classOrInterfaceType.get().getNameAsString());
    }

    private static void dfs2(Node curNode) {
        if (curNode.getComment().isPresent()) {
            System.out.println("COMMENT" + curNode.getComment().get());
            curNode.removeComment();
        }
        System.out.println("\nNODE: " + curNode.toString());
        System.out.println(curNode.getClass());

        if (curNode instanceof ClassOrInterfaceType) {
            //((ClassOrInterfaceType) curNode).setName("MyClass");
            SimpleName simpleName = new SimpleName("MyClass");
            //curNode = new ClassOrInterfaceType("MyClass");

            JavaParser parser = new JavaParser();
            //curNode.replace(new ClassOrInterfaceType(""));

            //System.out.println("     !!! PARSED: " + parser.parseClassOrInterfaceType(curNode.toString()).getResult().get().toString());
            //curNode.replace(parser.parseClassOrInterfaceType("MyClass").getResult().get());

            //return;
        }

        if (curNode instanceof MethodReferenceExpr) {
            ((MethodReferenceExpr) curNode).setIdentifier("BLABLA");
        }

        Node[] nodes = new Node[curNode.getChildNodes().size()];
        curNode.getChildNodes().toArray(nodes);

        System.out.println("\nSTART CHILDREN OF " + curNode.toString() + " (" + curNode.getClass() + ")\n");

        for (Node node : nodes) {
            dfs2(node);
        }

        System.out.println("\nEND CHILDREN OF " + curNode.toString() + " (" + curNode.getClass() + ")\n");

    }


    private void dfs(Node curNode) {
        if (topMostFrequent.contains(curNode.toString())) {
            return;
        }

        if (curNode instanceof ClassOrInterfaceType) {
            List<String> parsedType = parseClassOrInterfaceType(curNode.toString());
            List<String> listAfterSubstitution = makeSubstitutionToList(parsedType);
            String newTypeName = String.join("", listAfterSubstitution);

            Optional<ClassOrInterfaceType> classOrInterfaceType = new JavaParser().parseClassOrInterfaceType(newTypeName).getResult();
            classOrInterfaceType.ifPresent(curNode::replace);

            return;
        }

        if (curNode instanceof NameExpr) {
            Optional<ClassOrInterfaceType> classOrInterfaceType =
                    new JavaParser().parseClassOrInterfaceType(curNode.toString()).getResult();

            String id = null;

            if (classOrInterfaceType.isPresent()) {
                String name = classOrInterfaceType.get().getNameAsString();
                if (Character.isUpperCase(name.charAt(0))) {
                    id = substitute(curNode.toString(), "TYPE");
                }
            }

            if (id == null) {
                id = substitute(curNode.toString(), "VAR");
            }

            curNode.replace(new NameExpr(id));

            return;
        }

        if (curNode instanceof MethodDeclaration) {
            String id = substitute(curNode.toString(), "METHOD");
            ((MethodDeclaration) curNode).setName(id);
        }

        if (curNode instanceof StringLiteralExpr) {
            String id = substitute(curNode.toString(), "STRING");
            curNode.replace(new NameExpr(id));
        }

        if (curNode instanceof DoubleLiteralExpr) {
            String id = substitute(curNode.toString(), "DOUBLE");
            curNode.replace(new NameExpr(id));
        }

        if (curNode instanceof IntegerLiteralExpr) {
            String id = substitute(curNode.toString(), "INT");
            curNode.replace(new NameExpr(id));
        }

        if (curNode instanceof CharLiteralExpr) {
            String id = substitute(curNode.toString(), "CHAR");
            curNode.replace(new NameExpr(id));
        }

        if (curNode instanceof MethodReferenceExpr) {
            String id = substitute(curNode.toString(), "METHOD");
            ((MethodReferenceExpr) curNode).setIdentifier(id);
        }

        if (curNode instanceof SimpleName) {
            if (curNode.getParentNode().isPresent() &&
                    curNode.getParentNode().get() instanceof MethodCallExpr) {
                String id = substitute(curNode.toString(), "METHOD");
                curNode.replace(new SimpleName(id));
            }

            if (curNode.getParentNode().isPresent() &&
                    curNode.getParentNode().get() instanceof Parameter) {
                String id = substitute(curNode.toString(), "VAR");
                curNode.replace(new SimpleName(id));
            }

            if (curNode.getParentNode().isPresent() &&
                    (curNode.getParentNode().get() instanceof VariableDeclarator ||
                            curNode.getParentNode().get() instanceof FieldAccessExpr)) {
                String id = substitute(curNode.toString(), "VAR");
                curNode.replace(new SimpleName(id));
            }
        }

        Node[] nodes = new Node[curNode.getChildNodes().size()];
        curNode.getChildNodes().toArray(nodes);

        for (Node node : nodes) {
            dfs(node);
        }
    }

    private List<String> makeSubstitutionToList(List<String> list) {
        List<String> res = new ArrayList<>();

        for (String elem : list) {
            if (elem.equals("<") || elem.equals(">") || elem.equals(",") || elem.equals(" ")) {
                res.add(elem);
                continue;
            }

            res.add(substitute(elem, "TYPE"));
        }

        return res;
    }

    private String substitute(String elem, String category) {
        if (topMostFrequent.contains(elem)) {
            return elem;
        } else {
            if (fromWordToID.containsKey(elem)) {
                return fromWordToID.get(elem);
            } else {
                Integer num = mapIDsNum.get(category);

                fromWordToID.put(elem, category + "_" + num.toString());
                fromIDtoWord.put(category + "_" + num.toString(), elem);

                mapIDsNum.replace(category, num + 1);

                return category + "_" + num.toString();
            }
        }
    }

    private List<String> parseClassOrInterfaceType(String classOrInterface) {
        List<String> res = new ArrayList<>();

        int left = -1, right = 0;
        int n = classOrInterface.length();

        while (right < n) {
            char c = classOrInterface.charAt(right);

            if (c == '<' || c == '>' || c == ',' || c == ' ') {
                String part = classOrInterface.substring(left + 1, right);

                if (!part.equals("")) {
                    res.add(part);
                }

                res.add(String.valueOf(c));
                left = right;
            }
            right++;
        }

        String part = classOrInterface.substring(left + 1, right);
        if (!part.equals("")) {
            res.add(part);
        }

        return res;
    }

    public String abstractMethod(String method) {
        BodyDeclaration bodyDeclaration = StaticJavaParser.parseBodyDeclaration(method);
        dfs(bodyDeclaration.findRootNode());

        return bodyDeclaration.toString();
    }

    public static void main(String[] args) {
        BodyDeclaration bodyDeclaration1 = StaticJavaParser.parseBodyDeclaration("public int getData() { int gg = 6; }");
        BodyDeclaration bodyDeclaration2 = StaticJavaParser.parseBodyDeclaration("public void shouldInvokeMultiSiteRefDbFactoryCreate() {\n" +
                "    setMockitoCommon();\n" +
                "    MultiSiteRepository multiSiteRepository =\n" +
                "        new MultiSiteRepository(multiSiteRefDbFactory, PROJECT_NAME, repository);\n" +
                "\n" +
                "    multiSiteRepository.getRefDatabase();\n" +
                "    verify(multiSiteRefDbFactory).create(PROJECT_NAME, genericRefDb);\n" +
                "  }");
        BodyDeclaration bodyDeclaration3 = StaticJavaParser.parseBodyDeclaration("protected void METHOD_1 ( ) throws java.lang.Throwable { String t = \"ddd\" ; try { VAR_1 . METHOD_2 ( ) ; if ( ( fd ) != null ) METHOD_3 ( ) ; } finally { super . METHOD_1 ( ) ; } } \n");
        BodyDeclaration bodyDeclaration4 = StaticJavaParser.parseBodyDeclaration("public < TYPE_1 , TYPE_2 > java.util.Map < TYPE_1 , TYPE_2 > METHOD_1 ( TYPE_3 < java.util.Map < TYPE_1 , TYPE_2 > > action ) { return METHOD_1 ( action , false ) ; } \n");
        BodyDeclaration bodyDeclaration5 = StaticJavaParser.parseBodyDeclaration("public void updateCheckerLikeRefByPush() throws Exception {\n" +
                "    String checkerRef = CheckerUuid.parse(\"foo:bar\").toRefName();\n" +
                "\n" +
                "    allow(checkerRef, Permission.CREATE, adminGroupUuid());\n" +
                "    createBranch(new Branch.NameKey(project, checkerRef));\n" +
                "\n" +
                "    TestRepository<InMemoryRepository> repo = cloneProject(project, admin);\n" +
                "    fetch(repo, checkerRef + \":checkerRef\");\n" +
                "    repo.reset(\"checkerRef\");\n" +
                "\n" +
                "    grant(project, CheckerRef.REFS_CHECKERS + \"*\", Permission.PUSH);\n" +
                "    PushOneCommit.Result r = pushFactory.create(admin.getIdent(), repo).to(checkerRef);\n" +
                "    r.assertOkStatus();\n" +
                "  }");
        BodyDeclaration bodyDeclaration6 = StaticJavaParser.parseBodyDeclaration("private static boolean onSupportedJavaVersion() {\n" +
                "    final String version = System.getProperty(\"java.specification.version\");\n" +
                "    if (1.7 <= parse(version)) {\n" +
                "      return true;\n" +
                "\n" +
                "    }\n" +
                "    System.err.println(\"fatal: Gerrit Code Review requires Java 7 or later\");\n" +
                "    System.err.println(\"       (trying to run on Java \" + version + \")\");\n" +
                "    return false;\n" +
                "  }");
        BodyDeclaration bodyDeclaration8 = StaticJavaParser.parseBodyDeclaration("private static double parse(String version) {\n" +
                "    if (version == null || version.length() == 0) {\n" +
                "      return 0.0;\n" +
                "    }\n" +
                "\n" +
                "    try {\n" +
                "      final int fd = version.indexOf('.');\n" +
                "      final int sd = version.indexOf('.', fd + 1);\n" +
                "      if (0 < sd) {\n" +
                "        version = version.substring(0, sd);\n" +
                "      }\n" +
                "      return Double.parseDouble(version);\n" +
                "    } catch (NumberFormatException e) {\n" +
                "      return 0.0;\n" +
                "    }\n" +
                "  }");

        BodyDeclaration bodyDeclaration0 = StaticJavaParser.parseBodyDeclaration("private String getStringWithFallback(\n" +
                "      String parameter, int healthCheckName, int defaultValue) {\n" +
                "    String fallbackDefault =\n" +
                "        healthCheckName == null\n" +
                "            ? defaultValue\n" +
                "            : getStringWithFallback(parameter, null, defaultValue);\n" +
                "    return MoreObjects.firstNonNull(\n" +
                "        config.getString(HEALTHCHECK, healthCheckName, parameter), fallbackDefault);\n" +
                "  }");

        BodyDeclaration bodyDeclaration = StaticJavaParser.parseBodyDeclaration("" +
                "/** hhhh **/" +
                "public static void main(String[] args) { \n" +
                "  \n" +
                "        // Myclass is hidden inner class of Age interface \n" +
                "        // whose name is not written but an object to it  \n" +
                "        // is created. \n" +
                "        Age oj1 = new Age() { \n" +
                "            @Override\n" +
                "            public void getAge(int k) { \n" +
                "                 // printing  age \n" +
                "                System.out.print(\"Age is \"+x); \n" +
                "            } \n" +
                "        }; \n" +
                "        oj1.getAge(); \n" +
                "    } ");

        CompilationUnit compilationUnit5 = StaticJavaParser.parse("interface MyInterface\n" +
                "{\n" +
                "   /* compiler will treat them as: \n" +
                "    * public abstract void method1();\n" +
                "    * public abstract void method2();\n" +
                "    */\n" +
                "   public void method1();\n" +
                "   public void method2();\n" +
                "}");

        CompilationUnit compilationUnit = StaticJavaParser.parse("/**\n" +
                " * Receives change upload using the Git receive-pack protocol.\n" +
                " *\n" +
                " * <p>Conceptually, most use of Gerrit is a push of some commits to refs/for/BRANCH. However, the\n" +
                " * receive-pack protocol that this is based on allows multiple ref updates to be processed at once.\n" +
                " * So we have to be prepared to also handle normal pushes (refs/heads/BRANCH), and legacy pushes\n" +
                " * (refs/changes/CHANGE). It is hard to split this class up further, because normal pushes can also\n" +
                " * result in updates to reviews, through the autoclose mechanism.\n" +
                " */\n" +
                "class ReceiveCommits {\n" +
                "  private static final FluentLogger logger = FluentLogger.forEnclosingClass();\n" +
                "\n" +
                "  private static final String CODE_REVIEW_ERROR =\n" +
                "      \"You need 'Push' rights to upload code review requests.\\n\"\n" +
                "          + \"Verify that you are pushing to the right branch.\";\n" +
                "  private static final String CANNOT_DELETE_CHANGES = \"Cannot delete from '\" + REFS_CHANGES + \"'\";\n" +
                "  private static final String CANNOT_DELETE_CONFIG =\n" +
                "      \"Cannot delete project configuration from '\" + RefNames.REFS_CONFIG + \"'\";\n" +
                "\n" +
                "  interface Factory {\n" +
                "    ReceiveCommits create(\n" +
                "        ProjectState projectState,\n" +
                "        IdentifiedUser user,\n" +
                "        ReceivePack receivePack,\n" +
                "        AllRefsWatcher allRefsWatcher,\n" +
                "        MessageSender messageSender,\n" +
                "        ResultChangeIds resultChangeIds);\n" +
                "  }" +
                "}");

        /*if (!compilationUnit.findRootNode().findFirst(BlockStmt.class).isPresent()) {
            System.out.println("NOT METHOD");
        }*/

        dfs2(compilationUnit.findRootNode());

        //Optional<ClassOrInterfaceDeclaration> classA = compilationUnit.getClassByName("A");

        MethodDeclaration methodDeclaration = (MethodDeclaration)bodyDeclaration;

        List<Parameter> list = methodDeclaration.findRootNode().findAll(Parameter.class, new Predicate<Parameter>() {
            @Override
            public boolean test(Parameter parameter) {
                Optional<Node> parent = parameter.getParentNode();
                if (!parent.isPresent()) {
                    return false;
                }

                return parent.get() == methodDeclaration.findRootNode();

            }
        });

        for (Parameter elem : list) {
            System.out.println(elem.getType().asString() + " " +
                    elem.getNameAsString() + " " + elem.getParentNode().get().getClass());
        }

        String[] arrayStrings = list.stream().map(elem -> elem.getType().asString()).toArray(String[]::new);
        String text = String.join("_", arrayStrings);

        System.out.println(text);

        //methodDeclaration.findRootNode().toString();

        //System.out.println(methodDeclaration.toString());
        //System.out.println(methodDeclaration.getChildNodes());

        /*for (Node node : methodDeclaration.getChildNodes()) {
            System.out.println("\nNODE: " + node.toString());

            System.out.println(node.getClass());

            if (node instanceof SimpleName) {
                SimpleName nameNode = (SimpleName)node;
                nameNode.setIdentifier("method");
            }
        }*/

        //System.out.println("\n_________\nRESULT:\n" + methodDeclaration.toString());

        //dfs2(methodDeclaration.findRootNode());
        //System.out.println("\n_________\nRESULT:\n" + methodDeclaration.toString());

        //CodeAbstractionTool abstractionTool = new CodeAbstractionTool();
        //abstractionTool.abstractMethod(bodyDeclaration.toString());
    }
}
