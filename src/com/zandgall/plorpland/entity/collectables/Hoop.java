/* zandgall

 ## Hoop
 # A hoop collectable

 : MADE IN NEOVIM */

package com.zandgall.plorpland.entity.collectables;

import com.zandgall.plorpland.graphics.Image;

public class Hoop extends Collectable {
	private static final Image texture = new Image("/entity/hoop.png");

	public Hoop(double x, double y) {
		super(x, y);
	}

	public Image getTexture() {return texture;}

	public String getTitle() {
		return "Hoop";
	}
	
	public String getDescription() {
		return "A toy that you can swing around your body, or roll on its own.";
	}
}
