package grondag.hard_science.virtualblock;

import grondag.hard_science.superblock.block.SuperModelTileEntity;
import net.minecraft.util.BlockRenderLayer;

public class VirtualBlockTileEntity extends SuperModelTileEntity
{
    @Override
    public boolean shouldRenderInPass(int pass)
    {
        return this.getCachedModelState().getRenderPassSet().renderLayout.containsBlockRenderLayer(pass == 0 ? BlockRenderLayer.SOLID : BlockRenderLayer.TRANSLUCENT);
    }
    
    public boolean isVirtual() { return true; }
}
