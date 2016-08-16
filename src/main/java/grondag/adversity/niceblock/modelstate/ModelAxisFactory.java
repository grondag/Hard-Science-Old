package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelAxisFactory extends AbstractModelStateComponentFactory<EnumFacing.Axis>
{
    private final ModelAxis[] VALUES = new ModelAxis[EnumFacing.Axis.values().length];
//    public final ModelAxis KEY = new ModelAxis(null);
    public static final ModelAxisFactory INSTANCE = new ModelAxisFactory();
    
    ModelAxisFactory()
    {
        super(ModelStateComponentType.AXIS);
        for(int i = 0; i < EnumFacing.Axis.values().length; i++)
        {
            VALUES[i] = new ModelAxis(EnumFacing.Axis.values()[i]);
        }
    }
    
    @Override
    public ModelAxis getStateFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return VALUES[Math.max(0, Math.min(2, state.getValue(NiceBlock.META)))];
    }

    @Override
    public ModelAxis getStateFromBits(long bits)
    {
        return VALUES[(int) Math.max(0, Math.min(2, bits))];
    }
    
    @Override
    protected Class<Axis> getType()
    {
        return null;
    }
    
    public class ModelAxis extends ModelAxisFactory.ModelStateComponent
    {

        public ModelAxis(Axis valueIn)
        {
            super(valueIn);
        }

        @Override
        public long toBits()
        {
            return this.value.ordinal();
        }

    }
}