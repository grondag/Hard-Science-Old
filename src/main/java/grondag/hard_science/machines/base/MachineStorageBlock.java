package grondag.hard_science.machines.base;

import java.util.List;
import java.util.stream.Collectors;

import grondag.hard_science.machines.impl.logistics.SmartChestMachine;
import grondag.hard_science.machines.support.MachineItemBlock;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.ItemResourceWithQuantity;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.IResourceContainer;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public abstract class MachineStorageBlock extends MachineContainerBlock
{

    public MachineStorageBlock(String name, int guiID, ModelState modelState)
    {
        super(name, guiID, modelState);
    }

    @Override
    public AbstractMachine createNewMachine()
    {
        return new SmartChestMachine();
    }
    
    @Override
    public ItemStack getStackFromBlock(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        ItemStack result = super.getStackFromBlock(state, world, pos);
        
        // add lore for stacks generated on server
        if(result != null)
        {
            TileEntity blockTE = world.getTileEntity(pos);
            if (blockTE != null && blockTE instanceof MachineTileEntity) 
            {
                MachineTileEntity mste = (MachineTileEntity)blockTE;
                
                // client won't have the storage instance needed to do this
                if(mste.getWorld().isRemote) return result;
                
                // if device is somehow missing nothing to do, and would cause NPE to continue
                if(mste.machine() == null) return result;
                
                IResourceContainer<StorageTypeStack> store = mste.machine().itemStorage();
                
                if(store.usedCapacity() == 0) return result;
                
                // save client-side display info
                NBTTagCompound displayTag = result.getOrCreateSubCompound("display");
                    
                NBTTagList loreTag = new NBTTagList(); 

                List<AbstractResourceWithQuantity<StorageTypeStack>> items 
                        = store.find(store.storageType().MATCH_ANY)
                        .stream()
                        .sorted(ItemResourceWithQuantity.SORT_BY_QTY_DESC).collect(Collectors.toList());

                if(!items.isEmpty())
                {
                    long printedQty = 0;
                    int printedCount = 0;
                    for(AbstractResourceWithQuantity<StorageTypeStack> item : items)
                    {
                        loreTag.appendTag(new NBTTagString(item.toString()));
                        printedQty += item.getQuantity();
                        if(++printedCount == 10)
                        {
                            //FIXME: localize
                            loreTag.appendTag(new NBTTagString(String.format("...plus %,d of %d other items", 
                                    store.usedCapacity() - printedQty, items.size() - printedCount)));
                            break;
                        }
                    }
                    
                    result.setItemDamage(Math.max(1, (int) (MachineItemBlock.MAX_DAMAGE * store.availableCapacity() / store.getCapacity())));
                }
                displayTag.setTag("Lore", loreTag);
            }
        }
        return result;
    }
}
