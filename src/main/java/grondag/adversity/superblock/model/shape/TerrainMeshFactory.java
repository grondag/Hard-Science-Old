package grondag.adversity.superblock.model.shape;

import java.util.ArrayList;
import java.util.List;

import grondag.adversity.Output;
import grondag.adversity.library.Color;
import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.library.model.quadfactory.CSGShape;
import grondag.adversity.library.model.quadfactory.FaceVertex;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.modelstate.FlowHeightState;
import grondag.adversity.niceblock.support.CollisionBoxGenerator;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.model.painter.surface.Surface;
import grondag.adversity.superblock.model.painter.surface.SurfaceTopology;
import grondag.adversity.superblock.model.painter.surface.SurfaceType;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.model.state.ModelStateFactory.StateFormat;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TerrainMeshFactory extends ShapeMeshGenerator implements ICollisionHandler
{
    private static final Surface SURFACE_TOP = new Surface(SurfaceType.LAMP, SurfaceTopology.TILED);
    private static final Surface SURFACE_SIDE = new Surface(SurfaceType.MAIN, SurfaceTopology.TILED);
    
    private static ShapeMeshGenerator filler_instance;
    private static ShapeMeshGenerator height_instnace;
    
    public static ShapeMeshGenerator getFillerFactory()
    {
        if(filler_instance == null) filler_instance = new TerrainMeshFactory(true);
        return filler_instance; 
    }
    
    public static ShapeMeshGenerator getHeightFactory()
    {
        if(height_instnace == null) height_instnace = new TerrainMeshFactory(false);
        return height_instnace; 
    }
    
    private static final AxisAlignedBB[] COLLISION_BOUNDS =
    {
        new AxisAlignedBB(0, 0, 0, 1, 1, 1),
        new AxisAlignedBB(0, 0, 0, 1, 11F/12F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 10F/12F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 9F/12F, 1),
        
        new AxisAlignedBB(0, 0, 0, 1, 8F/12F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 7F/12F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 6F/12F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 5F/12F, 1),
        
        new AxisAlignedBB(0, 0, 0, 1, 4F/12F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 3F/12F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 2F/12F, 1),
        new AxisAlignedBB(0, 0, 0, 1, 1F/12F, 1),
        
        // These aren't actually valid meta values, but prevent NPE if we get one somehow
        new AxisAlignedBB(0, 0, 0, 1, 1, 1),
        new AxisAlignedBB(0, 0, 0, 1, 1, 1),
        new AxisAlignedBB(0, 0, 0, 1, 1, 1),
        new AxisAlignedBB(0, 0, 0, 1, 1, 1)
    };
    
    private final boolean isFiller;
    
    protected TerrainMeshFactory(boolean isFiller)
    {
        super(  StateFormat.FLOW, 
                ModelState.STATE_FLAG_NEEDS_POS, 
                SURFACE_TOP, SURFACE_SIDE);
        this.isFiller = isFiller;
    }

    @Override
    public ICollisionHandler collisionHandler()
    {
        return this;
    }

    @Override
    public boolean isSpeciesUsedForHeight()
    {
        return true;
    }
    
    @Override
    public int geometricSkyOcclusion(ModelState modelState)
    {
        return modelState.getFlowState().verticalOcclusion();
    }

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
    public List<RawQuad> getShapeQuads(ModelState modelState)
    {
        CSGShape rawQuads = new CSGShape();
        RawQuad template = new RawQuad();

        template.color = Color.WHITE;
        template.lockUV = true;
        template.surface = SURFACE_TOP;
        // default - need to change for sides and bottom
        template.setFace(EnumFacing.UP);


        FlowHeightState flowState = modelState.getFlowState();

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
                qSide.surface = SURFACE_SIDE;
                qSide.setFace(side.face);
                setupUVForSide(qSide, side.face);

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
                qSide.surface = SURFACE_SIDE;
                qSide.setFace(side.face);
                setupUVForSide(qSide, side.face);

                qSide.setupFaceQuad(
                        new FaceVertex(0, bottom, 0),
                        new FaceVertex(0.5, bottom, 0),
                        new FaceVertex(0.5, flowState.getMidSideVertexHeight(side) - flowState.getYOffset(), 0),
                        new FaceVertex(0, flowState.getMidCornerVertexHeight(HorizontalCorner.find(side, side.getRight())) - flowState.getYOffset(), 0),
                        EnumFacing.UP);
                rawQuads.add(qSide);

                qSide = new RawQuad(qSide);
                qSide.surface = SURFACE_SIDE;
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
//        qBottom.minU = 14 - qBottom.minU;
//        qBottom.maxU = qBottom.minU + 2;
        qBottom.surface = SURFACE_SIDE;
        qBottom.setFace(EnumFacing.DOWN);        
        qBottom.setupFaceQuad(0, 0, 1, 1, bottom, EnumFacing.NORTH);
        rawQuads.add(qBottom);



        CSGShape cubeQuads = new CSGShape();
        cubeQuads.add(template.clone().setSurface(SURFACE_SIDE).setupFaceQuad(EnumFacing.UP, 0, 0, 1, 1, 0, EnumFacing.NORTH));
        RawQuad faceQuad = template.clone();
        
        //flip X-axis texture on bottom face
//        faceQuad.minU = 14 - faceQuad.minU;
//        faceQuad.maxU = faceQuad.minU + 2;
        
        cubeQuads.add(faceQuad.clone().setSurface(SURFACE_SIDE).setupFaceQuad(EnumFacing.DOWN, 0, 0, 1, 1, 0, EnumFacing.NORTH));

        
        cubeQuads.add(setupUVForSide(faceQuad.clone(), EnumFacing.NORTH).setSurface(SURFACE_SIDE).setupFaceQuad(EnumFacing.NORTH, 0, 0, 1, 1, 0, EnumFacing.UP));
        cubeQuads.add(setupUVForSide(faceQuad.clone(), EnumFacing.SOUTH).setSurface(SURFACE_SIDE).setupFaceQuad(EnumFacing.SOUTH, 0, 0, 1, 1, 0, EnumFacing.UP));
        cubeQuads.add(setupUVForSide(faceQuad.clone(), EnumFacing.EAST).setSurface(SURFACE_SIDE).setupFaceQuad(EnumFacing.EAST, 0, 0, 1, 1, 0, EnumFacing.UP));
        cubeQuads.add(setupUVForSide(faceQuad.clone(), EnumFacing.WEST).setSurface(SURFACE_SIDE).setupFaceQuad(EnumFacing.WEST, 0, 0, 1, 1, 0, EnumFacing.UP));

        rawQuads = rawQuads.intersect(cubeQuads);

        // don't count quads as face quads unless actually on the face
        // will be useful for face culling
        rawQuads.forEach((quad) -> quad.setFace(quad.isOnFace(quad.getNominalFace()) ? quad.getNominalFace() : null));        

        
        // scale all quads UVs according to position to match what surface painter expects
        // Any quads with a null face are assumed to be part of the top face
        
        // We want top face textures to always join irrespective of Y.
        // Other face can vary based on orthogonal dimension to break up appearance of layers.
        for(RawQuad quad : rawQuads)
        {
            EnumFacing face = quad.getNominalFace();
            if(face == null) face = EnumFacing.UP;
            
            switch(face)
            {
                case NORTH:
                {
                    int zHash = MathHelper.hash(modelState.getPosZ());
                    quad.minU = 255 - ((modelState.getPosX() + (zHash >> 16)) & 0xFF);
                    quad.maxU = quad.minU +  1;
                    quad.minV = 255 - ((modelState.getPosY() + zHash) & 0xFF);
                    quad.maxV = quad.minV + 1;
                    break;
                }
                case SOUTH:
                {
                    int zHash = MathHelper.hash(modelState.getPosZ());
                    quad.minU = (modelState.getPosX() + (zHash >> 16)) & 0xFF;
                    quad.maxU = quad.minU +  1;
                    quad.minV = 255 - ((modelState.getPosY() + zHash) & 0xFF);
                    quad.maxV = quad.minV + 1;
                    break;
                }
                case EAST:
                {
                    int xHash = MathHelper.hash(modelState.getPosX());
                    quad.minU = 255 - ((modelState.getPosZ() + (xHash >> 16)) & 0xFF);
                    quad.maxU = quad.minU +  1;
                    quad.minV = 255 - ((modelState.getPosY() + xHash) & 0xFF);
                    quad.maxV = quad.minV + 1;
                    break;
                }
                case WEST:
                {
                    int xHash = MathHelper.hash(modelState.getPosX());
                    quad.minU = (modelState.getPosZ() + (xHash >> 16)) & 0xFF;
                    quad.maxU = quad.minU +  1;
                    quad.minV = 255 - ((modelState.getPosY() + xHash) & 0xFF);
                    quad.maxV = quad.minV + 1;
                    break;
                } 
                case DOWN:
                {
                    int yHash = MathHelper.hash(modelState.getPosY());
                    quad.minU = 255 - ((modelState.getPosX() + (yHash >> 16)) & 0xFF);
                    quad.maxU = quad.minU +  1;
                    quad.minV = (modelState.getPosZ() + (yHash >> 16)) & 0xFF;
                    quad.maxV = quad.minV + 1;
                    break;
                }
                case UP:
                default:
                {
                    quad.minU = modelState.getPosX();
                    quad.maxU = quad.minU +  1;
                    quad.minV = modelState.getPosZ();
                    quad.maxV = quad.minV + 1;
                    break;
                }
            }
        }
        
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

   
    private RawQuad setupUVForSide(RawQuad quad, EnumFacing face)
    {
        
//        quad.minU = (face.getAxis() == Axis.X ? flowTex.getZ() : flowTex.getX()) * 2;
        // need to flip U on these side faces so that textures align properly
        if(face == EnumFacing.EAST || face == EnumFacing.NORTH) 
        {
            quad.minU = 16;
            quad.maxU = 0;
        }
        else
        {
            quad.minU = 0;
            quad.maxU = 16;
        }
        return quad;
//         quad.maxU = quad.minU + 2;
//        quad.minV = 14 - flowTex.getY() * 2;
//        quad.maxV = quad.minV + 2;
    }
    
    @Override
    public boolean isCube(ModelState modelState)
    {
        return modelState.getFlowState().isFullCube();
    }

    @Override
    public boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, SuperBlock block, ModelState modelState)
    {
        return false;
    }

    @Override
    public SideShape sideShape(ModelState modelState, EnumFacing side)
    {
        return modelState.getFlowState().isFullCube() ? SideShape.SOLID : SideShape.MISSING;
    }

    @Override
    public List<AxisAlignedBB> getCollisionBoxes(ModelState modelState)
    {
        //TODO: caching - very slow to regenerate each time
        return CollisionBoxGenerator.makeCollisionBoxList(getShapeQuads(modelState));
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(ModelState modelState)
    {
        try
        {
            return COLLISION_BOUNDS[modelState.getFlowState().getCenterHeight() - 1];
        }
        catch (Exception ex)
        {
            Output.info("TerrainMeshFactory recevied Collision Bounding Box check for a foreign block.");
            return Block.FULL_BLOCK_AABB;
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(ModelState modelState)
    {
        return getCollisionBoundingBox(modelState);
    }

}
