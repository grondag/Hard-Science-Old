package grondag.adversity.library.model;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.model.quadfactory.QuadFactory;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

public class QuadContainer2
{
	public static final QuadContainer2 EMPTY_CONTAINER;
	
	static
	{
		QuadContainerBuilder builder = new QuadContainerBuilder();
		
		builder.setQuads(null, QuadFactory.EMPTY_QUAD_LIST);
		for(EnumFacing face : EnumFacing.values())
		{
			builder.setQuads(face, QuadFactory.EMPTY_QUAD_LIST);
		}
		EMPTY_CONTAINER = builder.build();
	}
	
	private final List<BakedQuad>[] quads;
 
	private QuadContainer2(List<BakedQuad>[] quads)
    {
        this.quads = quads;
    }

	public static QuadContainer2 merge(List<QuadContainer2> inputs)
	{
		@SuppressWarnings("unchecked")
		List<BakedQuad>[] newQuads = (List<BakedQuad>[]) new List[7];
		
		for(int i = 0; i < 7; i++)
		{
			ImmutableList.Builder<BakedQuad>  builder = new ImmutableList.Builder<>();
			for(QuadContainer2 qc : inputs)
			{
				if(qc.quads[i] != null) builder.addAll(qc.quads[i]);
			}
			newQuads[i] = builder.build();
		}
		return new QuadContainer2(newQuads);
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
    
    public static class QuadContainerBuilder
    {
        @SuppressWarnings("unchecked")
		private final List<BakedQuad>[] quads = (List<BakedQuad>[]) new List[7];

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
        
        public QuadContainer2 build()
        {
        	return new QuadContainer2(this.quads);
        }
    }

}
