package grondag.adversity.niceblock.model;

import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.ImmutableList;

import grondag.adversity.Adversity;
import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.library.model.QuadContainer;
import grondag.adversity.library.model.quadfactory.CSGShape;
import grondag.adversity.library.model.quadfactory.FaceVertex;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.base.IFlowBlock;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.niceblock.modelstate.ModelFlowTexComponent;
import grondag.adversity.niceblock.modelstate.ModelFlowTexComponent.FlowTexValue;
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
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;

public class FlowModelFactory extends ModelFactory<ModelFactory.ModelInputs>
{
    private final boolean enableCollision;

    public FlowModelFactory(ModelInputs modelInputs, boolean enableCollision, ModelStateComponent<?,?>... components) 
    {
        super(modelInputs, components);
        this.enableCollision = enableCollision;
    }

    @Override
    public String buildTextureName(String baseName, int offset)
    {
        return "adversity:blocks/" + baseName;
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
        //TODO: remove
        if(state == null || this.colorComponent == null)
        {
            Adversity.log.warn("derp!");
        }
        template.color = state.getValue(this.colorComponent).getColor(EnumColorMap.BASE);
        template.lockUV = true;
        //        template.rotation = state.getValue(this.rotationComponent);
        template.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                buildTextureName(modelInputs.textureName, state.getValue(textureComponent)));
        template.lightingMode = modelInputs.lightingMode;
        // default - need to change for sides and bottom
        template.setFace(EnumFacing.UP);

        ModelFlowTexComponent.FlowTexValue flowTex = state.getValue(this.flowTexComponent);
        template.minU = flowTex.getX() * 2;
        template.maxU = template.minU + 2;
        template.minV = flowTex.getZ() * 2;
        template.maxV = template.minV + 2;

        FlowHeightState flowState = state.getValue(ModelStateComponents.FLOW_JOIN);

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




        //single top face if it is relatively flat and all sides can be drawn without a mid vertex
        if(flowState.isTopSimple())
        {
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
            if(flowState.isSideSimple(side))
            {
                // top
                if(!flowState.isTopSimple())
                {
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
                RawQuad qSide = new RawQuad(template);
                qSide.setFace(side.face);
                setupUVForSide(qSide, flowTex, side.face);

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
                RawQuad qSide = new RawQuad(template);
                qSide.setFace(side.face);
                setupUVForSide(qSide, flowTex, side.face);

                qSide.setupFaceQuad(
                        new FaceVertex(0, bottom, 0),
                        new FaceVertex(0.5, bottom, 0),
                        new FaceVertex(0.5, flowState.getMidSideVertexHeight(side) - flowState.getYOffset(), 0),
                        new FaceVertex(0, flowState.getMidCornerVertexHeight(HorizontalCorner.find(side, side.getRight())) - flowState.getYOffset(), 0),
                        EnumFacing.UP);
                rawQuads.add(qSide);

                qSide = new RawQuad(qSide);
                qSide.setFace(side.face);
                qSide.setupFaceQuad(
                        new FaceVertex(0.5, bottom, 0),
                        new FaceVertex(1, bottom, 0),
                        new FaceVertex(1, flowState.getMidCornerVertexHeight(HorizontalCorner.find(side, side.getLeft())) - flowState.getYOffset(), 0),
                        new FaceVertex(0.5, flowState.getMidSideVertexHeight(side) - flowState.getYOffset(), 0),
                        EnumFacing.UP);
                rawQuads.add(qSide);
            }
        }     

        // Bottom face
        RawQuad qBottom = new RawQuad(template);
        //flip X-axis texture on bottom face
        qBottom.minU = 14 - qBottom.minU;
        qBottom.maxU = qBottom.minU + 2;
        qBottom.setFace(EnumFacing.DOWN);        
        qBottom.setupFaceQuad(0, 0, 1, 1, bottom, EnumFacing.NORTH);
        rawQuads.add(qBottom);



        CSGShape cubeQuads = new CSGShape();
        cubeQuads.add(template.clone().setupFaceQuad(EnumFacing.UP, 0, 0, 1, 1, 0, EnumFacing.NORTH));
        RawQuad faceQuad = template.clone();
        
        //flip X-axis texture on bottom face
        faceQuad.minU = 14 - faceQuad.minU;
        faceQuad.maxU = faceQuad.minU + 2;
        
        cubeQuads.add(faceQuad.clone().setupFaceQuad(EnumFacing.DOWN, 0, 0, 1, 1, 0, EnumFacing.NORTH));

        setupUVForSide(faceQuad, flowTex, EnumFacing.NORTH);
        cubeQuads.add(faceQuad.clone().setupFaceQuad(EnumFacing.NORTH, 0, 0, 1, 1, 0, EnumFacing.UP));
        setupUVForSide(faceQuad, flowTex, EnumFacing.SOUTH);
        cubeQuads.add(faceQuad.clone().setupFaceQuad(EnumFacing.SOUTH, 0, 0, 1, 1, 0, EnumFacing.UP));
        
        setupUVForSide(faceQuad, flowTex, EnumFacing.EAST);
        cubeQuads.add(faceQuad.clone().setupFaceQuad(EnumFacing.EAST, 0, 0, 1, 1, 0, EnumFacing.UP));
        setupUVForSide(faceQuad, flowTex, EnumFacing.WEST);
        cubeQuads.add(faceQuad.clone().setupFaceQuad(EnumFacing.WEST, 0, 0, 1, 1, 0, EnumFacing.UP));

        rawQuads = rawQuads.intersect(cubeQuads);

        // don't count quads as face quads unless actually on the face
        // will be useful for face culling
        rawQuads.forEach((quad) -> quad.setFace(quad.isOnFace(quad.getFace()) ? quad.getFace() : null));        

        // Removed: if we end up with an empty list, default to standard cube
        // Removed because block now behaves like air if this happens somehow.
        //        if(rawQuads.isEmpty())
        //        {            
        //            rawQuads.add(template.clone().setupFaceQuad(EnumFacing.UP, 0, 0, 1, 1, 0, EnumFacing.NORTH));
        //            rawQuads.add(template.clone().setupFaceQuad(EnumFacing.NORTH, 0, 0, 1, 1, 0, EnumFacing.UP));
        //            rawQuads.add(template.clone().setupFaceQuad(EnumFacing.SOUTH, 0, 0, 1, 1, 0, EnumFacing.UP));
        //            rawQuads.add(template.clone().setupFaceQuad(EnumFacing.EAST, 0, 0, 1, 1, 0, EnumFacing.UP));
        //            rawQuads.add(template.clone().setupFaceQuad(EnumFacing.WEST, 0, 0, 1, 1, 0, EnumFacing.UP));
        //            rawQuads.add(template.clone().setupFaceQuad(EnumFacing.DOWN, 0, 0, 1, 1, 0, EnumFacing.NORTH));
        //        }

        return rawQuads;
    }

    private void setupUVForSide(RawQuad quad, FlowTexValue flowTex, EnumFacing face)
    {
        quad.minU = (face.getAxis() == Axis.X ? flowTex.getZ() : flowTex.getX()) * 2;
        // need to flip U on these side faces so that textures align properly
        if(face == EnumFacing.EAST || face == EnumFacing.NORTH) quad.minU = 14 - quad.minU;
        quad.maxU = quad.minU + 2;
        quad.minV = 14 - flowTex.getY() * 2;
        quad.maxV = quad.minV + 2;
    }
    
    @Override
    public ICollisionHandler getCollisionHandler(ModelDispatcher dispatcher)
    {
        return enableCollision ? new FlowCollisionHandler(dispatcher) : null;
    }

    public class FlowCollisionHandler implements ICollisionHandler
    {
        private final ModelDispatcher dispatcher;
        
        private FlowCollisionHandler(ModelDispatcher dispatcher)
        {
            this.dispatcher = dispatcher;
        }
        
        @Override
        public long getCollisionKey(IBlockState state, IBlockAccess worldIn, BlockPos pos)
        {
            Block block = state.getBlock();
            if(IFlowBlock.isFlowBlock(block))
            {
                return ((NiceBlock) block).getModelStateKey(state, worldIn, pos);
            }
            else
            {
                return 0;
            }
        }
    
        @Override
        public List<AxisAlignedBB> getModelBounds(long collisionKey)
        {
                return CollisionBoxGenerator.makeCollisionBox(
                        makeRawQuads(dispatcher.getStateSet().getSetValueFromBits(collisionKey)));
        }
    
        @Override
        public int getKeyBitLength()
        {
            return ModelStateComponents.FLOW_JOIN.getBitLength();
        }
    }
}
