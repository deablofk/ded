package dev.cwby.graphics.opengl;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.Stack;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer2D {
    private static final int MAX_BATCH_SIZE = 10000;
    private static final int VERTEX_SIZE = 9; // x, y, z, r, g, b, a, u, v
    
    private final Shader rectShader;
    private final Shader textShader;
    private int vao;
    private int vbo;
    private FloatBuffer vertexBuffer;
    private int vertexCount;
    
    private float[] projectionMatrix;
    private Stack<ClipRect> clipStack;
    
    private static class ClipRect {
        float x, y, width, height;
        
        public ClipRect(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public Renderer2D(int screenWidth, int screenHeight) {
        // Create shaders
        String rectVertexShader = 
            "#version 330 core\n" +
            "layout (location = 0) in vec3 aPos;\n" +
            "layout (location = 1) in vec4 aColor;\n" +
            "layout (location = 2) in vec2 aTexCoord;\n" +
            "out vec4 vertexColor;\n" +
            "out vec2 texCoord;\n" +
            "uniform mat4 projection;\n" +
            "void main() {\n" +
            "    gl_Position = projection * vec4(aPos, 1.0);\n" +
            "    vertexColor = aColor;\n" +
            "    texCoord = aTexCoord;\n" +
            "}\n";

        String rectFragmentShader = 
            "#version 330 core\n" +
            "in vec4 vertexColor;\n" +
            "in vec2 texCoord;\n" +
            "out vec4 FragColor;\n" +
            "void main() {\n" +
            "    FragColor = vertexColor;\n" +
            "}\n";

        String textFragmentShader = 
            "#version 330 core\n" +
            "in vec4 vertexColor;\n" +
            "in vec2 texCoord;\n" +
            "out vec4 FragColor;\n" +
            "uniform sampler2D textTexture;\n" +
            "void main() {\n" +
            "    float alpha = texture(textTexture, texCoord).r;\n" +
            "    FragColor = vec4(vertexColor.rgb, vertexColor.a * alpha);\n" +
            "}\n";

        rectShader = new Shader(rectVertexShader, rectFragmentShader);
        textShader = new Shader(rectVertexShader, textFragmentShader);

        // Create VAO and VBO
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) MAX_BATCH_SIZE * VERTEX_SIZE * Float.BYTES, GL_DYNAMIC_DRAW);

        // Position attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, VERTEX_SIZE * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Color attribute
        glVertexAttribPointer(1, 4, GL_FLOAT, false, VERTEX_SIZE * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // Texture coordinate attribute
        glVertexAttribPointer(2, 2, GL_FLOAT, false, VERTEX_SIZE * Float.BYTES, 7 * Float.BYTES);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        vertexBuffer = BufferUtils.createFloatBuffer(MAX_BATCH_SIZE * VERTEX_SIZE);
        vertexCount = 0;
        clipStack = new Stack<>();
        
        updateProjection(screenWidth, screenHeight);
        
        // Enable blending for transparency
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void updateProjection(int width, int height) {
        // Update OpenGL viewport
        glViewport(0, 0, width, height);
        
        // Orthographic projection matrix (top-left origin)
        projectionMatrix = new float[16];
        projectionMatrix[0] = 2.0f / width;
        projectionMatrix[5] = -2.0f / height;
        projectionMatrix[10] = -1.0f;
        projectionMatrix[12] = -1.0f;
        projectionMatrix[13] = 1.0f;
        projectionMatrix[15] = 1.0f;
    }

    public void begin() {
        vertexCount = 0;
        vertexBuffer.clear();
    }

    public void end() {
        flush();
    }

    public void flush() {
        if (vertexCount == 0) return;

        vertexBuffer.flip();
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
        
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        
        vertexCount = 0;
        vertexBuffer.clear();
    }

    private void checkFlush() {
        if (vertexCount >= MAX_BATCH_SIZE - 6) {
            flush();
        }
    }

    public void drawRect(float x, float y, float width, float height, int color) {
        drawRect(x, y, width, height, color, false);
    }

    public void drawRect(float x, float y, float width, float height, int color, boolean outline) {
        if (outline) {
            float lineWidth = 5.0f;
            // Top
            drawFilledRect(x, y, width, lineWidth, color);
            // Bottom
            drawFilledRect(x, y + height - lineWidth, width, lineWidth, color);
            // Left
            drawFilledRect(x, y, lineWidth, height, color);
            // Right
            drawFilledRect(x + width - lineWidth, y, lineWidth, height, color);
        } else {
            drawFilledRect(x, y, width, height, color);
        }
    }

    private void drawFilledRect(float x, float y, float width, float height, int color) {
        checkFlush();
        
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;

        // Triangle 1
        addVertex(x, y, 0, r, g, b, a, 0, 0);
        addVertex(x + width, y, 0, r, g, b, a, 1, 0);
        addVertex(x, y + height, 0, r, g, b, a, 0, 1);

        // Triangle 2
        addVertex(x + width, y, 0, r, g, b, a, 1, 0);
        addVertex(x + width, y + height, 0, r, g, b, a, 1, 1);
        addVertex(x, y + height, 0, r, g, b, a, 0, 1);

        vertexCount += 6;
    }

    public void drawText(String text, float x, float y, GLFont font, int color) {
        if (text == null || text.isEmpty()) return;
        
        flush();
        textShader.bind();
        textShader.setUniformMatrix4("projection", projectionMatrix);
        textShader.setUniform("textTexture", 0);
        
        font.getTexture().bind();
        
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;

        float offsetX = x;
        float offsetY = y;

        for (int i = 0; i < text.length(); ) {
            int codepoint = text.codePointAt(i);
            GLFont.CharInfo charInfo = font.getCharInfo(codepoint);

            if (codepoint != ' ' && codepoint != '\t') {
                float x0 = offsetX + charInfo.x0;
                float y0 = offsetY + charInfo.y0;
                float x1 = offsetX + charInfo.x1;
                float y1 = offsetY + charInfo.y1;

                checkFlush();

                // Triangle 1
                addVertex(x0, y0, 0, r, g, b, a, charInfo.s0, charInfo.t0);
                addVertex(x1, y0, 0, r, g, b, a, charInfo.s1, charInfo.t0);
                addVertex(x0, y1, 0, r, g, b, a, charInfo.s0, charInfo.t1);

                // Triangle 2
                addVertex(x1, y0, 0, r, g, b, a, charInfo.s1, charInfo.t0);
                addVertex(x1, y1, 0, r, g, b, a, charInfo.s1, charInfo.t1);
                addVertex(x0, y1, 0, r, g, b, a, charInfo.s0, charInfo.t1);

                vertexCount += 6;
            }

            offsetX += charInfo.advance;
            i += Character.charCount(codepoint);
        }

        flush();
        font.getTexture().unbind();
        textShader.unbind();
        
        rectShader.bind();
        rectShader.setUniformMatrix4("projection", projectionMatrix);
    }

    private void addVertex(float x, float y, float z, float r, float g, float b, float a, float u, float v) {
        vertexBuffer.put(x).put(y).put(z);
        vertexBuffer.put(r).put(g).put(b).put(a);
        vertexBuffer.put(u).put(v);
    }

    public void pushClip(float x, float y, float width, float height) {
        flush();
        clipStack.push(new ClipRect(x, y, width, height));
        applyClip();
    }

    public void popClip() {
        flush();
        if (!clipStack.isEmpty()) {
            clipStack.pop();
        }
        if (clipStack.isEmpty()) {
            glDisable(GL_SCISSOR_TEST);
        } else {
            applyClip();
        }
    }

    private void applyClip() {
        if (!clipStack.isEmpty()) {
            ClipRect clip = clipStack.peek();
            glEnable(GL_SCISSOR_TEST);
            // Convert from top-left origin to bottom-left origin for OpenGL
            int screenHeight = (int)(2.0f / -projectionMatrix[5]);
            glScissor((int)clip.x, screenHeight - (int)(clip.y + clip.height), (int)clip.width, (int)clip.height);
        }
    }

    public void clear(int color) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;
        glClearColor(r, g, b, a);
        glClear(GL_COLOR_BUFFER_BIT);
    }

    public void startFrame() {
        rectShader.bind();
        rectShader.setUniformMatrix4("projection", projectionMatrix);
        begin();
    }

    public void endFrame() {
        end();
        rectShader.unbind();
    }

    public void cleanup() {
        rectShader.cleanup();
        textShader.cleanup();
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
    }
}