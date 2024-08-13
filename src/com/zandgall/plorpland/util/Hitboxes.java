/* zandgall

 ## Hitboxes
 # A hitbox type that consists of several rectangles joined together.

 : MADE IN NEOVIM */

package com.zandgall.plorpland.util;

import java.util.ArrayList;

public class Hitboxes extends Hitbox {
	protected ArrayList<Rect> boxes = new ArrayList<>(1);

	protected Rect bounds;

	public Hitboxes() {
		bounds = new Rect();
	}

	public Hitboxes(double x, double y, double width, double height) {
		boxes.add(new Rect(x, y, width, height));
		bounds = new Rect(x, y, width, height);
	}

	public Hitboxes(Rect box) {
		boxes.add(box);
		bounds = box;
	}

	public void add(double x, double y, double width, double height) {
		add(new Rect(x, y, width, height));
	}

	public void add(Rect rect) {
		boxes.add(rect);
		bounds.add(rect);
		// Sort in terms of biggest box to smallest, so that bigger boxes are checked
		// first in terms of intersection and such
		boxes.sort((a, b) -> {
			return (int) (a.w * a.h - b.w * b.h);
		});
	}

	public void add(Hitboxes other) {
		for (Rect box : other.boxes)
			add(box);
	}

	public void add(Hitbox other) {
		if(other instanceof Hitboxes)
			add((Hitboxes)other);
		else if(other instanceof Hitrect)
			add(((Hitrect)other).getBounds());
		else
			add(other.getBounds());
	}

	public boolean intersects(Rect rect) {
		if (boxes.size() > 1 && !bounds.intersects(rect))
			return false;
		for (Rect box : boxes)
			if (box.intersects(rect))
				return true;
		return false;
	}

	public boolean intersects(double x, double y, double width, double height) {
		if (boxes.size() > 1 && !bounds.intersects(x, y, width, height))
			return false;
		for (Rect box : boxes)
			if (box.intersects(x, y, width, height))
				return true;
		return false;
	}

	public boolean intersects(Hitbox other) {
		if (boxes.size() > 1 && !other.intersects(bounds))
			return false;
		for (Rect box : boxes)
			if (other.intersects(box))
				return true;
		return false;
	}

	public Hitbox translate(double x, double y) {
		Hitboxes out = new Hitboxes();
		for (Rect box : boxes) {
			out.add(new Rect(box.x + x, box.y + y, box.w, box.h));
		}
		return out;
	}

	public Hitbox translate(Vector vector) {
		return translate(vector.x, vector.y);
	}

	public Rect getBounds() {
		return bounds;
	}

	public ArrayList<Rect> getBoxes() {
		return boxes;
	}

	public static Hitboxes unit() {
		return new Hitboxes(0, 0, 1, 1);
	}

	public boolean coversUnit() {
		return bounds.contains(0, 0, 1, 1);
	}

}
