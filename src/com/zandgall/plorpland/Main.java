/* zandgall

 ## Main
 # Initiates as a javafx application

 : MADE IN NEOVIM */

package com.zandgall.plorpland;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map.Entry;

import com.zandgall.plorpland.entity.Entity;
import com.zandgall.plorpland.entity.EntityRegistry;
import com.zandgall.plorpland.entity.Player;
import com.zandgall.plorpland.staging.Cutscene;
import com.zandgall.plorpland.level.Level;

public class Main extends Application {

	public static long iteration = 0;

	// How long each timestep is that tick updates
	public static final double TIMESTEP = 0.01;

	public static Main instance = null;

	public static HashMap<KeyCode, Boolean> keys, pKeys;
	public static KeyCode lastKey = KeyCode.A;

	// JavaFX Elements
	public static Scene scene;
	public static Stage stage;
	public static Pane root;

	// Canvas and contexts for several layers.
	public static Canvas layer_0, layer_1, shadow_0, layer_2, shadow_1, hudCanvas, throwawayCanvas;
	public static GraphicsContext c0, c1, s0, c2, s1, hudContext, throwawayContext;

	// Instances of elements included in the game
	protected Player player;
	protected Camera camera;
	protected Level level;
	protected Hud hud;
	protected Cutscene cutscene = null;

	@Override
	public void start(Stage stage) {
		// Set up static elements
		Main.instance = this;
		Main.stage = stage;

		EntityRegistry.registerClasses();

		// Set up the scene and stage elements
		setupScene();
		stage.setTitle("Final");
		stage.setScene(scene);
		stage.show();
		stage.toFront();
		stage.requestFocus();

		// Update canvas sizes when stage dimensions are changed
		stage.widthProperty().addListener((obs, oldVal, newVal) -> {
			layer_0.setWidth(newVal.doubleValue());
			layer_1.setWidth(newVal.doubleValue());
			layer_2.setWidth(newVal.doubleValue());
			shadow_0.setWidth(newVal.doubleValue());
			shadow_1.setWidth(newVal.doubleValue());
			hudCanvas.setWidth(newVal.doubleValue());
		});
		stage.heightProperty().addListener((obs, oldVal, newVal) -> {
			layer_0.setHeight(newVal.doubleValue());
			layer_1.setHeight(newVal.doubleValue());
			layer_2.setHeight(newVal.doubleValue());
			shadow_0.setHeight(newVal.doubleValue());
			shadow_1.setHeight(newVal.doubleValue());
			hudCanvas.setHeight(newVal.doubleValue());
		});

		// Key input map and events
		keys = new HashMap<KeyCode, Boolean>();
		pKeys = new HashMap<KeyCode, Boolean>();
		for (KeyCode a : KeyCode.values()) {
			// Initialize all keys to false
			keys.put(a, false);
			pKeys.put(a, false);
		}

		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				Main.keys.put(event.getCode(), true);
				lastKey = event.getCode();
			}
		});
		scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				Main.keys.put(event.getCode(), false);	
			}
		});
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				Sound.kill();
			}
		});

		player = new Player();
		camera = new Camera();
		level = new Level();
		hud = new Hud();
		try {
			level.load("/level.bin");
			level.addEntity(player);
			// 'addEntity' operates on a queue, flush it to put all entities in the main list
			level.flushEntityQueues();
			// Make default quicksave
			quicksave();
		} catch (Exception e) {
			e.printStackTrace();
			return; // Can't play without level!
		}

		Sound.init();

		// The main loop. As this scene animation plays, the game is updated and
		// rendered
		new AnimationTimer() {
			private long lastTime = System.nanoTime();
			double delta = 0;

			@Override
			public void handle(long currentNanoTime) {
				delta += (currentNanoTime - lastTime) * 0.000000001;
				lastTime = currentNanoTime;
				// We tick in 1/100 second increments in order to ensure game
				// physics/interaction consistency
				while (delta >= TIMESTEP) {
					iteration++;
					tick();
					delta -= TIMESTEP;
				}
				render();
			}
		}.start();	
	}

	// Pulled to it's own function so that it can be overridden in LevelEditor
	public void setupScene() {
		root = new Pane();
		root.setStyle("-fx-background-color: #8fc9fc;");
		layer_0 = new Canvas(1280, 720);
		c0 = layer_0.getGraphicsContext2D();
		c0.setImageSmoothing(false);
		layer_1 = new Canvas(1280, 720);
		c1 = layer_1.getGraphicsContext2D();
		c1.setImageSmoothing(false);

		shadow_0 = new Canvas(1280, 720);
		s0 = shadow_0.getGraphicsContext2D();
		s0.setImageSmoothing(true);

		layer_2 = new Canvas(1280, 720);
		c2 = layer_2.getGraphicsContext2D();
		c2.setImageSmoothing(false);

		shadow_1 = new Canvas(1280, 720);
		s1 = shadow_1.getGraphicsContext2D();
		s1.setImageSmoothing(true);

		hudCanvas = new Canvas(1280, 720);
		hudContext = hudCanvas.getGraphicsContext2D();
		hudContext.setImageSmoothing(false);

		throwawayCanvas = new Canvas(0, 0);
		throwawayContext = throwawayCanvas.getGraphicsContext2D();

		root.getChildren().add(layer_0);
		root.getChildren().add(layer_1);
		root.getChildren().add(shadow_0);
		root.getChildren().add(layer_2);
		root.getChildren().add(shadow_1);
		root.getChildren().add(hudCanvas);

		scene = new Scene(root, 1280, 720);
	}

	// Backup main function
	public static void main(String[] args) {
		Application.launch(args);
	}

	// Update scene. If there is a cutscene, run cutscene until it's over
	public void tick() {
		if (cutscene == null) {
			level.tick();
			camera.target(player.getX() + player.getXVel() * 1.5, player.getY() + player.getYVel() * 1.5);
			camera.tick();
		} else if (cutscene.run()) {
			cutscene = null;
		}
		hud.tick();
		Sound.update();
		for(Entry<KeyCode, Boolean> a : keys.entrySet())
			pKeys.put(a.getKey(), a.getValue());
	}

	public void render() {
		// Clear all canvases
		c0.clearRect(0, 0, layer_0.getWidth(), layer_0.getHeight());
		c1.clearRect(0, 0, layer_1.getWidth(), layer_1.getHeight());
		s0.clearRect(0, 0, shadow_0.getWidth(), shadow_0.getHeight());
		c2.clearRect(0, 0, layer_2.getWidth(), layer_2.getHeight());
		s1.clearRect(0, 0, shadow_1.getWidth(), shadow_1.getHeight());
		hudContext.clearRect(0, 0, hudCanvas.getWidth(), hudCanvas.getHeight());

		// Save all context states
		c0.save();
		c1.save();
		s0.save();
		c2.save();
		s1.save();
		hudContext.save();

		// Transform all (except hud) with camera
		camera.transform(c0);
		camera.transform(c1);
		camera.transform(s0);
		camera.transform(c2);
		camera.transform(s1);
		// Don't transform hudContext

		// Draw!
		level.render(c0, c1, s0, c2, s1);
		hud.render(hudContext);

		// Restore context states
		c0.restore();
		c1.restore();
		s0.restore();
		c2.restore();
		s1.restore();
		hudContext.restore();
	}

	// Global accessers
	public static Level getLevel() {
		return instance.level;
	}

	public static Camera getCamera() {
		return instance.camera;
	}

	public static Player getPlayer() {
		return instance.player;
	}

	public static Hud getHud() {
		return instance.hud;
	}

	public static void playCutscene(Cutscene cutscene) {
		instance.cutscene = cutscene;
	}

	public static void quicksave() throws IOException {
		save("quick.sav");
	}

	public static void quickload() throws IOException {
		load("quick.sav");
	}

	public static void save(String filepath) throws IOException {
		ObjectOutputStream o;
		try {
			o = new ObjectOutputStream(new FileOutputStream(filepath));
		} catch(FileNotFoundException e) {
			System.err.println("Could not save \"" + filepath + "\"!");
			e.printStackTrace();
			return;
		}

		Level l = getLevel();
		o.writeInt(l.getEntities().size() - 1);
		o.writeObject(getPlayer());
		for(Entity e : l.getEntities())
			if(e != getPlayer())
				o.writeObject(e);
		Sound.save(o);
		o.close();
	}

	public static void load(String filepath) throws IOException {
		ObjectInputStream i;
		try {
			i = new ObjectInputStream(new FileInputStream(filepath));
		} catch(FileNotFoundException e) {
			System.err.println("Could not load \"" + filepath + "\"!");
			e.printStackTrace();
			return;
		}

		Level l = getLevel();
		l.getEntities().clear();
		int num = i.readInt();
		try {
			instance.player = (Player)i.readObject();
			instance.camera.snapTo(instance.player.getX(), instance.player.getY(), Camera.DEFAULT_ZOOM);
		} catch(ClassNotFoundException e) {
			System.err.println("FATAL!! COULD NOT LOAD PLAYER CLASS");
			i.close();
			return;
		}

		for(int n = 0; n < num; n++) {
			try {
				l.addEntity((Entity)i.readObject());
			} catch(ClassNotFoundException e) {
				System.err.println("Could not load entity #" + n);
				e.printStackTrace();
			}
		}
		l.addEntity(instance.player);
		Sound.load(i);
		i.close();
	}

	public static void close() {
		stage.close();
		Sound.kill();
	}

}
