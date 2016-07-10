package grondag.adversity.niceblock;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.base.ModelState;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.IColorProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

public class HeightModelFactory extends ModelFactory
{

    private final HeightController myController;

    public HeightModelFactory(ModelController controller)
    {
        super(controller);
        this.myController = (HeightController)controller;
    }


    @Override
    public List<BakedQuad> getFaceQuads(ModelState modelState, IColorProvider colorProvider, EnumFacing face)
    {
        if (face == null) return QuadFactory.EMPTY_QUAD_LIST;

        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
        long clientShapeIndex = modelState.getShapeIndex(controller.getRenderLayer());
        RawQuad quadInputs = new RawQuad();
        ColorMap colorMap = colorProvider.getColor(modelState.getColorIndex());
        quadInputs.color = colorMap.getColorMap(EnumColorMap.BASE);
          quadInputs.lockUV = true;
        quadInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(controller.getTextureName(myController.getAltTextureFromModelIndex(clientShapeIndex)));
        quadInputs.side = face;

        double height = (myController.getRenderHeight(clientShapeIndex) + 1) / 16.0;
        
  

        switch(face)
        {
        case UP:
            
            quadInputs.setupFaceQuad(
                    0.0,
                    0.0,
                    1.0,
                    1.0,
                    1-height,
                    EnumFacing.NORTH);
            builder.add(quadInputs.createNormalQuad());
            break;
             
        case EAST:
        case WEST:
        case NORTH:
        case SOUTH:
            quadInputs.setupFaceQuad(
                    0.0,
                    0.0,
                    1.0, 
                    height,
                    0.0,
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
