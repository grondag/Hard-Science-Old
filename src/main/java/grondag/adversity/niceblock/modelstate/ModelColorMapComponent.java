package grondag.adversity.niceblock.modelstate;

import grondag.adversity.niceblock.base.NiceBlock2;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.IColorMapProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ModelColorMapComponent extends ModelStateComponent<ModelColorMapComponent.ModelColorMap, ColorMap>
{
    private final IColorMapProvider colorProvider;
    
    public ModelColorMapComponent(int ordinal, WorldRefreshType refreshType, IColorMapProvider colorProvider)
    {
        super(ordinal, refreshType, colorProvider.getColorMapCount());
        this.colorProvider = colorProvider;
    }

    @Override
    public long getBitsFromWorld(NiceBlock2 block, IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return (long) state.getValue(NiceBlock2.META);
    }

    @Override
    public ModelColorMap createValueFromBits(long bits)
    {
        return new ModelColorMap(Math.min((int)getValueCount() - 1, Math.max(0, (int) bits)));
    }

    @Override
    public Class<ModelColorMap> getStateType()
    {
        return ModelColorMap.class;
    }

    @Override
    public Class<ColorMap> getValueType()
    {
        return ColorMap.class;
    }
    
    public class ModelColorMap extends ModelStateValue<ModelColorMapComponent.ModelColorMap, ColorMap>
    {
        //color maps don't know their own index, so need to save it
        private final int colorIndex;
        
        private ModelColorMap(int colorIndex)
        {
            super(colorProvider.getColorMap(colorIndex));
            this.colorIndex = colorIndex;
        }
        
        @Override
        public ModelStateComponent<ModelColorMap, ColorMap> getComponent()
        {
            return ModelColorMapComponent.this;
        }

        @Override
        public long getBits()
        {
            return colorIndex;
        }
    }

}
