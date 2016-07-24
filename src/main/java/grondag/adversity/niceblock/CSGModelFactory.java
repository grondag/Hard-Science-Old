package grondag.adversity.niceblock;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.model.quadfactory.CSGShape;
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
import net.minecraft.util.math.AxisAlignedBB;

public class CSGModelFactory extends ModelFactory
{

    final ColorController myController;
    
    public CSGModelFactory(ColorController controller)
    {
        super(controller);
        this.myController = controller;
    }

    @Override
    public List<BakedQuad> getFaceQuads(ModelState modelState, IColorProvider colorProvider, EnumFacing face)
    {
        if (face != null) return QuadFactory.EMPTY_QUAD_LIST;

        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
        long clientShapeIndex = modelState.getShapeIndex(controller.getRenderLayer());
        RawQuad template = new RawQuad();
        ColorMap colorMap = colorProvider.getColor(modelState.getColorIndex());
        template.color = colorMap.getColorMap(EnumColorMap.BASE);
          template.lockUV = true;
        template.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(controller.getTextureName(myController.getAltTextureFromModelIndex(clientShapeIndex)));
  
        CSGShape quadsA = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.0, 0.0, 0.0, 0.65, 1.0, 0.65), template));
        template.color = colorMap.getColorMap(EnumColorMap.BORDER);
        CSGShape quadsB = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.0, 0.1, 0.1, 0.9, 0.9, 0.9), template));
        
        CSGShape intersection = quadsA.intersect(quadsB);
//        quadsA.forEach((quad) -> builder.add(quad.createBakedQuad()));
//        quadsB.forEach((quad) -> builder.add(quad.createBakedQuad()));
        intersection.recolor();
        intersection.forEach((quad) -> builder.add(quad.createBakedQuad()));
        
        return builder.build();
    }

    @Override
    public List<BakedQuad> getItemQuads(ModelState modelState, IColorProvider colorProvider)
    {
        return getFaceQuads(modelState, colorProvider, null);
    }

}
