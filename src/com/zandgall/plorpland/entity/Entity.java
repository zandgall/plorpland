/* zandgall

 ## Entity
 # An abstract class that provides methods and fields common to entities

 : MADE IN NEOVIM */

package com.zandgall.plorpland.entity;

import java.io.Serializable;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Vector;
import com.zandgall.plorpland.level.Tile;

public abstract class Entity implements Serializable{
	protected Vector position, velocity;

	public Entity() {
		this(0, 0);
	}

	public Entity(double x, double y) {
		position = new Vector(x, y);
		velocity = new Vector(0, 0);
	}

	public abstract void tick();

	/*
	 * A method to define how to draw each type of entity 
	 */
	public abstract void render();

	public double getX() {
		return position.x;
	}

	public double getY() {
		return position.y;
	}

	public Vector getPosition() {
		return position;
	}

	public double getXVel() {
		return velocity.x;
	}

	public double getYVel() {
		return velocity.y;
	}

	public Vector getVelocity() {
		return velocity;
	}

	public int tileX() {
		return (int) Math.floor(getX());
	}

	public int tileY() {
		return (int) Math.floor(getY());
	}

	public void setX(double x) {
		position.x = x;
	}

	public void setY(double y) {
		position.y = y;
	}

	public void setXVel(double xVel) {
		velocity.x = xVel;
	}

	public void setYVel(double yVel) {
		velocity.y = yVel;
	}

	public abstract Hitbox getRenderBounds();

	public abstract Hitbox getUpdateBounds();

	public abstract Hitbox getSolidBounds();

	public abstract Hitbox getHitBounds();

	// Overrideable functions to handle different types of damage
	public void dealEnemyDamage(double damage) {
	}

	public void dealPlayerDamage(double damage) {
	}

	/**
	 * Handles movement and collision. Also applies friction to xVel and yVel.
	 * 
	 * @returns True whether hit a wall/solid object
	 */
	protected boolean move() {
		// If not moving, no need to move
		if (velocity.x == 0 && velocity.y == 0)
			return false;

		Vector safetyNetP = position.clone(), safetyNetV = velocity.clone();
		String debug = "Movement Error:\n";

		boolean hitWall = false;
	
		Vector next = position.clone();

		// Check it against all solid entities
		Hitbox box = getSolidBounds().translate(velocity.x * Main.TIMESTEP, velocity.y * Main.TIMESTEP);
		for (Entity e : Main.getLevel().getEntities()) {
			if (e == this)
				continue; // don't collide with self
			Hitbox solid = e.getSolidBounds();
			if (solid.intersects(box)) {
				debug += "Hit entity at: " + solid.getBounds().centerX() + ", " + solid.getBounds().centerY() + " (" + velocity.x + ", " + velocity.y + ") : " + next.x + ", " + next.y + " -> ";
				handleCollision(solid, next);
				debug += next.x + ", " + next.y + "\n";

				hitWall = true;
			}
		}

		// Get the range of tiles that intersect with the bounds
		int minX = (int) Math.floor(box.getBounds().x);
		int minY = (int) Math.floor(box.getBounds().y);
		int maxX = (int) Math.floor(box.getBounds().x + box.getBounds().w);
		int maxY = (int) Math.floor(box.getBounds().y + box.getBounds().h);

		// Loop through them and check if they intersect with this, handling collision
		// if they do
		for (int i = minX; i <= maxX; i++) {
			for (int j = minY; j <= maxY; j++) {
				Tile t = Main.getLevel().get(i, j);
				if (t != null && t.solidBounds(i, j) != null && t.solidBounds(i, j).intersects(box)) {
					debug += "Hit wall at: " + i + ", " + j + " (" + velocity.x + ", " + velocity.y + ") : " + next.x + ", " + next.y + " -> ";
					handleCollision(t.solidBounds(i, j), next);	
					debug += next.x + ", " + next.y + "\n";
					hitWall = true;
				}
			}
		}

		// Move to the next position
		// If there was a collision, nextX, nextY, xVel, yVel will have been modified
		// and we will be pressed up against a solid wall
		// TODO: So 'next' doesn't actually work, so we don't apply it
		position.add(velocity.getScale(Main.TIMESTEP));

		if(!Double.isFinite(velocity.x) || !Double.isFinite(velocity.y) || !Double.isFinite(position.x) || !Double.isFinite(position.y)) {
			position = safetyNetP.clone();
			velocity = safetyNetV.clone();
			System.err.println(debug);
			new RuntimeException().printStackTrace();
		}

		// Handle friction
		velocity.scale(0.9);

		return hitWall;
	}

	/**
	 * A collision exists in the way of this entity. Handle the collision and set
	 * next position accordingly.
	 * 
	 * @param solid The hitbox of the object colided with
	 * @param next An input/output variable for the position of this entity where
	 *              the collision occurs.
	 */
	private void handleCollision(Hitbox solid, Vector next) {
		if(velocity.x == 0 && velocity.y == 0)
			return;
		// We increment by 1/100th of a tile until we find the exact moment we intersect
		// with something solid
		Vector step = velocity.unit().scale(1.0/100.0);

		// System.out.printf("Intersection: going %.2f %.2f, step %.2f %.2f%n", velocity.x, velocity.y, step.x, step.y);

		Hitbox box = getSolidBounds().translate(next.getSub(position));

		if (box.intersects(solid)) {
			// Standing still, the player is intersecting something
			// But the player is also trying to move, we will give them 10 steps to see if
			// they make it out of the intersection
			Vector pre = next.clone();
			for (int i = 0; i < 10 && box.intersects(solid); i++) {
				next.add(step);
				box = box.translate(step);
			}
			// If they didn't make it out (i.e. walking against a wall instead of away)
			// Reset their position so they don't move
			if (box.intersects(solid)) {
				next.set(pre);
			}
		} else {
			// The player is not intersecting something right now, but is entering a hitbox
			// We will move them forward bit by bit to catch the point where they sit just
			// at the edge of the hitbox
			// We will also cap the number of attempts to 100 (or one full tile crossed),
			// just to avoid infinite loops
			for (int i = 0; i < 100 && !box.translate(step).intersects(solid); i++) {
				next.add(step);
				box = box.translate(step);
			}
		}

		// We are now within 1/100th of a tile within a solid box

		// If we step in the x direction and intersect, we must stop all x velocity
		if (box.translate(step.x, 0).intersects(solid))
			velocity.x = 0;
		// if we step in the y direction and intersect, we must stop all y velocity
		if (box.translate(0, step.y).intersects(solid))
			velocity.y = 0;
	}

	// An overridable function used to determine which entities get drawn over other
	// entities. By default, entities that are lower on screen are drawn in front of
	// entities that are higher
	public double getRenderLayer() {
		return getY();
	}

}
