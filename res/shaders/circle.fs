#version 330 core
#define PI 3.14159265359

in vec2 uv;

uniform vec3 color = vec3(1, 1, 1);
uniform float alpha = 1;
uniform float radius = 1, inner_radius = 0, angle_length = 2*PI;

out vec4 out_color;

void main() {
	vec2 nuv = 2 * uv - vec2(1);
	float r = nuv.x * nuv.x + nuv.y * nuv.y;
	if(r > radius || r < inner_radius)
		discard;
	if(mod(atan(nuv.y, nuv.x) + PI, 2*PI) >= angle_length)
		discard;
	out_color = vec4(color, alpha);
}
