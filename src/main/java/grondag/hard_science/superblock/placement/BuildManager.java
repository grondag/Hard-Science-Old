package grondag.hard_science.superblock.placement;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import grondag.hard_science.library.serialization.IReadWriteNBT;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.base.DomainManager.Domain;
import grondag.hard_science.simulator.base.DomainManager.IDomainMember;
import grondag.hard_science.simulator.persistence.IDirtListener;
import grondag.hard_science.simulator.persistence.IDirtListener.NullDirtListener;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;


public class BuildManager implements IReadWriteNBT, IDomainMember
{
    static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
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
    
    protected Domain domain;

    protected IDirtListener dirtListener = NullDirtListener.INSTANCE;
    
    private Int2ObjectOpenHashMap<Build> builds = new Int2ObjectOpenHashMap<Build>();
    
    public Build newBuild(World inWorld)
    {
        Build result = new Build(this, inWorld);
        this.builds.put(result.getId(), result);
        return result;
    }
    
    @Override
    public Domain getDomain()
    {
        return this.domain;
    }
    
    public void setDomain(Domain domain)
    {
        this.domain = domain;
        this.dirtListener = domain.getDirtListener();
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
                    Build b = new Build(this);
                    b.deserializeID((NBTTagCompound) subTag);
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