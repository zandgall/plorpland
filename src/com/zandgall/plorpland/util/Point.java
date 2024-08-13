/* zandgall

 ## Point
 # A simple class to represent an integer point

 : MADE IN NEOVIM */

package com.zandgall.plorpland.util;

import java.io.Serializable;
import java.util.Objects;

public class Point implements Comparable<Point>, Serializable {

	private int hash;

	public int x, y;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
		hash = Objects.hash(x, y);
	}

	public int compareTo(Point other) {
		if (x != other.x)
			return x > other.x ? 1 : -1;
		if (y != other.y)
			return y > other.y ? 1 : -1;
		return 0;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Point p)
			return p.x == x && p.y == y;
		return false;
	}

	@Override
	public int hashCode() {
		return hash;
	}
}
