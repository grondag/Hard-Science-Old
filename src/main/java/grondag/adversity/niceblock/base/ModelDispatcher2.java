package grondag.adversity.niceblock.base;

import grondag.adversity.Adversity;
import grondag.adversity.library.model.ItemModelDelegate2;
import grondag.adversity.niceblock.modelstate.ModelState;
import grondag.adversity.niceblock.modelstate.ModelStateGroup;
import grondag.adversity.niceblock.modelstate.ModelStateSet;
import grondag.adversity.niceblock.support.ICollisionHandler;

import java.util.UUID;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;

public abstract class ModelDispatcher2 implements IBakedModel
{
    protected final String resourceName = UUID.randomUUID().toString();
    protected final String particleTextureName;
    protected final ModelFactory2[] models;
    protected final ModelStateSet stateSet;
    
    protected TextureAtlasSprite particleTexture;
    
    public ModelDispatcher2(String particleTextureName, ModelFactory2... models)
    {
        this.particleTextureName = "adversity:blocks/" + particleTextureName;
        this.models = models;
        
        ModelStateGroup groups[] = new ModelStateGroup[models.length];
        for(int i = 0; i < models.length; i++)
        {
            groups[i] = models[i].getStateGroup();
        }
        this.stateSet = ModelStateSet.find(groups);
    }
        
    /** Update state from world in passed modelState if needed.  
     * Returns true if the state changed.
     */
    public boolean refreshModelStateFromWorld(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos, ModelState modelState, boolean isCachedStateDirty)
    {
        //TODO: make this side-aware, states only need to be refreshed on server if matter for collision detection
        
        long oldKey = modelState.stateValue.getKey();
        //TODO: handle block test somehow
        modelState.stateValue = stateSet.getRefreshedValueFromWorld(modelState.stateValue, block, null, state, world, pos);
        return modelState.stateValue.getKey() != oldKey;
    }

    public abstract void handleBakeEvent(ModelBakeEvent event);

    /**
     * Override if special collision handling is needed due to non-cubic shape.
     */
    public abstract ICollisionHandler getCollisionHandler();

    /**
     * Used by NiceBlock to control rendering.
     */
    public abstract boolean canRenderInLayer(BlockRenderLayer layer);
    
    public String getModelResourceString()
    {
        return Adversity.MODID + ":" + resourceName;
    }

    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        if(particleTexture == null)
        {
            particleTexture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(particleTextureName);
        }
        return particleTexture;
    }
    
    public abstract IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity);
 
    
    /**
     * Override to registers all textures that will be needed for associated controllers. 
     * Be sure to call super so that parent implementation handles particle texture.
     * Happens before model bake.
     */
    public void handleTexturePreStitch(Pre event)
    {
        event.getMap().registerSprite(new ResourceLocation(particleTextureName));
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return false;
    }
    
	@Override
	public boolean isGui3d()
	{
		return true;
	}
	
	@Override
	public ItemOverrideList getOverrides()
	{
		return new ItemModelDelegate2(this) ;
	}
	
	@Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return ItemCameraTransforms.DEFAULT;
    }

}