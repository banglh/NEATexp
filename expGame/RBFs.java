package expGame;

public class RBFs {
	private double [] rbfs;
	
	public RBFs(double[] newRBFs) {
		rbfs = newRBFs.clone();
	}
	
	public double[] getRBFs() {
		return rbfs.clone();
	}
}
