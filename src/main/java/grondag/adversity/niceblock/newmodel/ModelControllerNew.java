package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.newmodel.color.BlockColors;
import grondag.adversity.niceblock.newmodel.color.IColorProvider;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.common.property.IExtendedBlockState;

/**
 * Manages model state parameters for texture and physical shape. Single instance per texture set shared by multiple models/blocks.
 * 
 * getCollisionHandler get NiceBlockStateFactory()
 */
public abstract class ModelControllerNew
{
    /**
     * MUST BE SET IN INITIALIZER!
     */
    protected BakedModelFactory bakedModelFactory;

    /**
     * Folder and prefix for textures. Will use first texture as starting point
     */
    protected final String textureName;

    /**
     * How many versions of textures are provided in the atlas. (count includes the first texture) Does not include rotations.
     */
    protected final int alternateTextureCount;

    /**
     * How many texture are in a complete set of textures used by controller. Total number of textures to be loaded will be textureCount * alternateTextureCount
     * Set this in initialization of subclass and should not need to override getAllTextures;
     */
    protected int textureCount = 1;
    
    /**
     * If true, textures on each face can be rotated. Cookbook must still handle selection of specific textures to match the rotations if the faces have a visible orientation.
     */
    protected final boolean useRotatedTexturesAsAlternates;

    /**
     * Layer in which block faces should render.
     */
    protected final EnumWorldBlockLayer renderLayer;
    
    /**
     * If false, faces of the block are not shaded according to light levels.
     */
    public final boolean isShaded;
    
    /** 
     * For tile entity blocks, can this conroller's client state be cached 
     * in the client-side TE?  Set to false if state depends on neighbor blocks
     * that are NOT nice blocks that invalidate the cached state.
     * The block handlers for neighbor block updates are server-side, so they don't help us.
     */
    public boolean useCachedClientState = true;
    
    protected ModelControllerNew(String textureName, int alternateTextureCount, EnumWorldBlockLayer renderLayer, boolean isShaded,
            boolean useRotatedTexturesAsAlternates)
    {
        this.textureName = textureName;
        this.alternateTextureCount = Math.max(1, alternateTextureCount);
        this.renderLayer = renderLayer;
        this.isShaded = isShaded;
        this.useRotatedTexturesAsAlternates = useRotatedTexturesAsAlternates;
    }

    /**
     * used by block helper methods to get shape-related state
     */
    public abstract int getClientShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos);

    /**
     * used by dispatcher for cache initialization
     */
    public abstract int getShapeCount();

    public BakedModelFactory getBakedModelFactory()
    {
        return this.bakedModelFactory;
    }
    
    /**
     * used by model for texture lookup
     */
    public String getTextureName(int offset)
    {
        return "adversity:blocks/" + textureName + "_" + (offset >> 3) + "_" + (offset & 7);
    }

    /**
     * identifies all textures needed for texture stitch
     */
    public String[] getAllTextureNames()
    {
        final String retVal[] = new String[alternateTextureCount * textureCount];

        for (int i = 0; i < alternateTextureCount * textureCount; i++)
        {
            retVal[i] = getTextureName(i);
        }
        return retVal;
    }

    /**
     * Override if special collision handling is needed due to non-cubic shape.
     */
    public ICollisionHandler getCollisionHandler()
    {
        return null;
    }

    /**
     * Used by NiceBlock to control rendering.
     */
    public boolean canRenderInLayer(EnumWorldBlockLayer layer)
    {
        return layer == renderLayer;
    }
}
