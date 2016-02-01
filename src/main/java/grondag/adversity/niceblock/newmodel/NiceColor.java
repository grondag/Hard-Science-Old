package grondag.adversity.niceblock.newmodel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public enum NiceColor {
	STONE0(0xFFF9F8F7, 0xFFF9F8F7, 0xFFF9F8F7),
	STONE1(0xFFFCFCFC, 0xFFFCFCFC, 0xFFFCFCFC),
	STONE2(0xFFE5E0E1, 0xFFE5E0E1, 0xFFE5E0E1),
	STONE3(0xFFC1BDB5, 0xFFC1BDB5, 0xFFC1BDB5),
	STONE4(0xFFB0B2AE, 0xFFB0B2AE, 0xFFB0B2AE),
	STONE5(0xFFC5C8C9, 0xFFC5C8C9, 0xFFC5C8C9),
	STONE6(0xFFD0CFD1, 0xFFD0CFD1, 0xFFD0CFD1),
	STONE7(0xFFE8E7E1, 0xFFE8E7E1, 0xFFE8E7E1),
	STONE8(0xFFDCE2DE, 0xFFDCE2DE, 0xFFDCE2DE),
	STONE9(0xFFCCD0D1, 0xFFCCD0D1, 0xFFCCD0D1),
	STONE10(0xFFCFCFD3, 0xFFCFCFD3, 0xFFCFCFD3),
	STONE11(0xFFBDC1BB, 0xFFBDC1BB, 0xFFBDC1BB),
	STONE12(0xFFA3A7A8, 0xFFA3A7A8, 0xFFA3A7A8),
	STONE13(0xFF949693, 0xFF949693, 0xFF949693),
	STONE14(0xFF969593, 0xFF969593, 0xFF969593),
	STONE15(0xFF6B6869, 0xFF6B6869, 0xFF6B6869),

	COMP0(0xFF43A732, 0xFF43A732, 0xFF43A732),
	COMP1(0xFF572938, 0xFF572938, 0xFF572938),
	COMP2(0xFF967176, 0xFF967176, 0xFF967176),
	COMP3(0xFFB48612, 0xFFB48612, 0xFFB48612),
	COMP4(0xFFC64102, 0xFFC64102, 0xFFC64102),
	COMP5(0xFF259FAB, 0xFF259FAB, 0xFF259FAB),
	COMP6(0xFF681BAF, 0xFF681BAF, 0xFF681BAF),
	COMP7(0xFFAB5917, 0xFFAB5917, 0xFFAB5917),
	COMP8(0xFF591032, 0xFF591032, 0xFF591032),
	COMP9(0xFF6371AB, 0xFF6371AB, 0xFF6371AB),
	COMP10(0xFFBFA567, 0xFFBFA567, 0xFFBFA567),
	COMP11(0xFF123456, 0xFF123456, 0xFF123456),
	COMP12(0xFF654321, 0xFF654321, 0xFF654321),
	COMP13(0xFF5B7A0F, 0xFF5B7A0F, 0xFF5B7A0F),
	COMP14(0xFFFF00FF, 0xFFFF00FF, 0xFFFF00FF),
	COMP15(0xFFFFFF00, 0xFFFFFF00, 0xFFFFFF00),
	
	UU0(0xFF00FFFF, 0xFF00FFFF, 0xFF00FFFF),
	UU1(0xFF00FF00, 0xFF00FF00, 0xFF00FF00),
	UU2(0xFF0000FF, 0xFF0000FF, 0xFF0000FF),
	UU3(0xFFFF0000, 0xFFFF0000, 0xFFFF0000),
	UU4(0xFF55FF44, 0xFF55FF44, 0xFF55FF44),
	UU5(0xFF3322FF, 0xFF3322FF, 0xFF3322FF),
	UU6(0xFFFF5555, 0xFFFF5555, 0xFFFF5555),
	UU7(0xFF5555FF, 0xFF5555FF, 0xFF5555FF),
	UU8(0xFF55FF55, 0xFF55FF55, 0xFF55FF55),
	UU9(0xFFFFFF55, 0xFFFFFF55, 0xFFFFFF55),
	UU10(0xFFFF55FF, 0xFFFF55FF, 0xFFFF55FF),
	UU11(0xFF55FFFF, 0xFF55FFFF, 0xFF55FFFF),
	UU12(0xFFAAAAAA, 0xFFAAAAAA, 0xFFAAAAAA),
	UU13(0xFF777777, 0xFF777777, 0xFF777777),
	UU14(0xFFAA4499, 0xFFAA4499, 0xFFAA4499),
	UU15(0xFF22AA57, 0xFF22AA57, 0xFF22AA57);
	
	public final static byte STONES = 0;
	public final static byte COMPOSITES = 16;
	
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
