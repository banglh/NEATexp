package nogui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import jneat.Genome;
import expGame.GameEngine;
import jNeatCommon.EnvConstant;
import log.*;

public class Experiment {

	private Parameter a_parameter;
	private Session a_session;
	private Generation a_generation;
	protected HistoryLog logger;

	// for running experiments
	public static GameEngine ge;

	public static void main(String[] args) {
		// int[] s_vals = { 5, 7, 9, 11 };
		// double[] chi_vals = { 0.2, 0.4, 0.6, 0.8, 1.0 };
		// double[] d_vals = { 0, 0.1, 0.2, 0.3, 0.4 };
		// double[] p_vals = { 0, 0.1, 0.2, 0.3, 0.4 };
		int[] s_vals = { 5 };
		double[] chi_vals = { 0.8 };
		double[] d_vals = { 0.2 };
		double[] p_vals = { 0.2 };
		int n = 3;
		int method = EnvConstant.SPECIES_NEAT;

		// run experiment
		Experiment.runExperiments(method, s_vals, chi_vals, d_vals, p_vals, n);
		
		// clean temporary files
		File data2 = new File("..//nogui//data2");
		File data3 = new File("..//nogui//data3");
		for (File file: data2.listFiles()) file.delete();
		for (File file: data3.listFiles()) file.delete();

		// process results (get average performance for each setting)
		Experiment.processResults(s_vals, chi_vals, d_vals, p_vals, n);
	}

	// process results
	public static void processResults(int[] s_vals, double[] chi_vals, double[] d_vals, double[] p_vals, int n) {
		// results array
		double [][] results = new double [EnvConstant.NUMBER_OF_EPOCH][n];
		double [] lastResult = new double [EnvConstant.NUMBER_OF_EPOCH];

		// get method prefix
		String prefix = "";
		switch (EnvConstant.RUNNING_METHOD) {
			case EnvConstant.NEAT:
				prefix = "neat";
				break;
			case EnvConstant.NEAT_TODAI:
				prefix = "neatTodai";
				break;
			case EnvConstant.SPECIES_NEAT:
				prefix = "specieNeat";
				break;
			case EnvConstant.TOPOLOGY_NEAT:
				prefix = "topologyNeat";
				break;
		}

		for (int s : s_vals) {
			for (double chi : chi_vals) {
				for (double d : d_vals) {
					for (double p : p_vals) {
						try {
							// open each result file to get the results
							for (int i = 0; i < n; i++) {
								String fileN = String.format("%s//%s_s-%d_chi-%.1f_d-%.1f_p-%.1f_%d.csv", EnvConstant.RESULTS_DIR, prefix, s, chi, d, p, i);
								BufferedReader br = new BufferedReader(new FileReader(fileN));
							
								for (int gen = 0; gen < EnvConstant.NUMBER_OF_EPOCH; gen++) {
									double fit = Double.parseDouble(br.readLine());
									results[gen][i] = fit;
								}
								
								br.close();
							}
							
							// calculate the average results
							for (int gen = 0; gen < EnvConstant.NUMBER_OF_EPOCH; gen++) {
								// get average generation result
								lastResult[gen] = getAverage(results[gen]);
							}

							// open file to write last results
							String lastResultF = String.format("%s//%s_s-%d_chi-%.1f_d-%.1f_p-%.1f.csv", EnvConstant.RESULTS_DIR, prefix, s, chi, d, p);
							BufferedWriter bw = new BufferedWriter(new FileWriter(lastResultF));
							
							for (int gen = 0; gen < EnvConstant.NUMBER_OF_EPOCH; gen++) {
								bw.write(String.format("%f\n", lastResult[gen]));
							}
							
							bw.close();
							
							// delete temporary result files
							for (int i = 0; i < n; i++) {
								File f = new File(String.format("%s//neat_s-%d_chi-%.1f_d-%.1f_p-%.1f_%d.csv", EnvConstant.RESULTS_DIR, s, chi, d, p, i));
								if (!f.delete()) {
									System.out.println("Failed to delete temporary result file\n");
									return;
								}
							}
							
							System.out.printf("finished writing last results to file %s\n\n", lastResultF);
						} catch (Exception e) {
							System.out.println(e);
							return;
						}
					}
				}
			}
		}
	}
	
	// get average result
	private static double getAverage(double [] numArr) {
		double avg = 0.0;
		int length = numArr.length;
		
		for (double num : numArr) {
			avg += num;
		}
		
		return avg/length;
	}

	public Experiment() {
		a_parameter = new Parameter();
		a_session = new Session();
		a_generation = new Generation();
		ge = null;

		logger = new HistoryLog();

		a_parameter.setLog(logger);
		a_session.setLog(logger);
		a_generation.setLog(logger);
	}

	public static void runExperiments(int method, int[] s_vals, double[] chi_vals, double[] d_vals, double[] p_vals, int n) {
		// set method
		EnvConstant.RUNNING_METHOD = method;
		switch (EnvConstant.RUNNING_METHOD) {
			case EnvConstant.NEAT:
				EnvConstant.RUNNING_METHOD_NAME = EnvConstant.NEAT_NAME;
				break;
			case EnvConstant.NEAT_TODAI:
				EnvConstant.RUNNING_METHOD_NAME = EnvConstant.NEAT_TODAI_NAME;
				break;
			case EnvConstant.SPECIES_NEAT:
				EnvConstant.RUNNING_METHOD_NAME = EnvConstant.SPECIES_NEAT_NAME;
				break;
			case EnvConstant.TOPOLOGY_NEAT:
				EnvConstant.RUNNING_METHOD_NAME = EnvConstant.TOPOLOGY_NEAT_NAME;
				break;
		}

		// create new results folder
		String resultsDir = "..//results_" + System.currentTimeMillis();
		EnvConstant.RESULTS_DIR = resultsDir;
		File dir = new File(resultsDir);
		if (!dir.mkdirs()) {
			System.out.println("failed to create results folder");
			return;
		}

		// run experiment for each map
		for (int s : s_vals) {
			for (double chi : chi_vals) {
				for (double d : d_vals) {
					for (double p : p_vals) {
						System.out.printf("*** start %s experiments with s = %d, chi = %.1f, d = %.1f, p = %.1f ***\n", EnvConstant.RUNNING_METHOD_NAME, s, chi, d, p);
						for (int i = 0; i < n; i++) {
							// initialize experiment
							Experiment exp = new Experiment();

							// initialize game
							String mapDir = String.format("..//maps//s-%d_chi-%.1f_d-%.1f_p-%.1f//", s, chi, d, p);
							String mapFile = String.format("s-%d_chi-%.1f_d-%.1f_p-%.1f_%d.map", s, chi, d, p, i);
							mapFile = mapDir + mapFile;
							Experiment.ge = GameEngine.loadGame(mapFile);

							// update classes and constants
							game_inp.INPUT_NUM = Experiment.ge.getNf();
							game_out.OUTPUT_NUM = 1;
							game_fit.MAX_FITNESS = 1.0;
							EnvConstant.RUN_EXPERIMENTS = true;
							EnvConstant.EVALUATION_RUNS = 1000;
							EnvConstant.BEST_EVALUATION_RUNS = 10000;

							switch (EnvConstant.RUNNING_METHOD) {
								case EnvConstant.NEAT:
									EnvConstant.RESULTS_FILE = String.format("%s//neat_s-%d_chi-%.1f_d-%.1f_p-%.1f_%d.csv", resultsDir, s, chi, d, p, i);
									break;
								case EnvConstant.NEAT_TODAI:
									EnvConstant.RESULTS_FILE = String.format("%s//neatTodai_s-%d_chi-%.1f_d-%.1f_p-%.1f_%d.csv", resultsDir, s, chi, d, p, i);
									break;
								case EnvConstant.SPECIES_NEAT:
									EnvConstant.RESULTS_FILE = String.format("%s//specieNeat_s-%d_chi-%.1f_d-%.1f_p-%.1f_%d.csv", resultsDir, s, chi, d, p, i);
									break;
								case EnvConstant.TOPOLOGY_NEAT:
									EnvConstant.RESULTS_FILE = String.format("%s//topologyNeat_s-%d_chi-%.1f_d-%.1f_p-%.1f_%d.csv", resultsDir, s, chi, d, p, i);
									break;
							}

							// create new genome file
							String fn = EnvConstant.JNEAT_DIR + "\\data\\genome";
							Genome.createNewGenomeFile(fn, game_inp.INPUT_NUM, game_out.OUTPUT_NUM);

							// run experiment
							exp.run();
						}
					}
				}
			}
		}
	}

	public void reset() {
		a_parameter = new Parameter();
		a_session = new Session();
		a_generation = new Generation();
		ge = null;

		logger = new HistoryLog();

		a_parameter.setLog(logger);
		a_session.setLog(logger);
		a_generation.setLog(logger);
	}

	public void run() {
		// load parameters
		a_parameter.loadDefault();

		// load session setting
		a_session.loadSessDefault();

		// run neat
		a_generation.startExp();
	}

	// init game
	public void initGame(int s, double chi, double d, double p) {
		ge = new GameEngine(s, chi, d, p);
		ge.initGame();
	}
}
