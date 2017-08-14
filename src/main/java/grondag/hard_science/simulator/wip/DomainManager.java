package grondag.hard_science.simulator.wip;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

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
    public static final DomainManager INSTANCE = new DomainManager();
    public final AssignedNumbersAuthority ASSIGNED_NUMBERS_AUTHORITY = new AssignedNumbersAuthority();
    
    private boolean isDirty;
    
    private IdentifiedIndex<Domain> domains = this.ASSIGNED_NUMBERS_AUTHORITY.createIndex(AssignedNumber.DOMAIN);
    
    public final IdentifiedIndex<IStorage<?>> STORAGE_INDEX = this.ASSIGNED_NUMBERS_AUTHORITY.createIndex(AssignedNumber.STORAGE);
    
    /**
     * alternatives to {@link #INSTANCE} should only be used for testing.
     */
    public DomainManager() 
    {
        this.ASSIGNED_NUMBERS_AUTHORITY.setDirtListener(this);
    }
   
    public void clear()
    {
        this.domains.clear();
        this.ASSIGNED_NUMBERS_AUTHORITY.clear();
        this.STORAGE_INDEX.clear();
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
        return ImmutableList.copyOf(domains.valueCollection());
    }

    public Domain getDomain(int id)
    {
        return this.domains.get(id);
    }
    
    public synchronized Domain createDomain()
    {
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
        domains.unregister(domain);
        this.isDirty = true;
    }
    
    public class Domain implements IReadWriteNBT, IDirtListenerProvider, IIdentified
    {
        private int id;
        private String name;
        private boolean isSecurityEnabled;
        
        private HashMap<String, DomainUser> users = new HashMap<String, DomainUser>();
        
        // private constructor
        private Domain () {}
        
        private Domain (NBTTagCompound tag)
        {
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
            this.writeIdToNBT(tag);
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
        }

        @Override
        public void deserializeNBT(NBTTagCompound tag)
        {
            this.readIdFromNBT(tag);
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
            }

            @Override
            public void deserializeNBT(NBTTagCompound nbt)
            {
                this.userName = nbt.getString("name");
                this.priveledgeFlags = nbt.getInteger("flags");
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
        return "HS_Domains";
    }
}
