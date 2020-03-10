package world.evolution;

import javafx.scene.paint.Color;
import world.World;
import world.WorldObject;

public class DeadColonyCell implements WorldObject {
	private int organic, minerals, x, y;
	private boolean isDead = false;
	public WorldObject[] mates = new WorldObject[4];
	private byte colonyStatus = 0;
	private int matesCount = 0;
	private static int counter;
	public final int id;

	public DeadColonyCell(int x, int y, int organic, int minerals, WorldObject[] mates) throws Exception {
		this.x = x;
		this.y = y;
		this.minerals = minerals;
		this.mates = mates;
		this.organic = organic;
		updateColonyStatus();
		World.addWorldObject(this);
		id = ++counter;
		for (int i = 0; i < 4; i++) {
			WorldObject mate = mates[i];
			if (mate != null) {
				if (mate instanceof LiveCell) ((LiveCell) mate).mates[(i + 2) % 4] = this;
				if (mate instanceof DeadColonyCell) ((DeadColonyCell) mate).mates[(i + 2) % 4] = this;
			}
		}
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

	}

	@Override
	public void consumeMinerals(WorldObject food) {

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
		removeFromColony();
		World.getCell(x, y).addMinerals(minerals);
		minerals = 0;
	}

	private void removeFromColony() {
		for (int i = 0; i < 4; i++) {
			WorldObject mate = mates[i];
			if (mate != null) {
				if (mate instanceof LiveCell) {
					((LiveCell) mate).mates[(i + 2) % 4] = null;
					((LiveCell) mate).updateColonyStatus();
				}
				if (mate instanceof DeadColonyCell) {
					((DeadColonyCell) mate).mates[(i + 2) % 4] = null;
					((DeadColonyCell) mate).updateColonyStatus();
				}
			}
		}
	}

	@Override
	public void live() throws Exception {
		updateColonyStatus();
		if (organic < matesCount || colonyStatus == 0){ die(); return;}
		int organicPart = organic/matesCount;
		int mineralsPart = minerals/matesCount;
		for(int i = 0; i< 4; i++){
			if (mates[i] != null){
				mates[i].takeOrganic(-1*organicPart);
				mates[i].takeMinerals(-1*mineralsPart);
				organic -= organicPart;
				minerals -= mineralsPart;
			}
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
	}

	@Override
	public void setY(int y) {
	}

	@Override
	public Color getColor() {
		return Color.GRAY;
	}

	@Override
	public double getOpacity() {
		return organic * World.CELL_SHADOW_Q;
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	@Override
	public int getColonyStatus() {
		return (int) colonyStatus;
	}

	public void updateColonyStatus() {
		int status = 0;
		matesCount = 0;
		for (int i = 0; i < mates.length; i++) {
			if (mates[i] == null) {
				status = (status << 1) | 0;
			} else {
				status = (status << 1) | 1;
				matesCount++;
			}
		}
		colonyStatus = (byte) status;
	}
}
