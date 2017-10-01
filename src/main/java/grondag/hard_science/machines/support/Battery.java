package grondag.hard_science.machines.support;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class Battery extends AbstractPowerComponent
{
    private long maxEnergyJoules;
    
    private long storedEnergyJoules;
    
    private BatteryChemistry chemistry;

    private float maxPowerInputWatts;

    private long maxEnergyInputPerTick;

    private long inputLastTick;
    
    /** total of all energy accepted during the current tick. */
    private long inputThisTick;

    public Battery() {};
    
    public Battery(long maxEnergyJoules, BatteryChemistry chemistry)
    {
        this.setup(maxEnergyJoules, chemistry);
    }

    public Battery(NBTTagCompound tag)
    {
        this.deserializeNBT(tag);
    }
    
    private void setup(long maxEnergyJoules, BatteryChemistry chemistry)
    {
        this.maxEnergyJoules = maxEnergyJoules;
        this.chemistry = chemistry;
        this.maxEnergyInputPerTick = chemistry.maxChargeJoulesPerTick(maxEnergyJoules);
        this.maxPowerInputWatts = MachinePower.joulesPerTickToWatts(this.maxEnergyInputPerTick);
        this.setMaxOutputJoulesPerTick(chemistry.maxDischargeJoulesPerTick(maxEnergyJoules));
    }

    @Override
    public void advanceIOTracking()
    {
        super.advanceIOTracking();
        this.inputLastTick = this.inputThisTick;
        this.inputThisTick = 0;
    }
    
    @Override
    public long acceptEnergy(long maxInput, boolean allowPartial, boolean simulate)
    {        
        // prevent shenannigans/derpage
        if(maxInput <= 0) return 0;
        
        long result = Math.min(maxInput, this.maxEnergyInputPerTick - this.inputThisTick);

        result = Useful.clamp(result, 0, this.maxEnergyJoules - this.storedEnergyJoules);
       
        if(!(allowPartial || result == maxInput)) return 0;
        
        if(!(result == 0 || simulate)) 
        {
            this.inputThisTick += result;
            this.storedEnergyJoules += result;
        }        
        return result;
    }

    public BatteryChemistry getChemistry()
    {
        return chemistry;
    }
    
    @Override
    public long storedEnergyJoules()
    {
        return this.storedEnergyJoules;
    }

    @Override
    public long maxStoredEnergyJoules()
    {
        return this.maxEnergyJoules;
    }
    
    @Override
    public boolean canAcceptEnergy()
    {
        return this.storedEnergyJoules < this.maxEnergyJoules;
    }
    @Override
    public PowerComponentType componentType()
    {
        return PowerComponentType.STORED;
    }

    @Override
    public float maxPowerInputWatts()
    {
        return maxPowerInputWatts;
    }

    @Override
    public long maxEnergyInputJoulesPerTick()
    {
        return maxEnergyInputPerTick;
    }

    @Override
    public float powerInputWatts()
    {
        return this.inputLastTick * TimeUnits.TICKS_PER_SIMULATED_SECOND;
    }
    
    @Override
    public long energyInputCurrentTickJoules()
    {
        return this.inputThisTick;
    }

    @Override
    protected long provideEnergyImplementation(MachineTileEntity mte, long maxOutput, boolean allowPartial, boolean simulate)
    {
        // note that update tracking, and check against per-tick max has already been done by caller
        
        long energy = Math.min(maxOutput, this.storedEnergyJoules);
        
        if(energy > 0)
        {
            // no energy if partial not allowed and can't meet demand
            if(!allowPartial && energy < maxOutput) return 0;
            
            // consume energy if not simulating
            if(!simulate) this.storedEnergyJoules -= energy;
        }
        return energy;
    }
    
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.setup(
                tag.getLong(ModNBTTag.MACHINE_BATTERY_MAX_STORED_JOULES),
                Useful.safeEnumFromOrdinal(tag.getInteger(ModNBTTag.MACHINE_BATTERY_CHEMISTRY), BatteryChemistry.LITHIUM));
    }
    
    public void serializeNBT(NBTTagCompound tag)
    {
        tag.setLong(ModNBTTag.MACHINE_BATTERY_MAX_STORED_JOULES, this.maxEnergyJoules);
        tag.setInteger(ModNBTTag.MACHINE_BATTERY_CHEMISTRY, this.chemistry.ordinal());
    }
    
    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        super.fromBytes(pBuff);
        this.storedEnergyJoules = pBuff.readVarLong();
        this.inputLastTick = pBuff.readVarLong();
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        super.toBytes(pBuff);
        pBuff.writeVarLong(this.storedEnergyJoules);
        pBuff.writeVarLong(this.inputLastTick);
    }
}