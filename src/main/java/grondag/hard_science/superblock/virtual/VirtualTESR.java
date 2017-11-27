package grondag.hard_science.superblock.virtual;

import grondag.hard_science.superblock.block.SuperBlockTESR;
import grondag.hard_science.superblock.block.SuperTileEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VirtualTESR extends SuperBlockTESR
{

    public static final VirtualTESR INSTANCE = new VirtualTESR();
    
    @Override
    protected void renderBlock(SuperTileEntity te, BufferBuilder buffer)
    {
        if(!te.isVirtual() || !((VirtualTileEntity)te).isVisible()) return;
        
        super.renderBlock(te, buffer);
    }
}
