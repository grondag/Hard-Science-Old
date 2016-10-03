package grondag.adversity.library.model;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.BlockRenderLayer;

/**
 * Super lightweight version of EnumMap for Block layers to QuadContainer. Only stores values for keys that are used.
 */
public class SparseLayerMapBuilder
{
    private final int[] layerIndices = new int[BlockRenderLayer.values().length];
    private final int size;
    public final ImmutableList<BlockRenderLayer> layerList;
    
    public SparseLayerMapBuilder(List<BlockRenderLayer> layers)
    {
        this.size = layers.size();
        this.layerList = ImmutableList.copyOf(layers);
        int counter = 0;
        
        for(BlockRenderLayer l: layers)
        {
            layerIndices[l.ordinal()] = counter++;
        }
    }
    
    public SparseLayerMap makeNewMap()
    {
        if(size == 1)
            return new SparseLayerSingletonMap();
        else 
            return new SparseLayerArrayMap();
    }
    
    public abstract class SparseLayerMap
    {
//        private byte emptyness = EMPTY_UNKNOWN;
//        
//        private static final byte EMPTY_UNKNOWN = 0;
//        private static final byte EMPTY_YES = 1;
//        private static final byte EMPTY_NO = 2;
        
        public abstract QuadContainer get(BlockRenderLayer layer);
        
        public abstract void set(BlockRenderLayer layer, QuadContainer value);
        
//        public boolean isEmpty()
//        {
//            if(emptyness == EMPTY_UNKNOWN)
//            {
//                boolean result = true;
//                for(BlockRenderLayer layer : layerList)
//                {
//                    result = result && get(layer).isEmpty();
//                }
//                emptyness = result == true ? EMPTY_YES : EMPTY_NO;
//            }
//            return emptyness == EMPTY_YES;
//        }
    }
    
    private class SparseLayerArrayMap extends SparseLayerMap
    {
        private final QuadContainer[] values = new QuadContainer[size];
        
        private SparseLayerArrayMap()
        {
            //NOOP - just making it private
        }
        
        public QuadContainer get(BlockRenderLayer layer)
        {
            return values[layerIndices[layer.ordinal()]];
        }
        
        public void set(BlockRenderLayer layer, QuadContainer value)
        {
            values[layerIndices[layer.ordinal()]] = value;
        }
        
    }
     
    private class SparseLayerSingletonMap extends SparseLayerMap
    {
        private QuadContainer value;
        
        private SparseLayerSingletonMap()
        {
            //NOOP - just making it private
        }
        
        public QuadContainer get(BlockRenderLayer layer)
        {
            return value;
        }
        
        public void set(BlockRenderLayer layer, QuadContainer value)
        {
            this.value = value;
        }
    }
}
