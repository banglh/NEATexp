package nogui;

import java.util.*;
import java.lang.*;

import jneat.*;
import jNeatCommon.*;

import log.*;

public class Parameter {

	vectTableModel modello;
	Neat netx;

	protected HistoryLog logger;

	/**
	 * pan1 constructor comment.
	 */
	public Parameter() {
		logger = new HistoryLog();
		modello = new vectTableModel(new Vector());

		EnvConstant.OP_SYSTEM = System.getProperty("os.name");
		EnvConstant.OS_VERSION = System.getProperty("os.version");
		EnvConstant.JNEAT_DIR = System.getProperty("user.dir");
		EnvConstant.OS_FILE_SEP = System.getProperty("file.separator");
	}

	/** bang **/
	/* functions for the corresponding buttons in GUI version */

	// TODO "Load default" function
	public void loadDefault() {
		String xret = null;
		String name;
		boolean rc = false;

		logger.sendToLog(" loading file parameter...");
		netx = new Neat();
		netx.initbase();
		name = EnvRoutine.getJneatParameter();
		rc = netx.readParam(name);

		if (rc)
			xret = new String("  OK");
		else
			xret = new String("  *ERROR*");

		modello.data.clear();
		modello.rows = -1;
		netx.getParam(modello);
		modello.fireTableDataChanged();
		logger.sendToLog(" ok file parameter " + name + " loaded!");
		logger.sendToStatus("READY");
	}

	// TODO "Write" function
	public void write() {
		String name;

		name = EnvRoutine.getJneatParameter();
		logger.sendToLog(" writing file parameter " + name + "...");
		netx.updateParam(modello);
		netx.writeParam(name);
		logger.sendToLog(" okay : file writed");
		logger.sendToStatus("READY");
	}

	// TODO "Load file" function
	public void loadFile(String fileName) {
		String xret = null;
		boolean rc = false;

		logger.sendToLog(" loading file parameter...");
		netx = new Neat();
		netx.initbase();

		if (fileName != null) {
			rc = netx.readParam(fileName);

			if (rc)
				xret = new String("  ok");
			else
				xret = new String("  *ERROR*");

			modello.data.clear();
			modello.rows = -1;
			netx.getParam(modello);
			modello.fireTableDataChanged();
			logger.sendToLog(" read of file parameter " + fileName + xret);
			logger.sendToStatus("READY");
		}
	}

	// TODO "Write file" function
	public void writeFile(String fileName) {
		if (fileName != null) {
			logger.sendToLog(" writing file parameter " + fileName + "...");
			netx.updateParam(modello);
			netx.writeParam(fileName);
			logger.sendToLog(" okay : file writed");
			logger.sendToStatus("READY");
		}
	}

	public void setLog(HistoryLog _log) {
		logger = _log;
	}
}