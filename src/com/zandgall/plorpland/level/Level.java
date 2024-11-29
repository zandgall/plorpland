/* zandgall

 ## Level
 # Stores information about the world, and serves as a wrapper to update and render all things in a level

 : MADE IN NEOVIM */

package com.zandgall.plorpland.level;

import com.zandgall.plorpland.entity.EntityRegistry;
import com.zandgall.plorpland.entity.Player;
import com.zandgall.plorpland.graphics.FbSingle;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.graphics.Layer;
import com.zandgall.plorpland.graphics.Shader;
import com.zandgall.plorpland.entity.Entity;
import com.zandgall.plorpland.entity.Cloud;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Hitrect;
import com.zandgall.plorpland.util.Rect;
import com.zandgall.plorpland.util.Vector;
import com.zandgall.plorpland.Camera;
import com.zandgall.plorpland.Main;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL30.*;

import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class Level {
	public Rect bounds = new Rect();
	private HashMap<Integer, HashMap<Integer, Tile>> level = new HashMap<>();

	private static final int CHUNK_SIZE = 128;

	public String leveldir;
	public Player playerRef = null;

	// Level graphics
	// TODO: Benchmark performance of straight up drawing full images (with crop) vs in chunks
	private Vector graphics = new Vector(0, 0), spawnpoint = new Vector(0, 0);
	private Image[][] images_0, images_1, shadow_0, shadow_1;
	private Image layer0 = Image.BLANK, layer1 = Image.BLANK, shadow0 = Image.BLANK, shadow1 = Image.BLANK;

	// A set of special background images, only 1 layer (0) is used right now
	private ArrayList<SpecialImage> specialImages = new ArrayList<>();

	// List of entities and two queues for removal and addition
	private ArrayList<Entity> entities = new ArrayList<>(), removeQueue = new ArrayList<>(),
			addQueue = new ArrayList<>();
	private ArrayList<Cloud> clouds = new ArrayList<>();

	public Level(String leveldir) {
		this.leveldir = leveldir;
	}

	public void load() throws IOException {
		// Load special background images
		ObjectInputStream is = new ObjectInputStream(Level.class.getResourceAsStream(leveldir + "/bgdecor/index"));
		int numbgdecor = is.readInt();
		is.close();
		for(int i = 0; i < numbgdecor; i++) {
			is = new ObjectInputStream(Level.class.getResourceAsStream(leveldir + "/bgdecor/" + i));
			specialImages.add(new SpecialImage(leveldir + "/bgdecor/"+i+".png", is.readDouble(), is.readDouble(), is.readDouble(), is.readDouble(), is.readDouble()));
			is.close();
		}

		is = new ObjectInputStream(Level.class.getResourceAsStream(leveldir + "/levelproperties.bin"));
		while(is.available() > 0) {
			String key = is.readUTF();
			switch(key) {
				case "graphicsOffset" -> {graphics.set(is.readDouble(), is.readDouble());}
				case "spawnpoint" -> {spawnpoint.set(is.readDouble(), is.readDouble());}
			}
		}
		is.close();

		// Load level graphics
		// loadGraphics();
		layer0 = new Image(leveldir + "/0.png");
		layer1 = new Image(leveldir + "/1.png");
		shadow0 = new Image(leveldir + "/0s.png");
		shadow1 = new Image(leveldir + "/1s.png");
		loadTileData(leveldir + "/tiles.bin");
		loadEntityData(leveldir + "/entities.bin");

		// Populate clouds
		/*Random r = new Random();
		for (int i = 0; i < bounds.w * bounds.h / 200; i++)
			clouds.add(new Cloud(r.nextDouble(bounds.x, bounds.x + bounds.w),
				r.nextDouble(bounds.y, bounds.y + bounds.h)));*/
	}

	public void setPlayer(Player p) {
		if(playerRef != null)
			entities.remove(playerRef);
		entities.add(p);
		playerRef = p;
	}

	private void loadTileData(String path) throws IOException {

		// Clear level data
		level.clear();
		entities.clear();

		// Load resource
		ObjectInputStream s = new ObjectInputStream(Level.class.getResourceAsStream(path));

		// Check version number
		byte major = s.readByte();
		byte minor = s.readByte();

		if (major != 2 || minor != 0) {
			System.err.println("Unknown level version!!");
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
	}

	public void loadEntityData(String path) throws IOException {
		ObjectInputStream s = new ObjectInputStream(Level.class.getResourceAsStream(path));
		
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

	// TODO: Do level graphics again
	public void loadGraphics() {
		// Load images and create output array
		Image l0 = new Image(leveldir + "/0.png");
		Image l1 = new Image(leveldir + "/1.png");
		Image s0 = new Image(leveldir + "/0s.png");
		Image s1 = new Image(leveldir + "/1s.png");
		images_0 = new Image[(int)Math.ceil(l0.getWidth()/CHUNK_SIZE)][(int)Math.ceil(l0.getHeight()/CHUNK_SIZE)];
		images_1 = new Image[(int)Math.ceil(l0.getWidth()/CHUNK_SIZE)][(int)Math.ceil(l0.getHeight()/CHUNK_SIZE)];
		shadow_0 = new Image[(int)Math.ceil(l0.getWidth()/CHUNK_SIZE)][(int)Math.ceil(l0.getHeight()/CHUNK_SIZE)];
		shadow_1 = new Image[(int)Math.ceil(l0.getWidth()/CHUNK_SIZE)][(int)Math.ceil(l0.getHeight()/CHUNK_SIZE)];

		// Cropping and shader info
		FbSingle f = new FbSingle();
		Shader.Image.use().push().setModel(0, 0, 1, 1);
		Shader.Image.use().setView(new Matrix4f()).setProjection(new Matrix4f().ortho(0, 1, 0, 1, -1, 1));
		glViewport(0, 0, CHUNK_SIZE, CHUNK_SIZE);
		glClearColor(0, 0, 0, 0);
		// Get crop value
		float w = (float)CHUNK_SIZE / l0.getWidth(), h = (float)CHUNK_SIZE / l0.getHeight();
		glDisable(GL_BLEND);
		for(int i = 0; i < l0.getWidth() / CHUNK_SIZE; i++) {
			for(int j = 0; j < l0.getHeight() / CHUNK_SIZE; j++) {
				f.newTexture(CHUNK_SIZE, CHUNK_SIZE);
				if(l0 != null) {
					f.drawToThis();
					glClearColor(0, 0, 0, 0);
					glClear(GL_COLOR_BUFFER_BIT);
					Shader.Image.use().image(l0).crop(w * i, h * j, w, h);
					G.rawDrawSquare();
				}
				images_0[i][j] = new Image(f.getTexture());
				
				f.newTexture(CHUNK_SIZE, CHUNK_SIZE);
				if(l1 != null) {
					f.drawToThis();
					glClearColor(0, 0, 0, 0);
					glClear(GL_COLOR_BUFFER_BIT);
					Shader.Image.use().image(l1).crop(w*i, h*j, w, h);
					G.rawDrawSquare();
				}
				images_1[i][j] = new Image(f.getTexture());
				
				f.newTexture(CHUNK_SIZE, CHUNK_SIZE);
				if(s0 != null) {
					f.drawToThis();
					glClearColor(0, 0, 0, 0);
					glClear(GL_COLOR_BUFFER_BIT);
					Shader.Image.use().image(s0).crop(w*i, h*j, w, h);
					G.rawDrawSquare();
				}
				shadow_0[i][j] = new Image(f.getTexture());
				
				f.newTexture(CHUNK_SIZE, CHUNK_SIZE);
				if(s1 != null) {
					f.drawToThis();
					glClearColor(0, 0, 0, 0);
					glClear(GL_COLOR_BUFFER_BIT);
					Shader.Image.use().image(s1).crop(w*i, h*j, w, h);
					G.rawDrawSquare();
				}
				shadow_1[i][j] = new Image(f.getTexture());
			}
		}

		Shader.Image.use().pop();
		FbSingle.drawToScreen();

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public Vector getSpawnpoint() {
		return spawnpoint;
	}

	public void tick() {
		flushEntityQueues();

		for (Cloud cloud : clouds)
			cloud.tick();
	}

	public void flushEntityQueues() {
		Camera c = Main.getCamera();
		Hitbox screen = new Hitrect(c.getX() - 0.5 * Main.WIDTH / c.getZoom(),
			c.getY() - 0.5 * Main.HEIGHT / c.getZoom(), Main.WIDTH / c.getZoom(),
			Main.HEIGHT / c.getZoom());

		for (Entity e : addQueue)
			if (!entities.add(e))
				System.err.println("Could not add entity!");
		addQueue.clear();

		for (Entity e : entities)
			if (e.getUpdateBounds().intersects(screen))
				e.tick();

		for (Entity e : removeQueue)
			if (!entities.remove(e))
				System.err.println("Asked to remove entity that does not exist...");
		removeQueue.clear();
	}

	/**
	 * A level render method
	 */
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
		for(SpecialImage i : specialImages)
			if(screen.intersects(i.getRenderBox()))
				i.render();

		xMin = Math.max((int)((xMin - graphics.x - bounds.x - 0.5) / (CHUNK_SIZE / 16.0)), 0);
		yMin = Math.max((int)((yMin - graphics.y - bounds.y - 0.5) / (CHUNK_SIZE / 16.0)), 0);
		xMax = (int)((xMax - graphics.x - bounds.x) / (CHUNK_SIZE / 16.0));
		yMax = (int)((yMax - graphics.y - bounds.y) / (CHUNK_SIZE / 16.0));
		Shader.Image.use().drawToWorld().setModel(new Matrix4f().identity());
		/*for (int x = xMin; x <= xMax && x < images_0.length; x++) {
			for (int y = yMin; y <= yMax && y < images_0[x].length; y++) {
				// System.out.printf("Drawing %d %d%n", x, y);
				Layer.LEVEL_BASE.use();
				images_0[x][y].draw(x * CHUNK_SIZE / 16.0 + graphics.x + bounds.x, y * CHUNK_SIZE / 16.0 + graphics.y + bounds.y, CHUNK_SIZE / 16.0, CHUNK_SIZE / 16.0);
				Layer.SHADOW_BASE.use();
				shadow_0[x][y].draw(x * CHUNK_SIZE / 16.0 + graphics.x + bounds.x, y * CHUNK_SIZE / 16.0 + graphics.y + bounds.y, CHUNK_SIZE / 16.0, CHUNK_SIZE / 16.0);
				Layer.LEVEL_FOREGROUND.use();
				images_1[x][y].draw(x * CHUNK_SIZE / 16.0 + graphics.x + bounds.x, y * CHUNK_SIZE / 16.0 + graphics.y + bounds.y, CHUNK_SIZE / 16.0, CHUNK_SIZE / 16.0);
				Layer.SHADOW_FOREGROUND.use();
				shadow_1[x][y].draw(x * CHUNK_SIZE / 16.0 + graphics.x + bounds.x, y * CHUNK_SIZE / 16.0 + graphics.y + bounds.y, CHUNK_SIZE / 16.0, CHUNK_SIZE / 16.0);
			}
		}*/
		Layer.LEVEL_BASE.use();
		layer0.draw(graphics.x - layer0.getWidth() / 32.0, graphics.y - layer0.getHeight()/32.0, layer0.getWidth()/16.0, layer0.getHeight()/16.0);
		Layer.SHADOW_BASE.use();
		shadow0.draw(graphics.x - shadow0.getWidth() / 32.0, graphics.y - shadow0.getHeight()/32.0, shadow0.getWidth()/16.0, shadow0.getHeight()/16.0);
		Layer.LEVEL_FOREGROUND.use();
		layer1.draw(graphics.x - layer1.getWidth() / 32.0, graphics.y - layer1.getHeight()/32.0, layer1.getWidth()/16.0, layer1.getHeight()/16.0);
		Layer.SHADOW_FOREGROUND.use();
		shadow1.draw(graphics.x - shadow1.getWidth() / 32.0, graphics.y - shadow1.getHeight()/32.0, shadow1.getWidth()/16.0, shadow1.getHeight()/16.0);

		// Sort and draw all entities and then clouds if they intersect the screen
		ArrayList<Entity> sorted = new ArrayList<>(entities.size());
		for (Entity e : entities)
			if (e.getRenderBounds().intersects(screen)) {
				sorted.add(e);
			}
		sorted.sort((a, b) -> {
			return (int) Math.signum(a.getRenderLayer() - b.getRenderLayer());
		});
		for(Entity e : sorted) {
			// System.out.println(e);
			e.render();
		}
		for (Cloud cl : clouds) {
			if (cl.getRenderBounds().intersects(screen))
				cl.render();
		}
	}

	// Used by LevelEditor to write a reference image
	public void writeImage() {
		
	}

	public void put(int x, int y, Tile tile) {
		// Expand the bounds of the level to get minimums and maximums of coords
		bounds.add(x, y);
		if (level.get(x) == null)
			level.put(x, new HashMap<Integer, Tile>());
		level.get(x).put(y, tile);
	}

	public Tile get(int x, int y) {
		if (level.get(x) == null)
			return null;
		return level.get(x).get(y);
	}

	public void addEntity(Entity e) {
		addQueue.add(e);
	}

	public void removeEntity(Entity e) {
		removeQueue.add(e);
	}

	public ArrayList<Entity> getEntities() {
		return entities;
	}

}
