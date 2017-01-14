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
    
    private final HashMap<BlockPos, ParticleInfo> map = new HashMap<BlockPos, ParticleInfo>();

    private final TreeSet<ParticleInfo> set = new TreeSet<ParticleInfo>(
            new Comparator<ParticleInfo>() {
                @Override
                public int compare(ParticleInfo o1, ParticleInfo o2)
                {
                    return ComparisonChain.start()
                    // oldest first
                    .compare(o1.tickCreated, o2.tickCreated)
                    // larger first
                    .compare(o2.fluidUnits, o1.fluidUnits)
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
   
    public void addLavaForParticle(LavaSimulator sim, BlockPos pos, int fluidAmount)
    {
        ParticleInfo particle = map.get(pos);
        
        if(particle == null)
        {
            particle = new ParticleInfo(sim.getTickIndex(), pos, fluidAmount);
            map.put(pos, particle);
            set.add(particle);
            Adversity.log.info("ParticleManager added new particle @" + particle.pos.toString() + " with amount=" + particle.getFluidUnits());
        }
        else
        {
            //need to remove from tree before changing amount so can be successfully found
            //and then re-add once updated
            set.remove(particle);
            particle.addFluid(fluidAmount);
            set.add(particle);
            Adversity.log.info("ParticleManager updated particle @" + particle.pos.toString() + " with amount=" + particle.getFluidUnits() + " added " + fluidAmount);
        }
        
     
        //TODO: remove for release
        if(map.size() != set.size())
        {
            Adversity.log.warn("Particle tracking error: set size does not match map size.");
        }
        
    }
    
    public EntityLavaParticle pollFirstEligible(LavaSimulator sim)
    {
        if(set.isEmpty()) return null;
        
        ParticleInfo result = this.set.first();

        // wait until full size or at least five ticks old
        if(result != null && (result.fluidUnits >= LavaCell.FLUID_UNITS_PER_BLOCK || sim.getTickIndex() - result.tickCreated > 4))
        {
            
            set.pollFirst();
            
            map.remove(result.pos);
         
            //TODO: remove for release
            if(map.size() != set.size())
            {
                Adversity.log.warn("Connection tracking error: set size does not match map size.");
            }
            
            Adversity.log.info("ParticleManager poll particle @" + result.pos.toString() + " with amount=" + result.getFluidUnits());
            
            return new EntityLavaParticle(sim.world, result.fluidUnits, new Vec3d(result.pos.getX() + 0.5, result.pos.getY() + 0.4, result.pos.getZ() + 0.5), Vec3d.ZERO);
        }
        else
        {
            return null;
        }
        
    }
    
    public static class ParticleInfo
    {
        public final int tickCreated;
        private int fluidUnits = 0;
        public final BlockPos pos;
        
        private ParticleInfo(int tickCreated, BlockPos pos, int fluidUnits)
        {
            this.tickCreated = tickCreated;
            this.pos = pos;
            this.fluidUnits = fluidUnits;
        }
        
        private void addFluid(int fluidUnitsIn)
        {
            this.fluidUnits += fluidUnitsIn;
        }
        
        public int getFluidUnits()
        {
            return this.fluidUnits;
        }
    }

}
