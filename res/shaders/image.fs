#version 330 core

in vec2 uv;

uniform sampler2D text;

uniform vec4 crop = vec4(0, 0, 1, 1);
uniform float alpha = 1;

out vec4 out_color;

void main() {
	out_color = texture(text, uv * crop.zw + crop.xy);
	out_color.w *= alpha;
	if(out_color.w == 0)
		discard;
}
