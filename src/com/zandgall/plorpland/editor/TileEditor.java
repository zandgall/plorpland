package com.zandgall.plorpland.editor;

import static org.lwjgl.glfw.GLFW.*;

import com.zandgall.plorpland.Camera;
import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.graphics.Shader;
import com.zandgall.plorpland.level.Tile;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Hitrect;

public class TileEditor {

	Image tile = new Image("/tiles/5tiles.png");

	int tileX = 0, tileY = 0;

	public TileEditor() {

	}

	public void tick(Level lvl) {
		if(Main.keyEv[GLFW_KEY_LEFT])
			tileX--;
		if(Main.keyEv[GLFW_KEY_RIGHT])
			tileX++;
		if(Main.keyEv[GLFW_KEY_UP])
			tileY--;
		if(Main.keyEv[GLFW_KEY_DOWN])
			tileY++;
		if(Main.keys[GLFW_KEY_1])
			lvl.put(tileX, tileY, Tile.get(0));
		if(Main.keys[GLFW_KEY_2])
			lvl.put(tileX, tileY, Tile.get(1));
		if(Main.keys[GLFW_KEY_3])
			lvl.put(tileX, tileY, Tile.get(2));
		if(Main.keys[GLFW_KEY_4])
			lvl.put(tileX, tileY, Tile.get(3));
		if(Main.keys[GLFW_KEY_5])
			lvl.put(tileX, tileY, Tile.get(4));
		if(Main.keys[GLFW_KEY_5])
			lvl.put(tileX, tileY, Tile.get(5));
		Main.getCamera().target(tileX+0.5, tileY+0.5);
		Main.getCamera().tick();
	}

	public void render(Level lvl) {
		lvl.renderSpecialImages();
		lvl.renderGraphics();
		lvl.renderEntities();

		Camera c = Main.getCamera();
		Hitbox screen = new Hitrect(c.getX() - 0.5 * Main.WIDTH / c.getZoom(),
			c.getY() - 0.5 * Main.HEIGHT / c.getZoom(), Main.WIDTH / c.getZoom(),
			Main.HEIGHT / c.getZoom());
		int xMin = (int) screen.getBounds().x;
		int xMax = (int) (screen.getBounds().x + screen.getBounds().w);
		int yMin = (int) screen.getBounds().y;
		int yMax = (int) (screen.getBounds().y + screen.getBounds().h);

		for (int x = xMin; x <= xMax; x++)
			for (int y = yMin; y <= yMax; y++) {
				if (lvl.get(x, y) == null || lvl.get(x,y).getID() == 0)
					continue;
				tile.draw(16*(lvl.get(x,y).getID()-1), 0, 16, 16, x, y, 1, 1);
			}
		Shader.Color.use().push().drawToWorld()
			.color(1, 1, 1).alpha(0.1)
			.setModel(tileX, tileY, 1.0, 1.0);
		G.drawSquare();
		Shader.Color.use().pop();
	}
}
