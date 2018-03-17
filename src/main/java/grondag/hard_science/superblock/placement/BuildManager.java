package grondag.hard_science.superblock.placement;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import grondag.hard_science.init.ModNBTTag;
import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.domain.Privilege;
import grondag.hard_science.simulator.domain.DomainUser;
import grondag.hard_science.simulator.persistence.IDirtListener;
import grondag.hard_science.simulator.persistence.NullDirtListener;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;


public class BuildManager implements IReadWriteNBT, IDomainMember
{
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
    
    
    public BuildManager(Domain domain)
    {
        this.domain = domain;
        this.dirtListener = domain == null ? NullDirtListener.INSTANCE : domain.getDirtListener();
    }

    /**
     * Convenience method - retrieves active build for given player
     * in the player's current dimension.
     * Will fail if user doesn't have construction rights in current domain.
     */
    @Nullable
    public static Build getActiveBuildForPlayer(EntityPlayerMP player)
    {
        DomainUser user = DomainManager.instance().getActiveDomain(player).findPlayer(player);
        return user == null || !user.hasPrivilege(Privilege.CONSTRUCTION_EDIT) ? null : user.getActiveBuild(player.world.provider.getDimension());
    }
    
    protected Domain domain;

    protected IDirtListener dirtListener = NullDirtListener.INSTANCE;
    
    private Int2ObjectMap<Build> builds =Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<Build>());
    
    public Build newBuild(World inWorld)
    {
        return newBuild(inWorld.provider.getDimension());
    }
    
    public Build newBuild(int dimensionID)
    {
        Build result = new Build(this, dimensionID);
        this.builds.put(result.getId(), result);
        domain.domainManager().assignedNumbersAuthority().register(result);
        return result;
    }
    
    @Override
    public Domain getDomain()
    {
        return this.domain;
    }
    
    void setDirty()
    {
        this.dirtListener.setDirty();
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        NBTTagList nbtBuilds = tag.getTagList(ModNBTTag.BUILD_MANAGER_BUILDS, 10);
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
            tag.setTag(ModNBTTag.BUILD_MANAGER_BUILDS, nbtJobs);
        }        
    }
 
}
