package edu.lu.uni.serval.tbar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.lang.Math;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.core.util.FileUtil;
import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.AST.ASTGenerator;
import edu.lu.uni.serval.AST.ASTGenerator.TokenType;
import edu.lu.uni.serval.tbar.config.Configuration;
import edu.lu.uni.serval.tbar.context.ContextReader;
import edu.lu.uni.serval.tbar.direction.DonorCodeAnalyze;
import edu.lu.uni.serval.tbar.direction.JsUtils;
import edu.lu.uni.serval.tbar.direction.KeywordSearcher;
import edu.lu.uni.serval.tbar.direction.KeywordTree;
import edu.lu.uni.serval.tbar.direction.SimUtils;
import edu.lu.uni.serval.tbar.direction.TreeUtil;
import edu.lu.uni.serval.tbar.fixpatterns.CNIdiomNoSuperCall;
import edu.lu.uni.serval.tbar.fixpatterns.ClassCastChecker;
import edu.lu.uni.serval.tbar.fixpatterns.ConditionalExpressionMutator;
import edu.lu.uni.serval.tbar.fixpatterns.DataTypeReplacer;
import edu.lu.uni.serval.tbar.fixpatterns.ICASTIdivCastToDouble;
import edu.lu.uni.serval.tbar.fixpatterns.LiteralExpressionMutator;
import edu.lu.uni.serval.tbar.fixpatterns.MethodInvocationMutator;
import edu.lu.uni.serval.tbar.fixpatterns.NPEqualsShouldHandleNullArgument;
import edu.lu.uni.serval.tbar.fixpatterns.NullPointerChecker;
import edu.lu.uni.serval.tbar.fixpatterns.OperatorMutator;
import edu.lu.uni.serval.tbar.fixpatterns.RangeChecker;
import edu.lu.uni.serval.tbar.fixpatterns.ReturnStatementMutator;
import edu.lu.uni.serval.tbar.fixpatterns.StatementInserter;
import edu.lu.uni.serval.tbar.fixpatterns.StatementMover;
import edu.lu.uni.serval.tbar.fixpatterns.StatementRemover;
import edu.lu.uni.serval.tbar.fixpatterns.VariableReplacer;
import edu.lu.uni.serval.tbar.fixtemplate.FixTemplate;
import edu.lu.uni.serval.tbar.info.Patch;
import edu.lu.uni.serval.tbar.utils.Checker;
import edu.lu.uni.serval.tbar.utils.FileHelper;
import edu.lu.uni.serval.tbar.utils.FileUtils;
import edu.lu.uni.serval.tbar.utils.SuspiciousPosition;
import edu.lu.uni.serval.tbar.utils.SuspiciousCodeParser;


/**
 * 
 * @author kui.liu
 *
 */
@SuppressWarnings("unused")
public class TBarFixer extends AbstractFixer {

	public Granularity granularity = Granularity.FL;
	public String mode = "";

	public enum Granularity {
		Line, File, FL
	}

	private static Logger log = LoggerFactory.getLogger(TBarFixer.class);

	public TBarFixer(String path, String projectName, int bugId, String defects4jPath) {
		super(path, projectName, bugId, defects4jPath);
	}

	public TBarFixer(String path, String projectName, int bugId, String defects4jPath,
			String mode) {
		super(path, projectName, bugId, defects4jPath);
		this.mode = mode;
	}

	public TBarFixer(String path, String metric, String projectName, int bugId,
			String defects4jPath) {
		super(path, metric, projectName, bugId, defects4jPath);
	}

	@Override
	public void fixProcess() {
		// Read paths of the buggy project.
		if (!dp.validPaths)
			return;

		// Read suspicious positions.
		List<SuspiciousPosition> suspiciousCodeList = null;
		if (granularity == Granularity.Line) {
			// It assumes that the line-level bug positions are known.
			suspiciousCodeList = readKnownBugPositionsFromFile();
		} else if (granularity == Granularity.File) {
			// It assumes that the file-level bug positions are known.
			List<String> buggyFileList = readKnownFileLevelBugPositions();
			suspiciousCodeList = readSuspiciousCodeFromFile(buggyFileList);
		} else {
			suspiciousCodeList = readSuspiciousCodeFromFile();
		}

		if (suspiciousCodeList == null)
			return;

		List<SuspCodeNode> triedSuspNode = new ArrayList<>();
		System.out.println("number of suspiciousCodeList:" + suspiciousCodeList.size());
		log.info("=======TBar: Start to fix suspicious code======");

		List<SuspCodeNode> totalSuspNode = new ArrayList<>();
		for (SuspiciousPosition suspiciousCode : suspiciousCodeList) {
			List<SuspCodeNode> scns = parseSuspiciousCode(suspiciousCode);
			if (scns == null)
				continue;
			for (SuspCodeNode scn : scns) {
				totalSuspNode.add(scn);
			}
		}
		// JS: ingredient experiment and exit the program
		ingredientSearcher(totalSuspNode);
		System.exit(0);

		for (SuspiciousPosition suspiciousCode : suspiciousCodeList) {
			List<SuspCodeNode> scns = parseSuspiciousCode(suspiciousCode);
			if (scns == null)
				continue;

			for (SuspCodeNode scn : scns) {
				// log.debug(scn.suspCodeStr);
				if (triedSuspNode.contains(scn))
					continue;
				triedSuspNode.add(scn);

				// Parse context information of the suspicious code.
				List<Integer> contextInfoList = readAllNodeTypes(scn.suspCodeAstNode);
				List<Integer> distinctContextInfo = new ArrayList<>();
				for (Integer contInfo : contextInfoList) {
					if (!distinctContextInfo.contains(contInfo) && !Checker.isBlock(contInfo)) {
						distinctContextInfo.add(contInfo);
					}
				}
				// List<Integer> distinctContextInfo =
				// contextInfoList.stream().distinct().collect(Collectors.toList());

				// Match fix templates for this suspicious code with its context information.
				fixWithMatchedFixTemplates(scn, distinctContextInfo);
				// break;
				if (!isTestFixPatterns && minErrorTest == 0)
					break;
				if (this.patchId >= 10000)
					break;
			}
			if (!isTestFixPatterns && minErrorTest == 0)
				break;
			if (this.patchId >= 10000)
				break;
		}
		log.info("=======TBar: Finish off fixing======");

		FileHelper.deleteDirectory(
				Configuration.TEMP_FILES_PATH + this.dataType + "/" + this.buggyProject);
	}

	private List<SuspiciousPosition> readKnownBugPositionsFromFile() {
		List<SuspiciousPosition> suspiciousCodeList = new ArrayList<>();

		String[] posArray = FileHelper.readFile(Configuration.knownBugPositions).split("\n");
		Boolean isBuggyProject = null;
		for (String pos : posArray) {
			if (isBuggyProject == null || isBuggyProject) {
				if (pos.startsWith(this.buggyProject + "@")) {
					isBuggyProject = true;

					String[] elements = pos.split("@");
					String[] lineStrArr = elements[2].split(",");
					String classPath = elements[1];
					String shortSrcPath = dp.srcPath.substring(
							dp.srcPath.indexOf(this.buggyProject) + this.buggyProject.length() + 1);
					classPath = classPath.substring(shortSrcPath.length(), classPath.length() - 5);

					for (String lineStr : lineStrArr) {
						if (lineStr.contains("-")) {
							String[] subPos = lineStr.split("-");
							for (int line = Integer.valueOf(subPos[0]),
									endLine = Integer.valueOf(subPos[1]); line <= endLine; line++) {
								SuspiciousPosition sp = new SuspiciousPosition();
								sp.classPath = classPath;
								sp.lineNumber = line;
								suspiciousCodeList.add(sp);
							}
						} else {
							SuspiciousPosition sp = new SuspiciousPosition();
							sp.classPath = classPath;
							sp.lineNumber = Integer.valueOf(lineStr);
							suspiciousCodeList.add(sp);
						}
					}
				} else if (isBuggyProject != null && isBuggyProject)
					isBuggyProject = false;
			} else if (!isBuggyProject)
				break;
		}
		return suspiciousCodeList;
	}

	private List<String> readKnownFileLevelBugPositions() {
		List<String> buggyFileList = new ArrayList<>();

		String[] posArray = FileHelper.readFile(Configuration.knownBugPositions).split("\n");
		Boolean isBuggyProject = null;
		for (String pos : posArray) {
			if (isBuggyProject == null || isBuggyProject) {
				if (pos.startsWith(this.buggyProject + "@")) {
					isBuggyProject = true;

					String[] elements = pos.split("@");
					String classPath = elements[1];
					String shortSrcPath = dp.srcPath.substring(
							dp.srcPath.indexOf(this.buggyProject) + this.buggyProject.length() + 1);
					classPath = classPath.substring(shortSrcPath.length(), classPath.length() - 5)
							.replace("/", ".");

					if (!buggyFileList.contains(classPath)) {
						buggyFileList.add(classPath);
					}
				} else if (isBuggyProject != null && isBuggyProject)
					isBuggyProject = false;
			} else if (!isBuggyProject)
				break;
		}
		return buggyFileList;
	}

	public List<SuspiciousPosition> readSuspiciousCodeFromFile(List<String> buggyFileList) {
		File suspiciousFile = null;
		String suspiciousFilePath = "";
		if (this.suspCodePosFile == null) {
			suspiciousFilePath = Configuration.suspPositionsFilePath;
		} else {
			suspiciousFilePath = this.suspCodePosFile.getPath();
		}

		suspiciousFile =
				new File(suspiciousFilePath + "/" + this.buggyProject + "/" + this.metric + ".txt");
		if (!suspiciousFile.exists()) {
			System.out.println(
					"Cannot find the suspicious code position file." + suspiciousFile.getPath());
			suspiciousFile = new File(suspiciousFilePath + "/" + this.buggyProject + "/"
					+ this.metric.toLowerCase() + ".txt");
		}
		if (!suspiciousFile.exists()) {
			System.out.println(
					"Cannot find the suspicious code position file." + suspiciousFile.getPath());
			suspiciousFile = new File(suspiciousFilePath + "/" + this.buggyProject + "/All.txt");
		}
		if (!suspiciousFile.exists())
			return null;
		List<SuspiciousPosition> suspiciousCodeList = new ArrayList<>();
		try {
			FileReader fileReader = new FileReader(suspiciousFile);
			BufferedReader reader = new BufferedReader(fileReader);
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] elements = line.split("@");
				if (!buggyFileList.contains(elements[0]))
					continue;
				SuspiciousPosition sp = new SuspiciousPosition();
				sp.classPath = elements[0];
				sp.lineNumber = Integer.valueOf(elements[1]);
				suspiciousCodeList.add(sp);
			}
			reader.close();
			fileReader.close();
		} catch (Exception e) {
			e.printStackTrace();
			log.debug("Reloading Localization Result...");
			return null;
		}
		if (suspiciousCodeList.isEmpty())
			return null;
		return suspiciousCodeList;
	}


	public List<SuspiciousPosition> readSuspiciousCodeFromFile() {
		File suspiciousFile = null;
		String suspiciousFilePath = "";
		if (this.suspCodePosFile == null) {
			suspiciousFilePath = Configuration.suspPositionsFilePath;
		} else {
			suspiciousFilePath = this.suspCodePosFile.getPath();
		}
		suspiciousFile =
				new File(suspiciousFilePath + "/" + this.buggyProject + "/" + this.metric + ".txt");
		if (!suspiciousFile.exists()) {
			System.out.println(
					"Cannot find the suspicious code position file." + suspiciousFile.getPath());
			suspiciousFile = new File(suspiciousFilePath + "/" + this.buggyProject + "/"
					+ this.metric.toLowerCase() + ".txt");
		}
		if (!suspiciousFile.exists()) {
			System.out.println(
					"Cannot find the suspicious code position file." + suspiciousFile.getPath());
			suspiciousFile = new File(suspiciousFilePath + "/" + this.buggyProject + "/All.txt");
		}
		if (!suspiciousFile.exists())
			return null;
		List<SuspiciousPosition> suspiciousCodeList = new ArrayList<>();
		try {
			FileReader fileReader = new FileReader(suspiciousFile);
			BufferedReader reader = new BufferedReader(fileReader);
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] elements = line.split("@");
				SuspiciousPosition sp = new SuspiciousPosition();
				sp.classPath = elements[0];
				sp.lineNumber = Integer.valueOf(elements[1]);
				suspiciousCodeList.add(sp);
			}
			reader.close();
			fileReader.close();
		} catch (Exception e) {
			e.printStackTrace();
			log.debug("Reloading Localization Result...");
			return null;
		}
		if (suspiciousCodeList.isEmpty())
			return null;
		return suspiciousCodeList;
	}


	public void fixWithMatchedFixTemplates(SuspCodeNode scn, List<Integer> distinctContextInfo) {
		// generate patches with fix templates of TBar.
		FixTemplate ft = null;

		if (!Checker.isMethodDeclaration(scn.suspCodeAstNode.getType())) {
			boolean nullChecked = false;
			boolean typeChanged = false;
			boolean methodChanged = false;
			boolean operator = false;
			for (Integer contextInfo : distinctContextInfo) {
				if (Checker.isCastExpression(contextInfo)) {
					ft = new ClassCastChecker();
					if (isTestFixPatterns)
						dataType = readDirectory() + "/ClassCastChecker";

					if (!typeChanged) {
						generateAndValidatePatches(ft, scn);
						if (!isTestFixPatterns && this.minErrorTest == 0)
							return;
						if (this.fixedStatus == 2) {
							fixedStatus = 0;
							return;
						}
						typeChanged = true;
						ft = new DataTypeReplacer();
						if (isTestFixPatterns)
							dataType = readDirectory() + "/DataTypeReplacer";
					}
				} else if (Checker.isClassInstanceCreation(contextInfo)) {
					// ft = new CNIdiomNoSuperCall();
					// if (isTestFixPatterns) dataType = readDirectory() + "/CNIdiomNoSuperCall";
					if (!methodChanged) {
						// generateAndValidatePatches(ft, scn);
						// if (!isTestFixPatterns && this.minErrorTest == 0) return;
						methodChanged = true;
						ft = new MethodInvocationMutator();
						if (isTestFixPatterns)
							dataType = readDirectory() + "/MethodInvocationMutator";
					}
				} else if (Checker.isIfStatement(contextInfo) || Checker.isDoStatement(contextInfo)
						|| Checker.isWhileStatement(contextInfo)) {
					if (Checker.isInfixExpression(scn.suspCodeAstNode.getChild(0).getType())
							&& !operator) {
						operator = true;
						ft = new OperatorMutator(0);
						if (isTestFixPatterns)
							dataType = readDirectory() + "/OperatorMutator";
						generateAndValidatePatches(ft, scn);
						if (!isTestFixPatterns && this.minErrorTest == 0)
							return;
						if (this.fixedStatus == 2) {
							fixedStatus = 0;
							return;
						}
					}
					ft = new ConditionalExpressionMutator(2);
					if (isTestFixPatterns)
						dataType = readDirectory() + "/ConditionalExpressionMutator";
				} else if (Checker.isConditionalExpression(contextInfo)) {
					ft = new ConditionalExpressionMutator(0);
					if (isTestFixPatterns)
						dataType = readDirectory() + "/ConditionalExpressionMutator";
				} else if (Checker.isCatchClause(contextInfo)
						|| Checker.isVariableDeclarationStatement(contextInfo)) {
					if (!typeChanged) {
						ft = new DataTypeReplacer();
						if (isTestFixPatterns)
							dataType = readDirectory() + "/DataTypeReplacer";
						typeChanged = true;
					}
				} else if (Checker.isInfixExpression(contextInfo)) {
					ft = new ICASTIdivCastToDouble();
					if (isTestFixPatterns)
						dataType = readDirectory() + "/ICASTIdivCastToDouble";
					generateAndValidatePatches(ft, scn);
					if (!isTestFixPatterns && this.minErrorTest == 0)
						return;
					if (this.fixedStatus == 2) {
						fixedStatus = 0;
						return;
					}

					if (!operator) {
						operator = true;
						ft = new OperatorMutator(0);
						if (isTestFixPatterns)
							dataType = readDirectory() + "/OperatorMutator";
						generateAndValidatePatches(ft, scn);
						if (!isTestFixPatterns && this.minErrorTest == 0)
							return;
						if (this.fixedStatus == 2) {
							fixedStatus = 0;
							return;
						}
					}

					ft = new ConditionalExpressionMutator(1);
					if (isTestFixPatterns)
						dataType = readDirectory() + "/ConditionalExpressionMutator";
					generateAndValidatePatches(ft, scn);
					if (!isTestFixPatterns && this.minErrorTest == 0)
						return;
					if (this.fixedStatus == 2) {
						fixedStatus = 0;
						return;
					}

					ft = new OperatorMutator(4);
					if (isTestFixPatterns)
						dataType = readDirectory() + "/OperatorMutator";
				} else if (Checker.isBooleanLiteral(contextInfo)
						|| Checker.isNumberLiteral(contextInfo)
						|| Checker.isCharacterLiteral(contextInfo)
						|| Checker.isStringLiteral(contextInfo)) {
					ft = new LiteralExpressionMutator();
					if (isTestFixPatterns)
						dataType = readDirectory() + "/LiteralExpressionMutator";
				} else if (Checker.isMethodInvocation(contextInfo)
						|| Checker.isConstructorInvocation(contextInfo)
						|| Checker.isSuperConstructorInvocation(contextInfo)) {
					if (!methodChanged) {
						ft = new MethodInvocationMutator();
						if (isTestFixPatterns)
							dataType = readDirectory() + "/MethodInvocationMutator";
						methodChanged = true;
					}

					if (Checker.isMethodInvocation(contextInfo)) {
						if (ft != null) {
							generateAndValidatePatches(ft, scn);
							if (!isTestFixPatterns && this.minErrorTest == 0)
								return;
							if (this.fixedStatus == 2) {
								fixedStatus = 0;
								return;
							}
						}
						ft = new NPEqualsShouldHandleNullArgument();
						if (isTestFixPatterns)
							dataType = readDirectory() + "/NPEqualsShouldHandleNullArgument";
						generateAndValidatePatches(ft, scn);
						if (!isTestFixPatterns && this.minErrorTest == 0)
							return;
						if (this.fixedStatus == 2) {
							fixedStatus = 0;
							return;
						}

						ft = new RangeChecker(false);
						if (isTestFixPatterns)
							dataType = readDirectory() + "/RangeChecker";
					}
				} else if (Checker.isAssignment(contextInfo)) {
					ft = new OperatorMutator(2);
					if (isTestFixPatterns)
						dataType = readDirectory() + "/OperatorMutator";
				} else if (Checker.isInstanceofExpression(contextInfo)) {
					ft = new OperatorMutator(5);
					if (isTestFixPatterns)
						dataType = readDirectory() + "/OperatorMutator";
				} else if (Checker.isArrayAccess(contextInfo)) {
					ft = new RangeChecker(true);
					if (isTestFixPatterns)
						dataType = readDirectory() + "/RangeChecker";
				} else if (Checker.isReturnStatement(contextInfo)) {
					String returnType = ContextReader.readMethodReturnType(scn.suspCodeAstNode);
					if ("boolean".equalsIgnoreCase(returnType)) {
						ft = new ConditionalExpressionMutator(2);
						if (isTestFixPatterns)
							dataType = readDirectory() + "/ConditionalExpressionMutator";
					} else {
						ft = new ReturnStatementMutator(returnType);
						if (isTestFixPatterns)
							dataType = readDirectory() + "/ReturnStatementMutator";
					}
				} else if (Checker.isSimpleName(contextInfo)
						|| Checker.isQualifiedName(contextInfo)) {
					ft = new VariableReplacer();
					if (isTestFixPatterns)
						dataType = readDirectory() + "/VariableReplacer";

					if (!nullChecked) {
						generateAndValidatePatches(ft, scn);
						if (!isTestFixPatterns && this.minErrorTest == 0)
							return;
						if (this.fixedStatus == 2) {
							fixedStatus = 0;
							return;
						}
						nullChecked = true;
						ft = new NullPointerChecker();
						if (isTestFixPatterns)
							dataType = readDirectory() + "/NullPointerChecker";
					}
				}
				if (ft != null) {
					generateAndValidatePatches(ft, scn);
					if (!isTestFixPatterns && this.minErrorTest == 0)
						return;
					if (this.fixedStatus == 2) {
						fixedStatus = 0;
						return;
					}
				}
				ft = null;
				if (this.patchId >= 10000)
					break;
			}

			if (!nullChecked) {
				nullChecked = true;
				ft = new NullPointerChecker();
				if (isTestFixPatterns)
					dataType = readDirectory() + "/NullPointerChecker";
				generateAndValidatePatches(ft, scn);
				if (!isTestFixPatterns && this.minErrorTest == 0)
					return;
				if (this.fixedStatus == 2) {
					fixedStatus = 0;
					return;
				}
			}

			ft = new StatementMover();
			if (isTestFixPatterns)
				dataType = readDirectory() + "/StatementMover";
			generateAndValidatePatches(ft, scn);
			if (!isTestFixPatterns && this.minErrorTest == 0)
				return;
			if (this.fixedStatus == 2) {
				fixedStatus = 0;
				return;
			}

			ft = new StatementRemover();
			if (isTestFixPatterns)
				dataType = readDirectory() + "/StatementRemover";
			generateAndValidatePatches(ft, scn);
			if (!isTestFixPatterns && this.minErrorTest == 0)
				return;
			if (this.fixedStatus == 2) {
				fixedStatus = 0;
				return;
			}

			ft = new StatementInserter();
			if (isTestFixPatterns)
				dataType = readDirectory() + "/StatementInserter";
			generateAndValidatePatches(ft, scn);
			if (!isTestFixPatterns && this.minErrorTest == 0)
				return;
			if (this.fixedStatus == 2) {
				fixedStatus = 0;
				return;
			}
		} else {
			ft = new StatementRemover();
			if (isTestFixPatterns)
				dataType = readDirectory() + "/StatementRemover";
			generateAndValidatePatches(ft, scn);
			if (!isTestFixPatterns && this.minErrorTest == 0)
				return;
			if (this.fixedStatus == 2) {
				fixedStatus = 0;
				return;
			}
		}
	}

	private String readDirectory() {
		int index = dataType.indexOf("/");
		if (index > -1)
			dataType = dataType.substring(0, index);
		return dataType;
	}

	protected void generateAndValidatePatches(FixTemplate ft, SuspCodeNode scn) {
		ft.setSuspiciousCodeStr(scn.suspCodeStr);
		ft.setSuspiciousCodeTree(scn.suspCodeAstNode);
		if (scn.javaBackup == null)
			ft.setSourceCodePath(dp.srcPath);
		else
			ft.setSourceCodePath(dp.srcPath, scn.javaBackup);
		ft.setDictionary(dic);

		System.exit(0);

		// ft.generatePatches(patchIngredients);
		// below code is for original tbar system
		// ft.generatePatches();
		// List<Patch> patchCandidates = ft.getPatches();
		// if (patchCandidates.isEmpty()) return;
		// System.out.println("number of patch:" + patchCandidates.size());
		// For normal running please un-comment below code
		// testGeneratedPatches(patchCandidates, scn);
	}

	public void ingredientSearcher(List<SuspCodeNode> totalSuspNode) {
		// Initializing
		String projectPath = dp.srcPath;
		String keywordRun = "yes";
		String lcsRun = "no";
		String tfidfRun = "no";
		String globalSearchRun = "no";
		String donorCodeAnalyzeRun = "no";
		HashMap<ITree, Double> noContextLcsScores = new HashMap<>();
		HashMap<ITree, Double> contextLcsScores = new HashMap<>();
		HashMap<ITree, Double> noContextTfIdfScores = new HashMap<>();
		HashMap<ITree, Double> contextTfIdfScores = new HashMap<>();
		HashMap<ITree, Double> scoredStatements = new HashMap<ITree, Double>();
		HashSet<String> lcsNoContextIngredients = new HashSet<String>();
		HashSet<String> lcsContextIngredients = new HashSet<String>();
		HashSet<String> keywordIngredients = new HashSet<String>();

		ArrayList<String> donorCodes = JsUtils.getDonorCodes(this.buggyProject);
		ArrayList<String> donorCodes2 = JsUtils.getDonorCodes(this.buggyProject);
		HashMap<String, ArrayList<ITree>> donorCodeStmt = new HashMap<>();

		// Write the result of no donorcode case.
		if (donorCodes.size() == 0) {
			System.out.println("%%%%%%%%% There is no donorcode! %%%%%%%%");
			String hitRatioResultFolderDir = "/root/DIRECTION/Data/HitRatio/";
			// write the none result in result txt file
			if (keywordRun.equals("yes")) {
				String hitRatioResultDir = hitRatioResultFolderDir + "keyword.csv";
				try {
					BufferedWriter hitRatioWriter =
							new BufferedWriter(new FileWriter(new File(hitRatioResultDir), true));
					hitRatioWriter.write(this.buggyProject + ",none,0,0\n");
					hitRatioWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (lcsRun.equals("yes")) {
				String hitRatioResultDirNoc = hitRatioResultFolderDir + "lcs_noc.csv";
				String hitRatioResultDirC = hitRatioResultFolderDir + "lcs_c.csv";
				try {
					BufferedWriter hitRatioWriterNoc = new BufferedWriter(
							new FileWriter(new File(hitRatioResultDirNoc), true));
					BufferedWriter hitRatioWriterC =
							new BufferedWriter(new FileWriter(new File(hitRatioResultDirC), true));
					hitRatioWriterNoc.write(this.buggyProject + ",none,0,0\n");
					hitRatioWriterC.write(this.buggyProject + ",none,0,0\n");
					hitRatioWriterNoc.close();
					hitRatioWriterC.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.exit(0);
		}

		System.out.println("loop|" + totalSuspNode.size() + "|");

		String targetSearchSpace = "File"; // Project, Package, File, Method
		KeywordSearcher keywordSearcher = new KeywordSearcher(totalSuspNode, targetSearchSpace, projectPath);
		keywordSearcher.extractKeywords();
		keywordSearcher.collectSearchSpace();
		System.out.println("collect search space done");
		keywordSearcher.makeTree();
		System.out.println("make tree done");

		KeywordTree rootNode = keywordSearcher.getRootNode();
		KeywordTree targetNode = rootNode;

		System.out.println("tree size:" + keywordSearcher.getTreeContents().size());
		

		System.exit(0);


		int i = 0;
		for (SuspCodeNode scn : totalSuspNode) {
			ArrayList<String> projectFileList = new ArrayList<>();
			ArrayList<ITree> keywordStatementList = new ArrayList<>();

			ITree suspStatementTree = scn.suspCodeAstNode;
			ITree suspMethodNode = TreeUtil.findMethodNode(suspStatementTree);
			String suspFileCode = FileUtils.getCodeFromFile(scn.targetJavaFile);

			// String suspMethodCode = JsUtils.getMethodString(suspFileCode, suspMethodNode);
			String suspMethodCode = "";

			if (tfidfRun.equals("yes")) {
				try {
					BufferedWriter nocontextBufWriter = new BufferedWriter(new FileWriter(
							new File("/root/DIRECTION/tfidf/dataset_nocontext/stmt0.txt")));
					BufferedWriter contextBufWriter = new BufferedWriter(new FileWriter(
							new File("/root/DIRECTION/tfidf/dataset_context/stmt0.txt")));
					nocontextBufWriter.write(suspStatementTree.getLabel());
					contextBufWriter.write(suspMethodCode);
					contextBufWriter.close();
					nocontextBufWriter.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			int parentSize = suspStatementTree.getParents().size();
			ITree suspFileTree = suspStatementTree.getParents().get(parentSize - 1);
			ArrayList<ITree> contextElementNodes = new ArrayList<>();
			TreeUtil.extractContextNode(suspStatementTree, contextElementNodes);
			ArrayList<String> contextElementList =
					JsUtils.extractContextElement(contextElementNodes);
			JsUtils.findSubFileInPath(new File(projectPath), projectFileList);
			ArrayList<ITree> candStmts = new ArrayList<>();

			for (String filePath : projectFileList) {
				File tempFile = new File(filePath);
				ITree fileRootNode =
						new ASTGenerator().generateTreeForJavaFile(tempFile, TokenType.EXP_JDT);
				// JsUtils.keywordBasedSearch(slicedStatementList, contextElementList,
				// fileRootNode);

				if (keywordRun.equals("yes")) {
					TreeUtil.collectStatement(candStmts, fileRootNode);
				}

				if (lcsRun.equals("yes")) {
					SimUtils.lcsSimNoContext(suspStatementTree, fileRootNode, noContextLcsScores);
					SimUtils.lcsSimContext(suspMethodCode, fileRootNode, filePath,
							contextLcsScores);
				}

				if (tfidfRun.equals("yes")) {
					String noContextDir = "/root/DIRECTION/tfidf/dataset_nocontext/";
					String contextDir = "/root/DIRECTION/tfidf/dataset_context/";
					File noContextFolder = new File(noContextDir);
					File contextFolder = new File(contextDir);
					int noContextDirFileNum = noContextFolder.listFiles().length;
					int contextDirFileNum = contextFolder.listFiles().length;

				}

				if (donorCodeAnalyzeRun.equals("yes")) {
					DonorCodeAnalyze.findDonorCodes(fileRootNode, donorCodes, donorCodeStmt);
				}
			}
			System.out.println("stmt size:" + candStmts.size());

			System.out.print(++i + ",");
		}

		if (keywordRun.equals("yes")) {
			JsUtils.getPatchIngredient(scoredStatements, keywordIngredients);
			JsUtils.hitRatio(this.buggyProject, donorCodes, keywordIngredients, "keyword");
		}
		if (lcsRun.equals("yes")) {
			JsUtils.getPatchIngredient(noContextLcsScores, lcsNoContextIngredients);
			JsUtils.getPatchIngredient(contextLcsScores, lcsContextIngredients);
			JsUtils.hitRatio(this.buggyProject, donorCodes, lcsNoContextIngredients, "lcs_noc");
			JsUtils.hitRatio(this.buggyProject, donorCodes, lcsContextIngredients, "lcs_c");
		}

		if (donorCodeAnalyzeRun.equals("yes")) {
			DonorCodeAnalyze.printDonorCodes(this.buggyProject, donorCodes, donorCodeStmt);
		}
	}

	public List<Integer> readAllNodeTypes(ITree suspCodeAstNode) {
		List<Integer> nodeTypes = new ArrayList<>();
		nodeTypes.add(suspCodeAstNode.getType());
		List<ITree> children = suspCodeAstNode.getChildren();
		for (ITree child : children) {
			int childType = child.getType();
			if (Checker.isFieldDeclaration(childType) || Checker.isMethodDeclaration(childType)
					|| Checker.isTypeDeclaration(childType) || Checker.isStatement(childType))
				break;
			nodeTypes.addAll(readAllNodeTypes(child));
		}
		return nodeTypes;
	}
}
