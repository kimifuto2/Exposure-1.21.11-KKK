#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform ExposureColorProcessorConfig {
    vec3 Mul;
    vec3 Add;
    vec3 Params;
};

out vec4 fragColor;

void main() {
    vec2 oneTexel = 1.0 / InSize;
    vec4 InTexel = texture(InSampler, texCoord);

    vec3 RGB = InTexel.rgb * Mul + Add;

    float Saturation = Params.x;
    float Contrast = Params.y;

    vec3 Gray = vec3(0.3, 0.59, 0.11);
    float Luma = dot(RGB, Gray);
    vec3 Chroma = RGB - Luma;
    RGB = (Chroma * Saturation) + Luma;

    RGB = (RGB - 0.5) * Contrast + 0.5;

    fragColor = vec4(RGB, 1.0);
}
