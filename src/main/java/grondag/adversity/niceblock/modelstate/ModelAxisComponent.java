package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelAxisComponent extends ModelStateComponent<ModelAxisComponent.ModelAxis, EnumFacing.Axis>
{
    private final ModelAxis[] LOOKUP = new ModelAxis[EnumFacing.Axis.values().length];
    
    public ModelAxisComponent(int ordinal, boolean useWorldState)
    {
        super(ordinal, useWorldState);
        LOOKUP[EnumFacing.Axis.X.ordinal()] = new ModelAxis(EnumFacing.Axis.X);
        LOOKUP[EnumFacing.Axis.Y.ordinal()] = new ModelAxis(EnumFacing.Axis.Y);
        LOOKUP[EnumFacing.Axis.Z.ordinal()] = new ModelAxis(EnumFacing.Axis.Z);
    }
    
    public ModelAxis fromEnum(EnumFacing.Axis axis)
    {
        return LOOKUP[axis.ordinal()];
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
    
    public class ModelAxis extends ModelStateValue<ModelAxis, EnumFacing.Axis>
    {
        ModelAxis(EnumFacing.Axis axis)
        {
            super(axis);
        }

        @Override
        public long getBits()
        {
            return this.value.ordinal();
        }

        @Override
        public ModelStateComponent<ModelAxis, Axis> getComponent()
        {
            return ModelAxisComponent.this;
        }
    }
}