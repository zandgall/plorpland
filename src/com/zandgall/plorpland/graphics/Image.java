package com.zandgall.plorpland.graphics;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

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

	public Image(int texture) {
		this.texture = texture;
		glBindTexture(GL_TEXTURE_2D, texture);
		this.width = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
		this.height = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
		glBindTexture(GL_TEXTURE_2D, 0);
		this.path = "";
		this.pong = true;
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

	public void draw(double cX, double cY, double cW, double cH, double x, double y, double w, double h, double depth) {
		ping();
		Shader.push();
		Shader.Properties
			.move(x+w*0.5, y+h*0.5)
			.scale(w*0.5, h*0.5)
			.texture(texture)
			.layer(depth)
			.crop(cX / width, cY / height, cW / width, cH / height);
		G.drawSquare();
		Shader.pop();
	}

	public void draw(double x, double y, double w, double h, double depth) {
		draw(0, 0, width, height, x, y, w, h, depth);
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
