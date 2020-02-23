import graphics.ArrayVisualization;
import graphics.Display;
import graphics.Tile;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import world.World;

import static javafx.application.Application.launch;

public class App  extends Application {
	//DEFAULT SETTINGS
	private Tile[][] field;
	private static final int MENU_WIDTH = 100;
	private Display currentDisplay = Display.DEFAULT;

	private AnimationTimer globalTimer;
	private boolean isRunning = false;
	private GridPane root;
	private Pane fieldContainer = new Pane();
	private Pane controls = new VBox(5);
	private TextPanel text = new TextPanel(new Text());

	private void init(Stage primaryStage, int width, int height) {
		createStage(primaryStage, width, height);
		initField();
		initMenu();
	}

	private void initField() {
		field = new Tile[World.getHeight()][World.getWidth()];
		for (int y = 0; y < World.getHeight(); y++) {
			for (int x = 0; x < World.getWidth(); x++) {
				Tile cell = new Tile(World.getWorldMatrix()[y][x], x, y);
				field[y][x] = cell;
				fieldContainer.getChildren().add(cell);
				cell.setOnMouseEntered(event -> {
					if (isRunning) return;
					text.setText(cell.getLegend(currentDisplay));
					text.setVisible(true);
					text.setTranslateX(cell.getTranslateX() + ((cell.getFieldX() > 99)? (-1*cell.getWidth() - text.getRWidth()) :20));
					text.setTranslateY(cell.getTranslateY() + ((cell.getFieldY() > 79)? (-1*cell.getHeight() - text.getRHeight()) :20));
				});
				cell.setOnMouseExited(event -> {
					text.setVisible(false);
					if (isRunning) return;
					text.setText("");
				});
			}
		}
		fieldContainer.getChildren().add(text);
		text.setVisible(false);
		ArrayVisualization.setInitialDimensions(field, (int) fieldContainer.getPrefWidth(), (int) fieldContainer.getPrefHeight());
		ArrayVisualization.paintDefault(field);
		currentDisplay = Display.DEFAULT;

	}

	private void initMenu() {
		globalTimer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				World.step();
				showCurrentView();
			}
		};
		Button playPause = new Button("play");
		controls.getChildren().add(playPause);
		controls.setTranslateX(10);

		playPause.setOnAction((ActionEvent event) -> {
			if (!isRunning) {
				globalTimer.start();
				playPause.setText("pause");
			} else {
				globalTimer.stop();
				playPause.setText("play");
			}
			isRunning = isRunning ? false : true;
		});

		ToggleGroup displayView = new ToggleGroup();
		RadioButton defaultViewRadio = new RadioButton("default");
		RadioButton lightViewRadio = new RadioButton("light");
		RadioButton mineralsViewRadio = new RadioButton("minerals");

		defaultViewRadio.setUserData(Display.DEFAULT);
		lightViewRadio.setUserData(Display.LIGHT);
		mineralsViewRadio.setUserData(Display.MINERALS);

		defaultViewRadio.setToggleGroup(displayView);
		lightViewRadio.setToggleGroup(displayView);
		mineralsViewRadio.setToggleGroup(displayView);
		defaultViewRadio.setSelected(true);

		displayView.selectedToggleProperty().addListener(event -> {
			currentDisplay = (Display) displayView.getSelectedToggle().getUserData();
			if (!isRunning) showCurrentView();
		});

		controls.getChildren().addAll(defaultViewRadio, lightViewRadio, mineralsViewRadio);
	}

	private void showCurrentView() {
		switch (currentDisplay) {
			case LIGHT:
				ArrayVisualization.paintLight(field);
				break;
			case MINERALS:
				ArrayVisualization.paintMinerals(field);
				break;
			default:
				ArrayVisualization.paintDefault(field);
				break;
		}
	}

	private void createStage(Stage stage, int width, int height) {
		int stageWidth = width + MENU_WIDTH;
		root = new GridPane();
		root.setHgap(1);
		RowConstraints bottomConstraints = new RowConstraints();
		bottomConstraints.setVgrow(Priority.ALWAYS);
		root.getRowConstraints().add(bottomConstraints);
		ColumnConstraints leftColumnSettings = new ColumnConstraints(100);
		leftColumnSettings.setHgrow(Priority.NEVER);
		root.getColumnConstraints().add(leftColumnSettings);
		GridPane leftColumn = new GridPane();
		leftColumn.setValignment(leftColumn, VPos.TOP);
		//leftColumn.add(text, 0,0);
		leftColumn.add(controls, 0, 0);
		controls.setTranslateY(10);
		root.setValignment(leftColumn, VPos.TOP);

		fieldContainer.setId("field-container");
		fieldContainer.setPrefWidth(width);
		fieldContainer.setPrefHeight(height);
		root.add(fieldContainer, 1, 0);
		root.add(leftColumn, 0, 0);

		stage.setTitle("Visualisation");
		Scene scene = new Scene(root, stageWidth, height, Color.WHITE);
//		scene.getStylesheets().add(this.getClass().getResource("/css/app.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		init(primaryStage, 1100, 900);
	}

	public static void main(String[] args) {
		launch(args);
	}

	public TextPanel getText() {
		return text;
	}

	public boolean isRunning() {
		return isRunning;
	}

	private class TextPanel extends StackPane {
		private Text text;
		Rectangle r;

		TextPanel(Text text) {
			this.text = text;
			r = new Rectangle();
			r.setFill(Color.WHITE);
			setAlignment(Pos.TOP_LEFT);
			getChildren().addAll(r, text);
			r.setEffect(new DropShadow());
			text.setTranslateX(7);
			text.setTranslateY(5);
		}

		public double getRHeight(){
			return r.getHeight();
		}
		public double getRWidth(){
			return r.getWidth();
		}

		public void setText(String legend) {
			text.setText(legend);
			r.setHeight(text.getBoundsInLocal().getHeight()+10);
			r.setWidth(text.getBoundsInLocal().getWidth()+14);
			setHeight(text.getBoundsInLocal().getHeight()+10);
			setWidth(text.getBoundsInLocal().getWidth()+14);
		}
	}
}
