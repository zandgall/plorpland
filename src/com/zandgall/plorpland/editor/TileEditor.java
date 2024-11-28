package com.zandgall.plorpland.editor;

import static org.lwjgl.glfw.GLFW.*;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Shader;
import com.zandgall.plorpland.level.Tile;

public class TileEditor extends Editor {

	public TileEditor() {

	}

	public void tick(Level lvl) {
		if(Main.keyEv[GLFW_KEY_LEFT])
			x--;
		if(Main.keyEv[GLFW_KEY_RIGHT])
			x++;
		if(Main.keyEv[GLFW_KEY_UP])
			y--;
		if(Main.keyEv[GLFW_KEY_DOWN])
			y++;
		if(Main.keys[GLFW_KEY_1])
			lvl.put((int)Math.floor(x), (int)Math.floor(y), Tile.get(0));
		if(Main.keys[GLFW_KEY_2])
			lvl.put((int)Math.floor(x), (int)Math.floor(y), Tile.get(1));
		if(Main.keys[GLFW_KEY_3])
			lvl.put((int)Math.floor(x), (int)Math.floor(y), Tile.get(2));
		if(Main.keys[GLFW_KEY_4])
			lvl.put((int)Math.floor(x), (int)Math.floor(y), Tile.get(3));
		if(Main.keys[GLFW_KEY_5])
			lvl.put((int)Math.floor(x), (int)Math.floor(y), Tile.get(4));
		if(Main.keys[GLFW_KEY_6])
			lvl.put((int)Math.floor(x), (int)Math.floor(y), Tile.get(5));
	}

	public void render(Level lvl) {
		lvl.renderSpecialImages();
		lvl.renderGraphics();
		lvl.renderEntities();
		lvl.renderTiles();
		
		Shader.Color.use().push().drawToWorld()
			.color(1, 1, 1).alpha(0.1)
			.setModel(Math.floor(x), Math.floor(y), 1.0, 1.0);
		G.drawSquare();
		Shader.Color.use().pop();
	}
}
