package grondag.hard_science.simulator.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

import grondag.exotic_matter.Log;
import grondag.exotic_matter.serialization.IReadWriteNBT;
import grondag.exotic_matter.simulator.domain.DomainUser;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.domain.IDomainCapability;
import grondag.exotic_matter.simulator.domain.Privilege;
import grondag.exotic_matter.simulator.persistence.AssignedNumber;
import grondag.exotic_matter.simulator.persistence.IDirtListener;
import grondag.exotic_matter.simulator.persistence.IDirtListenerProvider;
import grondag.exotic_matter.simulator.persistence.IIdentified;
import grondag.hard_science.init.ModNBTTag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class Domain implements IReadWriteNBT, IDirtListenerProvider, IIdentified, IDomain
{
    private static final HashSet<Class<? extends IDomainCapability>> capabilityTypes = new HashSet<>();
    
    public static void registerCapability(Class<? extends IDomainCapability> capabilityType)
    {
        capabilityTypes.add(capabilityType);
    }
    
    private final DomainManager domainManager;
    int id;
    String name;
    boolean isSecurityEnabled;
    private final IdentityHashMap<Class<? extends IDomainCapability>, IDomainCapability> capabilities = new IdentityHashMap<>();

    private final EventBus eventBus = new EventBus();
    
    private HashMap<String, DomainUser> users = new HashMap<String, DomainUser>();
    
    // private constructor
    Domain (DomainManager domainManager)
    {
        this.domainManager = domainManager;    
        
        this.capabilities.clear();
        if(!capabilityTypes.isEmpty())
        {
            for(Class<? extends IDomainCapability> capType : capabilityTypes)
            {
                try
                {
                    IDomainCapability cap;
                    cap = capType.newInstance();
                    cap.setDomain(this);
                    this.capabilities.put(capType, cap);
                }
                catch (Exception e)
                {
                    Log.error("Unable to create domain capability", e);
                }
            }
        }
    }
    
    Domain (DomainManager domainManager, NBTTagCompound tag)
    {
        this(domainManager);
        this.deserializeNBT(tag);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <V extends IDomainCapability> V getCapability(Class<V> capability)
    {
        return (V) this.capabilities.get(capability);
    }
    
    @Override
    public EventBus eventBus()
    {
        return this.eventBus;
    }
    
    @Override
    public List<DomainUser> getAllUsers()
    {
        return ImmutableList.copyOf(users.values());
    }
    
    @Override
    @Nullable
    public DomainUser findPlayer(EntityPlayer player)
    {
        return this.findUser(player.getName());
    }
    
    @Override
    @Nullable
    public DomainUser findUser(String userName)
    {
        return this.users.get(userName);
    }
    
    @Override
    public boolean hasPrivilege(EntityPlayer player, Privilege privilege)
    {
        DomainUser user = findPlayer(player);
        return user == null ? false : user.hasPrivilege(privilege);
    }
    
    /** 
     * Will return existing user if already exists.
     */
    @Override
    public synchronized DomainUser addPlayer(EntityPlayer player)
    {
        return this.addUser(player.getName());
    }
    
    /** 
     * Will return existing user if already exists.
     */
    @Override
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
    
    @Override
    public String getName()
    {
        return name;
    }
    
    @Override
    public void setName(String name)
    {
        this.name = name;
        this.domainManager.isDirty = true;
    }

    @Override
    public boolean isSecurityEnabled()
    {
        return isSecurityEnabled;
    }

    @Override
    public void setSecurityEnabled(boolean isSecurityEnabled)
    {
        this.isSecurityEnabled = isSecurityEnabled;
        this.domainManager.isDirty = true;
    }
    
    @Override
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
    
    @Override
    public void afterDeserialization()
    {
        this.capabilities.values().forEach(c -> c.afterDeserialization());
    }

    @Override
    public void unload()
    {
        this.capabilities.values().forEach(c -> c.unload());
    }

    @Override
    public void loadNew()
    {
        this.capabilities.values().forEach(c -> c.loadNew());
    }
    
    
}