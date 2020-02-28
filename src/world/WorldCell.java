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

	public synchronized WorldObject getWorldObject() {
		if(worldObject == null) return null;
		if(worldObject.isDeleted()){
			worldObject = null;
			return null;
		}
		return worldObject;
	}

	public synchronized void setWorldObject(WorldObject worldObject) throws Exception {
		if(this.worldObject != null && !this.worldObject.isDeleted()) throw new Exception("I'm still alive!");
		this.worldObject = worldObject;
	}

	/**
	 * Removes and returns WorldObject;
	 * @return WorldObject;
	* */
	public synchronized WorldObject takeWorldObject(){
		WorldObject r = this.worldObject;
		worldObject = null;
		return r;
	}

	public double getOpacity() {
		return worldObject == null ? World.WATER_OPACITY : World.WATER_OPACITY - worldObject.getOpacity();
	}
}
