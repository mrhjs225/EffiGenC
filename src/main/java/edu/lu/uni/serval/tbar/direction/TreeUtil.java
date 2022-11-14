package edu.lu.uni.serval.tbar.direction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import edu.lu.uni.serval.AST.ASTGenerator;
import edu.lu.uni.serval.AST.ASTGenerator.TokenType;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.tbar.AbstractFixer.SuspCodeNode;
import edu.lu.uni.serval.tbar.utils.Checker;

public class TreeUtil {

    public static ArrayList<ITree> collectStmtInProject(String projectPath) {
        ArrayList<String> projectFileList = new ArrayList<>();
        System.out.println(projectPath);
        JsUtils.findSubFileInPath(new File(projectPath), projectFileList);
        ArrayList<ITree> stmtList = new ArrayList<>();
        for (String filePath : projectFileList) {
            ITree fileRootNode = new ASTGenerator().generateTreeForJavaFile(new File(filePath), TokenType.EXP_JDT);
            collectStatement(stmtList, fileRootNode);
        }
        
        return stmtList;
    }

    public static ArrayList<ITree> collectStmtInPackage(List<SuspCodeNode> totalSuspNode) {       
        // We assume there is only one suspicious package.
        SuspCodeNode representStmt = totalSuspNode.get(0);
        String targetFilePath = representStmt.targetJavaFile.toString();
        int len = targetFilePath.split("/").length;
        int fileNameLen = targetFilePath.split("/")[len-1].length();
        int pathLen = targetFilePath.length();
        String packagePath = targetFilePath.substring(0, pathLen-fileNameLen);

        return collectStmtInProject(packagePath);
    }

    public static ArrayList<ITree> collectStmtInFile(List<SuspCodeNode> totalSuspNode) {
        ArrayList<ITree> fileNodeList = new ArrayList<>();
        ArrayList<ITree> stmtList = new ArrayList<>();
        // Collect file root node from suspicious nodes
        for (SuspCodeNode suspNode : totalSuspNode) {
            ITree fileNode = findFileRootNode(suspNode.suspCodeAstNode);
            if (!fileNodeList.contains(fileNode)) {
                fileNodeList.add(fileNode);
            }
        }
        // Collect all statement from all suspicious file
        for (ITree fileNode : fileNodeList) {
            collectStatement(stmtList, fileNode);
        }
        return stmtList;
    }

    public static ArrayList<ITree> collectStmtInMethod(List<SuspCodeNode> totalSuspNode) {
        ArrayList<ITree> methodNodeList = new ArrayList<>();
        ArrayList<ITree> stmtList = new ArrayList<>();

        // Collect method node from suspicious nodes
        for (SuspCodeNode suspNode : totalSuspNode) {
            ITree methodNode = findMethodNode(suspNode.suspCodeAstNode);
            if (!methodNodeList.contains(methodNode)) {
                methodNodeList.add(methodNode);
            }
        }

        // Collect all statement from all suspicious method
        for (ITree methodNode : methodNodeList) {
            collectStatement(stmtList, methodNode);
        }
        return stmtList;
    }

    public static void collectStatement(ArrayList<ITree> stmtList, ITree node) {
        if (Checker.isPureStatement(node.getType())) {
            stmtList.add(node);
        }
        for (ITree childNode : node.getChildren()) {
            collectStatement(stmtList, childNode);
        }
    }

    public static ITree findMethodNode(ITree node) {
        if (Checker.isMethodDeclaration(node.getType())) {
            return node;
        }
        if (node.getParent() == null) {
            return null;
        }
        return findMethodNode(node.getParent());
    }

    public static ITree findFileRootNode(ITree node) {
        if (node.isRoot()) {
            return node;
        }
        return findFileRootNode(node.getParent());
    }

    public static void extractContextNode(ITree targetTree, ArrayList<ITree> contextElementNodes) {
        if (Checker.isIfStatement(targetTree.getType())) {
            for (ITree childNode : targetTree.getChildren()) {
                if (Checker.isInfixExpression(childNode.getType())) {
                    extractNode(childNode, contextElementNodes);
                    break;
                }
            }
        } else {
            extractNode(targetTree, contextElementNodes);
        }
    }

    public static void extractNode(ITree targetTree, ArrayList<ITree> nodeList) {
        int nodeType = targetTree.getType();
        if (Checker.isSimpleName(nodeType) || Checker.isSimpleType(nodeType)
                || (Checker.isMethodInvocation(nodeType)
                        && targetTree.toShortString().contains("Name:"))) {
            nodeList.add(targetTree);
        }
        for (ITree childNode : targetTree.getChildren()) {
            extractNode(childNode, nodeList);
        }
    }

    public static String getITreeName(ITree targetNode) {
        String nodeStr = targetNode.toString();
        if (nodeStr.contains("MethodName:")) {
            return nodeStr.split(":")[1].trim();
        } else if (nodeStr.contains("@@Name:")) {
            return nodeStr.split(":")[1].trim();
        } else {
            return nodeStr.split("@@")[1].trim();
        }
    }

}
