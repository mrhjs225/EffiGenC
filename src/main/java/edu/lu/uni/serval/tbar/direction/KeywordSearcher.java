package edu.lu.uni.serval.tbar.direction;

import java.util.ArrayList;
import java.util.List;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.tbar.AbstractFixer.SuspCodeNode;

public class KeywordSearcher {
    List<SuspCodeNode> totalSuspNode;
    ArrayList<ITree> keywordList;
    String targetSpace; // Project, Package, File, Method
    ArrayList<ITree> stmtList;


    public KeywordSearcher(List<SuspCodeNode> totalSuspNode, String targetSpace) {
        this.totalSuspNode = totalSuspNode;
        this.keywordList = new ArrayList<>();
        this.targetSpace = targetSpace;
        this.stmtList = new ArrayList<>();
    }

    public ArrayList<ITree> extractKeywords() {
        for (SuspCodeNode scn : this.totalSuspNode) {
            ITree suspStatementTree = scn.suspCodeAstNode;
            JsUtils.extractNode(suspStatementTree, keywordList);
        }
        return keywordList;
    }

    public ArrayList<ITree> collectStatement() {
        if (targetSpace.equals("Method")) {
            return collectMethodStmt();
        } else if (targetSpace.equals("File")) {
            return collectFileStmt();
        } else if (targetSpace.equals("Package")) {
            return collectPackageStmt();
        } else if (targetSpace.equals("Project")) {
            return collectProjectStmt();
        } else {
            return new ArrayList<>();
        }
    }

    private ArrayList<ITree> collectMethodStmt() {
        ArrayList<ITree> methodNodeList = new ArrayList<>();
        for (ITree suspNode : this.totalSuspNode) {
            ITree methodNode = JsUtils.findMethod(suspNode);
            methodNodeList.add(methodNode);
        }
        return new ArrayList<>();
    }

    private ArrayList<ITree> collectFileStmt() {
        return new ArrayList<>();
    }

    private ArrayList<ITree> collectPackageStmt() {
        return new ArrayList<>();
    }

    private ArrayList<ITree> collectProjectStmt() {
        return new ArrayList<>();
    }

    public static void makeTree() {
        // Collect statement which include keyword from target space.


        // Add ingredient in tree


        // Add ingredient in tree ingredient set


        // repeat the process to next level keyword until there is no statement in target space.

    }

    public static void searchDonorCode() {

    }

    public static void saveTree() {

    }
}
