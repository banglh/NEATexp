package nogui;

import jNeatCommon.EnvConstant;
import log.*;

public class Main {

	private Parameter a_parameter;
	private Session a_session;
	private Generation a_generation;
	protected HistoryLog logger;
	
	public static void main(String [] args) {
		Main engine = new Main();
		engine.run();
	}
	
	public Main() {
		a_parameter = new Parameter();
		a_session = new Session();
		a_generation = new Generation();
		
		logger = new HistoryLog();

		a_parameter.setLog(logger);
		a_session.setLog(logger);
		a_generation.setLog(logger);
	}
	
	public void reset() {
		a_parameter = new Parameter();
		a_session = new Session();
		a_generation = new Generation();
		
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
}
