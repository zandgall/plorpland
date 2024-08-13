/* zandgall

 ## Octoplorp
 # A boss fight for the player to reach and fight

 ## Tentacle
 # An entity that represents each of Octoplorp's tentacles

 : MADE IN NEOVIM */


package com.zandgall.plorpland.entity.octoplorp;

import java.io.IOException;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.Sound;
import com.zandgall.plorpland.entity.Entity;
import com.zandgall.plorpland.entity.Player;
import com.zandgall.plorpland.staging.Cutscene;
import com.zandgall.plorpland.util.*;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;

public class Octoplorp extends Entity{

	private static final Image body = new Image("/entity/octoplorp/body.png"),
			eye = new Image("/entity/octoplorp/eye.png");

	static enum State {
		SLEEPING, WAKING, GRABBING, RECOVERING, VULNERABLE
	}

	private State state = State.SLEEPING;

	private Vector eyePos = new Vector(0, 1), eyeTarget = new Vector(0, 0);
	private Point eyeFrame = new Point(3, 0);

	private double timer = 0;

	private Tentacle tutorialTentacle, firstTentacle, secondTentacle, finalTentacle;

	private Tentacle currentTentacle; // Reference only - never it's own instance

	public Octoplorp(double x, double y) {
		super(Math.round(x), Math.round(y));

		// Initialize Tentacles
		tutorialTentacle = new Tentacle(position.getAdd(5.5, 12.5), position.getAdd(0, 6.5), position.getAdd(0, 12), position.getAdd(-28, -9));
		tutorialTentacle.tutorial = true;
		firstTentacle = new Tentacle(position.getAdd(-5.5, 6.5), null, position.getAdd(-27.5, -2), position.getAdd(26, -8));
		secondTentacle = new Tentacle(position.getAdd(-5.5, 12.5), null, position.getAdd(28, -7), position.getAdd(-16.5, 19.5));
		finalTentacle = new Tentacle(position.getAdd(5.5, 6.5), null, position.getAdd(0, 18), position.getAdd(0, 6));

		Main.getLevel().addEntity(tutorialTentacle);
		Main.getLevel().addEntity(firstTentacle);
		Main.getLevel().addEntity(secondTentacle);
		Main.getLevel().addEntity(finalTentacle);

		tutorialTentacle.damage = 0;
		firstTentacle.speed = 4;
		secondTentacle.speed = 5;
		finalTentacle.speed = 6;

		currentTentacle = tutorialTentacle;
	}

	public void tick() {
		switch (state) {
			// When sleeping, check for player in bounds and start a cutscene if applicable
			case SLEEPING:
				if (new Hitrect(getX() - 15, getY() - 15, 30, 30).intersects(Main.getPlayer().getRenderBounds())) {
					state = State.WAKING;
					// Wake up cutscene, open eyes and target the boss
					Main.playCutscene(new Cutscene(5) {
						float t = 0;

						@Override
						protected void tick() {
							Main.getPlayer().cutsceneSword(Main.getPlayer().getSwordRotation(), 0, 0, Player.Special.NONE);

							eyePos.y *= 0.99; // Move eye up (towards eyePos.y=0)

							t += Main.TIMESTEP;
							if (t > 4.3)
								eyeFrame.x = 0;
						}

						// When the boss finishes waking up, fade in the BossBass sound
						protected void onEnd() {
							Sound.BossBass.fadeTo(1.f);
						}

						protected Vector getTarget() {
							return position;
						}

						protected double getTargetZoom() {
							return 48;
						}
					});
				}
				break;

			// When waking, play another cutscene where the player can't move and the tutorial tentacle grabs them and pulls them up
			case WAKING:
				state = State.GRABBING;
				tutorialTentacle.state = Tentacle.State.CHASING;
				tutorialTentacle.speed = 5;
				// Play cutscene where tutorial tentacle grabs player while they can't move
				Main.playCutscene(new Cutscene(5) {
					@Override
					protected void tick() {
						tutorialTentacle.tick();
					}

					// Quicksave at the end of the cutscene
					protected void onEnd() {
						try {
							Main.quicksave();
						} catch (IOException e) {
							System.err.println("Could not save");
							e.printStackTrace();
						}
					}

					protected boolean done() {
						return tutorialTentacle.state == Tentacle.State.GRABBED;
					}

					protected Vector getTarget() {
						return Main.getPlayer().getPosition();
					}


					protected double getTargetZoom() {
						return 48;
					}

				});
				break;

			case GRABBING:
				// When grabbing player, modify the eye frame and its direction
				if(currentTentacle != null) {
					if(currentTentacle == firstTentacle || currentTentacle == tutorialTentacle)
						eyeFrame.y = 1;
					else
						eyeFrame.y = 0;

					switch(currentTentacle.state) {
					case CHASING:
					case GRABBING:
					case GRABBED:
					case RETRACTING:
					case REPOSITION:
					case RESTING:
					case DEAD:
					case DYING:
					case WINDUP:
						if(Main.getPlayer().getX() < getX() - 4)
							eyeFrame.x = 2;
						else if(Main.getPlayer().getX() > getX() + 4)
							eyeFrame.x = 1;
						else
							eyeFrame.x = 0;
						eyeTarget = Main.getPlayer().getPosition().getSub(position.getAdd(0, -4)).getScale(0.01);
						if(eyeTarget.length() > 1)
							eyeTarget = eyeTarget.unit();
						if((System.currentTimeMillis()/100) % 50 == 0)
							eyeFrame.y = 0;
						else if((System.currentTimeMillis()/100) % 50 == 1)
							eyeFrame = new Point(3, 0);
						break;
					case INJURED:
						eyeFrame = new Point(3, 1);
						eyeTarget.set(0, 0);
						break;
					case SWINGING:
						eyeFrame = new Point(0, 0);
						break;
					}
					// Tend from eyePos towards eyeTarget
					eyePos.scale(0.99).add(eyeTarget.getScale(0.01));
				
					// If the current tentacle is dead, select the next tentacle
					if(currentTentacle.state == Tentacle.State.DEAD) {
						if(currentTentacle == tutorialTentacle) {
							currentTentacle = firstTentacle;
							Sound.BossGuitar.fadeTo(1.f);
						} else if(currentTentacle == firstTentacle) {
							currentTentacle = secondTentacle;
							Sound.BossCymbals.fadeTo(1.f);
						} else if(currentTentacle == secondTentacle)
							currentTentacle = finalTentacle;
						else if(currentTentacle == finalTentacle)
							currentTentacle = null;

						timer = 0;
						state = State.RECOVERING;

						// If out of tentacles, switch to vulnerable and set up ending audio mixing
						if(currentTentacle == null) {
							state = State.VULNERABLE;
							Sound.EndIt.setSmoothing(Sound.DEFAULT_SMOOTHING * 8);
							Sound.EndIt.fadeTo(1.f);
							Sound.Noise.fadeTo(0.f);
							Sound.Wind.fadeTo(0.f);
							Sound.Piano.fadeTo(0.f);
							Sound.EPiano.fadeTo(0.f);
							Sound.Drums.fadeTo(0.f);
							Sound.Plorp.fadeTo(0.f);
							Sound.BossDrums.fadeTo(0.f);
							Sound.BossEPiano.fadeTo(0.f);
							Sound.BossBass.fadeTo(0.f);
							Sound.BossGuitar.fadeTo(0.f);
							Sound.BossCymbals.fadeTo(0.f);
							Sound.Noise.setSmoothing(Sound.DEFAULT_SMOOTHING * 4);
							Sound.Wind.setSmoothing(Sound.DEFAULT_SMOOTHING * 4);
							Sound.Piano.setSmoothing(Sound.DEFAULT_SMOOTHING * 4);
							Sound.EPiano.setSmoothing(Sound.DEFAULT_SMOOTHING * 4);
							Sound.Drums.setSmoothing(Sound.DEFAULT_SMOOTHING * 4);
							Sound.Plorp.setSmoothing(Sound.DEFAULT_SMOOTHING * 4);
							Sound.BossDrums.setSmoothing(Sound.DEFAULT_SMOOTHING * 4);
							Sound.BossEPiano.setSmoothing(Sound.DEFAULT_SMOOTHING * 4);
							Sound.BossBass.setSmoothing(Sound.DEFAULT_SMOOTHING * 4);
							Sound.BossGuitar.setSmoothing(Sound.DEFAULT_SMOOTHING * 4);
							Sound.BossCymbals.setSmoothing(Sound.DEFAULT_SMOOTHING * 4);
						}
					}
				}
				break;

			// If recovering, just switch to grabbing, and start the next tentacle
			// TODO: redundant state
			case RECOVERING:
				timer += Main.TIMESTEP;
				if(timer > 0) {
					currentTentacle.state = Tentacle.State.WINDUP;
					state = State.GRABBING;
				}
				break;

			// When vulnerable, freeze select sounds at 0 volume
			case VULNERABLE:
				Sound.EPiano.setVolume(0);
				Sound.BossEPiano.setVolume(0);
				Sound.Drums.setVolume(0);
				Sound.BossDrums.setVolume(0);
				break;

		}
	}

	// Draw the body and eye with given frame and position
	public void render(GraphicsContext g, GraphicsContext shadow, GraphicsContext g2) {
		g.drawImage(body, getX() - 3, getY() - 3, 6, 6);
		if(eyeFrame.x == -1)
			g.drawImage(eye, 256, 32, 64, 64, getX() - 2 + eyePos.x, getY() - 4 + eyePos.y, 4, 4);
		else
			g.drawImage(eye, eyeFrame.x * 64, eyeFrame.y * 32, 64, 32, getX() - 2 + eyePos.x, getY() - 2 + eyePos.y, 4, 2);
	}

	public Hitbox getRenderBounds() {	
		return new Hitrect(tileX() - 3, tileY() - 3, 6, 6);
	}

	public Hitbox getUpdateBounds() {
		return new Hitrect(Main.getLevel().bounds);
	}

	public Hitbox getSolidBounds() {
		return new Hitrect(tileX() - 3, tileY() + 1, 6, 2);
	}

	public Hitbox getHitBounds() {
		if(state == State.VULNERABLE)
			return new Hitrect(tileX() - 3, tileY() - 3, 6, 6);
		return Hitnull.instance;
	}

	@Override
	public double getRenderLayer() {
		if(state == State.VULNERABLE)
			return Double.NEGATIVE_INFINITY;
		return getY() + 2;
	}

	// When receive any amount of damage (only possible when vulnerable) start the ending cutscene
	@Override
	public void dealPlayerDamage(double damage) {
		Sound.Heaven.fadeTo(0.8f);
		Sound.Heaven.setSmoothing(Sound.DEFAULT_SMOOTHING * 4);

		Main.playCutscene(new Cutscene(Double.POSITIVE_INFINITY) {
			boolean stabbing = false;
			double t = 0, y = position.y, upY = y - 6, stabY = y - 4;

			protected void tick() {
				// Freeze select sounds at 0
				Sound.BossDrums.setVolume(0.f);
				Sound.Drums.setVolume(0.f);
				Sound.BossEPiano.setVolume(0.f);
				Sound.EPiano.setVolume(0.f);

				t += Main.TIMESTEP;
				if(t < 1) { // During the first second, move player up above head and make octoplorp look upwards
					if(t < 0.2) {
						eyeFrame.x = 3;
						eyeFrame.y = 0;
					} else if(t < 0.4) {
						eyeFrame.x = 0;
						eyeFrame.y = 2;
					}
					else if(t < 0.6)
						eyeFrame.x = 1;
					else
						eyeFrame.x = 2;
					y = y * 0.95 + upY * 0.05;
					Main.getPlayer().setX(position.x);
					Main.getPlayer().setY(y);
					Main.getPlayer().cutsceneSword(0.5 * Math.PI, 4 + t, 0, Player.Special.NONE);
				} else if (!stabbing) { // Then wait until the 'Z' key is hit
					if(Main.keys.get(KeyCode.Z)) {
						Main.getPlayer().cutsceneSword(0.5 * Math.PI, 0, 0, Player.Special.STAB);
						stabbing = true;
						t = 1;
						Sound.TheKill.play();

					}
				} else if(t < 3) { // For the next two seconds, stab and wait (take away sword after stab)
					if(y < stabY) {
						y += 0.2;
					} else {
						y = stabY;
						eyeFrame.x = -1;
						Main.getPlayer().takeAwaySword();
					}
					Main.getPlayer().setY(y);
				} else { // Fade out with EndIt sound and player sinking
					Sound.EndIt.fadeTo(0.f);
					Main.getPlayer().getPosition().add(0, 0.04);
					Main.getHud().closeOut();
					if(Sound.EndIt.getVolume() < 0.01)
						Main.close();
				}
			}

			@Override
			protected Vector getTarget() {
				return position.getAdd(0, -3);
			}

			@Override
			protected double getTargetZoom() {
				return 48;
			}

			@Override
			protected void onEnd() {}

		});
	}
}
