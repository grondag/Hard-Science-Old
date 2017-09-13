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
     * Not populated on client.
     */
    private long inputThisSamplePeriod = 0;
    
    /**
     * Average Watts input for the last sampling period
     */
    private long avgInputLastSamplePeriod = 0;
    
    /**
     * On server, total output since last sample period, in joules.<br>
     * Not populated on client.
     */
    private long outputThisSamplePeriod = 0;
    
    /**
     * Average Watts output for the last sampling period
     */
    private long avgOutputLastSamplePeriod = 0;
    
    /**
     * {@link #avgInputLastSamplePeriod} - {@link #avgOutputLastSamplePeriod}
     */
    private long avgPowerGainLoss = 0;
    
    /**
     * See {@link #logAvgPowerInputDegrees()}
     */
    private int avgPowerInputDegrees;
    
    /**
     * See {@link #avgPowerOutputDegress()}
     */
    private int avgPowerOutputDegrees;
    
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
    public long maxEnergyInputPerTick()
    {
        return this.spec.maxEnergyInputPerTick;
    }

    @Override
    public long maxEnergyOutputPerTick()
    {
        return this.spec.maxEnergyOutputPerTick;
    }
    
    @Override
    public long maxPowerInOrOutWatts()
    {
        return this.spec.maxPowerInOrOutWatts;
    }

    @Override
    public long avgPowerInputWatts()
    {
        return this.avgInputLastSamplePeriod;
    }

    @Override
    public long avgPowerOutputWatts()
    {
        return this.avgOutputLastSamplePeriod;
    }

    @Override
    public long avgNetPowerGainLoss()
    {
        return this.avgPowerGainLoss;
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

        if(result > this.spec.maxEnergyOutputPerTick)
            result = this.spec.maxEnergyOutputPerTick;
        
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
    public int logAvgPowerInputDegrees()
    {
        return this.avgPowerInputDegrees;
    }

    @Override
    public int avgPowerOutputDegress()
    {
        return this.avgPowerOutputDegrees;
    }
    
    @Override
    public void deserializeFromArray(int[] bits)
    {
        if((bits == null || bits.length != 7))
        {
            if(!didWarnBadPacket)
            {
                Log.warn("Unable to deserialize power buffer on client - malformed packet. Machine power state may be woogy.");
                didWarnBadPacket = true;
            }
            return;
        }
  
        // sign on first long word is used to store failure indicator
        this.isFailureCause = (Useful.INT_SIGN_BIT & bits[0]) == Useful.INT_SIGN_BIT;

        this.storedEnergyJoules = ((long)(Useful.INT_SIGN_BIT_INVERSE & bits[0])) << 32 | (bits[1] & 0xffffffffL);
        this.avgInputLastSamplePeriod = ((long)bits[2]) << 32 | (bits[3] & 0xffffffffL);
        this.avgOutputLastSamplePeriod = ((long)bits[4]) << 32 | (bits[5] & 0xffffffffL);
        this.spec = MachinePower.FuelCellSpec.VALUES[bits[6]];
        this.avgPowerGainLoss = this.avgInputLastSamplePeriod - this.avgOutputLastSamplePeriod;
        
        this.avgPowerInputDegrees = (int) (this.avgInputLastSamplePeriod <= 0 ? 0 : Math.max(1, this.avgInputLastSamplePeriod * 180 / this.spec.maxPowerInOrOutWatts));
        this.avgPowerOutputDegrees = (int) (this.avgOutputLastSamplePeriod <= 0 ? 0 : Math.max(1, this.avgOutputLastSamplePeriod * 180 / this.spec.maxPowerInOrOutWatts));
     
        // clear client cached formated values
        this.formatedAvailableEnergyJoules = null;
        this.formattedAvgNetPowerGainLoss = null;
    }

    /** 
     * Returns true if stats changed.
     */
    private boolean updateStatistics()
    {
        long currentTime = CommonProxy.currentTimeMillis();
        long elapsedTime = currentTime - this.lastSampleTimeMillis;
        
        long oldIn = this.avgInputLastSamplePeriod;
        long oldOut = this.avgOutputLastSamplePeriod;
        if(elapsedTime > 500)
        {
            this.avgInputLastSamplePeriod = (long) (this.inputThisSamplePeriod * 200.0 / elapsedTime + oldIn * 0.8);
            this.avgOutputLastSamplePeriod = (long) (this.outputThisSamplePeriod * 200.0 / elapsedTime + oldOut * 0.8);
            this.inputThisSamplePeriod = 0;
            this.outputThisSamplePeriod = 0;
            this.lastSampleTimeMillis = CommonProxy.currentTimeMillis();

            return (oldIn != this.avgInputLastSamplePeriod || oldOut != this.avgOutputLastSamplePeriod);
        }
        
        return false;
        
    }
    
    @Override
    public int[] serializeToArray()
    {
        int[] result = new int[7];

        // sign on first long word is used to store failure indicator
        result[0] = (int) (this.isFailureCause ? this.storedEnergyJoules >> 32 | Useful.INT_SIGN_BIT : this.storedEnergyJoules >> 32);
        result[1] = (int) (this.storedEnergyJoules);

        result[2] = (int) (avgInputLastSamplePeriod >> 32);
        result[3] = (int) (avgInputLastSamplePeriod);
        
        result[4] = (int) (avgOutputLastSamplePeriod >> 32);
        result[5] = (int) (avgOutputLastSamplePeriod);
        
        result[6] = this.spec.ordinal();
        
        return result;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        if(tag.hasKey(ModNBTTag.MACHINE_POWER_BUFFER))
            this.deserializeFromArray(tag.getIntArray(ModNBTTag.MACHINE_POWER_BUFFER));
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        tag.setIntArray(ModNBTTag.MACHINE_POWER_BUFFER, this.serializeToArray());
    }

    @Override
    public boolean tick(MaterialBuffer PEBuffer)
    {
        // TODO consume PE and limit if not available
        boolean didChange = false;
        
        long capacity = this.spec.maxEnergyJoules - this.storedEnergyJoules;
        if(capacity > 0)
        {
            long input = Math.min(capacity, this.spec.maxPowerInputWatts / 20);
            
            if(input != 0)
            {
                this.storedEnergyJoules += input;
                this.inputThisSamplePeriod += input;
                didChange = true;
            }
        }
        
        return this.updateStatistics() || didChange;
    }

    private String formatedAvailableEnergyJoules;
    
    @Override
    @SideOnly(Side.CLIENT)
    public String formatedAvailableEnergyJoules()
    {
        if(this.formatedAvailableEnergyJoules == null)
        {
            this.formatedAvailableEnergyJoules = MachinePower.formatEnergy(this.availableEnergyJoules(), false);
        }
        return this.formatedAvailableEnergyJoules;
    }

    private String formattedAvgNetPowerGainLoss;
    
    @Override
    @SideOnly(Side.CLIENT)
    public String formattedAvgNetPowerGainLoss()
    {
        if(this.formattedAvgNetPowerGainLoss == null)
        {
            this.formattedAvgNetPowerGainLoss = MachinePower.formatPower(this.avgNetPowerGainLoss(), true);
        }
        return this.formattedAvgNetPowerGainLoss;
    }
}
