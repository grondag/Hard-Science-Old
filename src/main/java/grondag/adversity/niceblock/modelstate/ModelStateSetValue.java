package grondag.adversity.niceblock.modelstate;

public class ModelStateSetValue
    {
        private final ModelStateSet stateSet;
        private final Object[] values;
        private final long key;
        
        public ModelStateSetValue(ModelStateSet stateSet, AbstractModelStateComponentFactory<?>.ModelStateComponent... components)
        {
            this.stateSet = stateSet;
            values = new Object[stateSet.typeCount];
            for(AbstractModelStateComponentFactory<?>.ModelStateComponent c : components)
            {
                int index = stateSet.getIndexForType(c.getComponentType());
                values[index] = c;
            }
            key = stateSet.computeKey(components);
        }
        
        public <V>V getValue(AbstractModelStateComponentFactory<V> factory)
        {
            int index = stateSet.getIndexForType(factory.getComponentType());
            if(index == ModelStateSet.NOT_PRESENT) return null;
            return factory.getType().cast(values[index]);
        }
        
        public long getKey()
        {
            return key;
        }
    }