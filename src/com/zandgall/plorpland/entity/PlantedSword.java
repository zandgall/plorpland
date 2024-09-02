/* zandgall

 ## Planted Sword
 # A simple pickup item that grants the player a sword

 : MADE IN NEOVIM */

package com.zandgall.plorpland.entity;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.Sound;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.graphics.Shader;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Hitnull;
import com.zandgall.plorpland.util.Hitrect;

public class PlantedSword extends Entity {

	private static Image texture = new Image("/entity/planted_sword.png");

	private Hitbox swordbox, renderBounds, updateBounds;

	public PlantedSword() {
		super();
	}

	public PlantedSword(double x, double y) {
		super(x, y);
		swordbox = new Hitrect(x - 0.5, y - 0.2, 1.0, 0.4);
		renderBounds = new Hitrect(position.x - 1, position.y - 1.8, 2, 2);
		updateBounds = new Hitrect(position.x - 45, position.y - 45, 90, 90);
	}

	@Override
	public void tick() {
		if (swordbox == null)
			swordbox = new Hitrect(position.x - 0.5, position.y - 0.2, 1.0, 0.4);
		if(Main.getPlayer().getPosition().sqDist(position) < 45*45) {
			float f = 1.f - ((float)Main.getPlayer().getPosition().dist(position) / 45.f);
			Sound.EPiano.setMinVolume(f*f);
		}
		// Check if intersects with player, if so, give the player a sword
		if (Main.getPlayer().getSolidBounds().intersects(swordbox)) {
			Main.getPlayer().giveSword();
			Main.getLevel().removeEntity(this);
		}
	}

	@Override
	public void render() {
		Shader.Image.use();
		texture.draw(position.x - 1, position.y - 1.8, 2, 2, G.LAYER_1);
	}

	public Hitbox getRenderBounds() {
		return renderBounds;
	}

	public Hitbox getUpdateBounds() {
		return updateBounds;
	}

	public Hitbox getSolidBounds() {
		return Hitnull.instance;
	}

	public Hitbox getHitBounds() {
		return Hitnull.instance;
	}

}
