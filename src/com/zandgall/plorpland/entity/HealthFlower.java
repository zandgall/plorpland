/* zandgall

 ## Health Flower
 # A simple animated pickup entity that grants the player more health

 : MADE IN NEOVIM */

package com.zandgall.plorpland.entity;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.Sound;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Hitnull;
import com.zandgall.plorpland.util.Hitrect;

public class HealthFlower extends Entity {

	private static final Image texture = new Image("/entity/health_flower.png");

	private double timer;

	private Hitbox bounds;

	public HealthFlower(double x, double y) {
		super(x, y);
		bounds = new Hitrect(x - 0.5, y - 0.5, 1, 1);
	}

	/**
	 * Just checks for player intersection and adds health and dies when there is
	 */
	public void tick() {
		if (Main.getPlayer().getHitBounds().intersects(getUpdateBounds())) {
			Main.getPlayer().addHealth(5.0);
			Main.getLevel().removeEntity(this);
			Sound.EffectPluck.charge();
		}
		timer += Main.TIMESTEP;
	}

	public void render() {
		int frame = (int) Math.max((timer * 8) % 8 - 4, 0);
		texture.draw(0, frame * 16, 16, 16, position.x - 0.5, position.y - 0.5, 1, 1, G.LAYER_1);
	}

	public Hitbox getRenderBounds() {
		return bounds;
	}

	public Hitbox getUpdateBounds() {
		return bounds;
	}

	public Hitbox getSolidBounds() {
		return Hitnull.instance;
	}

	public Hitbox getHitBounds() {
		return Hitnull.instance;
	}

}
