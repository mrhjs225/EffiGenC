package edu.lu.uni.serval.tbar.direction;

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

public class DonorCodeAnalyze {
    public static void findDonorCodes(ITree node, ArrayList<String> donorCodes, HashMap<String, ArrayList<ITree>> donorCodeStmt) {
        if (Checker.isPureStatement(node.getType())) {
            ArrayList<ITree> candidateIdentifiers = new ArrayList<>();

            JsUtils.extractNode(node, candidateIdentifiers);

            for (ITree candidateIdentifier : candidateIdentifiers) {
                String nodeStr = candidateIdentifier.getLabel();
                if (nodeStr.contains("Name:")) {
                    nodeStr = nodeStr.split(":")[1].trim();
                }
                for (String donorCode : donorCodes) {
                    if (nodeStr.equals(donorCode)) {
                        if (donorCodeStmt.containsKey(donorCode)) {
                            donorCodeStmt.get(donorCode).add(node);
                        } else {
                            ArrayList<ITree> stmt = new ArrayList<>();
                            stmt.add(node);
                            donorCodeStmt.put(donorCode, stmt);
                        }
                        break;
                    }
                }
            }
        }
        for (ITree childNode : node.getChildren()) {
            findDonorCodes(childNode, donorCodes, donorCodeStmt);
        }
    }

    public static void printDonorCodes(String bugId, ArrayList<String> donorCodes, HashMap<String, ArrayList<ITree>> donorCodeStmt) {
        String baseDir = "/root/DIRECTION/Data/DonorCode/";
        String bugDir = baseDir + bugId + "/";
        File bugFolder = new File(bugDir);
        if (!bugFolder.exists()) {
            try {
                bugFolder.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (String donorCode : donorCodes) {
            String donorCodeFolderDir = bugDir + donorCode + "/";
            File dnorCodeFolder = new File(donorCodeFolderDir);
            if (!dnorCodeFolder.exists()) {
                try {
                    dnorCodeFolder.mkdir();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            int i = 0;
            for (ITree stmt : donorCodeStmt.get(donorCode)) {
                
                String stmtDir = donorCodeFolderDir + i;
                i++;
                try {
                    BufferedWriter bufWriter = new BufferedWriter(new FileWriter(new File(stmtDir)));
                    bufWriter.write(stmt.toShortString());
                    bufWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void findDonorCode(ITree node, ArrayList<String> donorCodes, String buggyProject) {
        if (Checker.isPureStatement(node.getType())) {
            ArrayList<ITree> candidateIdentifiers = new ArrayList<>();
            JsUtils.extractNode(node, candidateIdentifiers);

            for (ITree candidateIdentifier : candidateIdentifiers) {
                String nodeStr = candidateIdentifier.getLabel();
                if (nodeStr.contains("Name:")) {
                    nodeStr = nodeStr.split(":")[1].trim();
                }
                for (String donorCode : donorCodes) {
                    if (nodeStr.equals(donorCode)) {
                        try {
                            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("/root/DIRECTION/Data/globalsearch.csv"), true));
                            bufferedWriter.write(buggyProject + "," + donorCode + ",o\n");
                            bufferedWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        donorCodes.remove(donorCode);
                        break;
                    }
                }
            }
        }
        for (ITree childNode : node.getChildren()) {
            findDonorCode(childNode, donorCodes, buggyProject);
        }
    }
}