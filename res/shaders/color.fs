#version 330 core

in vec2 uv;

uniform vec3 color;
uniform float alpha = 1;

out vec4 out_color;

void main() {
	out_color = vec4(color, alpha);
}
