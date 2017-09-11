package grondag.hard_science.simulator.wip;

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
        private final AssignedNumber numberType;
        
        private IdentifiedIndex(AssignedNumber numberType)
        {
            this.numberType = numberType;
        }
        
        /**
         * If id is < 1 will assign a new ID before adding to collection.
         */
        public void register(T thing)
        {
            if(thing.getId() < 1) thing.setId(newNumber(this.numberType));
            
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
            
            if(thing.getId() < 1) thing.setId(newNumber(this.numberType));
            this.put(thing.getId(), thing);
        }
    }
    
    private int[] lastID = new int[AssignedNumber.values().length];
    
    private IDirtListener dirtListener = IDirtListener.NullDirtListener.INSTANCE;
    
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
        dirtListener.setDirty();;
        return ++this.lastID[numberType.ordinal()];
    }
    
    @Override
    public synchronized void deserializeNBT(NBTTagCompound tag)
    {
        int input[] = tag.getIntArray(ModNBTTag.ASSIGNED_NUMBERS_AUTHORITY.tag);
        if(input == null)
        {
            this.clear();
        }
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
        tag.setIntArray(ModNBTTag.ASSIGNED_NUMBERS_AUTHORITY.tag, Arrays.copyOf(lastID, lastID.length));
    }
    
    @Override
    public void setDirty()
    {
        this.dirtListener.setDirty();
    }

    @Override
    public void setDirtListener(IDirtKeeper listener)
    {
        this.dirtListener = listener;
    }
    
}
