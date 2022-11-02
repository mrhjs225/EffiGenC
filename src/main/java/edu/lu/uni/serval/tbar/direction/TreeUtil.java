package edu.lu.uni.serval.tbar.direction;

import java.util.ArrayList;
import java.util.List;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.tbar.AbstractFixer.SuspCodeNode;
import edu.lu.uni.serval.tbar.utils.Checker;

public class TreeUtil {

    public static ArrayList<ITree> collectProjectStmt() {
        return new ArrayList<>();
    }

    public static ArrayList<ITree> collectPackageStmt() {
        return new ArrayList<>();
    }

    public static ArrayList<ITree> collectFileStmt() {
        return new ArrayList<>();
    }

    public static ArrayList<ITree> collectMethodStmt(List<SuspCodeNode> totalSuspNode) {
        ArrayList<ITree> methodNodeList = new ArrayList<>();
        ArrayList<ITree> stmtList = new ArrayList<>();
        // Collect method node from suspicious nodes
        for (SuspCodeNode suspNode : totalSuspNode) {
            ITree methodNode = JsUtils.findMethod(suspNode.suspCodeAstNode);
            if (!methodNodeList.contains(methodNode)) {
                methodNodeList.add(methodNode);
            }
        }
        // Collect all statement from all method node
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

}
