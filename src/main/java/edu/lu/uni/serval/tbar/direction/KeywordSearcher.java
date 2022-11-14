package edu.lu.uni.serval.tbar.direction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.tbar.AbstractFixer.SuspCodeNode;

public class KeywordSearcher {
    List<SuspCodeNode> totalSuspNode;
    ArrayList<ITree> keywordList;
    String targetSpace; // Project, Package, File, Method
    ArrayList<ITree> stmtList;
    String projectPath;
    HashSet<String> treeContents;
    KeywordTree rootNode;

    public KeywordSearcher(List<SuspCodeNode> totalSuspNode, String targetSpace, String projectPath) {
        this.totalSuspNode = totalSuspNode;
        this.keywordList = new ArrayList<>();
        this.targetSpace = targetSpace;
        this.stmtList = new ArrayList<>();
        this.treeContents = new HashSet<>();
        this.rootNode = new KeywordTree(null, "root", 1);
    }

    public ArrayList<ITree> extractKeywords() {
        for (SuspCodeNode scn : this.totalSuspNode) {
            ITree suspStatementTree = scn.suspCodeAstNode;
            JsUtils.extractNode(suspStatementTree, keywordList);
        }
        return keywordList;
    }

    // To construct search space, it collect all statement from target search space
    public ArrayList<ITree> collectSearchSpace() {
        if (targetSpace.equals("Project")) {
            stmtList = TreeUtil.collectStmtInProject(projectPath);
        } else if (targetSpace.equals("Package")) {
            stmtList = TreeUtil.collectStmtInPackage(totalSuspNode);
        } else if (targetSpace.equals("File")) {
            stmtList = TreeUtil.collectStmtInFile(totalSuspNode);
        } else if (targetSpace.equals("Method")) {
            stmtList = TreeUtil.collectStmtInMethod(totalSuspNode);
        }
        return stmtList;
    }


    public void makeTree() {
        ArrayList<ITree> targetLevelNodeList = new ArrayList<>();
        
        for (ITree secondLevelNode : keywordList) {
            KeywordTree node = new KeywordTree(secondLevelNode, secondLevelNode.toString(), 2);
            String nodePackageName = "";
            node.setStmtContents(null);
            this.rootNode.childNodes.add(node);
            targetLevelNodeList.add(node);
            treeContents.add(secondLevelNode.toString());
        }

        int level = 3;
        while (!this.stmtList.isEmpty()) {
            boolean isEnd = True;
            ArrayList<ITree> nextLevelNodeList = new ArrayList<>();

            // 여기서 keywordnode에 대한 특정 level의 list 만들어야 할 듯
            for (ITree targetNode : targetLevelNodeList) {
                for (ITree statement : this.stmtList) {
                    ArrayList<ITree> identifierList = new ArrayList<>();
                    TreeUtil.extractNode(statement, identifierList);

                    boolean tag = JsUtils.isListHasSameNode(targetNode, identifierList);
                    isEnd = (isEnd && !tag);

                    if (tag == True) {
                        for (ITree identifier : identifierList) {
                            // tree 내용에 없으면 추가
                            if (!treeContents.contains(identifier.toString())) {
                                KeywordTree node = new KeywordTree(identifier, identifier.toString(), level);
                                node.setStmtContents(statement);
                                targetNode.childNodes.add(node);
                                nextLevelNodeList.add(node); 
                                treeContents.add(identifier.toString());
                            }
                        }
                        // statement를 list에서 제거
                        this.stmtList.remove(statement);
                    }
                }
            }
            if (isEnd == True) {
                break;
            }

            // TODO: deep copy 안될수도 있음
            targetLevelNodeList = nextLevelNodeList;
            level++;
        }

    }

    public KeywordTree getRootNode() {
        return this.rootNode;
    }

    public static void searchDonorCode() {

    }

    public static void saveTree() {

    }
}
