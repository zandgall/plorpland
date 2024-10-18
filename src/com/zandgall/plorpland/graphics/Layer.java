package com.zandgall.plorpland.graphics;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import com.zandgall.plorpland.Main;

import static org.lwjgl.opengl.GL30.*;

public class Layer {

	/* A set of predefined layers used in the base game */

	protected static final TreeSet<Layer> LAYER_BY_DEPTH = new TreeSet<>(new Comparator<Layer>() {
		public int compare(Layer a, Layer b) {
			return (int)Math.signum(a.depth - b.depth);
		}
	});

	public static final Layer
		LEVEL_BASE = new Layer(0.0),
		LEVEL_FOREGROUND = new Layer(3.0),
		LEVEL_BACKGROUND = new Layer(-3.0),
		TREE_LEAVES = new Layer(2.0),
		TREE_SHADOW = new Layer(1.5),
		ENTITY_BASE = new Layer(1.0),
		ENTITY_SHADOW = new Layer(0.5),
		CLOUD_SHADOW = new Layer(3.5),
		WORLD_INDICATORS = new Layer(4.5),
		HUD = new Layer(5.0);

	private FbSingle content;
	protected double depth;

	public Layer(double depth) {
		content = new FbSingle();
		content.newTexture(Main.WIDTH, Main.HEIGHT);
		this.depth = depth;

		LAYER_BY_DEPTH.add(this);
	}

	public void use() {
		glViewport(0, 0, Main.WIDTH, Main.HEIGHT);
		content.drawToThis();
	}

	public static void prepareFrame() {
		for(Layer l : LAYER_BY_DEPTH) {
			l.content.deleteTexture();
			l.content.newTexture(Main.WIDTH, Main.HEIGHT);
			l.use();
			glClearColor(0, 0, 0, 0);
			glClear(GL_COLOR_BUFFER_BIT);
		}
	}

	public static void flushToScreen() {
		FbSingle.drawToScreen();
		glDisable(GL_DEPTH_TEST);
		glViewport(0, 0, Main.WIDTH, Main.HEIGHT);
		glClearColor(0, 0, 0, 0);
		glClear(GL_COLOR_BUFFER_BIT);
		// Remember that framebuffers vertically flip everything!
		Shader.Image.use().push().drawToScreen().setModel(0, Main.HEIGHT, Main.WIDTH, -Main.HEIGHT);
		for(Layer l : LAYER_BY_DEPTH) {
			Shader.Image.use().texture(l.content.getTexture());
			G.drawSquare();
		}
		Shader.Image.use().pop();
	}
}
