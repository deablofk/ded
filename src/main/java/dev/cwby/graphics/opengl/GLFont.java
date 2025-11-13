package dev.cwby.graphics.opengl;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

public class GLFont {
    private final int bitmapWidth;
    private final int bitmapHeight;
    
    private final STBTTFontinfo fontInfo;
    private final ByteBuffer fontBuffer;
    private final Texture texture;
    private final STBTTPackedchar.Buffer charData;
    private final float fontSize;
    private final float ascent;
    private final float descent;
    private final float lineGap;
    private final Map<Integer, CharInfo> charInfoCache;

    public static class CharInfo {
        public float advance;
        public float width;
        public float height;
        public float xoffset;
        public float yoffset;
        public float x0, y0, x1, y1;
        public float s0, t0, s1, t1;
    }

    public GLFont(String fontPath, float fontSize) throws IOException {
        this.fontSize = fontSize;
        this.charInfoCache = new HashMap<>();
        
        // Calculate bitmap size based on font size
        // Larger fonts need more space in the atlas
        if (fontSize <= 25) {
            bitmapWidth = bitmapHeight = 512;
        } else if (fontSize <= 50) {
            bitmapWidth = bitmapHeight = 1024;
        } else if (fontSize <= 100) {
            bitmapWidth = bitmapHeight = 2048;
        } else {
            bitmapWidth = bitmapHeight = 4096;
        }
        
        // Load font file
        byte[] fontBytes = Files.readAllBytes(Paths.get(fontPath));
        fontBuffer = BufferUtils.createByteBuffer(fontBytes.length);
        fontBuffer.put(fontBytes);
        fontBuffer.flip();

        // Initialize font info
        fontInfo = STBTTFontinfo.create();
        if (!stbtt_InitFont(fontInfo, fontBuffer)) {
            throw new RuntimeException("Failed to initialize font");
        }

        // Get font metrics
        try (MemoryStack stack = stackPush()) {
            IntBuffer pAscent = stack.mallocInt(1);
            IntBuffer pDescent = stack.mallocInt(1);
            IntBuffer pLineGap = stack.mallocInt(1);
            
            stbtt_GetFontVMetrics(fontInfo, pAscent, pDescent, pLineGap);
            
            float scale = stbtt_ScaleForPixelHeight(fontInfo, fontSize);
            this.ascent = pAscent.get(0) * scale;
            this.descent = pDescent.get(0) * scale;
            this.lineGap = pLineGap.get(0) * scale;
        }

        // Create bitmap and pack characters
        ByteBuffer bitmap = BufferUtils.createByteBuffer(bitmapWidth * bitmapHeight);
        charData = STBTTPackedchar.malloc(96); // ASCII printable characters

        STBTTPackContext pc = STBTTPackContext.malloc();
        stbtt_PackBegin(pc, bitmap, bitmapWidth, bitmapHeight, 0, 1, NULL);
        stbtt_PackSetOversampling(pc, 2, 2);
        stbtt_PackFontRange(pc, fontBuffer, 0, fontSize, 32, charData);
        stbtt_PackEnd(pc);
        pc.free();

        texture = new Texture(bitmapWidth, bitmapHeight, bitmap);
    }

    public CharInfo getCharInfo(int codepoint) {
        if (charInfoCache.containsKey(codepoint)) {
            return charInfoCache.get(codepoint);
        }

        CharInfo info = new CharInfo();
        
        if (codepoint >= 32 && codepoint < 128) {
            STBTTPackedchar chardata = charData.get(codepoint - 32);
            
            try (MemoryStack stack = stackPush()) {
                FloatBuffer xpos = stack.floats(0.0f);
                FloatBuffer ypos = stack.floats(0.0f);
                STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
                
                stbtt_GetPackedQuad(charData, bitmapWidth, bitmapHeight, codepoint - 32, xpos, ypos, quad, false);
                
                info.x0 = quad.x0();
                info.y0 = quad.y0();
                info.x1 = quad.x1();
                info.y1 = quad.y1();
                info.s0 = quad.s0();
                info.t0 = quad.t0();
                info.s1 = quad.s1();
                info.t1 = quad.t1();
                info.width = quad.x1() - quad.x0();
                info.height = quad.y1() - quad.y0();
                info.xoffset = chardata.xoff();
                info.yoffset = chardata.yoff();
                info.advance = chardata.xadvance();
            }
        } else {
            // Default for unsupported characters
            info.advance = fontSize * 0.5f;
            info.width = 0;
            info.height = 0;
        }
        
        charInfoCache.put(codepoint, info);
        return info;
    }

    public float measureText(String text) {
        float width = 0;
        for (int i = 0; i < text.length(); ) {
            int codepoint = text.codePointAt(i);
            CharInfo info = getCharInfo(codepoint);
            width += info.advance;
            i += Character.charCount(codepoint);
        }
        return width;
    }

    public Texture getTexture() {
        return texture;
    }

    public float getLineHeight() {
        return ascent - descent + lineGap;
    }

    public float getAscent() {
        return ascent;
    }

    public float getDescent() {
        return descent;
    }

    public float getFontSize() {
        return fontSize;
    }

    public void cleanup() {
        texture.cleanup();
        charData.free();
    }
}