package grondag.hard_science.simulator.wip;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Log;
import grondag.hard_science.library.varia.BinaryEnumSet;
import grondag.hard_science.simulator.persistence.IDirtListener;
import grondag.hard_science.simulator.persistence.IPersistenceNode;
import grondag.hard_science.simulator.persistence.IReadWriteNBT;
import grondag.hard_science.simulator.wip.AssignedNumbersAuthority.IIdentified;
import grondag.hard_science.simulator.wip.AssignedNumbersAuthority.IdentifiedIndex;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class DomainManager implements IPersistenceNode
{
    public final AssignedNumbersAuthority ASSIGNED_NUMBERS_AUTHORITY = new AssignedNumbersAuthority();
    
    private boolean isDirty;
    
    private boolean isLoaded = false;
    
    private IdentifiedIndex<Domain> domains = this.ASSIGNED_NUMBERS_AUTHORITY.createIndex(AssignedNumber.DOMAIN);
    
    private final IdentifiedIndex<IStorage<?>> storageIndex = this.ASSIGNED_NUMBERS_AUTHORITY.createIndex(AssignedNumber.STORAGE);

    private Domain defaultDomain;
    
    /**
     * If isNew=true then won't wait for a deserialize to become loaded.
     */
    public DomainManager(boolean isNew) 
    {
        this.ASSIGNED_NUMBERS_AUTHORITY.setDirtListener(this);
        this.isLoaded = isNew;
        this.isDirty = isNew;
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
                this.defaultDomain.setId(1);
                this.defaultDomain.setName("Unmanaged");;
                this.domains.register(defaultDomain);
            }
        }
        return this.defaultDomain;
    }
    
    public void clear()
    {
        this.domains.clear();
        this.ASSIGNED_NUMBERS_AUTHORITY.clear();
        this.storageIndex().clear();
        this.defaultDomain = null;
        
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
        ACCESS_INVENTORY
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
        
        public int getId()
        {
            return id;
        }
       
        public void setId(int id)
        {
            this.id = id;
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
            tag.setBoolean("securityOn", this.isSecurityEnabled);
            tag.setString("name", this.name);
            
            NBTTagList nbtUsers = new NBTTagList();
            
            if(!this.users.isEmpty())
            {
                for (DomainUser user : this.users.values())
                {
                    nbtUsers.appendTag(user.serializeNBT());
                }
            }
            tag.setTag("users", nbtUsers);
            
            tag.setTag("itemStorage", this.ITEM_STORAGE.serializeNBT());
        }

        @Override
        public void deserializeNBT(NBTTagCompound tag)
        {
            this.deserializeID(tag);
            this.isSecurityEnabled = tag.getBoolean("securityOn");
            this.name = tag.getString("name");
            
            NBTTagList nbtUsers = tag.getTagList("users", 10);
            if( nbtUsers != null && !nbtUsers.hasNoTags())
            {
                for (int i = 0; i < nbtUsers.tagCount(); ++i)
                {
                    DomainUser user = new DomainUser(nbtUsers.getCompoundTagAt(i));
                    this.users.put(user.userName, user);
                }   
            }
            
            this.ITEM_STORAGE.deserializeNBT(tag.getCompoundTag("itemStorage"));
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
                nbt.setString("name", this.userName);
                nbt.setInteger("flags", this.priveledgeFlags);
                ITEM_STORAGE.serializeNBT(nbt);
            }

            @Override
            public void deserializeNBT(NBTTagCompound nbt)
            {
                this.userName = nbt.getString("name");
                this.priveledgeFlags = nbt.getInteger("flags");
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
        this.ASSIGNED_NUMBERS_AUTHORITY.deserializeNBT(tag);
        this.domains.clear();
        
        // need to do this before loading domains, otherwise they will cause complaints
        this.isLoaded = true;
        
        if(tag != null)
        {
            NBTTagList nbtDomains = tag.getTagList("domains", 10);
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
        this.ASSIGNED_NUMBERS_AUTHORITY.serializeNBT(tag);
        
        NBTTagList nbtDomains = new NBTTagList();
        
        if(!this.domains.isEmpty())
        {
            for (Domain domain : this.domains.valueCollection())
            {
                nbtDomains.appendTag(domain.serializeNBT());
            }
        }
        tag.setTag("domains", nbtDomains);
    }

    @Override
    public String tagName()
    {
        return "HSDomains";
    }

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
}
