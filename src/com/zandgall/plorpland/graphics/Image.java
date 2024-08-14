package com.zandgall.plorpland.graphics;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.stb.STBImage;

public class Image {
	private int texture, width, height;

	public Image(String path) {
		ByteArrayInputStream fileByteInput = (ByteArrayInputStream)Image.class.getResourceAsStream(path);
		byte[] fileBytes = fileByteInput.readAllBytes();
		ByteBuffer fileBuffer = BufferUtils.createByteBuffer(fileBytes.length);
		fileBuffer.put(fileBytes);
		fileBuffer.flip();
		fileBuffer.rewind();
		int[] x = {0}, y = {0}, c = {0};
		ByteBuffer imageBuffer = STBImage.stbi_load_from_memory(fileBuffer, x, y, c, STBImage.STBI_rgb_alpha);
		width = x[0];
		height = y[0];
		
		texture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texture);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glGenerateMipmap(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, texture);
	}

	public void draw(float cX, float cY, float cW, float cH, float x, float y, float w, float h, float depth) {
		Shader.Image.use();
		Shader.Image.setCrop(cX/width, cY/height, cW/width, cH/height);
		Shader.Image.setModel(x, y, w, h, depth);
		Shader.Image.setTexture(texture);
		GLHelper.drawRect();
	}

	public void draw(float x, float y, float w, float h, float depth) {
		draw(0, 0, width, height, x, y, w, h, depth);
	}

	public void draw(double cX, double cY, double cW, double cH, double x, double y, double w, double h, double depth) {
		draw((float)cX, (float)cY, (float)cW, (float)cH, (float)x, (float)y, (float)w, (float)h, (float)depth);
	}

	public void draw(double x, double y, double w, double h, double depth) {
		draw(0.0, 0.0, width, height, x, y, w, h, depth);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getTexture() {
		return texture;
	}
}
