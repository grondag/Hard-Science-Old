package grondag.adversity.niceblock.modelstate;

import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceBlock2;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelSpeciesComponent extends ModelStateComponent<ModelSpeciesComponent.ModelSpecies, Integer>
{
    
    public ModelSpeciesComponent(int ordinal)
    {
        super(ordinal, WorldRefreshType.CACHED, 16);
    }
    
    @Override
    public long getBitsFromWorld(NiceBlock2 block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return (long) state.getValue(NiceBlock.META);
    } 

    @Override
    public ModelSpecies createValueFromBits(long bits)
    {
        return new ModelSpecies((int) bits);
    }

    @Override
    public Class<ModelSpecies> getStateType()
    {
        return ModelSpecies.class;
    }

    @Override
    public Class<Integer> getValueType()
    {
        return Integer.class;
    }
    
    public class ModelSpecies extends ModelStateValue<ModelSpecies, Integer>
    {
        ModelSpecies(int species)
        {
            super(species);
        }

        @Override
        public long getBits()
        {
            return this.value;
        }

        @Override
        public ModelStateComponent<ModelSpecies, Integer> getComponent()
        {
            return ModelSpeciesComponent.this;
        }
    }
}