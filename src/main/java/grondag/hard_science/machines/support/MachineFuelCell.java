package grondag.hard_science.machines.support;

import grondag.hard_science.CommonProxy;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.machines.support.MachinePower.FuelCellSpec;
import jline.internal.Log;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MachineFuelCell implements IMachinePowerProvider
{
    private FuelCellSpec spec;
    
    private long storedEnergyJoules;
    
    /**
     * On server, total input since last sample period, in joules.<br>
     * On client, average Watts input for the period that just ended.
     */
    private long inputThisSamplePeriod = 0;
    
    /**
     * On server, total output since last sample period, in joules.<br>
     * On client, average Watts output for the period that just ended.
     */
    private long outputThisSamplePeriod = 0;
    
    /**
     * On server, end of last sample period / start of current sample period.<br>
     * Not used on client.
     */
    private long lastSampleTimeMillis = 0;
    
    /**
     * Set to true if this provider's (low) level has recently caused a 
     * processing failure.  Sent to clients for rendering to inform player
     * but not saved to world because is transient information.
     */
    private boolean isFailureCause;

    public MachineFuelCell(FuelCellSpec spec)
    {
        super();
        this.spec = spec;
    }
 
    @Override
    public long availableEnergyJoules()
    {
        return this.storedEnergyJoules;
    }

    @Override
    public long maxEnergyJoules()
    {
        return this.spec.maxEnergyJoules;
    }

    @Override
    public long maxPowerInputWatts()
    {
        return this.spec.maxPowerInputWatts;
    }

    @Override
    public long maxPowerOutputWatts()
    {
        return this.spec.maxPowerOutputWatts;
    }

    @Override
    public long maxPowerInOrOutWatts()
    {
        return this.spec.maxPowerInOrOutWatts;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public long avgPowerInputWatts()
    {
        // computation happens in packet serialization
        return this.inputThisSamplePeriod;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public long avgPowerOutputWatts()
    {
     // computation happens in packet serialization
        return this.outputThisSamplePeriod;
    }

    @Override
    public boolean canProvidePower()
    {
        return true;
    }

    @Override
    public boolean canReceivePower()
    {
        return false;
    }

    @Override
    public long receiveEnergy(long maxInput, boolean allowPartial, boolean simulate)
    {
        return 0;
    }

    @Override
    public long provideEnergy(long maxOutput, boolean allowPartial, boolean simulate)
    {
        // prevent shenannigans/derpage
        if(maxOutput < 0) 
            maxOutput = 0;
        
        long result = Math.min(maxOutput, this.storedEnergyJoules);
        
        if(!(allowPartial || result == maxOutput)) 
            result = 0;
        if(!(result == 0 || simulate)) 
        {
            this.storedEnergyJoules -= result;
            this.outputThisSamplePeriod += result;
        }
        return result;
    }
    
    @Override
    public boolean isFailureCause()
    {
        return isFailureCause;
    }

    @Override
    public void setFailureCause(boolean isFailureCause)
    {
        this.isFailureCause = isFailureCause;
    }
    
    private static boolean didWarnBadPacket = false;
    
    @Override
    public void deserializeFromArray(int[] bits)
    {
        if((bits == null || bits.length != 7) && !didWarnBadPacket)
        {
            Log.warn("Unable to deserialize power buffer on client - malformed packet. Machine power state may be woogy.");
            didWarnBadPacket = true;
        }
  
        // sign on first long word is used to store failure indicator
        this.isFailureCause = (Useful.INT_SIGN_BIT & bits[0]) == Useful.INT_SIGN_BIT;

        this.storedEnergyJoules = ((long)(Useful.INT_SIGN_BIT_INVERSE & bits[0])) << 32 | (bits[1] & 0xffffffffL);
        this.inputThisSamplePeriod = ((long)bits[2]) << 32 | (bits[3] & 0xffffffffL);
        this.outputThisSamplePeriod = ((long)bits[4]) << 32 | (bits[5] & 0xffffffffL);
        this.spec = MachinePower.FuelCellSpec.VALUES[bits[6]];
    }

    @Override
    public int[] serializeToArray()
    {
        long currentTime = CommonProxy.currentTimeMillis();
        long elapsedTime = this.lastSampleTimeMillis - currentTime;
        
        long avgIn = 0, avgOut = 0;
        if(elapsedTime > 0)
        {
            avgIn = this.inputThisSamplePeriod * 1000 / elapsedTime;
            avgOut = this.outputThisSamplePeriod * 1000 / elapsedTime;
        }

        this.inputThisSamplePeriod = 0;
        this.outputThisSamplePeriod = 0;
        this.lastSampleTimeMillis = CommonProxy.currentTimeMillis();
        
        int[] result = new int[7];

        // sign on first long word is used to store failure indicator
        result[0] = (int) (this.isFailureCause ? this.storedEnergyJoules >> 32 | Useful.INT_SIGN_BIT : this.storedEnergyJoules >> 32);
        result[1] = (int) (this.storedEnergyJoules);

        result[2] = (int) (avgIn >> 32);
        result[3] = (int) (avgIn);
        
        result[4] = (int) (avgOut >> 32);
        result[5] = (int) (avgOut);
        
        result[6] = this.spec.ordinal();
        
        return result;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        tag.setIntArray(ModNBTTag.MACHINE_POWER_BUFFER.tag, this.serializeToArray());
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.deserializeFromArray(tag.getIntArray(ModNBTTag.MACHINE_POWER_BUFFER.tag));
    }

    @Override
    public void tick(MaterialBuffer PEBuffer)
    {
        // TODO consume PE and limit if not available
        
        long capacity = this.spec.maxEnergyJoules - this.storedEnergyJoules;
        long input = Math.min(capacity, this.spec.maxPowerInputWatts / 20);
        this.storedEnergyJoules += input;
        this.inputThisSamplePeriod += input;
    }
}
