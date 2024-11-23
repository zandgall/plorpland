package com.zandgall.plorpland.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL30.*;

import com.zandgall.plorpland.Camera;
import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.entity.Entity;
import com.zandgall.plorpland.graphics.FbSingle;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.graphics.Layer;
import com.zandgall.plorpland.graphics.Shader;
import com.zandgall.plorpland.level.SpecialImage;
import com.zandgall.plorpland.level.Tile;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Hitrect;
import com.zandgall.plorpland.util.Rect;

public class Level {
	private Image tile = new Image("/tiles/5tiles.png");
	public Rect bounds = new Rect();
	public HashMap<Integer, HashMap<Integer, Tile>> tiles = new HashMap<>();
	protected static final int CHUNK_SIZE = 128;

	// Level graphics
	public double graphicsX = 0, graphicsY = 0;
	public Image layer0 = null, layer1 = null, shadow0 = null, shadow1 = null;

	protected ArrayList<ArrayList<SpecialImage>> specialImages = new ArrayList<>();

	protected ArrayList<Entity> entities = new ArrayList<>();

	public Level() {
		specialImages.add(new ArrayList<>());
	}

	public void load(String leveldir) throws IOException {
		Scanner s = new Scanner(new File(leveldir + "/imageobjects"));	
		while(s.hasNextLine()) {
			String line = s.nextLine();
			Scanner p = new Scanner(line);
			specialImages.get(p.nextInt()).add(new SpecialImage(leveldir + p.next(), p.nextDouble(), p.nextDouble(), p.nextDouble(), p.nextDouble(), p.nextDouble()));
			p.close();
		}
		s.close();

		// Load level graphics
		// loadGraphics(leveldir);
		layer0 = new Image(leveldir + "/layer0.png");
		layer1 = new Image(leveldir + "/layer1.png");
		shadow0 = new Image(leveldir + "/shadow0.png");
		shadow1 = new Image(leveldir + "/shadow1.png");
		loadTileData(leveldir + "/tiles.bin");
		loadTileData(leveldir + "/entities.bin");
	}

	private void loadTileData(String path) throws IOException {

		// Clear level data
		tiles.clear();
		entities.clear();

		// Load resource
		ObjectInputStream s = new ObjectInputStream(Level.class.getResourceAsStream(path));

		// Check version number
		byte major = s.readByte();
		byte minor = s.readByte();

		if (major != 2 || minor != 0) {
			System.err.println("Unknown level version!!");
			s.close();
			return;
		}

		// Read Y range
		for (int y = s.readInt(), end = y + s.readInt(); y <= end; y++) {
			// The left x position of this line
			int x = s.readInt();
			boolean reading = true;
			while (reading) {
				// Read tile for this position
				int tile = s.readInt();

				// If the tile is empty,
				if (tile == 0) {
					// Read next int
					int next = s.readInt();
					// If it is 0, it signifies the end of this line
					if (next == 0)
						reading = false;
					// If it's not 0, jump forward 'next' number of tiles
					// Functionally the same as adding 'next' number of empty tiles
					else {
						x += next;
					}
				} else {
					// Place the tile
					put(x, y, Tile.get(tile));
					x++;
				}
			}
		}
		s.close();
	}

	public void loadEntityData(String path) throws IOException {
		ObjectInputStream s = new ObjectInputStream(new FileInputStream(path));
		
		// Check version number
		byte major = s.readByte();
		byte minor = s.readByte();

		if (major != 2 || minor != 0) {
			System.err.println("Unknown level version!!");
			s.close();
			return;
		}

		int numEntities = s.readInt();
		for (int i = 0; i < numEntities; i++)
			try {
				entities.add((Entity)s.readObject());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				System.err.println("Could not add entity #" + i);
			}
		s.close();
	}

	public void put(int x, int y, Tile tile) {
		// Expand the bounds of the level to get minimums and maximums of coords
		bounds.add(x, y);
		if (tiles.get(x) == null)
			tiles.put(x, new HashMap<Integer, Tile>());
		tiles.get(x).put(y, tile);
	}

	public Tile get(int x, int y) {
		if (tiles.get(x) == null)
			return null;
		return tiles.get(x).get(y);
	}

	public void renderSpecialImages() {
		Camera c = Main.getCamera();
		Hitbox screen = new Hitrect(c.getX() - 0.5 * Main.WIDTH / c.getZoom(),
			c.getY() - 0.5 * Main.HEIGHT / c.getZoom(), Main.WIDTH / c.getZoom(),
			Main.HEIGHT / c.getZoom());

		Shader.Image.use().push().drawToWorld().setModel(new Matrix4f().identity());
		for(SpecialImage i : specialImages.get(0))
			if(screen.intersects(i.getRenderBox()))
				i.render();
		Shader.Image.use().pop();

	}

	public void renderGraphics() {
		Camera c = Main.getCamera();
		Hitbox screen = new Hitrect(c.getX() - 0.5 * Main.WIDTH / c.getZoom(),
			c.getY() - 0.5 * Main.HEIGHT / c.getZoom(), Main.WIDTH / c.getZoom(),
			Main.HEIGHT / c.getZoom());
		int xMin = (int) screen.getBounds().x;
		int xMax = (int) (screen.getBounds().x + screen.getBounds().w);
		int yMin = (int) screen.getBounds().y;
		int yMax = (int) (screen.getBounds().y + screen.getBounds().h);

		xMin = Math.max((int)((xMin - bounds.x - 0.5) / (CHUNK_SIZE / 16.0)), 0);
		yMin = Math.max((int)((yMin - bounds.y - 0.5) / (CHUNK_SIZE / 16.0)), 0);
		xMax = (int)((xMax - bounds.x) / (CHUNK_SIZE / 16.0));
		yMax = (int)((yMax - bounds.y) / (CHUNK_SIZE / 16.0));
		Shader.Image.use().drawToWorld().setModel(new Matrix4f().identity());
		Layer.LEVEL_BASE.use();
		if(layer0 != null)
			layer0.draw(graphicsX - layer0.getWidth() / 32.0, graphicsY - layer0.getHeight() / 32.0, layer0.getWidth() / 16.0, layer0.getHeight() / 16.0);
		Layer.SHADOW_BASE.use();
		if(shadow0 != null)
			shadow0.draw(graphicsX - shadow0.getWidth() / 32.0, graphicsY - shadow0.getHeight() / 32.0, shadow0.getWidth() / 16.0, shadow0.getHeight() / 16.0);
		Layer.LEVEL_FOREGROUND.use();
		if(layer1 != null)
			layer1.draw(graphicsX - layer1.getWidth() / 32.0, graphicsY - layer1.getHeight() / 32.0, layer1.getWidth() / 16.0, layer1.getHeight() / 16.0);
		Layer.SHADOW_FOREGROUND.use();
		if(shadow1 != null)
			shadow1.draw(graphicsX - shadow1.getWidth() / 32.0, graphicsY - shadow1.getHeight() / 32.0, shadow1.getWidth() / 16.0, shadow1.getHeight() / 16.0);
	}

	public void renderTiles() {
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
				if (get(x, y) == null || get(x,y).getID() == 0)
					continue;
				tile.draw(16*(get(x,y).getID()-1), 0, 16, 16, x, y, 1, 1);
			}
	}

	public void renderEntities() {
		Camera c = Main.getCamera();
		Hitbox screen = new Hitrect(c.getX() - 0.5 * Main.WIDTH / c.getZoom(),
			c.getY() - 0.5 * Main.HEIGHT / c.getZoom(), Main.WIDTH / c.getZoom(),
			Main.HEIGHT / c.getZoom());	// Sort and draw all entities and then clouds if they intersect the screen
		ArrayList<Entity> sorted = new ArrayList<>(entities.size());
		for (Entity e : entities)
			if (e.getRenderBounds().intersects(screen)) {
				sorted.add(e);
			}
		sorted.sort((a, b) -> {
			return (int) Math.signum(a.getRenderLayer() - b.getRenderLayer());
		});
		for(Entity e : sorted)
			e.render();

	}
}
