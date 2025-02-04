/* zandgall

 ## Camera
 # A class that stores and applies graphics transformation data to replicate a moving camera

 : MADE IN NEOVIM */

package com.zandgall.plorpland;

import org.joml.Matrix4f;

public class Camera {
	public static final double DEFAULT_SMOOTHING = 0.01, DEFAULT_ZOOM = 0.04;
	private double x, y;
	private double zoom; // The size a single pixel will be after transform

	private double targetX, targetY, targetZoom, smoothing;

	public Camera() {
		this.x = 0;
		this.y = 0;
		this.zoom = DEFAULT_ZOOM;
		this.smoothing = DEFAULT_SMOOTHING;
	}

	public void tick() {
		// Smooth x, y, and zoom to their target values
		this.x = x * (1 - smoothing) + targetX * smoothing;
		this.y = y * (1 - smoothing) + targetY * smoothing;
		this.zoom = zoom * (1 - smoothing) + targetZoom * smoothing;
	}

	public void setSmoothing(double smoothing) {
		this.smoothing = smoothing;
	}

	// Update targetX Y and zoom
	public void target(double x, double y, double zoom) {
		if(Double.isFinite(x))
			targetX = x;
		if(Double.isFinite(y))
			targetY = y;
		if(Double.isFinite(zoom))
			targetZoom = zoom;
	}

	// Update just target x and y
	public void target(double x, double y) {
		target(x, y, DEFAULT_ZOOM);
	}

	public void snapTo(double x, double y, double zoom) {
		this.x = x;
		this.y = y;
		this.zoom = zoom;
	}

	// Create the transformation with the given x, y and zoom
	public Matrix4f getTransform() {
		return new Matrix4f().scale((float)(zoom * Main.WIDTH), (float)zoom*Main.WIDTH, 1).translate((float)-x, (float)-y, 0);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZoom() {
		return zoom;
	}
}
