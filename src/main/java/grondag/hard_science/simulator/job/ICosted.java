package grondag.hard_science.simulator.job;

import java.util.List;

public interface ICosted
{
    public long getEnergyCost();
    
    //FIXME: make real
    public List<?> getMaterialCost();
    
    public int getComputeCost();
}
