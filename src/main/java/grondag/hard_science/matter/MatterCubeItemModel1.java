package grondag.hard_science.matter;

import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import grondag.hard_science.Log;
import grondag.hard_science.library.render.QuadHelper;
import grondag.hard_science.library.varia.Color;
import grondag.hard_science.library.varia.Color.EnumHCLFailureMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MatterCubeItemModel1 implements IBakedModel
{
    ///////////////////////////////////////////////////////////////////////////////
    // STATIC MEMBERS
    ///////////////////////////////////////////////////////////////////////////////


    // below is borrowed from ForgeBlockStateV1
    private static TRSRTransformation getTransform(float tx, float ty, float tz, float ax, float ay, float az, float s)
    {
        return TRSRTransformation.blockCenterToCorner(new TRSRTransformation(
                new Vector3f(tx / 16, ty / 16, tz / 16),
                TRSRTransformation.quatFromXYZDegrees(new Vector3f(ax, ay, az)),
                new Vector3f(s, s, s),
                null));
    }

    private static final TRSRTransformation flipX = new TRSRTransformation(null, null, new Vector3f(-1, 1, 1), null);

    private static TRSRTransformation leftify(TRSRTransformation transform)
    {
        return TRSRTransformation.blockCenterToCorner(flipX.compose(TRSRTransformation.blockCornerToCenter(transform)).compose(flipX));
    }
    static
    {

//        TRSRTransformation ground = getTransform(0, 3, 0, 0, 0, 0, 0.25f);
//        TRSRTransformation fixed = getTransform(0, 0, 0, 0, 0, 0, 0.5f);
//        TRSRTransformation thirdperson_right_hand = getTransform(0, 2.5f, 0, 75, 45, 0, 0.375f);
//        TRSRTransformation thirdperson_left_hand = leftify(thirdperson_right_hand);
//        TRSRTransformation firstperson_right_hand = getTransform(0, 0, 0, 0, 45, 0, 0.4f);
//        TRSRTransformation firstperson_left_hand = getTransform(0, 0, 0, 0, 255, 0, 0.4f);
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    // INSTANCE MEMBERS
    ///////////////////////////////////////////////////////////////////////////////
    
    private final ImmutableMap<TransformType, TRSRTransformation> transforms;
    
    private final List<BakedQuad> quads;
    
    public MatterCubeItemModel1(BulkItem matterCube)
    {
        {
            ImmutableMap.Builder<TransformType, TRSRTransformation> builder = ImmutableMap.builder();
            
            float renderScale = (float) MathHelper.clamp(Math.log10(matterCube.maxNanoLiters) / 12, 0.1, 1.0);
            
            builder.put(TransformType.GUI,                     getTransform(0, 0, 0, 0, 0, 0, renderScale));
            builder.put(TransformType.GROUND,                  getTransform(0, 3, 0, 0, 0, 0, 0.25f * renderScale));
            builder.put(TransformType.FIXED,                   getTransform(0, 0, 0, 0, 0, 0, 0.5f * renderScale));
            TRSRTransformation thirdperson_right_hand = getTransform(0, 2.5f, 0, 75, 45, 0, 0.375f * renderScale);
            builder.put(TransformType.THIRD_PERSON_RIGHT_HAND, thirdperson_right_hand);
            builder.put(TransformType.THIRD_PERSON_LEFT_HAND,  leftify(thirdperson_right_hand));
            builder.put(TransformType.FIRST_PERSON_RIGHT_HAND, getTransform(0, 0, 0, 0, 45, 0, 0.4f * renderScale));
            builder.put(TransformType.FIRST_PERSON_LEFT_HAND,  getTransform(0, 0, 0, 0, 255, 0, 0.4f * renderScale));
     
            this.transforms = builder.build();
        }
        
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        // add quads for type of matter
        switch(matterCube.matter.phase())
        {
            case GAS:
                new MatterModel.Gas(matterCube.matter.color).addBakedQuads(builder);
                break;
                
            case LIQUID:
                new MatterModel.Fluid(matterCube.matter.color).addBakedQuads(builder);
                break;
                
            case SOLID:
            default:
                new MatterModel.Solid(matterCube.matter.color).addBakedQuads(builder);
                break;
        
        }
        
        Color matterColor = Color.fromRGB(matterCube.matter.color);
        Color labelColor = Color.fromHCL(
                (matterColor.HCL_H + 180) % 360, 
                 matterColor.HCL_C,
                 matterColor.HCL_L > 0.5f ? 0 : Color.HCL_MAX, 
                 EnumHCLFailureMode.REDUCE_CHROMA);
        
        new MatterModel.FormulaLeft(I18n.translateToLocal("matter.label." + matterCube.matter.systemName().toLowerCase()), 0xFF000000 | labelColor.RGB_int).addBakedQuads(builder);

        
        //        for(MatterModel model : matterCube.matter.models)
//        {
//                model.addBakedQuads(builder);
//        }
        
        // add size symbol
//        for(RawQuad raw : matterCube.cubeSize.rawQuads())
//        {
//            builder.add(QuadBakery.createBakedQuad(raw, true));
//        }

        this.quads = builder.build();
    }
    
    @Override
    public boolean isAmbientOcclusion()
    {
        return true;
    }

    @Override
    public boolean isGui3d()
    {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        Log.warn("Unsupported method call: SimpleItemModel.getParticleTexture()");
        return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel().getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return side == null ? quads : QuadHelper.EMPTY_QUAD_LIST;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    // Below is borrowed from the old IPerspectiveAwareModel.
    // I confess I'm not exactly sure how it works.
    // Seems necessary to have item model render properly in 1st person.
    public static Pair<? extends IBakedModel, Matrix4f> handlePerspective(IBakedModel model, ImmutableMap<TransformType, TRSRTransformation> transforms, TransformType cameraTransformType)
    {
        TRSRTransformation tr = transforms.get(cameraTransformType);
        Matrix4f mat = null;
        if(tr != null && !tr.equals(TRSRTransformation.identity())) mat = TRSRTransformation.blockCornerToCenter(tr).getMatrix();
        return Pair.of(model, mat);
    }
    
    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
        return handlePerspective(this, this.transforms, cameraTransformType);

    }
}