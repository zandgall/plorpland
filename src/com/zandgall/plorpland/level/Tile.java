/* zandgall

 ## Tile
 # Present an assortment of tiles in order to fulfill the solidity and graphics of a level 

 : MADE IN NEOVIM */

package com.zandgall.plorpland.level;

import java.util.ArrayList;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Hitrect;

public abstract class Tile {

	private static ArrayList<Tile> tilemap = new ArrayList<Tile>();

	// Define all game tiles
	public static final Tile empty = new EmptyTile();
	public static final Tile solid = new SolidTile();
	public static final Tile diag0 = new DiagonalTile(0);
	public static final Tile diag1 = new DiagonalTile(1);
	public static final Tile diag2 = new DiagonalTile(2);
	public static final Tile diag3 = new DiagonalTile(3);

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
	 * A tile type that does not render
	 */
	private static class EmptyTile extends Tile {
		public EmptyTile() {
			super();
		}

		public Hitbox solidBounds(int x, int y) {
			return null;
		}
	}

	/**
	 * A tile type that draws a solid color
	 */
	private static class SolidTile extends Tile {
		private Hitbox solid;

		public SolidTile() {
			super();
			this.solid = new Hitrect(0, 0, 1, 1);
		}

		public Hitbox solidBounds(int x, int y) {
			return solid.translate(x, y);
		}
	}

	// TODO: add sloped hitbox type
	private static class DiagonalTile extends Tile {
		private Hitbox solid;

		public DiagonalTile(int orientation) {
			super();
			this.solid = new Hitrect(0, 0, 1, 1);
		}

		public Hitbox solidBounds(int x, int y) {
			return solid.translate(x, y);
		}
	}

}
