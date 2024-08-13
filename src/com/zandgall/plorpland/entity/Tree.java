/* zandgall

 ## Tree
 # A basic entity that simply displays a tree

 : MADE IN NEOVIM */

package com.zandgall.plorpland.entity;

import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.Sound;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Hitnull;
import com.zandgall.plorpland.util.Hitrect;
import com.zandgall.plorpland.util.ShadowImage;

public class Tree extends Entity {

	private static Image trunk = new Image("/entity/tree_trunk.png"), leaves = new Image("/entity/tree_leaves.png");
	private static ShadowImage shadow = new ShadowImage("/entity/tree_shadow.png", 5, 0.6);
	private double peekTransparency = 1;

	private Hitbox renderBounds, solidBounds;

	public Tree() {
		super();
	}

	public Tree(double x, double y) {
		super(x, y);
		renderBounds = new Hitrect(getX() - 1.5, getY() - 3.5, 3.0, 5.0);
		solidBounds = new Hitrect(getX() - 0.3, getY() - 0.3, 0.6, 0.6);
	}

	@Override
	public void tick() {
		// Tree does sound
		if(Main.getPlayer().getPosition().sqDist(position) < 16) {
			Sound.Wind.setMinVolume(1.f - (float)Main.getPlayer().getPosition().dist(position) / 4.f);
		}
	}

	@Override
	public void render(GraphicsContext g1, GraphicsContext gs, GraphicsContext g2) {
		Hitbox treebox = new Hitrect(getX() - 1.0, getY() - 2.5, 2, 1.6);
		// if the player is behind the leaves, slowly shift "peekTransparency" to 0.75
		// opacity, otherwise shift it to full opacity
		if (treebox.intersects(Main.getPlayer().getRenderBounds()))
			peekTransparency = peekTransparency * 0.95 + 0.75 * 0.05;
		else
			peekTransparency = peekTransparency * 0.95 + 1.0 * 0.05;

		// Tree texture is 3 x 4 tiles in dimensions. offset by -1.5, -3.5
		g1.drawImage(trunk, getX() - 1.5, getY() - 3.5, 3, 4);
		// Shadow is 1 tile lower
		shadow.render(gs, getX() - 1.5, getY() - 2.5, 3, 4);

		if (peekTransparency != 1.0) {
			g2.save();
			g2.setGlobalAlpha(peekTransparency);
			g2.drawImage(leaves, getX() - 1.5, getY() - 3.5, 3, 4);
			g2.restore();
		} else
			g2.drawImage(leaves, getX() - 1.5, getY() - 3.5, 3, 4);
	}

	public Hitbox getRenderBounds() {
		return renderBounds;
	}

	// Tree doesn't update, so empty hitbox
	public Hitbox getUpdateBounds() {
		return renderBounds;
	}

	// Only the tile at the trunk is solid
	public Hitbox getSolidBounds() {
		return solidBounds; 
	}

	public Hitbox getHitBounds() {
		return Hitnull.instance;
	}

	public double getRenderLayer() {
		return getY() + 0.7;
	}

}
