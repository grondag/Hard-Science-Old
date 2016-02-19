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
     * Unlocalized name for this shape and texturing of block
     */
    public final String styleName;

    /**
     * Folder and prefix for textures. Will use first texture as starting point
     */
    protected final String textureName;

    /**
     * How many texture are in a complete set of textures used by controller. Total number of textures to be loaded will be textureCount * alternateTextureCount
     */
    protected int textureCount = 1;

    /**
     * How many versions of textures are provided in the atlas. (count includes the first texture) Does not include rotations.
     */
    protected final int alternateTextureCount;

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
    
    protected ModelControllerNew(String styleName, String textureName, int alternateTextureCount, EnumWorldBlockLayer renderLayer, boolean isShaded,
            boolean useRotatedTexturesAsAlternates)
    {
        this.styleName = styleName;
        this.textureName = textureName;
        this.alternateTextureCount = Math.max(1, alternateTextureCount);
        this.renderLayer = renderLayer;
        this.isShaded = isShaded;
        this.useRotatedTexturesAsAlternates = useRotatedTexturesAsAlternates;
    }

    /**
     * used by block helper methods to get shape-related state
     */
    public abstract int getBlockShapeIndex(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos);

    // not useful?
    //public abstract int getItemShapeIndex(ItemStack stack);

    /**
     * used by dispatcher for cache initialization
     */
    public abstract int getBakedBlockModelCount();

    public abstract int getBakedItemModelCount();

    public abstract BakedModelFactory getBakedModelFactory();
    
    public abstract int getBlockModelIndex(ModelState state);
    public abstract int getItemModelIndex(ModelState state);
    
    public String getParticleTextureName()
    {
        return "adversity:blocks/particle";
        //return this.getTextureName(0);
    }
    
    public IColorProvider getColorProvider()
    {
        return BlockColors.INSTANCE;
    }


    /**
     * used by model for texture lookup
     */
    public String getTextureName(int offset)
    {
        return "adversity:blocks/" + textureName + "_" + (offset >> 3) + "_" + (offset & 7);
    }

    /**
     * identifies all textures needed for texture bake
     */
    public String[] getAllTextureNames()
    {
        final String retVal[] = new String[alternateTextureCount * textureCount + 1];

        for (int i = 0; i < alternateTextureCount * textureCount; i++)
        {
            retVal[i] = getTextureName(i);
        }
        retVal[alternateTextureCount * textureCount] = "adversity:blocks/particle";
        return retVal;
    }

    /**
     * Registers all textures that will be needed for this controller. Happens before model bake.
     */
    public void handleTexturePreStitch(Pre event)
    {
        Adversity.log.info("handleTexturePreStitch");
        
        for (String tex : getAllTextureNames())
        {
            event.map.registerSprite(new ResourceLocation(tex));
        }
    }

    /**
     * Override if special collision handling is needed due to non-cubic shape.
     */
    public abstract ICollisionHandler getCollisionHandler();

    /**
     * Used by NiceBlock to control rendering.
     */
    public boolean canRenderInLayer(EnumWorldBlockLayer layer)
    {
        return layer == renderLayer;
    }
}
