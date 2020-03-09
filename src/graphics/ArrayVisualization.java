package graphics;

import javafx.scene.paint.Color;
import world.World;
import world.WorldCell;
import world.WorldObject;

public class ArrayVisualization {
	private static final double DEFAULT_VIEW_BRIGHTNESS = 1500d;
	private static final double LIGHT_VIEW_BRIGHTNESS = 1200d;
	private static final Color WATER_COLOR = Color.web("#0095ff");
	private static final Color LIGHT_COLOR = Color.YELLOW;
	private static final Color CELL_COLOR_IN_LIGHT_VIEW = Color.GREEN;
	private static final Color MINERALS_COLOR = Color.hsb(180,1, 1);
	public static final double CELL_OPACITY_Q = 1 / 1500d;


	public static void paintLight(Tile[][] field){
		final int cols = World.getWidth();
		final int rows = World.getHeight();

		Tile.setEmptyGeometry(Tile.Geometry.NO_BORDER);
		Tile.setFullGeometry(Tile.Geometry.BORDER);

		for(int y= 0; y < rows; y++){
			for(int x = 0; x < cols; x++){
				Tile tile = field[y][x];
				WorldCell content = tile.getWorldCell();
				WorldObject worldObject = content.getWorldObject();
				tile.setBorderColor(Color.YELLOWGREEN);
				tile.setObjectBorderColor(Color.YELLOWGREEN);
				double brightnessFactor = content.getLight() / LIGHT_VIEW_BRIGHTNESS;
				Color light = LIGHT_COLOR.deriveColor(0d, 1d, brightnessFactor, 1d);
				tile.setWaterColor(light);
				if (worldObject != null) {
					double opacity = (double) worldObject.getOrganic() * CELL_OPACITY_Q;
					Color worldObjectColor = overlayColors(CELL_COLOR_IN_LIGHT_VIEW .deriveColor(0d, 1d, 1d, opacity), light);
					tile.setObjectColor(worldObjectColor);
				}
				tile.draw();
		}


	}
	}

	public static void paintDefault(Tile[][] field){
		final int cols = World.getWidth();
		final int rows = World.getHeight();

		Tile.setEmptyGeometry(Tile.Geometry.NO_BORDER);
		Tile.setFullGeometry(Tile.Geometry.BORDER);

		for(int y = 0; y < rows; y++){
			for(int x = 0; x <cols; x++){
				Tile tile = field[y][x];

				tile.setBorderColor(Color.SILVER);
				tile.setObjectBorderColor(Color.SILVER);

				WorldCell content = tile.getWorldCell();
				WorldObject worldObject = content.getWorldObject();
				double brightnessFactor = content.getLight() / DEFAULT_VIEW_BRIGHTNESS;
				Color light = WATER_COLOR.deriveColor(0, 1d, brightnessFactor, 1d);
				tile.setWaterColor(light);
				if(worldObject != null){
					Color objectBasicColor = worldObject.getColor();
					double opacity = (double) worldObject.getOrganic() * CELL_OPACITY_Q;
					Color fillColor = objectBasicColor.deriveColor(0d, opacity, 1.5 - (opacity * 0.5), 1d);
					tile.setObjectColor(fillColor);
				}
				tile.draw();
			}
		}
	}

	public static void paintMinerals(Tile[][] field){
		final int cols = World.getWidth();
		final int rows = World.getHeight();

		Tile.setEmptyGeometry(Tile.Geometry.NO_BORDER);
		Tile.setFullGeometry(Tile.Geometry.OBJECT_WATER_BORDER);

		for(int y = 0; y < rows; y++){
			for(int x = 0; x <cols; x++){
				Tile tile = field[y][x];
				WorldCell content = tile.getWorldCell();
				tile.setBorderColor(Color.SILVER);
				tile.setObjectBorderColor(Color.SILVER);

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
				}
				tile.setWaterColor(fill);
				if(worldObject != null){
					int cellMinerals = worldObject.getMinerals();
					if(cellMinerals == 0){
						tile.setObjectColor(Color.WHITE);
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
						tile.setObjectColor(MINERALS_COLOR.deriveColor(hueShift, saturation, brightness, 1));
					}
				}
				tile.draw();
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
