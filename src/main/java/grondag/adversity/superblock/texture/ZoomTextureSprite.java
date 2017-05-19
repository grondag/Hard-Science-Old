package grondag.adversity.superblock.texture;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import grondag.adversity.Output;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

//TODO: remove this if not used
public class ZoomTextureSprite extends TextureAtlasSprite
{

    public final int zoomLevel;
    public final TextureAtlasSprite source;
    public final int xIndex;
    public final int yIndex;

//    field_110977_n  minV
//    field_110978_o  maxV
//    field_110979_l  minU
//    field_110980_m  maxU

    private static Field fieldMinU;
    private static Field fieldMinV;
    private static Field fieldMaxU;
    private static Field fieldMaxV;
    static
    {
        fieldMinU = ReflectionHelper.findField(TextureAtlasSprite.class, "field_110979_l", "minU")    ;
        fieldMinV = ReflectionHelper.findField(TextureAtlasSprite.class, "field_110977_n", "minV")    ;
        fieldMaxU = ReflectionHelper.findField(TextureAtlasSprite.class, "field_110980_m", "maxU")    ;
        fieldMaxV = ReflectionHelper.findField(TextureAtlasSprite.class, "field_110978_o", "maxV")    ;
    }
    
    protected ZoomTextureSprite(TextureAtlasSprite source, int zoomLevel, int xIndex, int yIndex) throws IllegalArgumentException, IllegalAccessException
    {
        super(source.getIconName() + " zoom x" + zoomLevel);
        this.source = source;
        this.zoomLevel = zoomLevel;
        this.xIndex = xIndex;
        this.yIndex = yIndex;
        this.width = source.getIconWidth() / zoomLevel;
        this.height = source.getIconHeight() / zoomLevel;
        this.originX = source.getOriginX() + this.width * xIndex;
        this.originY = source.getOriginY() + this.height * yIndex;
        
        double uSlice = ((double)source.getMaxU() - source.getMinU()) / zoomLevel;
        double vSlice = ((double)source.getMaxV() - source.getMinV()) / zoomLevel;
        double minU = (float) (source.getMinU() + xIndex * uSlice);
        double minV = (float) (source.getMinV() + xIndex * vSlice);
        
        fieldMinU.setFloat(this, (float) minU);
        fieldMinV.setFloat(this, (float) minV);
        fieldMaxU.setFloat(this, (float) (minU + uSlice));
        fieldMaxV.setFloat(this, (float) (minV + vSlice));
    }
    
    @Override
    public int[][] getFrameTextureData(int index)
    {
        // not really supported - won't be properly zoomed
        if(Output.DEBUG_MODE) Output.getLog().warn("Unsupported operation on zoomed sprite: getFrameTextureData");
        return source.getFrameTextureData(index);
    }

    @Override
    public int getFrameCount()
    {
        return source.getFrameCount();
    }

    @Override
    public boolean hasAnimationMetadata()
    {
        return source.hasAnimationMetadata();
    }
 
    @Override
    public void setIconWidth(int newWidth)
    {
        if(Output.DEBUG_MODE) Output.getLog().warn("Unsupported operation on zoomed sprite: setIconWidth");
    }
    
    @Override
    public void setIconHeight(int newHeight)
    {
        if(Output.DEBUG_MODE) Output.getLog().warn("Unsupported operation on zoomed sprite: setIconHeight");
    }
    
    @Override
    public void initSprite(int inX, int inY, int originInX, int originInY, boolean rotatedIn)
    {
        if(Output.DEBUG_MODE) Output.getLog().warn("Unsupported operation on zoomed sprite: initSprite");
    }

    @Override
    public void copyFrom(TextureAtlasSprite atlasSpirit)
    {
        if(Output.DEBUG_MODE) Output.getLog().warn("Unsupported operation on zoomed sprite: copyFrom");
    }

    @Override
    public void updateAnimation()
    {
        if(Output.DEBUG_MODE) Output.getLog().warn("Unsupported operation on zoomed sprite: updateAnimation");
    }

    @Override
    public void loadSprite(PngSizeInfo sizeInfo, boolean p_188538_2_) throws IOException
    {
        if(Output.DEBUG_MODE) Output.getLog().warn("Unsupported operation on zoomed sprite: loadSprite");
    }

    @Override
    public void loadSpriteFrames(IResource resource, int mipmaplevels) throws IOException
    {
        if(Output.DEBUG_MODE) Output.getLog().warn("Unsupported operation on zoomed sprite: loadSpriteFrames");
    }

    @Override
    public void generateMipmaps(int level)
    {
        if(Output.DEBUG_MODE) Output.getLog().warn("Unsupported operation on zoomed sprite: generateMipmaps");
    }

    @Override
    public void clearFramesTextureData()
    {
        if(Output.DEBUG_MODE) Output.getLog().warn("Unsupported operation on zoomed sprite: clearFramesTextureData");
    }

    @Override
    public void setFramesTextureData(List<int[][]> newFramesTextureData)
    {
        if(Output.DEBUG_MODE) Output.getLog().warn("Unsupported operation on zoomed sprite: setFramesTextureData");
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location)
    {
        return false;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location)
    {
        if(Output.DEBUG_MODE) Output.getLog().warn("Unsupported operation on zoomed sprite: load");
        return false;
    }

}
