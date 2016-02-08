package grondag.adversity.niceblock.newmodel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public enum NiceColor {
	FLEXSTONE0(0xFFF5F5F1, 0xFFF5F5F1, 0xFFF5F5F1),
	FLEXSTONE1(0xFFD3CFCE, 0xFFD3CFCE, 0xFFD3CFCE),
	FLEXSTONE2(0xFFBBC1C8, 0xFFBBC1C8, 0xFFBBC1C8),
	FLEXSTONE3(0xFFA1A7A7, 0xFFA1A7A7, 0xFFA1A7A7),
    FLEXSTONE4(0xFF949397, 0xFF949397, 0xFF949397),
    FLEXSTONE5(0xFF80787C, 0xFF80787C, 0xFF80787C),
    FLEXSTONE6(0xFF515352, 0xFF515352, 0xFF515352),
    FLEXSTONE7(0xFF2F3034, 0xFF2F3034, 0xFF2F3034),
    FLEXSTONE8(0xFFD7BC99, 0xFFD7BC99, 0xFFD7BC99),
    FLEXSTONE9(0xFFC9B2AB, 0xFFC9B2AB, 0xFFC9B2AB),
    FLEXSTONE10(0xFFBB9F8D, 0xFFBB9F8D, 0xFFBB9F8D),
    FLEXSTONE11(0xFF938265, 0xFF938265, 0xFF938265),
	FLEXSTONE12(0xFF5E5440, 0xFF5E5440, 0xFF5E5440),
    FLEXSTONE13(0xFF5A524A, 0xFF5A524A, 0xFF5A524A),
	FLEXSTONE14(0xFF4F433C, 0xFF4F433C, 0xFF4F433C),
	FLEXSTONE15(0xFF2A2521, 0xFF2A2521, 0xFF2A2521),
    
	DURASTONE0(0xFF43A732, 0xFF43A732, 0xFF43A732),
	DURASTONE1(0xFF572938, 0xFF572938, 0xFF572938),
	DURASTONE2(0xFF967176, 0xFF967176, 0xFF967176),
	DURASTONE3(0xFFB48612, 0xFFB48612, 0xFFB48612),
	DURASTONE4(0xFFC64102, 0xFFC64102, 0xFFC64102),
	DURASTONE5(0xFF259FAB, 0xFF259FAB, 0xFF259FAB),
	DURASTONE6(0xFF681BAF, 0xFF681BAF, 0xFF681BAF),
	DURASTONE7(0xFFAB5917, 0xFFAB5917, 0xFFAB5917),
	DURASTONE8(0xFF591032, 0xFF591032, 0xFF591032),
	DURASTONE9(0xFF6371AB, 0xFF6371AB, 0xFF6371AB),
	DURASTONE10(0xFFBFA567, 0xFFBFA567, 0xFFBFA567),
	DURASTONE11(0xFF123456, 0xFF123456, 0xFF123456),
	DURASTONE12(0xFF654321, 0xFF654321, 0xFF654321),
	DURASTONE13(0xFF5B7A0F, 0xFF5B7A0F, 0xFF5B7A0F),
	DURASTONE14(0xFFFF00FF, 0xFFFF00FF, 0xFFFF00FF),
	DURASTONE15(0xFFFFFF00, 0xFFFFFF00, 0xFFFFFF00),
	
	HYPERSTONE0(0xFF00FFFF, 0xFF00FFFF, 0xFF00FFFF),
	HYPERSTONE1(0xFF00FF00, 0xFF00FF00, 0xFF00FF00),
	HYPERSTONE2(0xFF0000FF, 0xFF0000FF, 0xFF0000FF),
	HYPERSTONE3(0xFFFF0000, 0xFFFF0000, 0xFFFF0000),
	HYPERSTONE4(0xFF55FF44, 0xFF55FF44, 0xFF55FF44),
	HYPERSTONE5(0xFF3322FF, 0xFF3322FF, 0xFF3322FF),
	HYPERSTONE6(0xFFFF5555, 0xFFFF5555, 0xFFFF5555),
	HYPERSTONE7(0xFF5555FF, 0xFF5555FF, 0xFF5555FF),
	HYPERSTONE8(0xFF55FF55, 0xFF55FF55, 0xFF55FF55),
	HYPERSTONE9(0xFFFFFF55, 0xFFFFFF55, 0xFFFFFF55),
	HYPERSTONE10(0xFFFF55FF, 0xFFFF55FF, 0xFFFF55FF),
	HYPERSTONE11(0xFF55FFFF, 0xFF55FFFF, 0xFF55FFFF),
	HYPERSTONE12(0xFFAAAAAA, 0xFFAAAAAA, 0xFFAAAAAA),
	HYPERSTONE13(0xFF777777, 0xFF777777, 0xFF777777),
	HYPERSTONE14(0xFFAA4499, 0xFFAA4499, 0xFFAA4499),
	HYPERSTONE15(0xFF22AA57, 0xFF22AA57, 0xFF22AA57),
	
    SUPERWOOD0(0xFF00FFFF, 0xFF00FFFF, 0xFF00FFFF),
    SUPERWOOD1(0xFF00FF00, 0xFF00FF00, 0xFF00FF00),
    SUPERWOOD2(0xFF0000FF, 0xFF0000FF, 0xFF0000FF),
    SUPERWOOD3(0xFFFF0000, 0xFFFF0000, 0xFFFF0000),
    SUPERWOOD4(0xFF55FF44, 0xFF55FF44, 0xFF55FF44),
    SUPERWOOD5(0xFF3322FF, 0xFF3322FF, 0xFF3322FF),
    SUPERWOOD6(0xFFFF5555, 0xFFFF5555, 0xFFFF5555),
    SUPERWOOD7(0xFF5555FF, 0xFF5555FF, 0xFF5555FF),
    SUPERWOOD8(0xFF55FF55, 0xFF55FF55, 0xFF55FF55),
    SUPERWOOD9(0xFFFFFF55, 0xFFFFFF55, 0xFFFFFF55),
    SUPERWOOD10(0xFFFF55FF, 0xFFFF55FF, 0xFFFF55FF),
    SUPERWOOD11(0xFF55FFFF, 0xFF55FFFF, 0xFF55FFFF),
    SUPERWOOD12(0xFFAAAAAA, 0xFFAAAAAA, 0xFFAAAAAA),
    SUPERWOOD13(0xFF777777, 0xFF777777, 0xFF777777),
    SUPERWOOD14(0xFFAA4499, 0xFFAA4499, 0xFFAA4499),
    SUPERWOOD15(0xFF22AA57, 0xFF22AA57, 0xFF22AA57);
	
	public final int base;
	public final int highlight;
	public final int border;
	
	private TextureAtlasSprite particleTexture;
	
	private NiceColor(int base, int highlight, int border){
		this.base = base;
		this.highlight = highlight;
		this.border = border;
	}
	
	public String getParticleTextureName(){
	    return "adversity:blocks/raw_0_0";
	}
	
	public TextureAtlasSprite getParticleTexture()
	{
	    if(particleTexture == null)
	    {
	        particleTexture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(getParticleTextureName());
	    }
	    return particleTexture;
	}
}