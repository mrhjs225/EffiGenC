package edu.lu.uni.serval.tbar.direction;

import java.util.ArrayList;
import java.util.List;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.tbar.AbstractFixer.SuspCodeNode;

public class KeywordSearcher {
    List<SuspCodeNode> totalSuspNode;
    ArrayList<ITree> keywordList;
    String targetSpace;

    public KeywordSearcher(List<SuspCodeNode> totalSuspNode, String targetSpace) {
        this.totalSuspNode = totalSuspNode;
        this.keywordList = new ArrayList<>();
        this.targetSpace = targetSpace;
    }

    public ArrayList<ITree> extractKeywords() {
        for (SuspCodeNode scn : this.totalSuspNode) {
            ITree suspStatementTree = scn.suspCodeAstNode;
            JsUtils.extractNode(suspStatementTree, keywordList);
        }
        return keywordList;
    }

    public void collectStatement() {

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
