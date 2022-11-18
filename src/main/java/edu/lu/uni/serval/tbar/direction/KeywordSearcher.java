package edu.lu.uni.serval.tbar.direction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.tbar.AbstractFixer.SuspCodeNode;

public class KeywordSearcher {
    private String bugId;
    private List<SuspCodeNode> totalSuspNode;
    private ArrayList<ITree> keywordList;
    public String targetSpace; // Project, Package, File, Method
    private ArrayList<ITree> stmtList;
    private String projectPath;
    private HashSet<String> treeContents;
    private KeywordTree rootNode;
    private ArrayList<String> donorCodes;
    private HashMap<Integer, ArrayList<String>> searchTree;
    private String resultFileDir;
    private String treeInfoFileDir;

    public KeywordSearcher(String bugId, List<SuspCodeNode> totalSuspNode, String targetSpace, String projectPath) {
        this.bugId = bugId;
        this.totalSuspNode = totalSuspNode;
        this.keywordList = new ArrayList<>();
        this.targetSpace = targetSpace;
        this.projectPath = projectPath;
        this.stmtList = new ArrayList<>();
        this.treeContents = new HashSet<>();
        this.rootNode = new KeywordTree(null, "root", 1);
        this.donorCodes = new ArrayList<>();
        this.searchTree = new HashMap<>();
        this.resultFileDir = "/root/DIRECTION/Data/HitRatio/keyword_based_search.csv";
        this.treeInfoFileDir = "/root/DIRECTION/Data/HitRatio/keyword_based_search_treeinfo.csv";
    }

    public ArrayList<ITree> extractKeywords() {
        for (SuspCodeNode scn : this.totalSuspNode) {
            ITree suspStatementTree = scn.suspCodeAstNode;
            TreeUtil.extractNode(suspStatementTree, this.keywordList);
        }
        return this.keywordList;
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
        ArrayList<KeywordTree> targetLevelNodeList = new ArrayList<>();
        
        ArrayList<String> targetLevelStrList = new ArrayList<>();
        for (ITree secondLevelNode : keywordList) {
            String identifierStr = TreeUtil.getITreeName(secondLevelNode);
            if (!treeContents.contains(identifierStr)) {
                KeywordTree node = new KeywordTree(secondLevelNode, identifierStr, 2);
                String nodePackageName = "";
                node.setStmtContents(null);
                this.rootNode.childNodes.add(node);
                targetLevelNodeList.add(node);
                treeContents.add(identifierStr);
                targetLevelStrList.add(identifierStr);
            }
        }
        searchTree.put(2, targetLevelStrList);
        
        int level = 3;
        while (!this.stmtList.isEmpty()) {
            boolean isEnd = true;
            targetLevelStrList = new ArrayList<>();
            ArrayList<KeywordTree> nextLevelNodeList = new ArrayList<>();
            ArrayList<ITree> removedStmtList = new ArrayList<>();

            // 여기서 keywordnode에 대한 특정 level의 list 만들어야 할 듯
            for (KeywordTree targetNode : targetLevelNodeList) {
                for (ITree statement : this.stmtList) {
                    ArrayList<ITree> identifierList = new ArrayList<>();
                    TreeUtil.extractNode(statement, identifierList);

                    boolean tag = JsUtils.isListHasSameNode(targetNode.getNode(), identifierList);
                    isEnd = (isEnd && !tag);

                    if (tag == true) {
                        for (ITree identifier : identifierList) {
                            // tree 내용에 없으면 추가
                            String identifierStr = TreeUtil.getITreeName(identifier);
                            if (!treeContents.contains(identifierStr)) {
                                KeywordTree node = new KeywordTree(identifier, identifierStr, level);
                                node.setStmtContents(statement);
                                targetNode.childNodes.add(node);
                                nextLevelNodeList.add(node); 
                                treeContents.add(identifierStr);
                                targetLevelStrList.add(identifierStr);
                            }
                        }
                        // statement를 list에서 제거
                        removedStmtList.add(statement);
                    }
                }
            }
            searchTree.put(level, targetLevelStrList);
            if (isEnd == true) {
                break;
            }

            targetLevelNodeList = nextLevelNodeList;
            this.stmtList.removeAll(removedStmtList);
            level++;
        }

    }

    public void setDonorCodes(ArrayList<String> donorCodes) {
        this.donorCodes = donorCodes;
    }

    public KeywordTree getRootNode() {
        return this.rootNode;
    }

    public ArrayList<ITree> getKeywordList() {
        return this.keywordList;
    }

    public HashSet<String> getTreeContents() {
        return this.treeContents;
    }

    public void searchDonorCode() {
        String experimentResult = "";
        for (String donorCode : this.donorCodes) {
            int rank = 0;
            boolean tag = false;
            if (treeContents.contains(donorCode)) {
                for (Integer level : searchTree.keySet()) {
                    if (searchTree.get(level).contains(donorCode)) {
                        rank += searchTree.get(level).indexOf(donorCode);
                        tag = true;
                        // identifier, pass/fail, search space, rank, total node size, level
                        experimentResult += this.bugId + "," + donorCode + ",P," + this.targetSpace + "," + rank + "," + treeContents.size() + "," + level + "," + searchTree.size() + "\n";
                        // System.out.println(this.bugId + "," + donorCode + ",P," + this.targetSpace + "," + rank + "," + treeContents.size() + "," + level);
                        break;
                    }
                    rank += searchTree.get(level).size();
                }
            }
            if (tag == false) {
                experimentResult += this.bugId + "," + donorCode + ",F,,," + treeContents.size() + ",,\n";
                // System.out.println(this.bugId + "," + donorCode + ",F,,," + treeContents.size() + ",");
            }
        }
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(this.resultFileDir), true));
            bufferedWriter.write(experimentResult);
            bufferedWriter.close();
        } catch (Exception e) {}

        String treeInfo = this.bugId + ",";
        for (Integer level : searchTree.keySet()) {
            treeInfo += searchTree.get(level).size() + ",";
        }
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(this.treeInfoFileDir), true));
            bufferedWriter.write(treeInfo + "\n");
            bufferedWriter.close();
        } catch (Exception e) {}
    }

    public static void saveTree() {

    }
}
