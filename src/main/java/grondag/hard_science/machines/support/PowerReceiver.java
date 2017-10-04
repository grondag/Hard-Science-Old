package grondag.hard_science.machines.support;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.nbt.NBTTagCompound;

public class PowerReceiver extends AbstractPowerComponent
{
    
    public PowerReceiver()
    {
        
    }
    
    public PowerReceiver(long maxJoulesPerTick)
    {
        this.setMaxOutputJoulesPerTick(maxJoulesPerTick);
    }
    
    public PowerReceiver(NBTTagCompound tag)
    {
        this(
                tag.getLong(ModNBTTag.MACHINE_POWER_RECEIVER_MAX_JOULES)
            );
    }
    

    @Override
    public PowerComponentType componentType()
    {
        return PowerComponentType.EXTERNAL;
    }

    @Override
    public float powerInputWatts()
    {
        return this.powerOutputWatts();
    }

    @Override
    public long energyInputCurrentTickJoules()
    {
        return this.energyOutputCurrentTickJoules();
    }
    
    @Override
    public float maxPowerInputWatts()
    {
        return this.maxPowerOutputWatts();
    }

    @Override
    public long maxEnergyInputJoulesPerTick()
    {
        return this.maxEnergyOutputJoulesPerTick();
    }

    public void serializeNBT(NBTTagCompound tag)
    {
        tag.setLong(ModNBTTag.MACHINE_POWER_RECEIVER_MAX_JOULES, this.maxEnergyOutputJoulesPerTick());
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.setMaxOutputJoulesPerTick(tag.getLong(ModNBTTag.MACHINE_POWER_RECEIVER_MAX_JOULES));
        
    }

    @Override
    protected long provideEnergyImplementation(MachineTileEntity mte, long maxOutput, boolean allowPartial, boolean simulate)
    {
        // TODO: once power networks exist, need to actually draw power from them...
        return maxOutput;
    }
}