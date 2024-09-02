/* zandgall

 ## Plorp
 # A basic enemy entity

 : MADE IN NEOVIM */

package com.zandgall.plorpland.entity;

import java.util.Random;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.Sound;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.graphics.Shader;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Hitnull;
import com.zandgall.plorpland.util.Hitrect;
import com.zandgall.plorpland.util.Path;
import com.zandgall.plorpland.util.Vector;

public class Plorp extends Entity {
	private static Image sheet = new Image("/entity/little_guy.png");

	// State handling
	static enum State {
		DEAD, SLEEPING, FALLING_ASLEEP, RESTING, STANDING, WALKING, WALKING_HOME, SURPRISED, CHASING
	}

	private State state = State.RESTING;

	// Timing and rendering flags
	private double timer = 0;
	private int frame = 0, horizontalFlip = 1;

	// Pathfinding elements	
	private Vector home, target;
	private Path following = new Path();	

	private boolean hitWall = false;

	private double health = 5;
	private long lastHit = 0;

	public Plorp() {
		super();
	}

	public Plorp(double x, double y) {
		super(x, y);
		home = position.clone();
		target = position.clone();
	}

	@Override
	public void tick() {
		Random r = new Random();
		switch (state) {
			case SLEEPING:
				if (r.nextDouble() < 0.001) { // 0.1% chance of waking up
					state = State.RESTING;
					frame = r.nextInt(4); // look in random direction
				}
				break;

			case FALLING_ASLEEP:
				timer += Main.TIMESTEP;
				frame = (int) timer;

				if (frame >= 2) {
					state = State.SLEEPING;
					frame = 0;
					timer = 0;
				}
				break;

			case RESTING:
				handleResting(r);
				break;

			case STANDING:
				handleStanding(r);
				break;

			case WALKING:
				handleWalking(r);
				break;

			case WALKING_HOME:
				if (pursueTarget(0.15)) {
					frame = 0;
					timer = 0;
					state = State.STANDING;
				}
				break;

			case SURPRISED:
				timer += Main.TIMESTEP;
				if (timer > 0.25) {
					timer = 0;
					state = State.CHASING;
				}
				break;

			case CHASING:
				handleChasing(r);
				break;

			case DEAD:
			default:
		}
		if (Math.abs(velocity.x) > 0.001 || Math.abs(velocity.y) > 0.001) {
			hitWall |= move();
		} else {
			velocity.set(0, 0);
		}

		if(state != State.DEAD && Main.getPlayer().getPosition().sqDist(position) < 100) {
			Sound.Plorp.setMinVolume(1.f - (float)Main.getPlayer().getPosition().dist(position) / 10.f);
		}
	}

	// Plorp sits on the ground looking around and blinking
	private void handleResting(Random r) {
		timer += Main.TIMESTEP;

		// 5% chance of looking in a new direction
		if (r.nextDouble() < 0.005)
			frame = r.nextInt(4);

		// 0.1% chance of standing up
		if (r.nextDouble() < 0.001) {
			state = State.STANDING;
			frame = r.nextInt(2); // Look up or down randomly
			timer = 0;
		}

		// fall asleep after 10 seconds
		if (timer > 10) {
			state = State.FALLING_ASLEEP;
			frame = 0;
			timer = 0;
		}

		// Only check for player 0.5 seconds after switching states
		if (timer > 0.5)
			checkForPlayer();
	}

	// Plorp stands, with a chance of walking
	private void handleStanding(Random r) {
		timer += Main.TIMESTEP;

		// 0.1% chance of getting up and walking
		if (r.nextDouble() < 0.001) {
			state = State.WALKING;
			frame = 0;
			timer = 0;
			target(r.nextDouble(-2, 2) + home.x, r.nextDouble(-2, 2) + home.y);
		}

		// If standing for more than 5 seconds, sit down and rest
		if (timer > 5) {
			state = State.RESTING;
			frame = 0;
			timer = 0;
		}

		// Only check for player 0.5 seconds after switching states
		if (timer > 0.5)
			checkForPlayer();
	}

	// Plorp walks around to random points around its home coordinate
	private void handleWalking(Random r) {
		// Pursue target, and if reached, stand still
		if (pursueTarget(0.15))
			frame = 0;

		// 0.1% chance of targetting a random point
		if (r.nextDouble() < 0.001)
			target(r.nextDouble(-2, 2) + home.x, r.nextDouble(-2, 2) + home.y);

		// 0.05% chance of walking back home
		if (r.nextDouble() < 0.0005) {
			timer = 0;
			target(home.x, home.y);
			state = State.WALKING_HOME;
		}

		// Only check for player 0.5 seconds after switching states
		if (timer > 0.5)
			checkForPlayer();
	}

	// Chase after target, damaging player if caught
	private void handleChasing(Random r) {
		// Chase target and if hit something, check if we hit the player and damage
		if (pursueTarget(0.3)) {
			if (new Hitrect(getX() - 0.5, getY() - 0.5, 1, 1)
					.intersects(Main.getPlayer().getHitBounds())) { // If close enough player
				Main.getPlayer().dealEnemyDamage(1.0);
			} else if (!new Hitrect(getX() - 4, getY() - 4, 8, 8).intersects(Main.getPlayer().getSolidBounds())) {
				// Ran into a wall or something else stopped it, if can't see player, stop
				timer = 0;
				frame = 0;
				target(Main.getPlayer().getX(), Main.getPlayer().getY());
				state = State.WALKING;
			}
		}

		// End of every eighth a second, recheck if the player is within chasing bounds
		// Updating the target position if so
		if ((int) (timer * 8 + Main.TIMESTEP * 8) != (int) (timer * 8)
				&& new Hitrect(getX() - 8, getY() - 8, 16, 16).intersects(Main.getPlayer().getSolidBounds())) {
			// update target position
			target(Main.getPlayer().getX(), Main.getPlayer().getY());
		}
	}

	/**
	 * Look for player in square 4 radius
	 */
	private boolean checkForPlayer() {
		if (new Hitrect(getX() - 4, getY() - 4, 8, 8).intersects(Main.getPlayer().getHitBounds())) {
			timer = 0;
			frame = 0;
			target(Main.getPlayer().getX(), Main.getPlayer().getY());
			state = State.SURPRISED;
			return true;
		}
		return false;
	}

	// Pathfind (if necessary) towards target x and y
	private void target(double tx, double ty) {
		target.set(tx, ty);

		// Check if there is a wall between here and the target. If there is,
		// pathfinding is necessary
		boolean wall = false;
		// As long as we're within 'checkbox', we're between (x, y) and (tx, ty)
		Hitbox checkbox = new Hitrect(Math.min(tx, getX()), Math.min(ty, getY()), Math.abs(tx - getX()), Math.abs(ty - getY()));
		// Unit direction from (x, y) -> (tx, ty)
		Vector dir = position.unitDir(target);

		// Create a box from solid bounds. While it intersects 'checkbox', and a wall
		// hasn't been found, move it in the unit direction and check for a solid tile
		for (Hitbox box = getSolidBounds(); checkbox.intersects(box) && !wall; box = box.translate(dir)) {
			int tileX = (int) Math.floor(box.getBounds().centerX());
			int tileY = (int) Math.floor(box.getBounds().centerY());
			// if the tile at the center of the box has a hitbox, we'll need to pathfind
			// around it
			wall |= Main.getLevel().get(tileX, tileY).solidBounds(tileX, tileY) != null;
		}

		// Pathfind if there are obstacles,
		if (wall) {
			following = Path.pathfind(tileX(), tileY(), (int) Math.floor(tx), (int) Math.floor(ty));
			if (!following.empty())
				following.progress();
		} else
			following = new Path(); // Empty path to just follow x/yTarget
	}

	/**
	 * Used by the "WALKING", "WALKING_HOME", and "CHASING" states. And as such,
	 * updates 'frame' and 'timer' as they would otherwise
	 * 
	 * @param speed The speed to move at
	 */
	private boolean pursueTarget(double speed) {
		timer += Main.TIMESTEP;

		// Create a target, if there's a following path, follow that
		// otherwise, follow x/yTarget	
		Vector current = new Vector();
		if (!following.empty()) {
			current.x = following.current().x + 0.5;
			current.y = following.current().y + 0.5;
		} else {
			current.set(target);
		}

		// If we're close enough to the target, progress path or update x/yTarget
		double distance = position.dist(current);
		if (distance < 0.1 || hitWall) {
			// Hitwall is flagged when the Plorp hits something, which might be the player
			if (following.empty() || hitWall || position.dist(target) < 0.2) {
				// xTarget = x;
				// yTarget = y;
				hitWall = false;
				// Hit target
				return true;
			} else {
				// System.out.println("Progressing path");
				following.progress();
			}
		}

		// Update graphics flags
		horizontalFlip = (current.x < getX()) ? 1 : -1;
		frame = (int) (timer * 3) % 4;

		// Accelerate 0.3 units/sec^2 towards (tX, tY)
		velocity.x += 0.3 * (current.x - getX()) / distance;
		velocity.y += 0.3 * (current.y - getY()) / distance;

		// Did not hit target
		return false;
	}

	@Override
	public void render() {
		Shader.Image.use().push();
		// Half transparent if hit recently
		if (state != State.DEAD && System.currentTimeMillis() - lastHit < 100
				&& (System.currentTimeMillis() / 50) % 2 == 0)
			Shader.Image.use().alpha(0.5);

		float fX = 8 - horizontalFlip * 8;
		float fW = 16 * horizontalFlip;
		switch (state) {
			case SLEEPING:
				sheet.draw(fX, 64, fW, 16, getX()-0.5, getY()-0.5, 1, 1, G.LAYER_1);
				// TODO: "zzz" particles
				break;
			case FALLING_ASLEEP:
				sheet.draw(frame * 16 + fX, 32, fW, 16, getX()-0.5, getY()-0.5, 1, 1, G.LAYER_1);
				break;
			case RESTING:
				if ((int) (timer * 10) % 20 == 0) { // 1 out of 20 frames are a blink
					if (frame >= 2) // Looking back
						sheet.draw(16+fX, 48, fW, 16, getX()-0.5, getY()-0.5, 1, 1, G.LAYER_1);
					else
						sheet.draw(fX, 48, fW, 16, getX()-0.5, getY()-0.5, 1, 1, G.LAYER_1);
				} else {
					int xoff[] = { 0, 16, 0, 16 }, yoff[] = { 0, 0, 16, 16 };
					sheet.draw(xoff[frame]+fX, yoff[frame], fW, 16, getX()-0.5, getY()-0.5, 1, 1, G.LAYER_1);
				}
				break;
			case STANDING:
				sheet.draw(32 + 16 * frame+fX, 0, 16, 16, getX()-0.5, getY()-0.5, 1, 1, G.LAYER_1);
				break;
			case WALKING:
			case WALKING_HOME:
			case CHASING:
				int up = (target.y < getY()) ? 16 : 0;
				sheet.draw(32 + up + fX, frame * 16, fW, 16, getX()-0.5, getY()-0.5, 1, 1, G.LAYER_1);
				break;
			case SURPRISED:
				sheet.draw(16 + fX, 64, fW, 16, getX()-0.5, getY()-0.5, 1, 1, G.LAYER_1);
				break;
			case DEAD:
				sheet.draw(32 + frame * 16 + fX, 64, fW, 16, getX()-0.5, getY()-0.5, 1, 1, G.LAYER_1);
				break;
			default:
				break;
		}

		// Draw a health bar if applicable
		// TODO: Do health bar
		/* if (state != State.DEAD && health < 5) {
			g2.setFill(Color.RED);
			g2.fillRect(getX() - 0.5, getY() - 1.0, 1.0, 0.25);
			g2.setFill(Color.GREEN);
			g2.fillRect(getX() - 0.5, getY() - 1.0, health / 5.0, 0.25);
		} */

		Shader.Image.use().pop();
	}

	public Hitbox getRenderBounds() {
		return new Hitrect(getX() - 0.5, getY() - 0.5, 1, 1);
	}

	public Hitbox getUpdateBounds() {
		return new Hitrect(getX() - 10, getY() - 10, 20, 20);
	}

	public Hitbox getSolidBounds() {
		if (state == State.DEAD)
			return Hitnull.instance;
		return new Hitrect(getX() - 0.05, getY() - 0.05, 0.1, 0.1);
	}

	public Hitbox getHitBounds() {
		return new Hitrect(getX() - 0.4, getY() - 0.2, 0.8, 0.5);
	}

	public void dealPlayerDamage(double damage) {
		// Dont receive damage if dead or surprised
		if (state == State.DEAD || state == State.SURPRISED)
			return;
		lastHit = System.currentTimeMillis();
		health -= damage;
		state = State.SURPRISED;

		if (health <= 0 && state != State.DEAD) {
			timer = 0;
			frame = new Random().nextInt(2); // pick between 2 random dead sprites
			state = State.DEAD;
			Sound.EffectBonk.charge();
		}
	}
}
