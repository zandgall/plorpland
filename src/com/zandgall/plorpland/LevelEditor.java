/* zandgall

*----------------------------------------------------------------------------*
| THIS FILE IS MISCELLANEOUS                                                 |
| Although this file is being submitted as a part of the program, the content|
| of this file isn't related to the game. It serves as a subproject used to  |
| assist in designing the level for the game. It is messy and unorganized.   |
*----------------------------------------------------------------------------*

 ## Level Editor
 # An application built with the final project in order to edit and create levels

 : MADE IN NEOVIM */

package com.zandgall.plorpland;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JFileChooser;

import com.zandgall.plorpland.entity.Entity;
import com.zandgall.plorpland.entity.EntityRegistry;
import com.zandgall.plorpland.level.Level;
import com.zandgall.plorpland.level.Tile;
import com.zandgall.plorpland.util.Rect;

public class LevelEditor extends Main {

	private ArrayList<Entity> entityInstances = new ArrayList<Entity>();

	private double zoom = 1.0;
	private int tileX, tileY;
	private boolean selecting = false;
	private int selectX, selectY;
	private double entityX, entityY;
	private EditorEntity selectedEntity = null;

	private ArrayList<EditorEntity> entities = new ArrayList<EditorEntity>();

	private VBox editorRoot;

	/* Information */
	private Text mode;

	/* Tiles */
	private HBox tileRoot;
	private Canvas currentTileView;
	private GridPane tileOptionContainer;
	private double tileOptionOffset = 0;
	private ArrayList<Canvas> tileOptions;

	/* Entities */
	private HBox entityRoot;
	private Canvas currentEntityView;
	private HBox entityOptionContainer;
	private double entityOptionOffset = 0;
	private ArrayList<Canvas> entityOptions;

	@Override
	public void start(Stage stage) {
		super.start(stage);

		for (Class<?> e : EntityRegistry.classes)
			entityInstances.add(EntityRegistry.construct(e, 0, 0));

		updateEntityOptions();

		// level.getEntities().remove(player);
		level = new Level();

		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				Main.keys.put(event.getCode(), true);
				if (event.isShiftDown()) {
					if (!selecting) {
						selectX = tileX;
						selectY = tileY;
					}
					selecting = true;
				} else
					selecting = false;
				if (event.getCode() == KeyCode.MINUS)
					zoom = 0.25;
				if (event.getCode() == KeyCode.EQUALS)
					zoom = 1.0;
				if (event.getCode() == KeyCode.RIGHT)
					tileX++;
				if (event.getCode() == KeyCode.LEFT)
					tileX--;
				if (event.getCode() == KeyCode.DOWN)
					tileY++;
				if (event.getCode() == KeyCode.UP)
					tileY--;
				if (event.getCode() == KeyCode.S && event.isControlDown())
					try {
						save();
					} catch (IOException e) {
						e.printStackTrace();
						System.err.println("Could not save level");
					}
				if (event.getCode() == KeyCode.O && event.isControlDown())
					try {
						open();
					} catch (IOException e) {
						e.printStackTrace();
						System.err.println("Could not open level");
					}
				if (event.getCode() == KeyCode.T) {
					mode.setText("Tile mode");
					tileX = (int) entityX;
					tileY = (int) entityY;
				}
				if (event.getCode() == KeyCode.E) {
					mode.setText("Entity mode");
					entityX = tileX + 0.5;
					entityY = tileY + 0.5;
				}
				// If the user hits X, increment the ID of the selected tile
				if (mode.getText().equals("Tile mode")) {
					if (event.getCode() == KeyCode.X) {
						if (level.get(tileX, tileY) == null)
							level.put(tileX, tileY, Tile.get(1));
						else
							level.put(tileX, tileY, Tile.get(level.get(tileX, tileY).getID() + 1));
					}
					// Z decrements the selected tile ID
					if (event.getCode() == KeyCode.Z) {
						if (level.get(tileX, tileY) == null)
							level.put(tileX, tileY, Tile.get(-1)); // Tile.get has wrapping
						else
							level.put(tileX, tileY, Tile.get(level.get(tileX, tileY).getID() - 1));
					}
				}
				if (mode.getText().equals("Entity mode")) {
					if (event.getCode() == KeyCode.X) {
						if (selectedEntity == null) {
							selectedEntity = new EditorEntity(0, entityX, entityY);
							entities.add(selectedEntity);
						} else {
							selectedEntity.entity++;
							selectedEntity.entity %= entityInstances.size();
						}
					} else if (event.getCode() == KeyCode.Z) {
						if (selectedEntity == null) {
							selectedEntity = new EditorEntity(entityInstances.size() - 1, entityX, entityY);
							entities.add(selectedEntity);
						} else {
							selectedEntity.entity--;
							if (selectedEntity.entity < 0)
								selectedEntity.entity = entityInstances.size() - 1;
						}
					} else if (event.getCode() == KeyCode.C) {
						if (selectedEntity == null) {
							for (EditorEntity e : entities) {
								e.update();
								if (e.get().getRenderBounds().intersects(entityX - 0.1, entityY - 0.1, 0.2, 0.2)) {
									selectedEntity = e;
									entityX = e.x;
									entityY = e.y;
									entities.remove(e);
									entities.add(e); // move entity to end of list so it's lower priority to select next
														// time;
									break;
								}
							}
						} else
							selectedEntity = null;
					}
				}
			}
		});

		stage.heightProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal.doubleValue() <= 138)
				return;
			layer_0.setHeight(newVal.doubleValue() - 138);
			layer_1.setHeight(newVal.doubleValue() - 138);
			layer_2.setHeight(newVal.doubleValue() - 138);
			shadow_0.setHeight(newVal.doubleValue() - 138);
			shadow_1.setHeight(newVal.doubleValue() - 138);
		});

	}

	private void save() throws IOException {
		// Using swing just to use file dialogs
		JFrame frame = new JFrame();
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Save level as");
		int selection = chooser.showSaveDialog(frame);
		if (selection != JFileChooser.APPROVE_OPTION)
			return;
		File file = chooser.getSelectedFile();
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream s = new ObjectOutputStream(fos);

		// Version header
		// Currently: 1.2
		s.writeByte(1);
		s.writeByte(2);

		// Write the y range of the tiles in this level
		s.writeInt((int)level.bounds.y);
		s.writeInt((int)level.bounds.h);

		// Loop through every y value and write a line of tiles
		for (int y = (int)level.bounds.y; y <= level.bounds.y + level.bounds.h; y++) {
			boolean writing = false, wroteLineEnd = false;
			for (int x = (int)level.bounds.x; x <= level.bounds.x + level.bounds.w && !wroteLineEnd; x++) {
				if (level.get(x, y) == null || level.get(x,y).getID() == 0) {
					if (!writing)
						continue; // until we hit tiles

					// we wrote tiles and hit an empty tile, see if there are any more proper tiles
					// after this point
					boolean endOfLine = true;
					s.writeInt(0); // write empty tile
					System.out.println("Writing empty tile");
					for (int i = x; i < level.bounds.w; i++) {
						if (level.get(i, y) != null && level.get(i, y).getID() != 0) {
							endOfLine = false;
							s.writeInt((i - x) + 1); // write number of empty tiles in this line
							System.out.printf("Length %d%n", (i - x) + 1);
							x = i;
							break;
						}
					}
					// If we didn't hit any other tiles, it's the end of the line, write second 0
					if (endOfLine) {
						s.writeInt(0);
						System.out.printf("Didn't hit end at %d, %d, writing line%n", x, y);
						wroteLineEnd = true;
					}
				} else {
					if (!writing) {
						s.writeInt(x); // Write x position where this line actually starts
						writing = true;
						System.out.printf("Writing beginning x %d%n", x);
					}
					s.writeInt(level.get(x, y).getID());
				}
			}

			// If we didn't write anything, add 'int 0' for starting x, and 'int 0 int 0'
			// (equiv of newline)
			if (!writing) {
				s.writeInt(0);
				System.out.printf("We didn't write anything! Writing 0 for starting x, %b %b", writing, wroteLineEnd);
			}
			if (!wroteLineEnd) {
				s.writeInt(0);
				s.writeInt(0);
				System.out.println("Writing escape line feed");
			}
		}

		level.writeImage();

		// Write number of entities followed by that many entities
		s.writeInt(entities.size());
		for (EditorEntity e : entities) {
			s.writeUTF(EntityRegistry.reverseNameMap.get(entityInstances.get(e.entity).getClass()));
			s.writeDouble(e.x);
			s.writeDouble(e.y);
		}
		s.close();
		frame.dispose();
	}

	private void open() throws IOException {

		// Clear level
		level = new Level();

		// Using swing just to use file dialogs
		JFrame frame = new JFrame();
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Open level");
		int selection = chooser.showOpenDialog(frame);
		if (selection != JFileChooser.APPROVE_OPTION)
			return;
		File file = chooser.getSelectedFile();
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream s = new ObjectInputStream(fis);

		// Read version number
		byte major = s.readByte();
		byte minor = s.readByte();

		// read y range
		int minY = s.readInt();
		int height = s.readInt();

		for (int y = minY; y <= minY + height; y++) {
			int x = s.readInt();
			boolean reading = true;
			System.out.printf("Reading line %d starting at %d", y, x);
			while (reading) {
				int tile = s.readInt();
				if (tile == 0) {
					int spacing = s.readInt(); // number of empty spaces OR 0 = newline
					if (spacing == 0) {
						reading = false;
						System.out.printf(" to %d%n", x);
					} else {
						x += spacing;
						if(major == 1 && minor == 1)
							x++;
					}
				} else {
					level.put(x, y, Tile.get(tile));
					x++;
				}
			}
		}

		int numEntities = s.readInt();
		for (int i = 0; i < numEntities; i++) {
			entities.add(new EditorEntity(EntityRegistry.classes.indexOf(EntityRegistry.nameMap.get(s.readUTF())),
					s.readDouble(), s.readDouble()));
		}

		// Remove duplicates
		LinkedHashSet<EditorEntity> duplicates = new LinkedHashSet<>();
		for (int i = 0; i < entities.size(); i++)
			for (int j = i + 1; j < entities.size(); j++) {
				if (entities.get(i).equals(entities.get(j)))
					duplicates.add(entities.get(i));
			}
		for (EditorEntity a : duplicates)
			entities.remove(a);

		s.close();
		frame.dispose();
	}

	@Override
	public void setupScene() {
		// Run the same initialization process as Main
		root = new Pane();

		layer_0 = new Canvas(1280, 720);
		c0 = layer_0.getGraphicsContext2D();
		c0.setImageSmoothing(false);
		layer_1 = new Canvas(1280, 720);
		c1 = layer_1.getGraphicsContext2D();
		c1.setImageSmoothing(false);

		shadow_0 = new Canvas(1280, 720);
		s0 = shadow_0.getGraphicsContext2D();
		s0.setImageSmoothing(false);

		layer_2 = new Canvas(1280, 720);
		c2 = layer_2.getGraphicsContext2D();
		c2.setImageSmoothing(false);

		shadow_1 = new Canvas(1280, 720);
		s1 = shadow_1.getGraphicsContext2D();
		s1.setImageSmoothing(false);

		hudCanvas = new Canvas(1280, 720);
		hudContext = hudCanvas.getGraphicsContext2D();
		hudContext.setImageSmoothing(false);

		root.getChildren().add(layer_0);
		root.getChildren().add(layer_1);
		root.getChildren().add(shadow_0);
		root.getChildren().add(layer_2);
		root.getChildren().add(shadow_1);
		// Don't add HUD

		throwawayCanvas = new Canvas(0, 0);
		throwawayContext = throwawayCanvas.getGraphicsContext2D();

		// Create layout to include editor tools
		editorRoot = new VBox();

		/* Information */

		HBox infoRoot = new HBox();
		mode = new Text("Tile mode");
		infoRoot.getChildren().add(mode);

		editorRoot.getChildren().add(infoRoot);

		/* Tiles */
		setupTilesUI();
		tileRoot.getChildren().add(tileOptionContainer);
		editorRoot.getChildren().add(tileRoot);

		setupEntitiesUI();
		entityRoot.getChildren().add(entityOptionContainer);
		editorRoot.getChildren().add(entityRoot);

		editorRoot.getChildren().add(root);
		scene = new Scene(editorRoot, 1280, 976);
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void tick() {
		if (mode.getText().equals("Tile mode"))
			camera.target(tileX + 0.5, tileY + 0.5, zoom*Camera.DEFAULT_ZOOM);
		else {
			if (keys.get(KeyCode.RIGHT))
				entityX += 0.1;
			if (keys.get(KeyCode.LEFT))
				entityX -= 0.1;
			if (keys.get(KeyCode.DOWN))
				entityY += 0.1;
			if (keys.get(KeyCode.UP))
				entityY -= 0.1;
			if (selectedEntity != null) {
				selectedEntity.x = entityX;
				selectedEntity.y = entityY;
			}
			camera.target(entityX, entityY, zoom*Camera.DEFAULT_ZOOM);
		}
		// Tick camera 10 times to speed up targetting
		for (int i = 0; i < 10; i++)
			camera.tick();
	}

	private void setupTilesUI() {
		tileRoot = new HBox(58);
		currentTileView = new Canvas(72, 72);
		tileRoot.getChildren().add(currentTileView);
		tileOptionContainer = new GridPane();
		tileOptions = new ArrayList<Canvas>();
		for (int i = 0; i < 104; i++) {
			tileOptions.add(new Canvas(36, 36));
			tileOptionContainer.add(tileOptions.get(i), i / 2, i % 2);
			final int offset = i;
			tileOptions.get(i).setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (!mode.getText().equals("Tile mode"))
						return;
					int sel = (offset + (int) Math.floor(tileOptionOffset)) % (Tile.maxID() + 1);
					if (sel < 0)
						sel += Tile.maxID() + 1;
					if (selecting)
						for (int x = Math.min(tileX, selectX); x <= tileX || x <= selectX; x++)
							for (int y = Math.min(tileY, selectY); y <= tileY || y <= selectY; y++)
								level.put(x, y, Tile.get(sel));
					else
						level.put(tileX, tileY, Tile.get(sel));
				}
			});
		}
		tileOptionContainer.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				int previous = (int) Math.floor(tileOptionOffset);
				tileOptionOffset -= (event.getDeltaX() + event.getDeltaY()) * 0.1;
				if (previous != (int) Math.floor(tileOptionOffset))
					updateTileOptions();

			}
		});
		updateTileOptions();
	}

	private void setupEntitiesUI() {
		entityRoot = new HBox(56);
		currentEntityView = new Canvas(66, 66);
		entityRoot.getChildren().add(currentEntityView);
		entityOptionContainer = new HBox();
		entityOptions = new ArrayList<Canvas>();
		for (int i = 0; i < 28; i++) {
			entityOptions.add(new Canvas(66, 66));
			entityOptionContainer.getChildren().add(entityOptions.get(i));
			final int offset = i;
			entityOptions.get(i).setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (!mode.getText().equals("Entity mode"))
						return;
					if (selectedEntity == null) {
						selectedEntity = new EditorEntity(offset + (int) Math.floor(entityOptionOffset), entityX,
								entityY);
						entities.add(selectedEntity);
					} else
						selectedEntity.entity = offset + (int) Math.floor(entityOptionOffset);
				}
			});
		}
		entityOptionContainer.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				int previous = (int) Math.floor(entityOptionOffset);
				entityOptionOffset -= (event.getDeltaX() + event.getDeltaY()) * 0.025;
				if (previous != (int) Math.floor(entityOptionOffset))
					updateEntityOptions();

			}
		});
	}

	private void updateTileOptions() {
		for (int i = 0; i < tileOptions.size(); i++) {
			GraphicsContext tileContext = tileOptions.get(i).getGraphicsContext2D();
			tileContext.clearRect(0, 0, 36, 36);
			tileContext.setImageSmoothing(false);
			tileContext.setStroke(Color.BLACK);
			tileContext.setLineWidth(2);
			tileContext.strokeRect(0, 0, 36, 36);
			tileContext.save();
			tileContext.translate(2, 2);
			tileContext.scale(32, 32);
			int sel = (i + (int) Math.floor(tileOptionOffset)) % (Tile.maxID() + 1);
			if (sel < 0)
				sel += Tile.maxID() + 1;
			Tile.get(sel).render(tileContext);
			tileContext.restore();
		}
	}

	private void updateEntityOptions() {
		for (int i = 0; i < entityOptions.size(); i++) {
			GraphicsContext entityContext = entityOptions.get(i).getGraphicsContext2D();
			entityContext.clearRect(0, 0, 66, 66);
			int index = i + (int) Math.floor(entityOptionOffset);
			if (index >= entityInstances.size() || index < 0)
				continue;
			entityContext.setImageSmoothing(false);
			entityContext.save();

			entityContext.setStroke(Color.BLACK);
			entityContext.setLineWidth(2);
			entityContext.strokeRect(0, 0, 66, 66);

			Entity e = entityInstances.get(index);
			Rect bounds = e.getRenderBounds().getBounds();
			entityContext.translate(2, 2);
			entityContext.scale(64, 64);
			double maxDim = Math.max(bounds.w, bounds.h);
			double scale = 1.0 / maxDim;
			entityContext.scale(scale, scale);
			entityContext.translate(-bounds.x + (maxDim - bounds.w) / 2,
					-bounds.y + (maxDim - bounds.h) / 2);
			e.render(entityContext, throwawayContext, entityContext);
			entityContext.restore();
		}
	}

	@Override
	public void render() {
		c0.clearRect(0, 0, layer_0.getWidth(), layer_0.getHeight());
		c1.clearRect(0, 0, layer_1.getWidth(), layer_1.getHeight());
		s0.clearRect(0, 0, shadow_0.getWidth(), shadow_0.getHeight());
		c2.clearRect(0, 0, layer_2.getWidth(), layer_2.getHeight());
		s1.clearRect(0, 0, shadow_1.getWidth(), shadow_1.getHeight());

		c0.save();
		c1.save();
		s0.save();
		c2.save();
		s1.save();

		camera.transform(c0);
		camera.transform(c1);
		camera.transform(s0);
		camera.transform(c2);
		camera.transform(s1);

		if (mode.getText().equals("Tile mode")) {
			c1.setGlobalAlpha(0.5);
			c2.setGlobalAlpha(0.5);
		}

		level.render(c0, c1, s0, c2, s1);

		for (EditorEntity e : entities) {
			e.update();
			e.get().render(c1, s0, c2);
			Rect bounds = e.get().getRenderBounds().getBounds();
			c2.setStroke(Color.rgb(0, 0, 0, 0.5));
			c2.setLineWidth(0.05);
			c2.strokeRect(bounds.x, bounds.y, bounds.w, bounds.h);
		}

		if (mode.getText().equals("Tile mode")) {
			c2.setStroke(Color.BLACK);
			c2.setLineWidth(0.1);
			if (selecting) {
				c2.strokeRect(Math.min(tileX, selectX), Math.min(tileY, selectY), Math.abs(tileX - selectX) + 1,
						Math.abs(tileY - selectY) + 1);
			} else
				c2.strokeRect(tileX, tileY, 1, 1);
		} else {
			c2.setFill(Color.RED);
			c2.fillRect(entityX - 0.1, entityY - 0.1, 0.2, 0.2);
			if (selectedEntity != null) {
				selectedEntity.update();
				Entity e = selectedEntity.get();
				Rect bounds = e.getRenderBounds().getBounds();
				c2.setLineWidth(0.1);
				c2.setStroke(Color.BLACK);
				c2.strokeRect(bounds.x, bounds.y, bounds.w, bounds.h);
				bounds = e.getSolidBounds().getBounds();
				c2.setStroke(Color.RED);
				c2.strokeRect(bounds.x, bounds.y, bounds.w, bounds.h);
			}
		}

		c0.restore();
		c1.restore();
		s0.restore();
		c2.restore();
		s1.restore();

		GraphicsContext gc2 = currentTileView.getGraphicsContext2D();
		gc2.setImageSmoothing(false);
		gc2.clearRect(0, 0, 70, 70);
		gc2.setStroke(Color.BLACK);
		gc2.setLineWidth(3);
		gc2.strokeRect(3, 3, 64, 64);
		gc2.save();
		gc2.translate(3, 3);
		gc2.scale(64, 64);
		if (level.get(tileX, tileY) != null)
			level.get(tileX, tileY).render(gc2);
		gc2.restore();

		if (selectedEntity == null)
			return;
		Entity e = selectedEntity.get();
		Rect bounds = e.getRenderBounds().getBounds();
		gc2 = currentEntityView.getGraphicsContext2D();
		gc2.setImageSmoothing(false);
		gc2.clearRect(0, 0, 128, 128);
		gc2.setStroke(Color.BLACK);
		gc2.setLineWidth(3);
		gc2.strokeRect(3, 3, 128, 128);
		gc2.save();
		gc2.translate(3, 3);
		gc2.scale(128, 128);
		double scale = Math.min(1 / bounds.w, 1 / bounds.h);
		gc2.translate(-scale * bounds.x, -scale * bounds.y);
		gc2.scale(scale, scale);
		e.render(gc2, throwawayContext, gc2);
		gc2.restore();

	}

	private class EditorEntity {
		public double x, y;
		public int entity;

		public EditorEntity(int entity, double x, double y) {
			this.entity = entity;
			this.x = x;
			this.y = y;
		}

		public void update() {
			entityInstances.get(entity).setX(x);
			entityInstances.get(entity).setY(y);
		}

		public Entity get() {
			return entityInstances.get(entity);
		}

		public boolean equals(Object other) {
			if (other instanceof EditorEntity e)
				return e.entity == entity && e.x == x && e.y == y;
			return false;
		}
	}
}
