package grondag.hard_science.simulator.demand;

import java.util.HashMap;

import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.IResourcePredicate;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.superblock.block.SuperModelBlock;
import grondag.hard_science.superblock.items.SuperItemBlock;

public class BrokerManager implements IDomainMember
{
//    public final SimpleBroker<StorageTypeStack> ITEM_BROKER = new SimpleBroker<StorageTypeStack>();
//    public final SimpleBroker<StorageTypeStack> FLUID_BROKER = new SimpleBroker<StorageTypeStack>();
//    public final SimpleBroker<StorageTypeStack> GAS_BROKER = new SimpleBroker<StorageTypeStack>();
//    public final SimpleBroker<StorageTypeStack> POWER_BROKER = new SimpleBroker<StorageTypeStack>(this);
//    public final SimpleBroker<StorageTypeStack> BLOCK_BROKER = new SimpleBroker<StorageTypeStack>(this);
    
    private final HashMap<IResource<?>, IBroker<?>> simpleBrokers 
        = new HashMap<IResource<?>, IBroker<?>>();
    
    private final Domain domain;
    
    public BrokerManager(Domain domain)
    {
        this.domain = domain;
    }
    
    public <T extends StorageType<T>> IBroker<T> brokerForResourcePredicate(IResourcePredicate<T> predicate)
    {
        if(predicate.isEqualityPredicate())
        {
            IResource<T> resource = (IResource<T>)predicate;
            
            switch(resource.storageType().enumType)
            {
                case ITEM:
                {
                    ItemResource item = (ItemResource)resource;
                    
                    if(item.getItem() instanceof SuperItemBlock)
                    {
                        if(((SuperItemBlock)item.getItem()).getBlock().getClass() == SuperModelBlock.class)
                        {
//                            return (IBroker<T>) this.BLOCK_BROKER;
                        }
                    }
                }
                    
                case POWER:
                case FLUID:
                    //use per-resource default
                    return this.getOrCreateSimpleBroker(resource);

                case PRIVATE:
                    assert false : "Private storage type reference";
                    return null;
                default:
                    assert false : "Missing enum mapping";
                    return null;
            }
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private <T extends StorageType<T>> IBroker<T> getOrCreateSimpleBroker(IResource<T> resource)
    {
        synchronized(this.simpleBrokers)
        {
            IBroker<?> result = this.simpleBrokers.get(resource);
            if(result == null)
            {
                result = new SimpleBroker<T>(this, resource);
                this.simpleBrokers.put(resource, result);
            }
            return (IBroker<T>) result;
        }
    }

    @Override
    public Domain getDomain()
    {
        return this.domain;
    }

    /**
     * Called by brokers when last (non-inventory) producer
     * and last request are removed from them to free up memory.
     */
    public void removeSimpleBroker(SimpleBroker<?> simpleBroker)
    {
        synchronized(this.simpleBrokers)
        {
            this.simpleBrokers.remove(simpleBroker.resource());
        }        
    }
}
