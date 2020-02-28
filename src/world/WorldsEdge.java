package world;

import javafx.scene.paint.Color;

public enum WorldsEdge implements WorldObject {
	BOTTOM, SKY;
	@Override
	public int getOrganic() {return 0;}

	@Override
	public int getMinerals() {return this == BOTTOM? 100 : 0;}

	@Override
	public void consumeOrganic(WorldObject food) {}

	@Override
	public void consumeMinerals(WorldObject food) {}

	@Override
	public int takeMinerals(int amount) {return 0;}

	@Override
	public int takeOrganic(int amount) {return 0;}

	@Override
	public void die() {}

	@Override
	public void live() {}

	@Override
	public int getX() {return 0;}

	@Override
	public int getY() {return 0;}

	@Override
	public void setX(int x) {

	}

	@Override
	public void setY(int y) {

	}

	@Override
	public Color getColor() {return this == SKY? Color.DEEPSKYBLUE : Color.SILVER;}

	@Override
	public double getOpacity() {return 0;}

	@Override
	public boolean isDeleted() {return false;}
}
