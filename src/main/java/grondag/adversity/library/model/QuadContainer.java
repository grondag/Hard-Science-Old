package grondag.adversity.library.model;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

public class QuadContainer
{
    private final List<BakedQuad>[] quads;
 
    @SuppressWarnings("unchecked")
	public QuadContainer()
    {
        this.quads = (List<BakedQuad>[]) new List[7];
    }

    public List<BakedQuad> getQuads(EnumFacing face)
    {
    	if(face !=null)
    	{
    		return quads[face.ordinal()];
    	}
    	else
		{
			return quads[6];
		}
    }
    
    public void setQuads(EnumFacing face, List<BakedQuad> quads)
    {
    	if(face !=null)
    	{
    		this.quads[face.ordinal()] = quads;
    	}
    	else
		{
			this.quads[6] = quads;
		}
    }
}
