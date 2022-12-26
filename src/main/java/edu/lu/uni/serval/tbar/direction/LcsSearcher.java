package edu.lu.uni.serval.tbar.direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import edu.lu.uni.serval.jdt.tree.ITree;

public class LcsSearcher {
    private String bugId;
    private List<SuspCodeNode> totalSuspNode;
    public String targetSpace; // Project, Package, File, Method
    private String projectPath;
    private HashMap<ITree, ArrayList<Double>> noContextScoreList;
    private HashMap<ITree, ArrayList<Double>> contextScoreList;
    private HashMap<ITree, Double> noContextAverageScore;
    private HashMap<ITree, Double> noContextMaxScore;
    private HashMap<ITree, Double> contextAverageScore;
    private HashMap<ITree, Double> contextMaxScore;
    private ArrayList<ITree> stmtList;
    private ArrayList<ITree> methodList;
    private ArrayList<String> donorCodes;

    public LcsSearcher(String bugId, List<SuspCodeNode> totalSuspNode, String targetSpace, String projectPath) {
        this.bugId = bugId;
        this.totalSuspNode = totalSuspNode;
        this.targetSpace = targetSpace;
        this.projectPath = projectPath;
        this.noContextScoreList = new HashMap<>();
        this.contextScoreList = new HashMap<>();
        this.noContextAverageScore = new HashMap<>();
        this.noContextMaxScore = new HashMap<>();
        this.contextAverageScore = new HashMap<>();
        this.contextMaxScore = new HashMap<>();
        this.stmtList = new ArrayList<>();
        this.methodList = new ArrayList<>();
        this.donorCodes = new ArrayList<>();
    }

    // To construct search space, it collect all statement from target search space
    public void collectSearchSpace() {
        if (targetSpace.equals("Project")) {
            this.stmtList = TreeUtil.collectNodeInProject(projectPath, "Statement");
            this.methodList = TreeUtil.collectNodeInProject(projectPath, "Method");
        } else if (targetSpace.equals("Package")) {
            this.stmtList = TreeUtil.collectNodeInPackage(totalSuspNode, "Statement");
            this.methodList = TreeUtil.collectNodeInPackage(totalSuspNode, "Method");
        } else if (targetSpace.equals("File")) {
            this.stmtList = TreeUtil.collectNodeInFile(totalSuspNode, "Statement");
            this.methodList = TreeUtil.collectNodeInFile(totalSuspNode, "Method");
        } else if (targetSpace.equals("Method")) {
            this.stmtList = TreeUtil.collectNodeInMethod(totalSuspNode, "Statement");
            this.methodList = TreeUtil.collectNodeInMethod(totalSuspNode, "Method");
        }
    }

    // calculate lcs score and average or max similarity
    public void calculateSimilarity() {
        // noContext
        for (SuspCodeNode scn : totalSuspNode) {
            ITree suspStatementNode = scn.suspCodeAstNode;
            String suspStatementStr = suspStatementNode.getLabel();
            for (ITree stmtNode : stmtList) {
                ArrayList<ITree> keywordList = new ArrayList<>();
                TreeUtil.extractNode(stmtNode, keywordList);
                // Calculate lcs score
                String targetStatementStr = stmtNode.getLabel();
                double lcsScore = lcsScoring(suspStatementStr,targetStatementStr);
                for (ITree identifier : keywordList) {
                    if (noContextScoreList.containsKey(identifier)) {
                        noContextScoreList.get(identifier).add(lcsScore);
                    } else {
                        ArrayList<Double> scores = new ArrayList<>();
                        scores.add(lcsScore);
                        noContextScoreList.put(identifier, scores);
                    }
                }
            }
        }

        // Context
        for (SuspCodeNode scn : totalSuspNode) {
            ITree suspStatementNode = scn.suspCodeAstNode;
            ITree suspMethodNode = TreeUtil.findMethodNode(suspStatementNode);
            if (suspMethodNode == null) {
                continue;
            }
            String suspMethodStr = suspMethodNode.getLabel();
            for (ITree methodNode : methodList) {
                ArrayList<ITree> keywordList = new ArrayList<>();
                TreeUtil.extractNode(methodNode, keywordList);
                // Calculate lcs score
                String targetMethodStr = methodNode.getLabel();
                double lcsScore = lcsScoring(suspMethodStr,targetMethodStr);
                for (ITree identifier : keywordList) {
                    if (contextScoreList.containsKey(identifier)) {
                        contextScoreList.get(identifier).add(lcsScore);
                    } else {
                        ArrayList<Double> scores = new ArrayList<>();
                        scores.add(lcsScore);
                        contextScoreList.put(identifier, scores);
                    }
                }
            }
        }

        // calculate nocontext average, max score
        for (ITree key : noContextScoreList.keySet()) {
            double averageValue = 0.0;
            double maxValue = 0.0;
            for(double value : noContextScoreList.get(key)) {
                averageValue += value;
                if (value > maxValue) {
                    maxValue = value;
                }
            }
            averageValue /= (double) noContextScoreList.get(key).size();
            noContextAverageScore.put(key, averageValue);
            noContextMaxScore.put(key, maxValue);
        }
        noContextAverageScore = sortMapByValue(noContextAverageScore);
        noContextMaxScore = sortMapByValue(noContextMaxScore);

        // calculate context average, max score
        for (ITree key : contextScoreList.keySet()) {
            double averageValue = 0.0;
            double maxValue = 0.0;
            for(double value : contextScoreList.get(key)) {
                averageValue += value;
                if (value > maxValue) {
                    maxValue = value;
                }
            }
            averageValue /= (double) contextScoreList.get(key).size();
            contextAverageScore.put(key, averageValue);
            contextMaxScore.put(key, maxValue);
        }
        contextAverageScore = sortMapByValue(contextAverageScore);
        contextMaxScore = sortMapByValue(contextMaxScore);
    }

    public void searchDonorCode() {
        for (String donorCode : this.donorCodes) {
            int rank = 0;
            // get key list
            
            // get index of donorcode

            // print
        }
    }

    // ref: https://codechacha.com/ko/java-sort-map/
    public LinkedHashMap<String, Double> sortMapByValue(Map<String, Double> map) {
        List<Map.Entry<String, Double>> entries = new LinkedList<>(map.entrySet());
        Collections.sort(entries, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));

        LinkedHashMap<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public void setDonorCodes(ArrayList<String> donorCodes) {
        this.donorCodes = donorCodes;
    }

    public static double lcsScoring(String str1, String str2) {
        int dp[][];

        dp = new int[str2.length() + 1][str1.length() + 1];

        for (int i = 1; i <= str2.length(); i++) {
            for (int j = 1; j <= str1.length(); j++) {
                if (str2.charAt(i - 1) != str1.charAt(j - 1)) {
                    dp[i][j] = Math.max(dp[i][j - 1], dp[i - 1][j]);
                } else {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                }
            }
        }
        int lcs = dp[str2.length()][str1.length()];
        double lcsScore = 0.0;
        if (str1.length() > str2.length()) {
            lcsScore = (double) lcs / str1.length();
        } else {
            lcsScore = (double) lcs / str2.length();
        }
        return lcsScore;
    }

}