package com.zandgall.plorpland.graphics;

import com.zandgall.plorpland.Main;

import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL30.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

public class Shader {
	
	private int v, f, program;
	
	public static HashMap<Integer, String> ProgramNames = new HashMap<>();
	private static HashMap<Integer, ShaderState> InitialStates = new HashMap<>();
	private static HashMap<Integer, ArrayList<ShaderState>> States = new HashMap<>();
	private static HashMap<Integer, ShaderState> CurrentStates = new HashMap<>();

	public static Shader Image, Color, TintedImage, Circle;
	
	public static ShaderProperties Properties = new ShaderProperties();

	public static int ActiveProgram = 0;

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

		States.put(program, new ArrayList<>());
		int nUniforms[] = new int[1];
		glGetProgramiv(program, GL_ACTIVE_UNIFORMS, nUniforms);

		ShaderState state = new ShaderState();
		for(int i = 0; i < nUniforms[0]; i++) {
			IntBuffer type = BufferUtils.createIntBuffer(1), size = BufferUtils.createIntBuffer(1);
			String name = glGetActiveUniform(program, i, size, type);
			switch(type.get()) {
				case GL_FLOAT_MAT4 -> {	
					FloatBuffer f = BufferUtils.createFloatBuffer(16);
					glGetUniformfv(program, i, f);
					state.matrix4s.put(name, new Matrix4f(f)); 
				}
				case GL_FLOAT_VEC4 -> {
					float f[] = new float[4];
					glGetUniformfv(program, i, f);
					state.vector4s.put(name, new Vector4f(f));
				}
				case GL_FLOAT_VEC3 -> {
					float f[] = new float[3];
					glGetUniformfv(program, i, f);
					state.vector3s.put(name, new Vector3f(f));
				}
				case GL_FLOAT -> {
					state.floats.put(name, glGetUniformf(program, i));
				}
				case GL_INT -> {
					state.ints.put(name, glGetUniformi(program, i));
				}
			}
		}
		States.get(program).add(state);
		InitialStates.put(program, state);
		CurrentStates.put(program, state);
	}

	public ShaderProperties use() {
		glUseProgram(program);
		ActiveProgram = program;
		return Properties;
	}

	public static void push() {
		States.get(ActiveProgram).add(new ShaderState(CurrentStates.get(ActiveProgram)));
	}

	public static void pop() {
		if(States.get(ActiveProgram).isEmpty())
			CurrentStates.put(ActiveProgram, InitialStates.get(ActiveProgram));
		else
			CurrentStates.put(ActiveProgram, States.get(ActiveProgram).removeLast());
		ShaderState s = CurrentStates.get(ActiveProgram);
		for(Entry<String, Matrix4f> a : s.matrix4s.entrySet()) {
			try(MemoryStack stack = MemoryStack.stackPush()) {
				FloatBuffer buffer = a.getValue().get(stack.mallocFloat(16));
				glUniformMatrix4fv(glGetUniformLocation(ActiveProgram, a.getKey()), false, buffer);
			}
		}
		for(Entry<String, Vector4f> a : s.vector4s.entrySet())
			glUniform4f(glGetUniformLocation(ActiveProgram, a.getKey()), a.getValue().x, a.getValue().y, a.getValue().z, a.getValue().w);
		for(Entry<String, Vector3f> a : s.vector3s.entrySet())
			glUniform3f(glGetUniformLocation(ActiveProgram, a.getKey()), a.getValue().x, a.getValue().y, a.getValue().z);
		for(Entry<String, Float> a : s.floats.entrySet())
			glUniform1f(glGetUniformLocation(ActiveProgram, a.getKey()), a.getValue());
		for(Entry<String, Integer> a : s.ints.entrySet())
			glUniform1i(glGetUniformLocation(ActiveProgram, a.getKey()), a.getValue());
	}

	public void setAsInitialState() {
		InitialStates.put(program, new ShaderState(CurrentStates.get(program)));
	}

	public static void init() {
		try {
			Image = new Shader("/shaders/mvp.vs", "/shaders/image.fs");
			ProgramNames.put(Image.program, "Image");
			Image.use().drawToWorld().setModel(new Matrix4f().identity());
			Image.setAsInitialState();
		} catch (IOException e) {
			System.err.println("Could not initialize image shader");
			e.printStackTrace();
		}
		try {
			Color = new Shader("/shaders/mvp.vs", "/shaders/color.fs");
			ProgramNames.put(Color.program, "Color");
			Color.use().drawToWorld().setModel(new Matrix4f().identity());
			Color.setAsInitialState();
		} catch (IOException e) {
			System.err.println("Could not initialize color shader");
			e.printStackTrace();
		}
		try {
			TintedImage = new Shader("/shaders/mvp.vs", "/shaders/tintedimage.fs");
			ProgramNames.put(TintedImage.program, "TintedImage");
			TintedImage.use().drawToWorld().setModel(new Matrix4f().identity());
			TintedImage.setAsInitialState();
		} catch (IOException e) {
			System.err.println("Could not initialize tinted image shader");
			e.printStackTrace();
		}
		try {
			Circle = new Shader("/shaders/mvp.vs", "/shaders/circle.fs");
			ProgramNames.put(Circle.program, "Circle");
			Circle.use().drawToWorld().setModel(new Matrix4f().identity());
			Circle.setAsInitialState();
		} catch (IOException e) {
			System.err.println("Could not initialize circle shader");
			e.printStackTrace();
		}
	}

	public static class ShaderState {
		public HashMap<String, Matrix4f> matrix4s = new HashMap<>();
		public HashMap<String, Vector4f> vector4s = new HashMap<>();
		public HashMap<String, Vector3f> vector3s = new HashMap<>();
		public HashMap<String, Integer> ints = new HashMap<>();
		public HashMap<String, Float> floats = new HashMap<>();

		public ShaderState() {}

		public ShaderState(ShaderState source) {
			for(String a : source.matrix4s.keySet())
				matrix4s.put(a, new Matrix4f(source.matrix4s.get(a)));
			for(String a : source.vector4s.keySet())
				vector4s.put(a, new Vector4f(source.vector4s.get(a)));
			for(String a : source.vector3s.keySet())
				vector3s.put(a, new Vector3f(source.vector3s.get(a)));
			for(String a : source.floats.keySet())
				floats.put(a, source.floats.get(a));
			for(String a : source.ints.keySet())
				ints.put(a, source.ints.get(a));
		}
	}

		// A class that provides interactions with every provided shader property
	public static class ShaderProperties {

		public ShaderProperties() {}

		private int uniformLocation(String name) {
			int out = glGetUniformLocation(Shader.ActiveProgram, name);
			if(out == -1) {
				System.err.printf("Could not find \"%s\" uniform in \"%s\" shader%n", name, Shader.ProgramNames.get(Shader.ActiveProgram));
				throw new RuntimeException("Invalid Uniform");
			}
			return out;
		}

		/**
		 *
		 * Push and Pop with ShaderProperties chaining (calls Shader.push/pop)
		 *
		 */
		public ShaderProperties push() {
			Shader.push();
			return this;
		}

		public ShaderProperties pop() {
			Shader.pop();
			return this;
		}

		/**
		*
		* Generic uniform setters
		*
		*/

		public ShaderProperties setMatrix(String name, Matrix4f matrix) {
			try(MemoryStack stack = MemoryStack.stackPush()) {
				FloatBuffer buffer = matrix.get(stack.mallocFloat(16));
				glUniformMatrix4fv(uniformLocation(name), false, buffer);
				CurrentStates.get(Shader.ActiveProgram).matrix4s.put(name, new Matrix4f(matrix));
			}
			return this;
		}

		public ShaderProperties setMatrix(String name, Matrix4d matrix) {
			try(MemoryStack stack = MemoryStack.stackPush()) {
				FloatBuffer buffer = matrix.get(stack.mallocFloat(16));
				glUniformMatrix4fv(uniformLocation(name), false, buffer);
				CurrentStates.get(Shader.ActiveProgram).matrix4s.put(name, new Matrix4f(matrix));
			}
			return this;
		}

		public ShaderProperties setFloat(String name, float f) {
			glUniform1f(uniformLocation(name), f);
			CurrentStates.get(Shader.ActiveProgram).floats.put(name, f);
			return this;
		}

		public ShaderProperties setVec4(String name, float x, float y, float z, float w) {
			glUniform4f(uniformLocation(name), x, y, z, w);
			CurrentStates.get(Shader.ActiveProgram).vector4s.put(name, new Vector4f(x, y, z, w));
			return this;
		}

		public ShaderProperties setVec4(String name, double x, double y, double z, double w) {return setVec4(name, (float)x, (float)y, (float)z, (float)w);}

		public ShaderProperties setVec4(String name, Vector4f vec) {return setVec4(name, vec.x, vec.y, vec.z, vec.w);}

		public ShaderProperties setVec3(String name, float x, float y, float z) {
			glUniform3f(uniformLocation(name), x, y, z);
			CurrentStates.get(ActiveProgram).vector3s.put(name, new Vector3f(x, y, z));
			return this;
		}

		public ShaderProperties setVec3(String name, double x, double y, double z) {
			return setVec3(name, (float)x, (float)y, (float)z);
		}

		public ShaderProperties setVec3(String name, Vector3f vec) {return setVec3(name, vec.x, vec.y, vec.z);}

		public ShaderProperties setInt(String name, int i) {
			glUniform1i(uniformLocation(name), i);
			CurrentStates.get(ActiveProgram).ints.put(name, i);
			return this;
		}

		/**
		*
		* Specific uniforms, autofill and shorten code with these
		*
		*/
		
		public ShaderProperties setProjection(Matrix4f matrix) { return setMatrix("projection", matrix); }

		public ShaderProperties setView(Matrix4f matrix) { return setMatrix("view", matrix); }

		public ShaderProperties setModel(Matrix4f matrix) { return setMatrix("model", matrix); }
		public ShaderProperties setModel(Matrix4d matrix) { return setMatrix("model", matrix); }

		private Matrix4f getModelf() {
			return new Matrix4f(CurrentStates.get(ActiveProgram).matrix4s.get("model"));
		}

		private Matrix4d getModeld() {
			return new Matrix4d(CurrentStates.get(ActiveProgram).matrix4s.get("model"));
		}

		public ShaderProperties setModel(double x, double y, double w, double h) {
			return setModel(getModeld().translate((float)(x + w * 0.5f), (float)(y + h * 0.5f), 0).scale((float)w*0.5f, (float)h*0.5f, 1));
		}

		/**
		 *
		 * Specific screenspaces
		 *
		 */

		public ShaderProperties drawToWorld() {
			setProjection(new Matrix4f().ortho(-0.5f*Main.WIDTH, 0.5f*Main.WIDTH, 0.5f*Main.HEIGHT, -0.5f*Main.HEIGHT, -1f, 1f));
			setView(Main.getCamera().getTransform());
			return this;
		}

		public ShaderProperties drawToScreen() {
			setProjection(new Matrix4f().ortho(0, Main.WIDTH, Main.HEIGHT, 0, -1f, 1f));
			setView(new Matrix4f());
			return this;
		}

		/**
		*
		* Model handling
		*
		*/	

		public ShaderProperties move(double x, double y) {
			setModel(getModeld().translate(x, y, 0));
			return this;
		}

		public ShaderProperties rotate(double rotation) {
			setModel(getModeld().rotateZ(rotation));
			return this;
		}

		public ShaderProperties scale(double w, double h) {
			setModel(getModeld().scale(w, h, 1));
			return this;
		}

		public ShaderProperties scale(double scale) { return scale(scale, scale); }

		/**
		*
		* Shader specific properties
		* TODO: Add checks for values (probably in each 'uniformLocation' call
		*
		*/

		public ShaderProperties alpha(double alpha) {
			return setFloat("alpha", (float)alpha);
		}

		public ShaderProperties texture(int texture) {
			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, texture);
			return this;
		}

		public ShaderProperties image(Image i) { return texture(i.getTexture()); }

		public ShaderProperties crop(double x, double y, double w, double h) { return setVec4("crop", x, y, w, h); }

		public ShaderProperties color(double r, double g, double b) { return setVec3("color", r, g, b); }
		
		public ShaderProperties tint(double r, double g, double b) { return setVec3("tint", r, g, b); }

		public ShaderProperties angleLength(double a) { return setFloat("angle_length", (float)Math.min(Math.max(a, 0.0), 2 * Math.PI)); }

		public ShaderProperties radius(double a) { return setFloat("radius", (float)a); }

		public ShaderProperties innerRadius(double a) { return setFloat("inner_radius", (float)a); }
	}
}
