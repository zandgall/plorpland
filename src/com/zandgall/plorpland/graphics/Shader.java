package com.zandgall.plorpland.graphics;

import org.joml.Matrix4d;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL30.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.system.MemoryStack;

public class Shader {
	
	private int v, f, program;

	public static ArrayList<MVPShader> MVPs = new ArrayList<>();

	public static ImageShader Image;
	public static ColorShader Color;

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

	public void setVec3(String name, float x, float y, float z) {
		glUniform3f(uniformLocation(name), x, y, z);
	}

	public void setVec3(String name, double x, double y, double z) {
		glUniform3f(uniformLocation(name), (float)x, (float)y, (float)z);
	}


	public void setInt(String name, int i) {
		glUniform1i(uniformLocation(name), i);
	}

	public static void init() {
		Image = new ImageShader();
		Color = new ColorShader();
	}

	// A shader class blueprint that provides transformation modification
	public static abstract class MVPShader {
		protected Shader s;

		protected Matrix4d model = new Matrix4d();

		public MVPShader() {
			Shader.MVPs.add(this);
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

		public MVPShader use() {
			s.use();
			s.setMatrix("model", model);
			return this;
		}


		public MVPShader reset() {
			model.identity();
			return this;
		}

		public MVPShader at(double x, double y) {
			model.translate(x, y, 0);
			return this;
		}

		public MVPShader layer(double l) {
			model.translate(0, 0, l);
			return this;
		}

		public MVPShader rotate(double rotation) {
			model.rotateZ(rotation);
			return this;
		}

		public MVPShader scale(double w, double h) {
			model.scale(w, h, 1);
			return this;
		}

		public MVPShader scale(double scale) {
			return scale(scale, scale);
		}
	}

	// Shader state and interfacing class for image.vs/fs
	public static class ImageShader extends MVPShader {
		public ImageShader() {
			super();
			try {
				s = new Shader("/shaders/image.vs", "/shaders/image.fs");
				s.use();
				s.setInt("text", 0);
			} catch(IOException e) {
				System.err.println("Couldn't load Image Shader!");
				e.printStackTrace();
			}
		}

		// Chainable parent interaction
		public ImageShader use() { return (ImageShader)super.use(); }
		public ImageShader reset() { return (ImageShader)super.reset(); }
		public ImageShader at(double x, double y) { return (ImageShader)super.at(x, y); }
		public ImageShader layer(double l) { return (ImageShader)super.layer(l); }
		public ImageShader rotate(double rotation) { return (ImageShader)super.rotate(rotation); }
		public ImageShader scale(double w, double h) { return (ImageShader)super.scale(w, h); }
		public ImageShader scale(double scale) { return (ImageShader)super.scale(scale); }

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

	// Shader state and interfacing class for color.vs/fs
	// Draws a solid color with the given transformation
	public static class ColorShader extends MVPShader {
		public ColorShader() {
			super();
			try {
				s = new Shader("/shaders/color.vs", "/shaders/color.fs");
			} catch (IOException e) {
				System.err.println("Couldn't load color shader!");
				e.printStackTrace();
			}
		}

		public ColorShader reset() {
			super.reset();
			return alpha(1);
		}

		// Chainable parent interaction
		public ColorShader use() { return (ColorShader)super.use(); }
		public ColorShader at(double x, double y) { return (ColorShader)super.at(x, y); }
		public ColorShader layer(double l) { return (ColorShader)super.layer(l); }
		public ColorShader rotate(double rotation) { return (ColorShader)super.rotate(rotation); }
		public ColorShader scale(double w, double h) { return (ColorShader)super.scale(w, h); }
		public ColorShader scale(double scale) { return (ColorShader)super.scale(scale); }

		public ColorShader color(double r, double g, double b) {
			s.use();
			s.setVec3("color", r, g, b);
			return this;
		}

		public ColorShader alpha(double alpha) {
			s.use();
			s.setFloat("alpha", (float)alpha);
			return this;
		}

	}
}
