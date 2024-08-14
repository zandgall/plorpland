#version 330 core

in vec2 uv;

uniform sampler2D texture;

uniform vec4 crop;

out vec4 out_color;

void main() {
	out_color = texture2D(texture, uv * crop.zw + crop.xy);
}
