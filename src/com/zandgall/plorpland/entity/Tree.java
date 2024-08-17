/* zandgall

 ## Tree
 # A basic entity that simply displays a tree

 : MADE IN NEOVIM */

package com.zandgall.plorpland.entity;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.Sound;
import com.zandgall.plorpland.graphics.GLHelper;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.graphics.Shader;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Hitnull;
import com.zandgall.plorpland.util.Hitrect;

public class Tree extends Entity {

	private static Image trunk = new Image("/entity/tree_trunk.png"), leaves = new Image("/entity/tree_leaves.png");
	private static Image shadow = new Image("/entity/tree_shadow.png");
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
	public void render() {
		Hitbox treebox = new Hitrect(getX() - 1.0, getY() - 2.5, 2, 1.6);
		// if the player is behind the leaves, slowly shift "peekTransparency" to 0.75
		// opacity, otherwise shift it to full opacity
		if (treebox.intersects(Main.getPlayer().getRenderBounds()))
			peekTransparency = peekTransparency * 0.95 + 0.75 * 0.05;
		else
			peekTransparency = peekTransparency * 0.95 + 1.0 * 0.05;

		// Tree texture is 3 x 4 tiles in dimensions. offset by -1.5, -3.5
		trunk.draw((float)getX() - 1.5f, (float)getY() - 3.5f, 3, 4, GLHelper.LAYER_1_DEPTH);
		// Shadow is 1 tile lower
		shadow.draw((float)getX() - 1.5f, (float)getY() - 2.5f, 3, 4, GLHelper.LAYER_1_DEPTH);

		Shader.Image.setAlpha((float)peekTransparency);
		leaves.draw(getX() - 1.5, getY() - 3.5, 3, 4, GLHelper.LAYER_2_DEPTH);
		Shader.Image.setAlpha(1.f);
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
