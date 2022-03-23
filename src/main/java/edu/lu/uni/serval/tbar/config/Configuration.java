package edu.lu.uni.serval.tbar.config;

public class Configuration {

	public static String knownBugPositions = "Data/BugPositions.txt";
	public static String suspPositionsFilePath = "Data/SuspiciousCodePositions/";
	public static String failedTestCasesFilePath = "Data/FailedTestCases/";
	public static String faultLocalizationMetric = "Ochiai";
	public static String outputPath = "Data/OUTPUT/";

	public static final String TEMP_FILES_PATH = ".temp/";
	public static final long SHELL_RUN_TIMEOUT = 300L;
	public static final long TEST_SHELL_RUN_TIMEOUT = 600L;

}
