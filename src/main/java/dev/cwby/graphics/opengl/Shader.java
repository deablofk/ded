package dev.cwby.graphics.opengl;

import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final int programId;
    private final int vertexShaderId;
    private final int fragmentShaderId;

    public Shader(String vertexSource, String fragmentSource) {
        programId = glCreateProgram();
        if (programId == 0) {
            throw new RuntimeException("Could not create shader program");
        }

        vertexShaderId = createShader(vertexSource, GL_VERTEX_SHADER);
        fragmentShaderId = createShader(fragmentSource, GL_FRAGMENT_SHADER);

        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Error linking shader: " + glGetProgramInfoLog(programId, 1024));
        }

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating shader: " + glGetProgramInfoLog(programId, 1024));
        }
    }

    private int createShader(String source, int shaderType) {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new RuntimeException("Error creating shader. Type: " + shaderType);
        }

        glShaderSource(shaderId, source);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Error compiling shader: " + glGetShaderInfoLog(shaderId, 1024));
        }

        return shaderId;
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void setUniform(String name, int value) {
        int location = glGetUniformLocation(programId, name);
        if (location != -1) {
            glUniform1i(location, value);
        }
    }

    public void setUniform(String name, float value) {
        int location = glGetUniformLocation(programId, name);
        if (location != -1) {
            glUniform1f(location, value);
        }
    }

    public void setUniform(String name, float x, float y) {
        int location = glGetUniformLocation(programId, name);
        if (location != -1) {
            glUniform2f(location, x, y);
        }
    }

    public void setUniform(String name, float x, float y, float z) {
        int location = glGetUniformLocation(programId, name);
        if (location != -1) {
            glUniform3f(location, x, y, z);
        }
    }

    public void setUniform(String name, float x, float y, float z, float w) {
        int location = glGetUniformLocation(programId, name);
        if (location != -1) {
            glUniform4f(location, x, y, z, w);
        }
    }

    public void setUniformMatrix4(String name, float[] matrix) {
        int location = glGetUniformLocation(programId, name);
        if (location != -1) {
            glUniformMatrix4fv(location, false, matrix);
        }
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDetachShader(programId, vertexShaderId);
            glDetachShader(programId, fragmentShaderId);
            glDeleteShader(vertexShaderId);
            glDeleteShader(fragmentShaderId);
            glDeleteProgram(programId);
        }
    }

    public int getProgramId() {
        return programId;
    }
}