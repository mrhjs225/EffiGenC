package edu.lu.uni.serval.tbar.main;

import java.util.LinkedList;
import java.io.File;
import java.lang.Exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.serval.tbar.TBarFixer;
import edu.lu.uni.serval.tbar.TBarFixer.Granularity;
import edu.lu.uni.serval.tbar.config.Configuration;
import edu.lu.uni.serval.tbar.utils.FileUtils;
import edu.lu.uni.serval.tbar.DiffMatchPatch;


/**
 * Fix bugs with the known bug positions.
 * 
 * @author kui.liu
 *
 */
public class MainPerfectFL {
	
	private static Logger log = LoggerFactory.getLogger(MainPerfectFL.class);
	private static Granularity granularity = Granularity.File;
	
	public static void main(String[] args) {
//		if (args.length != 4) {
//			System.err.println("Arguments: \n"
//					+ "\t<Bug_Data_Path>: the directory of checking out Defects4J bugs. \n"
//					+ "\t<Bug_ID>: bug id of each Defects4J bug, such as Chart_1. \n"
//					+ "\t<defects4j_Home>: the directory of defects4j git repository.\n"
//					+ "\t<isTestFixPatterns>: true - try all fix patterns for each bug, false - perfect fault localization configuration.");
//			System.exit(0);
//		}
		String bugDataPath = args[0];//"/Users/kui.liu/Public/Defects4J_Data/";//
		String bugId = args[1]; //"Closure_4";// 
		String defects4jHome = args[2];//"/Users/kui.liu/Public/GitRepos/defects4j/";//
		// Configuration.failedTestCasesFilePath = args[3];//"/Users/kui.liu/eclipse-fault-localization/FL-VS-APR/data/FailedTestCases/";//
		// Configuration.knownBugPositions = args[4];
		// boolean isTestFixPatterns = false;//Boolean.valueOf(args[3]);//
		boolean isTestFixPatterns = Boolean.valueOf(args[3]);
		String mode = args[4];
		String granularityStr = "Line";
		System.out.println(bugId);
		// donorIdentify(bugId);

		// TODO:
		// please delete below /* for run normally
		// /*
		if ("line".equalsIgnoreCase(granularityStr) || "l".equalsIgnoreCase(granularityStr)) {
			granularity = Granularity.Line;
			if (isTestFixPatterns) Configuration.outputPath += "FixPatterns/";
			else Configuration.outputPath += "PerfectFL/";
//		} else if ("file".equalsIgnoreCase(granularityStr) || "f".equalsIgnoreCase(granularityStr)) {
//			granularity = Granularity.File;
//			Configuration.outputPath += "File/";
//		} else {
//			System.out.println("Last argument must be l, L, line, or Line, f, F, file, or File.");
//			System.exit(0);
		}
		long time1 = System.currentTimeMillis();
		fixBug(bugDataPath, defects4jHome, bugId, isTestFixPatterns, mode);
		long time2 = System.currentTimeMillis();
		System.out.println("=-=-=-=-=-=-=-=-=-=\njs_time:" + ((time2 - time1)/1000.0));
		// */
	}

	public static void fixBug(String bugDataPath, String defects4jHome, String bugIdStr, boolean isTestFixPatterns, String mode) {
		String[] elements = bugIdStr.split("_");
		String projectName = elements[0];
		int bugId;
		try {
			bugId = Integer.valueOf(elements[1]);
		} catch (NumberFormatException e) {
			System.err.println("Please input correct buggy project ID, such as \"Chart_1\".");
			return;
		}
		
		TBarFixer fixer = new TBarFixer(bugDataPath, projectName, bugId, defects4jHome, mode); 
		fixer.dataType = "TBar";
		fixer.isTestFixPatterns = isTestFixPatterns;
		switch (granularity) {
		case Line:
			fixer.granularity = Granularity.Line;
			break;
//		case File:
//			fixer.granularity = Granularity.File;
//			break;
		default:
			return;
		}
		
		if (Integer.MAX_VALUE == fixer.minErrorTest) {
			System.out.println("Failed to defects4j compile bug " + bugIdStr);
			return;
		}
		fixer.metric = Configuration.faultLocalizationMetric;
		fixer.fixProcess();
		
		int fixedStatus = fixer.fixedStatus;
		switch (fixedStatus) {
		case 0:
			log.info("=======Failed to fix bug " + bugIdStr);
			break;
		case 1:
			log.info("=======Succeeded to fix bug " + bugIdStr);
			break;
		case 2:
			log.info("=======Partial succeeded to fix bug " + bugIdStr);
			break;
		}
	}
	public static void donorIdentify(String bugId) {
		String[] bugInfo = bugId.split("_");
		String basicRoot = "/root/patches/" + bugInfo[0] + "/" + bugInfo[1] + "/";
		
		String beforeCode = FileUtils.getCodeFromFile(new File(basicRoot + "before.txt"));
		String afterCode = FileUtils.getCodeFromFile(new File(basicRoot + "after.txt"));
		LinkedList<DiffMatchPatch.Patch> patches = new DiffMatchPatch().patch_make(beforeCode, afterCode);
		int i = 0;
		for (DiffMatchPatch.Patch tempPatch : patches) {
			System.out.println("Patch" + i);
			// for (DiffMatchPatch.Diff tempDiff : tempPatch.diffs) {
			// 	System.out.println(tempDiff.text);
			// }
			System.out.println(tempPatch.toString());
			i++;
		}
	}	
}
