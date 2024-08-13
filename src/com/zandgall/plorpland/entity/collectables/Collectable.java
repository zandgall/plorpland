/* zandgall

 ## Collectable
 # An entity subclass that is used to easily and quickly instantialize collectables

 : MADE IN NEOVIM */

package com.zandgall.plorpland.entity.collectables;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.entity.Entity;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Hitnull;
import com.zandgall.plorpland.util.Hitrect;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public abstract class Collectable extends Entity {

	private Hitbox bounds;

	public Collectable(double x, double y) {
		super(x, y);
		bounds = new Hitrect(position.x - 0.5, position.y - 0.5, 1, 1);
	}

	public void tick() {
		if(Main.getPlayer().getHitBounds().intersects(getUpdateBounds())) {
			Main.getHud().collect(this);
			Main.getLevel().removeEntity(this);
		}
	}

	public void render(GraphicsContext g, GraphicsContext shadow, GraphicsContext g2) {
		g.drawImage(getTexture(), position.x - 0.5, position.y - 0.5, 1, 1);
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

	public abstract Image getTexture();

	public abstract String getTitle();

	public abstract String getDescription();
}
