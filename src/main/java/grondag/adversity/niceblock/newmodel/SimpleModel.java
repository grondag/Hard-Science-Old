package grondag.adversity.niceblock.newmodel;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;

public class SimpleModel extends SimpleCubeModel
{
    protected final List<BakedQuad> generalQuads;
    
    public SimpleModel(List<BakedQuad>[] faceQuads, List<BakedQuad> generalQuads, boolean isShaded)
    {
        super(faceQuads, isShaded);
        this.generalQuads = generalQuads;
    }

    @Override
    public List<BakedQuad> getGeneralQuads()
    {
        return this.generalQuads;
    }
    
    public SimpleModel (IBakedModel modelIn, int color)
    {
        this(extractFaceQuads(modelIn, color), extractGeneralQuads(modelIn, color), modelIn.isAmbientOcclusion());
    }
    
    private static List<BakedQuad> extractGeneralQuads(IBakedModel modelIn, int color)
    {
        ImmutableList.Builder<BakedQuad> general = new Builder<BakedQuad>();
        for( BakedQuad quad : modelIn.getGeneralQuads())
        {
            general.add(QuadFactory.recolorVanillaQuad(quad, color));
        }
        return general.build();
    }
    
    private static List<BakedQuad>[] extractFaceQuads(IBakedModel modelIn, int color)
    {
        @SuppressWarnings("unchecked")
		ImmutableList<BakedQuad>[] faces = (ImmutableList<BakedQuad>[]) new ImmutableList[6];
        for(EnumFacing face : EnumFacing.VALUES)
        {
            ImmutableList.Builder<BakedQuad> faceList = new ImmutableList.Builder();
            for( BakedQuad quad : modelIn.getFaceQuads(face))
            {
                faceList.add(QuadFactory.recolorVanillaQuad(quad, color));
            }
            faces[face.ordinal()] = faceList.build();
        }
        return faces;
    }
}
