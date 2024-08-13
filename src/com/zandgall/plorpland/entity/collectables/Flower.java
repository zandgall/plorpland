/* zandgall

 ## Flower
 # A flower collectable

 : MADE IN NEOVIM */

package com.zandgall.plorpland.entity.collectables;

import javafx.scene.image.Image;

public class Flower extends Collectable {
	public static final Image texture = new Image("/entity/flower.png");
	public Flower(double x, double y) {
		super(x, y);
	}

	public Image getTexture() {
		return texture;
	}

	public String getTitle() {
		return "Flower";
	}

	public String getDescription() {
		return "in memoriam.";
	}
}
