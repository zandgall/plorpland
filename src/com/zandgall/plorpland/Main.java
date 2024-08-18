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

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import com.zandgall.plorpland.entity.Entity;
import com.zandgall.plorpland.entity.EntityRegistry;
import com.zandgall.plorpland.entity.Player;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Shader;
import com.zandgall.plorpland.staging.Cutscene;
import com.zandgall.plorpland.level.Level;

public class Main {

	public static long iteration = 0;

	// How long each timestep is that tick updates
	public static final double TIMESTEP = 0.01;

	public static Main instance = null;

	public static int WIDTH = 1280, HEIGHT = 720;
	private static long window;

	public static boolean[] keys = new boolean[GLFW_KEY_LAST], pKeys = new boolean[GLFW_KEY_LAST];
	public static int lastKey = GLFW_KEY_LAST;

	private static ArrayList<Runnable> postGLFW = new ArrayList<>();

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
		window = glfwCreateWindow(1280, 720, "Plorpland", NULL, NULL);
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
		}

		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			keys[key] = action != GLFW_RELEASE;
		});

		glfwMakeContextCurrent(window);

		// TODO: VSYNC option?
		glfwSwapInterval(1);
		glfwShowWindow(window);
		GL.createCapabilities();
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		Shader.init();
		G.init();

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
		hud.tick();
		Sound.update();
		for(int i = 0; i < GLFW_KEY_LAST; i++)
			pKeys[i] = keys[i];
	}

	public static void render() {
		// Clear all canvases
		glViewport(0, 0, WIDTH, HEIGHT);
		// glClearColor(0.55f, 0.8f, 1.0f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		// Save all context states
		// Transform all (except hud) with camera
		Shader.Image.use();
		Shader.Image.setProjection(new Matrix4f().ortho(-WIDTH * 0.5f, WIDTH * 0.5f, HEIGHT * 0.5f, -HEIGHT * 0.5f, -1.f, 1.f));
		camera.transform();

		// Don't transform hudContext
		
		// Draw!
		level.render();
		hud.render();

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
	}

	public static void doAfterGLFW(Runnable r) {
		postGLFW.add(r);
	}

}
