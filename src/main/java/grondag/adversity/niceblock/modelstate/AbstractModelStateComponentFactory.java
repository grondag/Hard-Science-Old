package grondag.adversity.niceblock.modelstate;

import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.base.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public abstract class AbstractModelStateComponentFactory<T>
{
    abstract public ModelStateComponent getStateFromWorld(NiceBlock block, IBlockTest test, IBlockState state, IBlockAccess world, BlockPos pos);
    abstract public ModelStateComponent getStateFromBits(long bits);
    abstract protected Class<T> getType();
    protected final ModelStateComponentType componentType;
    
    protected AbstractModelStateComponentFactory(ModelStateComponentType type)
    {
        this.componentType = type;
    }
    
    public ModelStateComponentType getComponentType() { return componentType; }
    
    public abstract class ModelStateComponent
    {
        protected final T value;
        
        public ModelStateComponent(T valueIn)
        {
            this.value = valueIn;
        }

        public T getValue()
        {
            return this.value;
        }

        public ModelStateComponentType getComponentType() { return AbstractModelStateComponentFactory.this.getComponentType(); }
                
        abstract public long toBits();
    }
}