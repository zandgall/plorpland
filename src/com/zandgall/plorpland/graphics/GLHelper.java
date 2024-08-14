package com.zandgall.plorpland.graphics;

import static org.lwjgl.opengl.GL30.*;

public class GLHelper {

	// Square VAO, VBO, EBO
	private static int sqVAO, sqVBO, sqEBO;

	public static void init() {
		sqVAO = glGenVertexArrays();
		glBindVertexArray(sqVAO);
		sqVBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, sqVBO);
		glBufferData(GL_ARRAY_BUFFER, new float[] {
			 1.0f,  1.0f, 0.0f, 1, 1, 1, // top right
			 1.0f, -1.0f, 0.0f, 1, 1, 0, // bottom right
			-1.0f, -1.0f, 0.0f, 1, 0, 0, // bottom left
			-1.0f,  1.0f, 0.0f, 1, 0, 1, // top left
		}, GL_STATIC_DRAW);
		sqEBO = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, sqEBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, new int[] {0, 1, 3, 1, 2, 3}, GL_STATIC_DRAW);

		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 6 * Float.BYTES, 0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 6 * Float.BYTES, 4 * Float.BYTES);

		glBindVertexArray(0);
	}

	public static void drawRect() {
		glBindVertexArray(sqVAO);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
	}
}
