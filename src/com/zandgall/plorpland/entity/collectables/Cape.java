/* zandgall

 ## Cape
 # A cape collectable

 : MADE IN NEOVIM */

package com.zandgall.plorpland.entity.collectables;

import com.zandgall.plorpland.graphics.Image;

public class Cape extends Collectable {
	public static final Image texture = new Image("/entity/cape.png");

	public Cape(double x, double y) {
		super(x, y);
	}

	public Image getTexture() {
		return texture;
	}

	public String getTitle() {
		return "Cape";
	}

	public String getDescription() {
		return "A little fabric to play dress up. It's too big for grown-ups.";
	}
}
