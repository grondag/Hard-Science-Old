package grondag.adversity.niceblock.model;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.model.QuadContainer2;
import grondag.adversity.library.model.quadfactory.CSGShape;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.CSGBlock2;
import grondag.adversity.niceblock.base.ModelFactory2;
import grondag.adversity.niceblock.base.NiceBlock2;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import grondag.adversity.niceblock.support.CollisionBoxGenerator;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CSGModelFactory2 extends ModelFactory2<ModelFactory2.ModelInputs> implements ICollisionHandler
{
    
    public CSGModelFactory2(ModelInputs modelInputs, ModelStateComponent<?,?>... components)
    {
        super(modelInputs, components);
    }

    protected List<RawQuad> makeRawQuads(ModelStateSetValue state)
    {
        RawQuad template = new RawQuad();
        template.lockUV = true;
        ColorMap colorMap = state.getValue(colorComponent);
        template.color = colorMap.getColor(EnumColorMap.BASE);
        template.rotation = state.getValue(rotationComponent);
        template.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                buildTextureName(modelInputs.textureName, state.getValue(textureComponent)));
        template.lightingMode = modelInputs.isShaded ? LightingMode.SHADED : LightingMode.FULLBRIGHT;
    
        CSGShape  delta = null;
        CSGShape result = null;
        
        //union opposite overlapping coplanar faces
//        result = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0, .4, .5, 1, 1, 1), template));
//        delta = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(.3, 0, 0, .7, .6, .5), template));
//        result = result.union(delta);
        
        //union opposite overlapping coplanar faces created by diff
//        result = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.1, 0.1, 0.1, 0.9, 0.9, 0.9), template));
//        delta = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.3, 0.03, 0.5, 0.5, 0.95, 0.7), template));  
//        result = result.difference(delta);
//        delta = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.3, 0, 0, 0.4, .2, 1), template));
//        result = result.union(delta);
        
        // cylinder/cone test
        result = new CSGShape(QuadFactory.makeCylinder(new Vec3d(.5, 0, .5), new Vec3d(.5, 1, .5), 0.5, 0, template));
        
//        CSGShape quadsA = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0, 0.4, 0.4, 1.0, 0.6, 0.6), template));
//        template.color = colorMap.getColorMap(EnumColorMap.BORDER);
//        CSGShape quadsB = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.2, 0, 0.4, 0.6, 1.0, 0.8), template));

//        CSGShape quadsA = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.0, 0.0, 0.0, 1, 1, 1), template));
//        template.color = colorMap.getColorMap(EnumColorMap.BORDER);
//        CSGShape quadsB = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0, .1, .45, .05, 0.9, .55), template));

//        CSGShape result = quadsA.intersect(quadsB);
//        CSGShape result = quadsA.union(quadsB);
//      CSGShape result = quadsA.difference(quadsB);


      
//        
//        quadsB = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0, 0, 0.3, 1, 1, .7), template));
//        result = result.difference(quadsB);
        
//        template.color = colorMap.getColorMap(EnumColorMap.HIGHLIGHT);
//        quadsB = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.2, 0.2, 0, 0.8, 0.8, 1), template));
//        result = result.difference(quadsB);
//
//        template.color = colorMap.getColorMap(EnumColorMap.HIGHLIGHT);
//        quadsB = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0, 0, .4, 1, .4, .65), template));
//        result = result.difference(quadsB);
        
    //    result.recolor();
        
//        for(RawQuad quad : result)
//        {
//            Vec3d faceNormal = quad.getFaceNormal();
//            if(!(faceNormal.xCoord ==0 || Math.abs(Math.abs(faceNormal.xCoord) - 1) < QuadFactory.EPSILON)
//                    || !(faceNormal.yCoord ==0 || Math.abs(Math.abs(faceNormal.yCoord) - 1) < QuadFactory.EPSILON)
//                    || !(faceNormal.zCoord ==0 || Math.abs(Math.abs(faceNormal.zCoord) - 1) < QuadFactory.EPSILON)
//                    || !(Math.abs(Math.abs(faceNormal.xCoord + faceNormal.yCoord + faceNormal.zCoord) - 1) < QuadFactory.EPSILON))
//            {
//                Adversity.log.info("hmmm");
//            }
//            
//            for(int i = 0; i < 4; i++)
//            {
//                Vec3d vNorm = quad.getVertex(i).getNormal();
//                if(vNorm != null && !vNorm.equals(quad.getFaceNormal()))
//                {
//                    Adversity.log.info("hmmm");
//                }
//                if(quad.getVertex(i).hasNormal())
//                {
//                    Adversity.log.info("hmmm");
//                }
//                if(quad.getVertex(i).color != quad.color)
//                {
//                    Adversity.log.info("hmmm");
//                }
//            }
//        }
        
        return result;
    }
    
    @Override
    public QuadContainer2 getFaceQuads(ModelStateSetValue state, BlockRenderLayer renderLayer)
    {
        if(renderLayer != modelInputs.renderLayer) return QuadContainer2.EMPTY_CONTAINER;
        
        return QuadContainer2.fromRawQuads(this.makeRawQuads(state));
    }

    @Override
    public List<BakedQuad> getItemQuads(ModelStateSetValue state)
    {
        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
        for(RawQuad quad : this.makeRawQuads(state))
        {
      
            builder.add(quad.createBakedQuad());
            
        }   
        return builder.build();
    }
    
    @Override
    public ICollisionHandler getCollisionHandler()
    {
        return this;
    }

    @Override
    public long getCollisionKey(IBlockState state, World worldIn, BlockPos pos)
    {
        return 0;
    }

    @Override
    public List<AxisAlignedBB> getModelBounds(IBlockState state, World worldIn, BlockPos pos)
    {
        Block block = state.getBlock();
        if(block instanceof CSGBlock2 )
        {            
            return CollisionBoxGenerator.makeCollisionBox(
                    makeRawQuads(((NiceBlock2) block).dispatcher.getStateSet().getSetValueFromBits(getCollisionKey(state, worldIn, pos))));
        }
        else
        {
            return Collections.emptyList();
        }
    }

    @Override
    public int getKeyBitLength()
    {
        return 1;
    }
}
