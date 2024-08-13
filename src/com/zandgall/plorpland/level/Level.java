/* zandgall

 ## Level
 # Stores information about the world, and serves as a wrapper to update and render all things in a level

 : MADE IN NEOVIM */

package com.zandgall.plorpland.level;

import com.zandgall.plorpland.entity.EntityRegistry;
import com.zandgall.plorpland.entity.Entity;
import com.zandgall.plorpland.entity.Cloud;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Hitrect;
import com.zandgall.plorpland.util.Rect;
import com.zandgall.plorpland.Camera;
import com.zandgall.plorpland.Main;

import javafx.scene.transform.Affine;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;

import java.io.ObjectInputStream;
import java.io.File;
import java.io.IOException;

public class Level {
	public Rect bounds = new Rect();
	private HashMap<Integer, HashMap<Integer, Tile>> level = new HashMap<>();

	private static final int CHUNK_SIZE = 128;
	private static final boolean USE_TILES = false;

	// Level graphics
	// TODO: Pick.. better names
	private Image[][] images_0, images_1, shadow_0, shadow_1;

	// A set of special background images, only 1 layer (0) is used right now
	private ArrayList<ArrayList<SpecialImage>> specialImages = new ArrayList<>();

	// List of entities and two queues for removal and addition
	private ArrayList<Entity> entities = new ArrayList<>(), removeQueue = new ArrayList<>(),
			addQueue = new ArrayList<>();
	private ArrayList<Cloud> clouds = new ArrayList<>();

	public Level() {
		// Load special background images
		Scanner s = new Scanner(Level.class.getResourceAsStream("/special.txt"));
		specialImages.add(new ArrayList<>());
		while(s.hasNextLine()) {
			String line = s.nextLine();
			Scanner p = new Scanner(line);
			specialImages.get(p.nextInt()).add(new SpecialImage(p.next(), p.nextDouble(), p.nextDouble(), p.nextDouble(), p.nextDouble(), p.nextDouble()));
			p.close();
		}
		s.close();

		// Load level graphics
		if(!USE_TILES)
			loadGraphics();
	}

	public void load(String path) throws IOException {

		// Clear level data
		level.clear();
		entities.clear();

		// Load resource
		ObjectInputStream s = new ObjectInputStream(Level.class.getResourceAsStream(path));

		// Check version number
		byte major = s.readByte();
		byte minor = s.readByte();

		if (major != 1 || (minor != 1 && minor != 2)) {
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
						if(major == 1 && minor == 1)
							x ++; // 1.1 has off by one error
					}
				} else {
					// Place the tile
					put(x, y, Tile.get(tile));
					x++;
				}
			}
		}

		int numEntities = s.readInt();
		for (int i = 0; i < numEntities; i++) {
			// Read data and construct entity
			String entityName = s.readUTF();
			double x = s.readDouble(), y = s.readDouble();

			Class<?> entityClass = EntityRegistry.nameMap.get(entityName);
			addEntity(EntityRegistry.construct(entityClass, x, y));
		}

		// Populate clouds
		Random r = new Random();
		for (int i = 0; i < bounds.w * bounds.h / 200; i++)
			clouds.add(new Cloud(r.nextDouble(bounds.x, bounds.x + bounds.w),
					r.nextDouble(bounds.y, bounds.y + bounds.h)));

		s.close();

	}

	public void loadGraphics() {
		// Objects to crop the loaded images
		Canvas cropper = new Canvas(CHUNK_SIZE, CHUNK_SIZE);
		SnapshotParameters p = new SnapshotParameters();
		p.setFill(Color.TRANSPARENT);
		GraphicsContext g = cropper.getGraphicsContext2D();

		// Load images and create output array
		Image l0 = new Image("/level_0.png");
		Image l1 = new Image("/level_1.png");
		Image s0 = new Image("/shadow_0.png");
		Image s1 = new Image("/shadow_1.png");
		images_0 = new Image[(int)Math.ceil(l0.getWidth()/CHUNK_SIZE)][(int)Math.ceil(l0.getHeight()/CHUNK_SIZE)];
		images_1 = new Image[(int)Math.ceil(l0.getWidth()/CHUNK_SIZE)][(int)Math.ceil(l0.getHeight()/CHUNK_SIZE)];
		shadow_0 = new Image[(int)Math.ceil(l0.getWidth()/CHUNK_SIZE)][(int)Math.ceil(l0.getHeight()/CHUNK_SIZE)];
		shadow_1 = new Image[(int)Math.ceil(l0.getWidth()/CHUNK_SIZE)][(int)Math.ceil(l0.getHeight()/CHUNK_SIZE)];

		// Loop through every chunk, cropping each image and putting it in the images arrays
		for(int i = 0; i < l0.getWidth() / CHUNK_SIZE; i++) {
			for(int j = 0; j < l0.getHeight() / CHUNK_SIZE; j++) {
				g.clearRect(0, 0, CHUNK_SIZE, CHUNK_SIZE);
				g.drawImage(l0, -i*CHUNK_SIZE, -j*CHUNK_SIZE);
				images_0[i][j] = cropper.snapshot(p, null);
				g.clearRect(0, 0, CHUNK_SIZE, CHUNK_SIZE);
				g.drawImage(l1, -i*CHUNK_SIZE, -j*CHUNK_SIZE);
				images_1[i][j] = cropper.snapshot(p, null);
				g.clearRect(0, 0, CHUNK_SIZE, CHUNK_SIZE);
				g.drawImage(s0, -i*CHUNK_SIZE, -j*CHUNK_SIZE);
				shadow_0[i][j] = cropper.snapshot(p, null);
				g.clearRect(0, 0, CHUNK_SIZE, CHUNK_SIZE);
				g.drawImage(s1, -i*CHUNK_SIZE, -j*CHUNK_SIZE);
				shadow_1[i][j] = cropper.snapshot(p, null);
			}
		}

	}

	public void tick() {
		flushEntityQueues();

		for (Cloud cloud : clouds)
			cloud.tick();
	}

	public void flushEntityQueues() {
		Camera c = Main.getCamera();
		Hitbox screen = new Hitrect(c.getX() - 0.5 * Main.layer_0.getWidth() / c.getZoom(),
			c.getY() - 0.5 * Main.layer_0.getHeight() / c.getZoom(), Main.layer_0.getWidth() / c.getZoom(),
			Main.layer_0.getHeight() / c.getZoom());

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
	 * A level render method provided with several graphical layers
	 * 
	 * @param context_0 A context for layer 0 - usually reserved for tiles
	 * @param context_1 A context for layer 1
	 * @param shadow_0  A context specifically for shadows (applies to layers 1
	 *                  and 0)
	 * @param context_2 A context for layer 2
	 * @param shadow_1  A context specifically for shadows (applies to all layers
	 *                  below)
	 */
	public void render(GraphicsContext context_0, GraphicsContext context_1, GraphicsContext shadow_0,
			GraphicsContext context_2, GraphicsContext shadow_1) {
		// All contexts should use the same transform at this time
		Affine af = context_0.getTransform();
		int xMin = (int) Math.floor(-af.getTx() / af.getMxx());
		int xMax = (int) (-af.getTx() / af.getMxx() + (1 / af.getMxx()) * Main.layer_0.getWidth());
		int yMin = (int) Math.floor(-af.getTy() / af.getMyy());
		int yMax = (int) (-af.getTy() / af.getMyy() + (1 / af.getMyy()) * Main.layer_0.getHeight());
	
		Rect screenBounds = new Rect(-af.getTx() / af.getMxx(), -af.getTy() / af.getMyy(),
				Main.layer_0.getWidth() / af.getMxx(), Main.layer_0.getHeight() / af.getMyy());

		for(SpecialImage i : specialImages.get(0))
			if(screenBounds.intersects(i.getRenderBox()))
				i.render(context_0);

		if(USE_TILES)
			for (int x = xMin; x <= xMax; x++)
				for (int y = yMin; y <= yMax; y++) {
					if (level.get(x) == null || level.get(x).get(y) == null)
						continue;
					context_0.save();
					context_0.translate(x, y);
					level.get(x).get(y).render(context_0);
					context_0.restore();
				}
		else {
			xMin = Math.max((int)(xMin - bounds.x) / (CHUNK_SIZE / 16), 0);
			yMin = Math.max((int)(yMin - bounds.y) / (CHUNK_SIZE / 16), 0);
			xMax = (int)(xMax - bounds.x) / (CHUNK_SIZE / 16);
			yMax = (int)(yMax - bounds.y) / (CHUNK_SIZE / 16);
			for (int x = xMin; x <= xMax && x < images_0.length; x++) {
				for (int y = yMin; y <= yMax && y < images_0[x].length; y++) {
					context_0.drawImage(images_0[x][y], x*CHUNK_SIZE / 16 + bounds.x, y * CHUNK_SIZE / 16 + bounds.y, CHUNK_SIZE/16, CHUNK_SIZE / 16);
					context_2.drawImage(images_1[x][y], x*CHUNK_SIZE / 16 + bounds.x, y * CHUNK_SIZE / 16 + bounds.y, CHUNK_SIZE/16, CHUNK_SIZE / 16);
					shadow_0.drawImage(this.shadow_0[x][y], x*CHUNK_SIZE / 16 + bounds.x, y * CHUNK_SIZE / 16 + bounds.y, CHUNK_SIZE/16, CHUNK_SIZE / 16);
					shadow_1.drawImage(this.shadow_1[x][y], x*CHUNK_SIZE / 16 + bounds.x, y * CHUNK_SIZE / 16 + bounds.y, CHUNK_SIZE/16, CHUNK_SIZE / 16);
				}
			}
		}

		// Sort and draw all entities and then clouds if they intersect the screen
		ArrayList<Entity> sorted = new ArrayList<>(entities.size());
		for (Entity e : entities)
			if (e.getRenderBounds().intersects(screenBounds))
				sorted.add(e);
		sorted.sort((a, b) -> {
			return (int) Math.signum(a.getRenderLayer() - b.getRenderLayer());
		});
		for(Entity e : sorted)
			e.render(context_1, shadow_0, context_2);
		for (Cloud c : clouds) {
			if (c.getRenderBounds().intersects(screenBounds))
				c.render(shadow_1);
		}
	}

	// Used by LevelEditor to write a reference image
	public void writeImage() {
		Canvas c = new Canvas(bounds.w * 16, bounds.h * 16);
		GraphicsContext g = c.getGraphicsContext2D();
		for(Map.Entry<Integer, HashMap<Integer, Tile>> xT : level.entrySet())
			for(Map.Entry<Integer, Tile> yT : xT.getValue().entrySet()) {
				g.setTransform(16, 0, 0, 16, xT.getKey()*16-bounds.x*16, yT.getKey()*16-bounds.y*16);
				yT.getValue().render(g);
			}
		try {
			ImageIO.write(SwingFXUtils.fromFXImage(c.snapshot(null, null), null), "png", new File("res/level.png"));
		} catch (IOException io) {
			System.err.println("Couldn't write level image");
			io.printStackTrace();
		}
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
