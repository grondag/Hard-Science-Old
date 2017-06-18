package grondag.adversity.library.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.varia.Useful;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;

import static net.minecraft.util.EnumFacing.*;

public class QuadContainer
{
    public static final QuadContainer EMPTY_CONTAINER = new QuadContainer(Collections.emptyList());

    // Heavy usage, many instances, so using sublists of a single immutable list to improve LOR
    // and using instance variables to avoid memory overhead of another array. 
    // I didn't profile this to make sure it's worthwhile - don't tell Knuth.
     
    private final List<BakedQuad> up;
    private final List<BakedQuad> down;
    private final List<BakedQuad> east;
    private final List<BakedQuad> west;
    private final List<BakedQuad> north;
    private final List<BakedQuad> south;
    private final List<BakedQuad> general;
    private int[] occlusionHash;

    public static QuadContainer fromRawQuads(List<RawQuad> rawQuads)
    {
        if(rawQuads.isEmpty()) return EMPTY_CONTAINER;

        return new QuadContainer(rawQuads);
    }

    private QuadContainer(List<RawQuad> rawQuads)
    {

        @SuppressWarnings("unchecked")
        ArrayList<BakedQuad>[] buckets = new ArrayList[7];

        for(int i = 0; i < 7; i++)
        {
            buckets[i] = new ArrayList<BakedQuad>();
        }

        for(RawQuad r : rawQuads)
        {
            EnumFacing facing = r.getActualFace();
            if(facing == null)
            {
                buckets[6].add(QuadBakery.createBakedQuad(r));
            }
            else
            {
                buckets[facing.ordinal()].add(QuadBakery.createBakedQuad(r));
            }
        }

        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        if(!buckets[UP.ordinal()].isEmpty()) builder.addAll(buckets[UP.ordinal()]);
        if(!buckets[DOWN.ordinal()].isEmpty()) builder.addAll(buckets[DOWN.ordinal()]);
        if(!buckets[EAST.ordinal()].isEmpty()) builder.addAll(buckets[EAST.ordinal()]);
        if(!buckets[WEST.ordinal()].isEmpty()) builder.addAll(buckets[WEST.ordinal()]);
        if(!buckets[NORTH.ordinal()].isEmpty()) builder.addAll(buckets[NORTH.ordinal()]);
        if(!buckets[SOUTH.ordinal()].isEmpty()) builder.addAll(buckets[SOUTH.ordinal()]);
        if(!buckets[6].isEmpty()) builder.addAll(buckets[6]);
        
        ImmutableList<BakedQuad> quads = builder.build();

        int first = 0;
        
        this.up = quads.subList(first, first + buckets[UP.ordinal()].size());
        first +=  buckets[UP.ordinal()].size();

        this.down = quads.subList(first, first + buckets[DOWN.ordinal()].size());
        first +=  buckets[DOWN.ordinal()].size();
       
        this.east = quads.subList(first, first + buckets[EAST.ordinal()].size());
        first +=  buckets[EAST.ordinal()].size();
        
        this.west = quads.subList(first, first + buckets[WEST.ordinal()].size());
        first +=  buckets[WEST.ordinal()].size();
        
        this.north = quads.subList(first, first + buckets[NORTH.ordinal()].size());
        first +=  buckets[NORTH.ordinal()].size();
        
        this.south = quads.subList(first, first + buckets[SOUTH.ordinal()].size());
        first +=  buckets[SOUTH.ordinal()].size();
        
        this.general = quads.subList(first, first + buckets[6].size());
        
    }

    public List<BakedQuad> getQuads(EnumFacing face)
    {
        if(face ==null) return this.general;

        switch(face)
        {
        case DOWN:
            return this.down;
        case EAST:
            return this.east;
        case NORTH:
            return this.north;
        case SOUTH:
            return this.south;
        case UP:
            return this.up;
        case WEST:
            return this.west;
        default:
            return QuadFactory.EMPTY_QUAD_LIST;

        }
    }

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
