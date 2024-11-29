package com.zandgall.plorpland.staging;

import java.io.Serializable;

import com.zandgall.plorpland.util.Rect;

public class Exit implements Serializable {
	public Rect detection, placer;
	private String level_path;
	public Exit(String to, Rect detection, Rect placer) {
		this.level_path = to;
		this.detection = detection;
		this.placer = placer;
	}
}
