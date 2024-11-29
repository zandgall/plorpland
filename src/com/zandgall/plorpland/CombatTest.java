package com.zandgall.plorpland;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Matrix4f;

import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.graphics.Shader;

public class CombatTest {
	public static boolean CHOPPY = false, RELATIVE_SPECIALS = true;
	public static int INPUT_MODE = 0;

	private static final Image
		choppy0 = new Image("/hud/combattest/choppy0.png"),
		choppy1 = new Image("/hud/combattest/choppy1.png"),
		input0 = new Image("/hud/combattest/input0.png"),
		input1 = new Image("/hud/combattest/input1.png"),
		relative0 = new Image("/hud/combattest/relativespecials0.png"),
		relative1 = new Image("/hud/combattest/relativespecials1.png");

	public static void tick() {
		if(Main.keyEv[GLFW_KEY_C])
			CHOPPY = !CHOPPY;
		if(Main.keyEv[GLFW_KEY_R])
			RELATIVE_SPECIALS = !RELATIVE_SPECIALS;
		if(Main.keyEv[GLFW_KEY_I]) {
			INPUT_MODE = (INPUT_MODE + 1) % 5;
			if(INPUT_MODE == 3)
				glfwSetInputMode(Main.window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
			if(INPUT_MODE == 4)
				glfwSetInputMode(Main.window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
		}
	}

	public static void render() {
		Shader.TintedImage.use().push().drawToScreen()
			.setModel(new Matrix4f()).setModel(5, 5, choppy0.getWidth()*2, choppy0.getHeight()*2)
			.tint(CHOPPY ? 0 : 1, CHOPPY ? 1 : 0, 0);
		if((System.currentTimeMillis() / 1000) % 2 == 0)
			Shader.TintedImage.use().image(choppy0);
		else
			Shader.TintedImage.use().image(choppy1);
		G.drawSquare();
		Shader.TintedImage.use().pop();

		Shader.TintedImage.use().push().drawToScreen()
			.setModel(new Matrix4f()).setModel(15+choppy0.getWidth()*2+input0.getWidth()*2, 5, relative0.getWidth()*2, relative0.getHeight()*2)
			.tint(RELATIVE_SPECIALS ? 0 : 1, RELATIVE_SPECIALS ? 1 : 0, 0);
		if((System.currentTimeMillis() / 1000) % 2 == 0)
			Shader.TintedImage.use().image(relative0);
		else
			Shader.TintedImage.use().image(relative1);
		G.drawSquare();
		Shader.TintedImage.use().pop();


		Shader.Image.use().push().drawToScreen().setModel(new Matrix4f())
			.setModel(10 + choppy0.getWidth()*2, 5, input0.getWidth() * 2, input0.getHeight() / 2.5)
			.crop(0, 0.2 * INPUT_MODE, 1, 0.2);
		if((System.currentTimeMillis() / 1000) % 2 == 0)
			Shader.Image.use().image(input0);
		else
			Shader.Image.use().image(input1);
		G.drawSquare();
		Shader.Image.use().pop();
	}
}
