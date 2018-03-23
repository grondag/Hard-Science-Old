package grondag.hard_science.superblock.placement;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.domain.DomainManager;
import grondag.exotic_matter.simulator.domain.DomainUser;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.domain.IDomainCapability;
import grondag.exotic_matter.simulator.domain.Privilege;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;


public class BuildManager implements IDomainCapability
{
    private static final String NBT_SELF = NBTDictionary.claim("buildMgr");
    private static final String NBT_BUILDS = NBTDictionary.claim("buildData");


    /**
     * For use by builds and job tasks related to construction that do not
     * require world access and which don't have in-world machines to service them.
     * For anything that requires world access, create a machine or use World Task Manager.
     */
    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactory()
            {
                private AtomicInteger count = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r)
                {
                    Thread thread = new Thread(r, "Hard Science Build Manager Thread -" + count.getAndIncrement());
                    thread.setDaemon(true);
                    return thread;
                }
            });
    
    /**
     * Convenience method - retrieves active build for given player
     * in the player's current dimension.
     * Will fail if user doesn't have construction rights in current domain.
     */
    @Nullable
    public static Build getActiveBuildForPlayer(EntityPlayerMP player)
    {
        DomainUser user = DomainManager.instance().getActiveDomain(player).findPlayer(player);
        if(user == null || !user.hasPrivilege(Privilege.CONSTRUCTION_EDIT)) return null;
        BuildCapability cap = user.getCapability(BuildCapability.class);
        return cap == null ? null : cap.getActiveBuild(player.world.provider.getDimension());
    }
    
    protected IDomain domain;

    private Int2ObjectMap<Build> builds =Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<Build>());
    
    public Build newBuild(World inWorld)
    {
        return newBuild(inWorld.provider.getDimension());
    }
    
    public Build newBuild(int dimensionID)
    {
        Build result = new Build(this, dimensionID);
        this.builds.put(result.getId(), result);
        Simulator.instance().assignedNumbersAuthority().register(result);
        return result;
    }
    
    @Override
    public IDomain getDomain()
    {
        return this.domain;
    }
    
    @Override
    public void setDirty()
    {
        if(this.domain != null) this.domain.setDirty();
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        NBTTagList nbtBuilds = tag.getTagList(NBT_BUILDS, 10);
        if( nbtBuilds != null && !nbtBuilds.hasNoTags())
        {
            for(NBTBase subTag : nbtBuilds)
            {
                if(subTag != null)
                {
                    Build b = new Build(this, (NBTTagCompound) subTag);
                    this.builds.put(b.getId(), b);
                }
            }   
        }        
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        if(!this.builds.isEmpty())
        {
            NBTTagList nbtJobs = new NBTTagList();
            
            for(Build b : this.builds.values())
            {
                nbtJobs.appendTag(b.serializeNBT());
            }
            tag.setTag(NBT_BUILDS, nbtJobs);
        }        
    }

    @Override
    public String tagName()
    {
        return NBT_SELF;
    }


    @Override
    public void setDomain(IDomain domain)
    {
        this.domain = domain;
    }
 
}
