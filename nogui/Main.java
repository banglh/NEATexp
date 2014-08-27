package nogui;

import jNeatCommon.EnvConstant;
import log.*;

public class Main {

	private Parameter a_parameter;
	private Session a_session;
	private Generation a_generation;
	private Grafi a_grafi;
	protected HistoryLog logger;
	
	public static void main(String [] args) {
		
	}
	
	public Main() {
		a_parameter = new Parameter(_f);
		a_session = new Session(_f);
		a_generation = new Generation(_f);
		a_grafi = new Grafi(_f);
		
		logger = new HistoryLog();

		a_parameter.setLog(logger);
		a_session.setLog(logger);
		a_generation.setLog(logger);
		a_grafi.setLog(logger);
	}
}
