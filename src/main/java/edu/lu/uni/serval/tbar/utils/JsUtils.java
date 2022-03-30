package edu.lu.uni.serval.tbar.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.tbar.utils.Checker;

public class JsUtils {

    public static void staticSlicing(ArrayList<ITree> slicedStatementList,
            ArrayList<String> contextElementList, ITree node) {
        if (Checker.isPureStatement(node.getType())
                || Checker.isComplexExpression(node.getType())) {
            for (String contextElement : contextElementList) {
                if (checkSimpleName(node, contextElement)) {
                    // System.out.println("==========");
                    // System.out.println("ContextElement: " + contextElement);
                    // System.out.println(node.getType());
                    // System.out.println(node.getId() + ":" + node.toTreeString());
                    slicedStatementList.add(node);
                }
            }
        }
        for (ITree childNode : node.getChildren()) {
            staticSlicing(slicedStatementList, contextElementList, childNode);
        }
    }

    private static boolean checkSimpleName(ITree node, String element) {
        int nodeType = node.getType();
        if (Checker.isSimpleName(nodeType) || Checker.isSimpleType(nodeType)
                || (Checker.isMethodInvocation(nodeType)
                        && node.toShortString().contains("Name:"))) {
            String nodeName = "";
            if (node.toShortString().contains("Name:")) {
                nodeName = node.getLabel().split(":")[1].trim();
            } else {
                nodeName = node.getLabel().trim();
            }
            if (nodeName.equals(element)) {
                return true;
            } else {
                return false;
            }
        }
        for (ITree childNode : node.getChildren()) {
            boolean flag = checkSimpleName(childNode, element);
            if (flag == true) {
                return true;
            }
        }
        return false;
    }

    public static String searchFile(File srcPath, String targetFile) {
        File files[] = srcPath.listFiles();

        for (int i = 0; i < files.length; i++) {
            File tempFile = files[i];
            if (!tempFile.isDirectory()) {
                if (tempFile.getName().equals(targetFile)) {
                    return tempFile.getAbsolutePath();
                }
            } else {
                String tempStr = searchFile(tempFile, targetFile);
                if (tempStr != null) {
                    return tempStr;
                }

            }
        }
        return null;
    }

    public static void listUpFiles(File path, ArrayList<String> fileList) {
        File files[] = path.listFiles();

        for (int i = 0; i < files.length; i++) {
            File tempFile = files[i];
            if (tempFile.isFile()) {
                if (tempFile.getAbsolutePath().endsWith(".java")) {
                    fileList.add(tempFile.getAbsolutePath());
                }
            } else if (tempFile.isDirectory()) {
                listUpFiles(tempFile, fileList);
            }
        }
    }

    public static ITree findMethodNode(ITree node) {
        if (Checker.isMethodDeclaration(node.getType())) {
            return node;
        }
        return findMethodNode(node.getParent());
    }

    public static void hitRatio(HashSet<String> patchIngredient, String buggyProject, String mode)
            throws Exception {
        ArrayList<String> donorCodes = new ArrayList<>();
        FileReader donorCodeReader = null;
        BufferedReader donorCodeBuf = null;
        FileWriter hitRatioWriter = null;
        BufferedWriter hitRatioBuf = null;

        donorCodeReader =
                new FileReader(new File("/root/EffiGenC/Answerfiles/" + buggyProject.split("_")[0]
                        + "/" + buggyProject.split("_")[1] + "/DonorCode.txt"));
        donorCodeBuf = new BufferedReader(donorCodeReader);
        String line = null;

        while ((line = donorCodeBuf.readLine()) != null) {
            donorCodes = new ArrayList<>(Arrays.asList(line.split(",")));
        }

        int hitNum = 0;
        for (String donorCode : donorCodes) {
            if (patchIngredient.contains(donorCode)) {
                hitNum++;
            }
        }
        hitRatioWriter =
                new FileWriter("/root/EffiGenC/Results/HitRatio/HitRatio_" + mode + ".csv", true);
        hitRatioBuf = new BufferedWriter(hitRatioWriter);
        String tempStr = buggyProject + "," + hitNum + "," + donorCodes.size() + ","
                + patchIngredient.size() + "\n";
        hitRatioBuf.write(tempStr);
        hitRatioBuf.flush();

        donorCodeReader.close();
        donorCodeBuf.close();
        hitRatioWriter.close();
        hitRatioBuf.close();
    }

    public static void hitRatioOriginal(HashSet<String> patchIngredient, String buggyProject,
            String mode) throws Exception {
        ArrayList<String> donorCodes = new ArrayList<>();
        FileReader donorCodeReader = null;
        BufferedReader donorCodeBuf = null;
        FileWriter hitRatioWriter = null;
        BufferedWriter hitRatioBuf = null;

        donorCodeReader =
                new FileReader(new File("/root/EffiGenC/Answerfiles/" + buggyProject.split("_")[0]
                        + "/" + buggyProject.split("_")[1] + "/DonorCode.txt"));
        donorCodeBuf = new BufferedReader(donorCodeReader);
        String line = null;

        while ((line = donorCodeBuf.readLine()) != null) {
            donorCodes = new ArrayList<>(Arrays.asList(line.split(",")));
        }

        int hitNum = 0;
        for (String donorCode : donorCodes) {
            if (patchIngredient.contains(donorCode)) {
                hitNum++;
            }
        }
        hitRatioWriter = new FileWriter(
                "/root/EffiGenC/Results/HitRatio/HitRatio_original_" + mode + ".csv", true);
        hitRatioBuf = new BufferedWriter(hitRatioWriter);
        String tempStr = buggyProject + "," + hitNum + "," + donorCodes.size() + ","
                + patchIngredient.size() + "\n";
        hitRatioBuf.write(tempStr);
        hitRatioBuf.flush();

        donorCodeReader.close();
        donorCodeBuf.close();
        hitRatioWriter.close();
        hitRatioBuf.close();
    }

    public static ArrayList<String> extractContextElement(ArrayList<ITree> contetElementNodes) {
        ArrayList<String> contextElementList = new ArrayList<String>();

        for (ITree contextNode : contetElementNodes) {
            String nodeContent = contextNode.toShortString().split("@@")[1];
            if (nodeContent.contains("Name:")) {
                contextElementList.add(nodeContent.split(":")[1].trim());
            } else {
                contextElementList.add(nodeContent.trim());
            }
        }
        return contextElementList;
    }

    public static void extractContextNode(ITree targetTree, ArrayList<ITree> contetElementNodes) {
        if (Checker.isIfStatement(targetTree.getType())) {
            for (ITree childNode : targetTree.getChildren()) {
                if (Checker.isInfixExpression(childNode.getType())) {
                    extractNode(childNode, contetElementNodes);
                    break;
                }
            }
        } else {
            extractNode(targetTree, contetElementNodes);
        }
    }

    public static void extractNode(ITree targetTree, ArrayList<ITree> contetElementNodes) {
        int nodeType = targetTree.getType();
        if (Checker.isSimpleName(nodeType) || Checker.isSimpleType(nodeType)
                || (Checker.isMethodInvocation(nodeType)
                        && targetTree.toShortString().contains("Name:"))) {
            String tempStr = targetTree.toShortString().split("@@")[1];
            contetElementNodes.add(targetTree);
        }
        for (ITree childNode : targetTree.getChildren()) {
            extractNode(childNode, contetElementNodes);
        }
    }

    public static void levenRanking(ArrayList<ITree> slicedStatementList, ITree suspStatementTree,
            HashMap<ITree, Double> rankedStatement) {
        String suspStatementStr = suspStatementTree.getLabel();
        for (ITree slicedStatement : slicedStatementList) {
            String slicedStatementStr = slicedStatement.getLabel();
            rankedStatement.put(slicedStatement, similarity(suspStatementStr, slicedStatementStr));
            break;
        }
        // List<Entry<ITree, Double>> listEntries = new ArrayList<Entry<ITree, Double>>(rankedStatement.entrySet());
        // Collections.sort(listEntries, new Comparator<Entry<ITree, Double>>() {
        //     public int compare(Entry<ITree, Double> obj1, Entry<ITree, Double> obj2) {
        //         return obj2.getValue().compareTo(obj1.getValue());
        //     }
        // });
    }

    private static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;

        if (s1.length() < s2.length()) {
            longer = s2;
            shorter = s1;
        }

        int longerLength = longer.length();
        if (longerLength == 0)
            return 1.0;
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    private static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
        int[] costs = new int[s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
                int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];

                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }

                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }

            if (i > 0)
                costs[s2.length()] = lastValue;
        }

        return costs[s2.length()];
    }
}
