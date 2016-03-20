package grondag.adversity.niceblock.newmodel;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

public class SimpleQuadProvider implements IQuadProvider
{
    private final List<BakedQuad>[] quads;
 
    protected SimpleQuadProvider(List<BakedQuad>[] quads)
    {
        this.quads = quads;
    }

    @Override
    public List<BakedQuad> getQuads(EnumFacing face)
    {
    	return face == null ? quads[6] : quads[face.ordinal()];
    }
}
