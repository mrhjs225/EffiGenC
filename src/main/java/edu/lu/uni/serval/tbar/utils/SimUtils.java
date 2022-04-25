package edu.lu.uni.serval.tbar.utils;

import java.util.HashMap;
import edu.lu.uni.serval.jdt.tree.ITree;

import edu.lu.uni.serval.tbar.utils.Checker;
import edu.lu.uni.serval.tbar.utils.JsUtils;
import edu.lu.uni.serval.tbar.utils.FileUtils;



public class SimUtils {
    public static void lcsSimNoContext(ITree suspStatement, ITree node,
            HashMap<ITree, Double> noContextLcsScores) {
        if (Checker.isPureStatement(node.getType())) {
            noContextLcsScores.put(node, lcsScoring(suspStatement.getLabel(), node.getLabel()));
        }
        for (ITree childNode : node.getChildren()) {
            lcsSimNoContext(suspStatement, childNode, noContextLcsScores);
        }
    }

    public static void lcsSimContext(String suspMethodCode, ITree node, String filePath,
            HashMap<ITree, Double> contextLcsScores) {
        if (Checker.isMethodDeclaration(node.getType())) {
            // System.out.println("==============");
            String targetFileCode = FileUtils.getCodeFromFile(filePath);
            String targetMethodCode = JsUtils.getMethodString(targetFileCode, node);
            if (targetMethodCode != null) {
                contextLcsScores.put(node, lcsScoring(suspMethodCode, targetMethodCode));
            }
        }
        for (ITree childNode : node.getChildren()) {
            lcsSimContext(suspMethodCode, childNode, filePath, contextLcsScores);
        }
    }

    private static double lcsScoring(String str1, String str2) {
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

    public static void tfidfNoContextDataset(String datasetDir, int fileNum, ITree node) {
        // 재귀함수적으로 호출해서 부르기
    }
}
