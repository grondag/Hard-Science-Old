package grondag.adversity.niceblock;

import java.util.HashMap;
import java.util.List;

import javax.vecmath.Vector3f;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableList;

import grondag.adversity.library.NeighborBlocks.HorizontalCorner;
import grondag.adversity.library.model.QuadFactory;
import grondag.adversity.library.model.QuadFactory.FaceVertex;
import grondag.adversity.library.model.QuadFactory.QuadInputs;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.base.ModelState;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.IColorProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

public class NormalTestModelFactory extends ModelFactory
{

    public NormalTestModelFactory(ModelController controller)
    {
        super(controller);
    }

    @Override
    public List<BakedQuad> getFaceQuads(ModelState modelState, IColorProvider colorProvider, EnumFacing face)
    {
        if (face != null) return QuadFactory.EMPTY_QUAD_LIST;

        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
        QuadInputs quadInputs = new QuadInputs();
        int clientShapeIndex = modelState.getClientShapeIndex(controller.getRenderLayer().ordinal());
        ColorMap colorMap = colorProvider.getColor(modelState.getColorIndex());
        quadInputs.color = colorMap.getColorMap(EnumColorMap.BASE);
          quadInputs.lockUV = true;
        quadInputs.isShaded = controller.isShaded;
        quadInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(controller.getTextureName(controller.getAltTextureFromModelIndex(clientShapeIndex)));
 
        HashMap map = new MultiMap();
        
        /**
         * pyramid
         * five faces, five vertices
         * 
         * cubboid
         * six faces, eight vertices
         * 
         * cylinder
         * 18 faces, 32 vertices
         * 
         * 
         * structure
         * 
         * face has vertices and vertices have faces
         */
        
        
        switch(face)
        {
        case UP:
            
         
            //test if vertices are coplanar
            
            Vector3f vA = new Vector3f(0, hSW, 0);
            vA.sub(new Vector3f(1, hSE, 0));
 
            Vector3f vB = new Vector3f(1, hNE, 1);
            vB.sub(new Vector3f(0, hNW, 1));

            Vector3f vAxB = new Vector3f();
            vAxB.cross(vA, vB);
            
            Vector3f vC = new Vector3f(1, hNE, 1);
            vC.sub(new Vector3f(0, hSE, 0));

            if(vC.dot(vAxB) == 0)
            {
                // co-planar - single quad
                quadInputs.setupFaceQuad(
                        new FaceVertex(0, 0, 1.0-hSW),
                        new FaceVertex(1, 0, 1.0-hSE),
                        new FaceVertex(1, 1, 1.0-hNE),
                        new FaceVertex(0, 1, 1.0-hNW),
                        EnumFacing.NORTH);
                builder.add(quadInputs.createNormalQuad());            }
            else
            {
                // not co-planar, split so that seam is highest (convex face)
//                float meanHeight = (hSE + hSW + hNE + hNW) / 4;
//                
//
//                quadInputs.setupFaceQuad(
//                        new FaceVertex(0, 0, 1.0-hSW),
//                        new FaceVertex(1, 0, 1.0-hSE),
//                        new FaceVertex(0.5, 0.5, 1.0-meanHeight),
//                        new FaceVertex(0.5, 0.5, 1.0-meanHeight),
//                        EnumFacing.NORTH);
//                builder.add(quadInputs.createNormalQuad());
//                
//                quadInputs.setupFaceQuad(
//                        new FaceVertex(0.5, 0.5, 1.0-meanHeight),
//                        new FaceVertex(1, 0, 1.0-hSE),
//                        new FaceVertex(1, 1, 1.0-hNE),
//                        new FaceVertex(0.5, 0.5, 1.0-meanHeight),
//                        EnumFacing.NORTH);
//                builder.add(quadInputs.createNormalQuad());
//
//                quadInputs.setupFaceQuad(
//                        new FaceVertex(0.5, 0.5, 1.0-meanHeight),
//                        new FaceVertex(0.5, 0.5, 1.0-meanHeight),
//                        new FaceVertex(1, 1, 1.0-hNE),
//                        new FaceVertex(0, 1, 1.0-hNW),
//                        EnumFacing.NORTH);
//                builder.add(quadInputs.createNormalQuad());
//                
//                quadInputs.setupFaceQuad(
//                        new FaceVertex(0, 0, 1.0-hSW),
//                        new FaceVertex(0.5, 0.5, 1.0-meanHeight),
//                        new FaceVertex(0.5, 0.5, 1.0-meanHeight),
//                        new FaceVertex(0, 1, 1.0-hNW),
//                        EnumFacing.NORTH);
//                builder.add(quadInputs.createNormalQuad());
         
                
                if((hNE + hSW) > (hNW + hSE))
                {
                    quadInputs.setupFaceQuad(
                            new FaceVertex(0, 0, 1.0-hSW),
                            new FaceVertex(1, 0, 1.0-hSE),
                            new FaceVertex(0, 1, 1.0-hNW),
                            new FaceVertex(0, 1, 1.0-hNW),
                            EnumFacing.NORTH);
                    builder.add(quadInputs.createNormalQuad());
                    
                    quadInputs.setupFaceQuad(
                            new FaceVertex(1, 0, 1.0-hSE),
                            new FaceVertex(1, 0, 1.0-hSE),
                            new FaceVertex(1, 1, 1.0-hNE),
                            new FaceVertex(0, 1, 1.0-hNW),
                            EnumFacing.NORTH);
                    builder.add(quadInputs.createNormalQuad());
                }
                else
                {
                    quadInputs.setupFaceQuad(
                            new FaceVertex(0, 0, 1.0-hSW),
                            new FaceVertex(1, 0, 1.0-hSE),
                            new FaceVertex(1, 1, 1.0-hNE),
                            new FaceVertex(1, 1, 1.0-hNE),
                            EnumFacing.NORTH);
                    builder.add(quadInputs.createNormalQuad());
                    
                    quadInputs.setupFaceQuad(
                            new FaceVertex(0, 0, 1.0-hSW),
                            new FaceVertex(0.0, 0.0, 1.0-hSW),
                            new FaceVertex(1.0, 1.0, 1.0-hNE),
                            new FaceVertex(0, 1, 1.0-hNW),
                            EnumFacing.NORTH);
                    builder.add(quadInputs.createNormalQuad());
                }
            }
             break;
        case EAST:
            quadInputs.setupFaceQuad(
                    new FaceVertex(0, 0, 0),
                    new FaceVertex(1, 0, 0),
                    new FaceVertex(1, myController.getCornerHeightFromModelIndex(clientShapeIndex, HorizontalCorner.NORTH_EAST), 0),
                    new FaceVertex(0, myController.getCornerHeightFromModelIndex(clientShapeIndex, HorizontalCorner.SOUTH_EAST), 0),
                    EnumFacing.UP);
            builder.add(quadInputs.createNormalQuad());
            break;
        case NORTH:
            quadInputs.setupFaceQuad(
                    new FaceVertex(0, 0, 0),
                    new FaceVertex(1, 0, 0),
                    new FaceVertex(1, myController.getCornerHeightFromModelIndex(clientShapeIndex, HorizontalCorner.NORTH_WEST), 0),
                    new FaceVertex(0, myController.getCornerHeightFromModelIndex(clientShapeIndex, HorizontalCorner.NORTH_EAST), 0),
                    EnumFacing.UP);
            builder.add(quadInputs.createNormalQuad());
            break;
        case SOUTH:
            quadInputs.setupFaceQuad(
                    new FaceVertex(0, 0, 0),
                    new FaceVertex(1, 0, 0),
                    new FaceVertex(1, myController.getCornerHeightFromModelIndex(clientShapeIndex, HorizontalCorner.SOUTH_EAST), 0),
                    new FaceVertex(0, myController.getCornerHeightFromModelIndex(clientShapeIndex, HorizontalCorner.SOUTH_WEST), 0),
                    EnumFacing.UP);
            builder.add(quadInputs.createNormalQuad());
            break;
        case WEST:
            quadInputs.setupFaceQuad(
                    new FaceVertex(0, 0, 0),
                    new FaceVertex(1, 0, 0),
                    new FaceVertex(1, myController.getCornerHeightFromModelIndex(clientShapeIndex, HorizontalCorner.SOUTH_WEST), 0),
                    new FaceVertex(0, myController.getCornerHeightFromModelIndex(clientShapeIndex, HorizontalCorner.NORTH_WEST), 0),
                    EnumFacing.UP);
            builder.add(quadInputs.createNormalQuad());
            break;
        case DOWN:
        default:
            quadInputs.setupFaceQuad(0.0, 0.0, 1.0, 1.0, 0.0, EnumFacing.NORTH);
            builder.add(quadInputs.createNormalQuad());
            break;
        }


        return builder.build();
    }

    @Override
    public List<BakedQuad> getItemQuads(ModelState modelState, IColorProvider colorProvider)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
