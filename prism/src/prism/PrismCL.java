//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Dave Parker <david.parker@comlab.ox.ac.uk> (University of Oxford, formerly University of Birmingham)
//	
//------------------------------------------------------------------------------
//	
//	This file is part of PRISM.
//	
//	PRISM is free software; you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation; either version 2 of the License, or
//	(at your option) any later version.
//	
//	PRISM is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PRISM; if not, write to the Free Software Foundation,
//	Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//	
//==============================================================================

package prism;

import java.io.*;
import java.util.*;

import parser.*;
import parser.ast.*;
import pta.*;
import simulator.method.*;
import explicit.PrismExplicit;

// prism - command line version

public class PrismCL
{
	// flags
	private boolean verbose;
	private boolean fixdl = false;
	private boolean importpepa = false;
	private boolean importtrans = false;
	private boolean importstates = false;
	private boolean importlabels = false;
	private boolean importinitdist = false;
	private boolean steadystate = false;
	private boolean dotransient = false;
	private boolean exporttrans = false;
	private boolean exportstaterewards = false;
	private boolean exporttransrewards = false;
	private boolean exportstates = false;
	private boolean exportlabels = false;
	private boolean exportspy = false;
	private boolean exportdot = false;
	private boolean exporttransdot = false;
	private boolean exporttransdotstates = false;
	private boolean exportbsccs = false;
	private boolean exportresults = false;
	private boolean exportprism = false;
	private boolean exportprismconst = false;
	private boolean exportPlainDeprecated = false;
	private int exportType = Prism.EXPORT_PLAIN;
	private boolean exportordered = true;
	private boolean simulate = false;
	private boolean simpath = false;
	private ModelType typeOverride = null;
	private boolean explicit = false;
	private boolean explicitbuild = false;
	private boolean explicitbuildtest = false;
	private boolean nobuild = false;

	// property info
	private int propertyToCheck = -1;
	private String propertyString = "";

	// argument to -const switch
	private String constSwitch = null;

	// argument to -simpath switch
	private String simpathDetails = null;

	// files/filenames
	private String mainLogFilename = "stdout";
	private String techLogFilename = "stdout";
	private String modelFilename = null;
	private String importStatesFilename = null;
	private String importLabelsFilename = null;
	private String importInitDistFilename = null;
	private String propertiesFilename = null;
	private String exportTransFilename = null;
	private String exportStateRewardsFilename = null;
	private String exportTransRewardsFilename = null;
	private String exportStatesFilename = null;
	private String exportLabelsFilename = null;
	private String exportSpyFilename = null;
	private String exportDotFilename = null;
	private String exportTransDotFilename = null;
	private String exportTransDotStatesFilename = null;
	private String exportBSCCsFilename = null;
	private String exportResultsFilename = null;
	private String exportSteadyStateFilename = null;
	private String exportTransientFilename = null;
	private String exportPrismFilename = null;
	private String exportPrismConstFilename = null;
	private String simpathFilename = null;

	// logs
	private PrismLog mainLog = null;
	private PrismLog techLog = null;

	// prism object
	private Prism prism = null;

	// storage for parsed model/properties files
	private ModulesFile modulesFile = null;
	private PropertiesFile propertiesFile = null;

	// info about which properties to model check
	private int numPropertiesToCheck = 0;
	private Expression propertiesToCheck[] = null;

	// info about undefined constants
	private UndefinedConstants undefinedConstants;
	private Values definedMFConstants;
	private Values definedPFConstants;

	// built (symbolic) model storage
	private Model model = null;

	// built (explicit) model storage
	private PrismExplicit prismExpl = null;
	private explicit.Model modelExpl; 
	
	// results
	private ResultsCollection results[] = null;

	// time for transient computation
	private String transientTime;

	// simulation info
	private String simMethodName = null;
	private double simApprox;
	private double simConfidence;
	private int simNumSamples;
	private double simWidth;
	private int reqIterToConclude;
	private double simMaxReward;
	private int simMaxPath;
	private boolean simApproxGiven = false;
	private boolean simConfidenceGiven = false;
	private boolean simNumSamplesGiven = false;
	private boolean simWidthGiven = false;
	private boolean reqIterToConcludeGiven = false;
	private boolean simMaxRewardGiven = false;
	private boolean simMaxPathGiven = false;
	private boolean simManual = false;
	private SimulationMethod simMethod = null;

	// entry point - run method

	public void run(String[] args)
	{
		int i, j, k;
		ModulesFile modulesFileToCheck;
		Result res;

		// initialise
		try {
			initialise(args);
		} catch (PrismException e) {
			errorAndExit(e.getMessage());
		}

		// parse model/properties
		try {
			doParsing();
		} catch (PrismException e) {
			errorAndExit(e.getMessage());
		}

		// sort out properties to check
		sortProperties();

		// sort out undefined constants
		try {
			undefinedConstants = new UndefinedConstants(modulesFile, propertiesFile);
			undefinedConstants.defineUsingConstSwitch(constSwitch);
		} catch (PrismException e) {
			errorAndExit(e.getMessage());
		}

		// initialise storage for results
		results = new ResultsCollection[numPropertiesToCheck];
		for (i = 0; i < numPropertiesToCheck; i++) {
			results[i] = new ResultsCollection(undefinedConstants, propertiesToCheck[i].getResultName());
		}

		// iterate through as many models as necessary
		for (i = 0; i < undefinedConstants.getNumModelIterations(); i++) {

			definedMFConstants = undefinedConstants.getMFConstantValues();
			if (definedMFConstants != null)
				if (definedMFConstants.getNumValues() > 0)
					mainLog.println("\nModel constants: " + definedMFConstants);

			// set values for ModulesFile constants
			try {
				modulesFile.setUndefinedConstants(definedMFConstants);
			} catch (PrismException e) {
				// in case of error, report it, store as result for any properties, and go on to the next model
				// (might happen for example if overflow or another numerical problem is detected at this stage)
				error(e.getMessage());
				try {
					for (j = 0; j < numPropertiesToCheck; j++) {
						results[j].setMultipleErrors(definedMFConstants, null, e);
					}
				} catch (PrismException e2) {
					error("Problem storing results");
				}
				undefinedConstants.iterateModel();
				continue;
			}

			// if requested, generate a random path with simulator (and then skip anything else)
			if (simpath) {
				try {
					if (!simMaxPathGiven)
						simMaxPath = prism.getSettings().getInteger(PrismSettings.SIMULATOR_DEFAULT_MAX_PATH);
					File f = (simpathFilename.equals("stdout")) ? null : new File(simpathFilename);
					prism.generateSimulationPath(modulesFile, simpathDetails, simMaxPath, f);
				} catch (PrismException e) {
					error(e.getMessage());
				}
				undefinedConstants.iterateModel();
				continue;
			}

			// Do any requested exports of PRISM code
			// (except for PTA digital clocks case - postpone this)
			if (!(modulesFile.getModelType() == ModelType.PTA && prism.getSettings().getString(PrismSettings.PRISM_PTA_METHOD).equals("Digital clocks"))) {
				doPrismLangExports(modulesFile);
			}

			// Decide if model construction is necessary
			boolean doBuild = true;
			// If explicitly disabled...
			if (nobuild)
				doBuild = false;
			// No need if using approximate (simulation-based) model checking...
			if (simulate)
				doBuild = false;
			// No need if doing PTA model checking...
			// (even if needed for digital clocks, will be done later)
			if (modulesFile.getModelType() == ModelType.PTA)
				doBuild = false;

			// do model construction (if necessary)
			if (doBuild) {
				// build model
				try {
					buildModel(modulesFile, false);
				} catch (PrismException e) {
					// in case of error, report it, store as result for any properties, and go on to the next model
					error(e.getMessage());
					try {
						for (j = 0; j < numPropertiesToCheck; j++) {
							results[j].setMultipleErrors(definedMFConstants, null, e);
						}
					} catch (PrismException e2) {
						error("Problem storing results");
					}
					undefinedConstants.iterateModel();
					continue;
				}

				// do any exports
				doExports();

				// do steady state comp if required
				if (steadystate) {
					try {
						doSteadyState();
					} catch (PrismException e) {
						// in case of error, report it and proceed
						error(e.getMessage());
					}
				}

				// do transient comp if required
				if (dotransient) {
					try {
						doTransient();
					}
					// in case of error, report it and proceed
					catch (PrismException e) {
						error(e.getMessage());
					}
				}

				// export labels/states
				if (exportlabels) {
					try {
						if (propertiesFile != null) {
							definedPFConstants = undefinedConstants.getPFConstantValues();
							propertiesFile.setUndefinedConstants(definedPFConstants);
						}
						File f = (exportLabelsFilename.equals("stdout")) ? null : new File(exportLabelsFilename);
						prism.exportLabelsToFile(model, modulesFile, propertiesFile, exportType, f);
					}
					// in case of error, report it and proceed
					catch (FileNotFoundException e) {
						mainLog.println("Couldn't open file \"" + exportLabelsFilename + "\" for output");
					} catch (PrismException e) {
						mainLog.println("\nError: " + e.getMessage() + ".");
					}
				}
			}

			// work through list of properties to be checked
			for (j = 0; j < numPropertiesToCheck; j++) {

				// for simulation we can do multiple values of property constants simultaneously
				if (simulate && undefinedConstants.getNumPropertyIterations() > 1) {
					try {
						mainLog.println("\n-------------------------------------------");
						mainLog.println("\nSimulating: " + propertiesToCheck[j]);
						if (definedMFConstants != null)
							if (definedMFConstants.getNumValues() > 0)
								mainLog.println("Model constants: " + definedMFConstants);
						mainLog.println("Property constants: " + undefinedConstants.getPFDefinedConstantsString());
						simMethod = processSimulationOptions(propertiesToCheck[j]);
						prism.modelCheckSimulatorExperiment(modulesFile, propertiesFile, undefinedConstants, results[j], propertiesToCheck[j], null,
								simMaxPath, simMethod);
					} catch (PrismException e) {
						// in case of (overall) error, report it, store as result for property, and proceed
						error(e.getMessage());
						try {
							results[j].setMultipleErrors(definedMFConstants, null, e);
						} catch (PrismException e2) {
							error("Problem storing results");
						}
						undefinedConstants.iterateModel();
						continue;
					} catch (InterruptedException e) {
						// ignore - won't get interrupted
					}
				}
				// otherwise, treat each case individually
				else {
					for (k = 0; k < undefinedConstants.getNumPropertyIterations(); k++) {

						try {
							// set values for PropertiesFile constants
							if (propertiesFile != null) {
								definedPFConstants = undefinedConstants.getPFConstantValues();
								propertiesFile.setUndefinedConstants(definedPFConstants);
							}

							// log output
							mainLog.println("\n-------------------------------------------");
							mainLog.println("\n" + (simulate ? "Simulating" : "Model checking") + ": " + propertiesToCheck[j]);
							if (definedMFConstants != null)
								if (definedMFConstants.getNumValues() > 0)
									mainLog.println("Model constants: " + definedMFConstants);
							if (definedPFConstants != null)
								if (definedPFConstants.getNumValues() > 0)
									mainLog.println("Property constants: " + definedPFConstants);

							// for PTAs via digital clocks, do model translation and build
							if (modulesFile.getModelType() == ModelType.PTA
									&& prism.getSettings().getString(PrismSettings.PRISM_PTA_METHOD).equals("Digital clocks")) {
								DigitalClocks dc = new DigitalClocks(prism);
								dc.translate(modulesFile, propertiesFile, propertiesToCheck[j]);
								modulesFileToCheck = dc.getNewModulesFile();
								modulesFileToCheck.setUndefinedConstants(modulesFile.getConstantValues());
								doPrismLangExports(modulesFileToCheck);
								buildModel(modulesFileToCheck, true);
							} else {
								modulesFileToCheck = modulesFile;
							}

							// exact (non-approximate) model checking
							if (!simulate) {
								// PTA model checking
								if (modulesFileToCheck.getModelType() == ModelType.PTA) {
									res = prism.modelCheckPTA(modulesFileToCheck, propertiesFile, propertiesToCheck[j]);
								}
								// Non-PTA model checking
								else {
									if (!explicit) {
										res = prism.modelCheck(model, propertiesFile, propertiesToCheck[j]);
									} else {
										res = prismExpl.modelCheck(modelExpl, "REMOVEME", propertiesFile, propertiesToCheck[j]);
									}
								}
							}
							// approximate (simulation-based) model checking
							else {
								simMethod = processSimulationOptions(propertiesToCheck[j]);
								res = prism.modelCheckSimulator(modulesFileToCheck, propertiesFile, propertiesToCheck[j], null, simMaxPath, simMethod);
								simMethod.reset();
							}
						} catch (PrismException e) {
							// in case of error, report it, store exception as the result and proceed
							error(e.getMessage());
							res = new Result(e);
						}

						// store result of model checking
						try {
							results[j].setResult(definedMFConstants, definedPFConstants, res.getResult());
							if (res.getCounterexample() != null) {
								mainLog.println("\nCounterexample/witness:");
								mainLog.println(res.getCounterexample());
							}
						} catch (PrismException e) {
							error("Problem storing results");
						}

						// iterate to next property
						undefinedConstants.iterateProperty();
					}
				}
			}

			// clear model
			if (model != null) {
				model.clear();
			}

			// iterate to next model
			undefinedConstants.iterateModel();
		}

		// export results (if required)
		if (exportresults) {

			mainLog.print("\nExporting results ");
			if (!exportResultsFilename.equals("stdout"))
				mainLog.println("to file \"" + exportResultsFilename + "\"...");
			else
				mainLog.println("below:");
			PrismFileLog tmpLog = new PrismFileLog(exportResultsFilename);
			if (!tmpLog.ready()) {
				errorAndExit("Couldn't open file \"" + exportResultsFilename + "\" for output");
			}
			for (i = 0; i < numPropertiesToCheck; i++) {
				if (i > 0)
					tmpLog.println();
				tmpLog.print(propertiesToCheck[i] + ":\n" + results[i].toString(false, " ", " "));
			}
			tmpLog.close();
		}

		// close down
		closeDown();
	}

	// initialise

	private void initialise(String[] args) throws PrismException
	{
		// default to logs going to stdout
		// this means all errors etc. can be safely sent to the log
		// even if a new log is created shortly
		mainLog = new PrismFileLog("stdout");
		techLog = new PrismFileLog("stdout");

		// create prism object
		prism = new Prism(mainLog, techLog);
		prismExpl = new PrismExplicit(mainLog, prism.getSettings());

		// get prism defaults
		verbose = prism.getVerbose();

		// parse command line arguments
		parseArguments(args);

		// initialise
		prism.initialise();

		// print command line for reference
		printArguments(args);

		// do some processing of the options
		processOptions();
	}

	// parse model and properties

	private void doParsing() throws PrismException
	{
		int i;
		File sf = null, lf = null;

		// parse model

		try {
			if (importpepa) {
				mainLog.print("\nImporting PEPA file \"" + modelFilename + "\"...\n");
				modulesFile = prism.importPepaFile(new File(modelFilename));
			} else if (importtrans) {
				mainLog.print("\nImporting model (");
				mainLog.print(typeOverride == null ? "MDP" : typeOverride);
				mainLog.print(") from \"" + modelFilename + "\"");
				if (importstates) {
					mainLog.print(", \"" + importStatesFilename + "\"");
					sf = new File(importStatesFilename);
				}
				if (importlabels) {
					mainLog.print(", \"" + importLabelsFilename + "\"");
					lf = new File(importLabelsFilename);
				}
				mainLog.println("...");
				modulesFile = prism.parseModelFromExplicitFiles(sf, new File(modelFilename), lf, typeOverride);
			} else {
				mainLog.print("\nParsing model file \"" + modelFilename + "\"...\n");
				modulesFile = prism.parseModelFile(new File(modelFilename), typeOverride);
			}
		} catch (FileNotFoundException e) {
			errorAndExit("File \"" + modelFilename + "\" not found");
		} catch (PrismException e) {
			errorAndExit(e.getMessage());
		}

		// parse properties

		try {
			// if properties file specified...
			if (propertiesFilename != null) {
				mainLog.print("\nParsing properties file \"" + propertiesFilename + "\"...\n");
				propertiesFile = prism.parsePropertiesFile(modulesFile, new File(propertiesFilename));
			}
			// if properties were given on command line...
			else if (!propertyString.equals("")) {
				propertiesFile = prism.parsePropertiesString(modulesFile, propertyString);
			} else {
				propertiesFile = null;
			}
		} catch (FileNotFoundException e) {
			errorAndExit("File \"" + propertiesFilename + "\" not found");
		} catch (PrismException e) {
			errorAndExit(e.getMessage());
		}

		// print out properties (if any)

		if (propertiesFile == null)
			return;
		mainLog.print("\n" + propertiesFile.getNumProperties());
		mainLog.print(" propert" + ((propertiesFile.getNumProperties() == 1) ? "y" : "ies") + ":\n");
		for (i = 0; i < propertiesFile.getNumProperties(); i++) {
			mainLog.println("(" + (i + 1) + ") " + propertiesFile.getProperty(i));
		}
	}

	// sort out which properties need checking

	private void sortProperties()
	{
		int i;

		// no properties to check
		if (propertiesFile == null) {
			numPropertiesToCheck = 0;
			propertiesToCheck = null;
		}
		// unless specified, verify all properties
		else if (propertyToCheck == -1) {
			numPropertiesToCheck = propertiesFile.getNumProperties();
			propertiesToCheck = new Expression[numPropertiesToCheck];
			for (i = 0; i < numPropertiesToCheck; i++) {
				propertiesToCheck[i] = propertiesFile.getProperty(i);
			}
		}
		// otherwise just verify the relevant property
		else {
			if (propertyToCheck > 0 && propertyToCheck <= propertiesFile.getNumProperties()) {
				numPropertiesToCheck = 1;
				propertiesToCheck = new Expression[1];
				propertiesToCheck[0] = propertiesFile.getProperty(propertyToCheck - 1);
			} else {
				errorAndExit("There is not a property " + propertyToCheck + " to verify");
			}
		}
	}

	/**
	 * Do any exports of PRISM code that have been requested.
	 */
	private void doPrismLangExports(ModulesFile modulesFileToExport)
	{
		// output final prism model here if required
		if (exportprism) {
			mainLog.print("\nExporting parsed PRISM file ");
			if (!exportPrismFilename.equals("stdout"))
				mainLog.println("to file \"" + exportPrismFilename + "\"...");
			else
				mainLog.println("below:");
			PrismFileLog tmpLog = new PrismFileLog(exportPrismFilename);
			if (!tmpLog.ready()) {
				errorAndExit("Couldn't open file \"" + exportPrismFilename + "\" for output");
			}
			tmpLog.print(modulesFileToExport.toString());
		}
		if (exportprismconst) {
			mainLog.print("\nExporting parsed PRISM file (with constant expansion) ");
			if (!exportPrismConstFilename.equals("stdout"))
				mainLog.println("to file \"" + exportPrismConstFilename + "\"...");
			else
				mainLog.println("below:");
			PrismFileLog tmpLog = new PrismFileLog(exportPrismConstFilename);
			if (!tmpLog.ready()) {
				errorAndExit("Couldn't open file \"" + exportPrismConstFilename + "\" for output");
			}
			ModulesFile mfTmp = (ModulesFile) modulesFileToExport.deepCopy();
			try {
				mfTmp = (ModulesFile) mfTmp.replaceConstants(modulesFileToExport.getConstantValues());
				mfTmp = (ModulesFile) mfTmp.replaceConstants(definedMFConstants);
				// NB: Don't use simplify() here because doesn't work for the purposes of printing out
				// (e.g. loss of parethenses causes precedence problems)
			} catch (PrismLangException e) {
				error(e.getMessage());
			}
			tmpLog.print(mfTmp.toString());
		}
	}

	/**
	 * Build a model, usually from the passed in modulesFileToBuild. However, if importtrans=true,
	 * then explicit model import is done and modulesFileToBuild can be null.
	 * This method also displays model info and checks/fixes deadlocks.
	 * If flag 'digital' is true, then this (MDP) model was constructed
	 * from digital clocks semantics of a PTA - might need for error reporting.
	 */
	private void buildModel(ModulesFile modulesFileToBuild, boolean digital) throws PrismException
	{
		StateList states;
		int i;

		mainLog.println("\n-------------------------------------------");

		// build model
		if (!explicit) {
			if (importtrans) {
				model = prism.buildModelFromExplicitFiles();
			} else if (explicitbuild) {
				model = prism.buildModelExplicit(modulesFileToBuild);
			} else {
				model = prism.buildModel(modulesFileToBuild);
			}
			modelExpl = null;
		} else {
			if (importtrans) {
				// TODO: add -importtrans case using model.buildFromPrismExplicit(...);
				throw new PrismException("Explicit import not yet supported for explicit engine");
			} else {
				modelExpl = prismExpl.buildModel(modulesFileToBuild, prism.getSimulator());
			}
			model = null;
		}

		// print model info
		if (!explicit) {
			mainLog.println("\nType:        " + model.getModelType());
			mainLog.print("Modules:     ");
			for (i = 0; i < model.getNumModules(); i++) {
				mainLog.print(model.getModuleName(i) + " ");
			}
			mainLog.println();
			mainLog.print("Variables:   ");
			for (i = 0; i < model.getNumVars(); i++) {
				mainLog.print(model.getVarName(i) + " ");
			}
			mainLog.println();
		} else {
			mainLog.println("\nType:        " + modelExpl.getModelType());
		}

		// check for deadlocks
		if (!explicit) {
			states = model.getDeadlockStates();
			if (states != null && states.size() > 0) {
				// for pta models (via digital clocks)
				if (digital) {
					// by construction, these can only occur from timelocks
					throw new PrismException("Timelock in PTA, e.g. in state (" + states.getFirstAsValues() + ")");
				}
				// if requested, remove them
				else if (fixdl) {
					mainLog.print("\nWarning: " + states.size() + " deadlock states detected; adding self-loops in these states...\n");
					model.fixDeadlocks();
				}
				// otherwise print error and bail out
				else {
					mainLog.println();
					model.printTransInfo(mainLog, prism.getExtraDDInfo());
					mainLog.print("\nError: Model contains " + states.size() + " deadlock states");
					if (!verbose && states.size() > 10) {
						mainLog.print(".\nThe first 10 deadlock states are displayed below. To view them all use the -v switch.\n");
						states.print(mainLog, 10);
					} else {
						mainLog.print(":\n");
						states.print(mainLog);
					}
					mainLog.print("\nTip: Use the -fixdl switch to automatically add self-loops in deadlock states.\n");
					model.clear();
					exit();
				}
			}
		} else {
			BitSet deadlocks = modelExpl.findDeadlocks(fixdl);
			if (deadlocks.cardinality() > 0) {
				if (fixdl) {
					mainLog.println("Added self-loops in " + deadlocks.cardinality() + " states...");
				} else {
					mainLog.println();
					mainLog.print("\nError: Model contains " + deadlocks.size() + " deadlock states");
					mainLog.print("\nTip: Use the -fixdl switch to automatically add self-loops in deadlock states.\n");
					exit();
				}
			}
		}
		
		// print more model info
		mainLog.println();
		if (!explicit) {
			model.printTransInfo(mainLog, prism.getExtraDDInfo());
		} else {
			mainLog.print(modelExpl.infoStringTable());
		}
		
		// If enabled, also construct model explicitly and compare (for testing purposes)
		if (explicitbuildtest) {
			String tmpFile = "'";
			try {
				explicit.ConstructModel constructModel = new explicit.ConstructModel(prism.getSimulator(), mainLog);
				mainLog.println("\nConstructing model explicitly...");
				explicit.Model modelExplicit = constructModel.constructModel(modulesFileToBuild);
				tmpFile = File.createTempFile("explicitbuildtest", ".tra").getAbsolutePath();
				tmpFile = "explicitbuildtest.tra";
				mainLog.println("\nExporting (explicit) model to \"" + tmpFile + "1\"...");
				modelExplicit.exportToPrismExplicitTra(tmpFile + "1");
				mainLog.println("\nExporting (normal) model to \"" + tmpFile + "2\"...");
				prism.exportTransToFile(model, true, Prism.EXPORT_PLAIN, new File(tmpFile + "2"));
				explicit.ModelSimple modelExplicit2 = null;
				switch (model.getModelType()) {
				case DTMC:
					modelExplicit2 = new explicit.DTMCSimple();
					break;
				case CTMC:
					modelExplicit2 = new explicit.CTMCSimple();
					break;
				case MDP:
					modelExplicit2 = new explicit.MDPSimple();
					break;
				}
				modelExplicit2.buildFromPrismExplicit(tmpFile + "2");
				if (!modelExplicit.equals(modelExplicit2)) {
					throw new PrismException("Explicit models differ");
				}
			} catch (IOException e) {
				throw new PrismException("Could not create temporary file \"" + tmpFile + "\"");
			}
		}
	}

	// do any exporting requested

	private void doExports()
	{
		// export transition matrix to a file
		if (exporttrans) {
			try {
				File f = (exportTransFilename.equals("stdout")) ? null : new File(exportTransFilename);
				prism.exportTransToFile(model, exportordered, exportType, f);
			}
			// in case of error, report it and proceed
			catch (FileNotFoundException e) {
				error("Couldn't open file \"" + exportTransFilename + "\" for output");
			} catch (PrismException e) {
				error(e.getMessage());
			}

			if (exportPlainDeprecated)
				mainLog.println("\nWarning: The -exportplain switch is now deprecated. Please use -exporttrans in future.");
		}

		// export state rewards to a file
		if (exportstaterewards) {
			try {
				File f = (exportStateRewardsFilename.equals("stdout")) ? null : new File(exportStateRewardsFilename);
				prism.exportStateRewardsToFile(model, exportType, f);
			}
			// in case of error, report it and proceed
			catch (FileNotFoundException e) {
				error("Couldn't open file \"" + exportStateRewardsFilename + "\" for output");
			} catch (PrismException e) {
				error(e.getMessage());
			}
		}

		// export transition rewards to a file
		if (exporttransrewards) {
			try {
				File f = (exportTransRewardsFilename.equals("stdout")) ? null : new File(exportTransRewardsFilename);
				prism.exportTransRewardsToFile(model, exportordered, exportType, f);
			}
			// in case of error, report it and proceed
			catch (FileNotFoundException e) {
				error("Couldn't open file \"" + exportTransRewardsFilename + "\" for output");
			} catch (PrismException e) {
				error(e.getMessage());
			}
		}

		// export states list
		if (exportstates) {
			try {
				File f = (exportStatesFilename.equals("stdout")) ? null : new File(exportStatesFilename);
				prism.exportStatesToFile(model, exportType, f);
			}
			// in case of error, report it and proceed
			catch (FileNotFoundException e) {
				error("Couldn't open file \"" + exportStatesFilename + "\" for output");
			}
		}

		// export to spy file
		if (exportspy) {
			try {
				prism.exportToSpyFile(model, new File(exportSpyFilename));
			}
			// in case of error, report it and proceed
			catch (FileNotFoundException e) {
				error("Couldn't open file \"" + exportSpyFilename + "\" for output");
			}
		}

		// export mtbdd to dot file
		if (exportdot) {
			try {
				prism.exportToDotFile(model, new File(exportDotFilename));
			}
			// in case of error, report it and proceed
			catch (FileNotFoundException e) {
				error("Couldn't open file \"" + exportDotFilename + "\" for output");
			}
		}

		// export transition matrix graph to dot file
		if (exporttransdot) {
			try {
				File f = (exportTransDotFilename.equals("stdout")) ? null : new File(exportTransDotFilename);
				prism.exportTransToFile(model, exportordered, Prism.EXPORT_DOT, f);
			}
			// in case of error, report it and proceed
			catch (FileNotFoundException e) {
				error("Couldn't open file \"" + exportTransDotFilename + "\" for output");
			} catch (PrismException e) {
				error(e.getMessage());
			}
		}

		// export transition matrix graph to dot file (with states)
		if (exporttransdotstates) {
			try {
				File f = (exportTransDotStatesFilename.equals("stdout")) ? null : new File(exportTransDotStatesFilename);
				prism.exportTransToFile(model, exportordered, Prism.EXPORT_DOT_STATES, f);
			}
			// in case of error, report it and proceed
			catch (FileNotFoundException e) {
				error("Couldn't open file \"" + exportTransDotStatesFilename + "\" for output");
			} catch (PrismException e) {
				error(e.getMessage());
			}
		}

		// export BSCCs to a file
		if (exportbsccs) {
			try {
				File f = (exportBSCCsFilename.equals("stdout")) ? null : new File(exportBSCCsFilename);
				prism.exportBSCCsToFile(model, exportType, f);
			}
			// in case of error, report it and proceed
			catch (FileNotFoundException e) {
				error("Couldn't open file \"" + exportBSCCsFilename + "\" for output");
			}
		}
	}

	// do steady state computation (if required)

	private void doSteadyState() throws PrismException
	{
		File exportSteadyStateFile = null;

		// choose destination for output (file or log)
		if (exportSteadyStateFilename == null || exportSteadyStateFilename.equals("stdout"))
			exportSteadyStateFile = null;
		else
			exportSteadyStateFile = new File(exportSteadyStateFilename);

		// compute steady-state probabilities
		if (model.getModelType() == ModelType.CTMC || model.getModelType() == ModelType.DTMC) {
			prism.doSteadyState(model, exportType, exportSteadyStateFile);
		} else {
			mainLog.println("\nWarning: Steady-state probabilities only computed for DTMCs/CTMCs.");
		}
	}

	// do transient computation (if required)

	private void doTransient() throws PrismException
	{
		double d;
		int i;
		File exportTransientFile = null;

		// choose destination for output (file or log)
		if (exportTransientFilename == null || exportTransientFilename.equals("stdout"))
			exportTransientFile = null;
		else
			exportTransientFile = new File(exportTransientFilename);

		// compute transient probabilities
		if (model.getModelType() == ModelType.CTMC) {
			try {
				d = Double.parseDouble(transientTime);
			} catch (NumberFormatException e) {
				throw new PrismException("Invalid value \"" + transientTime + "\" for transient probability computation");
			}
			prism.doTransient(model, d, exportType, exportTransientFile, importinitdist ? new File(importInitDistFilename) : null);
		} else if (model.getModelType() == ModelType.DTMC) {
			try {
				i = Integer.parseInt(transientTime);
			} catch (NumberFormatException e) {
				throw new PrismException("Invalid value \"" + transientTime + "\" for transient probability computation");
			}
			prism.doTransient(model, i, exportType, exportTransientFile, importinitdist ? new File(importInitDistFilename) : null);
		} else {
			mainLog.println("\nWarning: Transient probabilities only computed for DTMCs/CTMCs.");
		}
	}

	// close down

	private void closeDown()
	{
		// clear up and close down
		prism.closeDown(true);
		mainLog.println();
	}

	// parse command line arguments

	public void parseArguments(String[] args) throws PrismException
	{
		int i, j;
		double d;
		String sw, s;
		PrismLog log;

		constSwitch = "";

		for (i = 0; i < args.length; i++) {

			// if is a switch...
			if (args[i].length() > 0 && args[i].charAt(0) == '-') {

				sw = args[i].substring(1);

				// print help
				if (sw.equals("help") || sw.equals("-help") || sw.equals("?")) {
					printHelp();
					exit();
				}
				// print version
				else if (sw.equals("version")) {
					printVersion();
					exit();
				}
				// print a list of all keywords (hidden option)
				else if (sw.equals("keywords")) {
					printListOfKeywords();
					exit();
				}

				// property/properties given in command line
				else if (sw.equals("pctl") || sw.equals("csl")) {
					if (i < args.length - 1) {
						propertyString = args[++i];
					} else {
						errorAndExit("No property specified for -" + sw + " switch");
					}
				}
				// do steady state
				else if (sw.equals("steadystate") || sw.equals("ss")) {
					steadystate = true;
				}
				// do steady state
				else if (sw.equals("transient") || sw.equals("tr")) {
					if (i < args.length - 1) {
						try {
							dotransient = true;
							transientTime = args[++i];
							// Make sure transient time parses as a +ve double
							d = Double.parseDouble(transientTime);
							if (d < 0)
								throw new NumberFormatException("");
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				}
				// export transition matrix to file
				else if (sw.equals("exporttrans")) {
					if (i < args.length - 1) {
						exporttrans = true;
						exportTransFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// export state rewards to file
				else if (sw.equals("exportstaterewards")) {
					if (i < args.length - 1) {
						exportstaterewards = true;
						exportStateRewardsFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// export transition rewards to file
				else if (sw.equals("exporttransrewards")) {
					if (i < args.length - 1) {
						exporttransrewards = true;
						exportTransRewardsFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// export both state/transition rewards to file
				else if (sw.equals("exportrewards")) {
					if (i < args.length - 2) {
						exportstaterewards = true;
						exporttransrewards = true;
						exportStateRewardsFilename = args[++i];
						exportTransRewardsFilename = args[++i];
					} else {
						errorAndExit("Two files must be specified for -" + sw + " switch");
					}
				}
				// export states
				else if (sw.equals("exportstates")) {
					if (i < args.length - 1) {
						exportstates = true;
						exportStatesFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// export labels/states
				else if (sw.equals("exportlabels")) {
					if (i < args.length - 1) {
						exportlabels = true;
						exportLabelsFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// export to spy file
				else if (sw.equals("exportspy")) {
					if (i < args.length - 1) {
						exportspy = true;
						exportSpyFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// export to dot file
				else if (sw.equals("exportdot")) {
					if (i < args.length - 1) {
						exportdot = true;
						exportDotFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// export transition matrix graph to dot file
				else if (sw.equals("exporttransdot")) {
					if (i < args.length - 1) {
						exporttransdot = true;
						exportTransDotFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// export transition matrix graph to dot file (with states)
				else if (sw.equals("exporttransdotstates")) {
					if (i < args.length - 1) {
						exporttransdotstates = true;
						exportTransDotStatesFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// export bsccs to file
				else if (sw.equals("exportbsccs")) {
					if (i < args.length - 1) {
						exportbsccs = true;
						exportBSCCsFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// export prism model to file
				else if (sw.equals("exportprism")) {
					if (i < args.length - 1) {
						exportprism = true;
						exportPrismFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// export prism model to file (with consts expanded)
				else if (sw.equals("exportprismconst")) {
					if (i < args.length - 1) {
						exportprismconst = true;
						exportPrismConstFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// export adversary to file
				else if (sw.equals("exportadv")) {
					if (i < args.length - 1) {
						prism.setExportAdv(Prism.EXPORT_ADV_DTMC);
						prism.setExportAdvFilename(args[++i]);
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// export adversary to file, as an mdp
				else if (sw.equals("exportadvmdp")) {
					if (i < args.length - 1) {
						prism.setExportAdv(Prism.EXPORT_ADV_MDP);
						prism.setExportAdvFilename(args[++i]);
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// export reachability target info to file
				else if (sw.equals("exporttarget")) {
					if (i < args.length - 1) {
						prism.setExportTarget(true);
						prism.setExportTargetFilename(args[++i]);
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// disable model construction
				else if (sw.equals("nobuild")) {
					nobuild = true;
				}
				// set scc computation algorithm
				else if (sw.equals("sccmethod") || sw.equals("bsccmethod")) {
					if (i < args.length - 1) {
						s = args[++i];
						if (s.equals("xiebeerel"))
							prism.getSettings().set(PrismSettings.PRISM_SCC_METHOD, "Xie-Beerel");
						else if (s.equals("lockstep"))
							prism.getSettings().set(PrismSettings.PRISM_SCC_METHOD, "Lockstep");
						else if (s.equals("sccfind"))
							prism.getSettings().set(PrismSettings.PRISM_SCC_METHOD, "SCC-Find");
						else
							errorAndExit("Unrecognised option for -" + sw + " switch (options are: xiebeerel, lockstep, sccfind)");
					} else {
						errorAndExit("No parameter specified for -" + sw + " switch");
					}
				}
				// export results
				else if (sw.equals("exportresults")) {
					if (i < args.length - 1) {
						exportresults = true;
						exportResultsFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// export steady-state probs (as opposed to displaying on screen) 
				else if (sw.equals("exportsteadystate") || sw.equals("exportss")) {
					if (i < args.length - 1) {
						exportSteadyStateFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// export transient probs (as opposed to displaying on screen) 
				else if (sw.equals("exporttransient") || sw.equals("exporttr")) {
					if (i < args.length - 1) {
						exportTransientFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// switch export mode to "matlab"
				else if (sw.equals("exportmatlab")) {
					exportType = Prism.EXPORT_MATLAB;
				}
				// switch export mode to "mrmc"
				else if (sw.equals("exportmrmc")) {
					exportType = Prism.EXPORT_MRMC;
				}
				// switch export mode to "rows"
				else if (sw.equals("exportrows")) {
					exportType = Prism.EXPORT_ROWS;
				}
				// export model to plain text file (deprecated)
				else if (sw.equals("exportplain")) {
					if (i < args.length - 1) {
						exporttrans = true;
						exportType = Prism.EXPORT_PLAIN;
						exportTransFilename = args[++i];
						exportPlainDeprecated = true;
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// exported matrix entries are ordered
				else if (sw.equals("exportordered") || sw.equals("ordered")) {
					exportordered = true;
				}
				// exported matrix entries are unordered
				else if (sw.equals("exportunordered") || sw.equals("unordered")) {
					exportordered = false;
				}
				// change model type to pepa
				else if (sw.equals("importpepa")) {
					importpepa = true;
				}
				// import model from explicit format
				else if (sw.equals("importtrans")) {
					importtrans = true;
				}
				// import states for explicit model import
				else if (sw.equals("importstates")) {
					if (i < args.length - 1) {
						importstates = true;
						importStatesFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// import labels for explicit model import
				else if (sw.equals("importlabels")) {
					if (i < args.length - 1) {
						importlabels = true;
						importLabelsFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// import initial distribution e.g. for transient probability distribution
				else if (sw.equals("importinitdist")) {
					if (i < args.length - 1) {
						importinitdist = true;
						importInitDistFilename = args[++i];
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// override model type to dtmc
				else if (sw.equals("dtmc")) {
					typeOverride = ModelType.DTMC;
				}
				// override model type to mdp
				else if (sw.equals("mdp")) {
					typeOverride = ModelType.MDP;
				}
				// override model type to ctmc
				else if (sw.equals("ctmc")) {
					typeOverride = ModelType.CTMC;
				}
				// use simulator
				else if (sw.equals("sim")) {
					simulate = true;
				}
				// use the number of iterations given instead of automatically deciding whether the variance is null ot not
				else if (sw.equals("simmanual")) {
					simManual = true;
				}
				// generate path with simulator
				else if (sw.equals("simpath")) {
					if (i < args.length - 2) {
						simpath = true;
						simpathDetails = args[++i];
						simpathFilename = args[++i];
					} else {
						errorAndExit("The -" + sw + " switch requires two arguments (path details, filename)");
					}
				}
				// which property to check
				else if (sw.equals("prop") || sw.equals("property")) {
					if (i < args.length - 1) {
						try {
							propertyToCheck = Integer.parseInt(args[++i]);
							if (propertyToCheck < 1)
								throw new NumberFormatException();
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				}

				// definition of undefined constants
				else if (sw.equals("const")) {
					if (i < args.length - 1) {
						// store argument for later use (append if already partially specified)
						if ("".equals(constSwitch))
							constSwitch = args[++i].trim();
						else
							constSwitch += "," + args[++i].trim();
					} else {
						errorAndExit("Incomplete -" + sw + " switch");
					}
				}

				// logs

				// specify main log
				else if (sw.equals("mainlog")) {
					if (i < args.length - 1) {
						mainLogFilename = args[++i];
						// use temporary storage because an error would go to the old log
						log = new PrismFileLog(mainLogFilename);
						if (!log.ready()) {
							errorAndExit("Couldn't open log file \"" + mainLogFilename + "\"");
						}
						mainLog = log;
						prism.setMainLog(mainLog);
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}
				// specify mtbdd log
				else if (sw.equals("techlog")) {
					if (i < args.length - 1) {
						techLogFilename = args[++i];
						log = new PrismFileLog(techLogFilename);
						if (!log.ready()) {
							errorAndExit("Couldn't open log file \"" + techLogFilename + "\"");
						}
						techLog = log;
						prism.setTechLog(techLog);
					} else {
						errorAndExit("No file specified for -" + sw + " switch");
					}
				}

				// engine

				// set engine to 'mtbdd'
				else if (sw.equals("mtbdd") || sw.equals("m")) {
					prism.setEngine(Prism.MTBDD);
					// and change default ordering
					prism.setOrdering(2);
				}
				// set engine to 'sparse'
				else if (sw.equals("sparse") || sw.equals("s")) {
					prism.setEngine(Prism.SPARSE);
				}
				// set engine to 'hybrid'
				else if (sw.equals("hybrid") || sw.equals("h")) {
					prism.setEngine(Prism.HYBRID);
				}

				// model construction options

				// mtbdd construction method
				else if (sw.equals("c1")) {
					prism.setConstruction(1);
				} else if (sw.equals("c2")) {
					prism.setConstruction(2);
				} else if (sw.equals("c3")) {
					prism.setConstruction(3);
				}
				// mtbdd variable ordering
				else if (sw.equals("o1")) {
					prism.setOrdering(1);
				} else if (sw.equals("o2")) {
					prism.setOrdering(2);
				}
				// zero-reward loops check on
				else if (sw.equals("zerorewardcheck")) {
					prism.setCheckZeroLoops(true);
				}
				// reachability off
				else if (sw.equals("noreach")) {
					prism.setDoReach(false);
				}
				// prob/rate checks off
				else if (sw.equals("noprobchecks")) {
					prism.setDoProbChecks(false);
				}

				// model checking options

				// linear equation solver method
				else if (sw.equals("power") || sw.equals("pow") || sw.equals("pwr")) {
					prism.setLinEqMethod(Prism.POWER);
				} else if (sw.equals("jacobi") || sw.equals("jac")) {
					prism.setLinEqMethod(Prism.JACOBI);
				} else if (sw.equals("gaussseidel") || sw.equals("gs")) {
					prism.setLinEqMethod(Prism.GAUSSSEIDEL);
					prism.setMDPSolnMethod(Prism.MDP_GAUSSSEIDEL);
				} else if (sw.equals("bgaussseidel") || sw.equals("bgs")) {
					prism.setLinEqMethod(Prism.BGAUSSSEIDEL);
				} else if (sw.equals("pgaussseidel") || sw.equals("pgs")) {
					prism.setLinEqMethod(Prism.PGAUSSSEIDEL);
				} else if (sw.equals("bpgaussseidel") || sw.equals("bpgs")) {
					prism.setLinEqMethod(Prism.BPGAUSSSEIDEL);
				} else if (sw.equals("jor")) {
					prism.setLinEqMethod(Prism.JOR);
					prism.setLinEqMethodParam(0.9);
				} else if (sw.equals("sor")) {
					prism.setLinEqMethod(Prism.SOR);
					prism.setLinEqMethodParam(0.9);
				} else if (sw.equals("bsor")) {
					prism.setLinEqMethod(Prism.BSOR);
					prism.setLinEqMethodParam(0.9);
				} else if (sw.equals("psor")) {
					prism.setLinEqMethod(Prism.PSOR);
					prism.setLinEqMethodParam(0.9);
				} else if (sw.equals("bpsor")) {
					prism.setLinEqMethod(Prism.BPSOR);
					prism.setLinEqMethodParam(0.9);
				}
				// linear equation solver parameter
				else if (sw.equals("omega")) {
					if (i < args.length - 1) {
						try {
							d = Double.parseDouble(args[++i]);
							prism.setLinEqMethodParam(d);
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				}
				// MDP solution method
				else if (sw.equals("valiter")) {
					prism.setMDPSolnMethod(Prism.MDP_VALITER);
				}
				else if (sw.equals("politer")) {
					prism.setMDPSolnMethod(Prism.MDP_POLITER);
				}
				else if (sw.equals("modpoliter")) {
					prism.setMDPSolnMethod(Prism.MDP_MODPOLITER);
				}
				// termination criterion (iterative methods)
				else if (sw.equals("absolute") || sw.equals("abs")) {
					prism.setTermCrit(Prism.ABSOLUTE);
				} else if (sw.equals("relative") || sw.equals("rel")) {
					prism.setTermCrit(Prism.RELATIVE);
				}
				// termination criterion parameter
				else if (sw.equals("epsilon") || sw.equals("e")) {
					if (i < args.length - 1) {
						try {
							d = Double.parseDouble(args[++i]);
							if (d < 0)
								throw new NumberFormatException("");
							prism.setTermCritParam(d);
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				}
				// max iters
				else if (sw.equals("maxiters")) {
					if (i < args.length - 1) {
						try {
							j = Integer.parseInt(args[++i]);
							if (j < 0)
								throw new NumberFormatException("");
							prism.setMaxIters(j);
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				}
				// verbose on
				else if (sw.equals("verbose") || sw.equals("v")) {
					prism.setVerbose(true);
					verbose = true;
				}
				// extra dd info on
				else if (sw.equals("extraddinfo")) {
					prism.setExtraDDInfo(true);
				}
				// extra reach info on
				else if (sw.equals("extrareachinfo")) {
					prism.setExtraReachInfo(true);
				}
				// precomputation algs off
				else if (sw.equals("nopre")) {
					prism.setPrecomp(false);
				}
				// fairness on
				else if (sw.equals("fair")) {
					prism.setFairness(true);
				}
				// fairness off
				else if (sw.equals("nofair")) {
					prism.setFairness(false);
				}
				// fix deadlocks
				else if (sw.equals("fixdl")) {
					fixdl = true;
				}
				// no bscc computation
				else if (sw.equals("nobscc")) {
					prism.setBSCCComp(false);
				}
				// no steady-state detection
				else if (sw.equals("nossdetect")) {
					prism.setDoSSDetect(false);
				}
				// sparse bits info
				else if (sw.equals("sbmax")) {
					if (i < args.length - 1) {
						try {
							j = Integer.parseInt(args[++i]);
							if (j < 0)
								throw new NumberFormatException();
							prism.setSBMaxMem(j);
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				} else if (sw.equals("sbl")) {
					if (i < args.length - 1) {
						try {
							j = Integer.parseInt(args[++i]);
							if (j < -1)
								throw new NumberFormatException();
							prism.setNumSBLevels(j);
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				}
				// hybrid sor info
				else if (sw.equals("sormax") || sw.equals("gsmax")) {
					if (i < args.length - 1) {
						try {
							j = Integer.parseInt(args[++i]);
							if (j < 0)
								throw new NumberFormatException();
							prism.setSORMaxMem(j);
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				} else if (sw.equals("sorl") || sw.equals("gsl")) {
					if (i < args.length - 1) {
						try {
							j = Integer.parseInt(args[++i]);
							if (j < -1)
								throw new NumberFormatException();
							prism.setNumSORLevels(j);
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				}
				// turn off compact option for sparse matrix storage
				else if (sw.equals("nocompact")) {
					prism.setCompact(false);
				}
				// cudd settings
				else if (sw.equals("cuddmaxmem")) {
					if (i < args.length - 1) {
						try {
							j = Integer.parseInt(args[++i]);
							if (j < 0)
								throw new NumberFormatException();
							prism.setCUDDMaxMem(j);
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				} else if (sw.equals("cuddepsilon")) {
					if (i < args.length - 1) {
						try {
							d = Double.parseDouble(args[++i]);
							if (d < 0)
								throw new NumberFormatException("");
							prism.setCUDDEpsilon(d);
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				}

				// simulation-based model checking methods
				else if (sw.equals("simmethod")) {
					if (i < args.length - 1) {
						s = args[++i];
						if (s.equals("ci") || s.equals("aci") || s.equals("apmc") || s.equals("sprt"))
							simMethodName = s;
						else
							errorAndExit("Unrecognised option for -" + sw + " switch (options are: ci, aci, apmc, sprt)");
					} else {
						errorAndExit("No parameter specified for -" + sw + " switch");
					}
				}
				// simulation confidence interval width
				else if (sw.equals("simwidth")) {
					if (i < args.length - 1) {
						try {
							simWidth = Double.parseDouble(args[++i]);
							if (simWidth <= 0)
								throw new NumberFormatException("");
							simWidthGiven = true;
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				}
				// simulation approximation parameter
				else if (sw.equals("simapprox")) {
					if (i < args.length - 1) {
						try {
							simApprox = Double.parseDouble(args[++i]);
							if (simApprox <= 0)
								throw new NumberFormatException("");
							simApproxGiven = true;
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				}
				// simulation confidence parameter
				else if (sw.equals("simconf")) {
					if (i < args.length - 1) {
						try {
							simConfidence = Double.parseDouble(args[++i]);
							if (simConfidence <= 0 || simConfidence >= 1)
								throw new NumberFormatException("");
							simConfidenceGiven = true;
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				}
				// simulation number of samples
				else if (sw.equals("simsamples")) {
					if (i < args.length - 1) {
						try {
							simNumSamples = Integer.parseInt(args[++i]);
							if (simNumSamples <= 0)
								throw new NumberFormatException("");
							simNumSamplesGiven = true;
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				}
				// simulation number of samples to conclude S^2=0 or not
				else if (sw.equals("simvar")) {
					if (i < args.length - 1) {
						try {
							reqIterToConclude = Integer.parseInt(args[++i]);
							if (reqIterToConclude <= 0)
								throw new NumberFormatException("");
							reqIterToConcludeGiven = true;
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				}
				// maximum value of reward
				else if (sw.equals("simmaxrwd")) {
					if (i < args.length - 1) {
						try {
							simMaxReward = Double.parseDouble(args[++i]);
							if (simMaxReward <= 0.0)
								throw new NumberFormatException("");
							simMaxRewardGiven = true;
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				}
				// simulation max path length
				else if (sw.equals("simpathlen")) {
					if (i < args.length - 1) {
						try {
							simMaxPath = Integer.parseInt(args[++i]);
							if (simMaxPath <= 0)
								throw new NumberFormatException("");
							simMaxPathGiven = true;
						} catch (NumberFormatException e) {
							errorAndExit("Invalid value for -" + sw + " switch");
						}
					} else {
						errorAndExit("No value specified for -" + sw + " switch");
					}
				}

				// enable symmetry reduction
				else if (sw.equals("symm")) {
					if (i < args.length - 2) {
						prism.getSettings().set(PrismSettings.PRISM_SYMM_RED_PARAMS, args[++i] + " " + args[++i]);
					} else {
						errorAndExit("-symm switch requires two parameters (num. modules before/after symmetric ones)");
					}
				}

				// pta model checking methods
				else if (sw.equals("ptamethod")) {
					if (i < args.length - 1) {
						s = args[++i];
						if (s.equals("digital"))
							prism.getSettings().set(PrismSettings.PRISM_PTA_METHOD, "Digital clocks");
						else if (s.equals("games"))
							prism.getSettings().set(PrismSettings.PRISM_PTA_METHOD, "Stochastic games");
						else if (s.equals("bisim"))
							prism.getSettings().set(PrismSettings.PRISM_PTA_METHOD, "Bisimulation minimisation");
						else
							errorAndExit("Unrecognised option for -" + sw + " switch (options are: digital, games)");
					} else {
						errorAndExit("No parameter specified for -" + sw + " switch");
					}
				}

				// abstraction-refinement engine options string (append if already partially specified)
				else if (sw.equals("aroptions")) {
					if (i < args.length - 1) {
						String arOptions = prism.getSettings().getString(PrismSettings.PRISM_AR_OPTIONS);
						if ("".equals(arOptions))
							arOptions = args[++i].trim();
						else
							arOptions += "," + args[++i].trim();
						prism.getSettings().set(PrismSettings.PRISM_AR_OPTIONS, arOptions);
					} else {
						errorAndExit("No parameter specified for -" + sw + " switch");
					}
				}

				// enable explicit-state engine
				else if (sw.equals("explicit")) {
					explicit = true;
				}
				
				// explicit-state model construction
				else if (sw.equals("explicitbuild")) {
					explicitbuild = true;
				}
				
				// (hidden) option for testing of prototypical explicit-state model construction
				else if (sw.equals("explicitbuildtest")) {
					explicitbuildtest = true;
				}

				// unknown switch - error
				else {
					errorAndExit("Invalid switch -" + sw + " (type \"prism -help\" for full list)");
				}
			}
			// otherwise argument must be a filename
			else if (modelFilename == null) {
				modelFilename = args[i];
			} else if (propertiesFilename == null) {
				propertiesFilename = args[i];
			}
			// anything else - must be something wrong with command line syntax
			else {
				errorAndExit("Invalid argument syntax");
			}
		}
	}

	// print command line arguments

	public void printArguments(String[] args)
	{
		String s;
		int i;
		mainLog.print("Command line: prism");
		// Print out the command-line arguments
		for (i = 0; i < args.length; i++) {
			s = args[i];
			// If necessary add quotes so can be pasted back into a shell
			// (where "necessary" means contains any non-safe characters)
			if (s.matches(".*[^_a-zA-Z0-9\\./\\-=].*")) {
				s = "'" + s + "'";
			}
			mainLog.print(" " + s);
		}
		mainLog.println();
	}

	// do some processing of the options

	private void processOptions()
	{
		int j;

		// make sure a model file is specified
		if (modelFilename == null) {
			mainLog.println("Usage: prism [options] <model-file> [<properties-file>] [more-options]");
			mainLog.println("\nFor more information, type: prism -help");
			exit();
		}

		// explicit overrides explicit build
		if (explicit) {
			explicitbuild = false;
		}
		
		// check not trying to do gauss-seidel with mtbdd engine
		if (prism.getEngine() == Prism.MTBDD) {
			j = prism.getLinEqMethod();
			if (j == Prism.GAUSSSEIDEL || j == Prism.BGAUSSSEIDEL || j == Prism.PGAUSSSEIDEL || j == Prism.BPGAUSSSEIDEL) {
				errorAndExit("Gauss-Seidel and its variants are currently not supported by the MTBDD engine");
			}
			if (j == Prism.SOR || j == Prism.BSOR || j == Prism.PSOR || j == Prism.BPSOR) {
				errorAndExit("SOR and its variants are currently not supported by the MTBDD engine");
			}
		}

		// or pseudo methods with sparse engine
		else if (prism.getEngine() == Prism.SPARSE) {
			j = prism.getLinEqMethod();
			if (j == Prism.PGAUSSSEIDEL || j == Prism.BPGAUSSSEIDEL || j == Prism.PSOR || j == Prism.BPSOR) {
				errorAndExit("Pseudo Gauss-Seidel/SOR methods are currently not supported by the sparse engine");
			}
		}
	}

	/**
	 * Process the simulation-related command-line options and generate
	 * a SimulationMethod object to be used for approximate model checking.
	 * @param expr The property to be checked (note: constants may not be defined)
	 * @throws PrismException if there are problems with the specified options
	 */
	private SimulationMethod processSimulationOptions(Expression expr) throws PrismException
	{
		SimulationMethod aSimMethod = null;

		// See if property to be checked is a reward (R) operator
		boolean isReward = (expr instanceof ExpressionReward);

		// See if property to be checked is quantitative (=?)
		boolean isQuant = Expression.isQuantitative(expr);

		// Pick defaults for simulation settings not set from command-line
		if (!simApproxGiven)
			simApprox = prism.getSettings().getDouble(PrismSettings.SIMULATOR_DEFAULT_APPROX);
		if (!simConfidenceGiven)
			simConfidence = prism.getSettings().getDouble(PrismSettings.SIMULATOR_DEFAULT_CONFIDENCE);
		if (!simNumSamplesGiven)
			simNumSamples = prism.getSettings().getInteger(PrismSettings.SIMULATOR_DEFAULT_NUM_SAMPLES);
		if (!simWidthGiven)
			simWidth = prism.getSettings().getDouble(PrismSettings.SIMULATOR_DEFAULT_WIDTH);

		if (!reqIterToConcludeGiven)
			reqIterToConclude = prism.getSettings().getInteger(PrismSettings.SIMULATOR_DECIDE);
		if (!simMaxRewardGiven)
			simMaxReward = prism.getSettings().getDouble(PrismSettings.SIMULATOR_MAX_REWARD);
		if (!simMaxPathGiven)
			simMaxPath = prism.getSettings().getInteger(PrismSettings.SIMULATOR_DEFAULT_MAX_PATH);

		// Pick a default method, if not specified
		// (CI for quantitative, SPRT for bounded)
		if (simMethodName == null) {
			simMethodName = isQuant ? "ci" : "sprt";
		}

		// CI
		if (simMethodName.equals("ci")) {
			if (simWidthGiven && simConfidenceGiven && simNumSamplesGiven) {
				throw new PrismException("Cannot specify all three parameters (width/confidence/samples) for CI method");
			}
			if (!simWidthGiven) {
				// Default (unless width specified) is to leave width unknown
				aSimMethod = new CIwidth(simConfidence, simNumSamples);
			} else if (!simNumSamplesGiven) {
				// Next preferred option (unless specified) is unknown samples
				if (simManual)
					aSimMethod = new CIiterations(simConfidence, simWidth, reqIterToConclude);
				else
					aSimMethod = (isReward ? new CIiterations(simConfidence, simWidth, simMaxReward) : new CIiterations(simConfidence, simWidth));
			} else {
				// Otherwise confidence unknown
				aSimMethod = new CIconfidence(simWidth, simNumSamples);
			}
			if (simApproxGiven) {
				mainLog.println("\nWarning: Option -simapprox is not used for the CI method and is being ignored");
			}
		}
		// ACI
		else if (simMethodName.equals("aci")) {
			if (simWidthGiven && simConfidenceGiven && simNumSamplesGiven) {
				throw new PrismException("Cannot specify all three parameters (width/confidence/samples) for ACI method");
			}
			if (!simWidthGiven) {
				// Default (unless width specified) is to leave width unknown
				aSimMethod = new ACIwidth(simConfidence, simNumSamples);
			} else if (!simNumSamplesGiven) {
				// Next preferred option (unless specified) is unknown samples
				if (simManual)
					aSimMethod = new ACIiterations(simConfidence, simWidth, reqIterToConclude);
				else
					aSimMethod = (isReward ? new ACIiterations(simConfidence, simWidth, simMaxReward) : new CIiterations(simConfidence, simWidth));
			} else {
				// Otherwise confidence unknown
				aSimMethod = new ACIconfidence(simWidth, simNumSamples);
			}
			if (simApproxGiven) {
				mainLog.println("\nWarning: Option -simapprox is not used for the ACI method and is being ignored");
			}
		}
		// APMC
		else if (simMethodName.equals("apmc")) {
			if (isReward) {
				throw new PrismException("Cannot use the APMC method on reward properties; try CI (switch -simci) instead");
			}
			if (simApproxGiven && simConfidenceGiven && simNumSamplesGiven) {
				throw new PrismException("Cannot specify all three parameters (approximation/confidence/samples) for APMC method");
			}
			if (!simApproxGiven) {
				// Default (unless width specified) is to leave approximation unknown
				aSimMethod = new APMCapproximation(simConfidence, simNumSamples);
			} else if (!simNumSamplesGiven) {
				// Next preferred option (unless specified) is unknown samples
				aSimMethod = new APMCiterations(simConfidence, simApprox);
			} else {
				// Otherwise confidence unknown
				aSimMethod = new APMCconfidence(simApprox, simNumSamples);
			}
			if (simWidthGiven) {
				mainLog.println("\nWarning: Option -simwidth is not used for the APMC method and is being ignored");
			}
		}
		// SPRT
		else if (simMethodName.equals("sprt")) {
			if (isQuant) {
				throw new PrismException("Cannot use SPRT on a quantitative (=?) property");
			}
			aSimMethod = new SPRTMethod(simConfidence, simConfidence, simWidth);
			if (simApproxGiven) {
				mainLog.println("\nWarning: Option -simapprox is not used for the SPRT method and is being ignored");
			}
			if (simNumSamplesGiven) {
				mainLog.println("\nWarning: Option -simsamples is not used for the SPRT method and is being ignored");
			}
		} else
			throw new PrismException("Unknown simulation method \"" + simMethodName + "\"");

		return aSimMethod;
	}

	// print help message

	private void printHelp()
	{
		mainLog.println("Usage: prism <model-file> [<properties-file>] [options]");
		mainLog.println();
		mainLog.println("Options:");
		mainLog.println("========");
		mainLog.println();
		mainLog.println("-help .......................... Display this help message");
		mainLog.println("-version ....................... Display tool version");
		mainLog.println();
		mainLog.println("-pctl <prop> (or -csl <prop>) .. Model check the PCTL/CSL property <prop>");
		mainLog.println("-property <n> (or -prop <n>) ... Only model check property <n> from the properties file");
		mainLog.println("-const <vals> .................. Define constant values as <vals> (e.g. for experiments)");
		mainLog.println("-steadystate (or -ss) .......... Compute steady-state probabilities (D/CTMCs only)");
		mainLog.println("-transient <x> (or -tr <x>) .... Compute transient probabilities for time <x> (D/CTMCs only)");
		mainLog.println("-simpath <options> <file>....... Generate a random path with the simulator");
		mainLog.println("-nobuild ....................... Skip model construction (just do parse/export)");
		mainLog.println();
		mainLog.println("IMPORT OPTIONS:");
		mainLog.println("-importpepa .................... Model description is in PEPA, not the PRISM language");
		mainLog.println("-importtrans <file> ............ Import the transition matrix directly from a text file");
		mainLog.println("-importstates <file>............ Import the list of states directly from a text file");
		mainLog.println("-importlabels <file>............ Import the list of labels directly from a text file");
		mainLog.println("-importinit <expr>.............. Specify the initial state for explicitly imported models");
		mainLog.println("-importinitdist <expr>.......... Specify the initial probability distribution for transient analysis");
		mainLog.println("-dtmc .......................... Force imported/built model to be a DTMC");
		mainLog.println("-ctmc .......................... Force imported/built model to be a CTMC");
		mainLog.println("-mdp ........................... Force imported/built model to be an MDP");
		mainLog.println();
		mainLog.println("EXPORT OPTIONS:");
		mainLog.println("-exportresults <file> .......... Export the results of model checking to a file");
		mainLog.println("-exporttrans <file> ............ Export the transition matrix to a file");
		mainLog.println("-exportstaterewards <file> ..... Export the state rewards vector to a file");
		mainLog.println("-exporttransrewards <file> ..... Export the transition rewards matrix to a file");
		mainLog.println("-exportstates <file> ........... Export the list of reachable states to a file");
		mainLog.println("-exportlabels <file> ........... Export the list of labels and satisfying states to a file");
		mainLog.println("-exportmatlab .................. When exporting matrices/vectors/labels/etc., use Matlab format");
		mainLog.println("-exportmrmc .................... When exporting matrices/vectors/labels, use MRMC format");
		mainLog.println("-exportrows .................... When exporting matrices, put a whole row on one line");
		mainLog.println("-exportordered ................. When exporting matrices, order entries (by row) [default]");
		mainLog.println("-exportunordered ............... When exporting matrices, don't order entries");
		mainLog.println("-exporttransdot <file> ......... Export the transition matrix graph to a dot file");
		mainLog.println("-exporttransdotstates <file> ... Export the transition matrix graph to a dot file, with state info");
		mainLog.println("-exportdot <file> .............. Export the transition matrix MTBDD to a dot file");
		mainLog.println("-exportbsccs <file> ............ Compute and export all BSCCs of the model");
		mainLog.println("-exportsteadystate <file> ...... Export steady-state probabilities to a file");
		mainLog.println("-exporttransient <file> ........ Export transient probabilities to a file");
		mainLog.println("-exportprism <file> ............ Export final PRISM model to a file");
		mainLog.println("-exportprismconst <file> ....... Export final PRISM model with expanded constants to a file");
		mainLog.println("-exportadv <file> .............. Export an adversary from MDP model checking (as a DTMC)");
		mainLog.println("-exportadvmdp <file> ........... Export an adversary from MDP model checking (as an MDP)");
		mainLog.println();
		mainLog.println("ENGINES/METHODS:");
		mainLog.println("-mtbdd (or -m) ................. Use the MTBDD engine");
		mainLog.println("-sparse (or -s) ................ Use the Sparse engine");
		mainLog.println("-hybrid (or -h) ................ Use the Hybrid engine [default]");
		mainLog.println("-ptamethod <name> .............. Specify PTA engine (games, digital) [default: games]");
		mainLog.println();
		mainLog.println("NUMERICAL SOLUTION OPTIONS:");
		mainLog.println("-power (or -pow, -pwr) ......... Use the Power method for numerical computation");
		mainLog.println("-jacobi (or -jac) .............. Use Jacobi for numerical computation [default]");
		mainLog.println("-gaussseidel (or -gs) .......... Use Gauss-Seidel for numerical computation");
		mainLog.println("-bgaussseidel (or -bgs) ........ Use Backwards Gauss-Seidel for numerical computation");
		mainLog.println("-pgaussseidel (or -pgs) ........ Use Pseudo Gauss-Seidel for numerical computation");
		mainLog.println("-bpgaussseidel (or -bpgs) ...... Use Backwards Pseudo Gauss-Seidel for numerical computation");
		mainLog.println("-jor ........................... Use JOR for numerical computation");
		mainLog.println("-sor ........................... Use SOR for numerical computation");
		mainLog.println("-bsor .......................... Use Backwards SOR for numerical computation");
		mainLog.println("-psor .......................... Use Pseudo SOR for numerical computation");
		mainLog.println("-bpsor ......................... Use Backwards Pseudo SOR for numerical computation");
		mainLog.println("-omega <x> ..................... Set over-relaxation parameter (for JOR/SOR/...) [default: 0.9]");
		mainLog.println("-relative (or -rel) ............ Use relative error for detecting convergence [default]");
		mainLog.println("-absolute (or -abs) ............ Use absolute error for detecting convergence");
		mainLog.println("-epsilon <x> (or -e <x>) ....... Set value of epsilon (for convergence check) [default: 1e-6]");
		mainLog.println("-maxiters <n> .................. Set max number of iterations [default: 10000]");
		mainLog.println();
		mainLog.println("MODEL CHECKING OPTIONS:");
		mainLog.println("-nopre ......................... Skip (optional) precomputation algorithms");
		mainLog.println("-fair .......................... Use fairness (for probabilistic reachability in MDPs)");
		mainLog.println("-nofair ........................ Don't use fairness (for probabilistic reachability in MDPs) [default]");
		mainLog.println("-fixdl ......................... Automatically put self-loops in deadlock states");
		mainLog.println("-noprobchecks .................. Disable checks on model probabilities/rates");
		mainLog.println("-zerorewardcheck ............... Check for absence of zero-reward loops");
		mainLog.println("-nossdetect .................... Disable steady-state detection for CTMC transient computations");
		mainLog.println("-sccmethod <name> .............. Specify SCC computation method (xiebeerel, lockstep, sccfind)");
		mainLog.println();
		mainLog.println("OUTPUT OPTIONS:");
		mainLog.println("-verbose (or -v) ............... Verbose mode: print out state lists and probability vectors");
		mainLog.println("-extraddinfo ................... Display extra info about some (MT)BDDs");
		mainLog.println("-extrareachinfo ................ Display extra info about progress of reachability");
		mainLog.println();
		mainLog.println("SPARSE/HYBRID/MTBDD OPTIONS:");
		mainLog.println("-nocompact ..................... Switch off \"compact\" sparse storage schemes");
		mainLog.println("-sbmax <n> ..................... Set memory limit (KB) (for hybrid engine) [default: 1024]");
		mainLog.println("-sbl <n> ....................... Set number of levels (for hybrid engine) [default: -1]");
		mainLog.println("-gsmax <n> (or sormax <n>) ..... Set memory limit (KB) for hybrid GS/SOR [default: 1024]");
		mainLog.println("-gsl <n> (or sorl <n>) ......... Set number of levels for hybrid GS/SOR [default: -1]");
		mainLog.println("-cuddmaxmem <n> ................ Set max memory for CUDD package (KB) [default: 200x1024]");
		mainLog.println("-cuddepsilon <x> ............... Set epsilon value for CUDD package [default: 1e-15]");
		mainLog.println();
		mainLog.println("SIMULATION OPTIONS:");
		mainLog.println("-sim ........................... Use the PRISM simulator to approximate results of model checking");
		mainLog.println("-simmethod <name> .............. Specify the method for approximate model checking (ci, aci, apmc, sprt)");
		mainLog.println("-simsamples <n> ................ Set the number of samples for the simulator (CI/ACI/APMC methods)");
		mainLog.println("-simconf <x> ................... Set the confidence parameter for the simulator (CI/ACI/APMC methods)");
		mainLog.println("-simwidth <x> .................. Set the interval width for the simulator (CI/ACI methods)");
		mainLog.println("-simapprox <x> ................. Set the approximation parameter for the simulator (APMC method)");
		mainLog.println("-simmanual ..................... Do not use the automated way of deciding whether the variance is null or not");
		mainLog.println("-simvar <n> .................... Set the minimum number of samples to know the variance is null or not");
		mainLog.println("-simmaxrwd <x> ................. Set the maximum reward -- useful to display the CI/ACI methods progress");
		mainLog.println("-simpathlen <n> ................ Set the maximum path length for the simulator");
	}

	// print version

	private void printVersion()
	{
		mainLog.println("PRISM version " + Prism.getVersion());
	}

	/**
	 * Print out a list of all PRISM language keywords.
	 */
	private void printListOfKeywords()
	{
		List<String> list = Prism.getListOfKeyords();
		mainLog.print("PRISM keywords:");
		for (String s : list) {
			mainLog.print(" " + s);
		}
		mainLog.println();
	}

	// report (non-fatal) error

	private void error(String s)
	{
		mainLog.println("\nError: " + s + ".");
	}

	// report error and exit cleanly

	private void errorAndExit(String s)
	{
		prism.closeDown(false);
		error(s);
		System.exit(1);
	}

	// exit cleanly (no error)

	private void exit()
	{
		prism.closeDown(true);
		System.exit(0);
	}

	// main method

	public static void main(String[] args)
	{
		new PrismCL().run(args);
	}
}

//------------------------------------------------------------------------------
