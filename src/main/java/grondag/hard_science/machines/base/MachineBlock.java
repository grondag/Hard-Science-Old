package grondag.hard_science.machines.base;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import grondag.hard_science.HardScience;
import grondag.hard_science.machines.support.MachineItemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class MachineBlock extends Block implements ITileEntityProvider 
{
    public static final Material MACHINE_MATERIAL = new Material(MapColor.SILVER_STAINED_HARDENED_CLAY) 
    {
        @Override
        public boolean isToolNotRequired() { return true; }

        @Override
        public EnumPushReaction getMobilityFlag() { return EnumPushReaction.BLOCK; }
    };
    
    public final int guiID;
    
    public MachineBlock(String name, int guiID)
    {
        super(MACHINE_MATERIAL);
        this.guiID = guiID;
        this.setUnlocalizedName(HardScience.MODID + "." + name);
        this.setRegistryName(name);
        this.setHarvestLevel(null, 0);
        this.setHardness(1);
    }
    
    /**
     * Need to destroy block here because did not do it during removedByPlayer.
     */
    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, @Nullable ItemStack stack) 
    {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.setBlockToAir(pos);
    }
    
    
    /**
     * {@inheritDoc} <br><br>
     * 
     * MachineBlock: Defer destruction of block until after drops when harvesting so can gather NBT from tile entity.
     */
    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) 
    {
        if (willHarvest) {
            return true;
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }
    
    /**
     * Main reason for override is that we have to add NBT to stack
     */
    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        Item item = Item.getItemFromBlock(this);
        if (item != null && item != Items.AIR)
        {
            ItemStack stack = new ItemStack(item, 1, this.damageDropped(state));
            TileEntity myTE = world.getTileEntity(pos);
            if(myTE != null && myTE instanceof MachineTileEntity && myTE.hasWorld() && !myTE.getWorld().isRemote) 
            {
                NBTTagCompound serverSideTag = new NBTTagCompound();
                stack.setTagInfo(MachineItemBlock.NBT_SERVER_SIDE_TAG, serverSideTag);
                ((MachineTileEntity)myTE).saveStateInStack(stack, serverSideTag);
            }
            return Collections.singletonList(stack);
        }
        else
        {
            return Collections.emptyList();
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity myTE = worldIn.getTileEntity(pos);
        if(myTE != null && myTE instanceof MachineTileEntity)
        {
            ((MachineTileEntity)myTE).disconnect();
        }
        super.breakBlock(worldIn, pos, state);
    }
 
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        TileEntity myTE = worldIn.getTileEntity(pos);
        if(myTE != null && myTE instanceof MachineTileEntity)
        {
            ((MachineTileEntity)myTE).updateRedstonePower();
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) 
    {
        if (world.isRemote || this.guiID < 0) {
            return true;
        }
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof MachineTileEntity)) 
        {
            return false;
        }
        player.openGui(HardScience.INSTANCE, this.guiID, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }
    
    
}
