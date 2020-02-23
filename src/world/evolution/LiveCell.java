package world.evolution;

import javafx.scene.paint.Color;
import world.Wall;
import world.World;
import world.WorldCell;
import world.WorldObject;

import java.util.Random;

public class LiveCell implements WorldObject, Commands {
	private final Species species;
	private final int genomeHash;
	private int commandIndex = 0;
	private int organic, minerals, x, y;
	private boolean isDead = false;
	private WorldObject[] mates = new WorldObject[4];
	private byte colonyStatus = 0;
	private int currentDirection = UP;

	//max commands per turn
	private static final int MAX_COMMANDS_PER_TURN = 15;

	//organic costs
	private static final int MAX_ORGANIC = 1000;
	private static final int BASIC_COST = 7;
	private static final int DIVISION_COST = 150;
	private static final int MOVEMENT_COST = 5;
	private static final int EAT_COST = 20;
	private static final int SURVIVAL_THRESHOLD = MAX_ORGANIC / 5;

	//mineral costs
	private static final int MAX_MINERALS = 500;
	public static final int DIVISION_MINERALS_COST = 162;
	private static final int BASIC_MINERALS_COST = 2;
	private static final int EAT_MINERALS_COST = 10;
	private static final int MINERAL_RELEASE_MAX = 50;
	private static final int PASSIVE_MINERAL_MAX = 27;

	public LiveCell(Species species, int x, int y, int minerals, int organic) {
		this.species = species;
		genomeHash = species.getGenome().hashCode();
		this.x = x;
		this.y = y;
		this.minerals = minerals;
		this.organic = organic;
		World.addWorldObject(x, y, this);
		species.increasePopulation();
	}


	@Override
	public int getOrganic() {
		return organic;
	}

	@Override
	public int getMinerals() {
		return minerals;
	}

	@Override
	public void consumeOrganic(WorldObject food) {
		organic += food.takeOrganic(MAX_ORGANIC - organic);
		if (food instanceof LiveCell) {
			((LiveCell) food).suffer();
		}
	}

	public void suffer() {
		if (organic < SURVIVAL_THRESHOLD) {
			die();
		} else {
			organic = organic / 2;
			WorldCell c = World.getWorldMatrix()[y][x];
			int outerMinerals = c.getMinerals();
			int s = (minerals + outerMinerals) / 2;
			int delta = minerals - s;
			delta = (delta > MINERAL_RELEASE_MAX) ? MINERAL_RELEASE_MAX : (delta < -1 * MINERAL_RELEASE_MAX) ? -1 * MINERAL_RELEASE_MAX : delta;
			minerals -= delta;
			c.addMinerals(delta);
		}

	}

	@Override
	public void consumeMinerals(WorldObject food) {
		minerals += food.takeMinerals(MAX_MINERALS - minerals);
	}

	@Override
	public void eat(WorldObject food) {
		if (organic < EAT_COST || minerals < EAT_MINERALS_COST) return;
		organic -= EAT_COST;
		minerals -= EAT_MINERALS_COST;
		World.getWorldMatrix()[y][x].addMinerals(EAT_MINERALS_COST);
		consumeMinerals(food);
		consumeOrganic(food);
	}

	private int eatWorldObject() {
		if (organic < EAT_COST || minerals < EAT_MINERALS_COST) return 1;
		organic -= EAT_COST;
		minerals -= EAT_MINERALS_COST;
		WorldObject neighbour = getNeighbourCell();
		if (neighbour == null) return 2;
		eat(neighbour);
		return 3;
	}

	private WorldObject getNeighbourCell(int direction) {
		int nX = (direction % 3) - 1;
		int nY = direction / 3;
		if (nX == 0 && nY == 0) return null;
		nX = x + nX;
		nY = nY + y;
		if (y >= World.getHeight()) return Wall.BOTTOM;
		if (y < 0) return Wall.TOP;
		nX = nX < 0 ? World.getWidth() - 1 : nX >= World.getWidth() ? 0 : nX;
		return World.getCell(nX, nY).getWorldObject();
	}

	private WorldObject getNeighbourCell() {
		return getNeighbourCell(currentDirection);
	}

	@Override
	public int takeMinerals(int amount) {
		int m = minerals > amount ? amount : minerals;
		minerals -= m;
		return m;
	}

	@Override
	public int takeOrganic(int amount) {
		int o = organic > amount ? amount : organic;
		organic -= o;
		if (organic < 1) die();
		return o;
	}

	@Override
	public void die() {
		WorldCell cell = World.getWorldMatrix()[y][x];
		isDead = true;
		if (organic <= 0) {
			cell.addMinerals(minerals);
			World.removeWorldObject(this);
		} else {
			new DeadCell(x, y, organic, minerals);
		}
		species.decreasePopulation();
	}

	@Override
	public void live() {
		//check if the cell has starved to death;
		if (organic < BASIC_COST || minerals < BASIC_COST) {
			die();
			return;
		}
		basicMetabolism();

		boolean breakFlag = false;
		byte[] genome = species.getGenome();
		for (int i = 0; i < MAX_COMMANDS_PER_TURN; i++) {
			switch (genome[commandIndex]) {
				case PHOTOSYNTHESIS:
					photosynthesis();
					incrementCommandIndex(genome);
					breakFlag = true;
					break;
				case EAT:
					int nextGene = eatWorldObject();
					gotoRelativeCommandIndex(genome, nextGene);
					breakFlag = true;
					break;
				default:
					gotoRelativeCommandIndex(genome, 0);
					break;
			}
			if (breakFlag) break;
		}
		passiveConsumeMinerals();
		if (organic >= MAX_ORGANIC && minerals >= DIVISION_MINERALS_COST) {
			divide();
		}
	}

	private int divide() {
		if (organic < DIVISION_COST || minerals < DIVISION_MINERALS_COST) {
			return 2;
		}
		int kidMinerals = (minerals - DIVISION_MINERALS_COST) / 2;
		int kidOrganic = (organic - DIVISION_COST) / 2;
		return divide(kidOrganic, kidMinerals, getOppositeDirection(currentDirection));
	}

	private int getOppositeDirection(int direction){
		int dir = direction % 9;
		switch (dir){
			case UP : return DOWN;
			case DOWN: return UP;

			case LEFT: return RIGHT;
			case RIGHT: return LEFT;

			case DOWN_LEFT : return UP_RIGHT;
			case DOWN_RIGHT : return UP_LEFT;

			case UP_LEFT: return DOWN_RIGHT;
			case UP_RIGHT: return DOWN_LEFT;

			default: return DOWN;
		}
	}

	private int divide(int kidOrganic, int kidMinerals, int startPosition) {
		int startY = (startPosition / 3)%3;
		int startX = startPosition % 3;
		for (int y1 = 0; y1 < 3; y1++) {
			int cY = ((y1 + startY)%3)-1;
			for (int x1 = 0; x1 < 3; x1++) {
				int cX = ((x1 + startX)%3)-1;
				if (cX == 1 && cY == 1) continue;
				int worldY = y + cY;
				int worldX = x + cX;
				worldX = worldX < 0 ? World.getWidth() - 1 : worldX >= World.getWidth() ? 0 : worldX;
				if (worldY < 0 || worldY >= World.getHeight()) {
					continue;
				}
				if (World.getCell(worldX, worldY).getWorldObject() == null) {
					if (organic < DIVISION_COST || minerals < DIVISION_MINERALS_COST) {
						return 2;
					}
					createNewCell(worldX, worldY, kidOrganic, kidMinerals);
					return 1;
				}
			}
		}
		//failed to divide
		die();
		return 2;
	}

	private void createNewCell(int x, int y, int kidOrganic, int kidMinerals) {
		organic -= DIVISION_COST;
		minerals -= DIVISION_MINERALS_COST / 2;
		int random = new Random().nextInt(Species.MUTATION_FACTOR * Species.GENOME_SIZE * Species.GENOME_SIZE);
		if (random > Species.GENOME_SIZE * Species.GENOME_SIZE) {
			new LiveCell(species, x, y, takeMinerals(kidMinerals), takeOrganic(kidOrganic));
		} else {
			byte[] mutated = species.getGenome().clone();
			byte mutation = (byte) (random / Species.GENOME_SIZE);
			int mutationIndex = random % Species.GENOME_SIZE;
			mutated[mutationIndex] = mutation;
			Species mutatedSpecies = new Species(mutated);
			new LiveCell(mutatedSpecies, x, y, takeMinerals(kidMinerals), takeOrganic(kidOrganic));
		}
	}

	private void photosynthesis() {
		if (organic > MAX_ORGANIC) return;
		WorldCell c = getMyCell();
		int light = (int) Math.round(0.75 * c.getLight() / (World.WATER_OPACITY - organic * World.CELL_SHADOW_Q));
		light /= 15;
		double bonus = 0.3;
		for (int i = 0; i < mates.length; i++) {
			if (mates[i] != null) bonus += 0.5;
		}
		bonus += minerals / 1500.0;
		organic += light * bonus;
	}

	private void incrementCommandIndex(byte[] genome) {
		int idx = (commandIndex + 1) % Species.GENOME_SIZE;
		if (genome[idx] != 0) {
			commandIndex = idx;
		}
	}

	private void gotoRelativeCommandIndex(byte[] genome, int index) {
		commandIndex = genome[index + commandIndex] % Species.GENOME_SIZE;
	}

	private void passiveConsumeMinerals() {
		WorldCell cell = getMyCell();
		int outerMinerals = cell.getMinerals();
		int delta = outerMinerals - minerals;
		delta += BASIC_MINERALS_COST + DIVISION_MINERALS_COST;
		if (delta > 0) {
			delta = (delta > PASSIVE_MINERAL_MAX) ? PASSIVE_MINERAL_MAX : delta;
		} else {
			delta = (Math.abs(delta) > PASSIVE_MINERAL_MAX) ? -1 * PASSIVE_MINERAL_MAX : delta;
		}
		if (delta < 0) {
			delta = (Math.abs(delta) > minerals) ? minerals : delta;
		} else {
			delta = (delta > outerMinerals) ? outerMinerals : delta;
		}
		minerals += delta;
		outerMinerals -= delta;
		cell.setMinerals(outerMinerals);

	}

	private void basicMetabolism() {
		//basic metabolism first
		organic -= BASIC_COST;
		minerals -= BASIC_MINERALS_COST;
		//add poop to the world poop bank
		World.addPoop(BASIC_MINERALS_COST);
	}

	private void updateColonyStatus() {
		int status = 0;
		for (int i = 0; i < mates.length; i++) {
			if (mates[i] == null) {
				status = (status << 1) | 0;
			} else {
				status = (status << 1) | 1;
			}
		}
		colonyStatus = (byte) status;
	}


	private WorldCell getMyCell() {
		return World.getWorldMatrix()[y][x];
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public Color getColor() {
		return Color.GREEN;
	}

	@Override
	public double getOpacity() {
		return organic * World.CELL_SHADOW_Q;
	}

	@Override
	public boolean isAlive() {
		return !isDead;
	}
}
