package grondag.adversity.niceblock;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.Adversity;
import grondag.adversity.library.model.quadfactory.CSGShape;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.IColorProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.modelstate.ModelState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

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

        long clientShapeIndex = modelState.getShapeIndex(controller.getRenderLayer());
        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
        makeRawQuads(clientShapeIndex).forEach((quad) -> builder.add(quad.createBakedQuad()));
        return builder.build();

    }

    @Override
    public List<BakedQuad> getItemQuads(ModelState modelState, IColorProvider colorProvider)
    {
        return getFaceQuads(modelState, colorProvider, null);
    }

    protected List<RawQuad> makeRawQuads(long clientShapeIndex)
    {
        RawQuad template = new RawQuad();
        template.lockUV = true;
        template.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(controller.getTextureName(myController.getAltTextureFromModelIndex(clientShapeIndex)));
        template.color = 0xFFFFFFFF;

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
}
