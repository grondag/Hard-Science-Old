package grondag.hard_science.simulator.wip;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import gnu.trove.map.hash.TIntObjectHashMap;
import grondag.hard_science.library.varia.BinaryEnumSet;
import grondag.hard_science.simulator.persistence.IPersistenceNode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class DomainManager implements IPersistenceNode
{
    private boolean isDirty;
    
    private int nextID = 1;
    
    private TIntObjectHashMap<Domain> domains = new TIntObjectHashMap<Domain>();
    
    public static interface IDomainMember
    {
        public Domain domain();
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
        Domain result = new Domain(this.nextID++);
        result.name = "Domain " + result.id;
        this.domains.put(result.id, result);
        this.isDirty = true;
        return result;
    }
    
    public class Domain
    {
        private final int id;
        private String name;
        private boolean isSecurityEnabled;
        
        private HashMap<String, DomainUser> users = new HashMap<String, DomainUser>();
        
        // private constructor
        private Domain (int id)
        { 
            this.id = id; 
        }
        
        private Domain (NBTTagCompound tag)
        {
            this.id = tag.getInteger("id");
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
        
        private NBTTagCompound getNBT()
        {
            NBTTagCompound tag = new NBTTagCompound();
            
            tag.setInteger("id", this.id);
            tag.setBoolean("securityOn", this.isSecurityEnabled);
            tag.setString("name", this.name);
            
            NBTTagList nbtUsers = new NBTTagList();
            
            if(!this.users.isEmpty())
            {
                for (DomainUser user : this.users.values())
                {
                    nbtUsers.appendTag(user.getNBT());
                }
            }
            tag.setTag("users", nbtUsers);
            
            return tag;
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


        public class DomainUser
        {
            public final String userName;
            
            private int priveledgeFlags;
            
            private DomainUser(String playerName)
            {
                this.userName = playerName;
            }
            
            private DomainUser(NBTTagCompound tag)
            {
                this.userName = tag.getString("name");
                this.priveledgeFlags = tag.getInteger("flags");
            }
            
            private NBTTagCompound getNBT()
            {
                NBTTagCompound result = new NBTTagCompound();
                result.setString("name", this.userName);
                result.setInteger("flags", this.priveledgeFlags);
                return result;
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
    public void readFromNBT(NBTTagCompound tag)
    {
        this.nextID = 1;
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
    public void writeToNBT(NBTTagCompound tag)
    {
        tag.setInteger("nextID", this.nextID);
        
        NBTTagList nbtDomains = new NBTTagList();
        
        if(!this.domains.isEmpty())
        {
            for (Domain domain : this.domains.valueCollection())
            {
                nbtDomains.appendTag(domain.getNBT());
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
