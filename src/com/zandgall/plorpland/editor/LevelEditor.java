package com.zandgall.plorpland.editor;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.File;

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

public class LevelEditor extends Main {

	public static double ZOOM = Camera.DEFAULT_ZOOM;

	private static int state = 0;

	private static Level lvl;

	private static TileEditor tileeditor = new TileEditor();
	private static SpecialImageEditor specialimageeditor = new SpecialImageEditor();
	private static GraphicsEditor graphicseditor = new GraphicsEditor();

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

	public static void tick() {
		switch(state) {
		case 0 -> tileeditor.tick(lvl);
		case 1 -> specialimageeditor.tick(lvl);
		case 2 -> graphicseditor.tick(lvl);
		}

		if(keys[GLFW_KEY_T]) {
			tileeditor.switchTo();
			state = 0;
		}
		if(keys[GLFW_KEY_S]) {
			specialimageeditor.switchTo();
			state = 1;
		}
		if(keys[GLFW_KEY_G]) {
			graphicseditor.switchTo();
			state = 2;
		}
		
		for(int i = 0; i < GLFW_KEY_LAST; i++) {
			pKeys[i] = keys[i];
			keyEv[i] = false;
		}
	}

	public static void render() {
		switch(state) {
		case 0 -> tileeditor.render(lvl);
		case 1 -> specialimageeditor.render(lvl);
		case 2 -> graphicseditor.render(lvl);
		}
		Layer.flushToScreen();
		glfwSwapBuffers(window);
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
}
