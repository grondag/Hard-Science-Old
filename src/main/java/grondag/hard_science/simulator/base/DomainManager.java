package grondag.hard_science.simulator.base;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.BinaryEnumSet;
import grondag.hard_science.simulator.base.AssignedNumbersAuthority.IdentifiedIndex;
import grondag.hard_science.simulator.persistence.IDirtListener;
import grondag.hard_science.simulator.persistence.IPersistenceNode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class DomainManager implements IPersistenceNode
{  
    public static final DomainManager INSTANCE = new DomainManager();
    
    private boolean isDirty = false;
    
    private boolean isLoaded = false;
    
    private final AssignedNumbersAuthority assignedNumbersAuthority;

    private final IdentifiedIndex<Domain> domains;
    
    private final IdentifiedIndex<IStorage<?>> storageIndex;

    private Domain defaultDomain;
    
    /**
     * If isNew=true then won't wait for a deserialize to become loaded.
     */
    private DomainManager() 
    {
        this.assignedNumbersAuthority = new AssignedNumbersAuthority();
        this.assignedNumbersAuthority.setDirtKeeper(this);
        this.domains = assignedNumbersAuthority().createIndex(AssignedNumber.DOMAIN);
        this.storageIndex = assignedNumbersAuthority().createIndex(AssignedNumber.STORAGE);
    }
   
    /**
     * Domain for unmanaged objects.  
     */
    public Domain defaultDomain()
    {
        this.checkLoaded();
        if(this.defaultDomain == null)
        {
            defaultDomain = domains.get(1);
            if(defaultDomain == null)
            {
                this.defaultDomain = new Domain();
                this.defaultDomain.setSecurityEnabled(false);
                this.defaultDomain.setId(IIdentified.DEFAULT_ID);
                this.defaultDomain.setName("Public");;
                this.domains.register(defaultDomain);
            }
        }
        return this.defaultDomain;
    }
    
    public static interface IDomainMember
    {
        public Domain getDomain();
    }
    
    public static enum Priveledge
    {
        ADMIN,
        REMOVE_NODE,
        ADD_NODE,
        ACCESS_INVENTORY,
        CONSTRUCTION_VIEW,
        CONSTRUCTION_EDIT
    }
    
    private static final BinaryEnumSet<Priveledge> PRIVLEDGE_FLAG_SET = new BinaryEnumSet<Priveledge>(Priveledge.class);
    
    public List<Domain> getAllDomains()
    {
        this.checkLoaded();
        return ImmutableList.copyOf(domains.valueCollection());
    }

    public Domain getDomain(int id)
    {
        this.checkLoaded();
        return this.domains.get(id);
    }
    
    public synchronized Domain createDomain()
    {
        this.checkLoaded();
        Domain result = new Domain();
        domains.register(result);
        result.name = "Domain " + result.id;
        this.isDirty = true;
        return result;
    }
    
    /**
     * Does NOT destroy any of the contained objects in the domain!
     */
    public synchronized void removeDomain(Domain domain)
    {
        this.checkLoaded();
        domains.unregister(domain);
        this.isDirty = true;
    }
    
    public class Domain implements IReadWriteNBT, IDirtListenerProvider, IIdentified
    {
        private int id;
        private String name;
        private boolean isSecurityEnabled;
        
        public final ItemStorageManager ITEM_STORAGE = new ItemStorageManager();
        
        private HashMap<String, DomainUser> users = new HashMap<String, DomainUser>();
        
        // private constructor
        private Domain ()
        {
            ITEM_STORAGE.setDomain(this);
        }
        
        private Domain (NBTTagCompound tag)
        {
            this();
            this.deserializeNBT(tag);
        }
        
        public List<DomainUser> getAllUsers()
        {
            return ImmutableList.copyOf(users.values());
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
        
        public boolean hasPriviledge(EntityPlayer player, Priveledge priviledge)
        {
            DomainUser user = findPlayer(player);
            return user == null ? false : user.hasPriveledge(priviledge);
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
                result = new DomainUser(userName);
                this.users.put(result.userName, result);
                isDirty = true;
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
            isDirty = true;
        }

        public boolean isSecurityEnabled()
        {
            return isSecurityEnabled;
        }

        public void setSecurityEnabled(boolean isSecurityEnabled)
        {
            this.isSecurityEnabled = isSecurityEnabled;
            isDirty = true;
        }

        @Override
        public void serializeNBT(NBTTagCompound tag)
        {
            this.serializeID(tag);
            tag.setBoolean(ModNBTTag.DOMIAN_SECURITY_ENABLED, this.isSecurityEnabled);
            tag.setString(ModNBTTag.DOMIAN_NAME, this.name);
            
            NBTTagList nbtUsers = new NBTTagList();
            
            if(!this.users.isEmpty())
            {
                for (DomainUser user : this.users.values())
                {
                    nbtUsers.appendTag(user.serializeNBT());
                }
            }
            tag.setTag(ModNBTTag.DOMIAN_USERS, nbtUsers);
            
            tag.setTag(ModNBTTag.DOMIAN_ITEM_STORAGE, this.ITEM_STORAGE.serializeNBT());
        }

        @Override
        public void deserializeNBT(NBTTagCompound tag)
        {
            this.deserializeID(tag);
            this.isSecurityEnabled = tag.getBoolean(ModNBTTag.DOMIAN_SECURITY_ENABLED);
            this.name = tag.getString(ModNBTTag.DOMIAN_NAME);
            
            NBTTagList nbtUsers = tag.getTagList(ModNBTTag.DOMIAN_USERS, 10);
            if( nbtUsers != null && !nbtUsers.hasNoTags())
            {
                for (int i = 0; i < nbtUsers.tagCount(); ++i)
                {
                    DomainUser user = new DomainUser(nbtUsers.getCompoundTagAt(i));
                    this.users.put(user.userName, user);
                }   
            }
            
            this.ITEM_STORAGE.deserializeNBT(tag.getCompoundTag(ModNBTTag.DOMIAN_ITEM_STORAGE));
        }
        
        public DomainManager domainManager()
        {
            return DomainManager.this;
        }
        
        public class DomainUser implements IReadWriteNBT
        {
            public String userName;
            
            private int priveledgeFlags;
            
            private DomainUser(String playerName)
            {
                this.userName = playerName;
            }
            
            private DomainUser(NBTTagCompound tag)
            {
                this.deserializeNBT(tag);
            }
          
            /**
             * Will return true for admin users, regardless of other Priveledge grants.
             * Will also return true if security is disabled for the domain.
             */
            public boolean hasPriveledge(Priveledge p)
            {
                return  !isSecurityEnabled
                        || PRIVLEDGE_FLAG_SET.isFlagSetForValue(Priveledge.ADMIN, priveledgeFlags)
                        || PRIVLEDGE_FLAG_SET.isFlagSetForValue(p, priveledgeFlags);
            }
            
            public void grantPriveledge(Priveledge p, boolean hasPriveledge)
            {
                this.priveledgeFlags = PRIVLEDGE_FLAG_SET.setFlagForValue(p, priveledgeFlags, hasPriveledge);
                isDirty = true;
            }
            
            public void setPriveledges(Priveledge... granted)
            {
                this.priveledgeFlags = PRIVLEDGE_FLAG_SET.getFlagsForIncludedValues(granted);
                isDirty = true;
            }

            @Override
            public void serializeNBT(NBTTagCompound nbt)
            {
                nbt.setString(ModNBTTag.DOMAIN_USER_NAME, this.userName);
                nbt.setInteger(ModNBTTag.DOMAIN_USER_FLAGS, this.priveledgeFlags);
                ITEM_STORAGE.serializeNBT(nbt);
            }

            @Override
            public void deserializeNBT(NBTTagCompound nbt)
            {
                this.userName = nbt.getString(ModNBTTag.DOMAIN_USER_NAME);
                this.priveledgeFlags = nbt.getInteger(ModNBTTag.DOMAIN_USER_FLAGS);
                ITEM_STORAGE.deserializeNBT(nbt);
            }
        }
        @Override
        public IDirtListener getDirtListener()
        {
            return DomainManager.this;
        }
    }

    @Override
    public boolean isSaveDirty()
    {
        return this.isDirty;
    }

    @Override
    public void setSaveDirty(boolean isDirty)
    {
        this.isDirty = isDirty;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        this.unload();
        
        this.assignedNumbersAuthority.deserializeNBT(tag);

        // need to do this before loading domains, otherwise they will cause complaints
        this.isLoaded = true;
        
        if(tag != null)
        {
            NBTTagList nbtDomains = tag.getTagList(ModNBTTag.DOMAIN_MANAGER_DOMAINS, 10);
            if( nbtDomains != null && !nbtDomains.hasNoTags())
            {
                for (int i = 0; i < nbtDomains.tagCount(); ++i)
                {
                    Domain domain = new Domain(nbtDomains.getCompoundTagAt(i));
                    this.domains.put(domain.id, domain);
                }   
            }
        }
        
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.assignedNumbersAuthority.serializeNBT(tag);
        
        NBTTagList nbtDomains = new NBTTagList();
        
        if(!this.domains.isEmpty())
        {
            for (Domain domain : this.domains.valueCollection())
            {
                nbtDomains.appendTag(domain.serializeNBT());
            }
        }
        tag.setTag(ModNBTTag.DOMAIN_MANAGER_DOMAINS, nbtDomains);
    }

    @Override
    public String tagName()
    {
        return ModNBTTag.DOMAIN_MANAGER;
    }

    public AssignedNumbersAuthority assignedNumbersAuthority() { return this.assignedNumbersAuthority; }
    
    public IdentifiedIndex<IStorage<?>> storageIndex()
    {
        this.checkLoaded();
        return storageIndex;
    }
    
    private boolean checkLoaded()
    {
        if(!this.isLoaded)
        {
            Log.warn("Domain manager accessed before it was loaded.  This is a bug and probably means simulation state has been lost.");
        }
        return this.isLoaded;
    }
    
    /**
     * Called at shutdown
     */
    public void unload()
    {
        this.domains.clear();
        this.storageIndex.clear();
        this.assignedNumbersAuthority.clear();
        this.isLoaded = false;
    }
    
    /**
     * Called by simulator if starting new world/simulation.
     */
    public void loadNew()
    {
        this.unload();
        this.isLoaded = true;
    }
    
}
