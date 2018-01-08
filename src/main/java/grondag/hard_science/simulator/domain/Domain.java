package grondag.hard_science.simulator.domain;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.demand.BrokerManager;
import grondag.hard_science.simulator.persistence.AssignedNumber;
import grondag.hard_science.simulator.persistence.IDirtListener;
import grondag.hard_science.simulator.persistence.IDirtListenerProvider;
import grondag.hard_science.simulator.persistence.IIdentified;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.FluidStorageManager;
import grondag.hard_science.simulator.storage.ItemStorageManager;
import grondag.hard_science.simulator.storage.PowerStorageManager;
import grondag.hard_science.simulator.storage.StorageManager;
import grondag.hard_science.simulator.storage.jobs.JobManager;
import grondag.hard_science.superblock.placement.BuildManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class Domain implements IReadWriteNBT, IDirtListenerProvider, IIdentified
{
    /**
     * 
     */
    private final DomainManager domainManager;
    int id;
    String name;
    boolean isSecurityEnabled;
    
    public final EventBus eventBus = new EventBus();
    public final StorageManager<StorageTypeStack> itemStorage;
    public final StorageManager<StorageTypeFluid> fluidStorage;
    public final StorageManager<StorageTypePower> powerStorage;
    public final JobManager jobManager;
    public final BuildManager buildManager;
    public final BrokerManager brokerManager;
    
    private HashMap<String, DomainUser> users = new HashMap<String, DomainUser>();
    
    // private constructor
    Domain (DomainManager domainManager)
    {
        this.domainManager = domainManager;    
        this.itemStorage = new ItemStorageManager(this);
        this.fluidStorage = new FluidStorageManager(this);
        this.powerStorage = new PowerStorageManager(this);
        this.jobManager = new JobManager(this);
        this.buildManager = new BuildManager(this);
        this.brokerManager = new BrokerManager(this);
    }
    
    Domain (DomainManager domainManager, NBTTagCompound tag)
    {
        this(domainManager);
        this.deserializeNBT(tag);
    }
    
    public void afterDeserialization()
    {
        this.jobManager.afterDeserialization();
    }
    
    public List<DomainUser> getAllUsers()
    {
        return ImmutableList.copyOf(users.values());
    }
    
    @SuppressWarnings("unchecked")
    public <V extends StorageType<V>> StorageManager<V> getStorageManager(StorageType<V> storeType)
    {
        switch(storeType.enumType)
        {
        case FLUID:
            return null;
        case ITEM:
            return (StorageManager<V>) this.itemStorage;
            
        case POWER:
            return null;
        default:
            return null;
        
        }
    }
    @Nullable
    public DomainUser findPlayer(EntityPlayer player)
    {
        return this.findUser(player.getName());
    }
    
    @Nullable
    public DomainUser findUser(String userName)
    {
        return this.users.get(userName);
    }
    
    public boolean hasPrivilege(EntityPlayer player, Privilege privilege)
    {
        DomainUser user = findPlayer(player);
        return user == null ? false : user.hasPrivilege(privilege);
    }
    
    /** 
     * Will return existing user if already exists.
     */
    public synchronized DomainUser addPlayer(EntityPlayer player)
    {
        return this.addUser(player.getName());
    }
    
    /** 
     * Will return existing user if already exists.
     */
    public synchronized DomainUser addUser(String userName)
    {
        DomainUser result = this.findUser(userName);
        if(result == null)
        {
            result = new DomainUser(this, userName);
            this.users.put(result.userName, result);
            this.domainManager.isDirty = true;
        }
        return result;
    }
    
    @Override
    public int getIdRaw()
    {
        return id;
    }

    @Override
    public void setId(int id)
    {
        this.id = id;
    }
    
    @Override
    public AssignedNumber idType()
    {
        return AssignedNumber.DOMAIN;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
        this.domainManager.isDirty = true;
    }

    public boolean isSecurityEnabled()
    {
        return isSecurityEnabled;
    }

    public void setSecurityEnabled(boolean isSecurityEnabled)
    {
        this.isSecurityEnabled = isSecurityEnabled;
        this.domainManager.isDirty = true;
    }
    
    public void setDirty()
    {
        this.domainManager.isDirty = true;
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.serializeID(tag);
        tag.setBoolean(ModNBTTag.DOMAIN_SECURITY_ENABLED, this.isSecurityEnabled);
        tag.setString(ModNBTTag.DOMAIN_NAME, this.name);
        
        NBTTagList nbtUsers = new NBTTagList();
        
        if(!this.users.isEmpty())
        {
            for (DomainUser user : this.users.values())
            {
                nbtUsers.appendTag(user.serializeNBT());
            }
        }
        tag.setTag(ModNBTTag.DOMAIN_USERS, nbtUsers);
        
        tag.setTag(ModNBTTag.DOMAIN_JOB_MANAGER, this.jobManager.serializeNBT());
        tag.setTag(ModNBTTag.DOMAIN_BUILD_MANAGER, this.buildManager.serializeNBT());
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.deserializeID(tag);
        this.isSecurityEnabled = tag.getBoolean(ModNBTTag.DOMAIN_SECURITY_ENABLED);
        this.name = tag.getString(ModNBTTag.DOMAIN_NAME);
        
        NBTTagList nbtUsers = tag.getTagList(ModNBTTag.DOMAIN_USERS, 10);
        if( nbtUsers != null && !nbtUsers.hasNoTags())
        {
            for (int i = 0; i < nbtUsers.tagCount(); ++i)
            {
                DomainUser user = new DomainUser(this, nbtUsers.getCompoundTagAt(i));
                this.users.put(user.userName, user);
            }   
        }
        
        this.jobManager.deserializeNBT(tag.getCompoundTag(ModNBTTag.DOMAIN_JOB_MANAGER));
        this.buildManager.deserializeNBT(tag.getCompoundTag(ModNBTTag.DOMAIN_BUILD_MANAGER));
    }
    
    public DomainManager domainManager()
    {
        return this.domainManager;
    }
    
    @Override
    public IDirtListener getDirtListener()
    {
        return this.domainManager;
    }
}