package com.zandgall.plorpland.graphics;

import java.util.ArrayList;
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
		SHADOW_BASE = new Layer(0.1),
		LEVEL_FOREGROUND = new Layer(3.0),
		SHADOW_FOREGROUND = new Layer(3.1),
		LEVEL_BACKGROUND = new Layer(-3.0),
		TREE_LEAVES = new Layer(3.05),
		TREE_SHADOW = new Layer(1.5),
		ENTITY_BASE = new Layer(1.0),
		ENTITY_SHADOW = new Layer(0.5),
		CLOUD_SHADOW = new Layer(3.5),
		WORLD_INDICATORS = new Layer(4.5),
		HUD = new Layer(5.0);
	public static Layer CURRENT = LEVEL_BASE;
	
	protected ArrayList<RenderCall> calls;
	protected double depth;

	public Layer(double depth) {
		calls = new ArrayList<>();
		this.depth = depth;

		LAYER_BY_DEPTH.add(this);
	}

	public void use() {
		CURRENT = this;
	}

	public void addCall(RenderCall call) {
		calls.add(call);
	}

	public static void flushToScreen() {
		FbSingle.drawToScreen();
		// glDisable(GL_DEPTH_TEST);
		glEnable(GL_BLEND);
		glViewport(0, 0, Main.WIDTH, Main.HEIGHT);
		glClearColor(0.55f, 0.8f, 1.0f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		// Remember that framebuffers vertically flip everything!
		// Shader.Post.use().push();
		for(Layer l : LAYER_BY_DEPTH) {
			for(RenderCall c : l.calls)
				c.call();
			l.calls.clear();
		}
		// Shader.Post.use().pop();
		// glDisable(GL_BLEND);
	}
}
