package world.evolution;

import javafx.scene.paint.Color;
import world.World;
import world.WorldCell;
import world.WorldObject;

public class DeadCell implements WorldObject {
	private static int counter;
	private int id;

	public Color getColor() {
		return Color.DARKMAGENTA;
	}

	private static final int MINERAL_RELEASE_MAX = 20;
	private static int maxMinerals;
	private int x, y, organic, minerals;
	private boolean isDead = false;

	@Override
	public String toString() {
		return "dead cell №" + id;
	}

	public DeadCell(int x, int y, int organic, int minerals) {
		this.x = x;
		this.y = y;
		this.organic = organic;
		this.minerals = minerals;
		maxMinerals = organic/13;
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
		maxMinerals = organic/13;
		if (organic < 1) die();
		return o;
	}

	@Override
	public void die() {
		World.removeWorldObject(this);
		isDead = true;
	}

	@Override
	public void live() {
		WorldCell c = World.getWorldMatrix()[y][x];
		int outerMinerals = c.getMinerals();
		int s = (minerals + outerMinerals) / 2;
		int delta = minerals - s;
		delta = (delta > MINERAL_RELEASE_MAX) ? MINERAL_RELEASE_MAX : (delta < -1 * MINERAL_RELEASE_MAX) ? -1 * MINERAL_RELEASE_MAX : delta;
		if(delta < 0 && maxMinerals <= maxMinerals) {
			minerals -= delta;
			c.addMinerals(delta);
		}if (minerals > maxMinerals){
			c.addMinerals(minerals - maxMinerals);
			minerals = maxMinerals;
		}
		int nY = y + 1;
		if (World.getWorldObject(x, nY) == null) {
			World.moveWorldObject(x, nY, this);
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
