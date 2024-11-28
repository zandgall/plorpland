/* zandgall

 ## Health Flower
 # A simple animated pickup entity that grants the player more health

 : MADE IN NEOVIM */

package com.zandgall.plorpland.entity.misc.combattest;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.Sound;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.graphics.Layer;
import com.zandgall.plorpland.graphics.Shader;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Hitnull;
import com.zandgall.plorpland.util.Hitrect;
import com.zandgall.plorpland.entity.Entity;

public class HealthFlower extends Entity {

	private static final Image texture = new Image("/entity/health_flower.png");

	private double timer, deadTime = 0;

	private Hitrect bounds;

	public HealthFlower(double x, double y) {
		super(x, y);
		bounds = new Hitrect(x - 0.5, y - 0.5, 1, 1);
	}

	/**
	 * Just checks for player intersection and adds health and dies when there is
	 */
	public void tick() {
		if (Main.getPlayer().getHitBounds().intersects(getUpdateBounds()) && deadTime < 0) {
			Main.getPlayer().addHealth(5.0);
			Sound.EffectPluck.charge();
			deadTime = 10;
		}
		timer += Main.TIMESTEP;
		deadTime -= Main.TIMESTEP;

	}

	public void render() {
		int frame = (int) Math.max((timer * 8) % 8 - 4, 0);
		Shader.Image.use();
		Layer.ENTITY_BASE.use();
		if(deadTime < 0 || (deadTime < 1 && Math.floor(deadTime * 100) % 2 == 0))
			texture.draw(0, frame * 16, 16, 16, position.x - 0.5, position.y - 0.5, 1, 1);
	}

	@Override
	public void setX(double x) {
		super.setX(x);
		bounds.set(x - 0.5, position.y - 0.5, 1, 1);
	}

	@Override
	public void setY(double y) {
		super.setY(y);
		bounds.set(position.x - 0.5, y - 0.5, 1, 1);
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
