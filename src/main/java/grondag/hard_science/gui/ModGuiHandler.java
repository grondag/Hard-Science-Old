package grondag.hard_science.gui;

import grondag.hard_science.machines.MachineContainer;
import grondag.hard_science.machines.MachineContainerTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModGuiHandler implements IGuiHandler
{
    public static enum ModGui
    {
        SUPERMODEL_ITEM,
        TEST_CONTAINER
    }
    
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) 
    {
        if(id == ModGui.TEST_CONTAINER.ordinal())
        {
            BlockPos pos = new BlockPos(x, y, z);
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof MachineContainerTileEntity) 
            {
                return new MachineContainer(player.inventory, (MachineContainerTileEntity) te, GuiSmartChest.LAYOUT);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) 
    {
        if(id < ModGui.values().length)
        {
            switch(ModGui.values()[id])
            {
                case  SUPERMODEL_ITEM:
                    return new SuperGuiScreen();
                
                case TEST_CONTAINER:
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    TileEntity te = world.getTileEntity(pos);
                    if (te instanceof MachineContainerTileEntity) 
                    {
                        MachineContainerTileEntity containerTileEntity = (MachineContainerTileEntity) te;
                        return new GuiSmartChest(containerTileEntity, new MachineContainer(player.inventory, containerTileEntity, GuiSmartChest.LAYOUT));
                    }
                    return null;
                }
                default:
            }
        }
        return null;
    }

}
