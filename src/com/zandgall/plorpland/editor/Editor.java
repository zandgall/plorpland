package com.zandgall.plorpland.editor;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL30.*;

import com.zandgall.plorpland.Camera;
import com.zandgall.plorpland.Hud;
import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.entity.EntityRegistry;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Layer;
import com.zandgall.plorpland.graphics.Shader;

public abstract class Editor extends Main {

	public static double ZOOM = Camera.DEFAULT_ZOOM;

	private static Level lvl;

	protected static double x = 0.5, y = 0.5;
	private static Editor tileeditor = new TileEditor(), 
					specialimageeditor = new SpecialImageEditor(),
					graphicseditor = new GraphicsEditor(),
					entityeditor = new EntityEditor(),
					stageeditor = new StageEditor();

	public static Editor currentEditor = tileeditor;

	public Editor() {}

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
			keyEv[i] = false;
		}

		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			keys[key] = action != GLFW_RELEASE;
			keyEv[key] = action != GLFW_RELEASE;
			if(keys[key])
				lastKey = key;
		});

		glfwSetScrollCallback(window, (window, deltaX, deltaY) -> {
			ZOOM += ZOOM * 0.1 * deltaY;
		});

		glfwMakeContextCurrent(window);

		// TODO: VSYNC option?
		glfwSwapInterval(1);
		glfwShowWindow(window);
		GL.createCapabilities();
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		camera = new Camera();
		camera.setSmoothing(Camera.DEFAULT_SMOOTHING*4);

		Shader.init();
		G.init();

		hud = new Hud();
		lvl = new Level();

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

	public abstract void tick(Level lvl);

	public static void tick() {
		currentEditor.tick(lvl);

		if(keys[GLFW_KEY_T]) {
			x = Math.floor(x) + 0.5;
			y = Math.floor(y) + 0.5;
			currentEditor = tileeditor;
		}
		if(keys[GLFW_KEY_S])
			currentEditor = specialimageeditor;
		if(keys[GLFW_KEY_G])
			currentEditor = graphicseditor;
		if(keys[GLFW_KEY_E])
			currentEditor = entityeditor;
		if(keys[GLFW_KEY_F])
			currentEditor = stageeditor;
		if(keyEv[GLFW_KEY_W]) {
			try {
				lvl.write(saveFolderDialog("Save level"));
			} catch(IOException e) {
				e.printStackTrace();
				System.err.println("Failed to write level");
			}
		}
		if(keyEv[GLFW_KEY_O]) {
			try {
				lvl.load(openFolderDialog("Open level"));
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Failed to load level");
			}
		}
		
		for(int i = 0; i < GLFW_KEY_LAST; i++) {
			pKeys[i] = keys[i];
			keyEv[i] = false;
		}
		
		camera.target(x, y, ZOOM);
		camera.tick();
	}

	public abstract void render(Level lvl);

	public static void render() {
		currentEditor.render(lvl);
		Layer.flushToScreen();
		glfwSwapBuffers(window);
	}

	public static File openFolderDialog(String prompt) {
		// Using swing just to use file dialogs
		JFrame frame = new JFrame();
		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle(prompt);
		int selection = chooser.showOpenDialog(frame);
		if (selection != JFileChooser.APPROVE_OPTION)
			return null;
		return chooser.getSelectedFile();
	}

	public static File openFileDialog(String prompt) {
		// Using swing just to use file dialogs
		JFrame frame = new JFrame();
		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
		chooser.setDialogTitle(prompt);
		int selection = chooser.showOpenDialog(frame);
		if (selection != JFileChooser.APPROVE_OPTION)
			return null;
		return chooser.getSelectedFile();
	}


	public static File saveFileDialog(String prompt) {
		// Using swing just to use file dialogs
		JFrame frame = new JFrame();
		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
		chooser.setDialogTitle(prompt);
		int selection = chooser.showSaveDialog(frame);
		if (selection != JFileChooser.APPROVE_OPTION)
			return null;
		return chooser.getSelectedFile();
	}

	public static File saveFolderDialog(String prompt) {
		// Using swing just to use file dialogs
		JFrame frame = new JFrame();
		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle(prompt);
		int selection = chooser.showSaveDialog(frame);
		if (selection != JFileChooser.APPROVE_OPTION)
			return null;
		return chooser.getSelectedFile();
	}

}
