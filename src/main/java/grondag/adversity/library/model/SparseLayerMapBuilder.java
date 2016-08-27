package grondag.adversity.library.model;

import java.util.List;

import net.minecraft.util.BlockRenderLayer;

/**
 * Super lightweight version of EnumMap for Block layers to QuadContainer. Only stores values for keys that are used.
 */
public class SparseLayerMapBuilder
{
    private final int[] layerIndices = new int[BlockRenderLayer.values().length];
    private final int size;
    
    public SparseLayerMapBuilder(List<BlockRenderLayer> layers)
    {
        this.size = layers.size();
        int counter = 0;
        
        for(BlockRenderLayer l: layers)
        {
            layerIndices[l.ordinal()] = counter++;
        }
    }
    
    public SparseLayerMap makeNewMap()
    {
        return new SparseLayerMap();
    }
    
    public class SparseLayerMap
    {
        private final QuadContainer2[] values = new QuadContainer2[size];
        
        private SparseLayerMap()
        {
        	//NOOP - just making it private
        }
        
        public QuadContainer2 get(BlockRenderLayer layer)
        {
            return values[layerIndices[layer.ordinal()]];
        }
        
        public void set(BlockRenderLayer layer, QuadContainer2 value)
        {
            values[layerIndices[layer.ordinal()]] = value;
        }
    }
}
