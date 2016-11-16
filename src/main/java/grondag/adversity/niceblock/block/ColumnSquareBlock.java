package grondag.adversity.niceblock.block;

import net.minecraft.util.math.BlockPos;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.base.NiceBlockPlus;
import grondag.adversity.niceblock.base.NiceItemBlock;
import grondag.adversity.niceblock.modelstate.ModelColorMapComponent;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ColumnSquareBlock extends NiceBlockPlus
{

    public ColumnSquareBlock(ModelDispatcher dispatcher, BaseMaterial material, String styleName)
    {
        super(dispatcher, material, styleName);
    }
 
    //TODO: should this be at least a little more optimized? 
    // may not be worth it - internal faces aren't generated in model iirc.
    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        return true;
    }

    @Override
    public int getMetaForPlacedBlockFromStack(World worldIn, BlockPos posPlaced, BlockPos posOn, EnumFacing facing, ItemStack stack, EntityPlayer player)
    {
        return facing.getAxis().ordinal();

    }

    //always drop Y-axis varient
    @Override
    public int damageDropped(IBlockState state)
    {
        return Axis.Y.ordinal();
    }
    
    //only display vertical variant in item search
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        list.add(getSubItems().get(Axis.Y.ordinal()));
    }
    
    //including all variants here for benefit of WAILA model lookup
    @Override
    public List<ItemStack> getSubItems()
    {
        ModelColorMapComponent colorMap = dispatcher.getStateSet().getFirstColorMapComponent();
        ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();
        for(Axis axis : Axis.values())
        {
            ItemStack stack = new ItemStack(this, 1, axis.ordinal());
            long key = dispatcher.getStateSet().computeKey(colorMap.createValueFromBits(0),
                        ModelStateComponents.AXIS.fromEnum(axis));
                NiceItemBlock.setModelStateKey(stack, key);
            itemBuilder.add(stack);
        }
        return itemBuilder.build();
    }
}
