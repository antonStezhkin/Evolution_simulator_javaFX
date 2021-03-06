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
	public WorldObject[] mates = new WorldObject[4];
	private byte colonyStatus = 0;
	private int currentDirection = UP;
	private byte gas = 0;
	private final int TOLERANCE = 2;
	private boolean divide = true;

	//max commands per turn
	private static final int MAX_COMMANDS_PER_TURN = 15;
	private static final int PREDATOR_HUE = 47;
	private static final int PLANT_HUE = 140;
	private static final int CHEM_HUE = 280;
	private static final double S = 1;
	private static final double B = 0.4;
	private double prevHue = PLANT_HUE;

	//organic costs
	private static final int MAX_ORGANIC = 1000;
	private static final int BASIC_COST = 7;
	private static final int DIVISION_COST = 150;
	private static final int MOVEMENT_COST = 3;
	private static final int EAT_COST = 5;
	private static final int SURVIVAL_THRESHOLD = MAX_ORGANIC / 10;
	private static final int SUFFER_THRESHOLD = MAX_ORGANIC / 5;
	private static final int POOP_TO_MINERALS_COST = 1;


	//mineral costs
	private static final int MAX_MINERALS = 250;
	public static final int DIVISION_MINERALS_COST = 120;
	private static final int BASIC_MINERALS_COST = 1;
	private static final int EAT_MINERALS_COST = 2;
	private static final int MINERAL_RELEASE_MAX = 60;
	private static final int PASSIVE_MINERAL_MAX = 30;
	private int totalGained = 1;
	private int photoGained = 1;
	private int predatorGained = 0;
	private int chemGained = 0;
	private int releaseMinerals = 0;

	public LiveCell(Species species, int x, int y, int minerals, int organic) throws Exception {
		this.species = species;
		genomeHash = species.getGenome().hashCode();
		this.x = x;
		this.y = y;
		this.minerals = minerals;
		this.organic = organic;
		World.addWorldObject(this);
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
	public void consumeOrganic(WorldObject food) throws Exception {
		int gain = food.takeOrganic(food.getOrganic());
		organic += gain;
		totalGained += gain;
		predatorGained += gain;
//		if (food instanceof LiveCell) {
//			((LiveCell) food).suffer();
//		}
	}

	public void suffer() throws Exception {
		if (species.acidResistant) return;
		if (organic < SURVIVAL_THRESHOLD) {
			die();
		} else if (organic < SUFFER_THRESHOLD) {
			organic -= organic / 4;
			WorldCell c = World.getWorldMatrix()[y][x];
			int outerMinerals = c.getMinerals();
			int s = (minerals + outerMinerals) / 4;
			int delta = minerals - s;
			delta = (delta > MINERAL_RELEASE_MAX) ? MINERAL_RELEASE_MAX : (delta < -1 * MINERAL_RELEASE_MAX) ? -1 * MINERAL_RELEASE_MAX : delta;
			minerals -= delta;
			c.addMinerals(delta);
		}
	}

	@Override
	public void consumeMinerals(WorldObject food) {
		minerals += food.takeMinerals(food.getMinerals());
	}

	private void eat(WorldObject food) throws Exception {
		if (organic < EAT_COST || minerals < EAT_MINERALS_COST) return;
		organic -= EAT_COST;
		minerals -= EAT_MINERALS_COST;
		World.addPoop(EAT_MINERALS_COST);
		consumeMinerals(food);
		consumeOrganic(food);
	}

	private int eatWorldObject() throws Exception {
		if (organic < EAT_COST || minerals < EAT_MINERALS_COST) return STARVING;
		organic -= EAT_COST;
		minerals -= EAT_MINERALS_COST;
		World.addPoop(EAT_MINERALS_COST);
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
		return World.getWorldObject(nX, nY);
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
	public int takeOrganic(int amount) throws Exception {
		int o = organic > amount ? amount : organic;
		organic -= o;
		if (organic < 1) die();
		return o;
	}

	@Override
	public void die() throws Exception {
		isDead = true;
		if (organic <= 0) {
			World.getCell(x, y).addMinerals(minerals);
			//World.removeWorldObject(this);
			minerals = 0;
		} else if(colonyStatus == 0){

			new DeadCell(x, y, organic, minerals);
		} else{
			new DeadColonyCell(x,y,organic,minerals,mates);
		}
		species.decreasePopulation();
	}

	@Override
	public void live() throws Exception {
		if (isDead) return;
		//check if the cell has starved to death;
		if (organic < BASIC_COST || minerals < BASIC_COST) {
			die();
			return;
		}
		basicMetabolism();

		boolean breakFlag = false;
		byte[] genome = species.getGenome();
		int nextGene;
		releaseMinerals = 0;
		divide = true;
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
				case TAXIS:
					nextGene = taxis(genome);
					gotoRelativeCommandIndex(genome, nextGene);
					breakFlag = true;
					break;
				case EAT_POOP:
					poopToMinerals(genome);
					incrementCommandIndex(genome);
					break;
				case EAT_MINERALS:
					nextGene = mineralsToOrganic(genome);
					gotoRelativeCommandIndex(genome, nextGene);
					breakFlag = true;
					break;
				case PUMP_MINERALS:
					pumpMinerals();
					incrementCommandIndex(genome);
					breakFlag = true;
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

				//Reproduction
				case SPAWN:
					nextGene = simpleDivision(genome);
					gotoRelativeCommandIndex(genome, nextGene);
					breakFlag = true;
					break;
				case DIVIDE:
					nextGene = divide();
					gotoRelativeCommandIndex(genome, nextGene);
					breakFlag = true;
					break;
				case NO_DIVISION:
					divide = false;
					incrementCommandIndex(genome);
					break;
				case GROW:
					nextGene = growColony(genome);
					gotoRelativeCommandIndex(genome, nextGene);
					break;

				//Special
				case DESTROY_CORPSE:
					destroyDeadCell();
					incrementCommandIndex(genome);
					break;
				case ACID:
					acid();
					incrementCommandIndex(genome);
					break;
				case RELEASE_MINERALS:
					int amountIndex = (commandIndex - 1) % Species.GENOME_SIZE;
					amountIndex = amountIndex < 0 ? Species.GENOME_SIZE + amountIndex : amountIndex;
					releaseMinerals += genome[amountIndex];
					incrementCommandIndex(genome);
					break;

				//Sensors
				case SURROUNDED:
					int next = isSurrounded() ? SUCCESS : INVALID_PARAM;
					gotoRelativeCommandIndex(genome, next);
					break;
				case CHECK_ENERGY:
					gotoRelativeCommandIndex(genome, checkEnergy(genome));
					break;
				case CHECK_MINERALS:
					gotoRelativeCommandIndex(genome, checkMinerals(genome));
					break;
				case CHECK_LIGHT:
					gotoRelativeCommandIndex(genome, checkLight(genome));
					break;
				case CHECK_RELATION:
					gotoRelativeCommandIndex(genome, checkRelation());
					break;
				default:
					gotoRelativeCommandIndex(genome, 0);
					break;
			}
			if (breakFlag) break;
		}
		passiveConsumeMinerals();
		if (releaseMinerals > 0) {
			releaseMinerals = takeMinerals(releaseMinerals);
			getMyCell().addMinerals(releaseMinerals);
		}
		passiveFloat();
		if (organic >= MAX_ORGANIC && minerals >= DIVISION_MINERALS_COST && divide) {
			if (colonyStatus != 0) {
				if (!passiveGrow()) {
					if (divide() == ALIEN) die();
				}
			} else if (divide() == ALIEN) {
				die();
			}
		}
	}

	private int checkLight(byte[] genome) {
		int amountIndex = (commandIndex - 1) % Species.GENOME_SIZE;
		amountIndex = amountIndex < 0 ? Species.GENOME_SIZE + amountIndex : amountIndex;
		return genome[amountIndex] * 15 < getMyCell().getLight() ? SUCCESS : INVALID_PARAM;
	}

	private int checkMinerals(byte[] genome) {
		int amountIndex = (commandIndex - 1) % Species.GENOME_SIZE;
		amountIndex = amountIndex < 0 ? Species.GENOME_SIZE + amountIndex : amountIndex;
		return genome[amountIndex] * 3 < minerals ? SUCCESS : INVALID_PARAM;
	}

	private int checkEnergy(byte[] genome) {
		int amountIndex = (commandIndex - 1) % Species.GENOME_SIZE;
		amountIndex = amountIndex < 0 ? Species.GENOME_SIZE + amountIndex : amountIndex;
		return genome[amountIndex] * 15 < organic ? SUCCESS : INVALID_PARAM;
	}

	private void destroyDeadCell() throws Exception {
		if (organic < BASIC_COST * 5) return;
		for (int i = -1; i < 2; i++) {
			int tY = y + i;
			if (tY < 0 || tY >= World.getHeight()) continue;
			for (int j = -1; j < 2; j++) {
				if (i == 0 && j == 0) continue;
				int tX = j + x;
				tX = tX < 0 ? World.getWidth() - 1 : tX >= World.getWidth() ? 0 : tX;
				WorldObject worldObject = World.getWorldObject(tX, tY);
				if (worldObject != null) {
					if (worldObject instanceof DeadCell) {
						worldObject.die();
						organic -= 30;
						return;
					}
				}
			}
		}
	}

	public boolean isAcidResistant() {
		return species.acidResistant;
	}

	private void acid() throws Exception {
		int nearbyTiles = (y - 1 < 0 || y + 1 >= World.getHeight()) ? 6 : 8;
		if (organic < (BASIC_COST * 2) + (nearbyTiles * 10)) return;
		organic -= nearbyTiles * 10;
		for (int i = -1; i < 2; i++) {
			int tY = y + i;
			if (tY < 0 || tY >= World.getHeight()) continue;
			for (int j = -1; j < 2; j++) {
				if (i == 0 && j == 0) continue;
				int tX = j + x;
				tX = tX < 0 ? World.getWidth() - 1 : tX >= World.getWidth() ? 0 : tX;
				WorldObject worldObject = World.getWorldObject(tX, tY);
				if (worldObject != null) {
					if (worldObject instanceof DeadCell || worldObject instanceof DeadColonyCell) {
						worldObject.takeOrganic(150);
					} else if (worldObject instanceof LiveCell) {
						if (!((LiveCell) worldObject).isAcidResistant() && !isMate(worldObject)) {
							worldObject.takeOrganic(5);
							((LiveCell) worldObject).suffer();
						}
					}
				}
			}
		}
	}

	private boolean isMate(WorldObject worldObject) {
		if (worldObject == null || !(worldObject instanceof LiveCell) || colonyStatus == 0) return false;
		for (int i = 0; i < mates.length; i++) {
			if (mates[i] == worldObject) return true;
		}
		return false;
	}

	private int mineralsToOrganic(byte[] genome) {
		if (organic >= MAX_ORGANIC) return SUCCESS;
		int amountIndex = (commandIndex - 1) % Species.GENOME_SIZE;
		amountIndex = amountIndex < 0 ? Species.GENOME_SIZE + amountIndex : amountIndex;
		int amount = genome[amountIndex];
		if (minerals - amount < BASIC_MINERALS_COST * 2) return STARVING;
		minerals -= amount;
		int gain = (int) (amount * 2.5);
		organic += gain;
		chemGained += gain;
		totalGained += gain;
		World.addPoop(amount);
		return SUCCESS;
	}

	private void pumpMinerals() {
		if (organic < BASIC_COST * 2 + 100 || minerals >= MAX_MINERALS) return;
		int nearbyTiles = (y - 1 < 0 || y + 1 >= World.getHeight()) ? 6 : 9;
		int totalMinerals = 0;
		for (int i = -1; i < 2; i++) {
			int tY = y + i;
			if (tY < 0 || tY >= World.getHeight()) continue;
			for (int j = -1; j < 2; j++) {
				int tX = j + x;
				tX = tX < 0 ? World.getWidth() - 1 : tX >= World.getWidth() ? 0 : tX;
				totalMinerals += World.getCell(tX, tY).getMinerals();
			}
		}
		int mineralsAmount = totalMinerals < 100 ? totalMinerals : 100;
		organic -= mineralsAmount;
		totalMinerals -= mineralsAmount;
		minerals += mineralsAmount;
		int part = totalMinerals / nearbyTiles;
		int rest = totalMinerals % nearbyTiles;
		if (part * nearbyTiles + rest != totalMinerals) System.out.println("FUCK!");
		for (int i = -1; i < 2; i++) {
			int tY = y + i;
			if (tY < 0 || tY >= World.getHeight()) continue;
			for (int j = -1; j < 2; j++) {
				int tX = j + x;
				tX = tX < 0 ? World.getWidth() - 1 : tX >= World.getWidth() ? 0 : tX;
				World.getCell(tX, tY).setMinerals(part);
			}
		}
		getMyCell().addMinerals(rest);
	}

	private void poopToMinerals(byte[] genome) {
		if (minerals > MAX_MINERALS) return;
		int amountIndex = (commandIndex - 1) % Species.GENOME_SIZE;
		amountIndex = amountIndex < 0 ? Species.GENOME_SIZE + amountIndex : amountIndex;
		int amount = genome[amountIndex] * 2;
		amount = World.takePoop(amount);
		organic -= amount;
		minerals += amount;
	}


	private int simpleDivision(byte[] genome) throws Exception {
		int directionIndex = (commandIndex - 1) % Species.GENOME_SIZE;
		int mineralsIndex = (commandIndex - 2) % Species.GENOME_SIZE;
		int organicIndex = (commandIndex - 3) % Species.GENOME_SIZE;

		directionIndex = directionIndex < 0 ? Species.GENOME_SIZE + directionIndex : directionIndex;
		mineralsIndex = mineralsIndex < 0 ? Species.GENOME_SIZE + mineralsIndex : mineralsIndex;
		organicIndex = organicIndex < 0 ? Species.GENOME_SIZE + organicIndex : organicIndex;

		int direction = (int) genome[directionIndex] % 9;
		if (direction == 4) return INVALID_PARAM;
		if (organic < DIVISION_COST + BASIC_COST * 6 || minerals < DIVISION_MINERALS_COST + BASIC_MINERALS_COST * 6)
			return STARVING;
		int org = (int) genome[organicIndex] * organic / 128;
		int min = (int) genome[mineralsIndex] * minerals / 128;
		if (organic - (org + DIVISION_COST) < BASIC_COST || minerals - (min + DIVISION_MINERALS_COST) < BASIC_MINERALS_COST)
			return STARVING;
		if (org < BASIC_COST || min < BASIC_MINERALS_COST) return STARVING;
		return divide(org, min, direction);
	}

	private int taxis(byte[] genome) {
		if(colonyStatus > 0) return RELATIVE;
		if (organic < MOVEMENT_COST + BASIC_COST * 3) return STARVING;
		int paramIndex = (commandIndex - 1) % Species.GENOME_SIZE;
		paramIndex = paramIndex < 0 ? Species.GENOME_SIZE + paramIndex : paramIndex;
		int max = Integer.MIN_VALUE;
		int tY = -1, tX = 0;
		try {
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
		} catch (Exception e) {
			return INVALID_PARAM;
		}
	}

	private int moveMe(byte[] genome) throws Exception {
		if(colonyStatus != 0) return RELATIVE;
		if (organic < MOVEMENT_COST + BASIC_COST * 3) return STARVING;
		int paramIndex = (commandIndex - 1) % Species.GENOME_SIZE;
		paramIndex = paramIndex < 0 ? Species.GENOME_SIZE + paramIndex : paramIndex;
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

	private int checkRelation() {
		return checkRelation(currentDirection);
	}

	private int checkRelation(int direction) {
		int dX = direction % 3 - 1;
		int dY = direction / 3 - 1;
		WorldObject worldObject = World.getWorldObject(x + dX, y + dY);
		if (worldObject == null || !(worldObject instanceof LiveCell)) return INVALID_PARAM;
		else return checkRelation((LiveCell) worldObject);
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

	private void passiveFloat() throws Exception {
		if (isDead || colonyStatus != 0) return;
		int k = gas > 0 ? 1 : gas < 0 ? -1 : 0;
		if (k == 0) return;
		int nY = y + k;
		if (World.getWorldObject(x, nY) == null) {
			World.moveWorldObject(x, nY, this);
		}
	}

	private int divide() throws Exception {
		if (organic < DIVISION_COST + BASIC_COST * 6 || minerals < DIVISION_MINERALS_COST + BASIC_MINERALS_COST * 6) {
			return STARVING;
		}
		int kidMinerals = (minerals - DIVISION_MINERALS_COST) / 2;
		int kidOrganic = (organic - DIVISION_COST) / 2;
		return divide(kidOrganic, kidMinerals, getOppositeDirection(currentDirection) - 1);
	}

	private int getOppositeDirection(int direction) {
		switch (Math.abs(direction) % 9) {
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

	private int divide(int kidOrganic, int kidMinerals, int startPosition) throws Exception {
		int startY = (startPosition / 3) % 3;
		int startX = startPosition % 3;
		for (int y1 = 0; y1 < 3; y1++) {
			int cY = ((y1 + startY) % 3) - 1;
			for (int x1 = 0; x1 < 3; x1++) {
				int cX = ((x1 + startX) % 3) - 1;
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
		int nX = x + 1 >= World.getWidth() ? 0 : x + 1;
		if (World.getWorldObject(nX, y + 1) == null && y + 1 < World.getHeight()) {
			if (organic < DIVISION_COST || minerals < DIVISION_MINERALS_COST) {
				return STARVING;
			}
			createNewCell(nX, y + 1, kidOrganic, kidMinerals);
			return SUCCESS;
		}
		//failed to divide
		return ALIEN;
	}

	private boolean passiveGrow() throws Exception {
		if (organic < DIVISION_COST + BASIC_COST * 6 || minerals < DIVISION_MINERALS_COST + BASIC_MINERALS_COST * 6) {
			return false;
		}
		int kidMinerals = (minerals - DIVISION_MINERALS_COST) / 2;
		int kidOrganic = (organic - DIVISION_COST) / 2;
		return grow(currentDirection, kidOrganic, kidMinerals) != INVALID_PARAM;
	}

	private int growColony(byte[] genome) throws Exception {
		if (organic < DIVISION_COST + BASIC_COST * 3 || minerals < DIVISION_MINERALS_COST + BASIC_MINERALS_COST * 3)
			return STARVING;
		int directionIndex = (commandIndex - 1) % Species.GENOME_SIZE;
		int mineralsIndex = (commandIndex - 2) % Species.GENOME_SIZE;
		int organicIndex = (commandIndex - 3) % Species.GENOME_SIZE;

		directionIndex = directionIndex < 0 ? Species.GENOME_SIZE + directionIndex : directionIndex;
		mineralsIndex = mineralsIndex < 0 ? Species.GENOME_SIZE + mineralsIndex : mineralsIndex;
		organicIndex = organicIndex < 0 ? Species.GENOME_SIZE + organicIndex : organicIndex;

		int direction = (int) genome[directionIndex] % 9;

		int org = (int) genome[organicIndex] * organic / 128;
		int min = (int) genome[mineralsIndex] * minerals / 128;
		if (organic - (org + DIVISION_COST) < BASIC_COST || minerals - (min + DIVISION_MINERALS_COST) < BASIC_MINERALS_COST)
			return STARVING;
		if (org < BASIC_COST || min < BASIC_MINERALS_COST) return STARVING;
		return grow(direction, org, min);
	}

	private int grow(int startDirection, int kidOrganic, int kidMinerals) throws Exception {
		startDirection = startDirection > 8 ? startDirection % 9 : startDirection;
		if (startDirection < 8) {
			startDirection = ((1 & startDirection) == 0) ? startDirection + 1 : startDirection;
		} else {
			startDirection = 1;
		}
		for (int i = 0; i < 8; i += 2) {
			startDirection = (startDirection + i) % 9;
			int kidX = (startDirection % 3) - 1 + x;
			kidX = kidX < 0 ? World.getWidth() - 1 : kidX >= World.getWidth() ? 0 : kidX;
			int kidY = (startDirection / 3) - 1 + y;
			if (World.getWorldObject(kidX, kidY) == null) {
				createColonyMate(kidX, kidY, kidOrganic, kidMinerals);
				return SUCCESS;
			}
		}
		return INVALID_PARAM;
	}

	private void createColonyMate(int kidX, int kidY, int kidOrganic, int kidMinerals) throws Exception {
		int mateIndex = (kidY == y - 1) ? 0 : (kidX == x + 1) ? 1 : (kidY == y + 1) ? 2 : 3;
		LiveCell kid = createNewCell(kidX, kidY, kidOrganic, kidMinerals);
		mates[mateIndex] = kid;
		updateColonyStatus();
		kid.mates[(mateIndex + 2) % 4] = this;
		kid.updateColonyStatus();
	}



	private LiveCell createNewCell(int x, int y, int kidOrganic, int kidMinerals) throws Exception {
		organic -= DIVISION_COST + kidOrganic;
		minerals -= DIVISION_MINERALS_COST + kidMinerals;
		World.addPoop(DIVISION_MINERALS_COST);
		int random = (int) (System.nanoTime() % (Species.MUTATION_FACTOR + 1));
		LiveCell kid;
		if (random > 1) {
			kid = new LiveCell(species, x, y, kidMinerals, kidOrganic);
		} else {
			Species mutatedSpecies = species.mutate();
			kid = new LiveCell(mutatedSpecies, x, y, kidMinerals, kidOrganic);
		}
		kid.setCurrentDirection(currentDirection);
		return kid;
	}

	private void photosynthesis() {
		if (organic > MAX_ORGANIC) return;
		WorldCell c = getMyCell();
		int light = (int) Math.round(0.80 * c.getLight() / (World.WATER_OPACITY - organic * World.CELL_SHADOW_Q));
		light /= 6;
		double bonus = 0.4;
		for (int i = 0; i < mates.length; i++) {
			if (mates[i] != null) bonus += 0.5;
		}
		bonus += minerals / 1500.0;
		int gain = 0;
		gain += light * bonus;
		organic += gain;
		totalGained += gain;
		photoGained += gain;
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

	private boolean isSurrounded() {
		for (int i = 0; i < 9; i++) {
			int yc = i / 3;
			int xc = i % 3;
			if (xc == 0 && yc == 0) continue;
			xc = x + xc - 1;
			yc = y + yc - 1;
			if (World.getWorldObject(xc, yc) == null) return false;
		}
		return true;
	}

	private void passiveConsumeMinerals() {
		if (minerals > MAX_MINERALS || isDead) return;
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
//		World.updateTotalMinerals();
//		System.out.println("passiveConsumeMinerals " + World.getMineralsChange());
	}

	private void basicMetabolism() {
		//basic metabolism first
		organic -= BASIC_COST;
		minerals -= BASIC_MINERALS_COST;
		//add poop to the world poop bank
		World.addPoop(BASIC_MINERALS_COST);
	}

	public void updateColonyStatus() {
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
		if (y >= 0 && y < World.getHeight())
			this.y = y;
	}

	@Override
	public Color getColor() {
		if (totalGained == 0) return Color.hsb(prevHue, S, B);
		double hue = (PREDATOR_HUE * predatorGained * 2 + PLANT_HUE * photoGained + CHEM_HUE * chemGained * 2) / (double) totalGained;
		prevHue = hue;
		return Color.hsb(hue, S, B);
	}

	@Override
	public double getOpacity() {
		return organic * World.CELL_SHADOW_Q;
	}

	@Override
	public boolean isDeleted() {
		return isDead;
	}

	@Override
	public int getColonyStatus() {
		return (int) colonyStatus;
	}

	@Override
	public String toString() {
		return species.getName();
	}
}
