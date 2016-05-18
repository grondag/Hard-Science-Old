package grondag.adversity.niceblock;

import java.util.List;

import javax.vecmath.Vector3f;

import com.google.common.collect.ImmutableList;

import grondag.adversity.Adversity;
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
import net.minecraft.util.math.Vec3d;

public class FlowModelFactory extends ModelFactory
{

    private final FlowController myController;
    
    public FlowModelFactory(ModelController controller)
    {
        super(controller);
        this.myController = (FlowController)controller;
    }

    @Override
    public List<BakedQuad> getFaceQuads(ModelState modelState, IColorProvider colorProvider, EnumFacing face)
    {
        if (face == null) return QuadFactory.EMPTY_QUAD_LIST;

        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
        long clientShapeIndex = modelState.getClientShapeIndex(controller.getRenderLayer().ordinal());
        QuadInputs quadInputs = new QuadInputs();
        ColorMap colorMap = colorProvider.getColor(modelState.getColorIndex());
        quadInputs.color = colorMap.getColorMap(EnumColorMap.BASE);
          quadInputs.lockUV = true;
        quadInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(controller.getTextureName(myController.getAltTextureFromModelIndex(clientShapeIndex)));
        quadInputs.side = face;

        
        // normal calculation for later reference - assumes a quad, but works for triangle also
        //Vec3d normal = (vertex[2].subtract(vertex[0]).crossProduct(vertex[3].subtract(vertex[1]))).normalize();

        switch(face)
        {
        case UP:
            
            float hSW = myController.getCornerHeightFromModelIndex(clientShapeIndex, HorizontalCorner.SOUTH_WEST);
            float hSE = myController.getCornerHeightFromModelIndex(clientShapeIndex, HorizontalCorner.SOUTH_EAST);
            float hNE = myController.getCornerHeightFromModelIndex(clientShapeIndex, HorizontalCorner.NORTH_EAST);
            float hNW = myController.getCornerHeightFromModelIndex(clientShapeIndex, HorizontalCorner.NORTH_WEST);
     
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
        ImmutableList.Builder<BakedQuad> general = new ImmutableList.Builder<BakedQuad>();
        for(EnumFacing face : EnumFacing.VALUES)
        {
            general.addAll(this.getFaceQuads(modelState, colorProvider, face));
        }        
        return general.build();
    }

}
