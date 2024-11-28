package com.zandgall.plorpland.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	public Image layer0 = Image.BLANK, layer1 = Image.BLANK, shadow0 = Image.BLANK, shadow1 = Image.BLANK;

	protected ArrayList<ArrayList<SpecialImage>> specialImages = new ArrayList<>();

	protected ArrayList<Entity> entities = new ArrayList<>();

	public Level() {
		specialImages.add(new ArrayList<>());
	}

	public void write(File leveldir) throws IOException {
		leveldir.mkdirs();
		new File(leveldir + "/bgdecor/").mkdirs();
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(leveldir + "/bgdecor/index"));
		os.writeInt(specialImages.get(0).size());
		os.close();
		for(int i = 0; i < specialImages.get(0).size(); i++) {
			specialImages.get(0).get(i).getImage().writeTo(leveldir + "/bgdecor/" + i + ".png");
			ObjectOutputStream spiOS = new ObjectOutputStream(new FileOutputStream(leveldir + "/bgdecor/" + i));
			spiOS.writeDouble(specialImages.get(0).get(i).getX());
			spiOS.writeDouble(specialImages.get(0).get(i).getY());
			spiOS.writeDouble(specialImages.get(0).get(i).getXOff());
			spiOS.writeDouble(specialImages.get(0).get(i).getYOff());
			spiOS.writeDouble(specialImages.get(0).get(i).getDamping());
			spiOS.close();
		}
		layer0.writeTo(leveldir + "/0.png");
		layer1.writeTo(leveldir + "/1.png");
		shadow0.writeTo(leveldir + "/0s.png");
		shadow1.writeTo(leveldir + "/1s.png");
		os = new ObjectOutputStream(new FileOutputStream(leveldir + "/graphicsoffset.bin"));
		os.writeDouble(graphicsX);
		os.writeDouble(graphicsY);
		os.close();
		writeTileData(leveldir + "/tiles.bin");
		writeEntityData(leveldir + "/entities.bin");
	}

	public void writeTileData(String path) throws IOException {
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(path));

		// Version number
		os.writeByte(2);
		os.writeByte(0);

		Rect r = new Rect();
		for(int x : tiles.keySet())
			for(int y : tiles.get(x).keySet())
				if(tiles.get(x).get(y).getID() != 0)
					r.add(x, y);

		os.writeInt((int)r.y);
		os.writeInt((int)r.h);
		for(int y = (int)r.y; y <= r.y + r.h; y++) {
			boolean writing = false, lineEnd = false;
			for(int x = (int)r.x; x <= r.x + r.w && !lineEnd; x++) {
				if(get(x, y) == null || get(x, y).getID() == 0) {
					if(!writing)
						continue; // Havent started writing yet, wait for tile
					
					// write one 0 and figure out what to do next
					os.writeInt(0);

					// we wrote tiles and hit an empty tile, check if this is the end of
					// this line of tiles
					lineEnd = true;
					for(int i = x; i <= r.w; i++) {
						if(get(i, y) != null && get(i, y).getID() != 0) {
							lineEnd = false;
							os.writeInt(i - x); // write number of empty tiles in this line
							// System.out.printf("%d: 0s space from %x to %x%n", y, x, i);
							x = i - 1;
							break;
						}
					}
					if(lineEnd) {
						os.writeInt(0);
						// System.out.printf("%d: eol at %d%n", y, x);
					}
				} else {
					// First tile being written, note x position
					if(!writing) {
						os.writeInt(x);
						writing = true;
					}
					os.writeInt(get(x, y).getID());
				}
			}

			// if we didn't write anything, add blank line "0 0 0"
			// (equiv of x = 0, newline)
			if(!writing) {
				os.writeInt(0);
				// System.out.printf("%d: blank line%n", y);
			}
			if(!lineEnd) {
				os.writeInt(0);
				os.writeInt(0);
				// System.out.printf("%d: premature new line%n", y);
			}
		}

		os.close();
	}

	public void writeEntityData(String path) throws IOException {
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(path));
		os.writeByte(2);
		os.writeByte(0);
		os.writeInt(entities.size());
		for(Entity e : entities)
			os.writeObject(e);
		os.close();
	}

	public void load(File leveldir) throws IOException {
		ObjectInputStream is = new ObjectInputStream(new FileInputStream(leveldir + "/bgdecor/index"));
		int numbgdecor = is.readInt();
		is.close();
		specialImages.get(0).clear();
		for(int i = 0; i < numbgdecor; i++) {
			is = new ObjectInputStream(new FileInputStream(leveldir + "/bgdecor/" + i));
			specialImages.get(0).add(new SpecialImage("", is.readDouble(), is.readDouble(), is.readDouble(), is.readDouble(), is.readDouble()));
			specialImages.get(0).get(i).setImage(new Image(Image.textureFrom(new FileInputStream(leveldir + "/bgdecor/" + i + ".png"))));
			is.close();
		}

		// Load level graphics
		// loadGraphics(leveldir);
		is = new ObjectInputStream(new FileInputStream(leveldir + "/graphicsoffset.bin"));
		graphicsX = is.readDouble();
		graphicsY = is.readDouble();
		is.close();

		layer0 = new Image(Image.textureFrom(new FileInputStream(leveldir + "/0.png")));
		layer1 = new Image(Image.textureFrom(new FileInputStream(leveldir + "/1.png")));
		shadow0 = new Image(Image.textureFrom(new FileInputStream(leveldir + "/0s.png")));
		shadow1 = new Image(Image.textureFrom(new FileInputStream(leveldir + "/1s.png")));
		loadTileData(leveldir + "/tiles.bin");
		loadEntityData(leveldir + "/entities.bin");
	}

	private void loadTileData(String path) throws IOException {

		// Clear level data
		tiles.clear();
		entities.clear();

		// Load resource
		ObjectInputStream s = new ObjectInputStream(new FileInputStream(path));

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
