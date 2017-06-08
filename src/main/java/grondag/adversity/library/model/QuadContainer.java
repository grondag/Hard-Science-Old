package grondag.adversity.library.model;

import java.util.List;
import java.util.TreeSet;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.Useful;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.library.model.quadfactory.RawQuad;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class QuadContainer
{
	public static final QuadContainer EMPTY_CONTAINER;
	
	// TODO: for memory efficiency (and possibly improved LOR
	// look at storing this as a single array with offsets to the
	// start of each section of quads - maybe within sparselayermap also?
	
	// would need flyweight list objects on top of it to avoid copying array into a new allocation
	
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
	private int[] occlusionHash;
 
	private QuadContainer(List<BakedQuad>[] quads)
    {
        this.quads = quads;
    }
	
	/** bakes quads and sorts into appropriate face */
	public static QuadContainer fromRawQuads(List<RawQuad> rawQuads)
	{
	    @SuppressWarnings("unchecked")
        ImmutableList.Builder<BakedQuad>[] faces = new ImmutableList.Builder[6];
        for(EnumFacing face : EnumFacing.values())
        {
            faces[face.ordinal()] = new ImmutableList.Builder<BakedQuad>();
        }
        ImmutableList.Builder<BakedQuad> general = new ImmutableList.Builder<BakedQuad>();
        
        for(RawQuad quad : rawQuads)
        {
            if(quad.getActualFace() == null)
            {
                general.add(quad.createBakedQuad());
            }
            else
            {
                faces[quad.getActualFace().ordinal()].add(quad.createBakedQuad());
            }
        }  
  
        QuadContainer.QuadContainerBuilder containerBuilder = new QuadContainer.QuadContainerBuilder();
        containerBuilder.setQuads(null, general.build());
        for(EnumFacing face : EnumFacing.values())
        {
            containerBuilder.setQuads(face, faces[face.ordinal()].build());
        }
        return containerBuilder.build();
	}

	public static QuadContainer merge(List<QuadContainer> inputs)
	{
		@SuppressWarnings("unchecked")
		List<BakedQuad>[] newQuads = (List<BakedQuad>[]) new List[7];
		
		for(int i = 0; i < 7; i++)
		{
			ImmutableList.Builder<BakedQuad>  builder = new ImmutableList.Builder<>();
			for(QuadContainer qc : inputs)
			{
				if(qc.quads[i] != null) builder.addAll(qc.quads[i]);
			}
			newQuads[i] = builder.build();
		}
		return new QuadContainer(newQuads);
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
    
//    public boolean isEmpty()
//    {
//        boolean result = true;
//        for(int i = 0; i < 7; i++)
//        {
//            result = result && quads[i].isEmpty();
//        }
//        return result;
//    }
    
    public int getOcclusionHash(EnumFacing face)
    {
        if(this.occlusionHash == null)
        {
            this.occlusionHash = new int[EnumFacing.values().length];
            for(EnumFacing f : EnumFacing.values())
            {
                this.occlusionHash[f.ordinal()] = computeOcclusionHash(f);
            }
        }
        
        if(face == null) return 0;
        
        return this.occlusionHash[face.ordinal()];
    }
    
    private int computeOcclusionHash(EnumFacing face)
    {
        List<BakedQuad> quads = getQuads(face);
        QuadListKeyBuilder keyBuilder = new QuadListKeyBuilder(face);
        for(BakedQuad q : quads)
        {
            LightUtil.putBakedQuad(keyBuilder, q);
        }
        return keyBuilder.getQuadListKey();
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
        
        public QuadContainer build()
        {
        	return new QuadContainer(this.quads);
        }
    }

    private static class QuadListKeyBuilder implements IVertexConsumer
    {
        private final int axis0;
        private final int axis1;

        private TreeSet<Long> vertexKeys = new TreeSet<Long>();
        
        private QuadListKeyBuilder(EnumFacing face)
        {
            switch(face.getAxis())
            {
            case X:
                axis0 = 1;
                axis1 = 2;
                break;
            case Y:
                axis0 = 0;
                axis1 = 2;
                break;
            case Z:
            default:
                axis0 = 0;
                axis1 = 1;
                break;
            }
        }
        
        /** call after piping vertices into this instance */
        private int getQuadListKey()
        {
            long key = 0L;
            for(Long vk : vertexKeys)
            {
               key += Useful.longHash(vk); 
            }
            return (int)(key & 0xFFFFFFFF);     
        }
        
        @Override
        public VertexFormat getVertexFormat()
        {
            return DefaultVertexFormats.POSITION;
        }
    
        @Override
        public void setQuadTint(int tint)
        {
            //NOOP - not used
        }
    
        @Override
        public void setQuadOrientation(EnumFacing orientation)
        {
            //NOOP - not used
        }
    
        @Override
        public void setApplyDiffuseLighting(boolean diffuse)
        {
            //NOOP - not used
        }
    
        @Override
        public void put(int element, float... data)
        {
            //don't need to check which element - position is the only one included
            vertexKeys.add(((long)(Float.floatToRawIntBits(data[axis0])) | ((long)(Float.floatToRawIntBits(data[axis1])) << 32)));
        }

        @Override
        public void setTexture(TextureAtlasSprite texture)
        {
            //NOOP - not used
        }
    }
}
