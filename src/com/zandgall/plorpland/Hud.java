/* zandgall

 ## Hud
 # A simple class that renders out an in-game Hud to draw a basic player healthbar to the screen

 : MADE IN NEOVIM */

package com.zandgall.plorpland;

import java.io.IOException;

import com.zandgall.plorpland.entity.collectables.Collectable;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.graphics.Layer;
import com.zandgall.plorpland.graphics.Shader;

public class Hud {
	private static final Image healthRed = new Image("/health_red.png"), healthGreen = new Image("/health_green.png"), healthShadow = new Image("/health_shadow.png");

	private double healthOpacity = 0, deathOpacity = 0, respawnOpacity = 0, closeOpacity = 0;
	private double pPlayerHealth = 20;
	private boolean respawning = false, closing = false;

	// Collectable flags
	private Collectable collected = null;
	private double collectableTimer = 0.0, collectablesOpacity = 0;

	public Hud() {}

	public void tick() {
		// Change health bar opacity depending on player health/health change
		if (Main.getPlayer().getHealth() == 20)
			healthOpacity = healthOpacity * 0.99;
		else if(Main.getPlayer().getHealth() != pPlayerHealth) {
			pPlayerHealth = Main.getPlayer().getHealth();
			healthOpacity = 1;
		} else {
			healthOpacity = healthOpacity * 0.99 + 0.005;
		}

		// Player is dead
		if(!Main.getLevel().getEntities().contains(Main.getPlayer())) {
			deathOpacity = deathOpacity * 0.99 + 0.005;
		} else
			deathOpacity = deathOpacity * 0.95;

		if(respawning) {
			respawnOpacity = respawnOpacity * 0.95 + 0.05;
		} else
			respawnOpacity = respawnOpacity * 0.95;

		if(!respawning && deathOpacity > 0.05 && respawnOpacity < 0.001 && Main.keys[Main.lastKey]) {
			respawning = true;
		}

		if(respawning && respawnOpacity > 0.999) {
			try {
				Main.quickload();
			} catch (IOException e) {
				System.err.println("Could not quickload!");
				e.printStackTrace();
			}
			respawning = false;
		}

		if(closing)
			closeOpacity = closeOpacity * 0.995 + 0.005;

		if(collected != null) {
			collectableTimer += Main.TIMESTEP;
			collectablesOpacity = collectablesOpacity * 0.95 + 0.05;
		} else
			collectablesOpacity = collectablesOpacity * 0.95;

		if(collectableTimer > 5) {
			collectableTimer = 0;
			collected = null;
		}
	}

	public void render() {
		Layer.HUD.use();
		Shader.Image.use().push().drawToScreen()
			.alpha(healthOpacity)
			.crop(0, 0, 1, 1)
			.image(healthShadow)
			.move(4, 4)
			.scale(healthShadow.getWidth()*4, healthShadow.getHeight()*4);
		G.draw01Square();
		Shader.Image.use().pop().push().drawToScreen()
			.alpha(healthOpacity)
			.image(healthRed)
			.move(4, 4)
			.scale(healthRed.getWidth()*4, healthRed.getHeight()*4);
		G.draw01Square();
		Shader.Image.use()
			.alpha(healthOpacity)
			.crop(0, 0, Main.getPlayer().getHealth() / 20.0, 1)
			.scale(Main.getPlayer().getHealth()/20.0, 1)
			.image(healthGreen);
		G.draw01Square();
		Shader.Image.use().pop();

		Shader.Color.use().push().drawToScreen()
			.setModel(0, 0, Main.WIDTH, Main.HEIGHT)
			.color(1, 0, 0)
			.alpha(deathOpacity);
		G.drawSquare();
		Shader.Color.use().color(1, 1, 1).alpha(respawnOpacity);
		G.drawSquare();
		Shader.Color.use().alpha(closeOpacity);
		G.drawSquare();
		Shader.Color.use().pop();

		// TODO: HUD + Text
	}

	public void closeOut() {
		closing = true;
	}

	public void collect(Collectable c) {
		collected = c;
		collectableTimer = 0;
	}
}
