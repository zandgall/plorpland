/* zandgall

 ## Rect
 # A simple class to represent a rectangle, that can be checked for intersections with other rectangles
 # Can add points and rectangles to spread the rectangle to cover an area

 : MADE IN NEOVIM */

package com.zandgall.plorpland.util;

import java.io.Serializable;

public class Rect implements Serializable {
	
	boolean empty = true;
	public double x, y, w, h;

	public Rect() {}

	public Rect(double x, double y, double w, double h) {
		set(x, y, w, h);
	}

	public void set(double x, double y, double w, double h) {
		empty = false;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public boolean intersects(double x, double y, double w, double h) {
		return !empty 
			&& this.x + this.w > x
			&& this.x < x + w
			&& this.y + this.h > y
			&& this.y < y + h;
	}
	public boolean intersects(Rect other) {
		return !empty && other.intersects(x, y, w, h);
	}

	public double centerX() {
		return x + w * 0.5;
	}

	public double centerY() {
		return y + h * 0.5;
	}

	public void add(double x, double y, double w, double h) {
		if(empty) {
			set(x, y, w, h);
			return;
		}

		double nX = Math.min(this.x, x), nY = Math.min(this.y, y);
		double nW = Math.max(this.x + this.w, x + w) - nX, nH = Math.max(this.y + this.h, y + h) - nY;
		set(nX, nY, nW, nH);
	}

	public void add(Rect other) {
		add(other.x, other.y, other.w, other.h);
	}

	public void add(double a, double b) {
		if(empty) {
			set(a, b, 1, 1);
			return;
		}
		double nX = Math.min(x, a), nY = Math.min(y, b), nW = Math.max(x + w, a) - nX, nH = Math.max(y + h, b) - nY;
		set(nX, nY, nW, nH);
	}

	public boolean contains(double x, double y, double w, double h) {
		return !empty 
			&& this.x + this.w >= x + w
			&& this.x <= x
			&& this.y + this.h >= y + h
			&& this.y <= y;
	}
}
