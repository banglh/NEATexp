package nogui;

//import java.awt.*;
//import java.awt.event.*;
//import java.awt.geom.*;
//import java.awt.image.*;
//import javax.swing.*;
//import javax.swing.event.*;
//import javax.swing.border.*;
import java.util.*;
import java.lang.*;
import jneat.*;
//import javax.swing.text.*;
import java.io.*;

import jNeatCommon.*;

import log.*;

public class Session {

	/* new definition start */

	/* new definition stop */

	protected HistoryLog logger;

	private volatile Thread lookupThread;

	String sessionSetting;
	String curr_fitness_class;
	String curr_input_data;
	String curr_output_data;

	final static String[] My_keyword = { ";", "activation", "data_from_file",
			"data_input", "data_target", "data_from_class",
			"class_compute_fitness", "start_from_genome", "genome_file",
			"start_from_random_population", "start_from_old_population",
			"population_file", "maximum_unit", "recursion",
			"probability_of_connection", "prefix_generation_file",
			"prefix_winner", "prefix_genome_random", "epoch", "public",
			"short", "float", "double", "int", "void", "class", "static", "if",
			"{", "}", "(", ")", "[", "]", "for", "new", "-", "+", "*", ">",
			"<=", ">=", "=", "<", ">", "/", "//", "%", "+=", "return" };

	final static String[] default_source = { "; \n",
			"; example of skeleton file  \n",
			";  is a XOR simulation with input from file\n",
			";data_from_file   Y\n", "data_from_class Y\n",
			"data_input       bin_inp\n", "data_target      xor_out\n",
			"class_compute_fitness xor_fit\n", "start_from_genome Y\n",
			"genome_file     genome\n", ";start_from_random_population Y\n",
			";start_from_old_population Y\n", "population_file primitive\n",
			";maximum_unit    5\n", ";recursion       N\n",
			";probability_of_connection 20\n", "epoch 10\n", "activation 0\n",
			";prefix_genome_random genome.rnd \n",
			"prefix_generation_file generation\n", "prefix_winner    winner\n" };

	final static String[] My_styles = { "normal", "italic", "bold",
			"bold-italic" };

	final static String[] initFitness = {
			"public class xor_fit { \n",
			" \n",
			"  public static double getMaxFitness() { return Math.pow (4.0, 2); } \n",
			" \n",
			"  public static double[]  computeFitness(int _sample, int _num_nodes, double _out[][], double _tgt[][]) \n",
			"  {",
			"     double d[] = new double[3]; \n",
			"     double errorsum = 0.0; \n",
			"     double fitness; \n",
			"     for ( int j = 0; j < _sample; j++) \n",
			"        { \n",
			"           errorsum  += ( double ) (Math.abs(_tgt[j][0] - _out[j][0])); \n",
			"        } \n",
			"     fitness = Math.pow ( ( 4.0 - errorsum ) , 2 ); \n", " \n",
			"     d[0] = fitness; \n", "     d[1] = errorsum; \n",
			"     d[2] = 0.0; \n", " \n",
			"     if ((_out[0][0] < 0.5) && (_out[1][0] >= 0.5) &&  \n",
			"            (_out[2][0] >= 0.5) && (_out[3][0] < 0.5)) \n",
			"        d[2] = 1.0; \n", " \n", "     return d; \n", "  } \n",
			"} \n" };

	final static String[] initDataClassInput = { "public class bin_inp {\n",
			" \n", "   public static int getNumSamples() { return 4; } \n",
			" \n", "   public static int getNumUnit()    { return 2; } \n",
			" \n", "   public static double getInput( int _plist[])\n",
			"   { \n", " \n", "      int _index = _plist[0]; \n",
			"      int _col   = _plist[1]; \n", " \n",
			"      if ( _index < 0 )  \n", "         _index = - _index; \n",
			" \n", "      if ( _index >= 4 ) \n",
			"         _index = _index % 4;  \n", " \n",
			"      double d[][] = new double[4][2];  \n", " \n",
			"      d[0][0] = 0; \n", "      d[0][1] = 0; \n", " \n",
			"      d[1][0] = 1; \n", "      d[1][1] = 0; \n", " \n",
			"      d[2][0] = 0; \n", "      d[2][1] = 1; \n", " \n",
			"      d[3][0] = 1; \n", "      d[3][1] = 1; \n", " \n",
			"      return d[_index][_col]; \n", " \n", "   } \n", " \n", "} \n" };

	final static String[] initDataClassOutput = { "public class xor_out {\n",
			" \n", "   public static int getNumUnit() { return 1; } \n", " \n",
			"   public static double getTarget( int _plist[]) \n", "   { \n",
			" \n", "      int _index = _plist[0];  \n",
			"      int _col   = _plist[1];  \n", "  \n",
			"      if ( _index < 0 ) \n", "         _index = - _index; \n",
			"  \n", "      if ( _index >= 4 ) \n",
			"         _index = _index % 4; \n", " \n",
			"      double d[] = new double[4]; \n", " \n",
			"      d[0] = 0; \n", "      d[1] = 1; \n", "      d[2] = 1; \n",
			"      d[3] = 0; \n", " \n", "      return d[_index]; \n", " \n",
			"   } \n", " \n", "} \n" };

	/**
	 * Session constructor comment.
	 */

	public Session() {
		logger = new HistoryLog();

		sessionSetting = null;
		curr_fitness_class = null;
		curr_input_data = null;
		curr_output_data = null;

		setSourceNew(default_source);

		EnvConstant.OP_SYSTEM = System.getProperty("os.name");
		EnvConstant.OS_VERSION = System.getProperty("os.version");
		EnvConstant.JNEAT_DIR = System.getProperty("user.dir");
		EnvConstant.OS_FILE_SEP = System.getProperty("file.separator");
	}

	// convert from String array to string
	public String convertToString(String[] stringArr) {
		String str = "";
		for (String string : stringArr) {
			str += string;
		}

		return str;
	}

	public static void main(String args[]) {
		Session ss = new Session();
		String str = ss.convertToString(initFitness);
		System.out.println(str);
	}

	public String[] convertToArray(String _text) {
		String s1 = _text;
		StringTokenizer riga;
		String elem;
		int sz;
		riga = new StringTokenizer(s1, "\n");
		sz = riga.countTokens();
		String[] source_new = new String[sz];

		for (int r = 0; r < sz; r++) {
			elem = (String) riga.nextToken();
			// System.out.print("\n conv.to.string --> elem["+r+"] --> "+elem);
			source_new[r] = new String(elem + "\n");
		}
		return source_new;
	}

	// "Load sess default" function
	public void loadSessDefault() {
		String nomef;

		logger.sendToStatus("wait....");
		EnvConstant.EDIT_STATUS = 0;
		nomef = EnvRoutine.getJneatSession();
		logger.sendToLog(" session: wait loading -> " + nomef);
		String xline;
		IOseq xFile;

		xFile = new IOseq(nomef);
		boolean rc = xFile.IOseqOpenR();
		if (rc) {
			StringBuffer sb1 = new StringBuffer("");
			try {
				xline = xFile.IOseqRead();

				while (xline != "EOF") {
					sb1.append(xline + "\n");
					xline = xFile.IOseqRead();
				}

				String[] source_new = convertToArray(sb1.toString());
				setSourceNew(source_new);
				logger.sendToLog(" ok file loaded");
				logger.sendToStatus("READY");
			}

			catch (Throwable e1) {
				logger.sendToStatus("READY");
				logger.sendToLog(" session: error during read " + e1);
			}

			xFile.IOseqCloseR();
		} else {
			logger.sendToStatus("READY");
			logger.sendToLog(" session: file not found");
		}
	}

	//  "Load sess file" function
	public void loadSessFile(String fileName) {
		EnvConstant.EDIT_STATUS = 0;

		if (fileName != null) {
			logger.sendToStatus("wait....");
			logger.sendToLog(" session: wait loading -> " + fileName);
			String xline;
			IOseq xFile;

			xFile = new IOseq(fileName);
			xFile.IOseqOpenR();
			StringBuffer sb1 = new StringBuffer("");
			try {
				xline = xFile.IOseqRead();
				while (xline != "EOF") {
					sb1.append(xline + "\n");
					xline = xFile.IOseqRead();
				}

				String[] source_new = convertToArray(sb1.toString());
				setSourceNew(source_new);
				logger.sendToLog(" ok file loaded");
				logger.sendToStatus("READY");
			} catch (Throwable e1) {
				logger.sendToStatus("READY");
				logger.sendToLog(" session: error during read " + e1);
			}

			xFile.IOseqCloseR();
		}
	}

	//  "Write sess" function
	public void writeSess() {
		String nomef;

		EnvConstant.EDIT_STATUS = 0;

		nomef = EnvRoutine.getJneatSession();
		logger.sendToStatus("wait....");
		logger.sendToLog(" session: wait writing -> " + nomef);
		IOseq xFile;
		xFile = new IOseq(nomef);
		xFile.IOseqOpenW(false);

		try {
			// String s1 = textPane1.getText();
			String s1 = sessionSetting;
			StringTokenizer riga;
			String elem;
			int sz;
			riga = new StringTokenizer(s1, "\n");
			sz = riga.countTokens();

			for (int r = 0; r < sz; r++) {
				elem = (String) riga.nextElement();
				String elem1 = new String(elem); // +"\n");
				xFile.IOseqWrite(elem);
			}

			logger.sendToLog(" ok file writed");

		} catch (Throwable e1) {
			logger.sendToStatus("READY");
			logger.sendToLog(" session: error during write " + e1);
		}

		xFile.IOseqCloseW();
		logger.sendToStatus("READY");

	}

	//  "Write sess file" function
	public void writeSessFile(String fileName) {
		EnvConstant.EDIT_STATUS = 0;

		if (fileName != null) {
			logger.sendToStatus("wait....");
			logger.sendToLog(" session: wait writing -> " + fileName);
			//
			// write to file genome in native format (for re-read)
			//
			IOseq xFile;
			xFile = new IOseq(fileName);
			xFile.IOseqOpenW(false);

			try {
				// String s1 = textPane1.getText();
				String s1 = sessionSetting;
				StringTokenizer riga;
				String elem;
				int sz;
				riga = new StringTokenizer(s1, "\n");
				sz = riga.countTokens();

				for (int r = 0; r < sz; r++) {
					elem = (String) riga.nextElement();
					String elem1 = new String(elem); // +"\n");
					xFile.IOseqWrite(elem);
				}
				logger.sendToLog(" ok file writed");
			}

			catch (Throwable e1) {
				logger.sendToStatus("READY");
				logger.sendToLog(" session: error during write " + e1);
			}

			xFile.IOseqCloseW();
			logger.sendToStatus("READY");
		}
	}

	//  "Load class fitness" function
	public void loadClassFitness() {
		logger.sendToStatus("wait...");
		EnvConstant.EDIT_STATUS = EnvConstant.EDIT_CLASS_FIT;
		if (curr_fitness_class != null) {
			load_from_disk_Class(curr_fitness_class, "fitness");
		} else
			logger.sendToLog(" session: *warning* before load fitness , load the sesssion !");

		logger.sendToStatus("READY");
	}

	//  "Load class data input" function
	public void loadClassDataInput() {
		logger.sendToStatus("wait...");
		EnvConstant.EDIT_STATUS = EnvConstant.EDIT_CLASS_INP;
		if (curr_input_data != null) {
			load_from_disk_Class(curr_input_data, "data");
		} else
			logger.sendToLog(" session: *warning* before load data-in , load the sesssion !");

		logger.sendToStatus("READY");
	}

	//  "Load class data target" function
	public void loadClassDataTarget() {
		logger.sendToStatus("wait...");
		EnvConstant.EDIT_STATUS = EnvConstant.EDIT_CLASS_OUT;
		if (curr_output_data != null) {
			load_from_disk_Class(curr_output_data, "data");
		} else
			logger.sendToLog(" session: *warning* before load data-out , load the sesssion !");

		logger.sendToStatus("READY");
	}

	//  "Set session file skeleton" function
	public void setSessionFileSkeleton() {
		logger.sendToStatus("wait...");
		EnvConstant.EDIT_STATUS = 0;
		// textPane1.setText("");
		setSourceNew(default_source);
		logger.sendToLog(" session: set to default skeleton for session");
		logger.sendToStatus("READY");
	}

	//  "Set fitness class skeleton" function
	public void setFitnessClassSkeleton() {
		logger.sendToStatus("wait...");
		EnvConstant.EDIT_STATUS = EnvConstant.EDIT_CLASS_FIT;
		// textPane1.setText("");
		setSourceNew(initFitness);
		logger.sendToLog(" session: set to default skeleton for fitness");
		logger.sendToStatus("READY");
	}

	//  "Set data_inp class skeleton" function
	public void setDataInputClassSkeleton() {
		logger.sendToStatus("wait...");
		EnvConstant.EDIT_STATUS = EnvConstant.EDIT_CLASS_INP;
		// textPane1.setText("");
		setSourceNew(initDataClassInput);
		logger.sendToLog(" session: set to default skeleton for  class/dataset generate input");
		logger.sendToStatus("READY");
	}

	//  "Set data_tgt class skeleton" function
	public void setDataTargetClassSkeleton() {
		logger.sendToStatus("wait...");
		EnvConstant.EDIT_STATUS = EnvConstant.EDIT_CLASS_OUT;
		// textPane1.setText("");
		setSourceNew(initDataClassOutput);
		logger.sendToLog(" session: set to default skeleton for  class/dataset generate output");
		logger.sendToStatus("READY");
	}

	//  "check keyword" function
	public void checkKeyword() {
		logger.sendToStatus("wait...");
		String[] source_new = convertToArray(sessionSetting);
		// String[] source_new = convertToArray(textPane1.getText());
		// textPane1.setText("");
		setSourceNew(source_new);
		logger.sendToStatus("READY");
	}

	//  "COMPILE" function
	public void compile() {
		if (EnvConstant.EDIT_STATUS == EnvConstant.EDIT_CLASS_FIT) {
			if (curr_fitness_class != null) {
				EnvConstant.CURRENT_CLASS = curr_fitness_class;
				Async_generationClass();
			}
		} else if (EnvConstant.TYPE_OF_SIMULATION == EnvConstant.SIMULATION_FROM_CLASS) {

			if (EnvConstant.EDIT_STATUS == EnvConstant.EDIT_CLASS_INP) {
				if (curr_input_data != null) {
					EnvConstant.CURRENT_CLASS = curr_input_data;
					Async_generationClass();
				}
			} else if (EnvConstant.EDIT_STATUS == EnvConstant.EDIT_CLASS_OUT) {
				if (curr_output_data != null) {
					EnvConstant.CURRENT_CLASS = curr_output_data;
					Async_generationClass();
				}
			}
		} else if (EnvConstant.TYPE_OF_SIMULATION == EnvConstant.SIMULATION_FROM_FILE) {
			if (EnvConstant.EDIT_STATUS == EnvConstant.EDIT_CLASS_INP) {
				if (curr_input_data != null) {
					EnvConstant.CURRENT_FILE = curr_input_data;
					Async_generationFile();
				}
			} else if (EnvConstant.EDIT_STATUS == EnvConstant.EDIT_CLASS_OUT) {
				if (curr_output_data != null) {
					EnvConstant.CURRENT_FILE = curr_output_data;
					Async_generationFile();
				}
			}
		}
	}

	public void setLog(HistoryLog _log) {
		logger = _log;
	}

	public void createClass(String _filename, String[] sourcecode) {
		try {
			FileWriter aWriter = new FileWriter(_filename, false);

			for (int r = 0; r < sourcecode.length; r++)
				aWriter.write(sourcecode[r]);

			aWriter.flush();
			aWriter.close();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean compileClass(String _filename) {

		String[] source = { new String(_filename) };
		PrintStream ps = System.err;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setErr(new PrintStream(baos));

		/*
		 * 
		 * // jdk 1.1.8 //
		 * 
		 * new sun.tools.javac.Main(baos, source[0]).compile(source);
		 * System.setErr(ps); if (baos.toString().indexOf("error") == -1) return
		 * true; else {
		 * 
		 * try {
		 * 
		 * logger.sendToLog(" session: *warning* error during compilation : ");
		 * logger.sendToLog(" session: "+baos.toString());
		 * 
		 * } catch (Throwable e1) { System.err.println(e1 +
		 * " session: error in try-compile  "+e1); } return false; }
		 */

		// jdk 1.3.1_01
		//

		com.sun.tools.javac.Main m1 = new com.sun.tools.javac.Main();
		m1.compile(source);
		System.setErr(ps);

		Date xdata = new Date();
		if (baos.toString().indexOf("error") == -1)
			return true;
		else {

			try {
				logger.sendToLog(" session: *warning* error during compilation : ");
				logger.sendToLog(" session: " + baos.toString());

			}

			catch (Throwable e1) {
				System.err
						.println(e1 + " session: error in try-compile  " + e1);
			}
			return false;
		}

	}

	public void Async_generationClass() {
		Runnable lookupRun = new Runnable() {
			public void run() {
				generationClass();
			}
		};
		lookupThread = new Thread(lookupRun, " looktest");
		lookupThread.start();
	}

	public void generationClass() {

		String _classname = EnvConstant.CURRENT_CLASS;
		String nomef = null;

		logger.sendToStatus("wait....");
		try {

			logger.sendToLog(" session: start compile ->" + _classname
					+ " in dir ->" + EnvConstant.JNEAT_DIR);
			// legge corrente nome source della classe da creare
			//
			nomef = EnvRoutine.getJneatFile(_classname + ".java");
			// converte da stringa unica a vettore di stringhe
			String[] source_new = convertToArray(sessionSetting);
			// String[] source_new = convertToArray(textPane1.getText());
			logger.sendToLog(" session: creation source " + _classname
					+ ".java");
			// genera il source.java
			//
			createClass(nomef, source_new);
			logger.sendToLog(" session: terminate creation source");
			logger.sendToLog(" session: creation class " + _classname
					+ ".class");
			// genera il file .class
			//
			compileClass(nomef);
			logger.sendToLog(" session: terminate creation class " + _classname
					+ ".class");
			// riaggiorna il pannello con quello che ha appena scritto
			// textPane1.setText("");
			setSourceNew(source_new);
			logger.sendToStatus("READY");

		}

		catch (Throwable e1) {
			logger.sendToLog(" session: error during compile fitness " + e1);
		}
		logger.sendToStatus("READY");

	}

	public void setSourceNew(String[] _source) {
		StringTokenizer riga;
		String elem;
		int sz;
		String prev_word;
		boolean fnd;
		// Document doc = textPane1.getDocument();

		sessionSetting = convertToString(_source);
		try {
			for (int i = 0; i < _source.length; i++) {
				// search name for fitness class;
				// search i/o class or files for input/target signal
				//

				int b1[] = new int[_source[i].length()];
				for (int j = 0; j < b1.length; j++)
					b1[j] = 0;

				String zriga = _source[i];
				int pos = 0;

				for (int k = 0; k < My_keyword.length; k++) {
					String ckey = My_keyword[k];
					pos = zriga.indexOf(ckey, 0);
					if (pos != -1) {
						for (int k1 = 0; k1 < ckey.length(); k1++)
							b1[pos + k1] = 1;
						boolean done = false;
						int offset = pos + ckey.length();
						while (!done) {
							pos = zriga.indexOf(ckey, offset);
							if (pos != -1) {
								for (int k1 = 0; k1 < ckey.length(); k1++)
									b1[pos + k1] = 1;
								offset = pos + ckey.length();
							} else
								done = true;
						}
					}
				}

				int n1 = 0;
				int n2 = 0;
				int v1 = 0;
				int v2 = 0;
				int k2 = 0;

				boolean comment = false;
				for (int k1 = 0; k1 < b1.length; k1++) {
					v1 = b1[k1];
					if (v1 == 1) {
						if (zriga.substring(k1, k1 + 1).equals(";")) {
							comment = true;
							break;
						} else
							comment = false;
						break;
					}
				}

				if (comment) {
					// doc.insertString(doc.getLength(), zriga,
					// textPane1.getStyle(My_styles[1]));
				}

				else {

					// cerca fino a che non trova n1 != n2;
					// int lun = 0;
					boolean again = true;
					for (int k1 = 0; k1 < b1.length; k1++) {
						v1 = b1[n1];
						n2 = n1;
						again = false;
						for (k2 = n1 + 1; k2 < b1.length; k2++) {
							v2 = b1[k2];
							if (v2 != v1) {
								again = true;
								break;
							}
							n2 = k2;
						}

						elem = zriga.substring(n1, n2 + 1);

						if (v1 == 0) {
							// doc.insertString(doc.getLength(), elem,
							// textPane1.getStyle(My_styles[0]));
						} else {
							// doc.insertString(doc.getLength(), elem,
							// textPane1.getStyle(My_styles[2]));
						}
						// System.out.print("\n n1="+n1+" n2="+n2+" found elem ->"+elem+"<- size("+elem.length()+")");
						k1 = k2;
						n1 = k2;
					}

					if (again) {
						elem = zriga.substring(b1.length - 1, b1.length);
						if (b1[b1.length - 1] == 0) {
							// doc.insertString(doc.getLength(), elem,
							// textPane1.getStyle(My_styles[0]));
						} else {
							// doc.insertString(doc.getLength(), elem,
							// textPane1.getStyle(My_styles[2]));
						}
						// System.out.print("\n **WW* found elem ->"+elem+"<- size("+elem.length()+")");
					}

					riga = new StringTokenizer(zriga);

					sz = riga.countTokens();
					prev_word = null;
					for (int r = 0; r < sz; r++) {
						elem = riga.nextToken();
						fnd = false;
						for (int k = 0; k < My_keyword.length; k++) {
							if (My_keyword[k].equalsIgnoreCase(elem)) {
								fnd = true;
								break;
							}
						}

						if ((prev_word != null)
								&& (prev_word
										.equalsIgnoreCase("data_from_file"))) {
							if ((!comment) && elem.equalsIgnoreCase("Y")) {
								EnvConstant.TYPE_OF_SIMULATION = EnvConstant.SIMULATION_FROM_FILE;
							}
						}

						if ((prev_word != null)
								&& (prev_word
										.equalsIgnoreCase("data_from_class"))) {
							if ((!comment) && elem.equalsIgnoreCase("Y")) {
								EnvConstant.TYPE_OF_SIMULATION = EnvConstant.SIMULATION_FROM_CLASS;
							}
						}

						if ((prev_word != null)
								&& (prev_word
										.equalsIgnoreCase("class_compute_fitness"))) {
							curr_fitness_class = new String(elem);
						}

						if ((prev_word != null)
								&& (prev_word.equalsIgnoreCase("data_input"))) {
							curr_input_data = new String(elem);
						}

						if ((prev_word != null)
								&& (prev_word.equalsIgnoreCase("data_target"))) {
							curr_output_data = new String(elem);
						}
						prev_word = elem;
					}
				}
			}
			// textPane1.setCaretPosition(1);
		} catch (Exception e1) {
			logger.sendToStatus(" session: Couldn't insert initial text.:" + e1);
		}
	}

	public void Async_generationFile() {
		Runnable lookupRun = new Runnable() {
			public void run() {
				generationFile();
			}
		};
		lookupThread = new Thread(lookupRun, " looktest");
		lookupThread.start();
	}

	public void generationFile() {
		String _fname = EnvConstant.CURRENT_FILE;

		logger.sendToStatus("wait....");
		logger.sendToLog(" session: start write file "
				+ EnvRoutine.getJneatFile(_fname));
		IOseq xFile;
		xFile = new IOseq(EnvRoutine.getJneatFile(_fname));
		xFile.IOseqOpenW(false);

		try {
			String s1 = sessionSetting;
			// String s1 = textPane1.getText();
			StringTokenizer riga;
			String elem;
			int sz;
			riga = new StringTokenizer(s1, "\n");
			sz = riga.countTokens();

			for (int r = 0; r < sz; r++) {
				elem = (String) riga.nextElement();
				String elem1 = new String(elem); // +"\n");
				xFile.IOseqWrite(elem);
			}

			logger.sendToLog(" ok file writed");
		}

		catch (Throwable e1) {
			logger.sendToStatus("READY");
			logger.sendToLog(" session: error during write " + e1);
		}

		xFile.IOseqCloseW();
		logger.sendToStatus("READY");

	}

	public void load_from_disk_Class(String _filename, String _type) {
		String nomef = null;

		if (_type.equalsIgnoreCase("fitness"))
			nomef = EnvRoutine.getJneatFile(_filename + ".java");

		else {
			if (EnvConstant.TYPE_OF_SIMULATION == EnvConstant.SIMULATION_FROM_CLASS)
				nomef = EnvRoutine.getJneatFile(_filename + ".java");
			else
				nomef = EnvRoutine.getJneatFile(_filename);
		}

		StringTokenizer st;
		String xline;
		IOseq xFile;

		xFile = new IOseq(nomef);
		boolean exist = xFile.IOseqOpenR();

		if (exist) {

			StringBuffer sb1 = new StringBuffer("");
			try {

				logger.sendToStatus(" session: wait....");
				logger.sendToLog("  session: wait loading " + nomef + "...");
				xline = xFile.IOseqRead();

				while (xline != "EOF") {
					sb1.append(xline + "\n");
					xline = xFile.IOseqRead();
				}

				// textPane1.setText("");
				String[] source_new = convertToArray(sb1.toString());
				setSourceNew(source_new);

				logger.sendToLog(" session: wait loaded " + nomef);
				logger.sendToStatus("READY");
			}

			catch (Throwable e1) {
				logger.sendToLog(" session: error during read " + nomef + " "
						+ e1);
			}

			xFile.IOseqCloseR();
			logger.sendToStatus("READY");

		} // exist cycle

		else {

			try {
				logger.sendToLog("  session: warning : file  " + nomef
						+ " not exist!");
			}

			catch (Throwable e2) {
				System.err.println(e2
						+ " session: error during text processing " + e2);
			}

			logger.sendToStatus("READY");

		}

	}
}