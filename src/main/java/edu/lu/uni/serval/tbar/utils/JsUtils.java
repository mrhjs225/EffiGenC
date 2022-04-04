package edu.lu.uni.serval.tbar.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Comparator;


import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.tbar.utils.Checker;

public class JsUtils {

    private static String donorCodesFileDir = "/root/DIRECTION/Data/DonorCodes.txt";

    public static void staticSlicing(ArrayList<ITree> slicedStatementList,
            ArrayList<String> contextElementList, ITree node) {
        if (Checker.isPureStatement(node.getType())
                || Checker.isComplexExpression(node.getType())) {
            for (String contextElement : contextElementList) {
                if (checkSimpleName(node, contextElement)) {
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

    public static ArrayList<String> extractContextElement(ArrayList<ITree> contextElementNodes) {
        ArrayList<String> contextElementList = new ArrayList<String>();

        for (ITree contextNode : contextElementNodes) {
            String nodeContent = contextNode.toShortString().split("@@")[1];
            if (nodeContent.contains("Name:")) {
                contextElementList.add(nodeContent.split(":")[1].trim());
            } else {
                contextElementList.add(nodeContent.trim());
            }
        }
        return contextElementList;
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
            String tempStr = targetTree.toShortString().split("@@")[1];
            nodeList.add(targetTree);
        }
        for (ITree childNode : targetTree.getChildren()) {
            extractNode(childNode, nodeList);
        }
    }

    public static void levenDist(ArrayList<ITree> slicedStatementList, ITree suspStatementTree,
            HashMap<ITree, Double> scoredStatements) {
        // System.out.println("slicedStatement size:" + slicedStatementList.size());
        String suspStatementStr = suspStatementTree.getLabel();
        for (ITree slicedStatement : slicedStatementList) {
            // System.out.println("count");
            String slicedStatementStr = slicedStatement.getLabel();
            scoredStatements.put(slicedStatement, similarity(suspStatementStr, slicedStatementStr));
        }
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

    public static ArrayList<String> getDonorCodes(String buggyProject) {
        ArrayList<String> donorCodes = new ArrayList<>();
        try {
            BufferedReader donorCodesReader = new BufferedReader(new FileReader(new File(donorCodesFileDir)));
            String line = "";
            while((line = donorCodesReader.readLine()) != null) {
                if (line.split("@")[0].trim().equals(buggyProject)) {
                    donorCodes =  new ArrayList<>(Arrays.asList(line.split("@")[1].trim().split(",")));
                }
            }
            donorCodesReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return donorCodes;
    }

    public static void getPatchIngredient(ArrayList<String> contextElementList, HashMap<ITree, Double> scoredStatements, HashSet<String> patchIngredient) {
        System.out.println("scoredStatements size: " + scoredStatements.size());
        List<Entry<ITree, Double>> listEntries = new ArrayList<Entry<ITree,Double>>(scoredStatements.entrySet());
        Collections.sort(listEntries, new Comparator<Entry<ITree, Double>>() {
            public int compare(Entry<ITree, Double> obj1, Entry<ITree, Double> obj2) {
                return obj2.getValue().compareTo(obj1.getValue());
            }
        });
        for (Entry<ITree, Double> entry: listEntries) {
            ArrayList<ITree> targetNodeList = new ArrayList<>();
            extractNode(entry.getKey(), targetNodeList);
            boolean flag = false;

            for (String contextElement: contextElementList) {
                for (ITree targetNode : targetNodeList) {
                    if (contextElement.equals(targetNode.getLabel().trim())) {
                        flag = true;
                        break;
                    }
                }
                if (flag == true) {
                    break;
                }
            }

            if (flag == true) {
                for (ITree targetNode : targetNodeList) {
                    String targetStr = targetNode.getLabel().trim();
                    if (targetStr.contains("Name:")) {
                        targetStr = targetStr.split(":")[1].trim();
                    }
                    patchIngredient.add(targetStr);
                }
            }
            // break;
        }
        System.out.println("patchingredient size: " + patchIngredient.size());
        for (String ingredient : patchIngredient) {
            System.out.println(ingredient);
        }
    }

    public static void hitRatiodIRECTION() {
        
    }
}
