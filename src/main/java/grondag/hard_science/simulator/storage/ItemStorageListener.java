package grondag.hard_science.simulator.storage;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.magicwerk.brownies.collections.Key2List;
import org.magicwerk.brownies.collections.function.IFunction;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import grondag.hard_science.simulator.storage.ItemStorageEvent.AfterItemStorageConnect;
import grondag.hard_science.simulator.storage.ItemStorageEvent.BeforeItemStorageDisconnect;
import grondag.hard_science.simulator.storage.ItemStorageEvent.ItemCapacityChange;
import grondag.hard_science.simulator.storage.ItemStorageEvent.ItemStoredUpdate;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;

// TODO: add and handle events for connectivity changes in connected mode
// TODO: add and handle events for machine on/off changes in connected mode

public class ItemStorageListener implements IStorageAccess<StorageTypeStack>
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
     * Used in connected mode to track which stores are currently
     * connected to the store in {@link #storage}. Includes 
     * the primary store.  Null in domain mode.
     */
    protected final Set<IStorage<StorageTypeStack>> stores;
    
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
        this.stores = null;
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
            
    public ItemStorageListener(@Nonnull ItemStorage storage, @Nonnull EntityPlayerMP player)
    {
        this.player = player;
        this.container = player.openContainer;
        this.mode = storage.isOn() ? Mode.CONNECTED : Mode.STORE;
        this.stores = storage.isOn() 
                ? new HashSet<IStorage<StorageTypeStack>>()
                : ImmutableSet.of(storage);
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
        case STORE:
            this.addStore(this.storage, builder);
            break;
            
        case CONNECTED:
            for(IStorage<StorageTypeStack> store : this.domain.itemStorage.stores())
            {
                ItemStorage itemStore = (ItemStorage)store;
                if(itemStore.isOn() && LogisticsService.ITEM_SERVICE.areDevicesConnected(this.storage.device(), itemStore.device()))
                {
                    this.stores.add(store);
                    this.addStore(itemStore, builder);
                }
            }
            break;
            
        case DOMAIN:
            this.totalCapacity = this.domain.itemStorage.getCapacity();
            for(AbstractResourceWithQuantity<StorageTypeStack> rwq : this.domain.itemStorage.findQuantityStored(StorageType.ITEM.MATCH_ANY))
            {
                builder.add(this.changeQuantity(rwq.resource(), rwq.getQuantity()));
            }
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
    
    private void addStore(ItemStorage storage, ImmutableList.Builder<ItemResourceDelegate> builder)
    {
        this.totalCapacity += storage.getCapacity();
        for(AbstractResourceWithQuantity<StorageTypeStack> rwq : storage.find(StorageType.ITEM.MATCH_ANY))
        {
            builder.add(this.changeQuantity(rwq.resource(), rwq.getQuantity()));
        }
    }
    
    private void removeStore(ItemStorage storage, ImmutableList.Builder<ItemResourceDelegate> builder)
    {
        this.totalCapacity -= storage.getCapacity();
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
        case STORE:
            if(this.stores.contains(event.storage))
            {
                ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(
                        ImmutableList.of(this.changeQuantity(event.resource, event.delta)),
                        this.totalCapacity, 
                        false), player);
            }
            break;
            
        case DOMAIN:
            ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(
                    ImmutableList.of(this.changeQuantity(event.resource, event.delta)),
                    this.totalCapacity, 
                    false), player);
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
        {
            assert event.storage != this.storage : "Attempt to reconnect primary storage to listener.";
            
            if(event.storage.isOn() && !this.stores.contains(event.storage) 
                    && LogisticsService.ITEM_SERVICE.areDevicesConnected(this.storage.device(), event.storage.device()))
            {
                    ImmutableList.Builder<ItemResourceDelegate> builder = ImmutableList.builder();
                    this.addStore((ItemStorage) event.storage, builder);
                    this.stores.add((ItemStorage) event.storage);
                    ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(
                            builder.build(), this.totalCapacity, false), player);
            }
            break;
        }
        
        case DOMAIN:
        {
            ImmutableList.Builder<ItemResourceDelegate> builder = ImmutableList.builder();
            this.addStore((ItemStorage) event.storage, builder);
            ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(
                    builder.build(), this.totalCapacity, false), player);
            break;
        }
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
        {
            if(event.storage == this.storage)
            {
                this.slots.clear();
                this.totalCapacity = 0;
                ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(ImmutableList.of(), this.totalCapacity, true), player);
                this.die();
            }
            else if(this.stores.contains(event.storage) 
                    && !LogisticsService.ITEM_SERVICE.areDevicesConnected(this.storage.device(), event.storage.device()))
            {
                    ImmutableList.Builder<ItemResourceDelegate> builder = ImmutableList.builder();
                    this.removeStore((ItemStorage) event.storage, builder);
                    this.stores.remove((ItemStorage) event.storage);
                    ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(
                            builder.build(), this.totalCapacity, false), player);
                
            }
            break;
        }
        
        case DOMAIN:
        {
            ImmutableList.Builder<ItemResourceDelegate> builder = ImmutableList.builder();
            this.removeStore((ItemStorage) event.storage, builder);
            ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(
                    builder.build(), this.totalCapacity, false), player);
            break;
        }
        
        case STORE:
            if(event.storage == this.storage)
            {
                this.slots.clear();
                this.totalCapacity = 0;
                ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(ImmutableList.of(), this.totalCapacity, true), player);
                this.die();
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
            if(this.stores.contains(event.storage))
            {
                this.totalCapacity += event.delta;
                ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(ImmutableList.of(), this.totalCapacity, false), player);
            }
            break;
            
        case DOMAIN:
            this.totalCapacity += event.delta;
            ModMessages.INSTANCE.sendTo(new PacketOpenContainerItemStorageRefresh(ImmutableList.of(), this.totalCapacity, false), player);
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

    @Override
    public ImmutableList<IStorage<StorageTypeStack>> stores()
    {
        return ImmutableList.copyOf(this.stores);
    }

    @Override
    public ImmutableList<IStorage<StorageTypeStack>> findSpaceFor(IResource<StorageTypeStack> resource, long quantity)
    {
        return this.mode == Mode.DOMAIN
                ? this.domain.itemStorage.findSpaceFor(resource, quantity)
                : IStorageAccess.super.findSpaceFor(resource, quantity);
    }

    @Override
    public ImmutableList<IStorage<StorageTypeStack>> getLocations(IResource<StorageTypeStack> resource)
    {
        return this.mode == Mode.DOMAIN
                ? this.domain.itemStorage.getLocations(resource)
                : IStorageAccess.super.getLocations(resource);
    }

    public long getQuantityStored(ItemResource targetResource)
    {
        ItemResourceDelegate d = this.slots.getByKey1(targetResource);
        return d == null ? 0 : d.getQuantity();
    }
}
