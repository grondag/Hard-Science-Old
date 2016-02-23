package grondag.adversity.niceblock.newmodel;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BigBlockHelper extends ColoredBlockHelperPlus
{
    //TODO: I'm sure this doesn't belong here
    private static final String STACK_PLACEMENT_TAG = "BBPlace";
    
    private static final byte[] STACK_PLACEMENT_DEFAULT_SHAPE = {4, 4, 4};
    
    public BigBlockHelper(ModelDispatcherBase dispatcher)
    {
        super(dispatcher);
    }

    // Can't pass metadata to block state for blocks that are meant to be tile entities
    // because the item metadata value (colorIndex) will be out of range.  
    // placeBlockAt will give the colorIndex value to the TE state.
    @Override
    public int getMetaForPlacedBlockFromStack(World worldIn, BlockPos pos, EnumFacing facing, ItemStack stack)
    {
        return 0;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        return super.getItemStackDisplayName(stack);
    }

    @Override
    public List<ItemStack> getSubItems()
    {
        List<ItemStack> subItems = super.getSubItems();
        for(ItemStack stack : subItems)
        {
            NBTTagCompound tag = stack.getTagCompound();
            
            if(tag == null){
                tag = new NBTTagCompound();
                stack.setTagCompound(tag);
            }
            tag.setByteArray(STACK_PLACEMENT_TAG, STACK_PLACEMENT_DEFAULT_SHAPE);

            NBTTagCompound display = new NBTTagCompound();
            stack.setTagInfo("display", display);

            NBTTagList lore = new NBTTagList();
            display.setTag("Lore", lore);
            lore.appendTag(new NBTTagString(String.format("Block Size: %1$d x %2$d x %3$d", STACK_PLACEMENT_DEFAULT_SHAPE[0], STACK_PLACEMENT_DEFAULT_SHAPE[1], STACK_PLACEMENT_DEFAULT_SHAPE[2])));
        }
            
        return subItems;
    }
    
    
}
