#version 330 core

in vec2 uv;

uniform sampler2D text;

uniform vec4 crop;
uniform float alpha = 1;

out vec4 out_color;

void main() {
	out_color = texture2D(text, uv * crop.zw + crop.xy);
	out_color.w *= alpha;
}
