#version 400

uniform mat4 mvp;

in vec3 position;

void main(void) 
{
	gl_Position = mvp * vec4(position, 1.0f);
}
