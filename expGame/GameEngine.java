package expGame;

import java.util.Arrays;
import java.util.Random;


public class GameEngine {
	// constants
	public static final int NORTH = 0;	// north direction
	public static final int EAST = 1;	// east direction
	
	// game parameters
	private double [][] map;		// game board
	private int s;					// board size
	private double chi, d, p;		// chi, d, p params
	private int nf;					// number of radial basis functions
	private int [][] RBFsPos;		// positions of radial basis function centers
	private double sdRBF; 			// standard deviation of radial basis functions
	private int [] agentInitPos;	// initial agent's position
	private boolean initialized = false;
	private Random rand;

	// game status
	private int [] agentCurPos;		// current position of agent
	private double totalReward;		// current total reward
	private boolean gameEnded = false;		// game ended or not

	// constructors
	public GameEngine(int s, double chi, double d, double p) {
		// set params
		this.s = s;
		this.chi = chi;
		this.d = d;
		this.p = p;
		this.sdRBF = 1.0;
		this.map = new double [s][s];
		this.nf = (int) Math.floor(chi*(s-1)*(s-1));
		this.RBFsPos = new int [this.nf][2];
		this.agentInitPos = new int [2];
		this.agentCurPos = new int [2];
		rand = new Random(System.currentTimeMillis());
	}
	
	// initialize game
	public void initGame() {
		// initialize map
		initMap();
		
		// initialize RBF centers
		initRBFs();
		
		// initialize agent position
		initAgentPos();

		agentCurPos[0] = agentInitPos[0];
		agentCurPos[1] = agentInitPos[1];
		totalReward = 0.0;
		initialized = true;
		gameEnded = false;
	}
	
	// reset game
	public void resetGame() {
		// reset agent's location
		agentCurPos[0] = agentInitPos[0];
		agentCurPos[1] = agentInitPos[1];
		
		// reset total reward
		totalReward = 0.0;
		
		// other information
		gameEnded = false;
	}
	
	// move function
	public int move(int expectedDirection) {
		int realDirection = -1;
		
		// check if the agent is in a terminal state
		if (isTerminalState(agentCurPos))
			return realDirection;
		
		// check if expected direction is valid
		if (expectedDirection != NORTH && expectedDirection != EAST) {
			return realDirection;
		}
		
		// get real moving direction
		double prob = rand.nextDouble();
		if (prob < 1- p)
			realDirection = expectedDirection;
		else
			realDirection = (NORTH + EAST) - expectedDirection;
		
		// moving 
		switch (realDirection) {
		case NORTH:
			// update current agent's location
			agentCurPos[0] -= 1;
			break;
		case EAST:
			// update current agent's location
			agentCurPos[1] += 1;
			break;
		}
		// update total reward
		totalReward += map[agentCurPos[0]][agentCurPos[1]];
		
		// check and update game state
		if (isTerminalState(agentCurPos))
			gameEnded = true;
		
		return realDirection;
	}

	// check if current state if a terminal state
	private boolean isTerminalState(int [] location) {
		boolean isTerminal = false;
		if (location[0] == 0 || location[1] == s-1)
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
		int maxNum = (s-1)*(s-1);
		int [] positions = new int [maxNum];
		int [] rbfs = new int [nf];
		
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
		int row = rand.nextInt(s - 1) + 1;		// from 1 to s-1
		int col = rand.nextInt(s - 1);			// from 0 to s-2
		agentInitPos[0] = row;
		agentInitPos[1] = col;	
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
	
	public double [][] getMap() {
		return map.clone();
	}
	
	public int [][] getRBFsPos() {
		return RBFsPos.clone();
	}
	
	public int [] getAgentInitPos() {
		return agentInitPos.clone();
	}
	
	public int [] getAgentCurPos() {
		return agentCurPos.clone();
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
		System.out.printf("[%d][%d]\n", agentInitPos[0], agentInitPos[1]);
		
		// Agent's current position
		System.out.println("+ Agent's current location");
		System.out.printf("[%d][%d]\n", agentCurPos[0], agentCurPos[1]);
	}

	// main function
	public static void main(String args[]) {
		int s = 5;
		double chi = 0.4;
		double d = 0.1;
		double p = 0.2;
		GameEngine ge = new GameEngine(s, chi, d, p);
		ge.printGameInfo();
		ge.initGame();
		ge.printGameInfo();
	}
}
