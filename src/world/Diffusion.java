package world;

public class Diffusion {
	private int[][] distributionMatrix;
	private WorldCell[][] matrix;
	private double diffusionSpeed = 1;
	private int width, height;


	public boolean needsDiffusion(WorldCell[][] matrix) {
		int minerals = matrix[0][0].getMinerals();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix[y][x].getMinerals() != minerals) return true;
			}
		}
		return false;
	}

	public boolean diffuse() {
		if (!needsDiffusion(matrix)) return false;
		int numberOfSteps = (int) diffusionSpeed;
		double speed = diffusionSpeed - numberOfSteps;
		for (int i = 0; i < numberOfSteps; i++) {
			if (needsDiffusion(matrix)) step(1);
		}
		if (needsDiffusion(matrix) && speed > 0.001) step(speed);
		return needsDiffusion(matrix);
	}

	private void step(double speed) {
		buildDistributionMatrix(speed);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				matrix[y][x].addMinerals(distributionMatrix[y][x]);
			}
		}
	}

	private void buildDistributionMatrix(double speed) {
		distributionMatrix = new int[height][width];
//        int minQ = (int) Math.floor(9 / speed);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				WorldCell currentCell = matrix[y][x];
				int minerals = currentCell.getMinerals();
				if (minerals == 0) continue;
				// int partSize = (int) Math.floor((minerals / 9) * speed);
				int partSize = (int) Math.floor((minerals / 9) * speed *0.75);
				if (partSize > 0) {
					boolean e = distributeEqually(y, x, partSize);
					int rest = minerals % partSize;
					if (rest > 0 && !e) distributeRandomly(rest, y, x, speed);

				} else {
					distributeRandomly(minerals, y, x, speed);
				}
			}
		}
	}

	private boolean distributeEqually(int y, int x, int part) {
		int minerals = matrix[y][x].getMinerals();
		boolean isAllEqual = true;
		for (int nY = y - 1; nY < y + 2; nY++) {
			if (nY < 0 || nY >= height) continue;
			for (int nX = x - 1; nX < x + 2; nX++) {
				if (nY != y || nX != x) {
					int tX = nX < 0 ? width - 1 : nX >= width ? 0 : nX;
					int neighbourMinerals = matrix[nY][tX].getMinerals();
					if (minerals == neighbourMinerals) continue;
					distributionMatrix[y][x] -= part;
					distributionMatrix[nY][tX] += part;
					isAllEqual = false;
				}
			}
		}
		return isAllEqual;
	}

	private void distributeRandomly(int minerals, int y, int x, double... speed) {
		if (minerals < 1) return;
		int deposit = minerals;
		int nX = -1, nY = -1;
		double s  = (speed != null && speed.length > 0)? speed[0] : 1;
		try {
			for (int i = 0; i < minerals; i++) {
				//if(s < 1){
				if(Math.random() > s*0.75) return;
				//}
				int r = (int) (Math.random() * 45);
				if (r > 9) continue;
				switch (r) {
					case 0:
						nX = x - 1;
						nY = y - 1;
						break;
					case 1:
						nX = x;
						nY = y - 1;
						break;
					case 2:
						nX = x + 1;
						nY = y - 1;
						break;
					case 3:
						nX = x - 1;
						nY = y;
						break;
					case 4:
						nX = x + 1;
						nY = y;
						break;
					case 5:
						nX = x - 1;
						nY = y + 1;
						break;
					case 6:
						nX = x;
						nY = y + 1;
						break;
					case 7:
						nX = x + 1;
						nY = y + 1;
						break;
					default:
						break;
				}
				if (nY < 0 || nY >= height) return;
				nX = (nX < 0) ? width - 1 : (nX >= width) ? 0 : nX;
				if (matrix[nY][nX].getMinerals()+1 <= deposit) {
					distributionMatrix[y][x]--;
					distributionMatrix[nY][nX]++;
					deposit--;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Diffusion(World.getWorldMatrix()).distributeRandomly(5, World.getHeight() / 2, World.getWidth() / 2);
	}

	public Diffusion(WorldCell[][] matrix) {
		this.matrix = matrix;
		width = matrix[0].length;
		height = matrix.length;
		distributionMatrix = new int[height][width];

	}

	public Diffusion(WorldCell[][] matrix, double diffusionSpeed) {
		this.matrix = matrix;
		width = matrix[0].length;
		height = matrix.length;
		distributionMatrix = new int[height][width];
		this.diffusionSpeed = diffusionSpeed;
	}

	public void setDiffusionSpeed(double diffusionSpeed) {
		this.diffusionSpeed = diffusionSpeed;
	}
}
