package grondag.adversity.niceblock.newmodel;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;

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
}
