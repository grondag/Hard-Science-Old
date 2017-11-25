package grondag.hard_science.superblock.placement.spec;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.superblock.placement.PlacementItem;
import jline.internal.Log;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * Parent of placements that place copies of the same item stack
 * at every block position within the placement. Separated
 * from root to allow for future composite placements (blueprints.)
 */
public abstract class SingleStackPlacementSpec extends AbstractPlacementSpec
{
    /**
     * Stack of the block that is to be placed.
     * The stack provided to constructor should ALREADY have correct
     * block rotation and species and other properties that are dependent on 
     * in-world placement context.
     * 
     * Stack does NOT need to be AIR for excavation-only placements.
     * Some placements (CSG) need a model state to define the placement geometry.
     * Excavation-only placements that do not need this will ignore the source stack.
     */
    private ItemStack sourceStack;
    
    protected ImmutableList<PlacementSpecEntry> entries;
    
    protected SingleStackPlacementSpec() {};

    /** 
     * Source stack should be already be modified for in-world placement context.
     */
    protected SingleStackPlacementSpec(PlacementSpecBuilder builder, ItemStack sourceStack)
    {
        super(builder);
        this.sourceStack = sourceStack;
    }
    
    @Override
    public ImmutableList<PlacementSpecEntry> entries()
    {
        return this.entries;
    }
    
    /**
     * Stack is modified for placement context.
     */
    public ItemStack sourceStack()
    {
        return this.sourceStack;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.sourceStack = new ItemStack(tag);
        
        ImmutableList.Builder<PlacementSpecEntry> builder = ImmutableList.builder();
        if(tag.hasKey(ModNBTTag.PLACEMENT_ENTRY_DATA))
        {
            int[] entryData = tag.getIntArray(ModNBTTag.PLACEMENT_ENTRY_DATA);
            if(entryData.length % 6 != 0)
            {
                Log.warn("Detected corrupt data on NBT read of construction specification. Some data may be lost.");
            }
            else
            {
                int i = 0;
                int entryIndex = 0;
                while(i < entryData.length)
                {
                    BlockPos pos = new BlockPos(entryData[i++], entryData[i++], entryData[i++]);
                    SingleStackEntry entry = new SingleStackEntry(entryIndex++, pos);
                    entry.excavationTaskID = entryData[i++];
                    entry.placementTaskID = entryData[i++];
                    entry.procurementTaskID = entryData[i++];
                    builder.add(entry);
                }
            }
        }
        this.entries = builder.build();
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        if(this.sourceStack != null) this.sourceStack.writeToNBT(tag);
        if(this.entries != null && !this.entries.isEmpty())
        {
            int i = 0;
            int[] entryData = new int[this.entries.size() * 6];
            for(PlacementSpecEntry entry : this.entries)
            {
                entryData[i++] = entry.pos().getX();
                entryData[i++] = entry.pos().getY();
                entryData[i++] = entry.pos().getZ();
                entryData[i++] = entry.excavationTaskID;
                entryData[i++] = entry.placementTaskID;
                entryData[i++] = entry.procurementTaskID;
            }
            tag.setIntArray(ModNBTTag.PLACEMENT_ENTRY_DATA, entryData);
        }
    }

    public class SingleStackEntry extends PlacementSpecEntry
    {
        protected SingleStackEntry(int index, BlockPos pos)
        {
            super(index, pos);
        }

        @Override
        public ItemStack placement()
        {
            return isExcavation ? Items.AIR.getDefaultInstance() : sourceStack;
        }

        @Override
        public boolean isExcavation()
        {
            return isExcavation;
        }

        @Override
        public PlacementItem placementItem()
        {
            // TODO Auto-generated method stub
            return null;
        }
    }
}