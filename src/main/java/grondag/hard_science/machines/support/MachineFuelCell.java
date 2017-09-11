package grondag.hard_science.machines.support;

import net.minecraft.nbt.NBTTagCompound;

public class MachineFuelCell implements IMachinePowerProvider
{

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public long availableEnergyJoules()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long maxEnergyJoules()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long maxPowerInputWatts()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long maxPowerOutputWatts()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long maxPowerInOrOutWatts()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long avgPowerInputWatts()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long avgPowerOutputWatts()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean canProvidePower()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canReceivePower()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long receiveEnergy(long maxInput, boolean allowPartial, boolean simulate)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long provideEnergy(long maxOutput, boolean allowPartial, boolean simulate)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void deserializeFromArray(int[] values)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int[] serializeToArray()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
