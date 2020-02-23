package graphics;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import world.WorldCell;
import world.WorldObject;

public class Tile extends StackPane {
	private WorldCell cell;
	private Rectangle bg, overlay;
	private double border = 0;
	private Paint borderColor = Color.BLACK;
	private int fieldX, fieldY;

	public int getFieldX() {
		return fieldX;
	}

	public int getFieldY() {
		return fieldY;
	}

	public String getLegend(Display display) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("X:%d, Y:%d", fieldX, fieldY));
		switch (display) {
			default: break;
			case LIGHT:
				sb.append(System.lineSeparator());
				sb.append("light: " + cell.getLight());
				break;
			case MINERALS:
				sb.append(System.lineSeparator());
				sb.append("minerals: " + cell.getMinerals());
				break;
		}
		WorldObject wo = cell.getWorldObject();
		if (cell.getWorldObject() != null) {
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

	public WorldCell getWorldCell() {
		return cell;
	}

	public void setWidth(double width) {
		super.setWidth(width);
		overlay.setWidth(width - 2 * border);
		bg.setWidth(width);
	}

	public void setHeight(double height) {
		super.setHeight(height);
		overlay.setHeight(height - 2 * border);
		bg.setHeight(height);
	}

	public void setFill(Paint color) {
		overlay.setFill(color);
	}


	public void setBorder(double border, Paint color) {
		this.border = border;
		borderColor = color;
		overlay.setX(border);
		overlay.setY(border);
		overlay.setWidth(bg.getWidth() - 2 * border);
		overlay.setHeight(bg.getHeight() - 2 * border);
		bg.setFill(borderColor);
	}

	public void setBorder(double border) {
		setBorder(border, borderColor);
	}

	public void setBorder(Paint color) {
		border = border > 0 ? border : 1;
		setBorder(border, color);
	}

	public Tile(WorldCell cell, int fieldX, int fieldY) {
		this.cell = cell;
		this.fieldX = fieldX;
		this.fieldY = fieldY;
		bg = new Rectangle();
		bg.setWidth(getWidth());
		bg.setHeight(getHeight());

		overlay = new Rectangle();
		overlay.setWidth(getWidth());
		overlay.setHeight(getHeight());

		getChildren().addAll(bg, overlay);
	}

	public void setX(double x) {
		this.setTranslateX(x);
	}

	public void setY(double y) {
		this.setTranslateY(y);
	}
}