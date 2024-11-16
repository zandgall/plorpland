package com.zandgall.plorpland.graphics;

import static org.lwjgl.opengl.GL30.*;

public class RenderCall {
	private Shader shader;
	private Shader.ShaderState state;
	private VAO vao;

	public RenderCall(Shader shader, Shader.ShaderState state, VAO vao) {
		this.shader = shader;
		this.state = state;
		this.vao = vao;
	}

	public void call() {
		shader.use().push();
		Shader.set(state);
		glBindVertexArray(vao.vao);
		glDrawElements(vao.mode, vao.count, vao.type, vao.indices);
		shader.use().pop();
	}
	
	public static class VAO {
		private int vao, mode, count, type, indices;
		public VAO(int vao, int mode, int count, int type, int indices) {
			this.vao = vao;
			this.mode = mode;
			this.count = count;
			this.type = type;
			this.indices = indices;
		}
	}
}
