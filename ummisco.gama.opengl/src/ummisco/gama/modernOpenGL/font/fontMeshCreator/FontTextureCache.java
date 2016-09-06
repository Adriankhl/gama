package ummisco.gama.modernOpenGL.font.fontMeshCreator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class FontTextureCache {
	
	private HashMap<String,Texture> textureMap = new HashMap<String,Texture>();
	
	public TextMeshData getTextMeshData(String textureName, String content, int yRatioBetweenPixelsAndModelUnits, int textSize) {
		String absolutePathToFontTextureFolder = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() 
				+ "res" + File.separator + "font" + File.separator;
		String fontFile = absolutePathToFontTextureFolder + textureName + ".fnt";
		FontType font = new FontType(fontFile);
		float scale = (10f / yRatioBetweenPixelsAndModelUnits) * textSize;
		GUIText text = new GUIText(content, scale, font, -1, true);
		return font.loadText(text);
	}

	public Texture getFontTexture(String textureName) {
		if (textureMap.containsKey(textureName)) {
			return textureMap.get(textureName);
		}
		else {
			String absolutePathToFontTextureFolder = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() 
					+ "res" + File.separator + "font" + File.separator;
			String fontImage = absolutePathToFontTextureFolder + textureName + ".png";
			Texture fontTexture = null;
			try {
				fontTexture = TextureIO.newTexture(new File(fontImage), false);
			} catch (GLException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			textureMap.put(textureName, fontTexture);
			return fontTexture;
		}
	}
}
