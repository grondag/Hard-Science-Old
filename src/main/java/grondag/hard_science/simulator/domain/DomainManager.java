package grondag.hard_science.simulator.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.BinaryEnumSet;
import grondag.hard_science.simulator.persistence.AssignedNumbersAuthority;
import grondag.hard_science.simulator.persistence.IIdentified;
import grondag.hard_science.simulator.persistence.IPersistenceNode;
import grondag.hard_science.superblock.virtual.ExcavationRenderTracker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class DomainManager implements IPersistenceNode
{  
    public static final DomainManager INSTANCE = new DomainManager();
    
    boolean isDirty = false;
    
    private boolean isLoaded = false;
    
    private final AssignedNumbersAuthority assignedNumbersAuthority;

    private Domain defaultDomain;

    /** 
     * Each player has a domain that is automatically created for them
     * and which they always own.  This will be their initially active domain.
     */
    private HashMap<String, Domain> playerIntrinsicDomains = new HashMap<String, Domain>();
    
    /** 
     * Each player has a currently active domain. This will initially be their intrinsic domain.
     */
    private HashMap<String, Domain> playerActiveDomains = new HashMap<String, Domain>();
    
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
        this.playerActiveDomains.clear();
        this.playerIntrinsicDomains.clear();
        this.defaultDomain = null;
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
                this.defaultDomain = new Domain(this);
                this.defaultDomain.setSecurityEnabled(false);
                this.defaultDomain.setId(IIdentified.DEFAULT_ID);
                this.defaultDomain.setName("Public");;
                this.assignedNumbersAuthority().domainIndex().register(defaultDomain);
            }
        }
        return this.defaultDomain;
    }
    
    public static final BinaryEnumSet<Privilege> PRIVILEGE_FLAG_SET = new BinaryEnumSet<Privilege>(Privilege.class);
    
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
        Domain result = new Domain(this);
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
        
        if(tag == null) return;
        
        NBTTagList nbtDomains = tag.getTagList(ModNBTTag.DOMAIN_MANAGER_DOMAINS, 10);
        if( nbtDomains != null && !nbtDomains.hasNoTags())
        {
            for (int i = 0; i < nbtDomains.tagCount(); ++i)
            {
                Domain domain = new Domain(this, nbtDomains.getCompoundTagAt(i));
                this.assignedNumbersAuthority().domainIndex().put(domain.id, domain);
            }   
        }
        
        NBTTagCompound nbtPlayerDomains = tag.getCompoundTag(ModNBTTag.DOMAIN_PLAYER_DOMAINS);
        if(nbtPlayerDomains != null && !nbtPlayerDomains.hasNoTags())
        {
            for(String playerName : nbtPlayerDomains.getKeySet())
            {
                Domain d = this.assignedNumbersAuthority().domainIndex().get(nbtPlayerDomains.getInteger(playerName));
                if(d != null) this.playerIntrinsicDomains.put(playerName, d);
            }
        }
        
        NBTTagCompound nbtActiveDomains = tag.getCompoundTag(ModNBTTag.DOMAIN_ACTIVE_DOMAINS);
        if(nbtActiveDomains != null && !nbtActiveDomains.hasNoTags())
        {
            for(String playerName : nbtActiveDomains.getKeySet())
            {
                Domain d = this.assignedNumbersAuthority().domainIndex().get(nbtActiveDomains.getInteger(playerName));
                if(d != null) this.playerActiveDomains.put(playerName, d);
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
        
        if(!this.playerIntrinsicDomains.isEmpty())
        {
            NBTTagCompound nbtPlayerDomains = new NBTTagCompound();
            for(Entry<String, Domain> entry : this.playerIntrinsicDomains.entrySet())
            {
                nbtPlayerDomains.setInteger(entry.getKey(), entry.getValue().getId());
            }
            tag.setTag(ModNBTTag.DOMAIN_PLAYER_DOMAINS, nbtPlayerDomains);
        }
        
        if(!this.playerActiveDomains.isEmpty())
        {
            NBTTagCompound nbtActiveDomains = new NBTTagCompound();
            for(Entry<String, Domain> entry : this.playerActiveDomains.entrySet())
            {
                nbtActiveDomains.setInteger(entry.getKey(), entry.getValue().getId());
            }
            tag.setTag(ModNBTTag.DOMAIN_ACTIVE_DOMAINS, nbtActiveDomains);
        }
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


    /**
     * The player's currently active domain. If player
     * has never specified, will be the player's intrinsic domain.
     */
    @Nonnull
    public Domain getActiveDomain(EntityPlayerMP player)
    {
        Domain result = this.playerActiveDomains.get(player.getName());
        if(result == null)
        {
            synchronized(this.playerActiveDomains)
            {
                result = this.playerActiveDomains.get(player.getName());
                if(result == null)
                {
                    result = this.getIntrinsicDomain(player);
                    this.playerActiveDomains.put(player.getName(), result);
                }
            }
        }
        return result;
    }
    
    /**
     * Set the player's currently active domain. 
     */
    public void setActiveDomain(EntityPlayerMP player, Domain domain)
    {
        synchronized(this.playerActiveDomains)
        {
            Domain result = this.playerActiveDomains.put(player.getName(), domain);
            if(result == null || result != domain )
            {
                ExcavationRenderTracker.INSTANCE.updatePlayerTracking(player);
            }
        }
    }
    
    /**
     * The player's private, default domain. Created if does not already exist.
     */
    @Nonnull
    public Domain getIntrinsicDomain(EntityPlayerMP player)
    {
        Domain result = this.playerIntrinsicDomains.get(player.getName());
        if(result == null)
        {
            synchronized(this.playerIntrinsicDomains)
            {
                result = this.playerIntrinsicDomains.get(player.getName());
                if(result == null)
                {
                    result = this.createDomain();
                    result.setSecurityEnabled(true);
                    //TODO: localize
                    result.setName("Default domain for " + player.getName());
                    DomainUser user = result.addPlayer(player);
                    user.setPrivileges(Privilege.ADMIN);
                    this.playerIntrinsicDomains.put(player.getName(), result);
                }
            }
        }
        return result;
    }
}
