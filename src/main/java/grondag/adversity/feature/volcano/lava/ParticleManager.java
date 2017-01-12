package grondag.adversity.feature.volcano.lava;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import com.google.common.collect.ComparisonChain;

import grondag.adversity.Adversity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ParticleManager
{
    
    private final HashMap<BlockPos, EntityLavaParticle> map = new HashMap<BlockPos, EntityLavaParticle>();

    private final TreeSet<EntityLavaParticle> set = new TreeSet<EntityLavaParticle>(
            new Comparator<EntityLavaParticle>() {
                @Override
                public int compare(EntityLavaParticle o1, EntityLavaParticle o2)
                {
                    return ComparisonChain.start()
                    // larger first
                    .compare(o2.getFluidAmount(), o1.getFluidAmount())
                    // unique key
                    .compare(o1.id, o2.id)
                    .result();
                  
                }});

            
    public void clear()
    {
        map.clear();
        set.clear();
    }
    
    public int size()
    {
        return map.size();
    }
   
    public EntityLavaParticle get(BlockPos pos)
    {
        return map.get(pos);
    }
    
    public void addLavaForParticle(LavaSimulator sim, BlockPos pos, int fluidAmount)
    {
        EntityLavaParticle particle = map.get(pos);
        
        if(particle == null)
        {
            // important that Y < 0.5 because getPosition from Entity adds .5 and don't want to round up or hashMap will have a key mismatch with TreeSet
            particle = new EntityLavaParticle(sim.world, fluidAmount, 
                    new Vec3d(pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5), Vec3d.ZERO);
            map.put(pos, particle);
        }
        else
        {
            //need to remove from tree before changing amount so can be successfully found
            //and then re-add once updated
            set.remove(particle);
            particle.setFluidAmount(particle.getFluidAmount() + fluidAmount);
            set.add(particle);
        }
        
     
        //TODO: remove for release
        if(map.size() != set.size())
        {
            Adversity.log.warn("Particle tracking error: set size does not match map size.");
        }
        
    }
    
    public EntityLavaParticle pollFirst()
    {
        
        EntityLavaParticle result = this.set.pollFirst();
        if(result != null)
        {
            map.remove(result.getPosition());
         
            //TODO: remove for release
            if(map.size() != set.size())
            {
                Adversity.log.warn("Connection tracking error: set size does not match map size.");
            }
        }
        return result;
    }
    

}
