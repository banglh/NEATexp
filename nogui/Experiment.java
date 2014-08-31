package nogui;

import java.io.BufferedWriter;
import java.io.File;
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
		double[] chi_vals = { 0.8, 1.0 };
		double[] d_vals = { 0, 0.1 };
		double[] p_vals = { 0, 0.1 };
		int n = 25;
		int method = EnvConstant.NEAT;

		// run experiment
		Experiment.runExperiments(method, s_vals, chi_vals, d_vals, p_vals, n);
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

		// run experiment for each map
		Experiment exp;
		for (int s : s_vals) {
			for (double chi : chi_vals) {
				for (double d : d_vals) {
					for (double p : p_vals) {
						for (int i = 0; i < n; i++) {
							// initialize experiment
							exp = new Experiment();

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

							// create new results folder
							String resultsDir = "results_" + System.currentTimeMillis();
							File dir = new File(resultsDir);
							if (!dir.mkdirs()) {
								System.out.println("failed to create results folder");
								return;
							}

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
		a_generation.start();
	}

	// init game
	public void initGame(int s, double chi, double d, double p) {
		ge = new GameEngine(s, chi, d, p);
		ge.initGame();
	}
}
