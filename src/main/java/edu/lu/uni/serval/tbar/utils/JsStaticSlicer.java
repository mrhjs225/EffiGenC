package edu.lu.uni.serval.tbar.utils;

import java.io.File;
import java.util.ArrayList;

import edu.lu.uni.serval.AST.ASTGenerator;
import edu.lu.uni.serval.AST.ASTGenerator.TokenType;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.tbar.utils.Checker;
import edu.lu.uni.serval.tbar.utils.JsUtils;


public class JsStaticSlicer {

    private static String projectPath;
    private static ArrayList<ITree> slicingElement;
    private static ArrayList<ITree> slicedStatement;
    private static ArrayList<String> overlapStatement;

    public JsStaticSlicer(String projectPath) {
        this.projectPath = projectPath;
        this.slicingElement = new ArrayList<>();
        this.slicedStatement = new ArrayList<>();
        this.overlapStatement = new ArrayList<>();
    }

    public static String getProjectPath() {
        return projectPath;
    }

    public static ArrayList<ITree> getSlicingElement() {
        return slicingElement;
    }

    public static ArrayList<ITree> getSlicedStatement() {
        return slicedStatement;
    }

    public static void staticSlicer(ITree targetNode) {
        staticBackwardSlicer(targetNode);
        staticForwardSlicer(targetNode);
    }

    
    public static void staticBackwardSlicer(ITree targetNode) {
        JsUtils.extractNode(targetNode, slicingElement);
        invocationSearcher(targetNode, 1);
    }
    
    public static void staticForwardSlicer(ITree targetNode) {
        JsUtils.extractNode(targetNode, slicingElement);
        invocationSearcher(targetNode, 2);
    }

    private static void invocationSearcher(ITree targetNode, int mode) {
        System.out.println("" + targetNode.toShortString());
        if (mode == 1) {
            backwardSlicer(targetNode);
        } else {
            forwardSlicer(targetNode);
        }
        ITree methodNode = JsUtils.findMethod(targetNode);
        String keyword = "";
        if (methodNode != null) {
            keyword = JsUtils.getMethodName(methodNode);
            if (keyword != null) {
                keyword = keyword.trim();
            }
        } else {
            ITree rootNode = JsUtils.findClass(targetNode);
            keyword = JsUtils.getClassName(rootNode);
            if (keyword != null) {
                keyword = keyword.trim();
            }
        }

        if (keyword.contains(":")) {
            keyword = keyword.split(":")[1];
        }

        ArrayList<String> projectFileList = new ArrayList<>();
		JsUtils.listUpFiles(new File(projectPath), projectFileList);

        for (String filePath: projectFileList) {
            File tempFile = new File(filePath);
            ITree projectFileTree = new ASTGenerator().generateTreeForJavaFile(tempFile, TokenType.EXP_JDT);
            findKeywordNode(projectFileTree, keyword, mode);
        }
    }

    private static void backwardSlicer(ITree targetNode) {      
        if (targetNode.getParent() == null) {
            return;
        }
        int nodeIndex = findNodeIndex(targetNode);
        int i = nodeIndex - 1;
        while(i >= 0) {
            ITree siblingNode = targetNode.getParent().getChild(i);
            if (isSlice(siblingNode)) {
                slicedStatement.add(siblingNode);
                overlapStatement.add(siblingNode.toShortString());
            }
            i--;
        }
        if (!Checker.isMethodDeclaration(targetNode.getParent().getType()) && (targetNode.getParent() != null)) {
            backwardSlicer(targetNode.getParent());
        }
    }

    private static void forwardSlicer(ITree targetNode) {      
        int nodeIndex = findNodeIndex(targetNode);
        int i = nodeIndex + 1;
        int childrenNumber = targetNode.getParent().getChildren().size(); 
        while(i < childrenNumber) {
            ITree siblingNode = targetNode.getParent().getChild(i);
            if (isSlice(siblingNode)) {
                slicedStatement.add(siblingNode);
                overlapStatement.add(siblingNode.toShortString());
            }
            i++;
        }
        if (!(Checker.isMethodDeclaration(targetNode.getParent().getType()) || targetNode.getParent().isRoot())) {
            forwardSlicer(targetNode.getParent());
        }
    }

    private static boolean isSlice(ITree targetNode) {
        if (!Checker.isPureStatement(targetNode.getType())) {
            return false;
        }
        ArrayList<ITree> identifiers = new ArrayList<>();
        JsUtils.extractNode(targetNode, identifiers);

        for (ITree identifier : identifiers) {
            for (ITree element : slicingElement) {
                if (identifier.getLabel().equals(element.getLabel())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int findNodeIndex(ITree targetnode) {
        // System.out.println(targetnode.toShortString());
        ITree parentNode = targetnode.getParent();
        int i = 0;
        for (ITree childNode : parentNode.getChildren()) {
            if (targetnode.equals(childNode)) {
                break;
            }
            i++;
        }
        return i;
    }

    public static void testPrint(ITree targetNode) {
        System.out.println("---------");
        System.out.println(targetNode.toShortString());
        System.out.println(targetNode.getLabel());

        for (ITree child : targetNode.getChildren()) {
            testPrint(child);
        }
    }

    public static ITree findRoot(ITree targetNode) {
        if (targetNode.isRoot())  {
            return targetNode;
        }
        return findRoot(targetNode.getParent());
    }


    private static void findKeywordNode(ITree node, String keyword, int mode) {
        if (Checker.isSimpleName(node.getType())) {
            if(node.toShortString().contains(":")) {
                if (node.toShortString().split(":")[1].trim().equals(keyword)) {
                    ITree stmtNode = JsUtils.findStatement(node);
                    if (stmtNode != null && !overlapStatement.contains(stmtNode.toShortString())) {
                        slicedStatement.add(stmtNode);
                        overlapStatement.add(stmtNode.toShortString());
                        invocationSearcher(stmtNode, mode);
                    }
                }
            } else {
                if (node.toShortString().split("@@")[1].trim().equals(keyword)) {
                    ITree stmtNode = JsUtils.findStatement(node);
                    if (stmtNode != null && !overlapStatement.contains(stmtNode.toShortString())) {
                        slicedStatement.add(stmtNode);
                        overlapStatement.add(stmtNode.toShortString());
                        invocationSearcher(stmtNode, mode);
                    }
                }
            }
        }
        for (ITree child : node.getChildren()) {
            findKeywordNode(child, keyword, mode);
        }
    }
   
}
