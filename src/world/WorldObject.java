package world;

import javafx.scene.paint.Color;

public interface WorldObject {
	int getOrganic();
	int getMinerals();
	void consumeOrganic(WorldObject food) throws Exception;
	void consumeMinerals(WorldObject food);
	//void eat(WorldObject food) throws Exception;
	int takeMinerals(int amount);
	int takeOrganic(int amount) throws Exception;
	void die() throws Exception;
	void live() throws Exception;
	int getX();
	int getY();
	void setX(int x);
	void setY(int y);
	Color getColor();
	double getOpacity();
	boolean isDeleted();
}
