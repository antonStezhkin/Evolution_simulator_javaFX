package graphics;

import javafx.scene.paint.Color;
import world.World;
import world.WorldCell;
import world.WorldObject;

public class ArrayVisualization {
	private static final double DEFAULT_VIEW_BRIGHTNESS = 1200d;
	private static final double LIGHT_VIEW_BRIGHTNESS = 1000d;
	private static final Color WATER_COLOR = Color.web("#0095ff");
	private static final Color LIGHT_COLOR = Color.YELLOW;
	private static final Color CELL_COLOR_IN_LIGHT_VIEW = Color.GREEN;
	private static final Color MINERALS_COLOR = Color.hsb(180,1, 1);
	public static final double CELL_OPACITY_Q = 1 / 1000d;


	public static void paintLight(Tile[][] field){
		final int cols = field[0].length;
		final int rows = field.length;
		for(int y= 0; y < rows; y++){
			for(int x = 0; x < cols; x++){
				Tile tile = field[y][x];
				WorldCell content = tile.getWorldCell();
				WorldObject worldObject = content.getWorldObject();
				double brightnessFactor = content.getLight() / LIGHT_VIEW_BRIGHTNESS;
				Color light = LIGHT_COLOR.deriveColor(0d, 1d, brightnessFactor, 1d);
				if (worldObject != null) {
					double opacity = (double) worldObject.getOrganic() * CELL_OPACITY_Q;
					Color worldObjectColor = overlayColors(CELL_COLOR_IN_LIGHT_VIEW .deriveColor(0d, 1d, 1d, opacity), light);
					tile.setFill(worldObjectColor);
					tile.setBorder(1, Color.YELLOWGREEN);
				}else{
					tile.setBorder(0);
					tile.setFill(light);
				}
			}
		}
	}

	public static void paintDefault(Tile[][] field){
		final int cols = field[0].length;
		final int rows = field.length;

		for(int y= 0; y < rows; y++){
			for(int x = 0; x < cols; x++){
				Tile tile = field[y][x];
				WorldCell content = tile.getWorldCell();
				WorldObject worldObject = content.getWorldObject();
				if(worldObject != null){
					Color objectBasicColor = worldObject.getColor();
					double opacity = (double) worldObject.getOrganic() * CELL_OPACITY_Q;
					Color fillColor = objectBasicColor.deriveColor(0d, opacity, 1.5 - (opacity * 0.5), 1d);
					tile.setBorder(0.5, Color.SILVER);
					tile.setFill(fillColor);
				}else{
					double brightnessFactor = content.getLight() / DEFAULT_VIEW_BRIGHTNESS;
					Color light = WATER_COLOR.deriveColor(0, 1d, brightnessFactor, 1d);
					tile.setFill(light);
					tile.setBorder(0);
				}
			}
		}
	}

	public static void paintMinerals(Tile[][] field){
		final int cols = field[0].length;
		final int rows = field.length;
		for(int y= 0; y < rows; y++){
			for(int x = 0; x < cols; x++){
				Tile tile = field[y][x];
				WorldCell content = tile.getWorldCell();
				WorldObject worldObject = content.getWorldObject();
				int minerals = content.getMinerals();
				Color fill = Color.WHITE;
				if(minerals > 0) {
					double hueShift = minerals - 40;
					double brightness = 1;
					double saturation = 1;
					if(hueShift > 72){
						hueShift = 72;
						brightness -= ((minerals - 72)/330d);
					}else if(hueShift < 0){
						hueShift = 0;
						saturation =  0.025*minerals;
					}
					fill = MINERALS_COLOR.deriveColor(hueShift, saturation, brightness, 1);
					//fill = overlayColors(fill, Color.WHITE);
				}
				if(worldObject == null){
//                    tile.setBorder(0);
					if(minerals > 0){tile.setBorder(0); tile.setBorder(0.5, Color.WHITE);}
					else{tile.setBorder(0.5, Color.SILVER);}
					tile.setFill(fill);
				}else{
					tile.setBorder(2, fill);
					int cellMinerals = worldObject.getMinerals();
					if(cellMinerals == 0){
						tile.setFill(Color.WHITE);
					}else{
						double hueShift = worldObject.getMinerals() - 40;
						double brightness = 1;
						double saturation = 1;
						if(hueShift > 72){
							hueShift = 72;
							brightness -= ((worldObject.getMinerals() - 82)/330d);
						}else if(hueShift < 0){
							hueShift = 0;
							saturation =  0.025*worldObject.getMinerals();
						}
						Color fill1 = MINERALS_COLOR.deriveColor(hueShift, saturation, brightness, 1);
						tile.setFill(fill1);
					}

				}
			}
		}
	}

	public static void setInitialDimensions(Tile[][] field, int fieldWidth, int fieldHeight){
		int cols = field[0].length;
		int rows = field.length;
		int width = fieldWidth / cols;
		int height = fieldHeight / rows;

		System.out.println(fieldHeight);
		System.out.println(height);

		for(int y= 0; y < rows; y++){
			int offsetY = y*height;
			for(int x = 0; x < cols; x++){
				int offsetX = x*width;
				Tile cell = field[y][x];
				cell.setHeight(height);
				cell.setWidth(width);
				cell.setX(offsetX);
				cell.setY(offsetY);
			}
		}
	}

	private static Color overlayColors(Color overlay, Color underlay) {
		double overlayAlpha = overlay.getOpacity() * 255d;
		double overlayRed = overlay.getRed();
		double overlayGreen = overlay.getGreen();
		double overlayBlue = overlay.getBlue();

		double underlayAlpha = underlay.getOpacity() * 255d;
		double underlayRed = underlay.getRed();
		double underlayGreen = underlay.getGreen();
		double underlayBlue = underlay.getBlue();

		double q = (1 - overlayAlpha / 255d) * underlayAlpha;
		double resultAlpha = q + overlayAlpha;
		double resultRed = (q * underlayRed + overlayAlpha * overlayRed) / resultAlpha;
		double resultGreen = (q * underlayGreen + overlayAlpha * overlayGreen) / resultAlpha;
		double resultBlue = (q * underlayBlue + overlayAlpha * overlayBlue) / resultAlpha;

		return Color.color(resultRed, resultGreen, resultBlue, resultAlpha / 255d);
	}
}
