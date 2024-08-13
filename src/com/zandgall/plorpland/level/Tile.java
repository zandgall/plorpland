/* zandgall

 ## Tile
 # Present an assortment of tiles in order to fulfill the solidity and graphics of a level

 TODO: This whole place is redundant with new graphics, rework for hitboxes only

 : MADE IN NEOVIM */

package com.zandgall.plorpland.level;

import java.util.ArrayList;
import java.util.Scanner;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.GraphicsContext;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Hitboxes;
import com.zandgall.plorpland.util.Hitrect;

public abstract class Tile {

	private static ArrayList<Tile> tilemap = new ArrayList<Tile>();

	// Define all game tiles
	public static final Tile empty = new EmptyTile();
	public static final Tile ground = new ColorTile(Color.web("#369c6c"));
	public static final Tile grass = new ImageTile("/tiles/grass.png");
	public static final Tile grass_tl = new ImageTile("/tiles/grass_tl.png");
	public static final Tile grass_tr = new ImageTile("/tiles/grass_tr.png");
	public static final Tile grass_bl = new ImageTile("/tiles/grass_bl.png");
	public static final Tile grass_br = new ImageTile("/tiles/grass_br.png");
	public static final Tile thickgrass = new ImageTile("/tiles/thickgrass.png");

	public static final Tile tutorial[] = new Tile[] {
			new ImageTile("/tiles/tut_up.png"),
			new ImageTile("/tiles/tut_right.png"),
			new ImageTile("/tiles/tut_down.png"),
			new ImageTile("/tiles/tut_left.png"),
			new ImageTile("/tiles/tut_z.png"),
			new ImageTile("/tiles/tut_x.png"),
	};

	public static final Tile path[] = new Tile[] {
			new ImageTile("/tiles/path.png"),
			new ImageTile("/tiles/path_b.png"),
			new ImageTile("/tiles/path_r.png"),
			new ImageTile("/tiles/path_t.png"),
			new ImageTile("/tiles/path_l.png"),
			new ImageTile("/tiles/path_str.png"),
			new ImageTile("/tiles/path_stl.png"),
			new ImageTile("/tiles/path_sbr.png"),
			new ImageTile("/tiles/path_sbl.png"),
			new ImageTile("/tiles/path_btr.png"),
			new ImageTile("/tiles/path_btl.png"),
			new ImageTile("/tiles/path_bbr.png"),
			new ImageTile("/tiles/path_bbl.png"),
			new ImageTile("/tiles/path_s0.png"),
			new ImageTile("/tiles/path_s1.png"),
	};

	public static final Tile flower[] = new Tile[] {
			new ImageTile("/tiles/flower_w.png"),
			new ImageTile("/tiles/flower_wbl.png"),
			new ImageTile("/tiles/flower_wbr.png"),
			new ImageTile("/tiles/flower_wtl.png"),
			new ImageTile("/tiles/flower_wtr.png"),
			new ImageTile("/tiles/flower_r.png"),
			new ImageTile("/tiles/flower_rbl.png"),
			new ImageTile("/tiles/flower_rbr.png"),
			new ImageTile("/tiles/flower_rtl.png"),
			new ImageTile("/tiles/flower_rtr.png"),
			new ImageTile("/tiles/flower_b.png"),
			new ImageTile("/tiles/flower_bbl.png"),
			new ImageTile("/tiles/flower_bbr.png"),
			new ImageTile("/tiles/flower_btl.png"),
			new ImageTile("/tiles/flower_btr.png"),
	};

	public static final Tile walls[] = ImageTile.loadCombinations("/tiles/walls/wall.combinations");
	public static final Tile edges[] = ImageTile.loadCombinations("/tiles/walls/edges.combinations");

	public static final Tile tut_sword[] = {
			new ImageTile("/tiles/tut_sword1.png"),
			new ImageTile("/tiles/tut_sword2.png")
	};

	private final int ID;

	public Tile() {
		this.ID = tilemap.size();
		tilemap.add(this);
	}

	public static Tile get(int ID) {
		if (ID >= tilemap.size())
			return empty;
		if (ID < 0)
			return tilemap.get(tilemap.size() - 1);
		return tilemap.get(ID);
	}

	public int getID() {
		return ID;
	}

	public static int maxID() {
		return tilemap.size() - 1;
	}

	/**
	 * Determine whether the tile is solid or not, returning null if not, and a
	 * Hitbox if it is
	 */
	public abstract Hitbox solidBounds(int x, int y);

	/**
	 * Draw a 1x1 size tile. The transformation is set beforehand, including
	 * positioning.
	 * Additional transformations can be applied however, just be sure to save and
	 * restore before and after drawing!
	 */
	public abstract void render(GraphicsContext g);

	/**
	 * A tile type that does not render
	 */
	private static class EmptyTile extends Tile {
		public EmptyTile() {
			super();
		}

		public void render(GraphicsContext g) {
		}

		public Hitbox solidBounds(int x, int y) {
			return null;
		}
	}

	/**
	 * A tile type that draws a solid color
	 */
	private static class ColorTile extends Tile {
		private Color color;
		private Hitbox solid;

		public ColorTile(Color color) {
			super();
			this.color = color;
			this.solid = null;
		}

		public Hitbox solidBounds(int x, int y) {
			if (solid == null)
				return null;
			return solid.translate(x, y);
		}

		public void render(GraphicsContext g) {
			g.setFill(color);
			double dx = 1 / g.getTransform().getMxx();
			double dy = 1 / g.getTransform().getMyy();
			g.fillRect(-dx, -dy, 1.0 + dx*2, 1.0 + dy*2);
		}
	}

	/**
	 * A tile that displays as an image
	 */
	private static class ImageTile extends Tile {
		private Image image;
		private Hitbox solid = null;

		public ImageTile(String path) {
			super();
			image = new Image(path);
		}

		public ImageTile(Image image, Hitbox solid) {
			this.image = image;
			this.solid = solid;
		}

		public Hitbox solidBounds(int x, int y) {
			if (solid == null)
				return null;
			return solid.translate(x, y);
		}

		public void render(GraphicsContext g) {
			if (image == null)
				return;
			g.drawImage(image, 0.0, 0.0, 1.0, 1.0);
		}

		// Creates an image that overlays several input images
		public static Image overlay(String... paths) {
			// Create a pane to hold imageviews holding every input image (given by paths)
			StackPane combiner = new StackPane();
			ImageView views[] = new ImageView[paths.length];
			for (int i = 0; i < paths.length; i++) {
				views[i] = new ImageView(new Image(paths[i]));
				combiner.getChildren().add(views[i]);
			}
			// Take a 'snapshot' which returns an image of everything overlayed
			return combiner.snapshot(null, null);
		}

		public static ImageTile[] loadCombinations(String path) {
			Scanner s = new Scanner(Tile.class.getResourceAsStream(path));
			ArrayList<ImageTile> output = new ArrayList<>();

			// For every line in the file,
			while (s.hasNext()) {
				// Load each path split by space
				String tags[] = s.nextLine().split("\\s+");
				ArrayList<String> paths = new ArrayList<>();
				Hitbox box = new Hitboxes();

				// Check if there's a .box and/or .png file with the given path,
				// Overlaying them all in the end
				for (int i = 0; i < tags.length; i++) {
					if (Tile.class.getResource(tags[i] + ".box") != null) {
						Hitbox c = Hitbox.load(tags[i] + ".box");
						if(c instanceof Hitrect)
							box = c;
						else if(box instanceof Hitboxes)
							((Hitboxes)box).add(Hitbox.load(tags[i] + ".box"));
					}
					if (Tile.class.getResource(tags[i] + ".png") != null)
						paths.add(tags[i] + ".png");
				}
				output.add(new ImageTile(overlay(paths.toArray(new String[paths.size()])), box));
			}
			s.close();
			return output.toArray(new ImageTile[output.size()]);
		}
	}
}
