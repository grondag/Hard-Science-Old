package grondag.hard_science.machines;

import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.machines.base.MachineTileEntityTickable;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ModularTankBlock extends MachineBlock
{
    public ModularTankBlock(String name)
    {
        super(name, ModGui.MODULAR_TANK.ordinal(), MachineBlock.creatBasicMachineModelState(null, Textures.BORDER_CHANNEL_DOTS));
    }

    @Override
    public AbstractMachine createNewMachine()
    {
        return new ModularTankMachine();
    }
    
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new MachineTileEntityTickable();
    }
    
    @Override
    public TextureAtlasSprite getSymbolSprite()
    {
        return Textures.DECAL_DRIP.getSampleSprite();
    }
    
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) 
    {
        // allow fluid handling logic to happen
        if(!world.isRemote && player.getHeldItem(hand).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
        {
            TileEntity te = world.getTileEntity(pos);
            if(te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side))
            {
                IFluidHandler fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
                if(FluidUtil.interactWithFluidHandler(player, hand, fluidHandler))
                    return true;
            }
        }
        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }
}
