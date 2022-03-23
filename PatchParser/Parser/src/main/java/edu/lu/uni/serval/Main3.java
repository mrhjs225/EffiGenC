package edu.lu.uni.serval;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.*;

import com.github.gumtreediff.actions.model.Action;

import edu.lu.uni.serval.gumtree.GumTreeComparer;
import edu.lu.uni.serval.BugCommit.parser.PatchParser;
import edu.lu.uni.serval.gumtree.regroup.HierarchicalActionSet;

public class Main3 {

	public static void main(String[] args) throws IOException {
		String basic_dir = "/root/EffiGenC/Answerfiles/";
		String project = args[0];
		int buggyNum = Integer.parseInt(args[1]);
		String before_dir = basic_dir + project + "/" + buggyNum + "/before/";
		String after_dir = basic_dir + project + "/" + buggyNum + "/after/";
		File files[] = new File(before_dir).listFiles();
		int changeAction = 0;
		ArrayList<Integer> changeOperation = new ArrayList<>();
		//System.out.print(project + "_" + buggyNum);
		//System.out.println("=-=-=-=-=-=-=-=-=-=-=");
		for (File file : files) {
			String fileName = file.toString().split("/")[file.toString().split("/").length - 1];
			System.out.print(project + "_" + buggyNum + "," + fileName);
			File beforeFile = new File(before_dir + fileName);
			File afterFile = new File(after_dir + fileName);
			List<HierarchicalActionSet> results = new PatchParser().parseChangedSourceCodeWithGumTreeSub(beforeFile, afterFile);
			new PatchParser().getBugPosition(results, beforeFile, afterFile);
			changeAction += results.size();
			for (HierarchicalActionSet result: results) {
				String actionStr = result.getActionString();
				if (actionStr.startsWith("INS")) {
					System.out.print("," + result.getFixStartLineNum());
				} else {
					String testStr = result.toString();
					System.out.print("," + result.getBugStartLineNum());
					//System.out.println(result.getBugEndLineNum());
					//System.out.println(result.getFixStartLineNum());
					//System.out.println(result.getFixEndLineNum());
				}
			}
			System.out.println("");
		}

		//String bufferStr = project + "_" + Integer.toString(buggyNum) + "," + Integer.toString(changeAction) + ",";
		//for (Integer tempInt : changeOperation) {
		//	bufferStr += Integer.toString(tempInt) + ",";
		//}
		//BufferedWriter bufWriter = new BufferedWriter(new FileWriter(new File(basic_dir + "changeActionDist.csv"), true));
		//bufWriter.write(bufferStr.substring(0, bufferStr.length() - 1) + "\n");
		//bufWriter.close();

		//System.out.println("===== " + project + " " + buggyNum + " done =====");
	}
}
