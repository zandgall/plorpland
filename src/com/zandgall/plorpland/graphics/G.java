package com.zandgall.plorpland.graphics;

import static org.lwjgl.opengl.GL30.*;

public class G {
	// Square VAO, VBO, EBO
	private static int sqVAO, sq01VAO;

	public static void init() {
		sqVAO = glGenVertexArrays();
		glBindVertexArray(sqVAO);
		int sqVBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, sqVBO);
		glBufferData(GL_ARRAY_BUFFER, new float[] {
			 1.0f,  1.0f, 0.0f, 1, 1, 1, // top right
			 1.0f, -1.0f, 0.0f, 1, 1, 0, // bottom right
			-1.0f, -1.0f, 0.0f, 1, 0, 0, // bottom left
			-1.0f,  1.0f, 0.0f, 1, 0, 1, // top left
		}, GL_STATIC_DRAW);
		int sqEBO = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, sqEBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, new int[] {0, 1, 3, 1, 2, 3}, GL_STATIC_DRAW);

		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 6 * Float.BYTES, 0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 6 * Float.BYTES, 4 * Float.BYTES);

		glBindVertexArray(0);
		
		sq01VAO = glGenVertexArrays();
		glBindVertexArray(sq01VAO);
		int sq01VBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, sq01VBO);
		glBufferData(GL_ARRAY_BUFFER, new float[] {
			1.0f, 1.0f, 0.0f, 1, 1, 1, // top right
			1.0f, 0.0f, 0.0f, 1, 1, 0, // bottom right
			0.0f, 0.0f, 0.0f, 1, 0, 0, // bottom left
			0.0f, 1.0f, 0.0f, 1, 0, 1, // top left
		}, GL_STATIC_DRAW);
		int sq01EBO = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, sq01EBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, new int[] {0, 1, 3, 1, 2, 3}, GL_STATIC_DRAW);

		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 6 * Float.BYTES, 0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 6 * Float.BYTES, 4 * Float.BYTES);

		glBindVertexArray(0);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	public static void drawSquare() {
		glBindVertexArray(sqVAO);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
	}

	public static void draw01Square() {
		glBindVertexArray(sq01VAO);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
	}

}
