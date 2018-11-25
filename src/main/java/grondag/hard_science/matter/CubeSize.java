package grondag.hard_science.matter;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.model.mesh.MeshHelper;
import grondag.exotic_matter.model.primitives.better.IPolygon;
import grondag.exotic_matter.varia.Color;
import grondag.exotic_matter.varia.Color.EnumHCLFailureMode;
import grondag.exotic_matter.world.Rotation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Package sizes for solid material blocks
 * and fluid containers.
 */
public enum CubeSize
{
    BLOCK(0, 1f, "medium_square"),
    ONE(1, 0.700f, "medium_dot"),
    TWO(2, 0.490f, "two_dots"),
    THREE(3, 0.343f, "big_triangle"),
    FOUR(4, 0.240f, "big_diamond"),
    FIVE(5, 0.168f, "big_pentagon"),
    SIX(6, 0.118f, "big_hexagon");
    
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
    
    /**
     * Length of cube edges, in mm
     */
    public final float edgeLength_mm;
    
    /*
     * Surface area of cube face in square millimeters
     */
    public final float faceSurfaceArea_mm2;
    
    /*
     * Surface area of cube face in square micrometers
     */
    public final long faceSurfaceArea_micrometer2;
    
    private CubeSize(int divisionLevel, float renderScale, String symbolTexture)
    {
        this.divisionLevel = divisionLevel;
        this.renderScale = renderScale;
        this.symbolTexture = symbolTexture;
        this.nanoLiters = VolumeUnits.KILOLITER.nL / (1 << (divisionLevel * 3));
        this.edgeLength_mm = 1000f / (1 << divisionLevel);
        this.faceSurfaceArea_mm2 = this.edgeLength_mm * this.edgeLength_mm;
        this.faceSurfaceArea_micrometer2 = (long) (this.faceSurfaceArea_mm2 * 1000000);
                
        // want colors that are visually separated, not too oversaturated, not too bright
        this.symbolColor = Color.fromHCL(360f * divisionLevel / 7 , 60, 60, EnumHCLFailureMode.REDUCE_CHROMA).RGB_int | 0xFF000000;;
    }
    
    @SideOnly(Side.CLIENT)
    private List<IPolygon> quads;

    @SideOnly(Side.CLIENT)
    public List<IPolygon> rawQuads()
    {
        if(this.quads == null)
        {
            ArrayList<IPolygon> quadList = new ArrayList<IPolygon>();
            
            MeshHelper.addTextureToAllFaces(false, this.symbolTexture, 0.02f, 0.26f, 0.24f, 1.025f, this.symbolColor, true, Rotation.ROTATE_NONE, quadList);
            
            this.quads = ImmutableList.copyOf(quadList);
        }
        return this.quads;
    }

    public String toolTip()
    {
        return I18n.translateToLocal("cubesize." + this.name().toLowerCase()).trim();
    }
}
