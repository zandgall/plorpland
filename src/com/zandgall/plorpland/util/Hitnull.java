/* zandgall

 ## Hitnull
 # A type of hitbox that always returns false on intersection

 : MADE IN NEOVIM */

package com.zandgall.plorpland.util;

public class Hitnull extends Hitbox {
	public static final Hitnull instance = new Hitnull();

	public Hitnull() {}

	public boolean intersects(double x, double y, double w, double h) {
		return false;
	}

	public boolean intersects(Hitbox box) {
		return false;
	}

	public Hitbox translate(double x, double y) {
		return this;
	}

	public Rect getBounds() {
		return new Rect();
	}
}
