package grondag.hard_science.virtualblock;

import grondag.hard_science.ClientProxy;
import grondag.hard_science.superblock.block.SuperBlockTESR;
import grondag.hard_science.superblock.block.SuperTileEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VirtualBlockTESR extends SuperBlockTESR
{

    public static final VirtualBlockTESR INSTANCE = new VirtualBlockTESR();
    
    @Override
    protected void renderBlock(SuperTileEntity te, BufferBuilder buffer)
    {
        if(te.isVirtual() && !ClientProxy.isVirtualBlockRenderingEnabled()) return;
        
        super.renderBlock(te, buffer);
    }
}
