package grondag.hard_science.virtualblock;

import grondag.hard_science.superblock.block.SuperModelTileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class VirtualTileEntity extends SuperModelTileEntity
{

//    @Override
//    public boolean shouldRenderInPass(int pass)
//    {
//        return this.getCachedModelState().getRenderPassSet().renderLayout.containsBlockRenderLayer(pass == 0 ? BlockRenderLayer.SOLID : BlockRenderLayer.TRANSLUCENT);
//    }
    
    @Override
    public boolean isVirtual() { return true; }

    @Override
    public void onLoad()
    {
        super.onLoad();
//        if(!this.world.isRemote)
//        {
//            VirtualBlockTracker.INSTANCE.get(world).enqueue(pos);
//            //FIXME: remove
//            Log.info("Enqueued virtual block @" + pos.toString());
//        }
    }
    
    @SideOnly(Side.CLIENT)
    public boolean isVisible()
    {
        //TODO:
        return true;
    }
}
