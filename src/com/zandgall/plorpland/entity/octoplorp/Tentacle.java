/* zandgall

## Tentacle
# Class used by Octoplorp that acts as its tentacles.
# Chases player and switches states

: MADE IN NEOVIM */

package com.zandgall.plorpland.entity.octoplorp;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_COMMA;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PERIOD;

import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Matrix4f;

import com.zandgall.plorpland.Camera;
import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.entity.Entity;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.graphics.Shader;
import com.zandgall.plorpland.staging.Cutscene;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Hitboxes;
import com.zandgall.plorpland.util.Hitnull;
import com.zandgall.plorpland.util.Hitrect;
import com.zandgall.plorpland.util.Path;
import com.zandgall.plorpland.util.Point;
import com.zandgall.plorpland.util.Util;
import com.zandgall.plorpland.util.Vector;

public class Tentacle extends Entity {
	protected static final Image sheet = new Image("/entity/octoplorp/tentacles.png"),
		healthOutline = new Image("/entity/health/outline10.png"),
		healthRed = new Image("/entity/health/red10.png"),
		healthGreen = new Image("/entity/health/green10.png");

	public static final boolean TENTACLE_DEBUG = false;

	public static enum State {
		DEAD, DYING, RESTING, GRABBING, GRABBED, WINDUP, CHASING, REPOSITION, RETRACTING, INJURED, SWINGING
	};

	public State state = State.RESTING;

	// Determines the segment types for rendering
	public static enum SegType {
		STRAIGHT, TURN_RIGHT, TURN_LEFT
	};

	// Double attributes - health, an internal timer, the speed of the tentacle, and how much damage it does to player
	public double health = 10.0, timer = 0, speed = 1, damage = 2;

	// Corpse Data
	private double corpseRotation = 1.5 * Math.PI, corpseRotationVel = 1;
	private Vector corpse;

	// Several Locations
	private Vector home, start, throwing, sword;
	
	// Segment Data
	private Path path = new Path(); // queue: points to travel
	private ArrayList<Point> traveled = new ArrayList<>(); // places traveled (in order)
	private HashMap<Point, Integer> segments = new HashMap<>(); // Segment rotations
	private HashMap<Point, SegType> segtypes = new HashMap<>(); // Segment Types

	// Same content as traveled, in solid hitbox form
	private Hitboxes hitbox = new Hitboxes();

	public ThrownSword thrownSword = null;

	private long lastHit = System.currentTimeMillis();

	/* 0 = right, 1 = down, 2 = left, 3 = up */
	private int orientation = 3;

	public boolean tutorial = false;

	public Tentacle(Vector pos, Vector home, Vector throwing, Vector sword) {
		super(pos.x, pos.y);
		hitbox.add(pos.x - 0.5, pos.y - 0.5, 1, 1);
		start = new Vector(pos.x, pos.y);
		this.home = home;
		this.throwing = throwing;
		this.sword = sword;
	}

	public void tick() {
		switch (state) {
			// TODO: Redundant state (DYING)
			case DYING:
			case DEAD:
			case RESTING:
				return;
			// Just rumble
			case WINDUP:
				timer+=Main.TIMESTEP;
				if(timer >= 1)
					state = State.CHASING;
				return;
			// When player is grabbed, deal damage
			case GRABBED:
				timer += Main.TIMESTEP;
				if(timer > 1.0)
					Main.getPlayer().dealEnemyDamage(damage);
			// If grabbed OR grabbing, tend towards 'home'
			case GRABBING:
				// Follow rest of path
				if (!path.empty()) {
					if(home != null)
						speed = Math.max(1, home.dist(position));
					followPath();
					if (path.empty()) {
						Point p = traveled.getLast();
						if(segments.get(p) == 1) {
							if(home != null)
								home.y += 1;
							orientation = 1;
						} else {
							if(home != null)
								home.y -= 1;
							orientation = 3;
						}
						segtypes.put(p, switch (segments.get(p)) {
							case 0 -> SegType.TURN_LEFT;
							case 1 -> SegType.STRAIGHT;
							case 2 -> SegType.TURN_RIGHT;
							case 3 -> SegType.STRAIGHT;
							default -> SegType.STRAIGHT;
						});
					}
				// Then switch to Grabbed and tend towards home
				} else {
					if(home != null)
						position.y = position.y * 0.99 + home.y * 0.01;
					if(state != State.GRABBED)
						timer = 0;
					state = State.GRABBED;
				}

				Main.getPlayer().setX(position.x + (nextPosition().x - tileX()) * 1.5);
				Main.getPlayer().setY(position.y + (nextPosition().y - tileY()) * 1.5);
				break;
			// When chasing, update path every 2 seconds (or when empty) and check if touching player
			case CHASING:
				timer += Main.TIMESTEP;
				if ((timer >= 2 || path.size() == 1) && (!TENTACLE_DEBUG || (Main.keys[GLFW_KEY_COMMA] && !Main.pKeys[GLFW_KEY_COMMA]))) {
					timer = 0;
					pathfindTo(Main.getPlayer().tileX(), Main.getPlayer().tileY(), false);
					if(path.empty())
						state = State.REPOSITION;
				}
				if (path.size() > 1)
					followPath();
				if (getHitBounds().intersects(Main.getPlayer().getHitBounds())) {
					state = State.GRABBING;
					speed = 5;
					if(home != null)
						pathfindTo((int) Math.floor(home.x), (int) Math.floor(home.y), false);
					if(path.empty() || home == null) {
						Point next = nextPosition();
						Vector dir = new Vector(next.x-tileX(), next.y-tileY());
						home = new Vector(tileX()+0.5, tileY()+0.5);
						home.add(dir.getScale(2));
						pathfindTo((int) Math.floor(home.x), (int) Math.floor(home.y), false);
						if(path.empty())
							home = home.getSub(dir);
					}
				}
				break;
			// Retrace path, checking every 2 seconds for a new path until there's a path towards the player
			case REPOSITION:
				timer += Main.TIMESTEP;
				if (timer >= 2) {
					timer = 0;
					pathfindTo(Main.getPlayer().tileX(), Main.getPlayer().tileY(), false);
					if(!path.empty())
						state = State.CHASING;
				}
				retracePath();
				break;
			// Delay, then prep and start swinging
			case INJURED:
				timer += Main.TIMESTEP;
				if (timer >= 1) {
					corpseRotation = orientation * 0.5 * Math.PI;
					corpse = new Vector(position.x, position.y).add(Vector.ofAngle(corpseRotation).scale(0.5));
					speed = 40;
					timer = 0;
					state = State.SWINGING;
				}
				Main.getPlayer().getPosition().set(position).add(Vector.ofAngle(orientation*0.5*Math.PI).scale(1.5));
				Main.getPlayer().getVelocity().set(0, 0);
				break;
			// Move corpse around and throw sword and player at opportune times
			case SWINGING:
				timer += Main.TIMESTEP * 0.5;
				corpseRotation += corpseRotationVel * timer * 0.1;
				Main.getPlayer().getPosition().set(corpse).add(Vector.ofAngle(corpseRotation));
				// Throw sword when corpseRotation is perpendicular to sword target
				if(thrownSword == null && Math.abs(corpseRotation) > 4*Math.PI && Math.abs(Util.signedAngularDistance(
						corpseRotation + corpseRotationVel * 0.5 * Math.PI,
						Math.atan2(sword.y-getY(), sword.x - getX()))) < 0.2 * timer) {
					Main.getPlayer().takeAwaySword();
					thrownSword = new ThrownSword(Main.getPlayer().getX(), Main.getPlayer().getY(), sword, corpseRotationVel);
					Main.getLevel().addEntity(thrownSword);
				// Throw player when corpseRotation is perpendicular to throwing target
				} else if(Math.abs(corpseRotation) > 8*Math.PI && Math.abs(Util.signedAngularDistance(
						corpseRotation + corpseRotationVel * 0.5 * Math.PI,
						Math.atan2(throwing.y - getY(), throwing.x - getX()))) < 0.2 * timer) {
					corpse.add(Vector.ofAngle(corpseRotation));
					state = State.RETRACTING;
				}	
				break;
			// Retrace path and switch to dying when reached the end
			case RETRACTING:
				if (corpse.sqDist(throwing) > 1) {
					Vector dir = corpse.unitDir(throwing).scale(0.1);
					corpse.add(dir);
					corpseRotation += corpseRotationVel * timer * 0.1;
					corpseRotationVel *= 0.99;
					Main.getPlayer().getPosition().set(corpse);
					Main.getPlayer().getVelocity().set(dir.getScale(100));
				}
				retracePath();
				if(traveled.isEmpty()) {
					state = State.DYING;
					Main.playCutscene(new Cutscene(1) {
						@Override
						protected Vector getTarget() {
							return thrownSword.getPosition();
						}

						@Override
						protected boolean done() {
							return super.done() && thrownSword.reachedTarget;
						}

						@Override
						protected void onEnd() {
							state = State.DEAD;
						}

						@Override
						protected double getTargetZoom() {
							return 48;
						}

						@Override
						protected double getSmoothing() {
							return Camera.DEFAULT_SMOOTHING * 5;
						}
					});
				}
				break;
		}
	}

	// Update path with a bit of frontway. No 180 degree turns or messing with the last path
	// TODO: watchDirection is redundant
	private void pathfindTo(int x, int y, boolean watchDirection) {
		if (path.empty() || path.size() <= 1 || watchDirection) {
			Point a = new Point(tileX(), tileY()), b = nextPosition();
			int dX = b.x - a.x, dY = b.y - a.y;
			ArrayList<Point> prep = new ArrayList<Point>(traveled);
			if(!prep.contains(a))
				prep.add(a);
			if(!prep.contains(b))
				prep.add(b);
			if(!segments.containsKey(a)) {
				// TODO: Mark unsure of
				segments.put(a, orientation);
				segtypes.put(a, SegType.STRAIGHT);
			}
			if(!segments.containsKey(b)) {
				// TODO: Mark unsure of
				segments.put(b, orientation);
				segtypes.put(b, SegType.STRAIGHT);
			}
			hitbox.add(a.x, a.y, 1, 1);
			hitbox.add(b.x, b.y, 1, 1);
			path = Path.pathfind(tileX() + dX, tileY() + dY, x, y, prep.toArray(new Point[prep.size()]));
			// Remove segments added before pathfinding
			for (int i = 0; i < traveled.size() && !path.empty(); i++)
				path.progress();
		} else {
			// Grant two tiles of previous path to lead into the next path
			Point a = path.progress(), b = path.progress();
			ArrayList<Point> prep = new ArrayList<Point>(traveled);
			if(!prep.contains(a))
				prep.add(a);
			if(!prep.contains(b))
				prep.add(b);
			path = Path.pathfind(b.x, b.y, x, y, prep.toArray(new Point[prep.size()]));
			for (int i = 0; i < traveled.size() && !path.empty(); i++)
				path.progress();
		}
	}

	/**
	 * Follow links of the path
	 */
	private void followPath() {
		// Hit indicates whether we need to progress the path
		boolean hit = false;
		if(TENTACLE_DEBUG){
			if(Main.keys[GLFW_KEY_PERIOD] && !Main.pKeys[GLFW_KEY_PERIOD])
				hit = true;
		} else
			switch (orientation) {
				case 0:
					position.x += Main.TIMESTEP * speed;
					hit = position.x - 0.5 >= path.current().x;
					break;
				case 1:
					position.y += Main.TIMESTEP * speed;
					hit = position.y - 0.5 >= path.current().y;
					break;
				case 2:
					position.x -= Main.TIMESTEP * speed;
					hit = position.x - 0.5 <= path.current().x;
					break;
				case 3:
					position.y -= Main.TIMESTEP * speed;
					hit = position.y - 0.5 <= path.current().y;
					break;
			}
		// If we need to progress the path, snap position to current node and progress towards next one, adding a segment
		if (hit) {
			position.x = path.current().x + 0.5;
			position.y = path.current().y + 0.5;

			Point p = new Point(tileX(), tileY());
			traveled.add(p);
			hitbox.add(tileX(), tileY(), 1, 1);

			int preOrientation = orientation;
			nextOrientation();

			if ((preOrientation + orientation) % 2 == 0) {
				segments.put(p, preOrientation);
				segtypes.put(p, SegType.STRAIGHT);
			} else if ((orientation > preOrientation && (orientation != 3 || preOrientation != 0))
					|| (orientation == 0 && preOrientation == 3)) {
				corpseRotationVel = 1;
				segments.put(p, preOrientation);
				segtypes.put(p, SegType.TURN_RIGHT);
			} else {
				corpseRotationVel = -1;
				segments.put(p, preOrientation);
				segtypes.put(p, SegType.TURN_LEFT);
			}

			path.progress();
		}
	}

	/**
	 * Retrace the path back to start
	 */
	private void retracePath() {
		if(traveled.isEmpty())
			return;
		boolean hit = false;
		switch(orientation) {
			case 0:
				position.x -= Main.TIMESTEP * speed;
				hit = position.x - 0.5 <= traveled.getLast().x;
				break;
			case 1:
				position.y -= Main.TIMESTEP * speed;
				hit = position.y - 0.5 <= traveled.getLast().y;
				break;
			case 2:
				position.x += Main.TIMESTEP * speed;
				hit = position.x - 0.5 >= traveled.getLast().x;
				break;
			case 3:
				position.y += Main.TIMESTEP * speed;
				hit = position.y - 0.5 >= traveled.getLast().y;
				break;

		}
		if(hit) {
			position.x = traveled.getLast().x + 0.5;
			position.y = traveled.getLast().y + 0.5;
			orientation = segments.get(traveled.getLast());
			traveled.removeLast();

			hitbox = new Hitboxes();
			for(Point p : traveled)
				hitbox.add(p.x, p.y, 1, 1);
		}
	}

	/**
	 * Quickly sets the orientation based on the path direction
	 */
	private void nextOrientation() {
		if (path.next() == null)
			return;	
		if (path.next().x > path.current().x)
			orientation = 0;
		else if (path.next().y > path.current().y)
			orientation = 1;
		else if (path.next().x < path.current().x)
			orientation = 2;
		else if (path.next().y < path.current().y)
			orientation = 3;
	}

	/**
	 * Quickly grab the next point that that the tentacle would head towards with
	 * the given orientation
	 */
	private Point nextPosition() {
		return switch (orientation) {
			case 0 -> new Point(tileX() + 1, tileY());
			case 1 -> new Point(tileX(), tileY() + 1);
			case 2 -> new Point(tileX() - 1, tileY());
			case 3 -> new Point(tileX(), tileY() - 1);
			default -> new Point(tileX(), tileY());
		};
	}

	public void render() {
		Shader.Image.use().push().drawToWorld().setModel(new Matrix4f().identity());
		// Draw dirt mound
		sheet.draw(48, 32, 48, 16, start.x - 1.5, start.y - 0.5, 3, 1, G.LAYER_1);

		for (Point p : traveled) {
			sheet.draw(96 + segments.get(p)*16, 
						segtypes.get(p) == SegType.STRAIGHT ? 0 : segtypes.get(p) == SegType.TURN_LEFT ? 16 : 32,
						16, 16, p.x, p.y, 1, 1, G.LAYER_1);
		}

		Shader.Image.use().move(getX(), getY()).rotate(Math.PI * 0.5 * orientation);

		if(state == State.WINDUP)
			Shader.Image.use().move((Math.random()-0.5)*timer*0.1, (Math.random()-0.5)*timer*0.1);

		if(state == State.GRABBING || state == State.GRABBED)
			Shader.Image.use().crop(48.f / sheet.getWidth(), 0, 48.f / sheet.getWidth(), 16.f / sheet.getHeight()).move(1, 0).scale(1.5, 0.5);
		else if(state == State.INJURED)
			Shader.Image.use().crop(0, 32.f / sheet.getHeight(), 48.f / sheet.getWidth(), 16.f / sheet.getHeight()).move(1, 0).scale(1.5, 0.5);
		else if(state == State.DEAD || state == State.DYING || state == State.RETRACTING || state == State.SWINGING)
			Shader.Image.use().crop(32.f / sheet.getWidth(), 16.f / sheet.getHeight(), 16.f / sheet.getWidth(), 16.f / sheet.getHeight()).scale(0.5);
		else {
			double gX = getX() - 0.5, tX = Math.floor(gX);
			double gY = getY() - 0.5, tY = Math.floor(gY);
			double clipset = switch(orientation) {
			case 0 -> 1 + tX - gX;
			case 1 -> 1 + tY - gY;
			case 2 -> gX - tX;
			case 3 -> gY - tY;
			default -> 0;
			};
			Shader.Image.use().crop(clipset * 16.f / sheet.getWidth(), 0, (48.f - clipset * 16.f) / sheet.getWidth(), 16.f / sheet.getHeight()).move(1+clipset*0.5, 0).scale(1.5-clipset*0.5, 0.5);
		}

		Shader.Image.use().image(sheet);
		G.drawSquare();

		if(state == State.DEAD || state == State.DYING || state == State.RETRACTING || state == State.SWINGING) {
			Shader.Image.use().push();
			Shader.Image.use().setModel(new Matrix4f().identity()).move(corpse.x, corpse.y).rotate(corpseRotation).move(0.9 + (state == State.SWINGING ? 0 : -0.9), 0).layer(G.LAYER_1).scale(1, 0.5).crop(64.f / sheet.getWidth(), 16.f / sheet.getHeight(), 32.f / sheet.getWidth(), 16.f / sheet.getHeight());
			G.drawSquare();
			Shader.Image.use().pop();
		}

		// Draw dirt mound cover
		if(state == State.DEAD || state == State.DYING)
			sheet.draw(48, 16, 16, 16, start.x - 0.5, start.y - 0.5, 1, 1, G.LAYER_1);
		else
			sheet.draw(64, 32, 16, 16, start.x - 0.5, start.y - 0.5, 1, 1, G.LAYER_1);
		Shader.Image.use().pop();

		if (health < 10 && (state == State.GRABBING || state == State.GRABBED)) {
			// TODO: Tentacle health bar
			double w = 71.0 / 16.0, h = 7.0 / 16.0, 
				x = getX() - (orientation == 1 ? 1.5 : 0.5)*w,
				y = getY() - (orientation == 3 ? 1.5 : 0.5)*w;
			healthOutline.draw(x, y, w, h, G.LAYER_2);
			healthRed.draw(x, y, w, h, G.LAYER_2);
			healthGreen.draw(0, 0, 71 * health / 10.0, 7.0, x, y, w*health/10.0, h, G.LAYER_2);
		}

	}

	public Hitbox getRenderBounds() {
		return new Hitrect(Main.getLevel().bounds);
	}

	public Hitbox getUpdateBounds() {
		return new Hitrect(Main.getLevel().bounds);
	}

	public Hitbox getSolidBounds() {
		if(state == State.DEAD || state == State.DYING || state == State.RETRACTING)
			return Hitnull.instance;
		return hitbox;
	}

	public Hitbox getHitBounds() {
		return switch (orientation) {
			case 0 -> new Hitrect(getX() - 0.5, getY() - 0.5, 2, 1);
			case 1 -> new Hitrect(getX() - 0.5, getY() - 0.5, 1, 2);
			case 2 -> new Hitrect(getX() - 1.5, getY() - 0.5, 2, 1);
			case 3 -> new Hitrect(getX() - 0.5, getY() - 1.5, 1, 2);
			default -> new Hitrect(getX() - 0.5, getY() - 0.5, 1, 1);
		};
	}

	public void dealPlayerDamage(double damage) {
		if(System.currentTimeMillis() - lastHit < 250)
			return;
		lastHit = System.currentTimeMillis();
		health -= damage;
		if (health <= 0 && (state == State.GRABBING || state == State.GRABBED || state == State.CHASING)) {
			timer = 0;
			state = State.INJURED;
		}
	}
}

