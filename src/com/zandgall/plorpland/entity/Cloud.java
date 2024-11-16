/* zandgall

 ## Cloud
 # A simple entity that just layers shadows across the ground

 : MADE IN NEOVIM */

package com.zandgall.plorpland.entity;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.graphics.Layer;
import com.zandgall.plorpland.graphics.Shader;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Hitrect;

import java.util.Random;

public class Cloud {
	public static Image textures[] = new Image[] {
			new Image("/entity/cloud_0.png"),
			new Image("/entity/cloud_1.png"),
			new Image("/entity/cloud_2.png"),
			new Image("/entity/cloud_3.png"),
			new Image("/entity/cloud_4.png"),
			new Image("/entity/cloud_5.png"),
			new Image("/entity/cloud_6.png"),
			new Image("/entity/cloud_7.png")
	};

	private int type;
	private double speed;
	private double x, y;

	private Hitrect bounds;

	public Cloud() {
		type = new Random().nextInt(8);
		speed = new Random().nextDouble(0.2, 0.5) * Main.TIMESTEP;
	}

	public Cloud(double x, double y) {
		this.x = x;
		this.y = y;
		type = new Random().nextInt(8);
		speed = new Random().nextDouble(0.2, 0.5) * Main.TIMESTEP;
		bounds = new Hitrect(x, y, 16, 16);
	}

	public void tick() {
		// Glide across the screen;
		x -= speed;
		// check against level bounds, if too far left, respawn on right side
		if (!getRenderBounds().intersects(Main.getLevel().bounds))
			x = Main.getLevel().bounds.x + Main.getLevel().bounds.w;
		// Update bounds
		bounds.set(x, y, 16, 16);
	}

	public void render() {
		Layer.CLOUD_SHADOW.use();
		Shader.TintedImage.use().push().drawToWorld().alpha(0.3).tint(0.2, 0.2, 0.23);
		textures[type].draw(x, y, textures[type].getWidth()/16, textures[type].getHeight()/16);
		Shader.TintedImage.use().pop();
	}

	public Hitbox getRenderBounds() {
		return bounds;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
}
