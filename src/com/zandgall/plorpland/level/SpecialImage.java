/*

 ## Special Image
 # Used for special background image, with some semblance of parallax

 : MADE IN NEOVIM */

package com.zandgall.plorpland.level;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.util.Rect;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class SpecialImage {
	private Image image;	
	private double x, y, xOff, yOff, damping;
	public SpecialImage(String path, double xOff, double yOff, double x, double y, double damping) {
		image = new Image(path);	
		this.xOff = xOff;
		this.yOff = yOff;
		this.x = x;
		this.y = y;
		this.damping = damping;
	}

	public void render(GraphicsContext g) {
		double w = image.getWidth() / 16;
		double h = image.getHeight() / 16;
		double x = this.x * (1 - damping) + Main.getCamera().getX() * damping - xOff;
		double y = this.y * (1 - damping) + Main.getCamera().getY() * damping - yOff;

		// Reminder that each unit is a 16x16 tile
		// So scale image down by 1/16 to match
		g.drawImage(image, x, y, w, h);
	}

	public Rect getRenderBox() {
		double w = image.getWidth() / 16;
		double h = image.getHeight() / 16;
		double x = this.x - (w / 2);
		double y = this.y - (h / 2);
		return new Rect(x, y, w, h);
	}
}
