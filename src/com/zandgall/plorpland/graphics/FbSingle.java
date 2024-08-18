package com.zandgall.plorpland.graphics;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;

public class FbSingle {
	public int framebuffer = 0, texture = 0, renderbuffer = 0;

	public FbSingle() {
		framebuffer = glGenFramebuffers();
		renderbuffer = glGenRenderbuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
		glBindRenderbuffer(GL_RENDERBUFFER, renderbuffer);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, renderbuffer);
/* 		int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
		if(status != GL_FRAMEBUFFER_COMPLETE)
			new RuntimeException("Framebuffer broken on init: " + status).printStackTrace(); */
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glBindRenderbuffer(GL_RENDERBUFFER, 0);
	}

	public void setTexture(int texture) {
		glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
		this.texture = texture;
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
		glBindRenderbuffer(GL_RENDERBUFFER, renderbuffer);
		glBindTexture(GL_TEXTURE_2D, texture);
		int w = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
		int h = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, w, h); 
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			new RuntimeException("Framebuffer broken on setting texture ").printStackTrace();
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindRenderbuffer(GL_RENDERBUFFER, 0);
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public void newTexture(int width, int height, int wrapping, int filtering, int format) {
		glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
		texture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texture);
		glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, NULL);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapping);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapping);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filtering);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filtering);
		setTexture(texture);
	}

	public void newTexture(int width, int height, int wrapping, int filtering) {
		newTexture(width, height, wrapping, filtering, GL_RGBA);
	}

	public void newTexture(int width, int height, int wrapping) {
		newTexture(width, height, wrapping, GL_NEAREST, GL_RGBA);
	}

	public void newTexture(int width, int height) {
		newTexture(width, height, GL_CLAMP_TO_EDGE, GL_NEAREST, GL_RGBA);
	}

	public int getTexture() {
		return texture;
	}

	public void deleteTexture() {
		glDeleteTextures(texture);
		this.texture = 0;
	}

	public void drawToThis() {
		glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
		glDrawBuffer(GL_COLOR_ATTACHMENT0);
	}

	public static void drawToScreen() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

}
