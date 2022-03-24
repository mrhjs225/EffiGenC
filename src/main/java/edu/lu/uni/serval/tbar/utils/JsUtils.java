package edu.lu.uni.serval.tbar.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;

import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.tbar.utils.Checker;

public class JsUtils {

	public static void staticSlicing(ArrayList<ITree> slicedStatementList, ArrayList<String> contextElementList, ITree node) {
		if (Checker.isStatement(node.getType())) {
			for (String contextElement : contextElementList) {
                if (node.getLabel().contains(contextElement)) {
                    System.out.println("Node:" + node.getLabel());
                }
            }
		}
		for(int i = 0; i < node.getChildren().size(); i++) {
			staticSlicing(slicedStatementList, contextElementList, node.getChildren().get(i));
		}
	}


	public static ITree getMethodNode(ITree node) {
		for(ITree parentNode : node.getParents()) {
			if(parentNode.getLabel().contains("MethodName:")) {
                return parentNode;
			}
		}
        return null;
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

    public static void hitRatio(HashSet<String> patchIngredient, String buggyProject, String mode) throws Exception {
		ArrayList<String> donorCodes = new ArrayList<>();
		FileReader donorCodeReader = null;
		BufferedReader donorCodeBuf = null;
		FileWriter hitRatioWriter = null;
		BufferedWriter hitRatioBuf = null;

        donorCodeReader = new FileReader(new File("/root/EffiGenC/Answerfiles/" + buggyProject.split("_")[0] + "/" + buggyProject.split("_")[1] + "/DonorCode.txt"));
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
        hitRatioWriter = new FileWriter("/root/EffiGenC/Results/HitRatio/HitRatio_" + mode + ".csv", true);
        hitRatioBuf = new BufferedWriter(hitRatioWriter);
        String tempStr = buggyProject + "," + hitNum + "," + donorCodes.size() + "," + patchIngredient.size() + "\n";
        hitRatioBuf.write(tempStr);
        hitRatioBuf.flush();

        donorCodeReader.close();
        donorCodeBuf.close();
        hitRatioWriter.close();
        hitRatioBuf.close();
    }

    public static void hitRatioOriginal(HashSet<String> patchIngredient, String buggyProject, String mode) throws Exception {
		ArrayList<String> donorCodes = new ArrayList<>();
		FileReader donorCodeReader = null;
		BufferedReader donorCodeBuf = null;
		FileWriter hitRatioWriter = null;
		BufferedWriter hitRatioBuf = null;

        donorCodeReader = new FileReader(new File("/root/EffiGenC/Answerfiles/" + buggyProject.split("_")[0] + "/" + buggyProject.split("_")[1] + "/DonorCode.txt"));
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
        hitRatioWriter = new FileWriter("/root/EffiGenC/Results/HitRatio/HitRatio_original_" + mode + ".csv", true);
        hitRatioBuf = new BufferedWriter(hitRatioWriter);
        String tempStr = buggyProject + "," + hitNum + "," + donorCodes.size() + "," + patchIngredient.size() + "\n";
        hitRatioBuf.write(tempStr);
        hitRatioBuf.flush();

        donorCodeReader.close();
        donorCodeBuf.close();
        hitRatioWriter.close();
        hitRatioBuf.close();
    }

    public static ArrayList<String> extractContextElement(ArrayList<ITree> contetElementNodes) {
        ArrayList<String> contextElementList  = new ArrayList<String>();

        for(ITree contextNode : contetElementNodes) {
            String nodeContent =  contextNode.toShortString().split("@@")[1];
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
        if (Checker.isSimpleName(targetTree.getType()) || Checker.isSimpleType(targetTree.getType()) ||
        (Checker.isMethodInvocation(targetTree.getType()) && targetTree.toShortString().contains("Name:")) ) {
            String tempStr = targetTree.toShortString().split("@@")[1];
            contetElementNodes.add(targetTree);
        }
        for (ITree childNode : targetTree.getChildren()) {
            extractNode(childNode, contetElementNodes);
        }
    }

}
