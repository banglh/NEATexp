package expGame;

public class Position {
	private int[] coordinate;

	public Position() {
		coordinate = new int[2];
	}

	public Position(int[] c) {
		coordinate = c.clone();
	}

	public int[] getCoordinate() {
		return coordinate.clone();
	}

	public void setCoordinate(int[] c) {
		coordinate[0] = c[0];
		coordinate[1] = c[1];
	}
	
	public boolean equals(Object o) {
		boolean equals = false;
		int [] o_coor = ((Position) o).getCoordinate();
		if (coordinate[0] == o_coor[0] && coordinate[1] == o_coor[1])
			equals = true;
		
		return equals;
	}
	
	public int hashCode() {
		return coordinate[0] * 10 + coordinate[1];
	}
}
