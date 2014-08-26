package expGame;

import java.util.Random;

public class RandomController extends Controller {
	private Random rand;
	private int [] actionsSet = {GameEngine.NORTH, GameEngine.EAST};

	public RandomController() {
		rand = new Random(System.currentTimeMillis());
	}

	@Override
	public int getDirection(double[] rbfVals) {
		// TODO Auto-generated method stub
		int actionIndx = rand.nextInt(2);
		int expectedDirection = actionsSet[actionIndx];
		
		return expectedDirection;
	}

}
