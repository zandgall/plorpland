/* zandgall

 ## Shadow Image
 # Caches a shadow texture with blur and a color effect, reduces overhead later

 : MADE IN NEOVIM */

package com.zandgall.plorpland.util;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class ShadowImage {
	private Image img;

	public ShadowImage(String resource, double blur, double alpha) {
		// Load the image,
		Image base = new Image(resource);

		// Create a canvas and set parameters of a graphics context
		Canvas c = new Canvas(base.getWidth() + blur * 2, base.getHeight() + blur * 2);
		GraphicsContext g = c.getGraphicsContext2D();
		g.setGlobalAlpha(alpha);
		g.setImageSmoothing(false);

		// Clear and draw the image, accounting for blur radius
		g.clearRect(0, 0, c.getWidth(), c.getHeight());
		g.drawImage(base, blur, blur);

		// Apply given effects
		g.applyEffect(new ColorAdjust(-0.8, 0.5, -0.8, 0.0));
		g.applyEffect(new GaussianBlur(blur));

		// Take a snapshot and set our image
		SnapshotParameters p = new SnapshotParameters();
		p.setFill(Color.TRANSPARENT);
		img = c.snapshot(p, null);
	}

	public void render(GraphicsContext g, double x, double y, double w, double h) {
		g.drawImage(img, x, y, w, h);
	}
}
