/* zandgall

 ## Ball
 # A ball collectable

 : MADE IN NEOVIM */

package com.zandgall.plorpland.entity.collectables;

import javafx.scene.image.Image;

public class Ball extends Collectable {
	public static final Image texture = new Image("/entity/ball.png");

	public Ball(double x, double y) {
		super(x, y);
	}

	public Image getTexture() {
		return texture;
	}

	public String getTitle() {
		return "Ball";
	}

	public String getDescription() {
		return "Something made to be kicked. Makes a good stool.";
	}
}
