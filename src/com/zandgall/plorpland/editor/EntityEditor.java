package com.zandgall.plorpland.editor;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Matrix4f;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.entity.Entity;
import com.zandgall.plorpland.entity.PlantedSword;
import com.zandgall.plorpland.entity.Tree;
import com.zandgall.plorpland.entity.misc.combattest.*;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Layer;
import com.zandgall.plorpland.graphics.Shader;
import com.zandgall.plorpland.util.Rect;

public class EntityEditor extends Editor {

	Entity selected = null;

	public EntityEditor() {

	}

	public void tick(Level lvl) {
		if(Main.keys[GLFW_KEY_LEFT])
			x -= 6.0 / camera.getZoom() * (Main.keys[GLFW_KEY_LEFT_CONTROL] ? 0.1 : 1);
		if(Main.keys[GLFW_KEY_RIGHT])
			x += 6.0 / camera.getZoom() * (Main.keys[GLFW_KEY_LEFT_CONTROL] ? 0.1 : 1);
		if(Main.keys[GLFW_KEY_UP])
			y -= 6.0 / camera.getZoom() * (Main.keys[GLFW_KEY_LEFT_CONTROL] ? 0.1 : 1);
		if(Main.keys[GLFW_KEY_DOWN])
			y += 6.0 / camera.getZoom() * (Main.keys[GLFW_KEY_LEFT_CONTROL] ? 0.1 : 1);

		if(Main.keyEv[GLFW_KEY_SPACE]) {
			selected = null;
			for(Entity e : lvl.entities) {
				if(e.getRenderBounds().intersects(x-0.05, y-0.05, 0.1, 0.1)) {
					lvl.entities.remove(e);
					lvl.entities.add(e);
					selected = e;
					break;
				}
			}	
		}

		if(Main.keys[GLFW_KEY_LEFT_CONTROL]) {
			if(Main.keyEv[GLFW_KEY_0]) {
				selected = new PlantedSword(x, y);
				lvl.entities.add(selected);
			} else if(Main.keyEv[GLFW_KEY_1]) {
				selected = new Plorp(x, y);
				lvl.entities.add(selected);
			} else if(Main.keyEv[GLFW_KEY_2]) {
				selected = new Tree(x, y);
				lvl.entities.add(selected);
			} else if(Main.keyEv[GLFW_KEY_3]) {
				selected = new HealthFlower(x, y);
				lvl.entities.add(selected);
			}
		} else if(selected!=null) {
			if(Main.keys[GLFW_KEY_1]) {
				selected.setX(x);
				selected.setY(y);
			} else if (Main.keyEv[GLFW_KEY_DELETE]) {
				lvl.entities.remove(selected);
				selected = null;
			}
		}
	}

	public void render(Level lvl) {
		lvl.renderGraphics();
		Shader.Image.use().push().alpha(0.1);
		lvl.renderTiles();
		Shader.Image.use().pop();
		lvl.renderSpecialImages();
		Layer.HUD.use();
		lvl.renderEntities();
		for(Entity e : lvl.entities) {
			Rect r = e.getRenderBounds().getBounds();
			// System.out.printf("Drawing box at %.1f, %.1f, %.1f, %.1f%n", r.x, r.y, r.w, r.h);
			Shader.Color.use().push().drawToWorld().setModel(new Matrix4f().identity())
				.color(0, 0, 0).alpha(0.2)
				.setModel(r.x, r.y + r.h, r.w, 3.0 / camera.getZoom());
			G.drawSquare();
			Shader.Color.use().setModel(new Matrix4f().identity())
				.setModel(r.x, r.y, r.w, 3.0 / camera.getZoom());
			G.drawSquare();
			Shader.Color.use().setModel(new Matrix4f().identity())
				.setModel(r.x, r.y, 3.0 / camera.getZoom(), r.h);
			G.drawSquare();
			Shader.Color.use().setModel(new Matrix4f().identity())
				.setModel(r.x + r.w, r.y, 3.0 / camera.getZoom(), r.h);
			G.drawSquare();
			Shader.Color.use().pop();
		}
		Shader.Color.use().push().drawToWorld()
			.color(1, 0, 0).alpha(0.5)
			.setModel(x-(3.0 / camera.getZoom()), y-(3.0 / camera.getZoom()), 6.0 / camera.getZoom(), 6.0 / camera.getZoom());
		G.drawSquare();
		if(selected!=null) {
			Shader.Color.use().setModel(new Matrix4f().identity())
				.setModel(selected.getRenderBounds().getBounds().x, selected.getRenderBounds().getBounds().y, selected.getRenderBounds().getBounds().w, selected.getRenderBounds().getBounds().h)
				.alpha(0.1).color(0, 1, 0);
			G.drawSquare();
		}
		Shader.Color.use().pop();
		Layer.LEVEL_BASE.use();
	}
}
