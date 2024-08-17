package com.zandgall.plorpland.graphics;

import org.joml.Matrix4d;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL30.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import org.lwjgl.system.MemoryStack;

public class Shader {
	
	private int v, f, program;

	public static ImageShader Image = new ImageShader();

	public Shader(String vPath, String fPath) throws IOException {
		// Gather all vertex shader code
		BufferedReader vScanner = new BufferedReader(new InputStreamReader(Shader.class.getResourceAsStream(vPath)));
		StringBuilder vCode = new StringBuilder();
		while(vScanner.ready())
			vCode.append(vScanner.readLine() + System.lineSeparator());
		vScanner.close();

		// Gather all fragment shader code
		BufferedReader fScanner = new BufferedReader(new InputStreamReader(Shader.class.getResourceAsStream(fPath)));
		StringBuilder fCode = new StringBuilder();
		while(fScanner.ready())
			fCode.append(fScanner.readLine() + System.lineSeparator());
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
			System.err.println("Log: " + glGetShaderInfoLog(v));
			glDeleteShader(v);
			throw new RuntimeException("Failed to compile vertex shader! \"" + vPath + "\"");
		}

		// Compile fragment shader, checking for errors
		f = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(f, fCode);
		glCompileShader(f);
		glGetShaderiv(f, GL_COMPILE_STATUS, result);
		if(result[0] == GL_FALSE) {
			System.err.println("Log: " + glGetShaderInfoLog(f));
			glDeleteShader(f);
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

	public void setMatrix(String name, Matrix4d matrix) {
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

	public void setVec4(String name, double x, double y, double z, double w) {
		glUniform4f(uniformLocation(name), (float)x, (float)y, (float)z, (float)w);
	}

	public void setInt(String name, int i) {
		glUniform1i(uniformLocation(name), i);
	}

	public static void init() {
		try {
			Image.s = new Shader("/shaders/image.vs", "/shaders/image.fs");
			Image.s.use();
			Image.s.setInt("text", 0);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not initiate one or more shaders!");
		}
	}

	public static class ImageShader {
		private Shader s;

		private Matrix4d model = new Matrix4d();

		public ImageShader use() {
			s.use();
			s.setMatrix("model", model);
			return this;
		}
	
		public void setProjection(Matrix4f matrix) {
			s.use();
			s.setMatrix("projection", matrix);
		}

		public void setView(Matrix4f matrix) {
			s.use();
			s.setMatrix("view", matrix);
		}

		public void setModel(Matrix4d matrix) {
			model = matrix;
		}

		public void setModel(double x, double y, double w, double h, double layer) {
			setModel(new Matrix4d().translate(x + w * 0.5f, y + h * 0.5f, layer).scale(w*0.5f, h*0.5f, 1));
		}

		public void setTexture(int texture) {
			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, texture);
		}

		public ImageShader reset() {
			model.identity();
			return this;
		}

		public ImageShader at(double x, double y) {
			model.translate(x, y, 0);
			return this;
		}

		public ImageShader layer(double l) {
			model.translate(0, 0, l);
			return this;
		}

		public ImageShader rotate(double rotation) {
			model.rotateZ(rotation);
			return this;
		}

		public ImageShader scale(double w, double h) {
			model.scale(w, h, 1);
			return this;
		}

		public ImageShader scale(double scale) {
			return scale(scale, scale);
		}

		public ImageShader alpha(double alpha) {
			s.use();
			s.setFloat("alpha", (float)alpha);
			return this;
		}

		public ImageShader texture(int texture) {
			setTexture(texture);
			return this;
		}

		public ImageShader image(Image i) {
			return texture(i.getTexture());
		}

		public ImageShader crop(double x, double y, double w, double h) {
			s.use();
			s.setVec4("crop", x, y, w, h);
			return this;
		}

	}

}
