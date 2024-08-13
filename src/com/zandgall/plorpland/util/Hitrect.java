/* zandgall

 ## Hitrect
 # A hitbox that consists of a single rectangle

 : MADE IN NEOVIM */

package com.zandgall.plorpland.util;

public class Hitrect extends Hitbox {
	private Rect rect;

	public Hitrect() {}

	public Hitrect(Rect rect) {
		this.rect = rect;
	}

	public Hitrect(double x, double y, double width, double height) {
		rect = new Rect(x, y, width, height);
	}

	public void set(double x, double y, double width, double height) {
		rect.set(x, y, width, height);
	}

	public boolean intersects(double x, double y, double width, double height) {
		return rect.intersects(x, y, width, height);
	}

	public boolean intersects(Hitbox other) {
		return other.intersects(rect);
	}

	public Hitbox translate(double x, double y) {
		return new Hitrect(rect.x + x, rect.y + y, rect.w, rect.h);
	}

	public Rect getBounds() {
		return rect;
	}
}
