package grondag.hard_science.materials;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.render.QuadHelper;
import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.library.varia.Color;
import grondag.hard_science.library.varia.Color.EnumHCLFailureMode;
import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.machines.support.StandardUnits;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public enum CubeSize
{
    BLOCK(StandardUnits.nL_HS_CUBE_ZERO, 1f, 0, "medium_square"),
    ONE(StandardUnits.nL_HS_CUBE_ONE, 0.700f, 1, "medium_dot"),
    TWO(StandardUnits.nL_HS_CUBE_TWO, 0.490f, 2, "two_dots"),
    THREE(StandardUnits.nL_HS_CUBE_THREE, 0.343f, 3, "big_triangle"),
    FOUR(StandardUnits.nL_HS_CUBE_FOUR, 0.240f, 4, "big_diamond"),
    FIVE(StandardUnits.nL_HS_CUBE_FIVE, 0.168f, 5, "big_pentagon"),
    SIX(StandardUnits.nL_HS_CUBE_SIX, 0.118f, 6, "big_hexagon");
    
    /**
     * Volume of the block, including any packaging.
     */
    public final long nanoLiters;
    
    /**
     * Scale to render cube, relative to a full block.
     * Can't render them as small as they actually are 
     * - would never be able to see them in game.
     */
    public final float renderScale;
    
    /**
     * How many times block has been split into 8 pieces
     * to create this size of cube.
     */
    public final int divisionLevel;
    
    /**
     * Render to show size in GUI. Does NOT include mod/blocks prefix!
     */
    public final String symbolTexture;
    
    /**
     * Color for size symbol GUI. 
     */
    public final int symbolColor;
    
    private CubeSize(long nanoLiters, float renderScale, int divisionLevel, String symbolTexture)
    {
        this.nanoLiters = nanoLiters;
        this.renderScale = renderScale;
        this.divisionLevel = divisionLevel;
        this.symbolTexture = symbolTexture;
        
        // want colors that are visually separated, not too oversaturated or too bright
        this.symbolColor = Color.fromHCL(360f * divisionLevel / 7 , 60, 60, EnumHCLFailureMode.REDUCE_CHROMA).RGB_int | 0xFF000000;;
    }
    
    @SideOnly(Side.CLIENT)
    private List<RawQuad> quads;

    @SideOnly(Side.CLIENT)
    public List<RawQuad> rawQuads()
    {
        if(this.quads == null)
        {
            ArrayList<RawQuad> quadList = new ArrayList<RawQuad>();
            
            QuadHelper.addTextureToAllFaces(this.symbolTexture, 0.02f, 0.26f, 0.24f, 1.025f, this.symbolColor, true, Rotation.ROTATE_NONE, quadList);
            
            this.quads = ImmutableList.copyOf(quadList);
        }
        return this.quads;
    }

    public String toolTip()
    {
        return I18n.translateToLocal("cubesize." + this.name().toLowerCase()).trim();
    }
}
