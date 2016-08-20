package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelAxisComponent extends ModelStateComponent<ModelAxis, EnumFacing.Axis>
{
    public ModelAxisComponent(int ordinal)
    {
        super(ordinal);
    }
    
    @Override
    public long getBitsFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return Math.max(0, Math.min(2, state.getValue(NiceBlock.META)));
    } 

    @Override
    public long getValueCount()
    {
        return EnumFacing.Axis.values().length;
    }

    @Override
    public ModelAxis createValueFromBits(long bits)
    {
        return new ModelAxis(EnumFacing.Axis.values()[(int) bits]);
    }

    @Override
    public Class<ModelAxis> getStateType()
    {
        return ModelAxis.class;
    }

    @Override
    public Class<Axis> getValueType()
    {
        return EnumFacing.Axis.class;
    }

//    @Override
//    public long getBits(Axis value)
//    {
//        return value.ordinal();
//    }

}