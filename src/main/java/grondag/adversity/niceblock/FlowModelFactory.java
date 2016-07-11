package grondag.adversity.niceblock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.library.model.quadfactory.FaceVertex;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.library.model.quadfactory.RawTri;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.base.ModelState;
import grondag.adversity.niceblock.color.IColorProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public class FlowModelFactory extends ModelFactory
{

    private final FlowController myController;
    
    protected LoadingCache<Long, List<RawQuad>> modelCache = CacheBuilder.newBuilder().maximumSize(0xFF).build(new CacheLoader<Long, List<RawQuad>>()
    {
        public List<RawQuad> load(Long key) throws Exception
        {
            return makeRawQuads(key);
        }
    });
    
    public FlowModelFactory(ModelController controller)
    {
        super(controller);
        this.myController = (FlowController)controller;
    }
    
    private float getFarCornerHeight(FlowHeightState flowState, HorizontalCorner corner)
    {
        int heightCorner = flowState.getCornerHeight(corner);
        
        if(heightCorner == FlowController.NO_BLOCK)
        {
            int max = Math.max(Math.max(flowState.getSideHeight(corner.face1), flowState.getSideHeight(corner.face2)), flowState.getCenterHeight());
            heightCorner = (max - 1) / 16 * 16;
        }
       
        return ((float) heightCorner) / 16f;
    }
    
    /**
     * Doesn't use getFarCornerHeight because would be redundant check of neighbor heights
     */
    private float getMidCornerHeight(FlowHeightState flowState, HorizontalCorner corner)
    {
        int heightSide1 = flowState.getSideHeight(corner.face1);
        int heightSide2 = flowState.getSideHeight(corner.face2);
        int heightCorner = flowState.getCornerHeight(corner);
        
        int max = Math.max(Math.max(heightSide1, heightSide2), Math.max(heightCorner, flowState.getCenterHeight()));
        max = (max - 1) / 16 * 16;
                
        if(heightSide1 == FlowController.NO_BLOCK) heightSide1 = max;
        if(heightSide2 == FlowController.NO_BLOCK) heightSide2 = max;
        if(heightCorner == FlowController.NO_BLOCK) heightCorner = max;
        
        float numerator = flowState.getCenterHeight() + heightSide1 + heightSide2 + heightCorner;
       
        return numerator / 64f;
        
    }
    
    private float getFarSideHeight(FlowHeightState flowState, HorizontalFace face)
    {
        return flowState.getSideHeight(face) == FlowController.NO_BLOCK ? 0 : ((float)flowState.getSideHeight(face)) / 16f;
    }

    private float getMidSideHeight(FlowHeightState flowState, HorizontalFace face)
    {
        float sideHeight = flowState.getSideHeight(face) == FlowController.NO_BLOCK ? 0 : (float)flowState.getSideHeight(face);
        return (sideHeight + (float) flowState.getCenterHeight()) / 32F;
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
    public List<BakedQuad> getFaceQuads(ModelState modelState, IColorProvider colorProvider, EnumFacing face)
    {    
       // if(face == EnumFacing.UP) return Collections.emptyList();
        
        int color = colorProvider.getColor(modelState.getColorIndex()).getColorMap(EnumColorMap.BASE);
        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
        

        for(RawQuad quad : modelCache.getUnchecked(modelState.getShapeIndex(controller.getRenderLayer())))
        {
            if(quad.face == face)
            {
                // could have multiple threads attempting to colorize same quad
                synchronized(quad)
                {
                    builder.add(quad.recolor(color).createNormalQuad());
                }
            }    
        }
        return builder.build();

    }

    protected List<RawQuad> makeRawQuads(long shapeIndex)
    {
       LinkedList<RawQuad> rawQuads = new LinkedList<RawQuad>();
        
        RawQuad template = new RawQuad();
        template.lockUV = true;
        template.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(controller.getTextureName(myController.getAltTextureFromModelIndex(shapeIndex)));
        template.lightingMode = myController.lightingMode;
  
        FlowHeightState flowState = myController.getFlowHeightStateFromModelIndex(shapeIndex);
                
        // center vertex setup
        float centerHeight = (float) flowState.getCenterHeight() / 16f;
        FaceVertex fvCenter = new FaceVertex(0.5, 0.5, 1.0-centerHeight);
        
        RawQuad quadInputsCenterLeft[] = new RawQuad[4];
        RawQuad quadInputsCenterRight[] = new RawQuad[4];
        ArrayList<ArrayList<RawQuad>> quadInputsSide = new ArrayList<ArrayList<RawQuad>>(4);
        ArrayList<ArrayList<RawQuad>> quadInputsCorner = new ArrayList<ArrayList<RawQuad>>(4);

        
        ///////////////////////////////////////////////
        // set up corner heights and face vertices
        ///////////////////////////////////////////////
        
        float midCornerHeight[] = new float[HorizontalCorner.values().length];
        float farCornerHeight[] = new float[HorizontalCorner.values().length];
        
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
            midCornerHeight[corner.ordinal()] = getMidCornerHeight(flowState, corner);
            farCornerHeight[corner.ordinal()] = getFarCornerHeight(flowState, corner);
            
            fvMidCorner[corner.ordinal()].depth = 1.0-midCornerHeight[corner.ordinal()];
            fvFarCorner[corner.ordinal()].depth = 1.0-farCornerHeight[corner.ordinal()];
            
            quadInputsCorner.add(new ArrayList<RawQuad>(8));            
        }
        
        float midSideHeight[] = new float[HorizontalFace.values().length];
        float farSideHeight[] = new float[HorizontalFace.values().length];
        
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
            midSideHeight[side.ordinal()] = getMidSideHeight(flowState, side);
            farSideHeight[side.ordinal()] = getFarSideHeight(flowState, side);
            fvMidSide[side.ordinal()].depth = 1.0-midSideHeight[side.ordinal()];
            fvFarSide[side.ordinal()].depth = 1.0-farSideHeight[side.ordinal()];
            
            quadInputsSide.add(new ArrayList<RawQuad>(8));   
            
   
            // build left and right quads on the block that edge this side
            template.face = EnumFacing.UP;
            RawTri qiWork = new RawTri(template);
            qiWork.setupFaceQuad(
                    fvMidSide[side.ordinal()],
                    fvMidCorner[HorizontalCorner.find(side, side.getLeft()).ordinal()],
                    fvCenter,
                    EnumFacing.NORTH);           
            quadInputsCenterLeft[side.ordinal()] = qiWork;
            quadInputsSide.get(side.ordinal()).add(qiWork);
            quadInputsCorner.get(HorizontalCorner.find(side, side.getLeft()).ordinal()).add(qiWork);

            qiWork = new RawTri(template);
            qiWork.setupFaceQuad(
                    fvMidCorner[HorizontalCorner.find(side, side.getRight()).ordinal()],
                    fvMidSide[side.ordinal()],
                    fvCenter,
                    EnumFacing.NORTH);
            quadInputsCenterRight[side.ordinal()] = qiWork;
            quadInputsSide.get(side.ordinal()).add(qiWork);
            quadInputsCorner.get(HorizontalCorner.find(side, side.getRight()).ordinal()).add(qiWork);
            
            // side block tri that borders this block
            qiWork = new RawTri(template);
            qiWork.setupFaceQuad(
                    fvFarSide[side.ordinal()],
                    fvMidCorner[HorizontalCorner.find(side, side.getLeft()).ordinal()],
                    fvMidSide[side.ordinal()],
                    EnumFacing.NORTH);           
            quadInputsSide.get(side.ordinal()).add(qiWork);
            quadInputsCorner.get(HorizontalCorner.find(side, side.getLeft()).ordinal()).add(qiWork);

            qiWork = new RawTri(template);
            qiWork.setupFaceQuad(
                    fvMidCorner[HorizontalCorner.find(side, side.getRight()).ordinal()],
                    fvFarSide[side.ordinal()],
                    fvMidSide[side.ordinal()],
                    EnumFacing.NORTH);           
            quadInputsSide.get(side.ordinal()).add(qiWork);
            quadInputsCorner.get(HorizontalCorner.find(side, side.getRight()).ordinal()).add(qiWork);

            // side block tri that connects to corner but does not border side
            qiWork = new RawTri(template);
            qiWork.setupFaceQuad(
                    fvMidCorner[HorizontalCorner.find(side, side.getLeft()).ordinal()],
                    fvFarSide[side.ordinal()],
                    fvFarCorner[HorizontalCorner.find(side, side.getLeft()).ordinal()],
                    EnumFacing.NORTH);           
            quadInputsCorner.get(HorizontalCorner.find(side, side.getLeft()).ordinal()).add(qiWork);

            qiWork = new RawTri(template);
            qiWork.setupFaceQuad(
                    fvMidCorner[HorizontalCorner.find(side, side.getRight()).ordinal()],
                    fvFarCorner[HorizontalCorner.find(side, side.getRight()).ordinal()],
                    fvFarSide[side.ordinal()],
                    EnumFacing.NORTH);           
            quadInputsCorner.get(HorizontalCorner.find(side, side.getRight()).ordinal()).add(qiWork);
            
        }

        double bottom = Math.min(Math.min(midCornerHeight[0], midCornerHeight[1]), Math.min(midCornerHeight[2], midCornerHeight[3]));
        bottom = Math.floor(bottom - QuadFactory.EPSILON);
        
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
        
        for(HorizontalFace side: HorizontalFace.values())
        {
            RawQuad qi = quadInputsCenterLeft[side.ordinal()];
            qi.setNormal(0, normSide[side.ordinal()]);
            qi.setNormal(1, normCorner[HorizontalCorner.find(HorizontalFace.values()[side.ordinal()], HorizontalFace.values()[side.ordinal()].getLeft()).ordinal()]);
            qi.setNormal(2, normCenter);
            rawQuads.add(qi);

            qi = quadInputsCenterRight[side.ordinal()];
            qi.setNormal(0, normCorner[HorizontalCorner.find(HorizontalFace.values()[side.ordinal()], HorizontalFace.values()[side.ordinal()].getRight()).ordinal()]);
            qi.setNormal(1, normSide[side.ordinal()]);
            qi.setNormal(2, normCenter);
            rawQuads.add(qi);
        }
       
        //Add sides
        for(HorizontalFace side : HorizontalFace.values())
        {
            template.face = side.face;
            
            RawQuad qLeft = new RawQuad(template);
            qLeft.setupFaceQuad(
                    new FaceVertex(0, bottom, 0),
                    new FaceVertex(0.5, bottom, 0),
                    new FaceVertex(0.5, midSideHeight[side.ordinal()], 0),
                    new FaceVertex(0, midCornerHeight[HorizontalCorner.find(side, side.getRight()).ordinal()], 0),
                    EnumFacing.UP);
            rawQuads.add(qLeft);

            RawQuad qRight = new RawQuad(template);
            qRight.setupFaceQuad(
                    new FaceVertex(0.5, bottom, 0),
                    new FaceVertex(1, bottom, 0),
                    new FaceVertex(1, midCornerHeight[HorizontalCorner.find(side, side.getLeft()).ordinal()], 0),
                    new FaceVertex(0.5, midSideHeight[side.ordinal()], 0),
                    EnumFacing.UP);
            rawQuads.add(qRight);
        }     
        
        // Bottom face(s)
        template.face = EnumFacing.DOWN;
        RawQuad qBottom = new RawQuad(template);
        qBottom.setupFaceQuad(0, 0, 1, 1, bottom, EnumFacing.NORTH);
     //   rawQuads.add(qBottom);


        LinkedList<RawQuad> swapQuads = new LinkedList<RawQuad>();
        
        rawQuads.forEach((quad) -> swapQuads.addAll(quad.clipToFace(EnumFacing.UP, template)));
        rawQuads.clear();
        swapQuads.forEach((quad) -> rawQuads.addAll(quad.clipToFace(EnumFacing.DOWN, template)));
        
        // don't count quads as face quads unless actually on the face
        // will be useful for face culling
        rawQuads.forEach((quad) -> quad.face = quad.isOnFace(quad.face) ? quad.face : null);
        
        return rawQuads;

    }
    
    @Override
    public List<BakedQuad> getItemQuads(ModelState modelState, IColorProvider colorProvider)
    {
        ImmutableList.Builder<BakedQuad> general = new ImmutableList.Builder<BakedQuad>();
        for(EnumFacing face : EnumFacing.VALUES)
        {
            general.addAll(this.getFaceQuads(modelState, colorProvider, face));
        }        
        return general.build();
    }

}
