package grondag.adversity.niceblock.model;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.model.QuadContainer;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import grondag.adversity.niceblock.support.ICollisionHandler;
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
    public HeightModelFactory(ModelFactory.ModelInputs modelInputs, ModelStateComponent<?, ?>... components)
    {
        super(modelInputs, components);
    }


    protected List<BakedQuad> makeFaceQuads(ModelStateSetValue state, EnumFacing face)
    {
        if (face == null) return QuadFactory.EMPTY_QUAD_LIST;

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
        
        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
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
            builder.add(result.createBakedQuad());
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
            builder.add(result.createBakedQuad());
            break;
            
        case DOWN:
        default:
            result.setupFaceQuad(0.0, 0.0, 1.0, 1.0, 0.0, EnumFacing.NORTH);
            builder.add(result.createBakedQuad());
            break;
        }

        return builder.build();

    }

  
    @Override
    public QuadContainer getFaceQuads(ModelStateSetValue state, BlockRenderLayer renderLayer)
    {
        if(renderLayer != modelInputs.renderLayer) return QuadContainer.EMPTY_CONTAINER;
         QuadContainer.QuadContainerBuilder builder = new QuadContainer.QuadContainerBuilder();
        builder.setQuads(null, QuadFactory.EMPTY_QUAD_LIST);
        for(EnumFacing face : EnumFacing.values())
        {
            builder.setQuads(face, this.makeFaceQuads(state, face));
        }
        return builder.build();
    }

    @Override
    public List<BakedQuad> getItemQuads(ModelStateSetValue state)
    {
        ImmutableList.Builder<BakedQuad> general = new ImmutableList.Builder<BakedQuad>();
        for(EnumFacing face : EnumFacing.VALUES)
        {
            general.addAll(this.makeFaceQuads(state, face));
        }        
        return general.build();
    }
    
    @Override
    public ICollisionHandler getCollisionHandler(ModelDispatcher dispatcher)
    {
        return new HeightCollisionHandler(dispatcher);
    }

    public class HeightCollisionHandler  implements ICollisionHandler
    {
        private HeightCollisionHandler(ModelDispatcher dispatcher)
        {
            //NOOP
        }
        
        @Override
        public long getCollisionKey(IBlockState state, IBlockAccess worldIn, BlockPos pos)
        {
            return (long) state.getValue(NiceBlock.META);
        }
    
        @Override
        public List<AxisAlignedBB> getModelBounds(long collisionKey)
        {
            ImmutableList<AxisAlignedBB> retVal = new ImmutableList.Builder<AxisAlignedBB>().add(new AxisAlignedBB(0, 0, 0, 1, (collisionKey + 1)/16.0, 1)).build();
            return retVal;
        }
    
    
        @Override
        public int getKeyBitLength()
        {
            return 4;
        }
    }
}
