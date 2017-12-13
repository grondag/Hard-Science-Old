package grondag.hard_science.simulator.persistence;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import gnu.trove.map.hash.TIntObjectHashMap;
import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import net.minecraft.nbt.NBTTagCompound;

public class AssignedNumbersAuthority implements IReadWriteNBT, IDirtNotifier
{
    
    public IdentifiedIndex createIndex(AssignedNumber numberType)
    {
        return new IdentifiedIndex(numberType);
    }
    
    public class IdentifiedIndex extends TIntObjectHashMap<IIdentified>
    {
        public final AssignedNumber numberType;
        
        private IdentifiedIndex(AssignedNumber numberType)
        {
            this.numberType = numberType;
        }
        
        public synchronized void register(IIdentified thing)
        {
            IIdentified prior = this.put(thing.getId(), thing);
            
            if(prior != null && !prior.equals(thing))
            {
                Log.warn("Assigned number index overwrote registered object due to index collision.  This is a bug.");
            }
        }
        
        public synchronized void unregister(IIdentified thing)
        {
            IIdentified prior = this.remove(thing.getId());
            if(prior == null || !prior.equals(thing))
            {
                Log.warn("Assigned number index unregistered wrong object due to index collision.  This is a bug.");
            }
        }
        
        public synchronized IIdentified get(int index)
        {
            return super.get(index);
        }
    }
    
    private int[] lastID = new int[AssignedNumber.values().length];
    
    
    private IDirtListener dirtKeeper = NullDirtListener.INSTANCE;
    
    private final IdentifiedIndex[] indexes;
    
    public AssignedNumbersAuthority()
    {
        this.indexes = new IdentifiedIndex[AssignedNumber.values().length];
        for(int i = 0; i < AssignedNumber.values().length; i++)
        {
            this.indexes[i] = createIndex(AssignedNumber.values()[i]);
        }
        this.clear();
    }
    
    public void register(@Nonnull IIdentified registrant)
    {
        this.indexes[registrant.idType().ordinal()].register(registrant);
    }
    
    public void unregister(@Nonnull IIdentified registrant)
    {
        this.indexes[registrant.idType().ordinal()].unregister(registrant);
    }
    
    @Nullable
    public IIdentified get(int id, @Nonnull AssignedNumber idType)
    {
        return this.indexes[idType.ordinal()].get(id);
    }
    
    public void clear()
    {
        lastID = new int[AssignedNumber.values().length];
        Arrays.fill(lastID, 999);
        for(int i = 0; i < AssignedNumber.values().length; i++)
        {
            this.indexes[i].clear();
        }
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

    public IdentifiedIndex getIndex(AssignedNumber idType)
    {
        return this.indexes[idType.ordinal()];
    }
    
}
