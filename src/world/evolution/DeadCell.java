package world.evolution;

import javafx.scene.paint.Color;
import world.World;
import world.WorldCell;
import world.WorldObject;

public class DeadCell implements WorldObject {
	private static int counter;
	private int id;
	private int step = 0;

	public Color getColor() {
		return Color.GRAY;
	}

	private static final int MINERAL_RELEASE_MAX = 20;
	private int maxMinerals;
	private int x, y, organic, minerals;
	private volatile boolean isDead = false;

	public boolean isFuckedUp;
	@Override
	public String toString() {
		return isFuckedUp? "["+x+","+y+"] "+ isDead + " fuckedUp":"dead cell №" + id;
	}

	public DeadCell(int x, int y, int organic, int minerals) {
		this.x = x;
		this.y = y;
		this.organic = organic;
		this.minerals = minerals;
		maxMinerals = organic/10;
		World.addWorldObject(x, y, this);
		id = ++counter;
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
	}

	@Override
	public void consumeMinerals(WorldObject food) {
	}

	@Override
	public void eat(WorldObject food) {
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
		maxMinerals = organic/10;
		if (organic < 1) die();
		return o;
	}

	@Override
	public void die() {
		isDead = true;
		World.removeWorldObject(this);
		World.getCell(x,y).addMinerals(minerals);
		minerals = 0;
	}

	@Override
	public void live() {
		WorldCell c = World.getCell(x,y);
		int outerMinerals = c.getMinerals();
		int s = (minerals + outerMinerals) / 2;
		int delta = minerals - s;
		delta = (delta > MINERAL_RELEASE_MAX) ? MINERAL_RELEASE_MAX : (delta < -1 * MINERAL_RELEASE_MAX) ? -1 * MINERAL_RELEASE_MAX : delta;
		if (minerals-delta >= maxMinerals){
//			c.addMinerals(minerals - maxMinerals);
//			minerals = maxMinerals;
			delta += minerals - maxMinerals;
			delta = (minerals-delta > maxMinerals)? 0 : (minerals-delta < 0)? minerals : delta;
		}
		minerals -= delta;
		c.addMinerals(delta);
		if(step > 4) {
			int nY = y + 1;
			if (World.getWorldObject(x, nY) == null) {
				World.moveWorldObject(x, nY, this);
			}
			step = 0;
		}else {
			step++;
		}
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
	public double getOpacity() {
		return organic * World.CELL_SHADOW_Q;
	}

	@Override
	public boolean isAlive() {
		return !isDead;
	}
}
