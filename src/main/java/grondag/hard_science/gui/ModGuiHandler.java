package grondag.hard_science.gui;

import grondag.hard_science.machines.BlockFabricatorTileEntity;
import grondag.hard_science.machines.SmartChestTileEntity;
import grondag.hard_science.machines.SolarAggregatorTileEntity;
import grondag.hard_science.machines.support.MachineStorageContainer;
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
        SMART_CHEST,
        BLOCK_FABRICATOR,
        SOLAR_AGGREGATOR;
    }
    
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) 
    {
        if(id == ModGui.SMART_CHEST.ordinal())
        {
            BlockPos pos = new BlockPos(x, y, z);
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof SmartChestTileEntity) 
            {
                return new MachineStorageContainer(player.inventory, (SmartChestTileEntity) te, GuiSmartChest.LAYOUT);
            }
        }
//        else if(id == ModGui.BLOCK_FABRICATOR.ordinal())
//        {
//            BlockPos pos = new BlockPos(x, y, z);
//            TileEntity te = world.getTileEntity(pos);
//            if (te instanceof BlockFabricatorTileEntity) 
//            {
//                return new BlockFabricatorContainer(player.inventory, (BlockFabricatorTileEntity) te, GuiBasicBuilder.LAYOUT);
//            }
//        }
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
                
                case SMART_CHEST:
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    TileEntity te = world.getTileEntity(pos);
                    if (te instanceof SmartChestTileEntity) 
                    {
                        SmartChestTileEntity containerTileEntity = (SmartChestTileEntity) te;
                        return new GuiSmartChest(containerTileEntity, new MachineStorageContainer(player.inventory, containerTileEntity, GuiSmartChest.LAYOUT));
                    }
                    return null;
                }
                
                case BLOCK_FABRICATOR:
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    TileEntity te = world.getTileEntity(pos);
                    if (te instanceof BlockFabricatorTileEntity) 
                    {
                        BlockFabricatorTileEntity containerTileEntity = (BlockFabricatorTileEntity) te;
                        return new GuiBasicBuilder(containerTileEntity);
                    }
                    return null;
                }
                    
                case SOLAR_AGGREGATOR:
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    TileEntity te = world.getTileEntity(pos);
                    if (te instanceof SolarAggregatorTileEntity) 
                    {
                        SolarAggregatorTileEntity mte = (SolarAggregatorTileEntity) te;
                        return new GuiSolarAggregator(mte);
                    }
                    return null;
                }
                
                default:
            }
        }
        return null;
    }

}
