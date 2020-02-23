package world;

import javafx.scene.paint.Color;

public interface WorldObject {
	int getOrganic();
	int getMinerals();
	void consumeOrganic(WorldObject food);
	void consumeMinerals(WorldObject food);
	void eat(WorldObject food);
	int takeMinerals(int amount);
	int takeOrganic(int amount);
	void die();
	void live();
	int getX();
	int getY();
	Color getColor();
	double getOpacity();
	boolean isAlive();
}
