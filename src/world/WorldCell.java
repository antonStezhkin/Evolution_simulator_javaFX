package world;

public class WorldCell {
	private int light;
	private int minerals;
	private WorldObject worldObject;

	public int getLight() {
		return light;
	}

	public void setLight(int light) {
		this.light = light;
	}

	public int getMinerals() {
		return minerals;
	}

	public void setMinerals(int minerals) {
		this.minerals = minerals;
	}
	public void addMinerals(int amount){minerals += amount;}

	public WorldObject getWorldObject() {
		return worldObject;
	}

	public void setWorldObject(WorldObject worldObject) {
		this.worldObject = worldObject;
	}

	public double getOpacity() {
		return worldObject == null ? World.WATER_OPACITY : World.WATER_OPACITY - worldObject.getOpacity();
	}
}
