package com.zandgall.plorpland.editor;

import static org.lwjgl.glfw.GLFW.*;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Layer;
import com.zandgall.plorpland.graphics.Shader;

public class StageEditor extends Editor {

	public StageEditor() {}

	public void tick(Level lvl) {
		if(Main.keys[GLFW_KEY_LEFT])
			x -= 6.0 / camera.getZoom() * (Main.keys[GLFW_KEY_LEFT_CONTROL] ? 0.1 : 1);
		if(Main.keys[GLFW_KEY_RIGHT])
			x += 6.0 / camera.getZoom() * (Main.keys[GLFW_KEY_LEFT_CONTROL] ? 0.1 : 1);
		if(Main.keys[GLFW_KEY_UP])
			y -= 6.0 / camera.getZoom() * (Main.keys[GLFW_KEY_LEFT_CONTROL] ? 0.1 : 1);
		if(Main.keys[GLFW_KEY_DOWN])
			y += 6.0 / camera.getZoom() * (Main.keys[GLFW_KEY_LEFT_CONTROL] ? 0.1 : 1);

		if(Main.keys[GLFW_KEY_1])
			lvl.spawnpoint.set(x, y);
	}

	public void render(Level lvl) {
		lvl.renderGraphics();
		Shader.Image.use().push().alpha(0.1);
		lvl.renderTiles();
		lvl.renderSpecialImages();
		lvl.renderEntities();
		Shader.Image.use().pop();
		Layer.HUD.use();
		lvl.renderStaging();

		Shader.Color.use().push().drawToWorld()
			.color(1, 0, 0).alpha(0.5)
			.setModel(x-(3.0 / camera.getZoom()), y-(3.0 / camera.getZoom()), 6.0 / camera.getZoom(), 6.0 / camera.getZoom());
		G.drawSquare();
		Shader.Color.use().pop();
		Layer.LEVEL_BASE.use();
	}

	public void switchTo() {
		x = camera.getX();
		y = camera.getY();
	}

}
