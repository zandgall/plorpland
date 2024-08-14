package com.zandgall.plorpland.graphics;

import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.util.Scanner;

import org.lwjgl.system.MemoryStack;

public class Shader {
	
	private int v, f, program;

	public Shader(String vPath, String fPath) {	
		// Gather all vertex shader code
		Scanner vScanner = new Scanner(Shader.class.getResourceAsStream(vPath));
		StringBuilder vCode = new StringBuilder();
		while(vScanner.hasNextLine())
			vCode.append(vScanner.nextLine() + System.lineSeparator());
		vScanner.close();

		// Gather all fragment shader code
		Scanner fScanner = new Scanner(Shader.class.getResourceAsStream(fPath));
		StringBuilder fCode = new StringBuilder();
		while(fScanner.hasNextLine())
			fCode.append(fScanner.nextLine() + System.lineSeparator());
		fScanner.close();

		// Generate program and shaders with provided code
		program = glCreateProgram();

		int[] result = {0};

		// Compile vertex shader, checking for errors
		v = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(v, vCode);
		glCompileShader(v);
		glGetShaderiv(v, GL_COMPILE_STATUS, result);
		if(result[0] == GL_FALSE) {
			glDeleteShader(v);
			System.err.println(glGetShaderInfoLog(v));
			throw new RuntimeException("Failed to compile vertex shader! \"" + vPath + "\"");
		}

		// Compile fragment shader, checking for errors
		f = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(f, fCode);
		glCompileShader(f);
		glGetShaderiv(f, GL_COMPILE_STATUS, result);
		if(result[0] == GL_FALSE) {
			glDeleteShader(f);
			System.err.println(glGetShaderInfoLog(f));
			throw new RuntimeException("Failed to compile fragment shader! \"" + fPath + "\"");
		}

		// Attach shaders to program, check for errors
		glAttachShader(program, v);
		glAttachShader(program, f);
		glLinkProgram(program);
		glValidateProgram(program);
		glGetProgramiv(program, GL_VALIDATE_STATUS, result);
		if(result[0] == GL_FALSE) {
			System.err.println(glGetProgramInfoLog(program));
			throw new RuntimeException("Failed to finalize program! \"" + vPath + "\" + \"" + fPath + "\"");
		}

		// Delete shaders
		glDeleteShader(v);
		glDeleteShader(f);
	}

	private int uniformLocation(String name) {
		return glGetUniformLocation(program, name);
	}

	public void use() {
		glUseProgram(program);
	}

	public void setMatrix(String name, Matrix4f matrix) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer buffer = matrix.get(stack.mallocFloat(16));
			glUniformMatrix4fv(uniformLocation(name), false, buffer);
		}
	}

	public void setFloat(String name, float f) {
		glUniform1f(uniformLocation(name), f);
	}

	public void setVec4(String name, float x, float y, float z, float w) {
		glUniform4f(uniformLocation(name), x, y, z, w);
	}

	public void setInt(String name, int i) {
		glUniform1i(uniformLocation(name), i);
	}

	public static void init() {
		Image.s = new Shader("shaders/image.vs", "shaders/image.fs");
	}

	public static class Image {
		private static Shader s;

		public static void use() {
			s.use();
		}
	
		public static void setProjection(Matrix4f matrix) {
			s.setMatrix("projection", matrix);
		}

		public static void setView(Matrix4f matrix) {
			s.setMatrix("view", matrix);
		}

		public static void setModel(Matrix4f matrix) {
			s.setMatrix("model", matrix);
		}

		public static void setModel(float x, float y, float w, float h, float layer) {
			setModel(new Matrix4f().translate(x + w * 0.5f, y + h * 0.5f, layer).scale(w, h, 1));
		}

		public static void setCrop(float x, float y, float w, float h) {
			s.setVec4("crop", x, y, w, h);
		}

		public static void setTexture(int texture) {
			s.setInt("texture", texture);
		}

		public static void setAlpha(float alpha) {
			s.setFloat("alpha", alpha);
		}

	}

}
