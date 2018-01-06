package grondag.hard_science.simulator.storage;

import javax.annotation.Nonnull;

import org.magicwerk.brownies.collections.Key2List;
import org.magicwerk.brownies.collections.function.IFunction;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;

import grondag.hard_science.network.ModMessages;
import grondag.hard_science.network.server_to_client.PacketOpenContainerItemStorageRefresh;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.ItemResourceDelegate;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.StorageEvent.AfterItemStorageConnect;
import grondag.hard_science.simulator.storage.StorageEvent.BeforeItemStorageDisconnect;
import grondag.hard_science.simulator.storage.StorageEvent.ItemCapacityChange;
import grondag.hard_science.simulator.storage.StorageEvent.ItemStoredUpdate;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;

public class ItemStorageListener
{
    private static enum Mode
    {
        /**
         * View all items in the domain.
         */
        DOMAIN,
        
        /**
         * View all items in or connected to the given store
         */
        CONNECTED,
        
        /**
         * View all items in a single store.
         */
        STORE;
    }
    
    private final Mode mode;
    
    /**
     * Null if in domain mode.
     */
    private final ItemStorage storage;
    
    /**
     * Domain if we are in domain mode, and last
     * known domain of storage in other modes.
     */
    private Domain domain;
    
    protected final Container container;
    protected final EntityPlayerMP player;
    
    protected long totalCapacity;
    
    /**
     * Set true once disconnected and deregistered.
     */
    private boolean isDead = false;
    
    /**
     * Sequence counter for generating resource handles.
     */
    private int nextHandle = 1;
    
    private final static IFunction<ItemResourceDelegate, Integer> RESOURCE_HANDLE_MAPPER
    = new IFunction<ItemResourceDelegate, Integer>() {
       @Override
       public Integer apply(ItemResourceDelegate elem)
       {
           return elem.handle();
       }};
       
    /**
     * All resources currently tracked along with a client-side handle
     */
    protected Key2List<ItemResourceDelegate, ItemResource, Integer> slots 
        = new Key2List.Builder<ItemResourceDelegate, ItemResource, Integer>().
              withPrimaryKey1Map(ItemResourceDelegate::resource).
              withUniqueKey2Map(RESOURCE_HANDLE_MAPPER).
              build();
    
    /**
     * Constructor for domain-level listener.<p>
     * 
     * Creates a new storage listener, but does not subscribe.
     * Subscription should be done via the LogisticsService to 
     * ensure that the initial snapshot is consistent with all 
     * later updates.<p>
     */
    public ItemStorageListener(Domain domain, @Nonnull EntityPlayerMP player)
    {
        this.player = player;
        this.container = player.openContainer;
        this.mode = Mode.DOMAIN;
        this.domain = domain;
        this.storage = null;
    }
    
   /**
    * Creates a new storage listener, but does not subscribe.
    * Subscription should be done via the LogisticsService to 
    * ensure that the initial snapshot is consistent with all 
    * later updates.<p>
    * 
    * @param storage  Storage to listen to. 
    * @param connectedMode If true, will listen to given storage plus 
    * all physically connected storage machines.
    */
            
    public ItemStorageListener(@Nonnull ItemStorage storage, boolean connectedMode, @Nonnull EntityPlayerMP player)
    {
        this.player = player;
        this.container = player.openContainer;
        this.mode = connectedMode ? Mode.CONNECTED : Mode.STORE;
        this.storage = storage;
        this.domain = storage.getDomain();
    }
    
    /**
     * To be called by LogisticsService from service thread.  Will gather starting
     * inventory snapshot, register self for events, and send initial refresh to client.
     */
    public void initialize()
    {
        ImmutableList.Builder<ItemResourceDelegate> builder = ImmutableList.builder();
        switch(this.mode)
        {
        case CONNECTED:
            break;
            
        case DOMAIN:
            this.totalCapacity = this.domain.itemStorage.getCapacity();
            for(AbstractResourceWithQuantity<StorageTypeStack> rwq : this.domain.itemStorage.findQuantityStored(StorageType.ITEM.MATCH_ANY))
            {
                builder.add(this.changeQuantity(rwq.resource(), rwq.getQuantity()));
            }
            break;
            
        case STORE:
            this.addStore(storage, builder);
            break;
            
        default:
            assert false : "Missing enum mapping";
            return;
        }
        this.domain.eventBus.register(this);
        ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(builder.build(), this.totalCapacity, true), player);
    }
    
    /**
     * Returns true if player has disconnected or closed the container.
     */
    public boolean isDead()
    {
        assert !this.isDead : "Dead item storage listener received events";
    
        if(this.player.hasDisconnected() || this.player.openContainer != this.container)
        {
            this.die();
        }
        
        return this.isDead;
    }
    
    private void addStore(AbstractStorage<StorageTypeStack> storage, ImmutableList.Builder<ItemResourceDelegate> builder)
    {
        this.totalCapacity += storage.capacity;
        for(AbstractResourceWithQuantity<StorageTypeStack> rwq : storage.find(StorageType.ITEM.MATCH_ANY))
        {
            builder.add(this.changeQuantity(rwq.resource(), rwq.getQuantity()));
        }
    }
    
    private void removeStore(AbstractStorage<StorageTypeStack> storage, ImmutableList.Builder<ItemResourceDelegate> builder)
    {
        this.totalCapacity -= storage.capacity;
        for(AbstractResourceWithQuantity<StorageTypeStack> rwq : storage.find(StorageType.ITEM.MATCH_ANY))
        {
            builder.add(this.changeQuantity(rwq.resource(), -rwq.getQuantity()));
        }
    }
    
    /**
     * Returns the delegate that was added or changed so that it
     * may be sent to client if desired.  Does not update client.<p>
     * 
     * Does not remove resources that are set to zero quantity because
     * handle values may still be retained on client.
     */
    private ItemResourceDelegate changeQuantity(ItemResource resource, long delta)
    {
        ItemResourceDelegate d = this.slots.getByKey1(resource);
        if(d == null)
        {
            d = new ItemResourceDelegate(this.nextHandle++, resource, delta);
            this.slots.add(d);
        }
        else
        {
            d.setQuantity(d.getQuantity() + delta);
            
            assert d.getQuantity() >= 0 : "Negative resource quantity";
        }
        return d;
    }
   
    private ItemResourceDelegate changeQuantity(IResource<StorageTypeStack> resource, long delta)
    {
        return this.changeQuantity((ItemResource)resource, delta);
    }
    
    @Subscribe
    public void onItemUpdate(ItemStoredUpdate event)
    {
        if(this.isDead()) return;
        
        switch(this.mode)
        {
        case CONNECTED:
            break;
            
        case DOMAIN:
            if(event.storage.getDomain() == this.domain)
            {
                ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(
                        ImmutableList.of(this.changeQuantity(event.resource, event.delta)),
                        this.totalCapacity, 
                        false), player);
            }
            break;
            
        case STORE:
            if(event.storage == this.storage)
            {
                ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(
                        ImmutableList.of(this.changeQuantity(event.resource, event.delta)),
                        this.totalCapacity, 
                        false), player);
            }
            break;
            
        default:
            assert false : "Missing enum mapping";
            break;
        }
    }
    
    @Subscribe
    public void afterItemStorageConnect(AfterItemStorageConnect event)
    {
        if(this.isDead()) return;
        
        switch(this.mode)
        {
        case CONNECTED:
            break;
        case DOMAIN:
            if(event.storage.getDomain() == this.domain)
            {
                ImmutableList.Builder<ItemResourceDelegate> builder = ImmutableList.builder();
                this.addStore(event.storage, builder);
                ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(
                        builder.build(), this.totalCapacity, false), player);
            }
            break;
            
        case STORE:
            assert false : "Attempt to add single item storage to listener outside initialization";
            break;
            
        default:
            assert false : "Missing enum mapping";
            break;
        }
    }
    
    @Subscribe
    public void beforeItemStorageDisconnect(BeforeItemStorageDisconnect event)
    {
        if(this.isDead()) return;
        
        switch(this.mode)
        {
        case CONNECTED:
            break;
        case DOMAIN:
            if(event.storage.getDomain() == this.domain)
            {
                ImmutableList.Builder<ItemResourceDelegate> builder = ImmutableList.builder();
                this.removeStore(event.storage, builder);
                ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(
                        builder.build(), this.totalCapacity, false), player);
            }
            break;
            
        case STORE:
            if(event.storage == this.storage)
            {
                this.slots.clear();
                this.totalCapacity = 0;
                ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(ImmutableList.of(), this.totalCapacity, true), player);
            }
            break;
            
        default:
            assert false : "Missing enum mapping";
            break;
        }
    }
    
    @Subscribe
    public void onCapacityChange(ItemCapacityChange event)
    {
        
        if(this.isDead()) return;
        
        switch(this.mode)
        {
        case CONNECTED:
            break;
        case DOMAIN:
            if(event.storage.getDomain() == this.domain)
            {
                this.totalCapacity += event.delta;
                ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(ImmutableList.of(), this.totalCapacity, false), player);
            }
            break;
            
        case STORE:
            if(event.storage == this.storage)
            {
                this.totalCapacity = event.storage.getCapacity();
                ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(ImmutableList.of(), this.totalCapacity, false), player);
            }
            break;
            
        default:
            assert false : "Missing enum mapping";
            break;
        }
    }

    /**
     * Will deregister for events if haven't already and release resource references.
     */
    public void die()
    {
        if(!this.isDead)
        {
            this.domain.eventBus.unregister(this);
            this.slots.clear();
            this.totalCapacity = 0;
            this.isDead = true;
        }
    }

    public ItemResource getResourceForHandle(int resourceHandle)
    {
        ItemResourceDelegate d = this.slots.getByKey2(resourceHandle);
        return d == null ? null : d.resource();
    }
}
