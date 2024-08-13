package com.zandgall.plorpland.graphics;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.stb.STBImage;

public class Image {
	private int texture;

	public Image(String path) {
		ByteArrayInputStream fileByteInput = (ByteArrayInputStream)Image.class.getResourceAsStream(path);
		byte[] fileBytes = fileByteInput.readAllBytes();
		ByteBuffer fileBuffer = BufferUtils.createByteBuffer(fileBytes.length);
		fileBuffer.put(fileBytes);
		fileBuffer.flip();
		fileBuffer.rewind();
		int[] x = {0}, y = {0}, c = {0};
		ByteBuffer imageBuffer = STBImage.stbi_load_from_memory(fileBuffer, x, y, c, STBImage.STBI_rgb_alpha);
		
		texture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texture);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, x[0], y[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glGenerateMipmap(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, texture);
	}
}
