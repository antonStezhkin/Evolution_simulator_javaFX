package world.evolution;

import javafx.scene.paint.Color;
import world.WorldsEdge;
import world.World;
import world.WorldCell;
import world.WorldObject;

public class LiveCell implements WorldObject, Commands {
	private final Species species;
	private final int genomeHash;
	private int commandIndex = 0;
	private int organic, minerals, x, y;
	private boolean isDead = false;
	private WorldObject[] mates = new WorldObject[4];
	private byte colonyStatus = 0;
	private int currentDirection = UP;
	private byte gas = 0;
	private final int TOLERANCE = 2;

	//max commands per turn
	private static final int MAX_COMMANDS_PER_TURN = 15;

	//organic costs
	private static final int MAX_ORGANIC = 1000;
	private static final int BASIC_COST = 7;
	private static final int DIVISION_COST = 150;
	private static final int MOVEMENT_COST = 3;
	private static final int EAT_COST = 20;
	private static final int SURVIVAL_THRESHOLD = MAX_ORGANIC / 5;

	//mineral costs
	private static final int MAX_MINERALS = 500;
	public static final int DIVISION_MINERALS_COST = 120;
	private static final int BASIC_MINERALS_COST = 2;
	private static final int EAT_MINERALS_COST = 6;
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
		if (organic < EAT_COST || minerals < EAT_MINERALS_COST) return STARVING;
		organic -= EAT_COST;
		minerals -= EAT_MINERALS_COST;
		WorldObject neighbour = getNeighbourCell();
		if (neighbour == null) return INVALID_PARAM;
		eat(neighbour);
		return SUCCESS;
	}

	private WorldObject getNeighbourCell(int direction) {
		int nX = (direction % 3) - 1;
		int nY = direction / 3;
		if (nX == 0 && nY == 0) return null;
		nX = x + nX;
		nY = nY + y;
		if (y >= World.getHeight()) return WorldsEdge.BOTTOM;
		if (y < 0) return WorldsEdge.SKY;
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
		int nextGene;
		for (int i = 0; i < MAX_COMMANDS_PER_TURN; i++) {
			switch (genome[commandIndex]) {
				case PHOTOSYNTHESIS:
					photosynthesis();
					incrementCommandIndex(genome);
					breakFlag = true;
					break;
				case EAT:
					nextGene = eatWorldObject();
					gotoRelativeCommandIndex(genome, nextGene);
					breakFlag = true;
					break;
				case GAS:
					if (gas >= 2) {
						gotoRelativeCommandIndex(genome, 2);
					} else {
						gas++;
						gotoRelativeCommandIndex(genome, 1);
					}
					break;
				case SINK:
					if (gas <= -2) {
						gotoRelativeCommandIndex(genome, 2);
					} else {
						gas--;
						gotoRelativeCommandIndex(genome, 1);
					}
					break;
				case MOVE:
					nextGene = moveMe(genome);
					gotoRelativeCommandIndex(genome, nextGene);
					breakFlag = true;
					break;
				case TAXIS:
					nextGene = taxis(genome);
					gotoRelativeCommandIndex(genome, nextGene);
					breakFlag = true;
					break;

				case SPAWN :
					nextGene = simpleDivision(genome);
					gotoRelativeCommandIndex(genome, nextGene);
					breakFlag = true;
					break;
				case DIVIDE :
					nextGene = divide();
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
		passiveFloat();
		if (organic >= MAX_ORGANIC && minerals >= DIVISION_MINERALS_COST) {
			if(divide() == ALIEN){
				die();
			}
		}
	}

	private int simpleDivision(byte[] genome) {
		int directionIndex = (commandIndex - 1) % Species.GENOME_SIZE;
		int mineralsIndex = (commandIndex - 2) % Species.GENOME_SIZE;
		int organicIndex =  (commandIndex - 3) % Species.GENOME_SIZE;
		int direction = (int)genome[directionIndex]%9;
		if(direction == 4) return INVALID_PARAM;
		if(organic < DIVISION_COST || minerals < DIVISION_MINERALS_COST) return  STARVING;
		int org = (int)genome[organicIndex]*organic/128;
		int min = (int)genome[mineralsIndex]*minerals/128;
		if(organic - (org+DIVISION_COST) < BASIC_COST || minerals - (min+DIVISION_MINERALS_COST) < BASIC_MINERALS_COST) return STARVING;
		if(org < BASIC_COST || min < BASIC_MINERALS_COST) return STARVING;
		return divide(org, min, direction);
	}

	private int taxis(byte[] genome) {
		if (organic < MOVEMENT_COST) return STARVING;
		int paramIndex = (commandIndex - 1) % Species.GENOME_SIZE;
		paramIndex = paramIndex < 0? Species.GENOME_SIZE + paramIndex : paramIndex;
		int max = Integer.MIN_VALUE;
		int tY = -1, tX = 0;
		try{
		boolean light = genome[paramIndex] % 2 == 1;
		for (int i = -1; i < 2; i++) {
			int nY = y + i;
			for (int j = -1; j < 2; j++) {
				if (nY < 0 || nY >= World.getHeight()) continue;
				int nX = x + j;
				WorldCell worldCell = World.getCell(nX, nY);
				int c = light ? worldCell.getLight() : worldCell.getMinerals();
				if (c > max) {
					max = c;
					tY = nY;
					tX = nX;
				}
			}
		}
		WorldObject worldObject = World.getWorldObject(tX, tY);
		if (worldObject != null) {
			if (worldObject instanceof LiveCell) {
				return checkRelation((LiveCell) worldObject);
			} else {
				return ALIEN;
			}
		} else {
			int direction = (tY - y + 1) * 3 + (tX - x + 1);
			if (direction == 4) return INVALID_PARAM;
			currentDirection = direction;
			organic -= MOVEMENT_COST;
			World.moveWorldObject(tX, tY, this);
			return SUCCESS;
		}
		}catch (Exception e){
			return INVALID_PARAM;
		}
	}

	private int moveMe(byte[] genome) {
		if (organic < MOVEMENT_COST) return STARVING;
		int paramIndex = (commandIndex - 1) % Species.GENOME_SIZE;
		paramIndex =  paramIndex < 0? Species.GENOME_SIZE + paramIndex : paramIndex;
		int direction = genome[paramIndex] % 9;
		//invalid direction
		if (direction == 4) return INVALID_PARAM;

		int nX = (direction % 3) - 1;
		int nY = (direction / 3) - 1;
		nY += y;

		//World's edge
		if (nY < 0) return WORLD_TOP;
		if (nY >= World.getHeight()) return WORLD_BOTTOM;
		nX += x;

		//Another object blocking the way
		WorldObject worldObject = World.getWorldObject(nX, nY);
		if (worldObject != null) {
			if (worldObject instanceof LiveCell) {
				return checkRelation((LiveCell) worldObject);
			} else {
				return ALIEN;
			}
		}

		//success
		World.moveWorldObject(nX, nY, this);
		organic -= MOVEMENT_COST;
		return SUCCESS;
	}

	private int checkRelation(LiveCell other) {
		if (other == null) return INVALID_PARAM;
		if (other.genomeHash == genomeHash) return CLONE;
		int differences = 0;
		byte[] genome = species.getGenome();
		byte[] otherGenome = other.species.getGenome();
		for (int i = 0; i < genome.length; i++) {
			if (genome[i] != otherGenome[i]) {
				differences++;
				if (differences > TOLERANCE) return ALIEN;
			}
		}
		return RELATIVE;
	}

	private void passiveFloat() {
		int k = gas > 0 ? 1 : gas < 0 ? -1 : 0;
		if (k == 0) return;
		int nY = y + k;
		if (World.getWorldObject(x, nY) == null) {
			World.moveWorldObject(x, nY, this);
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

	private int getOppositeDirection(int direction) {
		switch (direction % 9) {
			case UP_LEFT:
				return DOWN_RIGHT;
			case DOWN_RIGHT:
				return UP_LEFT;

			case UP:
				return DOWN;
			case DOWN:
				return UP;

			case UP_RIGHT:
				return DOWN_LEFT;
			case DOWN_LEFT:
				return UP_RIGHT;

			case LEFT:
				return RIGHT;
			case RIGHT:
				return LEFT;
			default:
				break;
		}
		return 4;
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
				if (World.getWorldObject(worldX, worldY) == null) {
					if (organic < DIVISION_COST || minerals < DIVISION_MINERALS_COST) {
						return STARVING;
					}
					createNewCell(worldX, worldY, kidOrganic, kidMinerals);
					return SUCCESS;
				}
			}
		}
		//failed to divide
		return ALIEN;
	}

	private void createNewCell(int x, int y, int kidOrganic, int kidMinerals) {
		organic -= DIVISION_COST;
		minerals -= DIVISION_MINERALS_COST / 2;
		int random = (int) (System.nanoTime() % (Species.MUTATION_FACTOR + 1));
		LiveCell kid = null;
		if (random > 1) {
			kid = new LiveCell(species, x, y, takeMinerals(kidMinerals), takeOrganic(kidOrganic));
		} else {
			Species mutatedSpecies = species.mutate();
			kid = new LiveCell(mutatedSpecies, x, y, takeMinerals(kidMinerals), takeOrganic(kidOrganic));
		}
		kid.setCurrentDirection(currentDirection);
	}

	private void photosynthesis() {
		if (organic > MAX_ORGANIC) return;
		WorldCell c = getMyCell();
		int light = (int) Math.round(0.80 * c.getLight() / (World.WATER_OPACITY - organic * World.CELL_SHADOW_Q));
		light /= 15;
		double bonus = 0.4;
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
		commandIndex = genome[(index + commandIndex) % Species.GENOME_SIZE];
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

	public void setCurrentDirection(int currentDirection) {
		this.currentDirection = currentDirection % 9 == 4 ? UP : currentDirection % 9;
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
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public void setY(int y) {
		this.y = y;
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
