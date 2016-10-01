package grondag.adversity.niceblock.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.common.collect.ImmutableList;

import grondag.adversity.Adversity;
import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.library.model.QuadContainer;
import grondag.adversity.library.model.quadfactory.CSGShape;
import grondag.adversity.library.model.quadfactory.FaceVertex;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import grondag.adversity.niceblock.support.CollisionBoxGenerator;
import grondag.adversity.niceblock.support.ICollisionHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FlowModelFactory extends ModelFactory<ModelFactory.ModelInputs> implements ICollisionHandler
{
    private final boolean enableCollision;
    //TODO: add caching to reduce memory consumption for multi-layer dispatchers and multi-species dispatchers
    // Also, make it configurable via parameter, because not all blocks need it.
    
    public FlowModelFactory(ModelInputs modelInputs, boolean enableCollision, ModelStateComponent<?,?>... components) 
    {
        super(modelInputs, components);
        this.enableCollision = enableCollision;
    }
    
    
    //TODO: make configurable
    /**
     * Flowing terrain tends to appear washed out due to simplistic lighting model.
     * Not hacking the lighter, but can scale horizontal component of vertex normals
     * to make the shadows a little deeper.
     */
    private Vec3d shadowEnhance(Vec3d vec)
    {
        return new Vec3d(vec.xCoord * 4, vec.yCoord, vec.zCoord * 2);
    }
    
    @Override
    public QuadContainer getFaceQuads(ModelStateSetValue state, BlockRenderLayer renderLayer)
    {
        if(renderLayer != modelInputs.renderLayer) return QuadContainer.EMPTY_CONTAINER;
     
        return QuadContainer.fromRawQuads(this.makeRawQuads(state));
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
    

    public List<RawQuad> makeRawQuads(ModelStateSetValue state)
    {
       CSGShape rawQuads = new CSGShape();
        RawQuad template = new RawQuad();
        template.color = state.getValue(this.colorComponent).getColor(EnumColorMap.BASE);
        template.lockUV = true;
        template.rotation = state.getValue(this.rotationComponent);
        template.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                buildTextureName(modelInputs.textureName, state.getValue(textureComponent)));
        template.lightingMode = modelInputs.lightingMode;
  
        boolean isAnimated = template.textureSprite.hasAnimationMetadata();
        
        FlowHeightState flowState = state.getValue(this.flowJoinComponent);
                
        
        // center vertex setup
        FaceVertex fvCenter = new FaceVertex(0.5, 0.5, 1.0 - flowState.getCenterVertexHeight() + flowState.getYOffset());
        
        RawQuad quadInputsCenterLeft[] = new RawQuad[4];
        RawQuad quadInputsCenterRight[] = new RawQuad[4];
        ArrayList<ArrayList<RawQuad>> quadInputsSide = new ArrayList<ArrayList<RawQuad>>(4);
        ArrayList<ArrayList<RawQuad>> quadInputsCorner = new ArrayList<ArrayList<RawQuad>>(4);

        
        ///////////////////////////////////////////////
        // set up corner heights and face vertices
        ///////////////////////////////////////////////
        
        
        // Coordinates assume quad will be set up with North=top orientation
        // Depth will be set separately.
        FaceVertex fvMidCorner[] = new FaceVertex[HorizontalFace.values().length];
        FaceVertex fvFarCorner[] = new FaceVertex[HorizontalFace.values().length];
        
        fvMidCorner[HorizontalCorner.NORTH_EAST.ordinal()] = new FaceVertex(1, 1, 1.0);
        fvMidCorner[HorizontalCorner.NORTH_WEST.ordinal()] = new FaceVertex(0, 1, 1.0);
        fvMidCorner[HorizontalCorner.SOUTH_EAST.ordinal()] = new FaceVertex(1, 0, 1.0);
        fvMidCorner[HorizontalCorner.SOUTH_WEST.ordinal()] = new FaceVertex(0, 0, 1.0);
        
        fvFarCorner[HorizontalCorner.NORTH_EAST.ordinal()] = new FaceVertex(1.5, 1.5, 1.0);
        fvFarCorner[HorizontalCorner.NORTH_WEST.ordinal()] = new FaceVertex(-0.5, 1.5, 1.0);
        fvFarCorner[HorizontalCorner.SOUTH_EAST.ordinal()] = new FaceVertex(1.5, -0.5, 1.0);
        fvFarCorner[HorizontalCorner.SOUTH_WEST.ordinal()] = new FaceVertex(-0.5, -0.5, 1.0);
        
        for(HorizontalCorner corner : HorizontalCorner.values())
        {
            
            fvMidCorner[corner.ordinal()].depth = 1.0 - flowState.getMidCornerVertexHeight(corner) + flowState.getYOffset();
            fvFarCorner[corner.ordinal()].depth = 1.0 - flowState.getFarCornerVertexHeight(corner) + flowState.getYOffset();
            
            quadInputsCorner.add(new ArrayList<RawQuad>(8));            
        }
        
        // Coordinates assume quad will be set up with North=top orientation
        // Depth will be set separately.
        FaceVertex fvMidSide[] = new FaceVertex[HorizontalFace.values().length];
        fvMidSide[HorizontalFace.NORTH.ordinal()] = new FaceVertex(0.5, 1, 1.0);
        fvMidSide[HorizontalFace.SOUTH.ordinal()] = new FaceVertex(0.5, 0, 1.0);
        fvMidSide[HorizontalFace.EAST.ordinal()] = new FaceVertex(1.0, 0.5, 1.0);
        fvMidSide[HorizontalFace.WEST.ordinal()] = new FaceVertex(0, 0.5, 1.0);
        
        FaceVertex fvFarSide[] = new FaceVertex[HorizontalFace.values().length];
        fvFarSide[HorizontalFace.NORTH.ordinal()] = new FaceVertex(0.5, 1.5, 1.0);
        fvFarSide[HorizontalFace.SOUTH.ordinal()] = new FaceVertex(0.5, -0.5, 1.0);
        fvFarSide[HorizontalFace.EAST.ordinal()] = new FaceVertex(1.5, 0.5, 1.0);
        fvFarSide[HorizontalFace.WEST.ordinal()] = new FaceVertex(-0.5, 0.5, 1.0);
        
        for(HorizontalFace side : HorizontalFace.values())
        {
            fvMidSide[side.ordinal()].depth = 1.0 - flowState.getMidSideVertexHeight(side) + flowState.getYOffset();
            fvFarSide[side.ordinal()].depth = 1.0 - flowState.getFarSideVertexHeight(side) + flowState.getYOffset();
            
        quadInputsSide.add(new ArrayList<RawQuad>(8));   
            
   
            // build left and right quads on the block that edge this side
            template.setFace(EnumFacing.UP);
            RawQuad qiWork = new RawQuad(template, 3);
            qiWork.setupFaceQuad(
                    fvMidSide[side.ordinal()],
                    fvMidCorner[HorizontalCorner.find(side, side.getLeft()).ordinal()],
                    fvCenter,
                    EnumFacing.NORTH);           
            quadInputsCenterLeft[side.ordinal()] = qiWork;
            quadInputsSide.get(side.ordinal()).add(qiWork);
            quadInputsCorner.get(HorizontalCorner.find(side, side.getLeft()).ordinal()).add(qiWork);

            qiWork = new RawQuad(template, 3);
            qiWork.setupFaceQuad(
                    fvMidCorner[HorizontalCorner.find(side, side.getRight()).ordinal()],
                    fvMidSide[side.ordinal()],
                    fvCenter,
                    EnumFacing.NORTH);
            quadInputsCenterRight[side.ordinal()] = qiWork;
            quadInputsSide.get(side.ordinal()).add(qiWork);
            quadInputsCorner.get(HorizontalCorner.find(side, side.getRight()).ordinal()).add(qiWork);
            
            // side block tri that borders this block
            qiWork = new RawQuad(template, 3);
            qiWork.setupFaceQuad(
                    fvFarSide[side.ordinal()],
                    fvMidCorner[HorizontalCorner.find(side, side.getLeft()).ordinal()],
                    fvMidSide[side.ordinal()],
                    EnumFacing.NORTH);           
            quadInputsSide.get(side.ordinal()).add(qiWork);
            quadInputsCorner.get(HorizontalCorner.find(side, side.getLeft()).ordinal()).add(qiWork);

            qiWork = new RawQuad(template, 3);
            qiWork.setupFaceQuad(
                    fvMidCorner[HorizontalCorner.find(side, side.getRight()).ordinal()],
                    fvFarSide[side.ordinal()],
                    fvMidSide[side.ordinal()],
                    EnumFacing.NORTH);           
            quadInputsSide.get(side.ordinal()).add(qiWork);
            quadInputsCorner.get(HorizontalCorner.find(side, side.getRight()).ordinal()).add(qiWork);

            // side block tri that connects to corner but does not border side
            qiWork = new RawQuad(template, 3);
            qiWork.setupFaceQuad(
                    fvMidCorner[HorizontalCorner.find(side, side.getLeft()).ordinal()],
                    fvFarSide[side.ordinal()],
                    fvFarCorner[HorizontalCorner.find(side, side.getLeft()).ordinal()],
                    EnumFacing.NORTH);           
            quadInputsCorner.get(HorizontalCorner.find(side, side.getLeft()).ordinal()).add(qiWork);

            qiWork = new RawQuad(template, 3);
            qiWork.setupFaceQuad(
                    fvMidCorner[HorizontalCorner.find(side, side.getRight()).ordinal()],
                    fvFarCorner[HorizontalCorner.find(side, side.getRight()).ordinal()],
                    fvFarSide[side.ordinal()],
                    EnumFacing.NORTH);           
            quadInputsCorner.get(HorizontalCorner.find(side, side.getRight()).ordinal()).add(qiWork);
            
        }

        /** Used for Y coord of bottom face and as lower Y coord of side faces*/
        double bottom = -2 - flowState.getYOffset();// - QuadFactory.EPSILON;
        
        Vec3d normCenter = quadInputsCenterLeft[0].getFaceNormal();
        normCenter = normCenter.add(quadInputsCenterLeft[1].getFaceNormal());
        normCenter = normCenter.add(quadInputsCenterLeft[2].getFaceNormal());
        normCenter = normCenter.add(quadInputsCenterLeft[3].getFaceNormal());
        normCenter = normCenter.add(quadInputsCenterRight[0].getFaceNormal());
        normCenter = normCenter.add(quadInputsCenterRight[1].getFaceNormal());
        normCenter = normCenter.add(quadInputsCenterRight[2].getFaceNormal());
        normCenter = normCenter.add(quadInputsCenterRight[3].getFaceNormal());
        normCenter = shadowEnhance(normCenter).normalize();

        Vec3d normSide[] = new Vec3d[4];
        for(HorizontalFace side : HorizontalFace.values())
        {
            Vec3d normTemp = null;
            for(RawQuad qi : quadInputsSide.get(side.ordinal()))
            {
                if(normTemp == null) 
                {
                    normTemp = qi.getFaceNormal();
                }
                else
                {
                    normTemp = normTemp.add(qi.getFaceNormal());
                }
            }
            normSide[side.ordinal()] = shadowEnhance(normTemp).normalize();
        }
        
        Vec3d normCorner[] = new Vec3d[4];
        for(HorizontalCorner corner : HorizontalCorner.values())
        {
            Vec3d normTemp = null;
            for(RawQuad qi : quadInputsCorner.get(corner.ordinal()))
            {
                if(normTemp == null) 
                {
                    normTemp = qi.getFaceNormal();
                }
                else
                {
                    normTemp = normTemp.add(qi.getFaceNormal());
                }
            }
            normCorner[corner.ordinal()] = shadowEnhance(normTemp).normalize();
        }
        
        boolean sideIsSimple[] = new boolean[4];
        boolean topIsSimple = true;
        
        for(HorizontalFace side: HorizontalFace.values())
        {
            double avg = flowState.getMidCornerVertexHeight(HorizontalCorner.find(side, side.getLeft()));
            avg += flowState.getMidCornerVertexHeight(HorizontalCorner.find(side, side.getRight()));
            avg /= 2;
            sideIsSimple[side.ordinal()] = Math.abs(avg - flowState.getMidSideVertexHeight(side)) < 2.0 / 16.0;
            topIsSimple = topIsSimple && sideIsSimple[side.ordinal()];
        }

        double cross1 = (flowState.getMidCornerVertexHeight(HorizontalCorner.NORTH_EAST) + flowState.getMidCornerVertexHeight(HorizontalCorner.SOUTH_WEST)) / 2.0;
        double cross2 = (flowState.getMidCornerVertexHeight(HorizontalCorner.NORTH_WEST) + flowState.getMidCornerVertexHeight(HorizontalCorner.SOUTH_EAST)) / 2.0;
        topIsSimple = topIsSimple & (Math.abs(cross1 - cross2) < 2.0 / 16.0);
        
        
        //single top face if it is relatively flat and all sides can be drawn without a mid vertex
        if(topIsSimple)
        {
            template.setFace(EnumFacing.UP);
            RawQuad qi = new RawQuad(template, 4);
            qi.setupFaceQuad(
                    fvMidCorner[HorizontalCorner.SOUTH_WEST.ordinal()],
                    fvMidCorner[HorizontalCorner.SOUTH_EAST.ordinal()],
                    fvMidCorner[HorizontalCorner.NORTH_EAST.ordinal()],
                    fvMidCorner[HorizontalCorner.NORTH_WEST.ordinal()],
                    EnumFacing.NORTH);   
            qi.setVertexNormal(0, normCorner[HorizontalCorner.SOUTH_WEST.ordinal()]);
            qi.setVertexNormal(1, normCorner[HorizontalCorner.SOUTH_EAST.ordinal()]);
            qi.setVertexNormal(2, normCorner[HorizontalCorner.NORTH_EAST.ordinal()]);
            qi.setVertexNormal(3, normCorner[HorizontalCorner.NORTH_WEST.ordinal()]);
            
            rawQuads.add(qi);    
        }
        
        for(HorizontalFace side: HorizontalFace.values())
        {
           
            // don't use middle vertex if it is close to being in line with corners
            if(sideIsSimple[side.ordinal()])
            {
                // top
                if(!topIsSimple)
                {
                    template.setFace(EnumFacing.UP);
                    RawQuad qi = new RawQuad(template, 3);
                    qi.setupFaceQuad(
                            fvMidCorner[HorizontalCorner.find(side, side.getLeft()).ordinal()],
                            fvCenter,
                            fvMidCorner[HorizontalCorner.find(side, side.getRight()).ordinal()],
                            EnumFacing.NORTH);   
                    qi.setVertexNormal(0, normCorner[HorizontalCorner.find(side, side.getLeft()).ordinal()]);
                    qi.setVertexNormal(1, normCenter);
                    qi.setVertexNormal(2, normCorner[HorizontalCorner.find(side, side.getRight()).ordinal()]);                    
                    rawQuads.add(qi);    
                }
                
                // side
                template.setFace(side.face);
                RawQuad qSide = new RawQuad(template);
                qSide.setupFaceQuad(
                        new FaceVertex(0, bottom, 0),
                        new FaceVertex(1, bottom, 0),
                        new FaceVertex(1, flowState.getMidCornerVertexHeight(HorizontalCorner.find(side, side.getLeft())) - flowState.getYOffset(), 0),
                        new FaceVertex(0, flowState.getMidCornerVertexHeight(HorizontalCorner.find(side, side.getRight())) - flowState.getYOffset(), 0),
                        EnumFacing.UP);
                rawQuads.add(qSide);
            
            }
            else
            {
                //tops
                RawQuad qi = quadInputsCenterLeft[side.ordinal()];
                qi.setVertexNormal(0, normSide[side.ordinal()]);
    //            qi.setVertexNormal(1, normCorner[HorizontalCorner.find(HorizontalFace.values()[side.ordinal()], HorizontalFace.values()[side.ordinal()].getLeft()).ordinal()]);
                qi.setVertexNormal(1, normCorner[HorizontalCorner.find(side, side.getLeft()).ordinal()]);
                qi.setVertexNormal(2, normCenter);
                
                rawQuads.add(qi);
    
                qi = quadInputsCenterRight[side.ordinal()];
    //            qi.setVertexNormal(0, normCorner[HorizontalCorner.find(HorizontalFace.values()[side.ordinal()], HorizontalFace.values()[side.ordinal()].getRight()).ordinal()]);
                qi.setVertexNormal(0, normCorner[HorizontalCorner.find(side, side.getRight()).ordinal()]);
                qi.setVertexNormal(1, normSide[side.ordinal()]);
                qi.setVertexNormal(2, normCenter);     
                
                rawQuads.add(qi);

                //Sides
                template.setFace(side.face);
                
                RawQuad qLeft = new RawQuad(template);
                qLeft.setupFaceQuad(
                        new FaceVertex(0, bottom, 0),
                        new FaceVertex(0.5, bottom, 0),
                        new FaceVertex(0.5, flowState.getMidSideVertexHeight(side) - flowState.getYOffset(), 0),
                        new FaceVertex(0, flowState.getMidCornerVertexHeight(HorizontalCorner.find(side, side.getRight())) - flowState.getYOffset(), 0),
                        EnumFacing.UP);
                rawQuads.add(qLeft);
    
                RawQuad qRight = new RawQuad(template);
                qRight.setupFaceQuad(
                        new FaceVertex(0.5, bottom, 0),
                        new FaceVertex(1, bottom, 0),
                        new FaceVertex(1, flowState.getMidCornerVertexHeight(HorizontalCorner.find(side, side.getLeft())) - flowState.getYOffset(), 0),
                        new FaceVertex(0.5, flowState.getMidSideVertexHeight(side) - flowState.getYOffset(), 0),
                        EnumFacing.UP);
                rawQuads.add(qRight);
            }
        }     
        
        // Bottom face
        template.setFace(EnumFacing.DOWN);
        RawQuad qBottom = new RawQuad(template);
        qBottom.setupFaceQuad(0, 0, 1, 1, bottom, EnumFacing.NORTH);
        rawQuads.add(qBottom);
        

        CSGShape cubeQuads = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0, 0, 0, 1, 1, 1), template));

        rawQuads = rawQuads.intersect(cubeQuads);
 
        // don't count quads as face quads unless actually on the face
        // will be useful for face culling
        rawQuads.forEach((quad) -> quad.setFace(quad.isOnFace(quad.getFace()) ? quad.getFace() : null));        
        
        // if we end up with an empty list, default to standard cube
        if(rawQuads.isEmpty())
        {            
            rawQuads.add(template.clone().setupFaceQuad(EnumFacing.UP, 0, 0, 1, 1, 0, EnumFacing.NORTH));
            rawQuads.add(template.clone().setupFaceQuad(EnumFacing.NORTH, 0, 0, 1, 1, 0, EnumFacing.UP));
            rawQuads.add(template.clone().setupFaceQuad(EnumFacing.SOUTH, 0, 0, 1, 1, 0, EnumFacing.UP));
            rawQuads.add(template.clone().setupFaceQuad(EnumFacing.EAST, 0, 0, 1, 1, 0, EnumFacing.UP));
            rawQuads.add(template.clone().setupFaceQuad(EnumFacing.WEST, 0, 0, 1, 1, 0, EnumFacing.UP));
            rawQuads.add(template.clone().setupFaceQuad(EnumFacing.DOWN, 0, 0, 1, 1, 0, EnumFacing.NORTH));
        }
        
        return rawQuads;
    }

    @Override
    public ICollisionHandler getCollisionHandler()
    {
        return enableCollision ? this : null;
    }

    @Override
    public long getCollisionKey(IBlockState state, World worldIn, BlockPos pos)
    {
        Block block = state.getBlock();
        if(block instanceof IFlowBlock && block instanceof NiceBlock)
        {
            return ((NiceBlock) block).getModelStateKey(state, worldIn, pos);
        }
        else
        {
            return 0;
        }
    }

    @Override
    public List<AxisAlignedBB> getModelBounds(IBlockState state, World worldIn, BlockPos pos)
    {
        Block block = state.getBlock();
        if(block instanceof IFlowBlock && block instanceof NiceBlock)
        {
            
            return CollisionBoxGenerator.makeCollisionBox(
                    makeRawQuads(((NiceBlock) block).dispatcher.getStateSet().getSetValueFromBits(getCollisionKey(state, worldIn, pos))));
        }
        else
        {
            return Collections.emptyList();
        }
    }


    @Override
    public int getKeyBitLength()
    {
        return ModelStateComponents.FLOW_JOIN.getBitLength();
    }
}
