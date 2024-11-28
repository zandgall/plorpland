package com.zandgall.plorpland.graphics;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageWrite;

public class Image {

	public static final Image BLANK = new Image(0);

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
		if(texture == 0) {
			this.width = 0;
			this.height = 0;
		} else {
			glBindTexture(GL_TEXTURE_2D, texture);
			this.width = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
			this.height = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
			glBindTexture(GL_TEXTURE_2D, 0);
		}
		this.path = "";
		this.pong = true;
	}

	private void ping() {
		if(pong)
			return;
		try {
			this.texture = textureFrom(Image.class.getResourceAsStream(path));
			glBindTexture(GL_TEXTURE_2D, texture);
			this.width = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
			this.height = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
			System.out.printf("Loaded texture \"%s\" %d%n", path, texture);
		} catch(IOException e) {
			System.err.println("Could not load image \""+path+"\"");
			e.printStackTrace();
		}
		pong = true;
	}

	public void writeTo(String filename) throws IOException {
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
		glBindTexture(GL_TEXTURE_2D, texture);
		glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		STBImageWrite.stbi_write_png(filename, width, height, 4, buffer, width * 4);
	}

	public static int textureFrom(InputStream in) throws IOException {
		BufferedInputStream fileByteInput = new BufferedInputStream(in);
		byte[] fileBytes = fileByteInput.readAllBytes();
		ByteBuffer fileBuffer = BufferUtils.createByteBuffer(fileBytes.length);
		fileBuffer.put(fileBytes);
		fileBuffer.flip();
		fileBuffer.rewind();
		int[] x = {0}, y = {0}, c = {0};
		ByteBuffer imageBuffer = STBImage.stbi_load_from_memory(fileBuffer, x, y, c, STBImage.STBI_rgb_alpha);
		
		int texture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texture);
		// imageBuffer might be null, but it's acceptable to pass null to glTexImage2D
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, x[0], y[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glGenerateMipmap(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, texture);
		if(imageBuffer != null)	
			STBImage.stbi_image_free(imageBuffer);
		return texture;
	}

	public void draw(double cX, double cY, double cW, double cH, double x, double y, double w, double h) {
		ping();
		Shader.push();
		Shader.Properties
			.move(x+w*0.5, y+h*0.5)
			.scale(w*0.5, h*0.5)
			.texture(texture)	
			.crop(cX / width, cY / height, cW / width, cH / height);
		G.drawSquare();
		Shader.pop();
	}

	public void draw(double x, double y, double w, double h) {
		draw(0, 0, width, height, x, y, w, h);
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
