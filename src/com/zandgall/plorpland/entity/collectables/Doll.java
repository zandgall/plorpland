/* zandgall

 ## Doll
 # A doll collectable

 : MADE IN NEOVIM */

package com.zandgall.plorpland.entity.collectables;

import com.zandgall.plorpland.graphics.Image;

public class Doll extends Collectable {
	public static final Image texture = new Image("/entity/doll.png");
	public Doll(double x, double y) {
		super(x, y);
	}

	public Image getTexture() {
		return texture;
	}

	public String getTitle() {
		return "Doll";
	}

	public String getDescription() {
		return "It resembles one of them, it's pretty big for a child.";
	}
}
