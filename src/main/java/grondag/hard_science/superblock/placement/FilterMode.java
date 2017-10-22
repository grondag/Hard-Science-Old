package grondag.hard_science.superblock.placement;

import grondag.hard_science.library.serialization.IMessagePlusImmutable;
import grondag.hard_science.library.serialization.IReadWriteNBTImmutable;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.ILocalized;
import grondag.hard_science.library.varia.Useful;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

/**
 * For selection modes that use a region, determines which blocks in the region are affected.
 */
public enum FilterMode implements IMessagePlusImmutable<FilterMode>, IReadWriteNBTImmutable<FilterMode>, ILocalized
{
    FILL_REPLACEABLE(false),
    REPLACE_SOLID(false),
    REPLACE_ALL(false),
    REPLACE_ONLY(true),
    REPLACE_ALL_EXCEPT(true);
  
    /**
     * True if this mode uses the list of specific blocks configured in the placement item as filters.
     */
    public final boolean usesFilterBlock;
    
    private FilterMode(boolean usesFilterBlock)
    {
        this.usesFilterBlock = usesFilterBlock;
    }
    
    @Override
    public FilterMode deserializeNBT(NBTTagCompound tag)
    {
        return Useful.safeEnumFromTag(tag, ModNBTTag.PLACEMENT_FILTER_MODE, this);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        Useful.saveEnumToTag(tag, ModNBTTag.PLACEMENT_FILTER_MODE, this);
    }

    @Override
    public FilterMode fromBytes(PacketBuffer pBuff)
    {
        return pBuff.readEnumValue(FilterMode.class);
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeEnumValue(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public String localizedName()
    {
        return I18n.translateToLocal("placement.filter_mode." + this.name().toLowerCase());
    }
    
    

    public boolean shouldAffectBlock(IBlockState blockState, World world, MutableBlockPos pos, ItemStack stack, EntityPlayer player)
    {
        Block block = blockState.getBlock();
        
        switch(this)
        {
        case FILL_REPLACEABLE:
        {
            return block.isReplaceable(world, pos) && block.canHarvestBlock(world, pos, player);
        }
        
        case REPLACE_ALL:
            return block.isAir(blockState, world, pos) || block.canHarvestBlock(world, pos, player);
            
        case REPLACE_ALL_EXCEPT:
            //TODO
            return block.isAir(blockState, world, pos) || block.canHarvestBlock(world, pos, player);

        case REPLACE_ONLY:
            //TODO
            return false;
            
        case REPLACE_SOLID:
        {
            return !block.isReplaceable(world, pos) && block.canHarvestBlock(world, pos, player);
        }
        
        default:
            return false;
        
        }
    }
}
