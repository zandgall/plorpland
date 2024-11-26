/*

 ## Special Image
 # Used for special background image, with some semblance of parallax

 : MADE IN NEOVIM */

package com.zandgall.plorpland.level;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.graphics.Layer;
import com.zandgall.plorpland.util.Rect;

public class SpecialImage {
	private Image image;
	private double x, y, xOff, yOff, damping;
	public SpecialImage(String path, double x, double y, double xOff, double yOff, double damping) {
		image = new Image(path);
		this.xOff = xOff;
		this.yOff = yOff;
		this.x = x;
		this.y = y;
		this.damping = damping;
	}

	public void render() {
		double w = image.getWidth() / 16;
		double h = image.getHeight() / 16;
		double x = this.x * (1 - damping) + (Main.getCamera().getX() - xOff) * damping - w * 0.5;
		double y = this.y * (1 - damping) + (Main.getCamera().getY() - yOff) * damping - h * 0.5;

		// Reminder that each unit is a 16x16 tile
		// So scale image down by 1/16 to match
		// TODO: Might want customization on this
		Layer.LEVEL_BACKGROUND.use();
		image.draw(x, y, w, h);
	}

	public Rect getRenderBox() {
		double w = image.getWidth() / 16;
		double h = image.getHeight() / 16;
		double x = this.x - (w / 2);
		double y = this.y - (h / 2);
		return new Rect(x, y, w, h);
	}

	public double getX() { return x; }
	public double getY() { return y; }
	public double getXOff() { return xOff; }
	public double getYOff() { return yOff; }
	public double getDamping() { return damping; }
	public Image getImage() { return image; }

	public void setX(double x) { this.x = x; }
	public void setY(double y) { this.y = y; }
	public void setXOff(double x) { this.xOff = x; }
	public void setYOff(double y) { this.yOff = y; }
	public void setDamping(double damping) { this.damping = damping; }
	public void setImage(Image image) { this.image = image; }
}
