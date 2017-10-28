package grondag.hard_science.simulator.base;

import java.util.Arrays;

import gnu.trove.map.hash.TIntObjectHashMap;
import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.persistence.IDirtListener;
import grondag.hard_science.simulator.persistence.IDirtListener.IDirtNotifier;
import net.minecraft.nbt.NBTTagCompound;

public class AssignedNumbersAuthority implements IReadWriteNBT, IDirtNotifier
{
    public <T extends IIdentified> IdentifiedIndex<T> createIndex(AssignedNumber numberType)
    {
        return new IdentifiedIndex<T>(numberType);
    }
    
    public class IdentifiedIndex<T extends IIdentified> extends TIntObjectHashMap<T>
    {
        public final AssignedNumber numberType;
        
        private IdentifiedIndex(AssignedNumber numberType)
        {
            this.numberType = numberType;
        }
        
        public void register(T thing)
        {
            IIdentified prior = this.put(thing.getId(), thing);
            
            if(prior != null && !prior.equals(thing))
            {
                Log.warn("Assigned number index overwrote registered object due to index collision.  This is a bug.");
            }
        }
        
        public void unregister(T thing)
        {
            IIdentified prior = this.remove(thing.getId());
            if(prior == null || !prior.equals(thing))
            {
                Log.warn("Assigned number index unregistered wrong object due to index collision.  This is a bug.");
            }
        }
    }
    
    private int[] lastID = new int[AssignedNumber.values().length];
    
    private IDirtListener dirtKeeper = IDirtListener.NullDirtListener.INSTANCE;
    
    public AssignedNumbersAuthority()
    {
        this.clear();
    }
    
    public void clear()
    {
        lastID = new int[AssignedNumber.values().length];
        Arrays.fill(lastID, 999);
    }
    
    /** 
     * First ID returned for each type is 1000 to allow room for system IDs.
     * System ID's should start at 1 to distinguish from missing/unset ID.
     */
    public synchronized int newNumber(AssignedNumber numberType)
    {
        dirtKeeper.setDirty();;
        return ++this.lastID[numberType.ordinal()];
    }
    
    @Override
    public synchronized void deserializeNBT(NBTTagCompound tag)
    {
        int input[] = tag.getIntArray(ModNBTTag.ASSIGNED_NUMBERS_AUTHORITY);
        if(input == null)
        {
            this.clear();
        }
        else
        {
            if(input.length == lastID.length)
            {
                lastID = Arrays.copyOf(input, input.length);
            }
            else
            {
                Log.warn("Simulation assigned numbers save data appears to be corrupt.  World may be borked.");
                this.clear();
                int commonLength = Math.min(lastID.length, input.length);
                System.arraycopy(input, 0, lastID, 0, commonLength);
            }
        }
    }
    
    @Override
    public synchronized void serializeNBT(NBTTagCompound tag)
    {
        tag.setIntArray(ModNBTTag.ASSIGNED_NUMBERS_AUTHORITY, Arrays.copyOf(lastID, lastID.length));
    }
    
    @Override
    public void setDirty()
    {
        this.dirtKeeper.setDirty();
    }

    @Override
    public void setDirtKeeper(IDirtKeeper keeper)
    {
        this.dirtKeeper = keeper;
    }
    
}
