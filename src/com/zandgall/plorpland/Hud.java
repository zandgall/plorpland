/* zandgall

 ## Hud
 # A simple class that renders out an in-game Hud to draw a basic player healthbar to the screen

 : MADE IN NEOVIM */

package com.zandgall.plorpland;

import java.io.IOException;

import com.zandgall.plorpland.entity.collectables.Collectable;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.graphics.Shader;

public class Hud {
	// private static final Image popup = new Image("/popup.png");
	// private static final Font FONT32 = Font.font(32), FONT16 = Font.font(16);

	private static final Image healthRed = new Image("/health_red.png"), healthGreen = new Image("/health_green.png"), healthShadow = new Image("/health_shadow.png");

	private double healthOpacity = 0, deathOpacity = 0, respawnOpacity = 0, closeOpacity = 0;
	private double pPlayerHealth = 20;
	private boolean respawning = false, closing = false;

	// Collectable flags
	private int numCollected = 0;
	private Collectable collected = null;
	private double collectableTimer = 0.0, collectablesOpacity = 0;

	public Hud() {}

	public void tick() {
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
		Shader.TintedImage.use().push().drawToScreen()
			.alpha(healthOpacity)
			.crop(0, 0, 1, 1)
			.image(healthShadow)
			.move(4, 4)
			.scale(healthShadow.getWidth()*4, healthShadow.getHeight()*4)
			.tint(1, 1, 1);
		G.draw01Square();
		Shader.TintedImage.use().pop().push().drawToScreen()
			.alpha(healthOpacity)
			.image(healthRed)
			.move(4, 4)
			.scale(healthRed.getWidth()*4, healthRed.getHeight()*4);
		G.draw01Square();
		Shader.TintedImage.use()
			.alpha(healthOpacity)
			.crop(0, 0, Main.getPlayer().getHealth() / 20.0, 1)
			.scale(Main.getPlayer().getHealth()/20.0, 1)
			.image(healthGreen);
		G.draw01Square();
		Shader.TintedImage.use().pop();

		// Shader.Image.reset().image(healthBar).at(0, 0).layer(G.LAYER_2).scale(4, 1).use();
		// G.drawSquare();

		// TODO: HUD + Text
		/*
		g.setGlobalAlpha(healthOpacity);
		g.setFill(Color.RED);
		g.fillRect(20, 20, 200, 20);
		g.setFill(Color.GREEN);
		g.fillRect(20, 20, Main.getPlayer().getHealth() * 10, 20);
		g.setStroke(Color.BLACK);
		g.setLineWidth(2.0);
		g.strokeRect(20, 20, 200, 20);

		g.setGlobalAlpha(deathOpacity);
		g.setFill(Color.RED);
		g.fillRect(0, 0, Main.stage.getWidth(), Main.stage.getHeight());

		g.setGlobalAlpha(respawnOpacity);
		g.setFill(Color.WHITE);
		g.fillRect(0, 0, Main.stage.getWidth(), Main.stage.getHeight());

		g.setGlobalAlpha(closeOpacity);
		g.setFill(Color.WHITE);
		g.fillRect(0, 0, Main.stage.getWidth(), Main.stage.getHeight());

		g.setGlobalAlpha(collectablesOpacity);
		g.setFill(Color.WHITE);
		g.setFont(Font.font(32));
		g.setTextAlign(TextAlignment.RIGHT);
		g.fillText(numCollected + " / 5", Main.scene.getWidth() - 8, 32);

		if(collected != null) {
			g.setGlobalAlpha(1.0);
			g.setTextAlign(TextAlignment.LEFT);
			g.drawImage(popup, 0, 0, 16, 48, 0, Main.scene.getHeight() - 96, 32, 96);
			g.drawImage(popup, 16, 0, 16, 48, 32, Main.scene.getHeight() - 96, Main.scene.getWidth() - 64, 96);
			g.drawImage(popup, 32, 0, 16, 48, Main.scene.getWidth() - 32, Main.scene.getHeight() - 96, 32, 96);
			g.drawImage(collected.getTexture(), 16, Main.scene.getHeight()-80, 64, 64);
			g.setFont(FONT32);
			g.setFill(new Color(0.05, 0.05, 0.1, 1));
			g.fillText(collected.getTitle(), 96, Main.scene.getHeight() - 48, Main.scene.getWidth() - 192);
			g.setFont(FONT16);
			g.fillText(collected.getDescription(), 96, Main.scene.getHeight() - 16, Main.scene.getWidth() - 192);
		}
		*/
	}

	public void closeOut() {
		closing = true;
	}

	public void collect(Collectable c) {
		collected = c;
		collectableTimer = 0;
		numCollected++;
	}
}
