package com.zandgall.plorpland.editor;

import static org.lwjgl.glfw.GLFW.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.graphics.Shader;

public class GraphicsEditor extends Editor {

	double contentX = 0, contentY = 0;
	Image content = null;

	public GraphicsEditor() {

	}

	public void tick(Level lvl) {
		if(Main.keys[GLFW_KEY_LEFT])
			x -= 6.0 / camera.getZoom() * (Main.keys[GLFW_KEY_LEFT_CONTROL] ? 0.1 : 1);
		if(Main.keys[GLFW_KEY_RIGHT])
			x += 6.0 / camera.getZoom() * (Main.keys[GLFW_KEY_LEFT_CONTROL] ? 0.1 : 1);
		if(Main.keys[GLFW_KEY_UP])
			y -= 6.0 / camera.getZoom() * (Main.keys[GLFW_KEY_LEFT_CONTROL] ? 0.1 : 1);
		if(Main.keys[GLFW_KEY_DOWN])
			y += 6.0 / camera.getZoom() * (Main.keys[GLFW_KEY_LEFT_CONTROL] ? 0.1 : 1);

		if(Main.keyEv[GLFW_KEY_1] || Main.keyEv[GLFW_KEY_2] || Main.keyEv[GLFW_KEY_3] || Main.keyEv[GLFW_KEY_4]) {
			File newImage = openFileDialog("Add image");
			Image next = null;
			if(newImage != null && newImage.isFile() && newImage.exists() && newImage.getName().endsWith(".png")) {
				try {
					next = new Image(Image.textureFrom(new FileInputStream(newImage)));
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("Could not load image");
					next = null;
				}
			} else
				System.err.println("Needs to be an existing .png file!");

			if(Main.keyEv[GLFW_KEY_1])
				lvl.layer0 = next;
			if(Main.keyEv[GLFW_KEY_2])
				lvl.layer1 = next;
			if(Main.keyEv[GLFW_KEY_3])
				lvl.shadow0 = next;
			if(Main.keyEv[GLFW_KEY_4])
				lvl.shadow1 = next;
		}

		if(Main.keys[GLFW_KEY_A]) {
			if(!Main.pKeys[GLFW_KEY_A]) {
				contentX = x - lvl.graphics.x;
				contentY = y - lvl.graphics.y;
			}
			lvl.graphics.x = x - contentX;
			lvl.graphics.y = y - contentY;
		}
	}

	public void render(Level lvl) {
		lvl.renderGraphics();
		lvl.renderEntities();
		Shader.Image.use().push().alpha(0.1);
		lvl.renderTiles();
		Shader.Image.use().pop();
		Shader.Image.use().push().alpha(0.1);
		lvl.renderSpecialImages();
		Shader.Image.use().pop();

		Shader.Color.use().push().drawToWorld()
			.color(1, 0, 0).alpha(0.5)
			.setModel(x-(3.0 / camera.getZoom()), y-(3.0 / camera.getZoom()), 6.0 / camera.getZoom(), 6.0 / camera.getZoom());
		G.drawSquare();
		Shader.Color.use().pop();
	}
}
