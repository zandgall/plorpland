/* zandgall

 ## Hitbox
 # An abstract class that describes how a general hitbox may be interacted with. Describing an intersection with a rect or another hitbox

 : MADE IN NEOVIM */

package com.zandgall.plorpland.util;

import java.io.Serializable;
import java.util.Scanner;

public abstract class Hitbox implements Serializable {
	public Hitbox() {}

	public boolean intersects(Rect rect) {
		return intersects(rect.x, rect.y, rect.w, rect.h);
	}

	public abstract boolean intersects(double x, double y, double width, double height);

	public abstract boolean intersects(Hitbox other);

	public abstract Hitbox translate(double x, double y);

	public Hitbox translate(Vector vector) {
		return translate(vector.x, vector.y);
	}

	public abstract Rect getBounds();

	/**
	 * Load hitbox from a text file. Used to load sets of different hitboxes
	 * 
	 * @param filepath The file to read hitboxes from
	 */
	public static Hitbox load(String path) {
		try {
			Scanner s = new Scanner(Hitbox.class.getResourceAsStream(path));
			int numBoxes = s.nextInt();
			if(numBoxes == 0) {
				s.close();
				return new Hitnull();
			}
			if(numBoxes == 1) {
				Hitrect out = new Hitrect(s.nextDouble(), s.nextDouble(), s.nextDouble(), s.nextDouble());
				s.close();
				return out;
			}

			Hitboxes out = new Hitboxes();
			for (int i = 0; i < numBoxes; i++)
				out.add(s.nextDouble(), s.nextDouble(), s.nextDouble(), s.nextDouble());
			s.close();
			return out;
		} catch (Exception e) {
			return new Hitnull();
		}
	}

	public static Hitbox unit() {
		return new Hitrect(0, 0, 1, 1);
	}

}
