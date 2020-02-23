package world;

import world.evolution.Commands;
import world.evolution.LiveCell;
import world.evolution.Species;

import java.util.LinkedList;
import java.util.Random;

public class World {
	public static final int WORLD_BRIGHTNESS = 1015;
	public static final double WATER_OPACITY = 0.985;

	public static final double CELL_SHADOW_Q = 1 / 15000d;

	private static WorldCell[][] matrix;
	private static int width, height;
	private static Diffusion diffusion;
	private static double diffusionSpeed = 3;
	private static int invisiblePoop = 0;


	public static LinkedList<WorldObject> actionList = new LinkedList<>();
	public static LinkedList<WorldObject> newObjects = new LinkedList<>();


	int totalMinerals = 0;

	static {
		width = 110;
		height = 90;
		matrix = new WorldCell[height][width];
		Random random = new Random();
		//int totalMinerals = 20 * width * height;
		diffusion = new Diffusion(matrix, diffusionSpeed);

		byte[] defaultGenome = new byte[Species.GENOME_SIZE];
		for (int i = 0; i < defaultGenome.length; i++) {
			defaultGenome[i] = Commands.PHOTOSYNTHESIS;
		}
		Species defaultSpecies = new Species(defaultGenome);

		for (int y = 0; y < height; y++) {
			matrix[y] = new WorldCell[width];
			for (int x = 0; x < width; x++) {
				matrix[y][x] = new WorldCell();
//				int organic = random.nextInt(20000) - 19000;
//				organic = organic < 0 ? 0 : organic;
//				if (organic > 0) {new LiveCell(defaultSpecies, x, y, 10, organic);}
//				if (organic > 0) matrix[i][x].setWorldObject(new DeadCell());
				matrix[y][x].setMinerals(100);
				}
			}

		new LiveCell(defaultSpecies, width/2, height/2, 10, 500);

		calculateLight();
	}

	public static WorldCell[][] getWorldMatrix() {
		return matrix;
	}

	public static void calculateLight() {
		for (int x = 0; x < width; x++) {
			double bri = WORLD_BRIGHTNESS;
			for (int y = 0; y < height; y++) {
				WorldCell cell = matrix[y][x];
				bri *= cell.getOpacity();
				cell.setLight((int) Math.round(bri));
			}
		}
	}

	public static void diffuse() {
//		diffusion.setDiffusionSpeed(diffusionSpeed + (Math.random()*0.3 - 0.15));
		diffusion.diffuse();
	}

	public static void addPoop(int poop) {
		invisiblePoop += poop;
	}

	public static int getWidth() {
		return width;
	}

	public static int getHeight() {
		return height;
	}

	public static WorldObject getWorldObject(int x, int y) {
		return matrix[y][x].getWorldObject();
	}

	public static void removeWorldObject(WorldObject worldObject) {
		addWorldObject(worldObject.getX(), worldObject.getY(), null);
		//actionList.remove(worldObject);
	}

	public static void addWorldObject(int x, int y, WorldObject worldObject) {
		matrix[y][x].setWorldObject(worldObject);
		newObjects.add(worldObject);
	}

	public static void moveWorldObject(int x, int y, WorldObject worldObject) {
		matrix[y][x].setWorldObject(worldObject);
	}

	public synchronized static void step() {
		actionList.addAll(newObjects);
		newObjects = new LinkedList<>();
		WorldObject current;
		while ((current = actionList.poll()) != null) {
			current.live();
			if (current.isAlive()) {
				newObjects.add(current);
			}
		}
		calculateLight();
		diffuse();
		recyclePoop();
	}

	public static WorldCell getCell(int x, int y){
		return matrix[y][x];
	}

	private static void recyclePoop() {
		if (invisiblePoop >= width) {
			int part = invisiblePoop / width;
			int limit = width * 110;
			for (int y = height - 1; y > 1; y--) {
				int levelLimit = limit - (12*y/10);
				if (calculateSum(y) < levelLimit) {
					for (int l = 0; l < part; l++) {
						int lY = y-l;
						for (int i = 0; i < matrix[height - 1].length; i++) {
							matrix[lY][i].addMinerals(1);
						}
					}
					break;

				}
			}
			invisiblePoop -= part * width;
		}
	}

	private static int calculateSum(int y) {
		int sum = 0;
		for (int i = 0; i < width; i++) {
			sum += matrix[y][i].getMinerals();
		}
		return sum;
	}
}
