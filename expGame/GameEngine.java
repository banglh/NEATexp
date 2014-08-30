package expGame;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class GameEngine {
	// constants
	public static final int NORTH = 0; // north direction
	public static final int EAST = 1; // east direction

	// game parameters
	private int s; // board size
	private double chi, d, p; // chi, d, p params
	private int nf; // number of radial basis functions
	private double sdRBF; // standard deviation of radial basis functions
	private Random rand;

	// game data
	private double[][] map; // game board
	private int[][] RBFsPos; // positions of radial basis function centers
	private Map<Position, RBFs> RBFsTable;
	private Position agentInitPos; // initial agent's position
	private double bestSolutionReward;
	private double worstSolutionReward;

	// game status
	private Position agentCurPos; // current position of agent
	private double totalReward; // current total reward
	private boolean gameEnded = false; // game ended or not
	private boolean initialized = false; // game is initialized or not

	// constructors
	public GameEngine(int s, double chi, double d, double p) {
		// set params
		this.s = s;
		this.chi = chi;
		this.d = d;
		this.p = p;
		this.sdRBF = 1.0;
		this.map = new double[s][s];
		this.nf = (int) Math.floor(chi * (s - 1) * (s - 1));
		this.RBFsPos = new int[this.nf][2];
		this.RBFsTable = new HashMap<Position, RBFs>();
		this.agentInitPos = new Position();
		this.agentCurPos = new Position();
		this.bestSolutionReward = Double.MAX_VALUE;
		this.worstSolutionReward = 0.0;
		rand = new Random(System.currentTimeMillis());
	}

	// initialize game
	public void initGame() {
		// initialize map
		initMap();

		// initialize RBF centers
		initRBFs();

		// initialize RBFs table
		initRBFsTable();

		// initialize agent position
		initAgentPos();

		// calculate the best and the worst solutions
		bestSolutionReward = calculateBestSolution(agentInitPos.getCoordinate());
		worstSolutionReward = calculateWorstSolution(agentInitPos
				.getCoordinate());

		agentCurPos.setCoordinate(agentInitPos.getCoordinate());
		totalReward = 0.0;
		initialized = true;
		gameEnded = false;
	}

	// reset game
	public void resetGame() {
		// reset agent's location
		agentCurPos.setCoordinate(agentInitPos.getCoordinate());

		// reset total reward
		totalReward = 0.0;

		// other information
		gameEnded = false;
	}

	// move function
	public int move(int expectedDirection) {
		int realDirection = -1;

		// check if the agent is in a terminal state
		if (isTerminalState(agentCurPos.getCoordinate()))
			return realDirection;

		// check if expected direction is valid
		if (expectedDirection != NORTH && expectedDirection != EAST) {
			return realDirection;
		}

		// get real moving direction
		double prob = rand.nextDouble();
		if (prob < 1 - p)
			realDirection = expectedDirection;
		else
			realDirection = (NORTH + EAST) - expectedDirection;

		// moving
		int[] curPos = agentCurPos.getCoordinate();
		switch (realDirection) {
		case NORTH:
			// update current agent's location
			curPos[0] -= 1;
			break;
		case EAST:
			// update current agent's location
			curPos[1] += 1;
			break;
		}
		agentCurPos.setCoordinate(curPos);

		// update total reward
		totalReward += map[curPos[0]][curPos[1]];

		// check and update game state
		if (isTerminalState(agentCurPos.getCoordinate()))
			gameEnded = true;

		return realDirection;
	}

	// run a controller n times to evaluate it's performance
	// return the normalized average reward of n runs
	public double evaluate(Controller controller, int n) {
		double avgRewardNorm = 0.0;

		for (int run = 0; run < n; run++) {
			// reset game
			resetGame();

			// run game
			while (!gameEnded) {
				// get RBF values
				RBFs curRBFs = getCurrentRBFs();
				double[] rbfs = curRBFs.getRBFs();

				// get expected direction from controller
				int expectedDirection = -1;
				if (rbfs != null)
					expectedDirection = controller.getDirection(rbfs);
				else {
					System.out.println("rbfs null");
					System.exit(1);
				}

				// move
				move(expectedDirection);
			}

			avgRewardNorm += totalReward;
		}

		avgRewardNorm /= n;
		avgRewardNorm = (avgRewardNorm - worstSolutionReward)
				/ (bestSolutionReward - worstSolutionReward);

		return avgRewardNorm;
	}

	// function to print game settings to file
	/**
	 * format ** + s, chi, d, p + map + RBFsPos + agentInitPos (coordinate)
	 */
	public boolean saveGame(String fileName) {
		boolean success = true;

		try {
			// create file
			FileWriter f = new FileWriter(fileName);
			BufferedWriter bw = new BufferedWriter(f);

			// write s, chi, d, p, nf, sdRBF
			String str = String.format("%d,%f,%f,%f\n", s, chi, d, p);
			bw.write(str);

			// write map
			for (int row = 0; row < s; row++) {
				str = "";
				for (int col = 0; col < s; col++) {
					if (col == s - 1)
						str += String.format("%f\n", map[row][col]);
					else
						str += String.format("%f,", map[row][col]);
				}
				bw.write(str);
			}

			// write RBFsPos
			for (int i = 0; i < nf; i++) {
				str = String.format("%d,%d\n", RBFsPos[i][0], RBFsPos[i][1]);
				bw.write(str);
			}

			// write agentInitPos
			str = String.format("%d,%d\n", agentInitPos.getCoordinate()[0],
					agentInitPos.getCoordinate()[1]);
			bw.write(str);

			// close file
			bw.close();
		} catch (Exception e) {
			System.out.println(e);
			success = false;
			return success;
		}

		return success;
	}

	// TODO function to load game settings from file
	public static GameEngine loadGame(String fileName) {
		GameEngine ge;
		int s;
		double chi, d, p;

		try {
			// open file
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);

			// read s, chi, d, p
			String str = br.readLine();
			String[] strArr = str.split(",");
			s = Integer.parseInt(strArr[0]);
			chi = Double.parseDouble(strArr[1]);
			d = Double.parseDouble(strArr[2]);
			p = Double.parseDouble(strArr[3]);
			ge = new GameEngine(s, chi, d, p);

			// read map
			for (int row = 0; row < ge.get_s(); row++) {
				str = br.readLine();
				strArr = str.split(",");
				for (int col = 0; col < ge.get_s(); col++) {
					// set value for map[row][col]
					if (!ge.setMapValue(row, col,
							Double.parseDouble(strArr[col]))) {
						throw new Exception("invalid reward for map grid");
					}
				}
			}

			// read RBFsPos
			for (int i = 0; i < ge.getNf(); i++) {
				str = br.readLine();
				strArr = str.split(",");
				if (!ge.setRBFPos(i, Integer.parseInt(strArr[0]),
						Integer.parseInt(strArr[1])))
					throw new Exception(
							"invalid row/col valud for RBF position");
			}

			// read initial agent location
			str = br.readLine();
			strArr = str.split(",");
			if (!ge.setAgentInitPos(Integer.parseInt(strArr[0]),
					Integer.parseInt(strArr[1])))
				throw new Exception("invalid initial agent position");

			// calculate RBFsTable
			ge.initRBFsTable();

			// calculate the best and the worst solution
			ge.setMaxMinRewards();
			
			ge.setInitialized();

			// close file
			br.close();

		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
		return ge;
	}

	// calculate the best and the worst solutions of current settings
	public void setMaxMinRewards() {
		bestSolutionReward = calculateBestSolution(agentInitPos.getCoordinate());
		worstSolutionReward = calculateWorstSolution(agentInitPos.getCoordinate());
	}

	// set initial agent position
	public boolean setAgentInitPos(int row, int col) {
		// check if row and col are valid
		if (row < 1 || row > s - 1)
			return false;
		if (col < 0 || col > s - 2)
			return false;

		int[] pos = { row, col };
		agentInitPos.setCoordinate(pos);
		agentCurPos.setCoordinate(pos);
		return true;
	}

	// set BRF center
	public boolean setRBFPos(int id, int row, int col) {
		// check if id is valid
		if (id < 0 || id > nf - 1)
			return false;

		// check if row and col are valid
		if (row < 1 || row > s - 1)
			return false;
		if (col < 0 || col > s - 2)
			return false;

		RBFsPos[id][0] = row;
		RBFsPos[id][1] = col;
		return true;
	}

	// set reward for a particular grid on the game map
	public boolean setMapValue(int row, int col, double val) {
		// check if row and col are valid
		if (row < 0 || row >= s)
			return false;
		if (col < 0 || col >= s)
			return false;

		// check if val is valid
		if (val < 0.0 || val > 1.0)
			return false;

		map[row][col] = val;
		return true;
	}

	// set initialized variable
	public void setInitialized() {
		initialized = true;
	}

	private RBFs getCurrentRBFs() {
		return RBFsTable.get(agentCurPos);
	}

	// calculate the best solution for the current game
	private double calculateBestSolution(int[] agentPos) {
		// recursively calculate best reward
		double bestReward = 0.0;

		if (isTerminalState(agentPos)) {
			bestReward = 0.0;
		} else {
			// agent's position after moving North/East
			int[] northPos = { agentPos[0] - 1, agentPos[1] };
			int[] eastPos = { agentPos[0], agentPos[1] + 1 };

			// received reward after moving North/East
			double northReward = map[northPos[0]][northPos[1]];
			double eastReward = map[eastPos[0]][eastPos[1]];

			// best long term reward of moving North/East
			double bestNorthReward = northReward
					+ calculateBestSolution(northPos);
			double bestEastReward = eastReward + calculateBestSolution(eastPos);

			// get best reward
			bestReward = Math.max(bestNorthReward, bestEastReward);
		}

		return bestReward;
	}

	// calculate the worst solution for the current game
	private double calculateWorstSolution(int[] agentPos) {
		// recursively calculate worst reward
		double worstReward = 0.0;

		if (isTerminalState(agentPos)) {
			worstReward = 0.0;
		} else {
			// agent's position after moving North/East
			int[] northPos = { agentPos[0] - 1, agentPos[1] };
			int[] eastPos = { agentPos[0], agentPos[1] + 1 };

			// received reward after moving North/East
			double northReward = map[northPos[0]][northPos[1]];
			double eastReward = map[eastPos[0]][eastPos[1]];

			// worst long term reward of moving North/East
			double worstNorthReward = northReward
					+ calculateWorstSolution(northPos);
			double worstEastReward = eastReward
					+ calculateWorstSolution(eastPos);

			// get worst reward
			worstReward = Math.min(worstNorthReward, worstEastReward);
		}

		return worstReward;
	}

	// check if current state if a terminal state
	private boolean isTerminalState(int[] location) {
		boolean isTerminal = false;
		if (location[0] == 0 || location[1] == s - 1)
			isTerminal = true;
		return isTerminal;
	}

	// initialize map
	private void initMap() {
		for (int row = 0; row < s; row++) {
			for (int col = 0; col < s; col++) {
				map[row][col] = rand.nextDouble();
			}
		}
	}

	// RBF centers init
	private void initRBFs() {
		int maxNum = (s - 1) * (s - 1);
		int[] positions = new int[maxNum];
		int[] rbfs = new int[nf];

		// init positions
		for (int i = 0; i < maxNum; i++) {
			positions[i] = i;
		}

		// randomly choose nf positions
		for (int i = 0; i < nf; i++) {
			// choose a random position from positions[i] ~ positions[maxNum-1]
			int newPos = rand.nextInt(maxNum - i) + i;

			// exchange positions[i] with positions[newPos]
			int tmp = positions[i];
			positions[i] = positions[newPos];
			positions[newPos] = tmp;

			rbfs[i] = positions[i];
		}

		// sorting rbfs
		Arrays.sort(rbfs);

		// convert positions
		for (int i = 0; i < nf; i++) {
			this.RBFsPos[i][0] = rbfs[i] / (s - 1) + 1;
			this.RBFsPos[i][1] = rbfs[i] % (s - 1);
		}
	}

	// initialize agent's position
	private void initAgentPos() {
		int row = rand.nextInt(s - 1) + 1; // from 1 to s-1
		int col = rand.nextInt(s - 1); // from 0 to s-2
		int[] coor = { row, col };
		agentInitPos.setCoordinate(coor);
	}

	// function to calculate RBF values for every locations in advance
	public void initRBFsTable() {
		// produce a dictionary <location-rbfs>
		for (int row = 0; row < s; row++) {
			for (int col = 0; col < s; col++) {
				int[] location = { row, col };
				double[] rbfs = getRBFValues(location);

				Position pos = new Position(location);
				RBFs rbfsObj = new RBFs(rbfs);

				RBFsTable.put(pos, rbfsObj);
			}
		}
	}

	// get RBF values for a particular location
	private double[] getRBFValues(int[] location) {
		double[] rbfVals = new double[nf];

		for (int i = 0; i < nf; i++) {
			rbfVals[i] = calculateRBF(RBFsPos[i], location);
		}

		return rbfVals;
	}

	// RBF
	private double calculateRBF(int[] rbfCenterPos, int[] location) {
		double rbf = 0.0;

		// Gaussian rbf
		double peak = map[rbfCenterPos[0]][rbfCenterPos[1]];
		double distanceSquare = Math.pow(rbfCenterPos[0] - location[0], 2)
				+ Math.pow(rbfCenterPos[1] - location[1], 2);
		rbf = peak
				* Math.exp(-0.5 * distanceSquare / (this.sdRBF * this.sdRBF));

		return rbf;
	}

	// get functions
	public int get_s() {
		return s;
	}

	public double get_chi() {
		return chi;
	}

	public double get_d() {
		return d;
	}

	public double get_p() {
		return p;
	}

	public int getNf() {
		return nf;
	}

	public double[][] getMap() {
		return map.clone();
	}

	public int[][] getRBFsPos() {
		return RBFsPos.clone();
	}

	public Position getAgentInitPos() {
		return agentInitPos;
	}

	public Position getAgentCurPos() {
		return agentCurPos;
	}

	public double getTotalReward() {
		return totalReward;
	}

	public boolean isGameEnded() {
		return gameEnded;
	}

	public boolean isGameInitialized() {
		return initialized;
	}

	// print game info
	public void printGameInfo() {
		System.out.println("******* GAME INFORMATION *******");
		// params
		System.out.printf("+ s = %d\n", s);
		System.out.printf("+ chi = %f\n", chi);
		System.out.printf("+ d = %f\n", d);
		System.out.printf("+ p = %f\n", p);
		System.out.printf("+ nf = %d\n", nf);
		System.out.printf("+ RBFs sd = %f\n", sdRBF);
		System.out.printf("+ total reward = %f\n", totalReward);
		System.out.printf("+ initialized = %b\n", initialized);
		System.out.printf("+ game ended = %b\n\n", gameEnded);

		// map
		System.out.println("+ Game Map");
		for (int row = 0; row < s; row++) {
			for (int col = 0; col < s; col++) {
				System.out.printf("%-6.3f", map[row][col]);
			}
			System.out.println("");
		}

		// RBFs pos
		System.out.println("+ RBFs center positions");
		for (int i = 0; i < nf; i++) {
			System.out.printf("[%d][%d]\n", RBFsPos[i][0], RBFsPos[i][1]);
		}

		// Agent's initial position
		System.out.println("+ Agent's initial location");
		System.out.printf("[%d][%d]\n", agentInitPos.getCoordinate()[0],
				agentInitPos.getCoordinate()[1]);

		// Agent's current position
		System.out.println("+ Agent's current location");
		System.out.printf("[%d][%d]\n", agentCurPos.getCoordinate()[0],
				agentCurPos.getCoordinate()[1]);

		// the best and the worst reward
		System.out.printf("Best Reward: %f\n", bestSolutionReward);
		System.out.printf("Worst reward: %f\n", worstSolutionReward);
	}

	// main function
	public static void main(String args[]) {
		int s = 5;
		double chi = 0.4;
		double d = 0.1;
		double p = 0.2;
		
		GameEngine ge = new GameEngine(s, chi, d, p);
		ge.initGame();
		ge.printGameInfo();
		ge.saveGame("game.txt");

		GameEngine g = GameEngine.loadGame("game.txt");
		g.printGameInfo();
	}
}