/* zandgall

##
#

: MADE IN NEOVIM */

package	com.zandgall.plorpland.entity.octoplorp;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.entity.Entity;
import com.zandgall.plorpland.entity.PlantedSword;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.graphics.Layer;
import com.zandgall.plorpland.graphics.Shader;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Hitnull;
import com.zandgall.plorpland.util.Hitrect;
import com.zandgall.plorpland.util.Util;
import com.zandgall.plorpland.util.Vector;

public class ThrownSword extends Entity {

	private static final Image image = new Image("/entity/sword.png"), plaque = new Image("/entity/sword_plaque.png");
	
	private Vector target;
	private double rotation = 0, rotationVel;

	public PlantedSword endState;
	public boolean reachedTarget = false;

	public ThrownSword(double x, double y, Vector target, double rotationVel) {
		super(x, y);
		this.target = target;
		this.rotationVel = rotationVel;
		endState = new PlantedSword(target.x, target.y);
	}

	public void tick() {
		position.add(position.unitDir(target).scale(10 * Main.TIMESTEP));
		rotation += rotationVel * 20 * Main.TIMESTEP;
		if(position.sqDist(target) < 1 && Util.signedAngularDistance(rotation, 1.5 * Math.PI) < 30 * Main.TIMESTEP) {
			position.set(target);
			Main.getLevel().removeEntity(this);
			Main.getLevel().addEntity(endState);
			reachedTarget = true;
		}
	}

	public void render() {
		Layer.ENTITY_BASE.use();
		Shader.Image.use().push().drawToWorld().move(getX(), getY()).rotate(rotation).scale(1, 0.5).image(image);
		G.drawSquare();
		plaque.draw(target.x-1, target.y-1.8, 2, 2);
		Shader.Image.use().pop();
	}

	@Override
	public Hitbox getHitBounds() {
		return Hitnull.instance;
	}

	@Override
	public Hitbox getSolidBounds() {	
		return Hitnull.instance;
	}

	@Override
	public Hitbox getRenderBounds() {
		return new Hitrect(getX()-1, getY()-1, 2, 2);
	}

	@Override
	public Hitbox getUpdateBounds() {
		return new Hitrect(Main.getLevel().bounds);
	}
}
