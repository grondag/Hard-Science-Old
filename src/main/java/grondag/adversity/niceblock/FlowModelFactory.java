package grondag.adversity.niceblock;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.NeighborBlocks.HorizontalFace;
import grondag.adversity.library.model.QuadFactory;
import grondag.adversity.library.model.QuadFactory.FaceVertex;
import grondag.adversity.library.model.QuadFactory.QuadInputs;
import grondag.adversity.niceblock.FlowController.FlowHeightState;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.base.ModelState;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.IColorProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public class FlowModelFactory extends ModelFactory
{

    private final FlowController myController;
    
    public FlowModelFactory(ModelController controller)
    {
        super(controller);
        this.myController = (FlowController)controller;
    }

    private float getCornerHeight(FlowHeightState flowState, HorizontalFace face1, HorizontalFace face2)
    {
        int side1 = flowState.getSideHeight(face1);
        int side2 = flowState.getSideHeight(face2);
        int corner = flowState.getCornerHeight(HorizontalCorner.find(face1, face2));
        
//        if(corner <= 16 && (side1 > 16  || side2 > 16))
//        {
//            return 1;
//        }
//        else
        {
//            return Math.max(0, (float) (flowState.getCornerHeight(HorizontalCorner.find(face1, face2)) + flowState.getCenterHeight()
//            + side1 + side2) / 64f);
            
            return (float) (corner + flowState.getCenterHeight()
            + side1 + side2) / 64f;
        }
        
        
        
    }
    
    @Override
    public List<BakedQuad> getFaceQuads(ModelState modelState, IColorProvider colorProvider, EnumFacing face)
    {
        if (face == null) return QuadFactory.EMPTY_QUAD_LIST;

        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
        long clientShapeIndex = modelState.getShapeIndex(controller.getRenderLayer());
        QuadInputs quadInputs = new QuadInputs();
        ColorMap colorMap = colorProvider.getColor(modelState.getColorIndex());
        quadInputs.color = colorMap.getColorMap(EnumColorMap.BASE);
          quadInputs.lockUV = true;
        quadInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(controller.getTextureName(myController.getAltTextureFromModelIndex(clientShapeIndex)));
        quadInputs.side = face;
        quadInputs.lightingMode = myController.lightingMode;

        FlowHeightState flowState = myController.getFlowHeightStateFromModelIndex(clientShapeIndex);
        
        // egregious hackery
        if(flowState.getSideHeight(HorizontalFace.NORTH) == 49)
        {
            return builder.build();
        }
        
        // get vertex heights
        float hCenter = (float) flowState.getCenterHeight() / 16f;

        
        float hNorth = (float) (flowState.getSideHeight(HorizontalFace.NORTH) + flowState.getCenterHeight()) / 32f;
        float hSouth = (float) (flowState.getSideHeight(HorizontalFace.SOUTH) + flowState.getCenterHeight()) / 32f;
        float hEast = (float) (flowState.getSideHeight(HorizontalFace.EAST) + flowState.getCenterHeight()) / 32f;
        float hWest = (float) (flowState.getSideHeight(HorizontalFace.WEST) + flowState.getCenterHeight()) / 32f;
        
        float hSW = getCornerHeight(flowState, HorizontalFace.SOUTH, HorizontalFace.WEST);
        float hSE = getCornerHeight(flowState, HorizontalFace.SOUTH, HorizontalFace.EAST);
        float hNW = getCornerHeight(flowState, HorizontalFace.NORTH, HorizontalFace.WEST);
        float hNE = getCornerHeight(flowState, HorizontalFace.NORTH, HorizontalFace.EAST);
   

//        if((flowState.getSideHeight(HorizontalFace.NORTH) > 16 || flowState.getSideHeight(HorizontalFace.EAST) > 16)
//                && hNE < 1.0f) hNE = 1.0f;
//
//        if((flowState.getSideHeight(HorizontalFace.NORTH) > 16 || flowState.getSideHeight(HorizontalFace.WEST) > 16)
//                && hNE < 1.0f) hNW = 1.0f;
//
//        if((flowState.getSideHeight(HorizontalFace.SOUTH) > 16 || flowState.getSideHeight(HorizontalFace.EAST) > 16)
//                && hNE < 1.0f) hSE = 1.0f;
//
//        if((flowState.getSideHeight(HorizontalFace.SOUTH) > 16 || flowState.getSideHeight(HorizontalFace.WEST) > 16)
//                && hNE < 1.0f) hSW = 1.0f;
        
// normal calculation for later reference - assumes a quad, but works for triangle also
        //Vec3d normal = (vertex[2].subtract(vertex[0]).crossProduct(vertex[3].subtract(vertex[1]))).normalize();

        switch(face)
        {
        case UP:
            
            // nine vertices, up to eight polygons
            FaceVertex fvCenter = new FaceVertex(0.5, 0.5, 1.0-hCenter);
            
            FaceVertex fvNorth = new FaceVertex(0.5, 1, 1.0-hNorth);
            FaceVertex fvSouth = new FaceVertex(0.5, 0, 1.0-hSouth);
            FaceVertex fvEast = new FaceVertex(1, 0.5, 1.0-hEast);
            FaceVertex fvWest = new FaceVertex(0, 0.5, 1.0-hWest);
  
            FaceVertex fvNE = new FaceVertex(1, 1, 1.0-hNE);
            FaceVertex fvNW = new FaceVertex(0, 1, 1.0-hNW);
            FaceVertex fvSE = new FaceVertex(1, 0, 1.0-hSE);
            FaceVertex fvSW = new FaceVertex(0, 0, 1.0-hSW);
            
            // W to SW
            QuadInputs qiW_SW = quadInputs.clone();
            qiW_SW.setupFaceQuad(
                    fvWest,
                    fvSW,
                    fvCenter,
                    fvCenter,
                    EnumFacing.NORTH);

            // SW to S
            QuadInputs qiSW_S = quadInputs.clone();
            qiSW_S.setupFaceQuad(
                    fvSW,
                    fvSouth,
                    fvCenter,
                    fvCenter,
                    EnumFacing.NORTH);
            
            // S to SE
            QuadInputs qiS_SE = quadInputs.clone();
            qiS_SE.setupFaceQuad(
                    fvSouth,
                    fvSE,
                    fvCenter,
                    fvCenter,
                    EnumFacing.NORTH);
            
            // SE to E
            QuadInputs qiSE_E = quadInputs.clone();
            qiSE_E.setupFaceQuad(
                    fvSE,
                    fvEast,
                    fvCenter,
                    fvCenter,
                    EnumFacing.NORTH);
            
            // E to NE
            QuadInputs qiE_NE = quadInputs.clone();
            qiE_NE.setupFaceQuad(
                    fvEast,
                    fvNE,
                    fvCenter,
                    fvCenter,
                    EnumFacing.NORTH);
            
            // NE to N
            QuadInputs qiNE_N = quadInputs.clone();
            qiNE_N.setupFaceQuad(
                    fvNE,
                    fvNorth,
                    fvCenter,
                    fvCenter,
                    EnumFacing.NORTH);
            
            // N to NW
            QuadInputs qiN_NW = quadInputs.clone();
            qiN_NW.setupFaceQuad(
                    fvNorth,
                    fvNW,
                    fvCenter,
                    fvCenter,
                    EnumFacing.NORTH);
            
            // NW to W
            QuadInputs qiNW_W = quadInputs.clone();
            qiNW_W.setupFaceQuad(
                    fvNW,
                    fvWest,
                    fvCenter,
                    fvCenter,
                    EnumFacing.NORTH);
            
            Vec3d fnW_SW = qiW_SW.getFaceNormal();
            Vec3d fnSW_S = qiSW_S.getFaceNormal();
            Vec3d fnS_SE = qiS_SE.getFaceNormal();
            Vec3d fnSE_E = qiSE_E.getFaceNormal();
            Vec3d fnE_NE = qiE_NE.getFaceNormal();
            Vec3d fnNE_N = qiNE_N.getFaceNormal();
            Vec3d fnN_NW = qiN_NW.getFaceNormal();
            Vec3d fnNW_W = qiNW_W.getFaceNormal();
            
            Vec3d normCenter = fnW_SW.add(fnSW_S).add(fnS_SE).add(fnSE_E).add(fnE_NE).add(fnNE_N).add(fnN_NW).add(fnNW_W).normalize();
            
            Vec3d normEast = fnE_NE.add(fnSE_E).normalize();
            Vec3d normWest = fnW_SW.add(fnNW_W).normalize();
            Vec3d normNorth = fnNE_N.add(fnN_NW).normalize();
            Vec3d normSouth = fnSW_S.add(fnS_SE).normalize();
            
            Vec3d normNE = fnE_NE.add(fnNE_N).normalize();
            Vec3d normNW = fnN_NW.add(fnNW_W).normalize();
            Vec3d normSE = fnS_SE.add(fnSE_E).normalize();
            Vec3d normSW = fnW_SW.add(fnSW_S).normalize();

            
            qiW_SW.vertex[0].setNormal(normWest);
            qiW_SW.vertex[1].setNormal(normSW);
            qiW_SW.vertex[2].setNormal(normCenter);
            qiW_SW.vertex[3].setNormal(normCenter);
            
            qiSW_S.vertex[0].setNormal(normSW);
            qiSW_S.vertex[1].setNormal(normSouth);
            qiSW_S.vertex[2].setNormal(normCenter);
            qiSW_S.vertex[3].setNormal(normCenter);

            qiS_SE.vertex[0].setNormal(normSouth);
            qiS_SE.vertex[1].setNormal(normSE);
            qiS_SE.vertex[2].setNormal(normCenter);
            qiS_SE.vertex[3].setNormal(normCenter);

            qiSE_E.vertex[0].setNormal(normSE);
            qiSE_E.vertex[1].setNormal(normEast);
            qiSE_E.vertex[2].setNormal(normCenter);
            qiSE_E.vertex[3].setNormal(normCenter);

            qiE_NE.vertex[0].setNormal(normEast);
            qiE_NE.vertex[1].setNormal(normNE);
            qiE_NE.vertex[2].setNormal(normCenter);
            qiE_NE.vertex[3].setNormal(normCenter);

            qiNE_N.vertex[0].setNormal(normNE);
            qiNE_N.vertex[1].setNormal(normNorth);
            qiNE_N.vertex[2].setNormal(normCenter);
            qiNE_N.vertex[3].setNormal(normCenter);

            qiN_NW.vertex[0].setNormal(normNorth);
            qiN_NW.vertex[1].setNormal(normNW);
            qiN_NW.vertex[2].setNormal(normCenter);
            qiN_NW.vertex[3].setNormal(normCenter);

            qiNW_W.vertex[0].setNormal(normNW);
            qiNW_W.vertex[1].setNormal(normWest);
            qiNW_W.vertex[2].setNormal(normCenter);
            qiNW_W.vertex[3].setNormal(normCenter);

            builder.add(qiW_SW.createNormalQuad());
            builder.add(qiSW_S.createNormalQuad());
            builder.add(qiS_SE.createNormalQuad());
            builder.add(qiSE_E.createNormalQuad());
            builder.add(qiE_NE.createNormalQuad());
            builder.add(qiNE_N.createNormalQuad());
            builder.add(qiN_NW.createNormalQuad());
            builder.add(qiNW_W.createNormalQuad());
            
            builder.add(qiW_SW.createNormalQuad());
            builder.add(qiSW_S.createNormalQuad());
            builder.add(qiS_SE.createNormalQuad());
            builder.add(qiSE_E.createNormalQuad());
            builder.add(qiE_NE.createNormalQuad());
            builder.add(qiNE_N.createNormalQuad());
            builder.add(qiN_NW.createNormalQuad());
            builder.add(qiNW_W.createNormalQuad());

            
//            //test if vertices are coplanar
//            
//            Vector3f vA = new Vector3f(0, hSW, 0);
//            vA.sub(new Vector3f(1, hSE, 0));
// 
//            Vector3f vB = new Vector3f(1, hNE, 1);
//            vB.sub(new Vector3f(0, hNW, 1));
//
//            Vector3f vAxB = new Vector3f();
//            vAxB.cross(vA, vB);
//            
//            Vector3f vC = new Vector3f(1, hNE, 1);
//            vC.sub(new Vector3f(0, hSE, 0));
//
//            if(vC.dot(vAxB) == 0)
//            {
//                // co-planar - single quad
//                quadInputs.setupFaceQuad(
//                        new FaceVertex(0, 0, 1.0-hSW),
//                        new FaceVertex(1, 0, 1.0-hSE),
//                        new FaceVertex(1, 1, 1.0-hNE),
//                        new FaceVertex(0, 1, 1.0-hNW),
//                        EnumFacing.NORTH);
//                builder.add(quadInputs.createNormalQuad());            }
//            else
//            {
//                if((hNE + hSW) > (hNW + hSE))
//                {
//                    quadInputs.setupFaceQuad(
//                            new FaceVertex(0, 0, 1.0-hSW),
//                            new FaceVertex(1, 0, 1.0-hSE),
//                            new FaceVertex(0, 1, 1.0-hNW),
//                            new FaceVertex(0, 1, 1.0-hNW),
//                            EnumFacing.NORTH);
//                    builder.add(quadInputs.createNormalQuad());
//                    
//                    quadInputs.setupFaceQuad(
//                            new FaceVertex(1, 0, 1.0-hSE),
//                            new FaceVertex(1, 0, 1.0-hSE),
//                            new FaceVertex(1, 1, 1.0-hNE),
//                            new FaceVertex(0, 1, 1.0-hNW),
//                            EnumFacing.NORTH);
//                    builder.add(quadInputs.createNormalQuad());
//                }
//                else
//                {
//                    quadInputs.setupFaceQuad(
//                            new FaceVertex(0, 0, 1.0-hSW),
//                            new FaceVertex(1, 0, 1.0-hSE),
//                            new FaceVertex(1, 1, 1.0-hNE),
//                            new FaceVertex(1, 1, 1.0-hNE),
//                            EnumFacing.NORTH);
//                    builder.add(quadInputs.createNormalQuad());
//                    
//                    quadInputs.setupFaceQuad(
//                            new FaceVertex(0, 0, 1.0-hSW),
//                            new FaceVertex(0.0, 0.0, 1.0-hSW),
//                            new FaceVertex(1.0, 1.0, 1.0-hNE),
//                            new FaceVertex(0, 1, 1.0-hNW),
//                            EnumFacing.NORTH);
//                    builder.add(quadInputs.createNormalQuad());
//                }
//            }
             break;
             
        case EAST:
//            quadInputs.setupFaceQuad(
//                    new FaceVertex(0, 0, 0),
//                    new FaceVertex(0.5, 0, 0),
//                    new FaceVertex(0.5, hEast, 0),
//                    new FaceVertex(0, hSE, 0),
//                    EnumFacing.UP);
//            builder.add(quadInputs.createNormalQuad());
//            
//            quadInputs.setupFaceQuad(
//                    new FaceVertex(0.5, 0, 0),
//                    new FaceVertex(1, 0, 0),
//                    new FaceVertex(1, hNE, 0),
//                    new FaceVertex(0.5, hEast, 0),
//                    EnumFacing.UP);
//            builder.add(quadInputs.createNormalQuad());        
            
            break;
            
        case NORTH:
//            quadInputs.setupFaceQuad(
//                    new FaceVertex(0, 0, 0),
//                    new FaceVertex(0.5, 0, 0),
//                    new FaceVertex(0.5, hNorth, 0),
//                    new FaceVertex(0, hNE, 0),
//                    EnumFacing.UP);
//            builder.add(quadInputs.createNormalQuad());
//            
//            quadInputs.setupFaceQuad(
//                    new FaceVertex(0.5, 0, 0),
//                    new FaceVertex(1, 0, 0),
//                    new FaceVertex(1, hNW, 0),
//                    new FaceVertex(0.5, hNorth, 0),
//                    EnumFacing.UP);
//            builder.add(quadInputs.createNormalQuad());
            
            break;
            
        case SOUTH:
//            quadInputs.setupFaceQuad(
//                    new FaceVertex(0, 0, 0),
//                    new FaceVertex(0.5, 0, 0),
//                    new FaceVertex(0.5, hSouth, 0),
//                    new FaceVertex(0, hSW, 0),
//                    EnumFacing.UP);
//            builder.add(quadInputs.createNormalQuad());
//            
//            quadInputs.setupFaceQuad(
//                    new FaceVertex(0.5, 0, 0),
//                    new FaceVertex(1, 0, 0),
//                    new FaceVertex(1, hSE, 0),
//                    new FaceVertex(0.5, hSouth, 0),
//                    EnumFacing.UP);
//            builder.add(quadInputs.createNormalQuad());
            
            break;
            
        case WEST:
//            quadInputs.setupFaceQuad(
//                    new FaceVertex(0, 0, 0),
//                    new FaceVertex(0.5, 0, 0),
//                    new FaceVertex(0.5, hWest, 0),
//                    new FaceVertex(0, hNW, 0),
//                    EnumFacing.UP);
//            builder.add(quadInputs.createNormalQuad());
//            
//            quadInputs.setupFaceQuad(
//                    new FaceVertex(0.5, 0, 0),
//                    new FaceVertex(1, 0, 0),
//                    new FaceVertex(1, hSW, 0),
//                    new FaceVertex(0.5, hWest, 0),
//                    EnumFacing.UP);
//            builder.add(quadInputs.createNormalQuad());
            
            break;
            
        case DOWN:
        default:
//            quadInputs.setupFaceQuad(0.0, 0.0, 1.0, 1.0, 0.0, EnumFacing.NORTH);
//            builder.add(quadInputs.createNormalQuad());
            break;
        }


        return builder.build();

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
