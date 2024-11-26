package com.zandgall.plorpland.editor;

import static org.lwjgl.glfw.GLFW.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.graphics.Shader;
import com.zandgall.plorpland.level.SpecialImage;
import com.zandgall.plorpland.util.Rect;

public class SpecialImageEditor {

	double x = 0, y = 0;

	SpecialImage selected = null;

	public SpecialImageEditor() {

	}

	public void tick(Level lvl) {
		if(Main.keys[GLFW_KEY_LEFT])
			x -= 6.0 / LevelEditor.ZOOM * (Main.keys[GLFW_KEY_LEFT_CONTROL] ? 0.1 : 1);
		if(Main.keys[GLFW_KEY_RIGHT])
			x += 6.0 / LevelEditor.ZOOM * (Main.keys[GLFW_KEY_LEFT_CONTROL] ? 0.1 : 1);
		if(Main.keys[GLFW_KEY_UP])
			y -= 6.0 / LevelEditor.ZOOM * (Main.keys[GLFW_KEY_LEFT_CONTROL] ? 0.1 : 1);
		if(Main.keys[GLFW_KEY_DOWN])
			y += 6.0 / LevelEditor.ZOOM * (Main.keys[GLFW_KEY_LEFT_CONTROL] ? 0.1 : 1);

		if(Main.keyEv[GLFW_KEY_A]) {
			File newSpecialImage = LevelEditor.openFileDialog("Add special image");
			if(newSpecialImage != null && newSpecialImage.isFile() && newSpecialImage.exists() && newSpecialImage.getName().endsWith(".png")) {
				try {
					selected = new SpecialImage("", x, y, 0, 0, 0.1);
					selected.setImage(new Image(Image.textureFrom(new FileInputStream(newSpecialImage))));
					lvl.specialImages.get(0).add(selected);
					System.out.printf("Added special image : %.1f %.1f, %.1f %.1f, %.1f [%.1f, %.1f, %.1f, %.1f]%n", selected.getX(), selected.getY(), selected.getXOff(), selected.getYOff(), selected.getDamping(), selected.getRenderBox().x, selected.getRenderBox().y, selected.getRenderBox().w, selected.getRenderBox().h);
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("Could not load specialimage");
					selected = null;
				}
			} else
				System.err.println("Needs to be an existing .png file!");
		}

		if(Main.keyEv[GLFW_KEY_SPACE]) {
			for(SpecialImage s : lvl.specialImages.get(0)) {
				if(s.getRenderBox().intersects(x-0.05, y-0.05, 0.1, 0.1)) {
					lvl.specialImages.get(0).remove(s);
					lvl.specialImages.get(0).add(s);
					selected = s;
					break;
				}
			}
		}

		if(selected!=null) {
			if(Main.keys[GLFW_KEY_1]) {
				selected.setX(x);
				selected.setY(y);
			} else if(Main.keys[GLFW_KEY_2]) {
				selected.setXOff(x - selected.getX());
				selected.setYOff(y - selected.getY());
			} else if(Main.keys[GLFW_KEY_3]) {
				selected.setDamping(y - selected.getY());
			}
		}
	
		Main.getCamera().target(x, y, LevelEditor.ZOOM);
		Main.getCamera().tick();
	}

	public void render(Level lvl) {
		lvl.renderGraphics();
		lvl.renderEntities();
		Shader.Image.use().push().alpha(0.1);
		lvl.renderTiles();
		Shader.Image.use().pop();
		lvl.renderSpecialImages();
		for(SpecialImage i : lvl.specialImages.get(0)) {
			Shader.Image.use().push().drawToWorld().setModel(new Matrix4f().identity());
			i.render();
			Shader.Image.use().pop();

			Rect r = i.getRenderBox();
			// System.out.printf("Drawing box at %.1f, %.1f, %.1f, %.1f%n", r.x, r.y, r.w, r.h);
			Shader.Color.use().push().drawToWorld().setModel(new Matrix4f().identity())
				.color(0, 0, 0).alpha(0.2)
				.setModel(r.x, r.y + r.h, r.w, 3.0 / LevelEditor.ZOOM);
			G.drawSquare();
			Shader.Color.use().setModel(new Matrix4f().identity())
				.setModel(r.x, r.y, r.w, 3.0 / LevelEditor.ZOOM);
			G.drawSquare();
			Shader.Color.use().setModel(new Matrix4f().identity())
				.setModel(r.x, r.y, 3.0 / LevelEditor.ZOOM, r.h);
			G.drawSquare();
			Shader.Color.use().setModel(new Matrix4f().identity())
				.setModel(r.x + r.w, r.y, 3.0 / LevelEditor.ZOOM, r.h);
			G.drawSquare();

			Shader.Color.use().setModel(new Matrix4f().identity())
				.color(1, 0, 0).alpha(1.0)
				.setModel(i.getX()-(1.5 / LevelEditor.ZOOM), i.getY()-(1.5 / LevelEditor.ZOOM), 3.0 / LevelEditor.ZOOM, 3.0 / LevelEditor.ZOOM);
			G.drawSquare();

			double xO = i.getXOff(), yO = i.getYOff();
			double len = Math.sqrt(xO*xO+yO*yO) / (3.0 / LevelEditor.ZOOM);
			Shader.Color.use().setModel(new Matrix4d(
				// i.getYOff()/len, i.getXOff()/len, 0, 0,
				-yO / len, xO / len, 0, 0,
				xO, yO, 0, 0,
				0, 0, 1, 0,
				i.getX(), i.getY(), 0, 1
			)).color(0, 0, 0).alpha(0.2);
			G.draw01Square();

			Shader.Color.use().setModel(new Matrix4f().identity())
				.color(0, 1, 0).alpha(1.0)
				.setModel(i.getX()+i.getXOff()-(3.0 / LevelEditor.ZOOM), i.getY()+i.getYOff()-(3.0 / LevelEditor.ZOOM), 6.0 / LevelEditor.ZOOM, 6.0 / LevelEditor.ZOOM);
			G.drawSquare();

			Shader.Color.use().setModel(new Matrix4d(
					6.0 / LevelEditor.ZOOM, 0, 0, 0,
					0, i.getDamping(), 0, 0,
					0, 0, 1, 0,
					i.getX(), i.getY(), 0, 1
				))
				.color(1, i.getDamping() < 0 ? 1 : 0, i.getDamping() > 0 ? 1 : 0).alpha(0.5);
			G.drawSquare();

			Shader.Color.use().pop();
		}
		Shader.Color.use().push().drawToWorld()
			.color(1, 0, 0).alpha(0.5)
			.setModel(x-(3.0 / LevelEditor.ZOOM), y-(3.0 / LevelEditor.ZOOM), 6.0 / LevelEditor.ZOOM, 6.0 / LevelEditor.ZOOM);
		G.drawSquare();
		if(selected!=null) {
			Shader.Color.use().setModel(new Matrix4f().identity())
				.setModel(selected.getRenderBox().x, selected.getRenderBox().y, selected.getRenderBox().w, selected.getRenderBox().h)
				.alpha(0.1).color(0, 1, 0);
			G.drawSquare();
		}
		Shader.Color.use().pop();
	}

	public void switchTo() {
		// selected = null; // TODO: Uncomment
		x = Main.getCamera().getX();
		y = Main.getCamera().getY();
	}
}
