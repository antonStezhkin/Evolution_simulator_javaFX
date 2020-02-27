package world;

import world.evolution.Commands;
import world.evolution.DeadCell;
import world.evolution.LiveCell;
import world.evolution.Species;

import java.util.LinkedList;
import java.util.Random;

public class World {
	public static final int WORLD_BRIGHTNESS = 1215;
	public static final double WATER_OPACITY = 0.985;

	public static final double CELL_SHADOW_Q = 1 / 17000d;

	private static WorldCell[][] matrix;
	private static int width, height;
	private static Diffusion diffusion;
	private static double diffusionSpeed = 3;
	private static int invisiblePoop = 0;
	private static int totalMinerals = 0;
	private static int oldValue = 0;
	private static int mineralsChange = 0;
	private static final int MAX_CONCENTRATION = 181;


	public static LinkedList<WorldObject> actionList = new LinkedList<>();
	public static LinkedList<WorldObject> newObjects = new LinkedList<>();


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
		defaultGenome[35] = Commands.ACID;
		Species defaultSpecies = new Species(defaultGenome);
		defaultSpecies.setName("first");

		for (int y = 0; y < height; y++) {
			matrix[y] = new WorldCell[width];
			for (int x = 0; x < width; x++) {
				matrix[y][x] = new WorldCell();
//				int organic = random.nextInt(20000) - 19000;
//				organic = organic < 0 ? 0 : organic;
//				if (organic > 0) {new LiveCell(defaultSpecies, x, y, 10, organic);}
//				if (organic > 0) matrix[i][x].setWorldObject(new DeadCell());
				matrix[y][x].setMinerals(150);
				}
			}

		new LiveCell(defaultSpecies, width/2, height/2, 10, 500);

		calculateLight();
		totalMinerals = width*height*150 + 10;
		oldValue = totalMinerals;
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
		if(y < 0) return WorldsEdge.SKY;
		if(y >= height) return WorldsEdge.BOTTOM;
		x = x < 0? width-1 : x >= width? 0 : x;
		return matrix[y][x].getWorldObject();
	}

	public static void removeWorldObject(WorldObject worldObject) {
		int x = worldObject.getX();
		int y = worldObject.getY();
		if(matrix[y][x].getWorldObject() != worldObject) {
			//TODO fix this
			boolean bastardFound = false;
			for(int i = 0; i<height; i++){
				if(bastardFound) break;
				for(int j = 0; j < width; j++){
					if(matrix[i][j].getWorldObject() == worldObject) {
						matrix[i][j].setWorldObject(null);
						System.out.println("found the bastard at: [" + j + "," + i + "]");
						bastardFound = true;
						break;
					}
				}
			}

		}else {
			matrix[y][x].setWorldObject(null);
		}
		newObjects.remove(worldObject);
	}

	public static void addWorldObject(WorldObject worldObject) {
		int y = worldObject.getY();
		int x = worldObject.getX();
		if(y<0 || y >= height || !worldObject.isAlive()) return;
		x = x < 0? width-1 : x >= width? 0 : x;
		worldObject.setX(x);
		matrix[y][x].setWorldObject(worldObject);
		newObjects.add(worldObject);
	}

	public static synchronized boolean moveWorldObject(int x, int y, WorldObject worldObject) {
		if(y<0 || y >= height) return false;
		x = x < 0? width-1 : x >= width? 0 : x;
		if(matrix[y][x].getWorldObject() != null){
			System.out.println("occupied");
			return false;
		}
		matrix[worldObject.getY()][worldObject.getX()].setWorldObject(null);
		matrix[y][x].setWorldObject(worldObject);
		worldObject.setX(x);
		worldObject.setY(y);
		return true;
	}


	public synchronized static void step() {
		actionList.addAll(newObjects);
		newObjects = new LinkedList<>();
		WorldObject current;
		while ((current = actionList.poll()) != null) {
			current.live();
			if(current instanceof DeadCell){
				((DeadCell) current).isFuckedUp = true;
			}
			if (current.isAlive()) {
				newObjects.add(current);
				if(current instanceof DeadCell){
					((DeadCell) current).isFuckedUp = false;
				}
			}
		}
		calculateLight();
		diffuse();
		recyclePoop();
		//updateTotalMinerals();
	}

	public static WorldCell getCell(int x, int y){
		if(y<0 || y >= height) return null;
		x = x < 0? width-1 : x >= width? 0 : x;
		return matrix[y][x];
	}

	private static void recyclePoop() {
		if (invisiblePoop >= width*2) {
			//int stopAt = invisiblePoop - width;
			for (int y = height - 1; y > 1; y--) {
				if(invisiblePoop <= 0) break;
				int limit =  MAX_CONCENTRATION - (height - y);
				for(int x = 0; x < width; x++){
					if(matrix[y][x].getMinerals() < limit){
						if(invisiblePoop > 0) {
							invisiblePoop--;
							matrix[y][x].addMinerals(1);
						}else {break;}
					}
				}
			}
		}
		for (int i = 0; i < width; i++){
			if(matrix[height-1][i].getMinerals() < 20){
				matrix[height-1][i].addMinerals(10);

			}
		}
//		if(bottomPump)System.out.println("bottomPump!");
//		updateTotalMinerals();
//		System.out.println("recycle "+mineralsChange);
	}

	public static int takePoop(int amount){
		amount = invisiblePoop > amount? amount : invisiblePoop;
		invisiblePoop -= amount;
		return amount;
	}

	private static int calculateSum(int y) {
		int sum = 0;
		for (int i = 0; i < width; i++) {
			sum += matrix[y][i].getMinerals();
		}
		return sum;
	}

	public static void updateTotalMinerals(){
		int oldValue = totalMinerals;
		totalMinerals = invisiblePoop;
		for(int i = 0; i< height; i++){
			for(int j=0; j< width; j++){
				totalMinerals += matrix[i][j].getMinerals();
				if(matrix[i][j].getWorldObject() != null){
					totalMinerals += matrix[i][j].getWorldObject().getMinerals();
					boolean good = newObjects.contains(matrix[i][j].getWorldObject());
					if(!good) System.out.println("not good!");
				}
			}
		}
		mineralsChange += totalMinerals - oldValue;
	}
	public static int getTotalMinerals(){
		return totalMinerals;
	}
	public static int getMineralsChange(){
		return mineralsChange;
	}
}
