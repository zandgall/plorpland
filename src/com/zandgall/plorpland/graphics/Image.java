package com.zandgall.plorpland.graphics;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.stb.STBImage;

public class Image {
	private int texture, width, height;
	private String path;
	private boolean pong = false;

	public Image(String path) {
		this.path = path;
		// if(GL.getCapabilities() == null)
			// System.err.println("Woo!");
	}

	private void ping() {
		if(pong)
			return;
		BufferedInputStream fileByteInput = new BufferedInputStream(Image.class.getResourceAsStream(path));
		try {
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
			STBImage.stbi_image_free(imageBuffer);
			System.out.printf("Loaded texture \"%s\" %d%n", path, texture);
		} catch(IOException e) {
			System.err.println("Could not load image \""+path+"\"");
			e.printStackTrace();
		}
		pong = true;
	}

	public void draw(float cX, float cY, float cW, float cH, float x, float y, float w, float h, float depth) {
		ping();
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
		ping();
		return width;
	}

	public int getHeight() {
		ping();
		return height;
	}

	public int getTexture() {
		ping();
		return texture;
	}
}
