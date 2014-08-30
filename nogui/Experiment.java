package nogui;

import java.io.BufferedWriter;
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
		// initialize experiment
		Experiment exp = new Experiment();

		// inititalize game
		String mapFile = "..//maps//s-9_chi-0.2_d-0.4_p-0.4//s-9_chi-0.2_d-0.4_p-0.4_4.map";
		Experiment.ge = GameEngine.loadGame(mapFile);

		// update classes and constants
		game_inp.INPUT_NUM = ge.getNf();
		game_out.OUTPUT_NUM = 1;
		game_fit.MAX_FITNESS = 1.0;
		EnvConstant.RUN_EXPERIMENTS = true;
		EnvConstant.EVALUATION_RUNS = 1000;
		EnvConstant.BEST_EVALUATION_RUNS = 10000;
		EnvConstant.RESULTS_FILE = "results.csv";

		// create new genome file
		String fn = EnvConstant.JNEAT_DIR + "\\data\\genome";
		Genome.createNewGenomeFile(fn, game_inp.INPUT_NUM, game_out.OUTPUT_NUM);

		// run experiment
		exp.run();
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

	public void runExperiments(int method) {

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
