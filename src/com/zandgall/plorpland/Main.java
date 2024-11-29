/* zandgall

 ## Main
 # Initiates as a javafx application

 : MADE IN NEOVIM */

package com.zandgall.plorpland;

import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import com.zandgall.plorpland.entity.Entity;
import com.zandgall.plorpland.entity.EntityRegistry;
import com.zandgall.plorpland.entity.Player;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Layer;
import com.zandgall.plorpland.graphics.Shader;
import com.zandgall.plorpland.staging.Cutscene;
import com.zandgall.plorpland.util.Vector;
import com.zandgall.plorpland.level.Level;

public class Main {

	public static long iteration = 0;

	// How long each timestep is that tick updates
	public static final double TIMESTEP = 0.01;

	public static Main instance = null;

	public static int WIDTH = 1280, HEIGHT = 720;
	protected static long window;

	public static boolean[] keys = new boolean[GLFW_KEY_LAST], pKeys = new boolean[GLFW_KEY_LAST], keyEv = new boolean[GLFW_KEY_LAST];
	public static int lastKey = GLFW_KEY_LAST - 1;
	public static boolean mouseLeft = false, mouseRight = false, pMouseLeft = false, pMouseRight = false;
	public static Vector mouse = new Vector(0, 0), mouseVel = new Vector(0, 0);

	// Functions to run after GLFW closes
	protected static ArrayList<Runnable> postGLFW = new ArrayList<>();

	// Instances of elements included in the game
	protected static Player player;
	protected static Camera camera;
	protected static Level level;
	protected static Hud hud;
	protected static Cutscene cutscene = null;
	
	public static void main(String[] args) {
		GLFWErrorCallback.createPrint(System.err).set();
		if(!glfwInit())
			throw new IllegalStateException("Could not initialize GLFW");

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

		// Set up static elements
		window = glfwCreateWindow(1280, 720, "Plorpland - Combat Test 1", NULL, NULL);
		if(window == NULL)
			throw new RuntimeException("Could not create window");

		EntityRegistry.registerClasses();

		// Update canvas sizes when stage dimensions are changed
		glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
			Main.WIDTH = width;
			Main.HEIGHT = height;
		});

		// Key input map and events
		for(int i = 0; i < GLFW_KEY_LAST; i++) {
			keys[i] = false;
			pKeys[i] = false;
			keyEv[i] = false;
		}

		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			keys[key] = action != GLFW_RELEASE;
			keyEv[key] = action != GLFW_RELEASE;
			if(keys[key])
				lastKey = key;
		});

		glfwSetCursorPosCallback(window, (window, x, y) -> {
			mouseVel.set(x, y).add(-mouse.x, -mouse.y);
			mouse.set(x, y);
		});

		glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
			if(button == GLFW_MOUSE_BUTTON_LEFT)
				mouseLeft = action != GLFW_RELEASE;
			if(button == GLFW_MOUSE_BUTTON_RIGHT)
				mouseRight = action != GLFW_RELEASE;
		});

		glfwMakeContextCurrent(window);

		// TODO: VSYNC option?
		glfwSwapInterval(1);
		glfwShowWindow(window);
		GL.createCapabilities();
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		camera = new Camera();

		Shader.init();
		G.init();

		player = new Player();
		hud = new Hud();
		level = new Level("/level/combattest");
		try {
			level.load();
			player.setX(level.getSpawnpoint().x);
			player.setY(level.getSpawnpoint().y);
			level.setPlayer(player);
			quicksave();
		} catch(IOException e) {
			e.printStackTrace();
			System.err.println("Can't play without level!");
			return;
		}
	
		Sound.init();

		// The main loop. As this scene animation plays, the game is updated and
		// rendered
		
		long lastTime = System.nanoTime();
		double delta = 0;

		while(!glfwWindowShouldClose(window)) {
			delta += (System.nanoTime() - lastTime) * 0.000000001;
			lastTime = System.nanoTime();
			// We tick in 1/100 second increments in order to ensure game
			// physics/interaction consistency
			while (delta >= TIMESTEP) {
				iteration++;
				tick();
				delta -= TIMESTEP;
			}

			render();

			glfwPollEvents();
		}

		close();
	}

	// Update scene. If there is a cutscene, run cutscene until it's over
	public static void tick() {
		if (cutscene == null) {
			level.tick();
			camera.target(player.getX() + player.getXVel() * 1.5, player.getY() + player.getYVel() * 1.5);
			camera.tick();
		} else if (cutscene.run()) {
			cutscene = null;
		}
		CombatTest.tick();
		hud.tick();
		Sound.update();
		for(int i = 0; i < GLFW_KEY_LAST; i++) {
			pKeys[i] = keys[i];
			keyEv[i] = false;
		}
		pMouseLeft = mouseLeft;
		pMouseRight = mouseRight;
		mouseVel.set(0, 0);
	}

	public static void render() {
		// Draw!
		level.render();
		hud.render();
		CombatTest.render();

		// Flush content to screen
		Layer.flushToScreen();

		// Swap buffers
		glfwSwapBuffers(window);
	}

	// Global accessers
	public static Level getLevel() {
		return level;
	}

	public static Camera getCamera() {
		return camera;
	}

	public static Player getPlayer() {
		return player;
	}

	public static Hud getHud() {
		return hud;
	}

	public static void playCutscene(Cutscene cutscene) {
		Main.cutscene = cutscene;
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
			player = (Player)i.readObject();
			camera.snapTo(player.getX(), player.getY(), Camera.DEFAULT_ZOOM);
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
		l.addEntity(player);
		Sound.load(i);
		i.close();
	}

	public static void close() {
		Sound.kill();

		Callbacks.glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		glfwTerminate();
	}

	public static void doAfterGLFW(Runnable r) {
		postGLFW.add(r);
	}

}
