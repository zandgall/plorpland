#version 330 core

layout (location = 0) in vec4 in_position;
layout (location = 1) in vect2 in_uv;

out vec2 uv;

uniform mat4 projection, view, model;

void main() {
	gl_Position = projection * view * model * in_position;
	uv = in_uv;
}
