package grondag.hard_science.library.render;

import java.util.Collection;
import java.util.function.Function;

import javax.vecmath.Vector3f;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

public class HSOBJModelWrapper implements IModel
{

    private final OBJModel wrapped;
    
    public HSOBJModelWrapper(OBJModel wrapped)
    {
        this.wrapped = wrapped;
    }
   
    public HSOBJModelWrapper(IModel wrapped)
    {
        this((OBJModel)wrapped);
    }
    
    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        ImmutableMap.Builder<TransformType, TRSRTransformation> builder = ImmutableMap.builder();
        
        // This is borked up for the 1st and 3rd person transforms - OBJ models not needed right now so setting aside
        // Qould normally not belong here - should register transforms at item creation time and then retrieve them here.
        
        builder.put(TransformType.FIRST_PERSON_RIGHT_HAND, getTransform(  0.250f, 1.000f,  0.000f, 000, -90,  25, 0.65f));
        builder.put(TransformType.FIRST_PERSON_LEFT_HAND,  getTransform( -0.250f, 1.000f,  0.000f, 000,  90, -25, 0.65f));
        
        builder.put(TransformType.THIRD_PERSON_RIGHT_HAND, getTransform(  0.000f, 0.000f,  0.000f, 000, 000, 000, 1.0f));
        builder.put(TransformType.THIRD_PERSON_LEFT_HAND,  getTransform(  0.000f, 0.000f,  0.000f, 000, 000, 000, 1.0f));
        
        builder.put(TransformType.FIXED,                   getTransform(  0.500f, 0.500f,  0.500f, 0, 180, 0,   0.9f));
        builder.put(TransformType.GUI,                     getTransform(  0.500f, 0.500f,  0.500f, 0, 0, 45,  1.0f));
        builder.put(TransformType.GROUND,                  getTransform(  0.500f, 0.400f,  0.500f, 0, 0, 0,     0.6f));
        

        IModelState perspectiveState = new SimpleModelState(builder.build());
        
        return wrapped.bake(perspectiveState, format, bakedTextureGetter);
    }
 
    private static TRSRTransformation getTransform(float tx, float ty, float tz, float ax, float ay, float az, float s)
    {
        return new TRSRTransformation(
                new Vector3f(tx, ty, tz),
                null,
                new Vector3f(s, s, s),
                TRSRTransformation.quatFromXYZDegrees(new Vector3f(ax, ay, az)));
    }
    @Override
    public Collection<ResourceLocation> getTextures()
    {
        return this.wrapped.getTextures();
    }
    
    @Override
    public IModel process(ImmutableMap<String, String> customData)
    {
        return new HSOBJModelWrapper(this.wrapped.process(customData));
    }
    
    @Override
    public IModel retexture(ImmutableMap<String, String> textures)
    {
        return new HSOBJModelWrapper(this.wrapped.retexture(textures));
    }
}
