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
	public Rect bounds = new Rect();
	public HashMap<Integer, HashMap<Integer, Tile>> tiles = new HashMap<>();
	protected static final int CHUNK_SIZE = 128;

	// Level graphics
	protected Image[][] images_0 = new Image[0][0], images_1 = new Image[0][0], shadow_0 = new Image[0][0], shadow_1 = new Image[0][0];

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
		loadGraphics(leveldir);
		loadTileData(leveldir + "/tiles.bin");
		loadTileData(leveldir + "/entities.bin");
	}

	public void loadGraphics(String leveldir) {
		// Load images and create output array
		Image l0 = new Image(leveldir + "/level_0.png");
		Image l1 = new Image(leveldir + "/level_1.png");
		Image s0 = new Image(leveldir + "/shadow_0.png");
		Image s1 = new Image(leveldir + "/shadow_1.png");
		images_0 = new Image[(int)Math.ceil(l0.getWidth()/CHUNK_SIZE)][(int)Math.ceil(l0.getHeight()/CHUNK_SIZE)];
		images_1 = new Image[(int)Math.ceil(l0.getWidth()/CHUNK_SIZE)][(int)Math.ceil(l0.getHeight()/CHUNK_SIZE)];
		shadow_0 = new Image[(int)Math.ceil(l0.getWidth()/CHUNK_SIZE)][(int)Math.ceil(l0.getHeight()/CHUNK_SIZE)];
		shadow_1 = new Image[(int)Math.ceil(l0.getWidth()/CHUNK_SIZE)][(int)Math.ceil(l0.getHeight()/CHUNK_SIZE)];

		// Cropping and shader info
		FbSingle f = new FbSingle();
		Shader.Image.use().push().setModel(0, 0, 1, 1);
		Shader.Image.use().setView(new Matrix4f()).setProjection(new Matrix4f().ortho(0, 1, 0, 1, -1, 1));
		glViewport(0, 0, CHUNK_SIZE, CHUNK_SIZE);
		// Get crop value
		float w = (float)CHUNK_SIZE / l0.getWidth(), h = (float)CHUNK_SIZE / l0.getHeight();
		glDisable(GL_BLEND);
		for(int i = 0; i < l0.getWidth() / CHUNK_SIZE; i++) {
			for(int j = 0; j < l0.getHeight() / CHUNK_SIZE; j++) {
				f.newTexture(CHUNK_SIZE, CHUNK_SIZE);
				f.drawToThis();
				glClear(GL_COLOR_BUFFER_BIT);
				Shader.Image.use().image(l0).crop(w * i, h * j, w, h);
				G.rawDrawSquare();
				images_0[i][j] = new Image(f.getTexture());
				
				f.newTexture(CHUNK_SIZE, CHUNK_SIZE);
				f.drawToThis();
				glClear(GL_COLOR_BUFFER_BIT);
				Shader.Image.use().image(l1).crop(w*i, h*j, w, h);
				G.rawDrawSquare();
				images_1[i][j] = new Image(f.getTexture());
				
				f.newTexture(CHUNK_SIZE, CHUNK_SIZE);
				f.drawToThis();
				glClear(GL_COLOR_BUFFER_BIT);
				Shader.Image.use().image(s0).crop(w*i, h*j, w, h);
				G.rawDrawSquare();
				shadow_0[i][j] = new Image(f.getTexture());
				
				f.newTexture(CHUNK_SIZE, CHUNK_SIZE);
				f.drawToThis();
				glClear(GL_COLOR_BUFFER_BIT);
				Shader.Image.use().image(s1).crop(w*i, h*j, w, h);
				G.rawDrawSquare();
				shadow_1[i][j] = new Image(f.getTexture());
			}
		}

		Shader.Image.use().pop();
		FbSingle.drawToScreen();

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
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

	public void render() {
		Camera c = Main.getCamera();
		Hitbox screen = new Hitrect(c.getX() - 0.5 * Main.WIDTH / c.getZoom(),
			c.getY() - 0.5 * Main.HEIGHT / c.getZoom(), Main.WIDTH / c.getZoom(),
			Main.HEIGHT / c.getZoom());
		int xMin = (int) screen.getBounds().x;
		int xMax = (int) (screen.getBounds().x + screen.getBounds().w);
		int yMin = (int) screen.getBounds().y;
		int yMax = (int) (screen.getBounds().y + screen.getBounds().h);

		Shader.Image.use().drawToWorld().setModel(new Matrix4f().identity());
		for(SpecialImage i : specialImages.get(0))
			if(screen.intersects(i.getRenderBox()))
				i.render();
	
		xMin = Math.max((int)((xMin - bounds.x - 0.5) / (CHUNK_SIZE / 16.0)), 0);
		yMin = Math.max((int)((yMin - bounds.y - 0.5) / (CHUNK_SIZE / 16.0)), 0);
		xMax = (int)((xMax - bounds.x) / (CHUNK_SIZE / 16.0));
		yMax = (int)((yMax - bounds.y) / (CHUNK_SIZE / 16.0));
		Shader.Image.use().drawToWorld().setModel(new Matrix4f().identity());
		for (int x = xMin; x <= xMax && x < images_0.length; x++) {
			for (int y = yMin; y <= yMax && y < images_0[x].length; y++) {
				// System.out.printf("Drawing %d %d%n", x, y);
				Layer.LEVEL_BASE.use();
				images_0[x][y].draw(x * CHUNK_SIZE / 16.0 + bounds.x, y * CHUNK_SIZE / 16.0 + bounds.y, CHUNK_SIZE / 16.0, CHUNK_SIZE / 16.0);
				Layer.SHADOW_BASE.use();
				shadow_0[x][y].draw(x * CHUNK_SIZE / 16.0 + bounds.x, y * CHUNK_SIZE / 16.0 + bounds.y, CHUNK_SIZE / 16.0, CHUNK_SIZE / 16.0);
				Layer.LEVEL_FOREGROUND.use();
				images_1[x][y].draw(x * CHUNK_SIZE / 16.0 + bounds.x, y * CHUNK_SIZE / 16.0 + bounds.y, CHUNK_SIZE / 16.0, CHUNK_SIZE / 16.0);
				Layer.SHADOW_FOREGROUND.use();
				shadow_1[x][y].draw(x * CHUNK_SIZE / 16.0 + bounds.x, y * CHUNK_SIZE / 16.0 + bounds.y, CHUNK_SIZE / 16.0, CHUNK_SIZE / 16.0);
			}
		}

		// Sort and draw all entities and then clouds if they intersect the screen
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
		for (int x = xMin; x <= xMax && x < images_0.length; x++) {
			for (int y = yMin; y <= yMax && y < images_0[x].length; y++) {
				// System.out.printf("Drawing %d %d%n", x, y);
				Layer.LEVEL_BASE.use();
				images_0[x][y].draw(x * CHUNK_SIZE / 16.0 + bounds.x, y * CHUNK_SIZE / 16.0 + bounds.y, CHUNK_SIZE / 16.0, CHUNK_SIZE / 16.0);
				Layer.SHADOW_BASE.use();
				shadow_0[x][y].draw(x * CHUNK_SIZE / 16.0 + bounds.x, y * CHUNK_SIZE / 16.0 + bounds.y, CHUNK_SIZE / 16.0, CHUNK_SIZE / 16.0);
				Layer.LEVEL_FOREGROUND.use();
				images_1[x][y].draw(x * CHUNK_SIZE / 16.0 + bounds.x, y * CHUNK_SIZE / 16.0 + bounds.y, CHUNK_SIZE / 16.0, CHUNK_SIZE / 16.0);
				Layer.SHADOW_FOREGROUND.use();
				shadow_1[x][y].draw(x * CHUNK_SIZE / 16.0 + bounds.x, y * CHUNK_SIZE / 16.0 + bounds.y, CHUNK_SIZE / 16.0, CHUNK_SIZE / 16.0);
			}
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
