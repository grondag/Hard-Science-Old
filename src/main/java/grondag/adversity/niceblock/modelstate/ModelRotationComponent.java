package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.Rotation;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelRotationComponent extends ModelStateComponent<ModelRotationComponent.ModelRotation, Rotation>
{
    private final IAlternator alternator;

    public ModelRotationComponent(int ordinal)
    {
        super(ordinal, WorldRefreshType.CACHED, 4);
        alternator = Alternator.getAlternator(4);
    }

    /** use this when want to force no rotation */
    public ModelRotationComponent(int ordinal, boolean noRotate)
    {
        super(ordinal, WorldRefreshType.CACHED, 1);
        alternator = Alternator.getAlternator(1);
    }
    
    @Override
    public long getBitsFromWorld(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return alternator.getAlternate(pos);
    }

    @Override
    public ModelRotationComponent.ModelRotation createValueFromBits(long bits)
    {
        return new ModelRotationComponent.ModelRotation(Rotation.values()[(int) bits]);
    }

    @Override
    public Class<ModelRotationComponent.ModelRotation> getStateType()
    {
        return ModelRotationComponent.ModelRotation.class;
    }

    @Override
    public Class<Rotation> getValueType()
    {
        return Rotation.class;
    }
    
    public class ModelRotation extends ModelStateValue<ModelRotationComponent.ModelRotation, Rotation>
    {
        private ModelRotation(Rotation value)
        {
            super(value);
        }

        @Override
        public long getBits()
        {
            return this.value.ordinal();
        }

        @Override
        public ModelStateComponent<ModelRotationComponent.ModelRotation, Rotation> getComponent()
        {
            return ModelRotationComponent.this;
        }
    }
}
