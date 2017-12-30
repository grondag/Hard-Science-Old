package grondag.hard_science.machines.support;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.machines.base.AbstractMachine;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
/**
 * Power supplies have up to three components:<br><br>
 * 
 * Fuel cell - to generate power at a fixed rate.<br>
 * Battery - to store power and efficiently satisfy variable demand. <br>
 * Power Receiver - to receive external power if it is available. <br>
 * 
 * @author grondag
 *
 */
public class MachinePowerSupply implements IMachinePowerProvider
{
    private FuelCell fuelCell;
    private Battery battery;
    private PowerReceiver powerReceiver; 
    
    /**
     * Set to true if this provider's (low) level has recently caused a 
     * processing failure.  Sent to clients for rendering to inform player
     * but not saved to world because is transient information.
     */
    private boolean isFailureCause;
    
    private final float maxPowerOutputWatts;
    private final long maxEnergyOutputPerTick;
    
    // used for packet serialization
    private static final int BIT_IS_FAILURE = 1;
    private static final int BIT_HAS_BATTERY = BIT_IS_FAILURE << 1;
    private static final int BIT_HAS_FUEL_CELL = BIT_HAS_BATTERY << 1;
    private static final int BIT_HAS_POWER_RECEIVER = BIT_HAS_FUEL_CELL << 1;
    
    private float powerOutputWatts;

    public MachinePowerSupply(FuelCell fuelCell, Battery battery, PowerReceiver powerReceiver)
    {
        super();
        this.fuelCell = fuelCell;
        this.battery = battery;
        this.powerReceiver = powerReceiver;
        
        this.maxPowerOutputWatts = 
                  (fuelCell == null ? 0 : fuelCell.maxPowerOutputWatts())
                + (battery == null ? 0 : battery.maxPowerOutputWatts())
                + (powerReceiver == null ? 0 : powerReceiver.maxPowerOutputWatts());      
        
        this.maxEnergyOutputPerTick = 
                (fuelCell == null ? 0 : fuelCell.maxEnergyOutputJoulesPerTick())
              + (battery == null ? 0 : battery.maxEnergyOutputJoulesPerTick())
              + (powerReceiver == null ? 0 : powerReceiver.maxEnergyOutputJoulesPerTick());  
    }
 
    public FuelCell fuelCell() { return this.fuelCell; }
    public Battery battery() { return this.battery; };
    public PowerReceiver powerReceiver() { return this.powerReceiver; }; 
 
    @Override
    public boolean canProvideEnergy(AbstractMachine machine)
    {
        return      (this.powerReceiver != null && this.powerReceiver.canProvideEnergy(machine)) 
                ||  (this.fuelCell != null && this.fuelCell.canProvideEnergy(machine))
                ||  (this.battery != null && this.battery.canProvideEnergy(machine));
    }
    
    @Override
    public float powerOutputWatts()
    {
        return this.powerOutputWatts;
    }

    @Override
    public float maxPowerOutputWatts()
    {
        return this.maxPowerOutputWatts;
    }

    @Override
    public long maxEnergyOutputPerTick()
    {
        return this.maxEnergyOutputPerTick;
    }
  
    @Override
    public boolean canAcceptEnergy()
    {
        return this.battery != null && this.battery.canAcceptEnergy();
    }

    @Override
    public long acceptEnergy(long maxInput, boolean allowPartial, boolean simulate)
    {
        return this.battery.acceptEnergy(maxInput, allowPartial, simulate);
    }
   
    /**
     * Tries to draw power from grid start (if available), then from fuel cell, then from battery.
     * Will combine output from multiple sources if necessary.
     */
    @Override
    public long provideEnergy(AbstractMachine machine, long maxOutput, boolean allowPartial, boolean simulate)
    {
        // prevent shenannigans/derpage
        if(maxOutput <= 0) return 0; 
        
        long result = 0;
        
        // if not accepting partial fulfillment, have to simulate all results start
        if(!allowPartial)
        {
            result = provideEnergyInner(machine, maxOutput, true);
            if(result != maxOutput) return 0;
            if(!simulate) result = provideEnergyInner(machine, maxOutput, false);
        }
        else
        {
            result = provideEnergyInner(machine, maxOutput, simulate);
        }
        
        // forgive self for prior failures if managed to do something this time
        if(result > 0 && !simulate && this.isFailureCause) this.setFailureCause(false);
        
        return result;
    }
    
    private long provideEnergyInner(AbstractMachine machine, long maxOutput, boolean simulate)
    {
        long result = this.powerReceiver == null ? 0 : this.powerReceiver.provideEnergy(machine, maxOutput, true, simulate);
        
        if(result < maxOutput)
        {
            result += this.fuelCell == null ? 0 : this.fuelCell.provideEnergy(machine, maxOutput - result, true, simulate);
            
            if(result < maxOutput && this.battery != null)
            {
                result += this.battery.provideEnergy(machine, maxOutput - result, true, simulate);
            }
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
    
  //FIXME: remove cruft
    
//    private static boolean didWarnBadPacket = false;
//
//    @Override
//    public int logAvgPowerInputDegrees()
//    {
//        return this.avgPowerInputDegrees;
//    }

//    @Override
//    public int avgPowerOutputDegress()
//    {
//        return this.avgPowerOutputDegrees;
//    }
//    
//    @Override
//    public void deserializeFromArray(int[] bits)
//    {
//        if((bits == null || bits.length != 6))
//        {
//            if(!didWarnBadPacket)
//            {
//                Log.warn("Unable to deserialize power buffer on client - malformed packet. Machine power state may be woogy.");
//                didWarnBadPacket = true;
//            }
//            return;
//        }
//  
//        // sign on start long word is used to store failure indicator
//        this.isFailureCause = (Useful.INT_SIGN_BIT & bits[0]) == Useful.INT_SIGN_BIT;
//
//        this.storedEnergyJoules = ((long)(Useful.INT_SIGN_BIT_INVERSE & bits[0])) << 32 | (bits[1] & 0xffffffffL);
//        this.avgInputLastSamplePeriod = ((long)bits[2]) << 32 | (bits[3] & 0xffffffffL);
//        this.avgOutputLastSamplePeriod = ((long)bits[4]) << 32 | (bits[5] & 0xffffffffL);
//        this.avgPowerGainLoss = this.avgInputLastSamplePeriod - this.avgOutputLastSamplePeriod;
//        
//        this.avgPowerInputDegrees = (int) (this.avgInputLastSamplePeriod <= 0 ? 0 : Math.max(1, this.avgInputLastSamplePeriod * 180 / this.maxPowerInOrOutWatts()));
//        this.avgPowerOutputDegrees = (int) (this.avgOutputLastSamplePeriod <= 0 ? 0 : Math.max(1, this.avgOutputLastSamplePeriod * 180 / this.maxPowerInOrOutWatts()));
//     
//        // clear client cached formated values
//        this.formatedAvailableEnergyJoules = null;
//        this.formattedAvgNetPowerGainLoss = null;
//    }
//
//    @Override
//    public int[] serializeToArray()
//    {
//        int[] result = new int[6];
//
//        // sign on start long word is used to store failure indicator
//        result[0] = (int) (this.isFailureCause ? this.storedEnergyJoules >> 32 | Useful.INT_SIGN_BIT : this.storedEnergyJoules >> 32);
//        result[1] = (int) (this.storedEnergyJoules);
//
//        result[2] = (int) (avgInputLastSamplePeriod >> 32);
//        result[3] = (int) (avgInputLastSamplePeriod);
//        
//        result[4] = (int) (avgOutputLastSamplePeriod >> 32);
//        result[5] = (int) (avgOutputLastSamplePeriod);
//        
//        return result;
//    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.fuelCell = tag.hasKey(ModNBTTag.MACHINE_FUEL_CELL_PLATE_SIZE) ? new PolyethyleneFuelCell(tag) : null;
        this.battery = tag.hasKey(ModNBTTag.MACHINE_BATTERY_MAX_STORED_JOULES) ? new Battery(tag) : null;
        this.powerReceiver = tag.hasKey(ModNBTTag.MACHINE_POWER_RECEIVER_MAX_JOULES) ? new PowerReceiver(tag) : null;
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        if(this.fuelCell != null) this.fuelCell.serializeNBT(tag);
        if(this.battery != null) this.battery.serializeNBT(tag);
        if(this.powerReceiver != null) this.powerReceiver.serializeNBT(tag);
    }

    @Override
    public boolean tick(AbstractMachine machine, long tick)
    {
        boolean didChange = false;
        boolean canBatteryCharge = this.battery != null && this.battery.canAcceptEnergy();
        
        // if machine is off, nothing to do unless we are going to recharge the battery
        if(canBatteryCharge || machine.isOn())
        {
            // Generate power to recharge battery if needed.
            // Do this before we clear per-tick limits so we don't 
            // consume capacity that is needed for production. 
            if(canBatteryCharge)
            {
                long joulesNeeded = this.battery.acceptEnergy(this.battery.maxEnergyInputJoulesPerTick(), true, true);
                
                if(joulesNeeded > 0)
                {
                    if(this.powerReceiver != null)
                    {
                        long joulesAvailable = this.powerReceiver.provideEnergy(machine, joulesNeeded, true, false);
                        if(joulesAvailable > 0)
                        {
                            didChange = true;
                            if(this.battery.acceptEnergy(joulesAvailable, true, false) != joulesAvailable && Log.DEBUG_MODE)
                            {
                                Log.info("Battery energy acceptance mismatch during recharging. This is a bug.  Some energy was lost.");
                            }
                            joulesNeeded = this.battery.acceptEnergy(this.battery.maxEnergyInputJoulesPerTick(), true, true);
                        }
                    }
                    
                    if(joulesNeeded > 0 && this.fuelCell != null)
                    {
                        long joulesAvailable = this.fuelCell.provideEnergy(machine, joulesNeeded, true, false);
                        if(joulesAvailable > 0)
                        {
                            didChange = true;
                            if(this.battery.acceptEnergy(joulesAvailable, true, false) != joulesAvailable && Log.DEBUG_MODE)
                            {
                                Log.info("Battery energy acceptance mismatch during recharging. This is a bug.  Some energy was lost.");
                            }
                        }
                    }
                }
            }
            
            // update tracking totals
            
            float powerLastTick = 0;
            
            if(this.battery != null)
            {
                this.battery.advanceIOTracking();
                powerLastTick += this.battery.powerOutputWatts();
                powerLastTick -= this.battery.powerInputWatts();
            }
            
            if(this.fuelCell != null)
            {
                this.fuelCell.advanceIOTracking();
                powerLastTick += this.fuelCell.powerOutputWatts();
            }
            
            if(this.powerReceiver != null)
            {
                this.powerReceiver.advanceIOTracking();
                powerLastTick += this.powerReceiver.powerOutputWatts();
            }
            
            this.powerOutputWatts = powerLastTick;
            
        }
        return didChange;
    }

//    private String formatedAvailableEnergyJoules;
//    
//    @Override
//    @SideOnly(Side.CLIENT)
//    public String formatedAvailableEnergyJoules()
//    {
//        if(this.formatedAvailableEnergyJoules == null)
//        {
//            this.formatedAvailableEnergyJoules = MachinePower.formatEnergy(this.availableEnergyJoules(), false);
//        }
//        return this.formatedAvailableEnergyJoules;
//    }
//
//    private String formattedAvgNetPowerGainLoss;
//    
//    @Override
//    @SideOnly(Side.CLIENT)
//    public String formattedAvgNetPowerGainLoss()
//    {
//        if(this.formattedAvgNetPowerGainLoss == null)
//        {
//            this.formattedAvgNetPowerGainLoss = MachinePower.formatPower(this.avgNetPowerGainLoss(), true);
//        }
//        return this.formattedAvgNetPowerGainLoss;
//    }

    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.powerOutputWatts = pBuff.readFloat();
        
        int flags = pBuff.readByte();
                
        this.isFailureCause = (flags & BIT_IS_FAILURE) != 0;
        
        if((flags & BIT_HAS_BATTERY) != 0)
        {
            // should never be null if getting packet for it, and 
            // won't properly initialize the battery 
            // but safeguard against possibility
            if(this.battery == null) 
            {
                if(Log.DEBUG_MODE) Log.info("Client machine power supply received update packet for non-existant battery. This is probably a bug.");
                this.battery = new Battery();
            }
                
            this.battery.fromBytes(pBuff);
        }
        
        if((flags & BIT_HAS_FUEL_CELL) != 0)
        {
            // should never be null if getting packet for it, and 
            // won't properly initialize the fuel cell 
            // but safeguard against possibility
            if(this.fuelCell == null) 
            {
                if(Log.DEBUG_MODE) Log.info("Client machine power supply received update packet for non-existant fuel cell. This is probably a bug.");
                this.fuelCell = new PolyethyleneFuelCell();
            }
            this.fuelCell.fromBytes(pBuff);
        }

        if((flags & BIT_HAS_POWER_RECEIVER) != 0)
        {
            // should never be null if getting packet for it, and 
            // won't properly initialize the power receiver 
            // but safeguard against possibility
            if(this.powerReceiver == null) 
            {
                if(Log.DEBUG_MODE) Log.info("Client machine power supply received update packet for non-existant receiver. This is probably a bug.");
                this.powerReceiver = new PowerReceiver();
            }
            this.powerReceiver.fromBytes(pBuff);
        }
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeFloat(this.powerOutputWatts);
        
        int flags = this.isFailureCause ? BIT_IS_FAILURE : 0;
        if(this.battery != null) flags |= BIT_HAS_BATTERY;
        if(this.fuelCell != null) flags |= BIT_HAS_FUEL_CELL;
        if(this.powerReceiver != null) flags |= BIT_HAS_POWER_RECEIVER;
        
        pBuff.writeByte(flags);
        if(this.battery != null) this.battery.toBytes(pBuff);
        if(this.fuelCell != null) this.fuelCell.toBytes(pBuff);
        if(this.powerReceiver != null) this.powerReceiver.toBytes(pBuff);
    }
}
