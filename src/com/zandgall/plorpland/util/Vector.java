/* zandgall

 ## Vector
 # Stores a double coordinate / 2d Vector

 : MADE IN NEOVIM */

package com.zandgall.plorpland.util;

import java.io.Serializable;

public class Vector implements Serializable {
	public double x, y;

	public Vector() {
		set(0, 0);
	}

	public Vector(double x, double y) {
		set(x, y);
	}

	public Vector set(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Vector set(Vector from) {
		return set(from.x, from.y);
	}

	public Vector add(double x, double y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public Vector add(Vector b) {
		add(b.x, b.y);
		return this;
	}

	public Vector scale(double s) {
		x *= s;
		y *= s;
		return this;
	}

	public Vector getAdd(double x, double y) {
		return new Vector(this.x + x, this.y + y);
	}

	public Vector getAdd(Vector b) {
		return new Vector(x + b.x, y + b.y);
	}

	public Vector getSub(Vector b) {
		return new Vector(x - b.x, y - b.y);
	}

	public Vector getScale(double s) {
		return new Vector(x * s, y * s);
	}

	/**
	 * Get square distance to other vector
	 */
	public double sqDist(Vector other) {
		return (x - other.x)*(x - other.x) + (y-other.y)*(y - other.y);
	}

	public double dist(Vector other) {
		return Math.sqrt(sqDist(other));
	}

	public double sqLength() {
		return x * x + y * y;
	}

	public double length() {
		return Math.sqrt(sqLength());
	}

	public Vector unit() {
		return getScale(1/length());
	}

	/**
	 * Get a unit vector pointing from this vector to the given vector
	 * @param towards The vector to point towards
	 */
	public Vector unitDir(Vector towards) {
		return towards.getSub(this).unit();
	}

	public Vector clone() {
		return new Vector(x, y);
	}

	public static Vector ofAngle(double radians) {
		return new Vector(Math.cos(radians), Math.sin(radians));
	}

}
