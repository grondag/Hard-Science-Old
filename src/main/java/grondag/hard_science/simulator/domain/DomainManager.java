package grondag.hard_science.simulator.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.domain.DomainUser;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.domain.Privilege;
import grondag.exotic_matter.simulator.persistence.AssignedNumber;
import grondag.exotic_matter.simulator.persistence.AssignedNumbersAuthority.IdentifiedIndex;
import grondag.exotic_matter.simulator.persistence.IIdentified;
import grondag.exotic_matter.simulator.persistence.ISimulationTopNode;
import grondag.hard_science.Log;
import grondag.hard_science.init.ModNBTTag;
import grondag.hard_science.simulator.jobs.AbstractTask;
import grondag.hard_science.simulator.jobs.Job;
import grondag.hard_science.superblock.placement.Build;
import grondag.hard_science.superblock.virtual.ExcavationRenderTracker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class DomainManager implements ISimulationTopNode
{  
    /**
     * A bit ugly but convenient.  Set to null when any 
     * instance is created, retrieved lazily from Simulator.
     * 
     * TODO do this in a better way, is asking for trouble
     */
    private static DomainManager instance;
    
    public static DomainManager instance()
    {
        if(instance == null)
        {
            instance = Simulator.instance().getNode(DomainManager.class);
        }
        return instance;
    }
    
    private boolean isDeserializationInProgress = false;
    
    boolean isDirty = false;
    
    private boolean isLoaded = false;

    private IDomain defaultDomain;

    /** 
     * Each player has a domain that is automatically created for them
     * and which they always own.  This will be their initially active domain.
     */
    private HashMap<String, IDomain> playerIntrinsicDomains = new HashMap<>();
    
    /** 
     * Each player has a currently active domain. This will initially be their intrinsic domain.
     */
    private HashMap<String, IDomain> playerActiveDomains = new HashMap<>();
    
    /**
     * If isNew=true then won't wait for a deserialize to become loaded.
     */
    public DomainManager() 
    {
        // force refresh of singleton reference
        instance = null;

    }
   
    /**
     * Called at shutdown
     */
    @Override
    public void unload()
    {
        this.playerActiveDomains.clear();
        this.playerIntrinsicDomains.clear();
        this.defaultDomain = null;
        this.isLoaded = false;
    }
    
    @Override
    public void loadNew()
    {
        this.unload();
        this.isLoaded = true;
    }
    
    /**
     * Domain for unmanaged objects.  
     */
    public IDomain defaultDomain()
    {
        this.checkLoaded();
        if(this.defaultDomain == null)
        {
            defaultDomain = domainFromId(1);
            if(defaultDomain == null)
            {
                this.defaultDomain = new Domain(this);
                this.defaultDomain.setSecurityEnabled(false);
                this.defaultDomain.setId(IIdentified.DEFAULT_ID);
                this.defaultDomain.setName("Public");;
                Simulator.instance().assignedNumbersAuthority().register(defaultDomain);
            }
        }
        return this.defaultDomain;
    }
    
    public List<IDomain> getAllDomains()
    {
        this.checkLoaded();
        ImmutableList.Builder<IDomain> builder = ImmutableList.builder();
        for (IIdentified domain : Simulator.instance().assignedNumbersAuthority().getIndex(AssignedNumber.DOMAIN).valueCollection())
        {
            builder.add((Domain)domain);
        }
        return builder.build();
    }

    public IDomain getDomain(int id)
    {
        this.checkLoaded();
        return domainFromId(id);
    }
    
    public synchronized IDomain createDomain()
    {
        this.checkLoaded();
        Domain result = new Domain(this);
        Simulator.instance().assignedNumbersAuthority().register(result);
        result.name = "Domain " + result.id;
        this.isDirty = true;
        return result;
    }
    
    /**
     * Does NOT destroy any of the contained objects in the domain!
     */
    public synchronized void removeDomain(IDomain domain)
    {
        this.checkLoaded();
        Simulator.instance().assignedNumbersAuthority().unregister(domain);
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
        this.isDeserializationInProgress = true;
        
        this.unload();

        // need to do this before loading domains, otherwise they will cause complaints
        this.isLoaded = true;
        
        if(tag == null) return;
        
        NBTTagList nbtDomains = tag.getTagList(ModNBTTag.DOMAIN_MANAGER_DOMAINS, 10);
        if( nbtDomains != null && !nbtDomains.hasNoTags())
        {
            for (int i = 0; i < nbtDomains.tagCount(); ++i)
            {
                Domain domain = new Domain(this, nbtDomains.getCompoundTagAt(i));
                Simulator.instance().assignedNumbersAuthority().register(domain);
            }   
        }
        
        NBTTagCompound nbtPlayerDomains = tag.getCompoundTag(ModNBTTag.DOMAIN_PLAYER_DOMAINS);
        if(nbtPlayerDomains != null && !nbtPlayerDomains.hasNoTags())
        {
            for(String playerName : nbtPlayerDomains.getKeySet())
            {
                IDomain d = domainFromId(nbtPlayerDomains.getInteger(playerName));
                if(d != null) this.playerIntrinsicDomains.put(playerName, d);
            }
        }
        
        NBTTagCompound nbtActiveDomains = tag.getCompoundTag(ModNBTTag.DOMAIN_ACTIVE_DOMAINS);
        if(nbtActiveDomains != null && !nbtActiveDomains.hasNoTags())
        {
            for(String playerName : nbtActiveDomains.getKeySet())
            {
                IDomain d = domainFromId(nbtActiveDomains.getInteger(playerName));
                if(d != null) this.playerActiveDomains.put(playerName, d);
            }
        }
        
        this.isDeserializationInProgress = false;
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        NBTTagList nbtDomains = new NBTTagList();
        
        IdentifiedIndex domains = Simulator.instance().assignedNumbersAuthority().getIndex(AssignedNumber.DOMAIN);
        
        if(!domains.isEmpty())
        {
            for (IIdentified domain : domains.valueCollection())
            {
                nbtDomains.appendTag(((Domain)domain).serializeNBT());
            }
        }
        tag.setTag(ModNBTTag.DOMAIN_MANAGER_DOMAINS, nbtDomains);
        
        if(!this.playerIntrinsicDomains.isEmpty())
        {
            NBTTagCompound nbtPlayerDomains = new NBTTagCompound();
            for(Entry<String, IDomain> entry : this.playerIntrinsicDomains.entrySet())
            {
                nbtPlayerDomains.setInteger(entry.getKey(), entry.getValue().getId());
            }
            tag.setTag(ModNBTTag.DOMAIN_PLAYER_DOMAINS, nbtPlayerDomains);
        }
        
        if(!this.playerActiveDomains.isEmpty())
        {
            NBTTagCompound nbtActiveDomains = new NBTTagCompound();
            for(Entry<String, IDomain> entry : this.playerActiveDomains.entrySet())
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
    public IDomain getActiveDomain(EntityPlayerMP player)
    {
        IDomain result = this.playerActiveDomains.get(player.getName());
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
    public void setActiveDomain(EntityPlayerMP player, IDomain domain)
    {
        synchronized(this.playerActiveDomains)
        {
            IDomain result = this.playerActiveDomains.put(player.getName(), domain);
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
    public IDomain getIntrinsicDomain(EntityPlayerMP player)
    {
        IDomain result = this.playerIntrinsicDomains.get(player.getName());
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
    
    public boolean isDeserializationInProgress()
    {
        return this.isDeserializationInProgress;
    }
    
    // convenience object lookup methods
    public static IDomain domainFromId(int id)
    {
        return (Domain)  Simulator.instance().assignedNumbersAuthority().get(id, AssignedNumber.DOMAIN);
    }
    
    public static Job jobFromId(int id)
    {
        return (Job)  Simulator.instance().assignedNumbersAuthority().get(id, AssignedNumber.JOB);
    }
    
    public static AbstractTask taskFromId(int id)
    {
        return (AbstractTask)  Simulator.instance().assignedNumbersAuthority().get(id, AssignedNumber.TASK);
    }
    
    public static Build buildFromId(int id)
    {
        return (Build)  Simulator.instance().assignedNumbersAuthority().get(id, AssignedNumber.BUILD);
    }
    
    @Override
    public void afterDeserialization()
    {
        IdentifiedIndex domains =  Simulator.instance().assignedNumbersAuthority().getIndex(AssignedNumber.DOMAIN);
        
        if(!domains.isEmpty())
        {
            for (IIdentified domain : domains.valueCollection())
            {
                ((Domain)domain).afterDeserialization();
            }
        }
    }

}
