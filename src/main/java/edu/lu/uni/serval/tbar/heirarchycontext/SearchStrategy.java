package edu.lu.uni.serval.tbar.heirarchycontext;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Collections;
import java.util.Comparator;


import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.tbar.utils.Checker;
import edu.lu.uni.serval.tbar.heirarchycontext.ContextNode;
import edu.lu.uni.serval.tbar.utils.JsUtils;
import edu.lu.uni.serval.tbar.utils.FileHelper;

public class SearchStrategy {

    public static void getDonorCode(String buggyProject, String mode, ContextNode rootNode) {
        HashMap<ContextNode, Double> relatedStatements = nearNodeTypeSim(rootNode);
        List<Entry<ContextNode, Double>> listEntries = new ArrayList<Entry<ContextNode, Double>>(relatedStatements.entrySet());

		Collections.sort(listEntries, new Comparator<Entry<ContextNode, Double>>() {
			public int compare(Entry<ContextNode, Double> obj1, Entry<ContextNode, Double> obj2) {
				return obj2.getValue().compareTo(obj1.getValue());
			}
		});

        HashMap<String, ArrayList<Double>> donorCodeMap = new HashMap<>();
		for(Entry<ContextNode, Double> entry : listEntries) {
            HashSet<String> donorCodes = new HashSet<>();
            JsUtils.collectOriginalElements(entry.getKey().getNode(), donorCodes);
            for (String donorCode : donorCodes) {
                if (donorCodeMap.containsKey(donorCode)) {
                    donorCodeMap.get(donorCode).add(entry.getValue());
                } else {
                    ArrayList<Double> scoreList = new ArrayList<>();
                    scoreList.add(entry.getValue());
                    donorCodeMap.put(donorCode, scoreList);
                }
            }
		}
        HashMap<String, Double> rankedDonorCodes = new HashMap<>();
        for (Entry<String, ArrayList<Double>> entry : donorCodeMap.entrySet()) {
            int num = 0;
            double total = 0.0;
            for (Double value : entry.getValue()) {
                num++;
                total += value;
            }
            double average = total / (double) num;
            rankedDonorCodes.put(entry.getKey(), average);
        }

        List<Entry<String, Double>> donorCodeEntries = new ArrayList<Entry<String, Double>>(rankedDonorCodes.entrySet());
		Collections.sort(donorCodeEntries, new Comparator<Entry<String, Double>>() {
			public int compare(Entry<String, Double> obj1, Entry<String, Double> obj2) {
				return obj2.getValue().compareTo(obj1.getValue());
			}
		});

        String fileBuf = "";
        for (Entry<String, Double> entry : donorCodeEntries) {
            fileBuf += entry.getValue() + "," + entry.getKey() + "\n";
            // System.out.println(entry.getValue() + "," + entry.getKey();
        }
        File file = new File("/root/EffiGenC/Results/IngredientRank/" + buggyProject + "_" + mode + ".txt");
        FileHelper.createFile(file, fileBuf);
    }
    public static HashMap<ContextNode, Double> nearNodeTypeSim(ContextNode rootNode) {
        ITree targetParentNode = rootNode.getNode().getParent();
        String targetParentNodeType = targetParentNode.toShortString().split("@@")[0];
        HashMap<String, Integer> targetSiblingTypes = new HashMap<>();
        HashMap<ContextNode, ArrayList<Integer>> resultMap = new HashMap<>();
        getSiblingTypes(targetParentNode, targetSiblingTypes);
        for (ContextNode oneLevelNode : rootNode.getChildren()) {
            for (ContextNode twoLevelNode : oneLevelNode.getChildren()) {
                int score = 0;
                ITree parentNode = twoLevelNode.getNode().getParent();
                String parentNodeType = parentNode.toShortString().split("@@")[0];
                HashMap<String, Integer> siblingTypes = new HashMap<>();
                getSiblingTypes(parentNode, siblingTypes);
                if (parentNodeType.equals(targetParentNode))
                    score++;
                for (String type : siblingTypes.keySet()) {
                    if (targetSiblingTypes.keySet().contains(type))
                        score += siblingTypes.get(type) > targetSiblingTypes.get(type) ? targetSiblingTypes.get(type) : siblingTypes.get(type);
                }
                if (resultMap.keySet().contains(twoLevelNode)) {
                    resultMap.get(twoLevelNode).add(score);
                } else {
                    ArrayList<Integer> tempList = new ArrayList<>();
                    tempList.add(score);
                    resultMap.put(twoLevelNode, tempList);
                }
            }
        }
        HashMap<ContextNode, Double> result = new HashMap<>();
        for (ContextNode contextNode : resultMap.keySet()) {
            int i = 0;
            double average = 0;
            for (int temp : resultMap.get(contextNode)) {
                average += temp;
                i++;
            }
            result.put(contextNode, average / (double) i);
        }
        return result;
    }

    private static void getSiblingTypes(ITree parentNode, HashMap<String, Integer> siblingTypes) {
        for (ITree siblingNode : parentNode.getChildren()) {
            String siblingType = siblingNode.toShortString().split("@@")[0];
            if (siblingTypes.keySet().contains(siblingType)) {
                siblingTypes.put(siblingType, siblingTypes.get(siblingType) + 1);
            } else {
                siblingTypes.put(siblingType, 1);
            }
        }
    }
}