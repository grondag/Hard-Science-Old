package grondag.hard_science.superblock.model.shape;
/**
 * Kept for salvage purposes - didn't want to dig back through source history.
 * Should be deleted when mined out.
 */
//package grondag.hard_science.niceblock.model;
//
//import java.util.List;
//
//import com.google.common.collect.ImmutableList;
//
//import grondag.hard_science.library.model.QuadContainer;
//import grondag.hard_science.library.model.quadfactory.CSGShape;
//import grondag.hard_science.library.model.quadfactory.LightingMode;
//import grondag.hard_science.library.model.quadfactory.QuadFactory;
//import grondag.hard_science.library.model.quadfactory.RawQuad;
//import grondag.hard_science.niceblock.base.ModelFactory;
//import grondag.hard_science.niceblock.color.ColorMap;
//import grondag.hard_science.niceblock.color.ColorMap.EnumColorMap;
//import grondag.hard_science.niceblock.model.texture.TextureProviders;
//import grondag.hard_science.niceblock.model.texture.TextureProvider.Texture.TextureState;
//import grondag.hard_science.niceblock.modelstate.ModelStateComponent;
//import grondag.hard_science.niceblock.modelstate.ModelStateComponents;
//import grondag.hard_science.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
//import grondag.hard_science.niceblock.support.SimpleCollisionHandler;
//import grondag.hard_science.superblock.model.shape.ModelShape;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.block.model.BakedQuad;
//import net.minecraft.util.BlockRenderLayer;
//import net.minecraft.util.math.Vec3d;
//
//public class CSGModelFactory extends ModelFactory
//{
//    public static SimpleCollisionHandler makeCollisionHandler()
//    {
//        CSGModelFactory factory = new CSGModelFactory(ModelStateComponents.COLORS_WHITE,
//                ModelStateComponents.TEXTURE_1, ModelStateComponents.ROTATION_NONE);
//        return new SimpleCollisionHandler(factory);
//    }
//    
//    public CSGModelFactory(ModelStateComponent<?,?>... components)
//    {
//        super(ModelShape.SPHERE, components);
//    }
//    
//    private static TextureState DEFAULT_TEXTURE_STATE 
//        = TextureProviders.BLOCK_INDIVIDUAL.get(0).getTextureState(false, LightingMode.SHADED, BlockRenderLayer.SOLID);
//    
//    protected List<RawQuad> makeRawQuads(TextureState texState, ModelStateSetValue state)
//    {
//        RawQuad template = new RawQuad();
//        template.lockUV = true;
//        ColorMap borderColor = state.getValue(colorComponent);
//        template.color = borderColor.getColor(EnumColorMap.BASE);
//        template.rotation = state.getValue(rotationComponent);
//        template.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
//                texState.buildTextureName(state.getValue(textureComponent)));
//        template.lightingMode = texState.lightingMode;
//    
//       // CSGShape  delta = null;
//        CSGShape result = null;
//        
//        //union opposite overlapping coplanar faces
////        result = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0, .4, .5, 1, 1, 1), template));
////        delta = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(.3, 0, 0, .7, .6, .5), template));
////        result = result.union(delta);
//        
//        //union opposite overlapping coplanar faces created by diff
////        result = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.1, 0.1, 0.1, 0.9, 0.9, 0.9), template));
////        delta = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.3, 0.03, 0.5, 0.5, 0.95, 0.7), template));  
////        result = result.difference(delta);
////        delta = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.3, 0, 0, 0.4, .2, 1), template));
////        result = result.union(delta);
//        
//        // cylinder/cone test
////        result = new CSGShape(QuadFactory.makeCylinder(new Vec3d(.5, 0, .5), new Vec3d(.5, 1, .5), 0.5, 0, template));
//        
//        // icosahedron (sphere) test
//      result = new CSGShape(QuadFactory.makeIcosahedron(new Vec3d(.5, .5, .5), 0.5, template));
//
//        
////        CSGShape quadsA = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0, 0.4, 0.4, 1.0, 0.6, 0.6), template));
////        template.color = borderColor.getColorMap(EnumColorMap.BORDER);
////        CSGShape quadsB = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.2, 0, 0.4, 0.6, 1.0, 0.8), template));
//
////        CSGShape quadsA = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.0, 0.0, 0.0, 1, 1, 1), template));
////        template.color = borderColor.getColorMap(EnumColorMap.BORDER);
////        CSGShape quadsB = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0, .1, .45, .05, 0.9, .55), template));
//
////        CSGShape result = quadsA.intersect(quadsB);
////        CSGShape result = quadsA.union(quadsB);
////      CSGShape result = quadsA.difference(quadsB);
//
//
//      
////        
////        quadsB = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0, 0, 0.3, 1, 1, .7), template));
////        result = result.difference(quadsB);
//        
////        template.color = borderColor.getColorMap(EnumColorMap.HIGHLIGHT);
////        quadsB = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.2, 0.2, 0, 0.8, 0.8, 1), template));
////        result = result.difference(quadsB);
////
////        template.color = borderColor.getColorMap(EnumColorMap.HIGHLIGHT);
////        quadsB = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0, 0, .4, 1, .4, .65), template));
////        result = result.difference(quadsB);
//        
//    //    result.recolor();
//        
////        for(RawQuad quad : result)
////        {
////            Vec3d faceNormal = quad.getFaceNormal();
////            if(!(faceNormal.xCoord ==0 || Math.abs(Math.abs(faceNormal.xCoord) - 1) < QuadFactory.EPSILON)
////                    || !(faceNormal.yCoord ==0 || Math.abs(Math.abs(faceNormal.yCoord) - 1) < QuadFactory.EPSILON)
////                    || !(faceNormal.zCoord ==0 || Math.abs(Math.abs(faceNormal.zCoord) - 1) < QuadFactory.EPSILON)
////                    || !(Math.abs(Math.abs(faceNormal.xCoord + faceNormal.yCoord + faceNormal.zCoord) - 1) < QuadFactory.EPSILON))
////            {
////                HardScience.log.info("hmmm");
////            }
////            
////            for(int i = 0; i < 4; i++)
////            {
////                Vec3d vNorm = quad.getVertex(i).getNormal();
////                if(vNorm != null && !vNorm.equals(quad.getFaceNormal()))
////                {
////                    HardScience.log.info("hmmm");
////                }
////                if(quad.getVertex(i).hasNormal())
////                {
////                    HardScience.log.info("hmmm");
////                }
////                if(quad.getVertex(i).color != quad.color)
////                {
////                    HardScience.log.info("hmmm");
////                }
////            }
////        }
//        
//        return result;
//    }
//    
//    @Override
//    public QuadContainer getFaceQuads(TextureState texState, ModelStateSetValue state, BlockRenderLayer renderIntent)
//    {
//        if(renderIntent != texState.renderLayer) return QuadContainer.EMPTY_CONTAINER;
//        
//        return QuadContainer.fromRawQuads(this.makeRawQuads(texState, state));
//    }
//
//    @Override
//    public List<BakedQuad> getItemQuads(TextureState texState, ModelStateSetValue state)
//    {
//        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
//        for(RawQuad quad : this.makeRawQuads(texState, state))
//        {
//      
//            builder.add(quad.createBakedQuad());
//            
//        }   
//        return builder.build();
//    }
//
////    @Override
////    public AbstractCollisionHandler getCollisionHandler(ModelDispatcher dispatcher)
////    {
////        return COLLISION_HANDLER;
////    }
//
//    @Override
//    public List<RawQuad> getCollisionQuads(ModelStateSetValue state)
//    {
//        return this.makeRawQuads(DEFAULT_TEXTURE_STATE, state);
//    }
//}
