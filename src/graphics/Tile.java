package graphics;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import world.World;
import world.WorldCell;
import world.WorldObject;

public class Tile extends StackPane {
	public enum Geometry {BORDER, NO_BORDER, OBJECT_WATER, OBJECT_WATER_BORDER}
	private WorldCell worldCell;
	private Rectangle water, object;
	private Line borderTop, borderRight, borderBottom, borderLeft, colonyTop, colonyRight, colonyBottom, colonyLeft;
	private int fieldX, fieldY;
	private static double widthAndHeight;
	private static Geometry emptyGeometry;
	private static Geometry fullGeometry;
	private Paint objectColor = Color.SILVER;
	private Paint waterColor = Color.AQUA;
	private Paint borderColor = Color.BLACK;
	private Paint objectBorderColor = Color.BLACK;

	public static Tile newTile(int fieldX, int fieldY){return new Tile(fieldX, fieldY);}
	public static void setWidthAndHeight(double widthAndHeight){
		Tile.widthAndHeight = widthAndHeight;
	}

	public static void setEmptyGeometry(Geometry emptyGeometry){
		Tile.emptyGeometry = emptyGeometry;
	}
	public static void setFullGeometry(Geometry emptyGeometry){
		Tile.fullGeometry = emptyGeometry;
	}

	public synchronized void draw(){
		WorldObject wo = worldCell.getWorldObject();
		if(wo == null){
			object.setVisible(false);
			water.setVisible(true);
			water.setFill(waterColor);
			colonyTop.setVisible(false);
			colonyLeft.setVisible(false);
			colonyRight.setVisible(false);
			colonyBottom.setVisible(false);

			switch (emptyGeometry){
				case BORDER:
					borderLeft.setVisible(true);
					borderRight.setVisible(true);
					borderBottom.setVisible(true);
					borderTop.setVisible(true);
					borderLeft.setStroke(borderColor);
					borderRight.setStroke(borderColor);
					borderBottom.setStroke(borderColor);
					borderTop.setStroke(borderColor);

					break;
				case NO_BORDER:
					borderLeft.setVisible(false);
					borderRight.setVisible(false);
					borderBottom.setVisible(false);
					borderTop.setVisible(false);
					break;
				default:
					break;
			}
		}
		else{
			int colonyStatus = wo.getColonyStatus();
			switch (fullGeometry){
				case BORDER:
					borderLeft.setStroke(borderColor);
					borderRight.setStroke(borderColor);
					borderBottom.setStroke(borderColor);
					borderTop.setStroke(borderColor);

					borderLeft.setVisible(true);
					borderRight.setVisible(true);
					borderBottom.setVisible(true);
					borderTop.setVisible(true);

					if(colonyStatus == 0) {
						water.setFill(objectColor);
						object.setVisible(false);
						colonyBottom.setVisible(false);
						colonyRight.setVisible(false);
						colonyLeft.setVisible(false);
						colonyTop.setVisible(false);
						object.setVisible(false);
					}else{
						water.setFill(waterColor);
						object.setFill(objectColor);
						object.setVisible(true);
						showBinds(colonyStatus);
					}
					break;
				case OBJECT_WATER_BORDER:
					borderLeft.setVisible(true);
					borderRight.setVisible(true);
					borderBottom.setVisible(true);
					borderTop.setVisible(true);

					borderLeft.setStroke(borderColor);
					borderRight.setStroke(borderColor);
					borderBottom.setStroke(borderColor);
					borderTop.setStroke(borderColor);
					object.setStroke(objectBorderColor);

					object.setVisible(true);
					object.setFill(objectColor);
					showBinds(colonyStatus);
					water.setVisible(true);
					water.setFill(waterColor);
					break;
				case OBJECT_WATER:
					borderLeft.setVisible(false);
					borderRight.setVisible(false);
					borderBottom.setVisible(false);
					borderTop.setVisible(false);

					borderTop.setVisible(false);
					object.setVisible(true);
					object.setFill(objectColor);
					object.setStroke(objectColor);
					showBinds(colonyStatus);
					water.setVisible(true);
					water.setFill(waterColor);
					break;
				case NO_BORDER:
					water.setFill(objectColor);
					object.setVisible(false);
					colonyBottom.setVisible(false);
					colonyRight.setVisible(false);
					colonyLeft.setVisible(false);
					colonyTop.setVisible(false);
					object.setVisible(false);

					borderLeft.setVisible(false);
					borderRight.setVisible(false);
					borderBottom.setVisible(false);
					borderTop.setVisible(false);
					break;
			}
		}
	}

	private void showBinds(int colonyStatus) {
		final int top = 1;
		final int right = 2;
		final int bottom = 4;
		final int left = 8;

		colonyTop.setStroke(objectColor);
		colonyLeft.setStroke(objectColor);
		colonyRight.setStroke(objectColor);
		colonyBottom.setStroke(objectColor);

		colonyTop.setVisible((top & colonyStatus) != 0);
		colonyLeft.setVisible((left & colonyStatus) != 0);
		colonyBottom.setVisible((bottom & colonyStatus) != 0);
		colonyRight.setVisible((right & colonyStatus) != 0);
	}


	public static void main(String[] args) {
		final int top = 1;
		final int right = 2;
		final int bottom = 4;
		final int left = 8;
		int z = Integer.parseInt("1001", 2);

		System.out.println((top & z) > 0);
		System.out.println((right & z) > 0);
		System.out.println((bottom & z) > 0);
		System.out.println((left & z) > 0);
	}

	private Tile (int fieldX, int fieldY){
		this.fieldX = fieldX;
		this.fieldY = fieldY;
		worldCell = World.getCell(fieldX, fieldY);
		setWidth(widthAndHeight);
		setHeight(widthAndHeight);
		setTranslateX((widthAndHeight)*fieldX);
		setTranslateY((widthAndHeight)*fieldY);

		emptyGeometry = Geometry.BORDER;
		fullGeometry = Geometry.OBJECT_WATER_BORDER;

		//RECTANGLES
		water = new Rectangle();
		water.setX(0);
		water.setY(0);
		water.setWidth(widthAndHeight);
		water.setHeight(widthAndHeight);
		water.setFill(Color.SILVER);
		object = new Rectangle();
		object.setStrokeWidth(1);
		object.setStroke(Color.DARKGREEN);
		double objWnH = 0.5*widthAndHeight;
		object.setTranslateY(-0.5);
		object.setTranslateX(-0.5);
		object.setHeight(objWnH);
		object.setWidth(objWnH);
		object.setFill(Color.DARKGREEN);

		//BORDER
		//border top
		borderTop = new Line(0,0, widthAndHeight, 0);
		borderTop.setTranslateY((widthAndHeight)/-2);
		borderTop.setStrokeWidth(1);
		borderTop.setStroke(Color.GRAY);

		//border right
		borderRight = new Line(widthAndHeight, 0, widthAndHeight, widthAndHeight);
		borderRight.setTranslateX(widthAndHeight/2);
		borderRight.setStrokeWidth(1);
		borderRight.setStroke(Color.GRAY);


		//border bottom
		borderBottom = new Line(0, widthAndHeight, widthAndHeight, widthAndHeight);
		borderBottom.setTranslateY(widthAndHeight/2);
		borderBottom.setStrokeWidth(1);
		borderBottom.setStroke(Color.GRAY);

		//border left
		borderLeft = new Line(0, 0, 0, widthAndHeight);
		borderLeft.setTranslateX(widthAndHeight/-2);
		borderBottom.setStrokeWidth(1);
		borderBottom.setStroke(Color.GRAY);

		//COLONY
		//top
		colonyTop = new Line(widthAndHeight/2, 0, widthAndHeight/2, widthAndHeight/2);
		colonyTop.setTranslateX(-0.5);
		colonyTop.setTranslateY(widthAndHeight/-4-1);
		colonyTop.setStroke(Color.DARKGREEN);
		colonyTop.setStrokeWidth(2);


		//left
		colonyRight = new Line(widthAndHeight/2, widthAndHeight/2-2, widthAndHeight, widthAndHeight/2-2);
		colonyRight.setTranslateY(-0.5);
		colonyRight.setTranslateX(widthAndHeight/4);
		colonyRight.setStroke(Color.DARKGREEN);
		colonyRight.setStrokeWidth(2);

		//bottom
		colonyBottom = new Line(widthAndHeight/2, widthAndHeight/2, widthAndHeight/2, widthAndHeight);
		colonyBottom.setTranslateX(-0.5);
		colonyBottom.setTranslateY(widthAndHeight/4);
		colonyBottom.setStroke(Color.DARKGREEN);
		colonyBottom.setStrokeWidth(2);

		//left
		colonyLeft = new Line(0, widthAndHeight/2, widthAndHeight/2, widthAndHeight/2);
		colonyLeft.setTranslateY(-0.5);
		colonyLeft.setTranslateX(widthAndHeight/-4-1);
		colonyLeft.setStroke(Color.DARKGREEN);
		colonyLeft.setStrokeWidth(2);

		getChildren().addAll(water,object,borderTop, borderRight, borderBottom, borderLeft, colonyTop, colonyRight,colonyBottom, colonyLeft);
	}

	public String getLegend(Display display) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("X:%d, Y:%d", fieldX, fieldY));
		switch (display) {
			default: break;
			case LIGHT:
				sb.append(System.lineSeparator());
				sb.append("light: " + worldCell.getLight());
				break;
			case MINERALS:
				sb.append(System.lineSeparator());
				sb.append("minerals: " + worldCell.getMinerals());
				break;
		}
		WorldObject wo = worldCell.getWorldObject();
		if (worldCell.getWorldObject() != null) {
			sb.append(System.lineSeparator());
			sb.append("------------");
			sb.append(System.lineSeparator());
			sb.append(wo);
			sb.append(System.lineSeparator());
			switch (display) {
				default:
					sb.append("organic: " + wo.getOrganic());
					break;
				case LIGHT:
					sb.append(String.format("opacity: %.3f", wo.getOpacity()));
					break;
				case MINERALS:
					sb.append("minerals: " + wo.getMinerals());
					break;
			}
		}
		return sb.toString();
	}

	public void setObjectColor(Paint objectColor) {
		this.objectColor = objectColor;
	}

	public void setWaterColor(Paint waterColor) {
		this.waterColor = waterColor;
	}

	public void setBorderColor(Paint borderColor) {
		this.borderColor = borderColor;
	}

	public void setObjectBorderColor(Paint objectBorderColor) {
		this.objectBorderColor = objectBorderColor;
	}

	public int getFieldX() {
		return fieldX;
	}

	public int getFieldY() {
		return fieldY;
	}

	public WorldCell getWorldCell() {
		return worldCell;
	}
}