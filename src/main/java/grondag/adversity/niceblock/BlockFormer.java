package grondag.adversity.niceblock;

import net.minecraft.item.Item;

public class BlockFormer extends Item
{
    //TODO: WIP
    private String label;
    private ShapeMode mode;
    private Object undoLog;
    private Object material;
    /**
    
    universal
        species
        axis
        geometry (flow)
        joins
        
    single
        10 color
         6 texture name / scale / type
         2 textureversion
         2 rotation (optional)
        10 bigtex (optional)




     */
    
    public static enum ShapeMode
    {
        BLOCK,
        BIG_BLOCK,
        GUIDED
    }

}
