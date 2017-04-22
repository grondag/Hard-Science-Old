package grondag.adversity.niceblock.model;

import java.util.Collections;
import java.util.List;
import com.google.common.collect.ImmutableList;

import grondag.adversity.Adversity;
import grondag.adversity.library.model.QuadContainer;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.ModelAppearance;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import grondag.adversity.niceblock.support.AbstractCollisionHandler;
import grondag.adversity.niceblock.support.SimpleCollisionHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class HeightModelFactory extends ColorModelFactory
{
    
    private static ModelAppearance COLLISION_INPUTS = new ModelAppearance("colored_stone", LightingMode.SHADED, BlockRenderLayer.SOLID);
    //main diff is lack of species
    private static HeightModelFactory COLLISION_INSTANCE = new HeightModelFactory(COLLISION_INPUTS, ModelStateComponents.COLORS_WHITE,
            ModelStateComponents.TEXTURE_1, ModelStateComponents.ROTATION_NONE);
    private static SimpleCollisionHandler COLLISION_HANDLER = new SimpleCollisionHandler(COLLISION_INSTANCE);
    
    private static final AxisAlignedBB[] COLLISION_BOUNDS =
    {
        new AxisAlignedBB(0, 0, 0, 1, 1F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 2F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 3F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 4F/16F, 1),

        new AxisAlignedBB(0, 0, 0, 1, 5F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 6F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 7F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 8F/16F, 1),
    
        new AxisAlignedBB(0, 0, 0, 1, 9F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 10F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 11F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 12F,16F),
        
        new AxisAlignedBB(0, 0, 0, 1, 13F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 14F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 15F/16F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 1, 1)
            
    };
    
    public HeightModelFactory(ModelAppearance modelInputs, ModelStateComponent<?, ?>... components)
    {
        super(modelInputs, components);
    }


    // TODO: need to handle lack of species for collision version
    protected RawQuad makeFaceQuad(ModelStateSetValue state, EnumFacing face)
    {
        if (face == null) return null;

        RawQuad result = new RawQuad();
        ColorMap colorMap = state.getValue(colorComponent);
        result.color = colorMap.getColor(EnumColorMap.BASE);
        result.rotation = state.getValue(rotationComponent);
        result.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                buildTextureName(modelInputs.textureName, state.getValue(textureComponent)));
        result.lightingMode = modelInputs.lightingMode;
        result.lockUV = true;
        result.setFace(face);
        double height = ((state.getValue(speciesComponent) & 15) + 1) / 16.0;
        
        switch(face)
        {
        case UP:
            
            result.setupFaceQuad(
                    0.0,
                    0.0,
                    1.0,
                    1.0,
                    1-height,
                    EnumFacing.NORTH);
            break;
             
        case EAST:
        case WEST:
        case NORTH:
        case SOUTH:
            result.setupFaceQuad(
                    0.0,
                    0.0,
                    1.0, 
                    height,
                    0.0,
                    EnumFacing.UP);
            break;
            
        case DOWN:
        default:
            result.setupFaceQuad(0.0, 0.0, 1.0, 1.0, 0.0, EnumFacing.NORTH);
            break;
        }

        return result;

    }

  
    @Override
    public QuadContainer getFaceQuads(ModelStateSetValue state, BlockRenderLayer renderLayer)
    {
        if(renderLayer != modelInputs.renderLayer) return QuadContainer.EMPTY_CONTAINER;
         QuadContainer.QuadContainerBuilder builder = new QuadContainer.QuadContainerBuilder();
        builder.setQuads(null, QuadFactory.EMPTY_QUAD_LIST);
        for(EnumFacing face : EnumFacing.values())
        {
            builder.setQuads(face, Collections.singletonList(this.makeFaceQuad(state, face).createBakedQuad()));
        }
        return builder.build();
    }

    @Override
    public List<BakedQuad> getItemQuads(ModelStateSetValue state)
    {
        ImmutableList.Builder<BakedQuad> general = new ImmutableList.Builder<BakedQuad>();
        for(EnumFacing face : EnumFacing.VALUES)
        {
            general.add(this.makeFaceQuad(state, face).createBakedQuad());
        }        
        return general.build();
    }
    
    @Override
    public AbstractCollisionHandler getCollisionHandler(ModelDispatcher dispatcher)
    {
        return COLLISION_HANDLER;
    }

    @Override
    public List<RawQuad> getCollisionQuads(ModelStateSetValue state)
    {
        ImmutableList.Builder<RawQuad> general = new ImmutableList.Builder<RawQuad>();
        for(EnumFacing face : EnumFacing.VALUES)
        {
            general.add(COLLISION_INSTANCE.makeFaceQuad(state, face));
        }        
        return general.build();
    }


    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        try
        {
            return COLLISION_BOUNDS[state.getValue(NiceBlock.META)];
        }
        catch (Exception ex)
        {
            Adversity.LOG.info("HeightModelFactory recevied Collision Bounding Box check for a foreign block.");
            return Block.FULL_BLOCK_AABB;
        }
    }
    
    
}
