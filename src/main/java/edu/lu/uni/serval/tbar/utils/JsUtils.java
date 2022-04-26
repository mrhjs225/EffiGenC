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
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Comparator;


import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.tbar.utils.Checker;

public class JsUtils {

    private static String donorCodesFileDir = "/root/DIRECTION/Data/DonorCodes.txt";
    private static String hitRatioResultFolderDir = "/root/DIRECTION/Data/HitRatio/";

    public static void keywordBasedSearch(ArrayList<ITree> slicedStatementList,
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
            keywordBasedSearch(slicedStatementList, contextElementList, childNode);
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
            nodeList.add(targetTree);
        }
        for (ITree childNode : targetTree.getChildren()) {
            extractNode(childNode, nodeList);
        }
    }

    public static void levenDist(ArrayList<ITree> slicedStatementList, ITree suspStatementTree,
            HashMap<ITree, Double> scoredStatements) {
        String suspStatementStr = suspStatementTree.getLabel();
        for (ITree slicedStatement : slicedStatementList) {
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
            BufferedReader donorCodesReader =
                    new BufferedReader(new FileReader(new File(donorCodesFileDir)));
            String line = "";
            while ((line = donorCodesReader.readLine()) != null) {
                try {
                    if (line.split("@").length > 1 && line.split("@")[0].equals(buggyProject)) {
                        donorCodes = new ArrayList<>(
                                Arrays.asList(line.split("@")[1].trim().split(",")));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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

    public static void getPatchIngredient(ArrayList<String> contextElementList,
            HashMap<ITree, Double> scoredStatements, HashSet<String> patchIngredients) {
        List<Entry<ITree, Double>> listEntries =
                new ArrayList<Entry<ITree, Double>>(scoredStatements.entrySet());
        HashMap<String, ArrayList<Double>> ingredientScoreList = new HashMap<>();
        HashMap<String, Double> scoredIngredients = new HashMap<>();
        Collections.sort(listEntries, new Comparator<Entry<ITree, Double>>() {
            public int compare(Entry<ITree, Double> obj1, Entry<ITree, Double> obj2) {
                return obj2.getValue().compareTo(obj1.getValue());
            }
        });
        for (Entry<ITree, Double> entry : listEntries) {
            ArrayList<ITree> targetNodeList = new ArrayList<>();
            extractNode(entry.getKey(), targetNodeList);

            for (ITree targetNode : targetNodeList) {
                String targetStr = targetNode.getLabel().trim();
                if (targetStr.contains("Name:")) {
                    targetStr = targetStr.split(":")[1].trim();
                }
                if (ingredientScoreList.containsKey(targetStr)) {
                    ingredientScoreList.get(targetStr).add(entry.getValue());
                } else {
                    ArrayList<Double> scoreList = new ArrayList<>();
                    scoreList.add(entry.getValue());
                    ingredientScoreList.put(targetStr, scoreList);
                }
            }
        }

        // Converting list to single score by calculating average.
        for (HashMap.Entry<String, ArrayList<Double>> entry : ingredientScoreList.entrySet()) {
            double final_score = 0.0;
            for (double single_score : entry.getValue()) {
                final_score += single_score;
            }
            final_score /= (double) entry.getValue().size();

            scoredIngredients.put(entry.getKey(), final_score);
        }

        // Patch ingredient prioritization
        List<Entry<String, Double>> ingredientEntries =
                new ArrayList<Entry<String, Double>>(scoredIngredients.entrySet());
        Collections.sort(ingredientEntries, new Comparator<Entry<String, Double>>() {
            public int compare(Entry<String, Double> obj1, Entry<String, Double> obj2) {
                return obj2.getValue().compareTo(obj1.getValue());
            }
        });
        for (Entry<String, Double> entry : ingredientEntries) {
            patchIngredients.add(entry.getKey());
        }
    }

    public static void hitRatio(String buggyProject, ArrayList<String> donorCodes,
            HashSet<String> patchIngredients, String resultFileName) {
        String hitRatioResultDir = hitRatioResultFolderDir + resultFileName + ".csv";
        for (String donorCode : donorCodes) {
            boolean flag = false;
            int rank = 1;
            for (String patchIngredient : patchIngredients) {
                if (donorCode.equals(patchIngredient)) {
                    flag = true;
                    try {
                        BufferedWriter hitRatioWriter = new BufferedWriter(
                                new FileWriter(new File(hitRatioResultDir), true));
                        hitRatioWriter.write(buggyProject + "," + donorCode + "," + rank + ","
                                + patchIngredients.size() + "\n");
                        hitRatioWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                rank++;
            }
            if (flag == false) {
                try {
                    BufferedWriter hitRatioWriter =
                            new BufferedWriter(new FileWriter(new File(hitRatioResultDir), true));
                    hitRatioWriter.write(buggyProject + "," + donorCode + ",fail,"
                            + patchIngredients.size() + "\n");
                    hitRatioWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static ITree findStatement(ITree node) {
        if (Checker.isPureStatement(node.getType())) {
            return node;
        }
        if (node.getParent() == null) {
            return null;
        }
        return findStatement(node.getParent());
    }

    public static ITree findMethod(ITree node) {
        for (ITree parentNode : node.getParents()) {
            if (parentNode.getLabel().contains("MethodName:")) {
                return parentNode;
            }
        }
        return null;
    }

    public static String getMethodName(ITree methodNode) {
        for (ITree childNode : methodNode.getChildren()) {
            if (childNode.toShortString().contains("42@@MethodName:")) {
                return childNode.toShortString();
            }
        }
        return null;
    }

    public static ITree findClass(ITree targetNode) {
        for (ITree parentNode : targetNode.getParents()) {
            if (parentNode.getLabel().contains("ClassName:")) {
                return parentNode;
            }
        }
        return null;
    }

    // except two class in one file
    public static String getClassName(ITree classNode) {
        for (ITree childNode : classNode.getChildren()) {
            if (childNode.toShortString().contains("42@@ClassName:")) {
                return childNode.toShortString();
            }
        }
        return null;
    }

    public static void testNodePrint(ITree node) {
        if (Checker.isMethodDeclaration(node.getType())) {
            System.out.println("================================");
            System.out.println(node.toShortString());
            System.out.println(node.getSize());
            System.out.println(node.getLength());
            System.out.println(node.getHeight());
            System.out.println(node.getPos());
            System.out.println(node.getEndPos());
        }
        // System.out.println(node.toShortString());
        for (ITree childNode: node.getChildren()) {
            testNodePrint(childNode);
        }
    }

    public static List<String> getMethodCodeList(String code) {
		List<String> result = new ArrayList<String>();
		code = code.replaceAll("private", "public");
		String[] items = code.split("public");
		for (int j = 1; j < items.length; j++) {
			String item = items[j];
			int startPoint = item.indexOf('{') + 1;
			int braceCount = 1;
			for (int i = startPoint; i < item.length(); i++) {
				if (item.charAt(i) == '}') {
					if (--braceCount == 0) {
						result.add(item.substring(0, i + 1));
						break;
					}
				}
				if (item.charAt(i) == '{') {
					braceCount++;
				}
			}
		}
		return result;
	}

    public static String getMethodString(String code, ITree methodNode) {
        List<String> getMethodList = getMethodCodeList(code);
        // System.out.println(getMethodList.size());
        String methodName = getMethodName(methodNode);
        String arguments = methodNode.toShortString().split("@@")[3].split(":")[1].trim();
        ArrayList<String> argumentList = new ArrayList<>();
        methodName = methodName.split(":")[1].trim();

        if (!arguments.equals("null")) {
            int i = 0;
            String argument = "";
            for (String tempString : arguments.split("\\+")) {
                if (i == 0) {
                    argument += tempString;
                    i++;
                } else {
                    argumentList.add(argument + " " + tempString);
                    i = 0;
                    argument = "";
                }
            }
        }
        for (String methodCode : getMethodList) {
            // System.out.println("=-=-=-=-=-=\n" + methodCode);
            if (methodCode.contains("{")) {
                String tempStr = methodCode.substring(0, methodCode.indexOf("{")).trim();
                if (arguments.equals("null")) {
                    tempStr = tempStr.replaceAll(" ", "");
                    // System.out.println("=======\n"+tempStr);
                    // System.out.println("---\n"+methodName);
                    // System.out.println("-----\n" + tempStr.contains(methodName+"()"));
                    if (tempStr.contains(methodName + "()")) {
                        return methodCode;
                    }
                } else {
                    if (tempStr.contains(methodName)) {
                        boolean tag = true;
                        for (String argument : argumentList) {
                            if (!(tempStr.contains(argument+",") || tempStr.contains(argument + " ") || tempStr.contains(argument + ")"))) {
                                tag = false;
                                break;
                            }
                        }
                        if (tag == true) {
                            return methodCode;
                        }
                    }
                }
            }
        }
        return null;
    }
}
