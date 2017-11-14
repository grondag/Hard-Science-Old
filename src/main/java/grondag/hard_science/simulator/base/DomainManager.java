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
import grondag.hard_science.simulator.base.jobs.AbstractTask;
import grondag.hard_science.simulator.base.jobs.Job;
import grondag.hard_science.simulator.base.jobs.JobManager;
import grondag.hard_science.simulator.persistence.IDirtListener;
import grondag.hard_science.simulator.persistence.IPersistenceNode;
import grondag.hard_science.superblock.placement.BuildManager;
import grondag.hard_science.superblock.placement.BuildManager.Build;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class DomainManager implements IPersistenceNode
{  
    public static final DomainManager INSTANCE = new DomainManager();
    
    private boolean isDirty = false;
    
    private boolean isLoaded = false;
    
    private final AssignedNumbersAuthority assignedNumbersAuthority;

    private Domain defaultDomain;
    
    /**
     * If isNew=true then won't wait for a deserialize to become loaded.
     */
    private DomainManager() 
    {
        this.assignedNumbersAuthority = new AssignedNumbersAuthority();
        this.assignedNumbersAuthority.setDirtKeeper(this);
    }
   
    
    /**
     * Called at shutdown
     */
    public void unload()
    {
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
    
    /**
     * Domain for unmanaged objects.  
     */
    public Domain defaultDomain()
    {
        this.checkLoaded();
        if(this.defaultDomain == null)
        {
            defaultDomain = this.assignedNumbersAuthority().domainIndex().get(1);
            if(defaultDomain == null)
            {
                this.defaultDomain = new Domain();
                this.defaultDomain.setSecurityEnabled(false);
                this.defaultDomain.setId(IIdentified.DEFAULT_ID);
                this.defaultDomain.setName("Public");;
                this.assignedNumbersAuthority().domainIndex().register(defaultDomain);
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
        return ImmutableList.copyOf(this.assignedNumbersAuthority().domainIndex().valueCollection());
    }

    public Domain getDomain(int id)
    {
        this.checkLoaded();
        return this.assignedNumbersAuthority().domainIndex().get(id);
    }
    
    public synchronized Domain createDomain()
    {
        this.checkLoaded();
        Domain result = new Domain();
        this.assignedNumbersAuthority().domainIndex().register(result);
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
        this.assignedNumbersAuthority().domainIndex().unregister(domain);
        this.isDirty = true;
    }
    
    public class Domain implements IReadWriteNBT, IDirtListenerProvider, IIdentified
    {
        private int id;
        private String name;
        private boolean isSecurityEnabled;
        
        public final ItemStorageManager ITEM_STORAGE = new ItemStorageManager();
        public final JobManager JOB_MANAGER = new JobManager();
        public final BuildManager BUILD_MANAGER = new BuildManager();
        
        private HashMap<String, DomainUser> users = new HashMap<String, DomainUser>();
        
        // private constructor
        private Domain ()
        {
            ITEM_STORAGE.setDomain(this);
            JOB_MANAGER.setDomain(this);
            BUILD_MANAGER.setDomain(this);
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
            
            tag.setTag(ModNBTTag.DOMAIN_ITEM_STORAGE, this.ITEM_STORAGE.serializeNBT());
            tag.setTag(ModNBTTag.DOMAIN_JOB_MANAGER, this.JOB_MANAGER.serializeNBT());
            tag.setTag(ModNBTTag.DOMAIN_BUILD_MANAGER, this.BUILD_MANAGER.serializeNBT());
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
                    DomainUser user = new DomainUser(nbtUsers.getCompoundTagAt(i));
                    this.users.put(user.userName, user);
                }   
            }
            
            this.ITEM_STORAGE.deserializeNBT(tag.getCompoundTag(ModNBTTag.DOMAIN_ITEM_STORAGE));
            this.JOB_MANAGER.deserializeNBT(tag.getCompoundTag(ModNBTTag.DOMAIN_JOB_MANAGER));
            this.BUILD_MANAGER.deserializeNBT(tag.getCompoundTag(ModNBTTag.DOMAIN_BUILD_MANAGER));
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
                    this.assignedNumbersAuthority().domainIndex().put(domain.id, domain);
                }   
            }
        }
        
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        this.assignedNumbersAuthority.serializeNBT(tag);
        
        NBTTagList nbtDomains = new NBTTagList();
        
        if(!this.assignedNumbersAuthority().domainIndex().isEmpty())
        {
            for (Domain domain : this.assignedNumbersAuthority().domainIndex().valueCollection())
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
    
    private boolean checkLoaded()
    {
        if(!this.isLoaded)
        {
            Log.warn("Domain manager accessed before it was loaded.  This is a bug and probably means simulation state has been lost.");
        }
        return this.isLoaded;
    }
}
