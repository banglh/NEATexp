package expGame;

import jneat.NNode;
import jneat.Network;
import jneat.Organism;

public class ANNController extends Controller{
	private Organism org;
	private Network nn;

	public ANNController() {
		
	}
	
	// set organism
	public void setOrganism(Organism o) {
		org = o;
		nn = org.getNet();
	}

	@Override
	public int getDirection(double[] rbfVals) {
		// TODO Auto-generated method stub
		// add a bias input
		double [] inputs = new double [rbfVals.length + 1];
		for (int i = 0; i < rbfVals.length; i++) {
			inputs[i] = rbfVals[i];
		}
		inputs[rbfVals.length] = 1.0;
		
		// plug input values to network
		nn.load_sensors(rbfVals);
		
		// calculate output value
		nn.activate();
		
		// get output
		double output = ((NNode) nn.getOutputs().elementAt(0)).getActivation();
		int expectedDirection = toAction(output);
		
		return expectedDirection;
	}

	// convert output of network to action
	private int toAction(double output) {
		if (output < 0.5) {
			return GameEngine.NORTH;
		} else {
			return GameEngine.EAST;
		}
	}
}
