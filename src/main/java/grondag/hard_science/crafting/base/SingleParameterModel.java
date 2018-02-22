package grondag.hard_science.crafting.base;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

public class SingleParameterModel
{
    private Object2DoubleOpenHashMap<Object> inputs 
        = new Object2DoubleOpenHashMap<Object>();
    
    private Object2DoubleOpenHashMap<Object> outputs 
        = new Object2DoubleOpenHashMap<Object>();
    
    public void createInput(Object key, double coefficient)
    {
        this.inputs.put(key, coefficient);
    }

    public void createOutput(Object key, double coefficient)
    {
        this.outputs.put(key, coefficient);
    }
    
    public ResultBuilder builder()
    {
        return new ResultBuilder();
    }
    
    public class ResultBuilder
    {
        private double parameter = -1;
        
        /**
         * Ensures input for given variable does not exceed
         * the given bound.  If this is the first constraint,
         * sets result so that result matches the given bound.
         */
        public ResultBuilder limitInput(Object var, double upperBound)
        {
            double p = upperBound / inputs.getDouble(var);
            parameter = parameter == -1
                    ?  p
                    : Math.min(parameter, p);
            return this;
        }
        
        /**
         * Ensures output for given variable is at least equal
         * to the given bound.  If this is the first constraint,
         * sets result so that result matches the given bound.
         */
        public ResultBuilder ensureOutput(Object var, double lowerBound)
        {
            double p = lowerBound / outputs.getDouble(var);
            parameter = parameter == -1
                    ?  p
                    : Math.max(parameter, p);
            return this;
        }
        
        public Result build()
        {
            return new Result(this.parameter);
        }
    }
    public class Result
    {
        private double parameter;
        
        private Result(double parameter)
        {
            this.parameter = parameter;
        }
        
        public double inputValue(Object var)
        {
            return inputs.getDouble(var) * this.parameter;
        }
        
        public long inputValueDiscrete(Object var)
        {
            return Math.round(this.inputValue(var));
        }
        
        public double outputValue(Object var)
        {
            return outputs.getDouble(var) * this.parameter;
        }
        
        public long outputValueDiscrete(Object var)
        {
            return Math.round(this.outputValue(var));
        }
    }
}
