package graphics;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import world.WorldCell;

public class MegaTile extends StackPane {
	public enum geometry {BORDER, NO_BORDER, INNER_RECTANGLE, COLONY}
	private WorldCell worldCell;
	private Rectangle water, object;
	private Line borderTop, borderRight, borderBottom, borderLeft, colonyTop, colonyRight, colonyBottom, colonyLeft;
	private double border = 0;
	private Paint borderColor = Color.BLACK;
	private int fieldX, fieldY;
	private static double widthAndHeight;

	public static MegaTile newTile(int fieldX, int fieldY){return new MegaTile(fieldX, fieldY);}
	public static void setWidthAndHeight(double widthAndHeight){
		MegaTile.widthAndHeight = widthAndHeight;
	}

	private MegaTile (int fieldX, int fieldY){
		this.fieldX = fieldX;
		this.fieldY = fieldY;
		setWidth(widthAndHeight);
		setHeight(widthAndHeight);
		setTranslateX(widthAndHeight*fieldX);
		setTranslateY(widthAndHeight*fieldY);
		water = new Rectangle(widthAndHeight,widthAndHeight);
		object = new Rectangle();

		//border top
		borderTop = new Line();
		borderTop.setStartX(0);
		borderTop.setStartY(0);
		borderTop.setEndX(widthAndHeight-1);
		borderTop.setEndY(0);
		borderTop.setStrokeWidth(1);

		//border right
		borderTop = new Line();
		borderTop.setStartX(widthAndHeight-1);
		borderTop.setStartY(0);
		borderTop.setEndX(widthAndHeight-1);
		borderTop.setEndY(widthAndHeight-1);
		borderTop.setStrokeWidth(1);
	}

}
